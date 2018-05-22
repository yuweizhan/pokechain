import java.security.*;
import java.util.ArrayList;

class Transaction {
    String id;
    PublicKey sender;
    PublicKey recipient;
    float value;
    private byte[] signature;

    ArrayList<TransactionInput> inputs;
    ArrayList<TransactionOutput> outputs = new ArrayList<>();

    private static int sequence = 0;

    Transaction(PublicKey sender, PublicKey recipient, float value, ArrayList<TransactionInput> inputs) {
        this.sender = sender;
        this.recipient = recipient;
        this.value = value;
        this.inputs = inputs == null ? new ArrayList<>() : inputs;
    }

    void generateSignature(PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(value);
        signature = StringUtil.applyECDSASignature(privateKey, data);
    }

    boolean verifySignature() {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(value);
        return StringUtil.verifyECDSASignature(sender, data, signature);
    }

    boolean processTransaction() {
        if (!verifySignature())
            return false;

        for (TransactionInput input : inputs)
            input.transactionOutput = PokeChain.UTXOs.get(input.transactionOutputId);

        id = calculateHash();
        outputs.add(new TransactionOutput(this.recipient, value, id));
        outputs.add(new TransactionOutput(this.sender, getInputValue() - value, id));

        for (TransactionOutput output : outputs)
            PokeChain.UTXOs.put(output.id, output);

        for (TransactionInput input : inputs)
            if (input.transactionOutput != null)
                PokeChain.UTXOs.remove(input.transactionOutput.id);

        return true;
    }

    float getInputValue() {
        float total = 0;

        for (TransactionInput input : inputs)
            if (input.transactionOutput != null)
                total += input.transactionOutput.value;

        return total;
    }

    float getOutputValue() {
        float total = 0;

        for (TransactionOutput output : outputs)
            total += output.value;

        return total;
    }

    private String calculateHash() {
        sequence++;

        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(value) + sequence;
        return StringUtil.sha256(data);
    }
}
