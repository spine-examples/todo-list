/*
 * Copyright 2019, TeamDev. All rights reserved.
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

// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

/*
 * In development mode, to ignore zone related error stack frames such as
 * `zone.run`, `zoneDelegate.invokeTask` for easier debugging, you can
 * import the following file, but please comment it out in production mode
 * because it will have performance impact when throw error
 */
import 'zone.js/dist/zone-error'; // Included with Angular CLI.

/**
 * The default unnamed environment configuration.
 *
 * This configuration is used for the local development when executing Angular CLI `build`, `serve`,
 * and `test` commands without specifying of a `--configuration` property.
 *
 * Configures an application to work with:
 *  - a remote local backend server running on AppEngine emulator. See "Running the application
 *    locally" section of `deployment/appengine-web/README.md` for details.
 *  - a development Firebase application.
 */
export const environment = {
  production: false,
  firebaseConfig: {
    apiKey: 'AIzaSyBl2wQmozjqxLX7v9WT-_OjWAdUXs7f0Hg',
    authDomain: 'spine-todo-list-example.firebaseio.com',
    databaseURL: 'https://spine-todo-list-example.firebaseio.com',
    projectId: 'spine-todo-list-example',
    storageBucket: '',
    messagingSenderId: '297411113023'
  },
  host: 'http://localhost:8080'
};
