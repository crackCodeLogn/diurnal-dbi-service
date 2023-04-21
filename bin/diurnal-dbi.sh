if [[ $# -lt 5 ]]; then
  echo "Incomplete args to start diurnal dbi"
  exit
fi

APP_NAME="diurnal-dbi-service"
APP_VERSION="0.0.5-SNAPSHOT"
JAVA_PARAM="-Xmx351m"
APP_PARAMS="-Dquarkus.profile=dev"

JAR_PATH=""
if [[ $1 == "v2" ]]; then
  echo Using "$PROM_HOME_PARENT"
  BIN_PATH=$PROM_HOME_PARENT/Diurnal/$APP_NAME/bin     #PROM-HOME-PARENT :: exported in .bashrc
  JAR_PATH=$BIN_PATH/../target/quarkus-app/quarkus-run.jar
else
  JAR_PATH=$HOME/diurnal-dbi/quarkus-run.jar
  APP_PARAMS=$APP_PARAMS" -DSERVER.PORT=8080 -DDBI_USER_CRED=$2 -DDBI_ACCESS_GH_TOKEN=$3 -DDBI_ACCESS_GH_REPO=$4 -DDBI_LIMIT_PERIOD_DAYS_CLOUD_EXEMPTION_EMAILS=example@sample.com"
  echo $APP_PARAMS
fi

echo "Starting '$APP_NAME' with java param: '$JAVA_PARAM', app params: '$APP_PARAMS' at '$JAR_PATH'"
java $JAVA_PARAM $APP_PARAMS -jar $JAR_PATH
