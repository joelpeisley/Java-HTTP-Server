// Imports
import java.io.*;
import java.util.*;
import java.text.*;
import javax.xml.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;

/**
* This file is used for many file operations.
*
* @author Joel Peisley
* @version 1.0
* @since 14-11-2014
**/
public class FileIO {
        
        /**
        * The default constructor, does nothing 
        * and is used to create an object.
        **/
	public FileIO(){
	        //Empty
	}
	
	/**
	* Will read in an entire XML file and pass back the results.
	* After the root element the XML can only have one more level 
	* of depth.
	* 
	* @param f The filename of the XML file.
	* @param r The name of the root element.
	* @param e All of the elements you would like to read.
	* @return An array with all of the results in the order of the array you passed.
	**/
	public static String[] readInShallowXML(String f, String r, String[] e){
	        try {
	                //Getting the XML file
                        File file = new File(f);
                        //Error if the file does not exist.
                        if(!file.exists()){
                                System.out.println("FATAL_ERROR: "+f+" not found!");
                                System.exit(-1);
                        }
                        //Creating the document handlers
                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        DocumentBuilder db = dbf.newDocumentBuilder();
                        Document doc = db.parse(file);
                        doc.getDocumentElement().normalize();
                        
                        //Create a nodelist using the root element
                        NodeList nodeLst = doc.getElementsByTagName(r);
                        
                        Node fstNode = nodeLst.item(0);
                        
                        //create an array the same length as the one passed.
                        String[] returnList = new String[e.length];
                        
                        //loop for each item in the array
                        for(int i = 0; i != e.length; i++){                
                                //Read in elements
                                Element fstElmnt = (Element) fstNode;
                                NodeList hostElmntLst = fstElmnt.getElementsByTagName(e[i]);
                                Element hostElmnt = (Element) hostElmntLst.item(0);
                                NodeList host = hostElmnt.getChildNodes();
                                returnList[i] = ((Node) host.item(0)).getNodeValue();
                        } //end for
                        
                        return returnList;
                        
                } catch(ParserConfigurationException | SAXException | IOException ex) {
                        System.out.println("FATAL_ERROR: Reading config file. "+ex);
                        System.exit(-1);
                } //end catch
                
                //Program will never get here as long as the exit exists in the catch.
                return null;
	
	} //end readInShallowXML
	
        /**
        * Function opens the file passed and returns a byte array.
        *   
        * @param s The name of the file to be read.
        * @return A byte array with the contents of the file.
        **/
	public static byte[] readInBytes(String s){
	        try{
	                //Open file s
		        File file = new File(s);
		
		        FileInputStream fin = new FileInputStream(file);
		
		        //create byte array the size of the file.
		        byte[] fileContent = new byte[(int)file.length()];
		
		        //Read in the entire file
		        fin.read(fileContent);
		
		        //Return the byte array to the caller
		        return fileContent;
		
	        } //Catch exceptions
	        catch(FileNotFoundException e) {
	                System.err.println(e + ". Could not find '"+s+"'");
	        } catch(IOException e) {
	                System.err.println("Error in readInBytes. "+e);
	        } //end catch
		
		//Return null if an exception occurs.
		return null;
	
	} //end readInBytes

        /**
        *   Function will write the byte array to the file string.
        *
        * @param s The filename to write to.
        * @param array The byte array to write.
        *
        **/
	public static synchronized void writeOutBytes(String s, byte[] array) {
	        try{
	                //create/overwrite file s
		        FileOutputStream fos = new FileOutputStream(s);
		
		        //Write the array to file.
		        fos.write(array);
		        fos.close();
	        } catch(IOException e){
	                System.err.println("Error in writeOutBytes. "+ e);
	        } //end catch
	} //end writeOutBytes

        /**
        * This method is used for logging. Adds the current time and date to log.
        *
        * @param f The file to log to. Appends to file.
        * @param s The message to log.
        **/
        public static synchronized void log(String f, String s){
                try {
                        BufferedWriter bw = new 
                                BufferedWriter(new FileWriter(f, true));
                        Date dNow = new Date();
                        SimpleDateFormat ft = new SimpleDateFormat(
                                "E dd.MM.YYYY 'at' hh:mm:ss a zzz");
                        bw.write(ft.format(dNow)+" - "+s+"\n");
                        bw.flush(); 
                } catch(IOException e) { 
                         System.out.println("Logger Error "+e); 
                } //end catch
        } //end log

        /**
        * This function will read a file in, line by line.
        *
        * @param s The file to read in.
        * @return A String array containing the lines. Or null if there is an error.
        **/
	public static String[] readInLines(String s){
		try {		
			int count = 0;
			File f = new File(s);
			FileInputStream fis = new FileInputStream(f);
 
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
 
			String line = null;
			String[] sArray = new String[50];

			while ((line = br.readLine()) != null) {
				sArray[count] = line;
				count ++;
				if(count == sArray.length-1){
					sArray = increaseArray(sArray);
				}
			}
			br.close();

			String[] array = new String[(count+1)];

			int count2 = 0;
			while(count2 < count){
				array[count2] = sArray[count2];
				count2++;
			}

			return array;
		
		} catch (FileNotFoundException e){
			System.out.println(e);
		} catch (IOException e){
			System.out.println(e);
		}
		return null;
	} //end readInLines

/**
        * This function will read a file in, line by line.
        *
        * @param s The file to read in.
        * @return A LinkedList containing the lines. Or null if there is an error.
        **/
	public static synchronized LinkedList<String> readInLinesLinked(String s){
		try {		
		        LinkedList<String> ll = new LinkedList<String>();
			int count = 0;
			File f = new File(s);
			FileInputStream fis = new FileInputStream(f);
 
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
 
			String line = null;

			while ((line = br.readLine()) != null) {
                                ll.add(line);
			}
			br.close();

			return ll;
		
		} catch (FileNotFoundException e){
			System.out.println(e);
		} catch (IOException e){
			System.out.println(e);
		}
		return null;
	} //end readInLines

        /**
        * This function will increase the size of the
        * String array passed by 100.
        * 
        * @param s The String array to increase.
        * @return The larger String array with contents.
        **/
	public static String[] increaseArray(String[] s){
		String[] temp = new String[s.length+100];
		int count = 0;
		while(count != s.length){
			temp[count] = s[count];
			count++;
		}
		return temp;
	}
} //end class
