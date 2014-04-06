package main

import (
        "net"
        "os"
        "runtime"
)

const (
        RECV_BUF_LEN = 1024
)

func main() {
        runtime.GOMAXPROCS(runtime.NumCPU())
        println("Starting the server")

        listener, err := net.Listen("tcp", "0.0.0.0:1234")
        if err != nil {
                println("Error Listen:", err.Error())
                os.Exit(1)
        }

        for {
                conn, err := listener.Accept()
                if err != nil {
                        println("Error Accept:", err.Error())
                        return
                }
                go Handler(conn)
        }
}

func Handler(conn net.Conn) {
        defer conn.Close()
        buf := make([]byte, RECV_BUF_LEN)
        _, err := conn.Read(buf)
        if err != nil {
                println("Error Read:", err.Error())
                return
        }

        _, err = conn.Write([]byte("HTTP/1.0 200 OK\r\nDate: Fri, 31 Dec 1999 23:59:59 GMT\r\nContent-Type: text/html\r\nContent-Length: 0\r\n\r\n"))
        if err != nil {
                println("Error Write:", err.Error())
        }
}