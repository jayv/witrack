package be.pursuit.witrack.json.admin;

import be.pursuit.witrack.mongo.Position;
import be.pursuit.witrack.mongo.Scanner;

import java.util.List;

/**
 * @author Jo Voordeckers - jo.voordeckers@pursuit.be
 */
public class GetPositionsResponse extends AdminResponse {

    private List<Position> positions;

    public GetPositionsResponse(final List<Position> positions) {
        this.positions = positions;
    }

    public List<Position> getPositions() {
        return positions;
    }

    public void setPositions(final List<Position> positions) {
        this.positions = positions;
    }
}
