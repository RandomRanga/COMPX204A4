// Java provides SSLSocket and SSLServerSocket classes, which are roughly 
// equivalent to Socket and ServerSocket:
//       SSLServerSocket listens on a port for incoming connections, like ServerSocket
//       SSLSocket connects to an SSLServerSocket, like Socket, and represents an individual 
//       connection accepted from an SSLServerSocket.
// To create a SSLSocket or SSLServerSocket, we must use "factories"

// Socket factories are a convenient way to set TLS parameters that will 
// apply to Sockets created from the factory, e.g:
//       Which TLS versions to support
//       Which Ciphers and Hashes to use
//       Which Keys to use and which Certificates to trust
// As you might guess by the names
//       SSLServerSocketFactory creates SSLServerSocket objects
//       SSLSocketFactory creates SSLSocket objects

// Java uses KeyStore objects to store Keys and Certificates
// A KeyStore object is used when encrypting and authenticating
// The files that contain Keys and Certificates are password protected

// THE CODE BELOW IS INCOMPLETE AND HAS PROBLEMS
// FOR EXAMPLE, IT IS MISSING THE NECESSARY EXCEPTION HANDLING

import java.security.KeyStore;
import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.*;


public class MyTLSFileServer {

   private static ServerSocketFactory getSSF() throws Exception
   {
      // Get 
      //    an SSL Context that speaks some version of TLS, 
      //    a KeyManager that can hold certs in X.509 format,  
      //    and a JavaKeyStore (JKS) instance   
      SSLContext ctx = SSLContext.getInstance("TLS");
      KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
      KeyStore ks = KeyStore.getInstance("JKS");

      // Store the passphrase to unlock the JKS file.   
      // INSECURE! DON'T DO IT.
      char[] passphrase = "asdfgh".toCharArray();

      // Load the keystore file. The passphrase is   
      // an optional parameter to allow for integrity   
      // checking of the keystore. Could be null   
      ks.load(new FileInputStream("server.jks"), passphrase);

      // Init the KeyManagerFactory with a source   
      // of key material. The passphrase is necessary   
      // to unlock the private key contained.   
      kmf.init(ks, passphrase);

      // initialise the SSL context with the keys.   
      ctx.init(kmf.getKeyManagers(), null, null);

      // Get the factory we will use to create   
      // our SSLServerSocket   
      SSLServerSocketFactory ssf = ctx.getServerSocketFactory();
      return ssf;
   }

   public static void main(String args[]) throws Exception
   { 
      //check amount of args is 1 
      if (args.length != 1){
         System.out.println("Usage: java MyTSLFileServer <port>");
         return;
      }
      
 
      int port = Integer.parseInt(args[0]);

      try{
         // use the getSSF method to get a  SSLServerSocketFactory and 
         // create our  SSLServerSocket, bound to specified port  
         ServerSocketFactory ssf = getSSF(); 
         SSLServerSocket ss =  (SSLServerSocket) ssf.createServerSocket(port); 
         String EnabledProtocols[] = {"TLSv1.2", "TLSv1.3"}; 
         ss.setEnabledProtocols(EnabledProtocols); 

         System.out.println("server started, waiting connect");

         SSLSocket s = (SSLSocket)ss.accept();

         while(true){
               System.out.println("connected");

               // I/O streams to force TLS handshake 
               BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
               System.out.println("reader");
               BufferedOutputStream bos = new BufferedOutputStream(s.getOutputStream());
               System.out.println("hsdfhakj");

               //reads the files requested still in hand shake 
               String name = reader.readLine();
               System.out.println("file name: " + name);
               

               //check if the file exists on server 
               File file = new File(name);
               if (file.exists() && !file.isDirectory()){
                  System.out.println("File found");

                  try (FileInputStream fileStream = new FileInputStream(file)){
                     byte[] buffer = new byte[4096];
                     int readByte;
                     while ((readByte = fileStream.read(buffer)) != -1){
                        bos.write(buffer, 0, readByte);
                     }
                     System.out.println("file transfered");
                  }

               }
               else {
                  System.err.println("file not found");
               }  
               bos.flush();
         }
            
      }
      catch(Exception ex){
         System.out.println(ex.toString());
      }
   }
}




