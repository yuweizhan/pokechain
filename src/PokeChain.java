import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class PokeChain {
    static HashMap<String, TransactionOutput> UTXOs = new HashMap<>();

    private static ArrayList<Block> blocks = new ArrayList<>();
    private static Transaction dummyTransaction;
    private static int DIFFICULTY = 3;

    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());

        Wallet wallet1 = new Wallet();
        Wallet wallet2 = new Wallet();
        Wallet dummy = new Wallet();

        dummyTransaction = new Transaction(dummy.publicKey, wallet1.publicKey, 100f, null);
        dummyTransaction.generateSignature(dummy.privateKey);
        dummyTransaction.id = "0";

        TransactionOutput dummyOutput = new TransactionOutput(dummyTransaction.recipient, dummyTransaction.value, dummyTransaction.id);
        dummyTransaction.outputs.add(dummyOutput);
        UTXOs.put(dummyOutput.id, dummyOutput);

        System.out.println("Mining the Genesis block...\n");
        Block genesisBlock = new Block("0");
        genesisBlock.addTransaction(dummyTransaction);
        System.out.println("Wallet 1's balance: " + wallet1.getBalance() + "\n");

        System.out.println("Mining block 1...\n");
        Block block1 = new Block(genesisBlock.hash);
        System.out.println("Wallet 1 is sending 40 Poke coins to Wallet 2...\n");
        block1.addTransaction(wallet1.sendCoin(wallet2.publicKey, 40f));
        addBlock(block1);
        System.out.println("Wallet 1's balance: " + wallet1.getBalance() + "\n");
        System.out.println("Wallet 2's balance: " + wallet2.getBalance() + "\n");

        System.out.println("Mining block 2...\n");
        Block block2 = new Block(block1.hash);
        System.out.println("Wallet 1 is sending 1000 Poke coins to Wallet 2...\n");
        block2.addTransaction(wallet1.sendCoin(wallet2.publicKey, 1000f));
        addBlock(block2);
        System.out.println("Wallet 1's balance: " + wallet1.getBalance() + "\n");
        System.out.println("Wallet 2's balance: " + wallet2.getBalance() + "\n");

        System.out.println("Mining block 3...\n");
        Block block3 = new Block(block2.hash);
        System.out.println("Wallet 2 is sending 20 Poke coins to Wallet 1...\n");
        block3.addTransaction(wallet2.sendCoin(wallet1.publicKey, 20f));
        System.out.println("Wallet 1's balance: " + wallet1.getBalance() + "\n");
        System.out.println("Wallet 2's balance: " + wallet2.getBalance() + "\n");

        System.out.println("Is chain valid: " + isChainValid());
    }

    private static void addBlock(Block block) {
        block.mineBlock(DIFFICULTY);
        blocks.add(block);
    }

    private static boolean isChainValid() {
        Block previousBlock, currentBlock;
        String hashTarget = StringUtil.getHashTarget(DIFFICULTY);

        HashMap<String, TransactionOutput> UTXOs = new HashMap<>();
        TransactionOutput outputInGenesisTransaction = dummyTransaction.outputs.get(0);
        UTXOs.put(outputInGenesisTransaction.id, outputInGenesisTransaction);

        for (int i = 1; i < blocks.size(); i++) {
            currentBlock = blocks.get(i);
            previousBlock = blocks.get(i - 1);

            if (!currentBlock.hash.equals(currentBlock.calculateHash()))
                return false;

            if (!previousBlock.hash.equals(currentBlock.previousHash))
                return false;

            if (!currentBlock.hash.substring(0, DIFFICULTY).equals(hashTarget))
                return false;

            for (int j = 0; j < currentBlock.transactions.size(); j++) {
                Transaction transaction = currentBlock.transactions.get(j);

                if (!transaction.verifySignature())
                    return false;

                if (transaction.getInputValue() != transaction.getOutputValue())
                    return false;

                for (TransactionInput input : transaction.inputs) {
                    TransactionOutput output = UTXOs.get(input.transactionOutputId);

                    if (output == null)
                        return false;

                    if (input.transactionOutput.value != output.value)
                        return false;

                    UTXOs.remove(input.transactionOutputId);
                }

                for (TransactionOutput output : transaction.outputs)
                    UTXOs.put(output.id, output);

                if (transaction.outputs.get(0).recipient != transaction.recipient)
                    return false;

                if (transaction.outputs.get(1).recipient != transaction.sender)
                    return false;
            }
        }

        return true;
    }
}