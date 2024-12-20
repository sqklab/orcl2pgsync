FROM centos:latest
#RUN yum update -y
#RUN yum install httpd httpd-tools -y
#RUN yum install epel-release -y \
#    && yum update -y \
#    && yum install htop -y \
#    && yum install unzip -y \

RUN curl -o kafka_2.12-3.2.1.tgz 'https://downloads.apache.org/kafka/3.2.1/kafka_2.12-3.2.1.tgz'
RUN curl -o debezium-connector-postgres.tar.gz https://repo1.maven.org/maven2/io/debezium/debezium-connector-postgres/1.9.5.Final/debezium-connector-postgres-1.9.5.Final-plugin.tar.gz

RUN tar -xf kafka_2.12-3.2.1.tgz
RUN mkdir kafka_2.12-3.2.1/plugin

RUN tar -xf debezium-connector-postgres.tar.gz
RUN mv debezium-connector-postgres kafka_2.12-3.2.1/plugin

#RUN yum install java-11-openjdk
#
#RUN java -version
RUN cd /etc/yum.repos.d/
RUN sed -i 's/mirrorlist/#mirrorlist/g' /etc/yum.repos.d/CentOS-*
RUN sed -i 's|#baseurl=http://mirror.centos.org|baseurl=http://vault.centos.org|g' /etc/yum.repos.d/CentOS-*

RUN yum -y install java

WORKDIR kafka_2.12-3.2.1

RUN echo 'plugin.path=./plugin/' &>> config/connect-distributed.properties

COPY kafka.sh .
COPY create-connector.sh .
COPY create-secret.sh .
COPY connector.json .

#https://stackoverflow.com/questions/62715790/creating-kafka-connector-after-distributed-kafka-connect-is-started-in-docker-en
RUN chmod +x kafka.sh
RUN chmod +x create-connector.sh
RUN chmod +x create-secret.sh

ENTRYPOINT ["/bin/bash", "kafka.sh"]
