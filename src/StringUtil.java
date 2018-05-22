import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

class StringUtil {
    static String sha256(String input) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);

                if (hex.length() == 1)
                    hexString.append('0');

                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static byte[] applyECDSASignature(PrivateKey privateKey, String input) {
        try {
            Signature signature = Signature.getInstance("ECDSA", "BC");
            signature.initSign(privateKey);
            signature.update(input.getBytes());

            return signature.sign();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static boolean verifyECDSASignature(PublicKey publicKey, String data, byte[] signatureToVerify) {
        try {
            Signature signature = Signature.getInstance("ECDSA", "BC");
            signature.initVerify(publicKey);
            signature.update(data.getBytes());

            return signature.verify(signatureToVerify);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static String getStringFromKey(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    static String getHashTarget(int difficulty) {
        char[] chars = new char[difficulty];
        Arrays.fill(chars, '0');

        return new String(chars);
    }

    static String getMerkleRoot(ArrayList<Transaction> transactions) {
        int count = transactions.size();
        ArrayList<String> previousTreeLayer = new ArrayList<>();

        for (Transaction transaction : transactions)
            previousTreeLayer.add(transaction.id);

        ArrayList<String> treeLayer = previousTreeLayer;

        while (count > 1) {
            treeLayer = new ArrayList<>();

            for (int i = 1; i < previousTreeLayer.size(); i++)
                treeLayer.add(sha256(previousTreeLayer.get(i - 1) + previousTreeLayer.get(i)));

            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }

        return treeLayer.size() == 1 ? treeLayer.get(0) : "";
    }
}
