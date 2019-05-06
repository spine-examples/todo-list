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
 - Install the `gcloud` CLI using this [instruction](https://cloud.google.com/sdk/docs/#install_the_latest_cloud_tools_version_cloudsdk_current_version).
 - Install AppEngine component: `gcloud components install app-engine-java`.
 - Install Datastore emulator component: `gcloud components install gcd-emulator`.

Emulating of the Firebase server relies on the [`firebase-server`](https://www.npmjs.com/package/firebase-server)
NPM library that must be installed globally. To install `firebase-server` globally run the following
command:
   ```bash
   npm install -g firebase-server
   ```

## Running the application locally

The application can be run locally by AppEngine, Datastore, and Firebase emulators. To run the
application do the following:

1. Assemble the application:
    ```bash
    ./gradlew clean assemble
    ```
    
2. Start the local AppEngine server:

    The following command runs the server on `localhost:8080`. It also runs
    the local Datastore emulator on `localhost:8081` and the local Firebase server
    on `localhost:8082`:
    ```bash
    ./gradlew appengineRun
    ```
 
After the command is executed, the To-Do List server is available on `localhost:8080`.
To debug the local server, create a new Remote configuration and run it in the debug mode.
The configuration should connect to `localhost:5005`.

#### Stopping the application
 
The local server can be stopped with `./gradlew appengineStop` command or just by terminating a
console process. When stopping the local server with a respective command, AppEngine, Datastore, and
Firebase emulators are stopped. When terminating a console process, Datastore and Firebase 
emulators stay serving.

If the command to run the application locally is executed when the Datastore emulator is
already running on the respective port, the Datastore emulator is reset.

## Application deployment

In order to run the application at AppEngine Standard Environment the Google Cloud project
and Firebase application must be initialized. This module is adjusted to work with "spine-dev"
GCloud and Firebase projects, the respective properties can be found in
`appengine-web/src/main/resources/config.properties` file. The application deployment process also
relies on the `appengine-web.xml` descriptor file replacement, descriptor replacement is described
in `appengine-web/scripts/appengine.gradle` script.
 
To use custom GCloud and Firebase projects, point them in the config properties file and adjust
the `appengine-web/deployment/dev/appengine-web.template.xml` respectively.

In order to deploy application manually, authenticate `gcloud` CLI and set current Google Cloud
project:

 - Login with your Google account: `gcloud auth login`.
 - Set the current project to work with: `gcloud config set project <your-project-name>`.

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
The application is automatically deployed to the development environment. The deployment is
performed by Travis CI after a merge into master branch using the common GCloud CLI tool. The GCloud
CLI is authenticated with a respective service account key for the "spine-dev" project.

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

The service account key is decrypted during the Travis CI build and stored under the
`deployment/appengine-web/src/main/resources/spine-dev.json` path.

#### Encrypt credentials for Travis
Travis `encrypt-file` command creates the same keys for multiple invocations. In order to create
multiple encrypted files use encrypt files with openssl:
```bash
./scripts/encrypt-file.sh secret_api secret.json
```
