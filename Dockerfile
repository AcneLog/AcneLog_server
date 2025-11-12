FROM amazoncorretto:17-alpine
ARG JAR_FILE=./api-module/build/libs/api-module-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["sh", "-c", "java -Duser.timezone=Asia/Seoul -jar /app.jar"]