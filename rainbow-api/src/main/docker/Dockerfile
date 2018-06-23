FROM openjdk:8-jre
VOLUME /tmp
ADD rainbow-api-1.0-SNAPSHOT.jar app.jar
RUN bash -c 'touch /app.jar'
EXPOSE 8000
#ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-classpath '.:lib/*'", "-Drainbow.target=znn", "org.sa.rainbow.core.RainbowDelegate"]
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n", "-jar","/app.jar"]