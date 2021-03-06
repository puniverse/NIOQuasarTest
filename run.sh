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
    echo warm up the server ...
    go run ../gobench/gobench.go -k=false -u http://localhost:1234 -c 500 -t 10 > /dev/null
    echo launch the clients second time ...
    go run ../gobench/gobench.go -k=false -u http://localhost:1234 -c 10000 -t 60 >> res.log
    pkill -KILL -P $SERVER_PID
}

benchmark "runAsyncServerFJ"
#benchmark "runAsyncServerCached"
benchmark "runAsyncServerFixed"
benchmark "run1"
#benchmark "run2"
#benchmark "runAsyncServerExecutor"
#benchmark "runQuasar"
#benchmark "runQuasarTP"
#benchmark "runQuasarFJ"
#benchmark "runQuasarFJ2"
#benchmark "runQuasarFJ3"
#benchmark "runQuasarFJ4"
#benchmark "runQuasarIO1"
#benchmark "runQuasarIO2"
cat res.log

