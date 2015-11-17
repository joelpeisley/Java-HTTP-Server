import java.io.*;
import java.net.*;
/**
* A sample plugin that could handle perl scripts.
* 
* @author Joel Peisley
* @version 1.0
* @since 14-11-2014
**/
public class pl {
        /**
        * The constructor that will be called by the server.
        * If a plugin is created without this a fatal error will be thrown.
        *
        * @param s The request from the client.
        * @param pw The PrintWriter object which has the ability to 
        * send back messages to the client.
        * @param soc The client socket which should be closed when finished.
        **/
        public pl(String[] s, PrintWriter pw, Socket soc){
                pw.println("HTTP/1.0 200 OK");
                pw.println("The 'pl' Plugin is working!");
                pw.println("");
                try{soc.close();}catch(IOException e){}
        }
}
