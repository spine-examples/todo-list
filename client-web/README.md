## `client-web`

Introducing the TodoList Web client.

Currently, the client is capable of creating and displaying simple tasks (i.e. tasks with 
a description only).

To run the client, open the `client-web/app/index.html` with the most convenient 
way (e.g. from IDEA).

### Prerequirements

In order to launch the Web application successfully
 - start the [`local-web`](../deployment/local-web/README.md) server;
 - if the server uses a custom Firebase project (not `spine-dev`), point the client to that project
 in `client-web/lib/firebase_client.js`;
 - build the `client-web` Gradle project with `./gradlew :client-web:build` (the general 
 `./gradlew build` works as well).
