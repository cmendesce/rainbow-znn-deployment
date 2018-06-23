FROM openjdk:8-jre
VOLUME /tmp
ADD rainbow-znn-2.2-SNAPSHOT.jar app.jar
RUN bash -c 'touch /app.jar'
EXPOSE 8000
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n", "-jar","/app.jar"]