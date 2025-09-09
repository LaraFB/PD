package pt.isec.pd.trabalho_pratico.Rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ServerInterface extends Remote {
    List<String> getUsers() throws RemoteException;
    List<String> getGroups() throws RemoteException;
    void addObserver(ObserverInterface observer) throws java.rmi.RemoteException;
}
