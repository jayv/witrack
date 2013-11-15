package be.pursuit.witrack;

import be.pursuit.witrack.json.admin.*;
import be.pursuit.witrack.mongo.Database;
import be.pursuit.witrack.mongo.Position;
import be.pursuit.witrack.mongo.Scanner;
import com.google.common.collect.Lists;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author Jo Voordeckers - jo.voordeckers@pursuit.be
 */
@Singleton
public class AdminService {

    @Inject
    private Database db;

    public AdminResponse handleCommand(AdminCommand command) {

        AdminResponse response;

        if (command.getType() == AdminCommand.Type.GET_SCANNERS) {

            response = getScanners();

        } else if (command.getType() == AdminCommand.Type.UPDATE_SCANNER) {

            response = updateScanner(command);

        } else if (command.getType() == AdminCommand.Type.GET_POSITIONS) {

            response = getPositions();

        } else {

            throw new IllegalArgumentException("Unexpected command: " + command.getType());
        }

        response.setType(command.getType());
        
        return response;
        
    }

    private GetPositionsResponse getPositions() {

        Date tenMinutesAgo = new Date(System.currentTimeMillis() - 10 * 60 * 1000);

        ArrayList<Position> positions = Lists.newArrayList(
                db.positions().find("db.positions.aggregate({$match : { time : { $gt: # }}}, " +
                        "{$group : {_id: \"$deviceId\",location: {$last: \"$location\"}, time: {$last: \"$time\"}}}, " +
                        "{$project : { _id : 0, deviceId: \"$_id\", location : 1, time: 1}} )",
                        tenMinutesAgo).as(Position.class));

        return new GetPositionsResponse(positions);

    }

    private GetScannersResponse getScanners() {

        Iterable<Scanner> allScanners = db.scanners().find("").as(Scanner.class);
        return new GetScannersResponse(Lists.newArrayList(allScanners));
    }

    private UpdatedScannerResponse updateScanner(final AdminCommand command) {

        db.scanners().update("{ _id : #}", command.getUpdatedScanner().get_id())
                .with(" { $set : { Location: # } } ", command.getUpdatedScanner().getLocation());

        Scanner updatedScanner = db.scanners().findOne(command.getUpdatedScanner().get_id()).as(Scanner.class);
        return new UpdatedScannerResponse(updatedScanner);
    }

}
