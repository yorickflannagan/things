@ECHO OFF

CALL env.bat
SET __CMD_LINE=-cp %__SNMP%;%__THINGS% %__NO_SSL% -Djava.rmi.server.hostname=%__JMX_HOST% -Dcom.sun.management.jmxremote.port=%__JMX_PORT% %__BOOTSTRAP% %__TARGET_UPD% %__SANDMAN_CFG%
ECHO __CMD_LINE=%__CMD_LINE%
%JAVA_HOME%\jre\bin\java.exe %__CMD_LINE%
