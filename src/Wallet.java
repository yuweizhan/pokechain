import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;

class Wallet {
    PrivateKey privateKey;
    PublicKey publicKey;

    private HashMap<String, TransactionOutput> UTXOs = new HashMap<>();

    Wallet() {
        generateKeyPair();
    }

    float getBalance() {
        float total = 0;

        for (TransactionOutput output : PokeChain.UTXOs.values()) {
            if (output.belongTo(publicKey)) {
                UTXOs.put(output.id, output);
                total += output.value;
            }
        }

        return total;
    }

    Transaction sendCoin(PublicKey recipient, float value) {
        if (getBalance() < value)
            return null;

        ArrayList<TransactionInput> inputs = new ArrayList<>();
        float total = 0;

        for (TransactionOutput output : UTXOs.values()) {
            total += output.value;
            inputs.add(new TransactionInput(output.id));

            if (total > value)
                break;
        }

        Transaction transaction = new Transaction(publicKey, recipient, value, inputs);
        transaction.generateSignature(privateKey);

        for (TransactionInput input : inputs)
            UTXOs.remove(input.transactionOutputId);

        return transaction;
    }

    private void generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec("prime192v1");

            keyPairGenerator.initialize(ecGenParameterSpec, secureRandom);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
