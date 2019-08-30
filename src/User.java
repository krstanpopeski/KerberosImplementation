import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.spec.KeySpec;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class User {

    private String ID;
    private String password;
    private SecretKey secretKey;
    private String nonce;
    private KDC KDCConnection;
    private SecretKey sessionKey;


    public User(String ID,String password, KDC kdc){
        this.ID = ID;
        this.password = password;
        this.secretKey = generateSecretKey();
        this.nonce = generateNonce();
        this.KDCConnection = kdc;
    }

    private SecretKey generateSecretKey(){
        byte [] salt = ID.getBytes();
        SecretKey secret = null;
        try {
            System.out.println("User "+ this.ID + " generates his secret key.");
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

        return secret;
    }

    private String generateNonce(){
        System.out.println("User "+this.getID()+" is generating his nonce.");
        String dateTimeString = LocalDateTime.now().toString();
        byte[] nonceByte = dateTimeString.getBytes();
        return Base64.getMimeEncoder().encodeToString(nonceByte);
    }

    private byte [] encrypt(SecretKey key, byte [] byteArray){
        byte [] encrypted = null;
        try{
            System.out.println("User " + this.getID() + "is encrypting data.");
            Cipher ci = Cipher.getInstance("AES");
            ci.init(Cipher.ENCRYPT_MODE, key);
            encrypted = ci.doFinal(byteArray);
        } catch(Exception e){
            System.out.println(e.getMessage());
        }
        return encrypted;
    }

    private byte [] decrypt(SecretKey key, byte [] encrypted){
        byte data [] = null;
        try{
            System.out.println("User " + this.getID() + "is decrypting data.");
            Cipher ci = Cipher.getInstance("AES");
            ci.init(Cipher.DECRYPT_MODE, key);
            data = ci.doFinal(encrypted);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        return data;
    }

    public boolean verifyNonce(String noncePrim){
        System.out.println("User " + this.getID() + "is verifying nonce.");
        if(this.nonce.equals(noncePrim)){
            return true;
        }
        else{
            return false;
        }
    }

    public boolean verifyIDB(String Idb, String Id){
        return(Idb.equals(Id));
    }

    public boolean verifyLifeTime(TimeStamp T){
        System.out.println("User " + this.getID() + "is verifying LifeTime.");
        if(T.getExpireTime().compareTo(LocalDateTime.now()) > 0){
            return true;
        }
        else{
            return false;
        }

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


    public boolean sendRequest(User bob){
        System.out.println("User " + this.getID() + "is sending a request to KDC server");
        List<byte []> encryptedData = KDCConnection.receiveRequest(this.ID, bob.getID(),nonce);
        byte [] decryptedYa = decrypt(secretKey,encryptedData.get(0));
        System.out.println();
        ByteArrayInputStream bis = new ByteArrayInputStream(decryptedYa);
        ObjectInput in = null;

        try{
            in = new ObjectInputStream(bis);
            YA ya = (YA) in.readObject();
            System.out.println("User " + this.getID() + " received server's response and it's verifying it.");
            if(verifyNonce(ya.getRa()) && verifyIDB(bob.ID,ya.getIDb()) && verifyLifeTime(ya.getT())){
                System.out.println("User " + this.getID() + "verified the response.");
                TimeStamp Ts = new TimeStamp();
                YAB yab = new YAB(this.ID,Ts);
                byte [] yabArray = ObjectToArray(yab);
                byte [] yabEncrypted = encrypt(ya.getKses(),yabArray);
                List<byte []> list = new ArrayList<>();
                list.add(yabEncrypted);
                list.add(encryptedData.get(1));
                this.sessionKey = ya.getKses();
                System.out.println("User " + this.getID() + "sent the messages to " + bob.getID() + ".");
                return bob.receiveRequest(list);

            }
            else{
                System.out.println("Authentication has failed!");
                return false;
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }

        return false;

    }

    public Object ArrayToObject(byte [] array){
        ByteArrayInputStream bis = new ByteArrayInputStream(array);
        ObjectInput in = null;
        Object o = null;
        try{
            in = new ObjectInputStream(bis);
            o = in.readObject();
        }catch (Exception e){
            System.out.println(e.getMessage());
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return o;
    }

    private boolean receiveRequest(List<byte []> data){
        byte [] decryptedYB = decrypt(this.secretKey,data.get(1));
        YB yb = (YB) ArrayToObject(decryptedYB);
        byte [] decryptedYAB = decrypt(yb.getKses(),data.get(0));
        YAB yab = (YAB) ArrayToObject(decryptedYAB);
        if(yb.getIDa().equals(yab.getIDa()) &&  verifyLifeTime(yb.getT()) && verifyLifeTime(yab.getTs())){
            System.out.println("User " + this.getID() + "received the messages, successfully decrypted and verified them.");
            this.sessionKey = yb.getKses();
            return true;
        }
        else{
            System.out.println("Authentication has failed!");
            return false;
        }



    }

    public void sendMessage(User user, String message){
        System.out.println("Message to be sent: " + message);
        byte [] array = ObjectToArray(message);
        byte [] encryptedArray = encrypt(this.sessionKey,array);
        user.receieveMessage(encryptedArray);
    }

    public void receieveMessage(byte [] encryptedData){
        byte [] decrypted = decrypt(this.sessionKey,encryptedData);
        String message = (String) ArrayToObject(decrypted);
        System.out.println("Message received: " +  message);
    }



    public String getID() {
        return ID;
    }
}
