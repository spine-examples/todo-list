#!/usr/bin/env bash

#
# Copyright 2021, TeamDev. All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Redistribution and use in source and/or binary forms, with or without
# modification, must retain the above copyright notice and the following
# disclaimer.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
# "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
# LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
# A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
# OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
# SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
# LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
# DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
# THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#

#
# Encrypts the file and prints the instruction on updating Travis build configuration.
# This script requires 2 parameters:
# 1) Travis environment variable name.
# 2) The file to encrypt.
#

if [ "$#" -ne 2 ]; then
    echo
    echo "Error: This script requires 2 parameters:"
    echo "1) Travis environment variable name."
    echo "2) The file to encrypt."
    echo
    exit 1
fi

if [[ "$1" =~ [^a-zA-Z0-9_] ]]; then
    echo
    echo "Error: The first argument is Travis environment variable name."
    echo "It must contain only alphanumeric characters and \"_\"."
    echo
    exit 1
fi

# Create temporary file.
tmp_secrets=$(mktemp)

# Delete temporary file when application exits. 
trap "rm -f $tmp_secrets" 0 2 3 15

# Create key and iv. 
openssl aes-256-cbc -k $(openssl rand -base64 32) -P -md sha1 > $tmp_secrets
NEW_TRAVIS_KEY=$(less $tmp_secrets | grep "key=" | cut -d'=' -f 2)
NEW_TRAVIS_IV=$(less $tmp_secrets | grep "iv =" | cut -d'=' -f 2)

# Encrypt file. 
openssl aes-256-cbc -K ${NEW_TRAVIS_KEY} -iv ${NEW_TRAVIS_IV} -in "$2" -out "$2".enc

# Print instructions.
echo
echo "Now add the following environment variables to the Travis: https://travis-ci.com/SpineEventEngine/todo-list/settings"
echo
echo "    "$1"_key  =  "${NEW_TRAVIS_KEY}
echo "    "$1"_iv   =  "${NEW_TRAVIS_IV}
echo
echo "And adjust decryption command in .travis.yml:"
echo
echo "    openssl aes-256-cbc -K $"$1"_key -iv $"$1"_iv -in" $2".enc -out" $2 "-d"
echo
