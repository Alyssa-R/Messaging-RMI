package message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client implements ClientInterface {

  public Client() {}

  /**
   * Provides a RMI handle to allow the server to send messages to the client.
   */
  public Boolean push(String sender, String message) {
    System.out.println(sender + "> " + message);
    return true;
  }

  /**
   * Defines the behavior of the client console and calls RMI methods on the
   * server.
   */
  public static void main(String[] args) throws IOException {
    // Read the IP address of the server from the terminal arguments.
    String host = (args.length < 1) ? null : args[0];
    // Prepare the regex pattern for sending messages.
    Pattern validMsg = Pattern.compile("([a-zA-Z0-9]++): (.++)");
    // Set up a buffered reader to read input from the command line.
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    try {
      // Expose this client to a server through the RMI registry.
      Client client = new Client();
      ClientInterface stub = (ClientInterface) UnicastRemoteObject.exportObject(client, 0);
      Registry clientRegistry = LocateRegistry.getRegistry();
      clientRegistry.bind("ClientInterface", stub);
      // Attempt to connect to the server.
      Registry serverRegistry = LocateRegistry.getRegistry(host);
      ServerInterface serverStub = (ServerInterface) serverRegistry.lookup("ServerInterface");
      // Initialize the variables for the username request loop.
      System.out.print("Client ready\nSelect your username: ");
      String username = "";
      int usernameSuccess;
      do {
        username = br.readLine();
        usernameSuccess = serverStub.register(username, InetAddress.getLocalHost().getHostAddress());
        if (usernameSuccess == 1){
         System.out.print("Someone already has that username. Sorry!\nTry another username: ");
        }
        else if(usernameSuccess == 2){
          System.out.print("Username invalid if it contains a colon. \nTry another username: ");
        }
      } while (usernameSuccess != 0);
      // Once the user has successfully chosen a username, we can provide
      // instructions for using the chatroom.
      System.out.print("Welcome " + username + "\n" +
        "Use [recipient username]: [message] to send a message.\n" +
        "Type \\directory to list the users connected to this server.\n" +
        "Type \\quit to exit the chatroom.\n>>> ");
      // Initialize the variables
      String cmd = "";
      while ((cmd = br.readLine()) != null) {
        // Handle a request for the directory from the server.
        if (cmd.equals("\\directory")) {
          String[] directory = serverStub.getDirectory();
          for (int i = 0; i < directory.length; i++) {
            System.out.println(directory[i]);
          }
        }
        // Exist the application.
        else if (cmd.equals("\\quit")) {
          System.out.print("Done!\n");
          br.close();
          clientRegistry.unbind("ClientInterface");
          UnicastRemoteObject.unexportObject(client, true);
          return;
        }
        // Otherwise, try to interpret the input as a message.
        else {
          Matcher m = validMsg.matcher(cmd);
          if (m.find()) {
            boolean success = serverStub.send(username, m.group(1), m.group(2));
            if (success) {
              System.out.println("Message sent to " + m.group(1));
            } else {
              System.out.print("No such user is in the directory.\n");
            }
          } else {
            System.out.print("Invalid message.\nUse [recipient username]: [message] to send a message.\n");
          }
        }
        System.out.print(">>> ");
      }
    } catch (Exception e) {
      System.err.println("Client exception: " + e.toString());
      e.printStackTrace();
    }
  }
}
