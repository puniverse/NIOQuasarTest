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
    echo >> res.log
    echo @@@ $1 >> res.log
    ./gradlew -q $1 > server.log 2>&1 &
    SERVER_PID=$!
    sleep 1
    while [ -z `grep -F started server.log | awk '{print $1}'` ]; do
        printf '.'
        sleep 1
    done
    echo  STARTED
    echo launch the clients...
    go run ../gobench/gobench.go -k=false -u http://localhost:1234 -c 500 -t 5 >> res.log
    echo launch the clients second time ...
    go run ../gobench/gobench.go -k=false -u http://localhost:1234 -c 500 -t 30 >> res.log
    pkill -KILL -P $SERVER_PID
}

benchmark "runAsyncServerCached"
benchmark "runQuasarIO3"
cat res.log

