/*
 * Created on Jan 20, 2012
 * Author: Paul Woelfel
 * Email: frig@frig.at
 */
package be.hcpl.devoxx.model;

import java.util.Collection;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * @author Paul Woelfel (paul@woelfel.at)
 * @author hanscappelle limited to min req, fixed some obj inits
 */
public class WifiScanResult {

	/**
	 * @uml.property name="id"
	 */
	protected int id;

	/**
	 * @uml.property name="timestamp"
	 */
	protected long timestamp;

	/**
	 * @uml.property name="bssids"
	 */
	protected Collection<BssidResult> bssids;

	/**
	 * @uml.property name="location"
	 * @uml.associationEnd
	 */
	protected Location location = new Location();

	/**
	 * @uml.property name="projectLocation"
	 * @uml.associationEnd
	 */
	protected ProjectSite projectLocation;

	public WifiScanResult() {

	}

	public WifiScanResult(long timestamp, Location location,
			ProjectSite projectLocation) {
		this.timestamp = timestamp;
		this.location = location;
		this.projectLocation = projectLocation;
	}

	/**
	 * @return the timestamp
	 * @uml.property name="timestamp"
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp
	 *            the timestamp to set
	 * @uml.property name="timestamp"
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return the location
	 * @uml.property name="location"
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * @param location
	 *            the location to set
	 * @uml.property name="location"
	 */
	public void setLocation(Location location) {
		this.location = location;
	}

	/**
	 * @return the projectLocation
	 * @uml.property name="projectLocation"
	 */
	public ProjectSite getProjectLocation() {
		return projectLocation;
	}

	/**
	 * @param projectLocation
	 *            the projectLocation to set
	 * @uml.property name="projectLocation"
	 */
	public void setProjectLocation(ProjectSite projectLocation) {
		this.projectLocation = projectLocation;
	}

	/**
	 * @return the id
	 * @uml.property name="id"
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the bssids
	 * @uml.property name="bssids"
	 */
	public Collection<BssidResult> getBssids() {
		return bssids;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

}
