:: Starts the local Firebase server emulator with enabled REST API. Starts at the given local port
:: from the first parameter, applying Firebase Realtime Database rules taken from the path, passed
:: as a second parameter.
::
:: To start the emulator from the Gradle task at port 9999 with Firebase rules form
:: `todo-list/example-firebase-rules.json`, do the following:
::
:: "./scripts/start-firebase-server.bat 9999 ./example-firebase-rules.json".execute()
::
firebase-server --port %1 --rules %2 --rest
