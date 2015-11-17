// Imports
import java.net.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.lang.reflect.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class JavaServer {

        private static String[] message; //Holds the entire message from client
        private static FileIO fio; //The file IO object
        private static ServerSocket ss; //What the server binds to.
        private static PrintWriter textOut; //Allows to send text to the user.
        private static DataOutputStream dataOut; //Allows to send data to the user.
        private static int requestSize; //The request size (bytes).
        private static String filePath; //Holders for the file information
        private static String fileName;
        private static String fileType;
        private static String LOGS; //Holders for the values defined in config.xml
        private static String DEFAULT_FILE;
        private static String FILE_DIR;
        private static String[] DYNAMIC_FILETYPES;
        private static Socket client; //The socket the client connects to
        private static String METHOD; //The method the user is using.
        
        /**
        * This function checks for redirection defined in redirect.xml
        **/
        private static void checkForRedirect(){
                try {
                        //The redirect file
	                File fXmlFile = new File("redirect.xml");
	                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	                Document doc = dBuilder.parse(fXmlFile);

	                doc.getDocumentElement().normalize();
                        //The list of redirect elements
	                NodeList nList = doc.getElementsByTagName("redirect");
                        
	                for (int temp = 0; temp < nList.getLength(); temp++) {
 
	                	Node nNode = nList.item(temp);
 
	                	if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 
	                		Element eElement = (Element) nNode;
                                        //If file is in the redirect file
			                if(filePath.equals(eElement.getElementsByTagName("file").item(0).getTextContent())){
			                        String code = "";
			                        //If code is 301
			                        if(eElement.getElementsByTagName("code").item(0).getTextContent().equals("301")){
			                                code = "HTTP/1.0 301 Moved Permanently";
			                        }
			                        //if code is 302
			                        if(eElement.getElementsByTagName("code").item(0).getTextContent().equals("302")){
			                                code = "HTTP/1.0 302 Moved Temporarily";
			                        }
			                        String send = "Location: " + 
			                                eElement.getElementsByTagName("location").item(0).getTextContent();
			                        textOut.println(code); //send code information to client
			                        textOut.println(send); //Send location to client
			                        client.close(); //close the connection
			                        return; //end the function
			                        
			                }
		                }//end if
	                }//end for
	                //Send error 404, file is not found
	                textOut.println("HTTP/1.0 404 File Not Found");
	                textOut.println("");
	                client.close(); //close the connection
	                return; //end the function
	                
                } catch (Exception e) {
	                System.out.println("Check for redirect error! "+e);
                }
        }

        /**
        * This function will read the 'Stats.txt' file and generate a HTML page from the results.
        * This can be used to see which browser is popular from unique IPs.
        **/
	public static synchronized void generateHTML(){
		LinkedList<String> ips = new LinkedList<String>(); //List of IPs
		LinkedList<String> file = new LinkedList<String>(); //List of access attempts
		file = fio.readInLinesLinked("Stats.txt"); //read in the Stats.txt file
		int mozilla = 0; //Holders for the individual browser counts
		int safari = 0;
		int chrome = 0;
		int ie = 0;
		int webkit = 0;
		int unknown = 0;
		String browser = null;
		
		//loop while access attempts remain
		for (int i = 0; i < file.size(); i++) {
			StringTokenizer st = new StringTokenizer(file.get(i), "!"); //the '!' token is used to seperate the data
			while(st.hasMoreTokens()){
			        //Skip the first token
				String ip = st.nextToken();
				//Store the next token
				ip = st.nextToken();
				//Check for unique IP
				if(! ips.contains(ip)){
					ips.add(ip); //Add ip to ip list
					browser = st.nextToken(); //store the next token for comparing
					if(findWithinString(browser) == 1){
						webkit++;
					} else if(findWithinString(browser) == 2){
					        chrome++;
					} else if(findWithinString(browser) == 3){
					        safari++;
					} else if(findWithinString(browser) == 4){
					        ie++;
					} else if(findWithinString(browser) == 5){
					        mozilla++;
					} else {
					        unknown++;//keeps track of unknown browsers
					}
					
				} else {
					ip = st.nextToken();//skips next token if IP is not unique
				}
				
			}//end while
		}//end while
		//Calculate the total hits
		float total = (float)ie +(float)webkit + (float)mozilla + (float)safari + (float)chrome + (float)unknown;
		//Store the HTML and results
		String html = "<!DOCTYPE><html><head></head><body>"+
				"<h1>Browser Rankings</h1>"+
				"<h2>Android Webkit: "+webkit+" hits ("+Math.round(webkit/total*100)+"%)</h2>"+
				"<h2>Mozilla: "+mozilla+" hits ("+Math.round(mozilla/total*100)+"%)</h2>"+
				"<h2>Safari: "+safari+" hits ("+Math.round(safari/total*100)+"%)</h2>"+
				"<h2>Internet Explorer: "+ie+" hits ("+Math.round(ie/total*100)+"%)</h2>"+
				"<h2>Chrome: "+chrome+" hits ("+Math.round(chrome/total*100)+"%)</h2>"+
				"<h2>Unknown Browser: "+unknown+" hits ("+Math.round(unknown/total*100)+"%)</h2>"+
				"<h1>IP Addresses</h1>";
	        		//Loops and outputs all unique IP addresses
	        		for (int j = 0; j < ips.size(); j++) {
            				html = (html + ips.get(j)+"<br/>");
        			}
				html = html + "</body></html>";
	        //Write the HTML to file
		fio.writeOutBytes("files/Browsers.html", html.getBytes());
	}//end generateHTML
        
        /**
        * This function will look for browser specific traits within the 
        * User-Agent line of the message given.
        *
        * @param s The user-agent string.
        * @return 1 if Android, 2 if Chrome, 3 if safari, 4 if IE, 5 if firefox and -1 if unknown.
        **/
        public static int findWithinString(String s){
                //Defined traits to look for
                char[] firefox = {'F','i','r','e','f','o','x'};
                char[] safari = {'S','a','f','a','r','i'};
                char[] chrome = {'C','h','r','o','m','e'};
                char[] webkit = {'A','n','d','r','o','i','d'};
                char[] ie = {'T','r','i','d','e','n','t'};
                int i = 0;
                int result = 0;
                char[] temp;
                //Check for Android Trait
                temp = new char[webkit.length];
                while(i < s.length()){
                        result = s.indexOf(webkit[0],i);
                        if(result > 0){
                                s.getChars(result, (result+webkit.length), temp, 0);
                                if(Arrays.equals(temp,webkit)){
                                        return 1;
                                }//end if
                        }//end if
                        i++;
                }//end while                
                i = 0;
                result = 0;
                //Check for Chrome Trait
                temp = new char[chrome.length];
                while(i < s.length()){
                        result = s.indexOf(chrome[0],i);
                        if(result > 0){
                                s.getChars(result, (result+chrome.length), temp, 0);
                                if(Arrays.equals(temp, chrome)){
                                        return 2;
                                }//end if
                        }//end if
                        i++;
                }//end while                                  
                i = 0;
                result = 0;
                //Check for Safari Trait
                temp = new char[safari.length];
                while(i < s.length()){
                        result = s.indexOf(safari[0],i);
                        if(result > 0){
                                s.getChars(result, (result+safari.length), temp, 0);
                                if(Arrays.equals(temp, safari)){
                                        return 3;
                                }//end if
                        }//end if
                        i++;
                }//end while
                i = 0;
                result = 0;
                //Check for IE Trait
                temp = new char[ie.length];
                while(i < s.length()){
                        result = s.indexOf(ie[0],i);
                        if(result > 0){
                                s.getChars(result, (result+ie.length), temp, 0);
                                if(Arrays.equals(temp, ie)){
                                        return 4;
                                }//end if
                        }//end if
                        i++;
                }//end while         
                i = 0;
                result = 0;
                //Check for Firefox trait
                temp = new char[firefox.length];
                while(i < s.length()){
                        result = s.indexOf(firefox[0],i);
                        if(result > 0){
                                s.getChars(result, (result+firefox.length), temp, 0);
                                if(Arrays.equals(temp, firefox)){
                                        return 5;
                                }//end if
                        }//end if
                        i++;
                }//end while                         
   
                return -1; //return unknown
        }//end findWithinString
        
        /**
        * The default constructor. This will load the configuration of the server and bind to a port
        **/
        public JavaServer(){
                //Used for FileIO operations
                fio = new FileIO();
                System.out.println("Loading configuration...");
                //-----CONFIG LOADING SECTION------
                //The items to read from the config.
                String[] elements = {"port", "webFilesDir", "defaultFile", "logDir", "dynamicFiles", "requestSize"};
                //The array to store the results
                String[] values;
                //Read into the array
                values = fio.readInShallowXML("config.xml", "settings", elements);
                
                //Store the values for the rest of the program
                int port = Integer.parseInt(values[0]);
                //The folder for all web files
                FILE_DIR = values[1];
                //The default file to send
                DEFAULT_FILE = values[2];    
                //The logs directory
                LOGS = values[3];
                //The requestSize
                requestSize = Integer.parseInt(values[5]);

                //Create an array of dynamic filetypes
                StringTokenizer stok = 
                        new StringTokenizer(values[4], ",");
                int i = 0;
                DYNAMIC_FILETYPES = 
                        new String[stok.countTokens()];
                while(stok.hasMoreElements()){
                        DYNAMIC_FILETYPES[i] = 
                                (String)stok.nextElement();
                        i++;
                }//end while
                System.out.println("Configuration loaded successfully!");
                //------END CONFIG-----------
                System.out.println("Attempting to host JavaServer...");
                //Checking for needed directories
                String[] array = { LOGS, FILE_DIR };
                i = 0;
                while(i != array.length) {
                        File f = new File(array[i]);
                        if(! f.exists() && ! f.isDirectory()){
                                if(! f.mkdirs()){
                                        System.out.println("FATAL_ERROR: Cannot create '"+array[i]+"' folder!");
                                        System.exit(-1);
                                }//end if
                        }//end if
                        i++;
                }//end while
                
                //----------SERVER BINDING-----------
                
                try{
                        ss = new ServerSocket(port);
                } catch (IOException e){
                        fio.log(LOGS+"server.log", "FATAL: Server failed to start on port "+port+
		        "! Were you running as admin? "+e);
                }//end catch
                
                System.out.println(ss.getInetAddress().getHostName()+":"+ss.getLocalPort()+" Started Successfully.");
        }//end JavaServer
        
        /**
        * This is used by the threads to wait for a connection from a client.
        * After which this function will process the request and attempt to 
        * retrieve the file.
        **/
        public static void waitForConnection(){
                
                try{
                //-------WAITING FOR CONNECTION--------
                        //Wait for a client connection
                        client = ss.accept();
                        
                        //Create a text stream out to the client
                        textOut = new PrintWriter(client.getOutputStream(), true);
                        //Create a data output stream to client.
                        dataOut = new DataOutputStream(client.getOutputStream());
                        //Create an input stream from the client
                        BufferedReader in = new BufferedReader(new
                                InputStreamReader(client.getInputStream()));
                
                        //------GETTING THE MESSAGE-------
                        int lineNumber = 0;
                        String inputLine = null;
                        message = new String[50];
                        //read in client messages until null
                        inputLine = in.readLine();
                        
                        if(inputLine != null && !inputLine.equals("")&&
                                !inputLine.isEmpty()){
                                System.out.println("Request in:"+inputLine);        
                        }else {
                                client.close();
                                return;
                        }
			//Get IP address
			String ip = client.getInetAddress().toString();
                        while(inputLine != null && !inputLine.equals("")&&
                                !inputLine.isEmpty()) {
                                message[lineNumber] = inputLine;
                                if(inputLine.contains("User-Agent")){
                                        //set useragent
					fio.log("Stats.txt","!"+ip+"!"+message[lineNumber]);
                                }//end if
                                lineNumber++;
                                inputLine = in.readLine();
                        }//end while
                        
                        //----------PARSE FILE NAME-----------
                        StringTokenizer stok = new StringTokenizer(message[0]);
                        
                        //Test client request
                        if(stok.countTokens() != 3){
                                textOut.println("HTTP/1.0 400 Bad Request\n");
                                client.close();
                                return;
                        
                        }
                        //Get the HTTP request method
                        METHOD = stok.nextToken();
                        
                        //Get the filePath
                        filePath = stok.nextToken();
                        
                        //test for / request
                        
                        if(filePath.equals("/")){
                                filePath = DEFAULT_FILE;
                        }
                        
                        //Get file type
                        stok = new StringTokenizer(filePath, ".");
                        while(stok.hasMoreElements()){
                                fileType = (String)stok.nextElement();
                        }//end while
                        
                        //----------CHECK IF FILE EXISTS-------
                        //append the default directory to filepath
                        filePath = FILE_DIR + filePath;

                        File file = new File(filePath);
                        if(!file.exists()){
                                checkForRedirect();
                                client.close();
                                return;
                        }
                        
                        //---------TEST FOR DYNAMIC FILE-------
                        lineNumber = 0;
                        boolean found = false;
                        while(lineNumber < DYNAMIC_FILETYPES.length &&
                                !found){
                                if(fileType.equals(
                                        DYNAMIC_FILETYPES[lineNumber]) || METHOD.equals("POST")){
                                        //Running plugin
                                        Class<?> clazz = 
                                                Class.forName(fileType);
                                        Constructor<?> ctor = 
                                                clazz.getConstructor(
                                                String[].class, PrintWriter.class, Socket.class);
                                        Object object = 
                                                ctor.newInstance(
                                                (Object)message, (Object)textOut, (Object)client);
                                        client.close();
                                        return;
                                }//end if
                                lineNumber++;
                        }//end while
                        
                        //-------------GET AND HEAD SECTION-------------------
                        //Get last modified date
                        Date fDate = new Date(file.lastModified());
                
                        //Get Current Date
                        Date dNow = new Date();
                        SimpleDateFormat ft = new SimpleDateFormat(
                                "E',' dd MMM YYYY hh:mm:ss zzz");
                                
                        textOut.println("HTTP/1.0 200 OK\n"+
                                "Date: "+ ft.format(dNow)+"\n" +
                                "Server: Custom JavaServer\n"+
                                "Last-Modified: "+ft.format(fDate)+"\n"+
                                "Content-Length: "+file.length()+"\n"+
                                "Connection: close\n"+
                                "Content-Type: text/html");
                        
                        generateHTML();        
                        
                        textOut.println("");
                        
                        if(METHOD.equals("GET")){
                                byte[] data = fio.readInBytes(filePath);
                                dataOut.write(data, 0, data.length);
                                dataOut.flush();
                                textOut.println("");
                        }
                        
                        
                        client.close();
                        return;
                } catch(IOException e) {
                        fio.log(LOGS+"server.log", "FATAL: WaitforConnectionError "+e);
                        textOut.println("HTTP/1.0 500 Internal Server Error");
                        try{client.close();}catch(IOException err){System.out.println(err);}
                        System.exit(-2);
                } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException er) {
                        fio.log(LOGS+"server.log", "FATAL: Plugin Error "+er);
                        textOut.println("HTTP/1.0 500 Internal Server Error");
                        try{client.close();}catch(IOException e){System.out.println(e);}
                }//end catch
        }//end waitForConnection
}//end class
