FROM openjdk:8-jre
VOLUME /tmp
ADD rainbow-znn-2.2-SNAPSHOT.jar app.jar
RUN bash -c 'touch /app.jar'
#ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-classpath '.:lib/*'", "-Drainbow.target=znn", "org.sa.rainbow.core.RainbowDelegate"]
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]