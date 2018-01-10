# Todo List Android Client

This module contains the Todo List Android client application.

### Building and Running

Provided the required Android specific development 
[environment](https://developer.android.com/studio/index.html), follow these steps to build 
the application:

 1. Publish the required modules of Todo List core to the Maven local. To do that, execute the 
 `publishToMavenLocal` Gradle task on the [core Gradle project](../).
 2. Go to [Firebase console](https://console.firebase.google.com) > Select `spine-firestore-dev`
 project > `Settings` > `Project settings` > `General` > `Your apps`. Download
 the `google-services.json` for the Todo List Android app and place the file under `client-android/app`.
 3. Build the `client-android` module with `./gradlew clean build`.
 
To run the application, open the `client-android` module in the Android Studio and launch the app with
the default application run configuration.

Alternatively, build the app as described above, go to 
[`./app/build/outputs/apk/debug/app-debug.apk`](./app/build/outputs/apk/debug/app-debug.apk), copy 
the `.apk` file to an Android device and install and start the application.

### Connection to the Server

By default, the application connects to the `localhost` server started on the port `50051`.
Override this behavior by changing the constant values in 
[`Clients.java`](./app/src/main/java/io/spine/examples/todolist/connection/Clients.java).
