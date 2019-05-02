## To-Do List Server for AppEngine Standard Environment

This module is an App Engine Standard Environment application serving the To-Do List
application. It depends on all the other modules in To-Do List application implementing the
[Spine Web](https://github.com/SpineEventEngine/web) server API.

The deployment uses the servlet-based Spine `web` API and its Firebase implementation.
The command and query endpoints are defined under the `/command` and `/query` paths
respectively. The endpoints for creation of subscriptions are `/subscription/create`, 
`/subscription/keep-up`, and `/subscription/cancel`.

## Prerequirements
The server application is running under [AppEngine](https://cloud.google.com/appengine/docs/standard/java/)
and the development relies on [gcloud](https://cloud.google.com/sdk/gcloud/) CLI tool. 

Install `gcloud` CLI and its components:
1. Install the gcloud CLI using this [instruction](https://cloud.google.com/sdk/docs/#install_the_latest_cloud_tools_version_cloudsdk_current_version).
2. Install AppEngine component: `gcloud components install app-engine-java`.
3. Install Datastore emulator component: `gcloud components install gcd-emulator`.

In order to deploy application manually, authenticate `gcloud` CLI and set current Google Cloud
project to `spine-dev`:  
4. Login with your Google account: `gcloud auth login`.
5. Set the current project to work with: `gcloud config set project spine-dev`.

## Credentials
In order to deploy application to the AppEngine Standard Environment or to run the AppEngine
emulator locally, the Google Cloud project must be initialized. This module is adjusted to work
with `spine-dev` project and requires the respective service account key.
 
Google Service Account with following roles must be provided:
- App Engine Deployer
- App Engine Service Admin
- Storage Admin
- Cloud Datastore Index Admin
- Cloud Scheduler Admin
- Cloud Task Queue Admin

To generate a key for an existing service account execute the following `gcloud` command under
project root directory:
```bash
./gcloud iam service-accounts keys create deployment/appengine-web/src/main/resources/spine-dev.json --iam-account firebase-adminsdk-c5bfw@spine-dev.iam.gserviceaccount.com
```
Note, that a service account key file __must not__ be stored in VCS.

The same service account is used during the automatic deployment on Travis build. The service
account key is decrypted and stored under `deployment/appengine-web/src/main/resources/spine-dev.json`
path.

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
credentials are available under the `appengine-web/src/main/resources/spine-dev.json`.

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

#### Encrypt credentials for Travis
Travis `encrypt-file` command creates the same keys for multiple invocations. In order to create
multiple encrypted files use encrypt files with openssl:
```bash
./scripts/encrypt-file.sh secret_api secret.json
```
