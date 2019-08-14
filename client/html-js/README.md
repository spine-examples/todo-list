## `html-js`

Introducing the TodoList Web client.

Currently, the client is capable of creating and displaying simple tasks (i.e. tasks with 
a description only).

To run the client, open the `/client/html-js/app/index.html` with the most convenient 
way (e.g. from IDEA).

### Prerequirements

In order to launch the Web application successfully
 - start the local [`appengine-web`](../../deployment/appengine-web/README.md) server;
 - if the server uses a custom Firebase project (not `spine-todo-list-example`), point the client 
 to that project in `client/html-js/lib/firebase_client.js`;
 - build the `html-js` Gradle project with `./gradlew :client:html-js:build` (the general 
 `./gradlew build` works as well).
