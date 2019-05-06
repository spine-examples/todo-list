::Starts the local Google Datastore emulator at the port passed as a parameter.
::
::To start the emulator from the Gradle task, do the following:
::"./scripts/start-datastore.bat 8081".execute()
::
gcloud beta emulators datastore start --host-port=localhost:%1 --consistency 1.0 --no-store-on-disk
