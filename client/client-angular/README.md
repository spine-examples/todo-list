# Angular Client for To-Do List

A client for To-Do List application based on [Angular](https://angular.io/) version 7.

The client connects to the local backend server.

How to run:

1. Start the local AppEngine backend server (from project root):
    ```bash
    ./gradlew :appengine-web:appengineRun
    ```
    The local backend server will start on `localhost:8080`.
    
2. Run the client (in separate terminal):
    ```bash
    cd client-angular
    npm run ng serve 
    # or just `ng serve` if you have Angular CLI installed.
    ```
    
    It will run the web application with a default environment. The default environment
    configures web application to work with a backend server on the `localhost:8080` and a
    development Firebase application.
    
3. Navigate to `localhost:4200`.
