import java.io.*;
import java.net.*;
/**
* A sample plugin that could handle php scripts.
* 
* @author Joel Peisley
* @version 1.0
* @since 14-11-2014
**/
public class php {
        /**
        * The constructor that will be called by the server.
        * If a plugin is created without this a fatal error will be thrown.
        *
        * @param s The request from the client.
        * @param pw The PrintWriter object which has the ability to 
        * send back messages to the client.
        * @param soc The client socket which should be closed when finished.
        **/
        public php(String[] s, PrintWriter pw, Socket soc){
                pw.println("HTTP/1.0 200 OK");
                pw.println("");
                pw.println("<!DOCTYPE html><html><head><title>PHP Plugin</title></head><body>"
                        +"<h1>PHP Plugin Response</h1><p>The PHP Plugin recieved this message:"
                        +"<pre>");
                int i = 0;
                while(i < s.length && s[i] != null){
                        pw.println(s[i]);
                        i++;
                }
                pw.println("</pre></body></html>");
                pw.println("");
                try{soc.close();}catch(IOException e){}
        }
}
