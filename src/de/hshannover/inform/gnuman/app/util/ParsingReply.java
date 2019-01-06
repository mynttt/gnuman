package de.hshannover.inform.gnuman.app.util;

import de.hshannover.inform.gnuman.app.model.storage.MapData;

/**
 * Answer to a parsing request.
 * @author Marc Herschel
 */

public class ParsingReply {
        MapData data;
        ParsingStatus status;

        ParsingReply(MapData data, ParsingStatus status) { this.data = data; this.status = status; }
        public ParsingStatus status() { return status;  }
        public MapData data() { return data; }
    }
