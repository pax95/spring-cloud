FROM java:8-jre
MAINTAINER Preben Asmussen <preben.asmussen@gmail.com>
ADD target/play-client-0.0.1-SNAPSHOT.jar app.jar
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
EXPOSE 9000
