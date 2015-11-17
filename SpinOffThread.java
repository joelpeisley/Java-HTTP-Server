// Imports
import java.io.*;
import java.net.*;
import java.util.*;

/**
* This class creates a thread to help the ServerObject.
* 
* @see JavaServer
* @author Joel Peisley
* @since 14-11-2014
* @version 1.0
**/
public class SpinOffThread implements Runnable{
        
        //Create an object to handle the I/O
        private FileIO fio;
        //The server object itself
        private JavaServer js;
        //The name of the thread
        private String name;
        //The logs directory
        private String LOGS;

        /**
        * The default constructor that will initialize the thread.
        *
        * @param j The JavaServer Object that contains a running server.
        * @param n The name of the thread.
        * @param lo The logs directory.
        **/
        public SpinOffThread(JavaServer j, String n, String lo){
                js = j;
                name = n;
                fio = new FileIO();
                LOGS = lo;
        }

        /**
        * The main function that all threads will run.
        **/
        public void run(){
                fio.log(LOGS+"threads.log", name+": Waiting for connection");
      		js.waitForConnection();
                fio.log(LOGS+"threads.log", name+": Close Connection. Thread Ending.");
		return;
        }
}
