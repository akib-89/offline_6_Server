package com.akib;

import data.Loader;

import java.io.IOException;
import java.net.ServerSocket;

public class Main {

    public static void main(String[] args) {
	// write your code here
        Loader.getInstance().read();
        try(ServerSocket server = new ServerSocket(60000)){
            while(true){
                new NetThread(server.accept()).start();
            }
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
}
