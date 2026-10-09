#!/bin/sh
# Builds olb.war and deploys it to a local Apache Tomcat 7 as the ROOT context.
# Requires: JDK 8 (javac -source 1.7), Maven 3, Tomcat 7.0.x at $CATALINA_HOME (default ~/tools/apache-tomcat-7.0.109).
set -e
export JAVA_HOME=${JAVA_HOME:-/usr/lib/jvm/java-8-openjdk-amd64}
CATALINA_HOME=${CATALINA_HOME:-$HOME/tools/apache-tomcat-7.0.109}
PORT=${PORT:-8080}
cd "$(dirname "$0")"
mvn -B -q package -DskipTests
rm -rf "$CATALINA_HOME/webapps/ROOT" "$CATALINA_HOME/webapps/ROOT.war"
cp target/olb.war "$CATALINA_HOME/webapps/ROOT.war"
sed -i "s/port=\"8080\"/port=\"$PORT\"/" "$CATALINA_HOME/conf/server.xml"
export CATALINA_OPTS="-Xmx256m -Duser.timezone=America/New_York"
exec "$CATALINA_HOME/bin/catalina.sh" run
