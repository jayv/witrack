package be.hcpl.devoxx.trilateration;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;

import be.hcpl.devoxx.model.AccessPoint;
import be.hcpl.devoxx.model.BssidResult;
import be.hcpl.devoxx.model.Location;
import be.hcpl.devoxx.model.PointF;
import be.hcpl.devoxx.model.ProjectSite;
import be.hcpl.devoxx.model.WifiScanResult;

/**
 * @author Paul Woelfel (paul@woelfel.at)
 * 
 * @author hanscappelle removed all gui related
 */
public abstract class AccessPointTrilateration {

	/**
	 * The project site. This is where the measurement data is located
	 * 
	 * @uml.property name="projectSite"
	 * @uml.associationEnd
	 */
	protected ProjectSite projectSite;

	/** The hash map that links access points to measurement data */
	protected HashMap<AccessPoint, Vector<MeasurementDataSet>> measurementData;

	public HashMap<AccessPoint, Vector<MeasurementDataSet>> getMeasurementData() {
		return measurementData;
	}

	/** The hash map that links BSSIDs to access points */
	protected HashMap<String, AccessPoint> accessPoints;

	/**
	 * The default constructor. Requires the context and the project site as
	 * parameters.
	 * 
	 * @param projectSite
	 */
	public AccessPointTrilateration(final ProjectSite projectSite) {
		this.projectSite = projectSite;
		parseMeasurementData();
	}

	/**
	 * Takes the measurement data from the project site and converts it into
	 * data that can be used by the algorithms. Requires that the projectSite is
	 * already set (which is forced by the constructor).
	 */
	protected void parseMeasurementData() {
		measurementData = new HashMap<AccessPoint, Vector<MeasurementDataSet>>();
		accessPoints = new HashMap<String, AccessPoint>();

		// Walk through all measuring points
		for (WifiScanResult result : projectSite.getScanResults()) {

			// Walk through all captured BSSIDs within the measuring point
			for (BssidResult bssidResult : result.getBssids()) {
				// Only calculate the BSSID location if the BSSID is selected
				// if (projectSite.isBssidSelected(bssidResult.getBssid())) {
				Vector<MeasurementDataSet> measurements = new Vector<MeasurementDataSet>();

				if (!accessPoints.containsKey(bssidResult.getBssid())) {
					accessPoints.put(bssidResult.getBssid(), new AccessPoint(bssidResult));
				}

				if (!measurementData.containsKey(accessPoints.get(bssidResult.getBssid()))) {
					measurements = new Vector<MeasurementDataSet>();
					measurementData.put(accessPoints.get(bssidResult.getBssid()), measurements);
				} else {
					measurements = measurementData.get(accessPoints.get(bssidResult.getBssid()));
				}

				Location loc = result.getLocation();
				measurements.add(new MeasurementDataSet(loc.getX(), loc.getY(), bssidResult.getLevel()));
				// }
			}
		}
	}

	/**
	 * Takes all measuring data it has, calculates the access points positions,
	 * creates <b>AccessPointDrawable</b>s and sets the relative positions of
	 * the access points. An <b>AccessPoint</b> object is handed over to the
	 * <b>AccessPointDrawable</b> so that the popup text can be set accordingly.
	 * 
	 * @return A vector of AccessPointDrawables
	 */
	public Vector<PointF> calculateAll() {
		Vector<PointF> aps = new Vector<PointF>();

		// for progress indication only
		// int count = 0;

		// if (progressDialog != null)
		// progressDialog.setMax(measurementData.entrySet().size());
		//

		for (Entry<AccessPoint, Vector<MeasurementDataSet>> pair : measurementData.entrySet()) {
			PointF position = calculateAccessPointPosition(pair.getKey());

			if (position != null) {
				pair.getKey().setLocation(new Location(position.x, position.y));
				// AccessPointDrawable ap = new AccessPointDrawable(context,
				// pair.getKey());
				// ap.setRelativePosition(position);
				aps.add(position);
			}

			// count ++;

			// if (progressDialog != null)
			// progressDialog.setProgress(count);

		}

		return aps;
	}

	/**
	 * Here the magic happens. The access point location is calculated with the
	 * measuring data given.
	 * 
	 * @param ap
	 *            The access point to calculate the position of
	 * @return The point where the access point is believed to be located
	 */
	public abstract PointF calculateAccessPointPosition(AccessPoint ap);

}
