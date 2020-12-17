/*
 * Copyright 2020, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * The environment configuration for the production.
 *
 * Configures an application to work with:
 *  - a remote development backend server deployed to the AppEngine Standard environment.
 *    See `deployment/appengine-web/README.md` for details.
 *  - a development Firebase application.
 *
 * Note, that assembling of the production version is done using "AOT" compiler.
 * See [The Ahead-of-Time (AOT) compiler](https://angular.io/guide/aot-compiler) for details.
 */
export const environment = {
  production: true,
  firebaseConfig: {
    apiKey: 'AIzaSyBl2wQmozjqxLX7v9WT-_OjWAdUXs7f0Hg',
    authDomain: 'spine-todo-list-example.firebaseio.com',
    databaseURL: 'https://spine-todo-list-example.firebaseio.com',
    projectId: 'spine-todo-list-example',
    storageBucket: '',
    messagingSenderId: '297411113023'
  },
  host: 'https://spine-todo-list-example.appspot.com'
};
