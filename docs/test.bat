@ECHO OFF

CALL env.bat
ECHO CHECKING STATUS...
SET __CMD_LINE=-cp %__SNMP%;%__THINGS% %__NO_SSL% %__STATUS% %__SANDMAN_CFG% 5 5000
ECHO __CMD_LINE=%__CMD_LINE%
%JAVA_HOME%\jre\bin\java.exe %__CMD_LINE%

ECHO SHUTING DOWN SANDMAN...
SET __CMD_LINE=-cp %__SNMP%;%__THINGS% %__NO_SSL% %__SHUTDOWN% --runner %__SANDMAN_CFG%
ECHO __CMD_LINE=%__CMD_LINE%
%JAVA_HOME%\jre\bin\java.exe %__CMD_LINE%
PAUSE

ECHO CHECKING STATUS...
SET __CMD_LINE=-cp %__SNMP%;%__THINGS% %__NO_SSL% %__STATUS% %__SANDMAN_CFG%
ECHO __CMD_LINE=%__CMD_LINE%
%JAVA_HOME%\jre\bin\java.exe %__CMD_LINE%
PAUSE

ECHO LAUCHING SANDMAN...
SET __CMD_LINE=-cp %__SNMP%;%__THINGS% %__NO_SSL% %__LAUNCH% --agent %__JMX_HOST% %__JMX_PORT% --runner %__SANDMAN_CFG%
ECHO __CMD_LINE=%__CMD_LINE%
%JAVA_HOME%\jre\bin\java.exe %__CMD_LINE%
PAUSE

ECHO CHECKING STATUS...
SET __CMD_LINE=-cp %__SNMP%;%__THINGS% %__NO_SSL% %__STATUS% %__SANDMAN_CFG% 5 5000
ECHO __CMD_LINE=%__CMD_LINE%
%JAVA_HOME%\jre\bin\java.exe %__CMD_LINE%
PAUSE

ECHO SHUTING DOWN ALL...
SET __CMD_LINE=-cp %__SNMP%;%__THINGS% %__NO_SSL% %__SHUTDOWN% --agent %__JMX_HOST% %__JMX_PORT% --runner %__SANDMAN_CFG%
ECHO __CMD_LINE=%__CMD_LINE%
%JAVA_HOME%\jre\bin\java.exe %__CMD_LINE%
PAUSE
%JAVA_HOME%\bin\jps.exe

::SET JAVA_HOME=
::SET __JMX_HOST=
::SET __JMX_PORT=
::SET __TARGET_UPD=
::SET __SANDMAN_CFG=
::SET __THINGS=
::SET __BOOTSTRAP=
::SET __STATUS=
::SET __SHUTDOWN=
::SET __LAUNCH=
::SET __SNMP=
::SET __NO_SSL=
