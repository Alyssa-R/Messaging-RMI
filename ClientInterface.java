package message;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInterface extends Remote {
    Boolean push(String sender, String message) throws RemoteException;
}