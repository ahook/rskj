package co.rsk.peg.btcLockSender;

import co.rsk.bitcoinj.core.Address;
import co.rsk.bitcoinj.core.BtcTransaction;
import co.rsk.bitcoinj.core.Sha256Hash;
import co.rsk.bitcoinj.script.Script;
import org.ethereum.crypto.HashUtil;
import org.ethereum.util.ByteUtil;

public class P2shP2wshBtcLockSender extends BtcLockSender {

    public P2shP2wshBtcLockSender(BtcTransaction btcTx) throws BtcLockSenderParseException {
        super(btcTx);
        this.transactionType = TxType.P2SHP2WSKH;
    }

    @Override
    protected void parse(BtcTransaction btcTx) throws BtcLockSenderParseException {
        if (btcTx == null) {
            throw new BtcLockSenderParseException();
        }
        if (!btcTx.hasWitness()) {
            throw new BtcLockSenderParseException();
        }
        if (btcTx.getInput(0).getScriptBytes() == null) {
            throw new BtcLockSenderParseException();
        }
        if (btcTx.getInput(0).getScriptSig().getChunks().size() != 1) {
            throw new BtcLockSenderParseException();
        }
        if (btcTx.getWitness(0).getPushCount() < 3) { //un 0, 1 firma, redeem script
            throw new BtcLockSenderParseException();
        }

        int pushsLength = btcTx.getWitness(0).getPushCount();
        byte[] redeemScript = btcTx.getWitness(0).getPush(pushsLength - 1); //Redeem script is the last push of the witness

        Script redeem = new Script(redeemScript);
        if (!redeem.isSentToMultiSig()) {
            throw new BtcLockSenderParseException();
        }

        // Get btc address
        //hash160(sha256(220020,sha256(redeemscfript))))

        //origen: "scriptPubKey": "0020a7bacf8aa40bc642ba50f20ce1df007f687cf7f7370281ec94c17afcc4eee159"
        //destino :"scriptPubKey": "9cef33093ae858b01c56ef6d1ef4f9b55f2c0324",

        byte[] hashed = Sha256Hash.hash(redeemScript);
        byte[] pre =  new byte[]{0x00, 0x20};
        byte[] merged = ByteUtil.merge(pre, hashed);
        byte[] hashedAgain = Sha256Hash.hash(merged);
        byte[] scriptPubKey = HashUtil.ripemd160(hashedAgain);
        //Al Sha256Hash.hash(redeemScript) agregarle un 0 y un espacio en blanco adelante. Luego aplicar el hash 160
        //Mergear los bytes del 0 y del espacio en blanco con el array de bytes obtenido de aplicar sha256
        //0220... algo por el estilo, ver en BIP 141
        this.btcAddress = new Address(btcTx.getParams(), btcTx.getParams().getP2SHHeader(), scriptPubKey);
        //2N7Z1x59hVvnSc7HpnSigg5k77hNUeBFQUA
        //this.btcAddress = Address.fromP2SHHash(btcTx.getParams(), scriptPubKey); //Alternativa? Probar
    }
}
