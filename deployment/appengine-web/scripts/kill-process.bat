:: Kills the process by the specified PID.
::
:: To kill the process by PID do the following:
::
:: "./scripts/kill-process.bat 12345".execute()
::
taskkill /f /pid %1
