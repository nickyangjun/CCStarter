FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY build/app.jar /app/app.jar

EXPOSE 18080

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
