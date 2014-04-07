#!/bin/sh -vx
echo start > res.log

echo quasar-non-fj >> res.log
./gradlew -q runServer > server.log &
SERVER_PID=$!
while [ ! `grep -Fp "started" server.log` ]; do
    sleep 1
done
go run ../gobench.go -k=false -u http://localhost:1234 -c 500 -t 10 >> res.log
kill -9 $SERVER_PID

echo quasar-fj >> res.log
./gradlew -q runServerFJ > server.log &
SERVER_PID=$!
while [ ! `grep -Fp "started" server.log` ]; do
    sleep 1
done
go run ../gobench.go -k=false -u http://localhost:1234 -c 500 -t 10 >> res.log
kill -9 $SERVER_PID

echo runAsyncServer >> res.log
./gradlew -q runAsyncServer > server.log &
SERVER_PID=$!
while [ ! `grep -Fp "started" server.log` ]; do
    sleep 1
done
go run ../gobench.go -k=false -u http://localhost:1234 -c 500 -t 10 >> res.log
kill -9 $SERVER_PID

cat res.log