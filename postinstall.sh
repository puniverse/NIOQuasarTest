git clone https://github.com/cmpxchg16/gobench.git
sudo apt-get -y install golang
cat > sysctl.conf <<EOF
net.ipv4.tcp_tw_recycle = 1
net.ipv4.tcp_tw_reuse = 1
net.ipv4.tcp_fin_timeout = 1
net.ipv4.tcp_timestamps = 1
net.ipv4.tcp_syncookies = 0
net.ipv4.ip_local_port_range = 1024 65535
EOF
sudo sh -c "cat sysctl.conf >> /etc/sysctl.conf" 
sudo sysctl -p /etc/sysctl.conf
cat <<EOF
USAGE: 
$ sudo bash
$ ulimit -n 2000000

For quasar server
$ cd NIOQuasarTest
$ ./gradlew

For async java server
$ cd NIOQuasarTest
$ ./gradlew runAsyncServer

For client
$ cd gobench
$ go run gobench.go  -k=false -u http://localhost:1234 -c 500 -t 10

Enjoy
EOF
cd NIOQuasarTest
sudo bash -c "./run.sh"
