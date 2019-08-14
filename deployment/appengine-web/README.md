## To-Do List Web Server

This module is an App Engine Standard Environment application serving the To-Do List
application. It depends on all the other modules in To-Do List application implementing the
[Spine Web](https://github.com/SpineEventEngine/web) server API.

The deployment uses the servlet-based Spine `web` API and its Firebase implementation.
The command and query endpoints are defined under the `/command` and `/query` paths
respectively. The endpoints for creation of subscriptions are `/subscription/create`, 
`/subscription/keep-up`, and `/subscription/cancel`.
 
## Running the application locally

The application can be run locally by AppEngine and Datastore emulators. To run the application
do the following:

1. Assemble the application:
    ```bash
    ./gradlew clean assemble
    ```
    
2. Start the local AppEngine server:

    The following command runs the server on `localhost:8080` and also runs
    the local Datastore emulator on `localhost:8081`:
    ```bash
    ./gradlew appengineRun
    ```
 
After the command is executed, the To-Do List server is available on `localhost:8080`.
To debug the local server, create a new Remote configuration and run it in the debug mode.
The configuration should connect to `localhost:5005`.

#### Stopping the application
 
The local server can be stopped with `./gradlew appengineStop` command or just by terminating a
console process. When stopping the local server with a respective command, both
AppEngine and Datastore emulators are stopped. When terminating a console process, the Datastore
emulator stays serving.

If the command to run the application locally is executed when the Datastore emulator is
already running on the respective port, the Datastore emulator is reset.

## Application deployment
The deployment uses the Firebase Realtime Database. It is required that the Firebase
credentials are available under the `appengine-web/src/main/resources/service-account.json`.

To deploy the application:

1. Assemble the application with a respective `buildProfile` parameter:
    ```bash
    ./gradlew clean assemble -PbuildProfile=[ENVIRONMENT]
    ```
    
    The `buildProfile` specifies the environment to build the application for
    and may have the following values:
    - dev - for Development environment;
    - local - for the local AppEngine server. This environment is the default and
     __should not__ be used for deployment;
2. Deploy the application using `appengineDeploy` task:
    ```bash
    ./gradlew appengineDeploy -PbuildProfile=[ENVIRONMENT]
    ```

## Automatic deployment
The application is also automatically deployed to the development environment. The deployment is
performed by Travis after a merge into master branch.

#### Credentials
Google Service Account with following roles must be provided in order for the application to be
deployed by Travis:
- App Engine Deployer
- App Engine Service Admin
- Storage Admin
- Cloud Datastore Index Admin
- Cloud Scheduler Admin
- Cloud Task Queue Admin

To generate a key for an existing service account execute the following `gcloud` command under
project root directory:
```bash
./gcloud iam service-accounts keys create deployment/appengine-web/src/main/resources/service-account.json --iam-account firebase-adminsdk-l4lav@spine-todo-list-example.iam.gserviceaccount.com
```

The same service account is used during the automatic deployment on Travis build. The service
account key is decrypted and stored under `deployment/appengine-web/src/main/resources/service-account.json`
path.

#### Encrypt credentials for Travis
Travis `encrypt-file` command creates the same keys for multiple invocations. In order to create
multiple encrypted files use encrypt files with openssl:
```bash
./scripts/encrypt-file.sh secret_api secret.json
```
