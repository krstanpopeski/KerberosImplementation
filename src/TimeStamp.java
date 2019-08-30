import java.io.Serializable;
import java.time.LocalDateTime;


public class TimeStamp implements Serializable {
    private LocalDateTime creationTime;
    private LocalDateTime expireTime;

    public TimeStamp(){
        creationTime = LocalDateTime.now();
        expireTime = creationTime.plusHours(8);
    }

    @Override
    public String toString() {
        String createTime = creationTime.toString();
        String expTime = expireTime.toString();

        return "Time of creation: " + createTime + "\nTime of expiration: "  + expTime;

    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }
}
