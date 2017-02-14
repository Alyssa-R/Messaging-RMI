package message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client implements ClientInterface {

  public Client() {}

  public Boolean push(String sender, String message) {
    System.out.println(">>> " + sender + ": " + message);
    return true;
  }

  public static void main(String[] args) throws IOException {
    Pattern validMsg = Pattern.compile("([a-zA-Z0-9]++): ([a-zA-Z0-9]++)");
    InputStreamReader fileInputStream = new InputStreamReader(System.in);
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    String host = (args.length < 1) ? null : args[0];
    try {
      Registry registry = LocateRegistry.getRegistry(host);
      ServerInterface serverStub = (ServerInterface) registry.lookup("ServerInterface");
      System.out.print("Client ready\nSelect your username: ");
      String username = br.readLine();
      while (!serverStub.register(username, InetAddress.getLocalHost().getHostAddress())) {
        System.out.println("Someone already has that username. Sorry!\nTry another username: ");
        username = br.readLine();
      }
      System.out.print("Welcome " + username + "\n" +
        "Use [recipient username]: [message] to send a message.\n" +
        "Type \\directory to list the users connected to this server.\n");
      String cmd = "";
      while (br.ready()) {
        cmd += br.readLine();
        if (cmd.equals("\\directory")) {
          String[] directory = serverStub.getDirectory();
          for (int i = 0; i < directory.length; i++) {
            System.out.println(directory[i]);
          }
        } else {
          Matcher m = validMsg.matcher(cmd);
          if (m.find()) {
            boolean success = serverStub.send(username, m.group(0), m.group(1));
            if (success) {
              System.out.println("Message sent to " + m.group(0));
            } else {
              System.out.print("Invalid message.\nUse [recipient username]: [message] to send a message.\n");
            }
          } else {
            System.out.print("Invalid message.\nUse [recipient username]: [message] to send a message.\n");
          }
        }
      }
      br.close();
    } catch (Exception e) {
      System.err.println("Client exception: " + e.toString());
      e.printStackTrace();
    }
  }
}
