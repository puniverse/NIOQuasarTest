#!/bin/sh
echo start > res.log

ulimit -n 200000

if [ `netstat -ano|grep 1234 |grep LISTEN|awk '{print $2}'` ]
then
    echo port 1234 is in use
    exit
fi

benchmark()
{
    echo running $1 configuration
    echo $1 >> res.log
    ./gradlew -q $1 > server.log &
    sleep 1
    SERVER_PID=$!
    echo kkkk $SERVER_PID
    while [ -z `grep -F "started" server.log` ]; do
        echo -n .
        sleep 1
    done
    echo  STARTED
    echo launch the clients...
    go run ../gobench/gobench.go -k=false -u http://localhost:1234 -c 500 -t 10 >> res.log
    pkill -SIGINT -P $SERVER_PID
}

benchmark "runServer" 
benchmark "runServerFJ"
benchmark "runAsyncServer"
cat res.log

