#install OS
FROM centos
#install java
RUN yum install -y java
#make directory structure to store temporary files
RUN mkdir -p /store
#put jar into container
ADD target/my-app-1.7-SNAPSHOT.jar my-app.jar
#run jar
ENTRYPOINT ["java", "-jar", "/my-app.jar"]