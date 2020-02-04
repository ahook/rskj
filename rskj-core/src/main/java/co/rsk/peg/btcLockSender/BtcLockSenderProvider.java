package co.rsk.peg.btcLockSender;

import co.rsk.bitcoinj.core.BtcTransaction;

import java.util.Optional;

public class BtcLockSenderProvider {

    public Optional<BtcLockSender> tryGetBtcLockSender(BtcTransaction tx) {
        if (tx == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(new P2pkhBtcLockSender(tx));
        } catch(BtcLockSenderParseException e) {
            // Nothing to do...
        }
        try {
            return Optional.of(new P2shP2wpkhBtcLockSender(tx));
        } catch(BtcLockSenderParseException e) {
            // Nothing to do...
        }

        return Optional.empty();
    }
}


//Crear tarea deuda técnica, cambiar el enfoque del BtLockSenderProvider para que no use excepciones y vaya probando con los distintos parsers
    //BtcLockSender debería ser una clase concreta, tener varias intancias de un parser
    /*

    btcLockSender = parser1.tryParse(tx);
    if (btcLockSender.isPresent()) {
        return btcLockSender;
    }
    btcLockSender = parser2.tryParse(tx);
    if (btcLockSender.isPresent()) {
        return btcLockSender;
    }

     */
//Crear tarea deuda técnica, agregar logs cuando no se puede parsear el BtcLockSender y poner la causa