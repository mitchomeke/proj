# base ubuntu official image 
FROM ubuntu

# run a command (install a package)
#RUN apt-get update && apt-get install iproute2 -y
RUN apt-get clean && apt-get update --fix-missing && apt-get install -y iproute2

# Copy openjdk 17 from another image
ENV JAVA_HOME=/opt/java/openjdk
COPY --from=eclipse-temurin:17 $JAVA_HOME $JAVA_HOME
ENV PATH=$PATH:$JAVA_HOME/bin

# working directory inside docker image
WORKDIR /home/sd

# copy hibernate config
COPY hibernate.cfg.xml hibernate.cfg.xml

COPY messages.props messages.props

# copy an example image for experiments
COPY example.png example.png
ADD hibernate.cfg.xml .
ADD messages.props .
# copy the jar created by assembly to the docker image
COPY target/sd2526-tp1-1.jar sd2526.jar

# run Discovery when starting the docker image
#CMD ["java", "-cp", "sd2526.jar", "lab5.server.rest.RestUsersServer"]
