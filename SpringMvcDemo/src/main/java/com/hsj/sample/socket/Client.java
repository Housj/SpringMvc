package com.hsj.sample.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author sergei
 * @create 2020-04-16
 */
public class Client {
    public static void main(String[] args) throws IOException, InterruptedException {
        String msg = "Client Data";
        Socket socket = new Socket("127.0.0.1",8080);
        PrintWriter pw = new PrintWriter(socket.getOutputStream());
        pw.print(msg);
        pw.flush();

        BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String line = is.readLine();
        System.out.println("from server: "+line);
        pw.close();
        is.close();
        socket.close();
    }
}
