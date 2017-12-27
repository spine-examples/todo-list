# Local Firebase server

A local server which uses in-memory storage and `FirebaseSubscriptionMirror`.

### Preliminary configuration

Running the local Firebase server requires a `serviceAccount.json` configuration file to be present
in the server's classpath. The file contains sensitive information (such as RSA private key), thus
it should not be tracked by Git. Follow these steps to launch the server:
 - Go to the [Firebase Console](https://console.firebase.google.com) and select 
 the `spine-firestore-test` project.
 - Go to `Settings` > `Project settings` > `Service accounts` > `Firebase Admin SDK` and generate 
 new private key.
 - Put the downloaded file under the `local-firebase/src/main/resources`. The file name should be 
 `serviceAccount.json`. Please, ensure that the file is **not** added to Git.
 
After following these steps, start the server (from `LocalFirebaseServer` class).
