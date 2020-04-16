package com.hsj.sample.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author sergei
 * @create 2020-04-16
 */
public class Server {
    public static void main(String[] args) throws IOException, InterruptedException {
        ServerSocket server = new ServerSocket(8080);
        Socket socket = server.accept();
        BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String line = is.readLine();
        System.out.println("from client:"+line);

        PrintWriter pw = new PrintWriter(socket.getOutputStream());
        pw.println("data: "+line);
        pw.flush();
        pw.close();
        is.close();
        socket.close();
        server.close();
    }
}
