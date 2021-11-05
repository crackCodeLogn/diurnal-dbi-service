APP_NAME="diurnal-dbi-service"
APP_VERSION="0.0.5-SNAPSHOT"
JAVA_PARAM="-Xmx351m"
APP_PARAMS="-Dquarkus.profile=dev"

BIN_PATH=$PROM_HOME_PARENT/Diurnal/$APP_NAME/bin     #PROM-HOME-PARENT :: exported in .bashrc
JAR_PATH=$BIN_PATH/../target/quarkus-app/quarkus-run.jar

echo "Starting '$APP_NAME' with java param: '$JAVA_PARAM', app params: '$APP_PARAMS' at '$JAR_PATH'"
java $JAVA_PARAM $APP_PARAMS -jar $JAR_PATH
