@ECHO OFF

SET JAVA_HOME=C:\Users\nobody\jdk-7
SET __JMX_HOST=127.0.0.1
SET __JMX_PORT=5053
SET __TARGET_UPD=127.0.0.1/8163
SET __NO_SSL=-Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
SET __SANDMAN_CFG=C:\Users\nobody\dev\things\docs\config-sandman.xml
SET __THINGS=C:\Users\nobody\dev\things\target\classes
SET __SNMP=C:\Users\nobody\.m2\repository\org\snmp4j\snmp4j\2.5.6\snmp4j-2.5.6.jar
SET __BOOTSTRAP=org.crypthing.things.appservice.Bootstrap
SET __STATUS=org.crypthing.things.appservice.Status
SET __SHUTDOWN=org.crypthing.things.appservice.Shutdown
SET __LAUNCH=org.crypthing.things.appservice.Launch

ECHO JAVA_HOME=%JAVA_HOME%
ECHO __JMX_HOST=%__JMX_HOST%
ECHO __JMX_PORT=%__JMX_PORT%
ECHO __TARGET_UPD=%__TARGET_UPD%
ECHO __NO_SSL=%__NO_SSL%
ECHO __SANDMAN_CFG=%__SANDMAN_CFG%
ECHO __THINGS=%__THINGS%
ECHO __SNMP=%__SNMP%
ECHO __BOOTSTRAP=%__BOOTSTRAP%
ECHO __STATUS=%__STATUS%
ECHO __SHUTDOWN=%__SHUTDOWN%
ECHO __LAUNCH=%__LAUNCH%