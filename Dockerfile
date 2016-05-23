FROM codenvy/ubuntu_jdk8

ENV MAVEN_VERSION 3.3.9

WORKDIR "/home/user"
ADD pom.xml /home/user/pom.xml
RUN ["mvn", "dependency:resolve"];

ADD src /home/user/src
RUN ["mvn", "package"]

EXPOSE 8080

CMD ["java", "-jar", "target/AuthService-1.0-SNAPSHOT.jar"]  
