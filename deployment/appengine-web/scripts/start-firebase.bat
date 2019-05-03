:: Starts the local Firebase server emulator with REST API enabled.
::
:: Requires the following parameters:
::  1. The local port to run the emulator on;
::  2. The path to the file with Firebase Realtime Database security rules;
::  3. The path to the file to store the PID of the emulator;
::
:: To start the emulator from the Gradle task at the port 9999 with specific Firebase rules
:: and file to store the process ID, do the following:
::
:: "./scripts/start-firebase.bat 9999 ./my-firebase-rules.json ./firebase-emulator.pid".execute()
::
firebase-server --port %1 --rules %2 --rest --pid %3
