package be.hcpl.devoxx.trilateration;

import java.util.Iterator;
import java.util.Vector;

import be.hcpl.devoxx.model.AccessPoint;
import be.hcpl.devoxx.model.PointF;
import be.hcpl.devoxx.model.ProjectSite;

/**
 * 
 * @author hanscappelle removed gui related
 *
 */
public class WeightedCentroidTrilateration extends AccessPointTrilateration {

	protected static float g = 1.3f;

	public WeightedCentroidTrilateration(ProjectSite projectSite) {
		super(projectSite);
	}
	
	@Override
	public PointF calculateAccessPointPosition(AccessPoint ap) {
		Vector<MeasurementDataSet> originalData = this.measurementData.get(ap);
		
		if (originalData.size() > 3) {
			Vector<MeasurementDataSet> data = new Vector<MeasurementDataSet>();

			float sumRssi = 0;
			
			for (Iterator<MeasurementDataSet> it = originalData.iterator(); it.hasNext();) {
				MeasurementDataSet dataSet = it.next();
				
				float newRssi = (float) Math.pow(Math.pow(10, dataSet.getRssi() / 20), g);
				sumRssi += newRssi;
				
				data.add(new MeasurementDataSet(dataSet.getX(), dataSet.getY(), newRssi));
			}

			float x = 0;
			float y = 0;
			
			for (Iterator<MeasurementDataSet> itd = data.iterator(); itd.hasNext();) {
				MeasurementDataSet dataSet = itd.next();
				
				float weight = dataSet.getRssi() / sumRssi;
				x += dataSet.getX() * weight;
				y += dataSet.getY() * weight;
			}
			
			return new PointF(x, y);
		} else {
			return null;
		}
	}

}
