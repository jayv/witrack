package be.hcpl.devoxx;

import java.io.InputStreamReader;
import java.util.Map.Entry;
import java.util.Vector;

import be.hcpl.devoxx.model.AccessPoint;
import be.hcpl.devoxx.model.PointF;
import be.hcpl.devoxx.model.ProjectSite;
import be.hcpl.devoxx.trilateration.LocalSignalStrengthGradientTrilateration;
import be.hcpl.devoxx.trilateration.MeasurementDataSet;

import com.google.gson.Gson;

/**
 * 
 * @author hanscappelle
 * 
 */
public class Service {

	public static void main(final String[] args) throws Exception {

		System.out.println("starting");
		System.out.println("load config");

		// load project site from sample data
		Gson gson = new Gson();
		ProjectSite site = gson.fromJson(
				new InputStreamReader(Service.class.getResourceAsStream("/be/hcpl/devoxx/data/projectsite.json")),
				ProjectSite.class);

		// BufferedReader br = new BufferedReader(new
		// InputStreamReader(Service.class
		// .getResourceAsStream("projectsite.json")));
		// String ln = null;
		// while ((ln=br.readLine())!=null){
		// System.out.println(ln);
		// }

		System.out.println("project site loaded: " + site);

		// LOAD AP, not sure why we need to do this manually? Can be extracted
		// from the scan results, check Android App
		// System.out.println("loading AP");
		// WifiScanResult results = gson.fromJson(
		// new
		// InputStreamReader(Service.class.getResourceAsStream("/be/hcpl/devoxx/data/ap.json")),
		// AccessPoint.class);
		// System.out.println("results loaded: " + results);

		// calc positions here now
		System.out.println("calculating...");

		// site.getScanResults().add(results);

		LocalSignalStrengthGradientTrilateration alg = new LocalSignalStrengthGradientTrilateration(site);
		// alg.parseMeasurementData();
		Vector<PointF> points = alg.calculateAll();

		// TODO display positions

		// for (AccessPoint ap : site.getAccessPoints()) {
		// System.out.println(ap);
		// }

		for (Entry<AccessPoint, Vector<MeasurementDataSet>> pair : alg.getMeasurementData().entrySet()) {
			System.out.println(pair);
		}

		// for (PointF point : points) {
		// System.out.println(point);
		// }

		System.out.println("done");

	}
}
