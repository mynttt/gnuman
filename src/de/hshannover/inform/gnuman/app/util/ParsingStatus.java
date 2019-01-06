package de.hshannover.inform.gnuman.app.util;

/**
 * Status of map parsing.
 * @author Marc Herschel
 */

public class ParsingStatus {
    int code, x, y;

    ParsingStatus(int code, int x, int y) {
        this.code = code;
        this.x = x;
        this.y = y;
    }

    public boolean isSuccess() {
        return code >= 0;
    }

    public String statusMessage() {
        return MapParser.lookupStatusCode(this);
    }
}
