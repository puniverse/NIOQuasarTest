#scp to the server, chmod+x and run

# install jdk8
sudo apt-get update
wget --no-cookies --no-check-certificate --header "Cookie: gpw_e24=http%3A%2F%2Fwww.oracle.com%2F; oraclelicense=accept-securebackup-cookie" "http://download.oracle.com/otn-pub/java/jdk/8-b132/jdk-8-linux-x64.tar.gz"
sudo mkdir /usr/local/java
sudo mv jdk-8-linux-x64.tar.gz /usr/local/java
cd /usr/local/java
sudo tar zxvf jdk-8-linux-x64.tar.gz
cd /usr/bin/
sudo ln -s  /usr/local/java/jdk1.8.0/bin/java .
sudo ln -s  /usr/local/java/jdk1.8.0/bin/javac .

#install git
sudo apt-get -y install git-core
cd

#project specific scripts
git clone https://github.com/puniverse/NIOQuasarTest.git
NIOQuasarTest/postinstall.sh