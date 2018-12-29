package org.iota.ict.ixi;

import org.iota.ict.network.event.GossipReceiveEvent;
import org.iota.ict.network.event.GossipSubmitEvent;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteIxiModule extends Remote {

    void onTransactionReceived(GossipReceiveEvent event) throws RemoteException;

    void onTransactionSubmitted(GossipSubmitEvent event) throws RemoteException;
}