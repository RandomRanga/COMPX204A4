// The client is usually much more straight forward
// Defaults will load Java’s set of Trusted Certificates
// Java will validate there is a path to a trusted CA
// By default, Java will NOT do hostname validation,
// but the more secure thing to do is to check!

// THE CODE BELOW IS INCOMPLETE AND HAS PROBLEMS
// FOR EXAMPLE, IT IS MISSING THE NECESSARY EXCEPTION HANDLING


import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.security.cert.X509Certificate;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

public class MyTLSFileClient {
  public static void main(String args[])throws Exception
  {

    //check amount of args is 1 
    if (args.length != 3){
      System.out.println("Usage: java MyTSLFileClient <host> <port> <file>");
      return;
    }
    
    String hostName = args[0];
    int port = Integer.parseInt(args[1]);
    String file = args[2];


    try{
      SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
      SSLSocket socket = (SSLSocket)factory.createSocket(hostName, port);

      // set HTTPS-style checking of HostName _before_ 
      // the handshake
      SSLParameters params = new SSLParameters();
      // params.setEndpointIdentificationAlgorithm("HTTPS");
      socket.setSSLParameters(params);

      socket.startHandshake(); // explicitly starting the TLS handshake

      // at this point, can use getInputStream and 
      // getOutputStream methods as you would in a regular Socket

      // get the X509Certificate for this session
      SSLSession session = socket.getSession();
      X509Certificate cert = (X509Certificate) session.getPeerCertificates()[0];

      // extract the CommonName, and then compare
      getCommonName(cert);
    }
    catch(Exception ex){
      System.err.println(ex.toString());
    }

  }

  static String getCommonName(X509Certificate cert) throws Exception
  {
    String name = cert.getSubjectX500Principal().getName();
    LdapName ln = new LdapName(name);
    String cn = null;
    
    // Rdn: Relative Distinguished Name
    for(Rdn rdn : ln.getRdns()) 
      if("CN".equalsIgnoreCase(rdn.getType()))
        cn = rdn.getValue().toString();
    return cn;
  }
}