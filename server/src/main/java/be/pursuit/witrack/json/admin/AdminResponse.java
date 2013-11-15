package be.pursuit.witrack.json.admin;

/**
 * @author Jo Voordeckers - jo.voordeckers@pursuit.be
 */
public abstract class AdminResponse {

    private AdminCommand.Type type;

    public AdminCommand.Type getType() {
        return type;
    }

    public void setType(final AdminCommand.Type type) {
        this.type = type;
    }
}
