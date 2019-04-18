#!/usr/bin/env bash

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
echo "Now add the following environment variables to the Travis: https://travis-ci.com/OurClients/Contractors/settings"
echo
echo "    "$1"_key  =  "${NEW_TRAVIS_KEY}
echo "    "$1"_iv   =  "${NEW_TRAVIS_IV}
echo
echo "And adjust decryption command in .travis.yml:"
echo
echo "    openssl aes-256-cbc -K $"$1"_key -iv $"$1"_iv -in" $2".enc -out" $2 "-d"
echo
