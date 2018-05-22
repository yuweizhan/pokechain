import java.security.PublicKey;

class TransactionOutput {
    String id;
    PublicKey recipient;
    float value;

    TransactionOutput(PublicKey recipient, float value, String transactionId) {
        this.recipient = recipient;
        this.value = value;

        String data = StringUtil.getStringFromKey(recipient) + Float.toString(value) + transactionId;
        this.id = StringUtil.sha256(data);
    }

    boolean belongTo(PublicKey publicKey) {
        return publicKey == recipient;
    }
}
