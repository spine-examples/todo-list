#!/usr/bin/env bash
#
# Starts the local Google Datastore emulator at the port passed as a parameter.
#
# To start the emulator from the Gradle task, do the following:
# "./scripts/start-datastore.sh spine-dev 8081".execute()
#
gcloud beta emulators datastore start --project=$1 --host-port=localhost:$2 --consistency 1.0 --no-store-on-disk
