# Local Firebase server

A local server which uses in-memory storage and `FirebaseSubscriptionMirror`.

### Description

This server configuration uses the in-memory storage. The difference between this config and 
the [`local-inmem`](../local-inmem/) is in the fact that this configuration uses Cloud Firestore
database to post the entity state updates to the clients.

Note that the `SubscriptionService` is deployed as well for consistency. However, in a real-life 
system, it would be better not to deploy the `SubscriptionService` gRPC service, but use only 
the subscription mirror instead.

See [the Firebase subscription mirror](../../firebase-mirror/) for more details on the subscription 
mirror usage.

### Preliminary configuration

Running the local Firebase server requires a `serviceAccount.json` configuration file to be present
in the server's classpath. The file contains sensitive information (such as RSA private key), thus
it should not be tracked by the VCS. Follow these steps to launch the server:
 - Go to the [Firebase Console](https://console.firebase.google.com) and select 
 the `spine-firestore-test` project.
 - Go to `Settings` > `Project settings` > `Service accounts` > `Firebase Admin SDK` and generate 
 new private key.
 - Put the downloaded file under the `local-firebase/src/main/resources`. The file name should be 
 `serviceAccount.json`. Please, ensure that the file is **not** added to the VCS.
 
After following these steps, start the server (from `LocalFirebaseServer` class).
