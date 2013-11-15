package be.pursuit.witrack;

import be.pursuit.witrack.json.admin.AdminCommand;
import be.pursuit.witrack.json.admin.AdminResponse;
import be.pursuit.witrack.json.collector.Scan;
import be.pursuit.witrack.json.collector.ScanResult;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.InetSocketAddress;
import java.util.ArrayList;

/**
 * @author Jo Voordeckers - jo.voordeckers@pursuit.be
 */
@Singleton
public class MessageServer {

    private static final Logger LOG = LoggerFactory.getLogger(MessageServer.class);

    private Configuration config;

    private SocketIOServer server;

    @Inject
    private AdminService adminService;

    @Inject
    private Ingester ingester;

    public void start() {

        config = new Configuration();
        // config.setHostname("localhost");
        config.setPort(3000);

        server = new SocketIOServer(config);

        server.addEventListener("scan", ScanResult.class, new DataListener<ScanResult>() {

            public void onData(final SocketIOClient socketIOClient, final ScanResult scanResult, final AckRequest ackRequest) {

                String ipAddress = ((InetSocketAddress) socketIOClient.getRemoteAddress()).getAddress().getHostAddress();

                Scan[] scans = scanResult.getScans();

                ArrayList<Scan> filtered = new ArrayList<Scan>();
                for (int i = 0; i < scans.length; i++) {
                    Scan scan = scans[i];
//                    if (scan.getMac().equalsIgnoreCase("40:B3:95:51:A2:D1")) {
                        filtered.add(scan);
//                    }
                }
                scanResult.setScans(filtered.toArray(new Scan[filtered.size()]));

                ingester.ingest(scanResult, ipAddress);

            }
        });

        server.addEventListener("admin", AdminCommand.class, new DataListener<AdminCommand>() {

            public void onData(final SocketIOClient socketIOClient, final AdminCommand command, final AckRequest ackRequest) {
                AdminResponse adminResponse = adminService.handleCommand(command);
                socketIOClient.sendEvent("admin", adminResponse);
            }
        });


        server.start();
    }

    public void stop() {

        server.stop();
    }

}
