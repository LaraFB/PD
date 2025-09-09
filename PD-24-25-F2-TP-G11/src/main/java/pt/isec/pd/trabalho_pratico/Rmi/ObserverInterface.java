package pt.isec.pd.trabalho_pratico.Rmi;

import java.rmi.Remote;

public interface ObserverInterface extends Remote {
    void stateChanged(String update) throws java.rmi.RemoteException;
}
