FROM jdk-11.0.11_9-alpine
COPY build/libs/github-activities-0.0.1-SNAPSHOT.jar /app.jar
ENTRYPOINT ["java", "-jar", "-Duser.timezone=Asia/Phnom_Penh", "/app.jar"]