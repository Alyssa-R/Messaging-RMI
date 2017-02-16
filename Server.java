package message;

import java.rmi.NotBoundException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.util.Hashtable;
import java.util.Set;

public class Server implements ServerInterface {
  private Hashtable<String, ClientInterface> directory;


  public Server() {
    directory = new Hashtable<String, ClientInterface>();
  }

  /*Returns true and adds username to directory if not already in use
   * If username in use, returns false (fails)
   * establishes each client's connection to server
   */
  public int register(String name, String ip) {
    if(directory.containsKey(name)){
      System.out.println("Error 1: used");
      return 1;
    }
    else if (name.contains(":")){
      System.out.println("Error 2: colon");
      return 2;
    }
    else{
      try{
        //get registry -- how do we get host? send it in as arg from client? is it an ip addr?
        Registry registry = LocateRegistry.getRegistry(ip);
        //get stub
          ClientInterface ClientStub = (ClientInterface) registry.lookup("ClientInterface");
          directory.put(name, ClientStub);
          //confirmation message?
          return 0;
      }
        catch(RemoteException e){
          System.out.println(e);
          return 3;
        }
        catch(NotBoundException e){
          System.out.println(e);
          return 3;
        }
      }
    }


    /*Returns true if message successfully passed
     * returns false upon failure
     */
    public Boolean send(String sender, String recipient, String message) {
      try{
        if (directory.containsKey(recipient)){

          ClientInterface recipStub = directory.get(recipient);
          recipStub.push(sender,message);
          return true;
        }
        else{
          System.out.println ("No such user is logged on. Please try again.");
          return false;
        }
      }
      catch(Exception e){
        System.out.println(e);
        return false;
      }
    }

    /*Returns String array of usernames in use in this chat room
     *
     */
    public String[] getDirectory() {
      Set<String> users = directory.keySet();
      String[] usersArray = new String[users.size()];
      return users.toArray(usersArray);
    }

    public static void main(String args[]) {

      try {
        Server obj = new Server();
        ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(obj, 0);

        // Bind the remote object's stub in the registry
        Registry registry = LocateRegistry.getRegistry();
        registry.bind("ServerInterface", stub);

        System.err.println("Server ready");

        //get the client interface -- remove these altogether? establishing connection in register username, seems risky
        //get registry
        /*
        Registry registry = LocateRegistry.getRegistry(host);
        //get stub
        Hello stub = (Hello) registry.lookup("Hello"); */
      } catch (Exception e) {
        System.err.println("Server exception: " + e.toString());
        e.printStackTrace();
      }
    }
  }
