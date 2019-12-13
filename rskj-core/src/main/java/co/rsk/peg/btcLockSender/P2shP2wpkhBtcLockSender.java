package co.rsk.peg.btcLockSender;

import co.rsk.bitcoinj.core.Address;
import co.rsk.bitcoinj.core.BtcECKey;
import co.rsk.bitcoinj.core.BtcTransaction;
import co.rsk.bitcoinj.core.Sha256Hash;
import co.rsk.bitcoinj.script.Script;
import co.rsk.core.RskAddress;
import org.ethereum.crypto.HashUtil;
import org.ethereum.util.ByteUtil;

public class P2shP2wpkhBtcLockSender extends BtcLockSender {

    public P2shP2wpkhBtcLockSender(BtcTransaction tx) throws BtcLockSenderParseException {
        super(tx);
        this.transactionType = TxType.P2SHP2WPKH;
    }

    @Override
    protected void parse(BtcTransaction tx) throws BtcLockSenderParseException {
        if (tx == null) {
            throw new BtcLockSenderParseException();
        }
        if (!tx.hasWitness()) {
            throw new BtcLockSenderParseException();
        }
        if (tx.getInput(0).getScriptBytes() == null) {
            throw new BtcLockSenderParseException();
        }
        if (tx.getInput(0).getScriptSig().getChunks().size() != 1) {
            throw new BtcLockSenderParseException();
        }
        if (tx.getWitness(0).getPushCount() != 2) {
            throw new BtcLockSenderParseException();
        }

        byte[] pubKey = tx.getWitness(0).getPush(1);
        // get pubKey from witness
        BtcECKey key = BtcECKey.fromPublicOnly(tx.getWitness(0).getPush(1));

        if (!key.isCompressed()) {
            throw new BtcLockSenderParseException();
        }

        // pubkeyhash = hash160(sha256(pubKey))
        byte[] keyHash = key.getPubKeyHash();

        // witnessVersion = 0x00
        // push20 = 0x14
        // scriptPubKey = hash160(sha256(witnessVersion push20 pubkeyhash))
        byte[] redeemScript = ByteUtil.merge(new byte[]{ 0x00, 0x14}, keyHash);
        byte[] scriptPubKey = HashUtil.ripemd160(Sha256Hash.hash(redeemScript));

        this.btcAddress = new Address(tx.getParams(), tx.getParams().getP2SHHeader(), scriptPubKey);
        this.rskAddress = new RskAddress(org.ethereum.crypto.ECKey.fromPublicOnly(pubKey).getAddress());
    }
}
