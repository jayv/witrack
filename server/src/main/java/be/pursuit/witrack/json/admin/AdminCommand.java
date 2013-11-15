package be.pursuit.witrack.json.admin;

import be.pursuit.witrack.mongo.Scanner;

/**
 * @author Jo Voordeckers - jo.voordeckers@pursuit.be
 */
public class AdminCommand {

    private Type type;

    private Scanner updatedScanner;

    public static enum Type {

        GET_SCANNERS,
        UPDATE_SCANNER,
        GET_POSITIONS
    }

    public Type getType() {
        return type;
    }

    public void setType(final Type type) {
        this.type = type;
    }

    public Scanner getUpdatedScanner() {
        return updatedScanner;
    }

    public void setUpdatedScanner(final Scanner updatedScanner) {
        this.updatedScanner = updatedScanner;
    }
}
