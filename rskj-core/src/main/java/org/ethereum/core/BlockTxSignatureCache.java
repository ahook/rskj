package org.ethereum.core;

import co.rsk.core.RskAddress;
import co.rsk.remasc.RemascTransaction;
import co.rsk.util.MaxSizeHashMap;

import java.util.Map;

public class BlockTxSignatureCache {

    private static final int MAX_CACHE_SIZE = 6000;

    private final Map<Transaction, RskAddress> addressesCache;
    private ReceivedTxSignatureCache cacheN2;

    public BlockTxSignatureCache(ReceivedTxSignatureCache cacheN2) {
        this.cacheN2 = cacheN2;
        addressesCache = new MaxSizeHashMap<>(MAX_CACHE_SIZE,true);
    }

    public RskAddress getSender(Transaction transaction) {

        RskAddress sender;

        if (transaction instanceof RemascTransaction) {
            return RemascTransaction.REMASC_ADDRESS;
        }

        if (addressesCache.containsKey(transaction)) {
            return addressesCache.get(transaction);
        }

        if (cacheN2.containsTx(transaction)) {
            sender = cacheN2.getSender(transaction);
            addressesCache.put(transaction, sender);
        } else {
            sender = addressesCache.computeIfAbsent(transaction, Transaction::getSender);
        }

        return sender;
    }
}
