# Local Firebase server

A local web server which uses in-memory storage and Firebase Realtime Database emulator.

### Prerequisites

Make sure to install the Firebase Database emulator. See the [official docs](https://firebase.google.com/docs/emulator-suite/install_and_configure#security_rules_configuration) on the installation and configuration.

If you are transitioning from the older versions of this Spine example project, use this
[migration guide](https://github.com/urish/firebase-server/blob/master/docs/firebase-emulators.md).

### Running the server
The server can be run with the command:
```bash
./gradlew :local-firebase:runServer
```

The command will start a local Firebase emulator, and a local Spine web server at the address 
`localhost:8080`.

After that, the [`html-js`](../../client/html-js) or [`angular`](../../client/angular) client can 
be used to connect to the server via HTTP and run commands and queries related to the To-Do List 
tasks.

No additional configuration is required.

#### Server Networking Errors

Sometimes, the server prints errors caused by invalid HTTP responses from the Firebase emulator.
This is an issue with the emulator itself, not with the client. Such errors are not reproducible on
real-life instances of Firebase.
