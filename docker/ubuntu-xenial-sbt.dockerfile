FROM ubuntu:xenial
WORKDIR /root
ADD deb /deb
RUN ["apt-get", "update"]
RUN dpkg -i /deb/scala-2.12.2.deb || dpkg -i /deb/sbt-0.13.15.deb || apt-get -y -f install
RUN ["sbt", "sbtVersion"]