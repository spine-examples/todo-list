## `appengine-web`

Introducing the `web` deployment configuration.

The deployment uses the servlet-based Spine `web` API and its Firebase implementation. The command and query endpoints are defined under the `/command` and `/query` paths respectively.

The `local-web` module uses the Gretty plugin to run locally. Run `./gradlew :local-web:appRun` to start the server.

### Prerequirements

The deployment uses the Firebase Realtime Database. It is required that the Firebase credentials are available under the `local-web/src/main/resources/spine-dev.json`.

To use a custom Firebase project (not `spine-dev`) put the credentials into the module classpath end edit the `io.spine.examples.todolist.server.FirebaseCredentials` class accordingly.
