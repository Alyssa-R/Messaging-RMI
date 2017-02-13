package message;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
    Boolean register(String name) throws RemoteException;
    Boolean send(String recipient, String message) throws RemoteException;
    String[] getDirectory() throws RemoteException;
}