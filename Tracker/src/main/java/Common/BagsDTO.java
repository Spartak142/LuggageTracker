package Common;

import java.io.Serializable;

public interface BagsDTO extends Serializable {

    public String getOwnerName();

    public int getId();

    public int getNumber();

    public String getRoom();

    public String getStoredBy();
}
