::Starts the local Firebase server emulator at the local port passed as a parameter.
::
::To start the emulator from the Gradle task, do the following:
::"./scripts/start-firebase-server.bat 8082".execute()
::
firebase-server -p %1 -e -b
