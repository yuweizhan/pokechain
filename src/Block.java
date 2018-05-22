import java.util.ArrayList;
import java.util.Date;

class Block {
    String hash;
    String previousHash;
    ArrayList<Transaction> transactions = new ArrayList<>();

    private String merkleRoot;
    private long timestamp;
    private int nonce;

    Block(String previousHash) {
        this.previousHash = previousHash;
        this.timestamp = new Date().getTime();
        this.hash = calculateHash();
    }

    String calculateHash() {
        return StringUtil.sha256(previousHash + Long.toString(timestamp) + Integer.toString(nonce) + merkleRoot);
    }

    void mineBlock(int difficulty) {
        merkleRoot = StringUtil.getMerkleRoot(transactions);
        String hashTarget = StringUtil.getHashTarget(difficulty);
        while (!hash.substring(0, difficulty).equals(hashTarget)) {
            nonce++;
            hash = calculateHash();
        }
    }

    void addTransaction(Transaction transaction) {
        if (transaction == null)
            return;

        if (!transaction.processTransaction())
            return;

        transactions.add(transaction);
    }
}
