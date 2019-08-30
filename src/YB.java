import javax.crypto.SecretKey;
import java.io.Serializable;

public class YB implements Serializable {

    private SecretKey Kses;
    private String IDa;
    private TimeStamp T;

    public YB(SecretKey kses, String IDa, TimeStamp t) {
        Kses = kses;
        this.IDa = IDa;
        T = t;
    }

    public SecretKey getKses() {
        return Kses;
    }

    public String getIDa() {
        return IDa;
    }

    public TimeStamp getT() {
        return T;
    }
}
