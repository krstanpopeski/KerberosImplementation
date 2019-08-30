import javax.crypto.SecretKey;
import java.io.Serializable;

public class YA implements Serializable {

    private SecretKey Kses;
    private String ra;
    private TimeStamp T;
    private String IDb;

    public YA(SecretKey kses, String ra, TimeStamp t, String IDb) {
        Kses = kses;
        this.ra = ra;
        T = t;
        this.IDb = IDb;
    }

    public SecretKey getKses() {
        return Kses;
    }

    public String getRa() {
        return ra;
    }

    public TimeStamp getT() {
        return T;
    }

    public String getIDb() {
        return IDb;
    }
}
