package org.iota.ict.ec;

import org.iota.ict.utils.crypto.SignatureScheme;

public abstract class EconomicActor {

    protected final String address;

    public EconomicActor(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    protected String messageToSign(String trunk, String branch) {
        return SignatureScheme.hash(trunk + branch);
    }
}