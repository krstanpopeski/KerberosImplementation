public class KerberosImplementation {

    public static void main(String[] args) {
        KDC kdcServer = new KDC();
        User Alice = new User("Alice001", "winteriscoming", kdcServer);
        User Bob = new User("BobBo001", "weneversow", kdcServer);

        System.out.println("Registering users!");
        kdcServer.registerUser(Alice.getID(),"winteriscoming");
        kdcServer.registerUser(Bob.getID(), "weneversow");
        System.out.println("Successfully registered!");

        if(Alice.sendRequest(Bob)){
            Alice.sendMessage(Bob,"Test Message");
        }
    }
}
