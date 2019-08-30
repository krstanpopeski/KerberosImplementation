import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class KDC {

    private HashMap<String, SecretKey> users;

    public KDC(){
        users = new HashMap<>();
    }

    public void registerUser(String ID, String password){
        byte [] salt = ID.getBytes();
        try {
            System.out.println("KDC generates "+ ID + "'s secret key.");
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
            users.put(ID,secret);
            System.out.println("User "+ ID + " has been registered.");
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    private byte [] encrypt(SecretKey key, byte [] byteArray){
        byte [] encrypted = null;
        try{
            System.out.println("Encrypting...");
            Cipher ci = Cipher.getInstance("AES");
            ci.init(Cipher.ENCRYPT_MODE, key);
            encrypted = ci.doFinal(byteArray);
        } catch(Exception e){
            System.out.println(e.getMessage());
        }
        System.out.println("Successfully encrypted.");
        return encrypted;
    }

    public List<byte []> receiveRequest(String IDa, String IDb, String nonce){
        byte [] encryptedYa = null;
        byte [] encryptedYb = null;
        List<byte []> encryptedData = new ArrayList<>();

        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            SecretKey sessionKey = keyGen.generateKey();
            TimeStamp T = new TimeStamp();

            SecretKey KA = users.get(IDa);
            SecretKey KB = users.get(IDb);

            YA ya = new YA(sessionKey,nonce,T,IDb);
            YB yb = new YB(sessionKey,IDa,T);

            byte[] yaPlain = ObjectToArray(ya);
            byte[] ybPlain = ObjectToArray(yb);

            encryptedYa = encrypt(KA,yaPlain);
            encryptedYb = encrypt(KB,ybPlain);

            encryptedData.add(encryptedYa);
            encryptedData.add(encryptedYb);

        } catch (Exception e){
            System.out.println(e.getMessage());
        }


        return encryptedData;
    }

    public byte [] ObjectToArray(Object o){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte [] array = null;
        try {
            ObjectOutput out = null;
            out = new ObjectOutputStream(bos);
            out.writeObject(o);
            out.flush();
            array = bos.toByteArray();
        } catch(Exception e){
            System.out.println(e.getMessage());
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return array;
    }

}
