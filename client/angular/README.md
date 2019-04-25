## Angular Client for To-Do List

A client for To-Do List application based on [Angular](https://angular.io/) version 7 and
[Angular Material](https://material.angular.io/). Represents a material-styled web application    
and interacts with a Spine-based backend server. The client-server interaction is carried out by
[spine-web](https://www.npmjs.com/package/spine-web) library.

## Prerequirements

The client application is based on Angular 7+. So, the development relies on `npm` and Angular CLI.
First, make sure that Node.js is installed (it also includes npm). See the
[Angular Getting started](https://angular.io/guide/quickstart) section for the respective installation instructions.

## Environment configuration
The Angular client supports different named build configurations, such as 'local' and 'production',
with different defaults. See ["Configuring application environments"](https://angular.io/guide/build#configuring-application-environments)
on how the Angular environment file replacement works.

The environment configurations are stored in `./client/angular/src/environments` directory.
Each environment configuration specifies the backend server URL (remote or local) and a Firebase
application configuration to interact with.

The Angular CLI `build`, `serve`, and `test` commands accept environment name as a `--configuration` parameter
and then replace files with appropriate versions for your intended target environment.

## Build
Run `ng build` from `./client/angular` root to build the project. The build artifacts will be stored
in the `dist/` directory. 

## Running unit tests
Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running the application locally

1. Start the local AppEngine backend server (from project root):
    ```bash
    ./gradlew appengineRun
    ```
    The local backend server will start on `localhost:8080`. See "Running the application locally" section
    of `appengine-web/README.md` for details. 
    
2. Run the client (in separate terminal):
    ```bash
    cd client/angular
    npm run ng serve 
    # or just `ng serve` if you have Angular CLI installed.
    ```
    
    This command will run the web application on `localhost:4200` and adjust it to work
    with a backend server on the `localhost:8080` and a development "spine-dev" Firebase application.
    
3. Navigate to `localhost:4200`, the app will automatically reload if you change any of the source files.
