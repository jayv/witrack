/*
 * Created on Dec 7, 2011
 * Author: Paul Woelfel
 * Email: frig@frig.at
 */
package at.fhstp.wificompass.userlocation;

import java.util.List;

import at.fhstp.wificompass.model.Location;

/**
 * @author   Paul Woelfel (paul@woelfel.at)
 */
public interface LocationService {

	/**
	 * return current location
	 * 
	 * @return current location
	 * @see at.fhstp.wificompass.model.Location
	 */
	Location getLocation();

	/**
	 * <p>
	 * update the location service with the current position.<br />
	 * This method checks which position is more accurate and uses the most accurate. If force is set, the location will be forced to overwrite the current location.
	 * </p>
	 * 
	 * @param pos current position
	 * @param force overwrite if set 
	 * @return 
	 */
	Location updateLocation(Location pos, boolean force);

	/**
	 * <p>
	 * update the location service with the current position.<br />
	 * This method checks which position is more accurate and uses the most accurate
	 * </p>
	 * @param pos current position
	 * @see LocationService#updateLocation(Location, boolean)
	 */
	void updateLocation(Location pos);
	
	/**
	 * <p>register a new LocationProvider<br />
	 * should call the LocationProvider.setLocationService</p>
	 * @param provider new LocationProvider
	 * @see LocationProvider
	 */
	void registerProvider(LocationProvider provider);
	
	/**
	 * <p>unregister a  LocationProvider<br />
	 * should call the LocationProvider.setLocationService</p>
	 * @param provider currently registerd LocationProvider
	 * @see LocationProvider
	 */
	void unregisterProvider(LocationProvider provider);
	
	
	/**
	 * <p>register a new LocationProvider by its class name.<br />
	 * This method uses the default constructor of the object to create an instance</p>
	 * @param provider classname of the LocationProvider
	 * @throws InstantiationException if the Class could not be instanced or the class is not a subclass of LocationProvider 
	 * @throws IllegalAccessException if the Class could not be accessed
	 * @throws ClassNotFoundException if the Class could not be found
	 */
	void registerProvider(String provider) throws InstantiationException, IllegalAccessException, ClassNotFoundException;

	
	/**
	 * get a list of currently registered LocationProviders
	 * @return List of registered LocationProviders
	 */
	List<LocationProvider> getLocationProviders();
	
	
	/**
	 * <p>define in which angle the magnetic north is to the map</p> <p>The angle must be between 0 and 2*π.</p>
	 * @param  angle
	 * @uml.property  name="relativeNorth"
	 */
	void setRelativeNorth(float angle);
	
	/**
	 * @return   angle of the direction to magnetic north
	 * @uml.property  name="relativeNorth"
	 */
	float getRelativeNorth();
	
	/**
	 * <p>set the grid spacing, defining one meter in the grid.<br />
	 * The default value is 30 for x and y, meaning that one meter are 30 pixels.
	 * </p> 
	 * 
	 * @param x
	 * @param y
	 */
	public void setGridSpacing(float x,float y);
	
	/**
	 * @return the x grid spacing
	 */
	public float getGridSpacingX();
	
	/**
	 * @return the y grid spacing
	 */
	public float getGridSpacingY();
	
}
