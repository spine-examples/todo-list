## Web Client for To-Do List

A Web client of the To-Do List application. Represents a minimal example of a web application
interacting with a Spine-based server. Implemented without UI frameworks and built with Webpack.
The client-server interaction is carried out by [spine-web](https://www.npmjs.com/package/spine-web) library.

Currently, the client is capable of creating and displaying simple tasks (i.e. tasks with 
a description only).

### Prerequirements

In order to launch the Web application successfully:
 - make sure that Node.js is installed (it also includes npm);
 - if the server uses a custom Firebase project (not `spine-dev`), point the client to that project
 in `client/web/lib/firebase_client.js`;
 
## Running the application

1. Start the local AppEngine backend server (from project root):
    ```bash
    ./gradlew :appengine-web:appengineRun
    ```
    The local backend server will start on `localhost:8080`. For details about running the
    application server locally see `deeployment/appengine-web/README.md`. 
2. Build the application with `./gradlew :client:web:build` command. It will install all module
   dependencies, generate the Protobuf sources and assemble the application with Webpack. The general
   `./gradlew build` for the root project works as well.
3. To run the client, open the `client/web/app/index.html` with the most convenient 
   way (e.g. from IDEA).
    
   The application is adjusted to work with a backend server on the `localhost:8080` and a
   development "spine-dev" Firebase application.
