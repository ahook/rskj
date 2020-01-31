package org.ethereum.core;

import co.rsk.core.RskAddress;

public interface SignatureCache {
    RskAddress getSender(Transaction transaction);

    boolean containsTx(Transaction transaction);
}
