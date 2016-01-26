FROM tomcat

COPY ./build/libs/hystrix-exploration-1.0.war $CATALINA_HOME/webapps/