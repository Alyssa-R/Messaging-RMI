package message;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
    Boolean register(String name, String ip) throws RemoteException;
    Boolean send(String sender, String recipient, String message) throws RemoteException;
    String[] getDirectory() throws RemoteException;
}