public class Driver {
        public static void main(String[] args){
                if(args.length <= 0){
                        System.out.println("Usage: java Driver <number of threads>");
                        System.exit(-1);
                }
    
                JavaServer js = new JavaServer();
                int numThreads = Integer.parseInt(args[0]);
                int i = 0;
                Thread[] tArray = new Thread[numThreads];
                while(i != numThreads){
                        tArray[i] = new Thread(new SpinOffThread(js, "T"+i, "logs/"));
                        tArray[i].start();
                        i++;
                }//end while
                
                while(true){
                        i = 0;
                        while(i < numThreads){
                                if(! tArray[i].isAlive()){
                                        tArray[i] = new Thread(new SpinOffThread(js, "T"+i, "logs/"));
                                        tArray[i].start();
                                        System.out.println("Thread"+i+" Revived!");
                                }//end if
                        i++;
                        }//end while
                        //This is used to save resources.
                        try{Thread.sleep(2000);}catch(InterruptedException e){}
                }//end while
        } //end main
}//end class
