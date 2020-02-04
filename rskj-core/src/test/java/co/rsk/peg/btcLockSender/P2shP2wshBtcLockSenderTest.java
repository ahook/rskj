package co.rsk.peg.btcLockSender;

import co.rsk.bitcoinj.core.*;
import co.rsk.bitcoinj.script.Script;
import co.rsk.bitcoinj.script.ScriptBuilder;
import co.rsk.config.BridgeConstants;
import co.rsk.config.BridgeRegTestConstants;
import co.rsk.core.RskAddress;
import co.rsk.peg.PegTestUtils;
import org.bouncycastle.util.encoders.Hex;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class P2shP2wshBtcLockSenderTest {
    private static NetworkParameters networkParameters;
    private static BridgeConstants bridgeConstants;

    @BeforeClass
    public static void setup() {
        bridgeConstants = BridgeRegTestConstants.getInstance();
        networkParameters = bridgeConstants.getBtcParams();
    }

    @Test(expected = BtcLockSenderParseException.class)
    public void throws_exception_if_transaction_is_null() throws BtcLockSenderParseException {
        new P2shP2wshBtcLockSender(null);
    }

    @Test(expected = BtcLockSenderParseException.class)
    public void throws_exception_if_transaction_doesnt_have_witness() throws BtcLockSenderParseException {
        BtcTransaction btcTx = new BtcTransaction(networkParameters);
        new P2shP2wshBtcLockSender(btcTx);
    }

    @Test(expected = BtcLockSenderParseException.class)
    public void throws_exception_if_tx_doesnt_have_scriptsig() throws BtcLockSenderParseException {
        BtcTransaction btcTx = new BtcTransaction(networkParameters);
        btcTx.addInput(new TransactionInput(btcTx.getParams(), null, null));
        TransactionWitness witness = new TransactionWitness(1);
        witness.setPush(0, new byte[]{});
        btcTx.setWitness(0, witness);

        new P2shP2wshBtcLockSender(btcTx);
    }

//    @Test(expected = BtcLockSenderParseException.class)
//    public void throws_exception_if_transaction_has_scriptsig_with_more_than_one_chunk() throws BtcLockSenderParseException {
//        BtcTransaction btcTx = new BtcTransaction(networkParameters);
//        btcTx.addInput(PegTestUtils.createHash(1), 0, ScriptBuilder.createInputScript(null, new BtcECKey()));
//        TransactionWitness witness = new TransactionWitness(1);
//        witness.setPush(0, new byte[]{});
//        btcTx.setWitness(0, witness);
//
//        new P2shP2wshBtcLockSender(btcTx);
//    }

    //Bitcoinj-thin no tiene soporte para witness, hacer pruebas con raw transactions mejor

    @Test(expected = BtcLockSenderParseException.class)
    public void throws_exception_if_transaction_witness_doesnt_have_at_least_three_pushes() throws BtcLockSenderParseException {
        BtcTransaction btcTx = new BtcTransaction(networkParameters);
        btcTx.addInput(PegTestUtils.createHash(1), 0, ScriptBuilder.createInputScript(null, new BtcECKey()));
        TransactionWitness witness = new TransactionWitness(2);
        witness.setPush(0, new byte[]{});
        witness.setPush(1, new byte[]{});
        btcTx.setWitness(0, witness);

        new P2shP2wshBtcLockSender(btcTx);
    }

    @Test(expected = BtcLockSenderParseException.class)
    public void reject_p2wpkh_transaction() throws BtcLockSenderParseException {
        String rawTx = "020000000001017d6228912c3d2dfe0f054913f00722e4ff5fe002e73d09ae57b30414ed079ff40000000000fffff" +
                "fff02d4d5f5050000000017a914ae3f6da3f010a5a107cecf8e477719498092497c8700e1f5050000000017a9145cad76d21" +
                "f9aa75daf3e0846571fb720d64418e5870247304402207225fdb8bd03762588587732aafe559d155c04135ab4ea8f6e7f07e" +
                "e5d7d34230220576a21b784d1dc5789c7e814f0d8b2882b7e8b09538bfb95132c17602769f223012103a83a3a251030faf3a" +
                "9080aac7bb84fb289b6e17ba7805655a62bb6b29ec9048b00000000";

        BtcTransaction tx = new BtcTransaction(networkParameters, Hex.decode(rawTx));

        new P2shP2wshBtcLockSender(tx);
    }

    @Test
    public void gets_p2sh_p2wsh_btc_lock_sender_from_raw_transaction() throws BtcLockSenderParseException {
        //Transaction sent from 2/3 p2sh multisig address to a p2sh address
        String rawTx = "02000000000101d05593206bb67160a420e6fb25030037d04ee3bf64d18f8660de7de56d014c03010000002322002" +
                "0a7bacf8aa40bc642ba50f20ce1df007f687cf7f7370281ec94c17afcc4eee159ffffffff022092e111000000001976a914c" +
                "38d00f2362ccc10f9fc784c04f0cd3d677bada488ac00a3e1110000000017a9147c9ed9c5e76480b613288b4d6cf793ffda1" +
                "9419187040047304402205f507c9531bcdaf34fbcefdf9ba46cf5b62b3bd7d28af78144a083f6c7d43ed502207d812da6dd9" +
                "951e65a8c7dc2c7af0e04ff5553d0cc2ba45cc8439ff2fc56bf6a01473044022055d5fc47bbfa0d92751a0a4803faa7e2acf" +
                "3db84d7aebec57ef4cc5ac7d6e2360220738b233e5a2564ccccfefd05d434eaa08392d678762080db690b4df913da0cba016" +
                "9522103a01b7993e674a3fe7d303073e5c5b366589586873f38abc3f0270fd927a1a02c210321367e27d2b6dd49c1b07ec7d" +
                "ef3fb99e13989b6c48a049c70ffc2d930d66c3921039998ef58c172f7a449e719263853b69815fca58f2b383c0ee7456c58da" +
                "f341a253ae00000000";
        rawTx = "020000000001015e51eee2d634da2a057bddc968e54aea68002f582d0501a6302c1cd9229a509d0100000023220020c7c0f8ad1d7d46a206629e71dbf38d18cdbe8391005916594a0e3910154606e9ffffffff022054cd1d000000001976a91460910d8918d712d6bde9f15d909be6ee4d06280b88ac0065cd1d0000000017a9142e6d06504caf95a9aa751e0abdaaf88db96d3f1a870400473044022009f4ac99cc347660638481201a4b61078f2808b3c0c3a68ee032a61498f7bb5e02201baf929d2d9bf66a821ac011bcbbd68c91b494e43c9d58c7fcc10e80b7c47f7d014730440220069c8b994da09cabc7ef8c7cf81f70f71aac9954dbb24a0c5516b0ecfcb8f63602203acbeea534d1158c570e709ba2d6c40a953cf96f812c70eb4a325a5ce471b18c01695221024320e8c9816ab3f498028b68e4789dfa633159d5bf40b92382e5de6fef346c842103e084caf6e57333afd93aff0d3ffb5f8eaf0e6362535e573a7cd1a5b0e9a3e41421022a55231f3622790791c333f8750a44a970a228b493882e69e4bae69ec517c01753ae00000000";
        BtcTransaction btcTx = new BtcTransaction(networkParameters, Hex.decode(rawTx));

        BtcLockSender lockSender = new P2shP2wshBtcLockSender(btcTx);

        byte[] redeemScript = Hex.decode(
                "522103a01b7993e674a3fe7d303073e5c5b366589586873f38abc3f0270fd927a1a02c210321367e27d2b6dd49c1b07ec" +
                        "7def3fb99e13989b6c48a049c70ffc2d930d66c3921039998ef58c172f7a449e719263853b69815fca58f2b383c0" +
                        "ee7456c58daf341a253ae"
        );
        byte[] scriptPubKey = HashUtil.ripemd160(Sha256Hash.hash(redeemScript));

        Address btcAddress = new Address(btcTx.getParams(), btcTx.getParams().getP2SHHeader(), scriptPubKey);

        Assert.assertEquals(btcAddress.toString(), "2N7Z1x59hVvnSc7HpnSigg5k77hNUeBFQUA");
        Assert.assertEquals(btcAddress, lockSender.getBTCAddress());
        Assert.assertEquals(BtcLockSender.TxType.P2SHP2WSKH, lockSender.getType());
        Assert.assertNull(lockSender.getRskAddress());
    }
}
