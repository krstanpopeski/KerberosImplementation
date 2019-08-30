import java.io.Serializable;

public class YAB implements Serializable {

    private String IDa;
    private TimeStamp Ts;

    public YAB(String IDa, TimeStamp ts) {
        this.IDa = IDa;
        Ts = ts;
    }

    public String getIDa() {
        return IDa;
    }

    public TimeStamp getTs() {
        return Ts;
    }
}
