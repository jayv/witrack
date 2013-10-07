package be.hcpl.devoxx.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * @author Paul Woelfel (paul@woelfel.at)
 * @author hanscappelle the AP will be the scanned devices in our scenario,
 *         limited to min
 */
public class AccessPoint {

	/**
	 * @uml.property name="id"
	 */
	protected int id = 0;

	/**
	 * @uml.property name="bssid"
	 */
	protected String bssid;

	/**
	 * @uml.property name="ssid"
	 */
	protected String ssid;

	/**
	 * @uml.property name="location"
	 * @uml.associationEnd
	 */
	protected Location location;

	/**
	 * @uml.property name="projectSite"
	 * @uml.associationEnd
	 */
	protected ProjectSite projectSite;

	/**
	 * @uml.property name="calculated"
	 */
	protected boolean calculated = true;

	public AccessPoint() {

	}

	public AccessPoint(BssidResult bssidResult) {
		this.bssid = bssidResult.getBssid();
		this.ssid = bssidResult.getSsid();
	}

	public AccessPoint(AccessPoint copy) {
		this.bssid = copy.bssid;
		ssid = copy.ssid;
		calculated = copy.calculated;
		if (copy.location != null)
			location = new Location(copy.location);
		else
			location = null;
		projectSite = copy.projectSite;
	}

	/**
	 * @return
	 * @uml.property name="ssid"
	 */
	public String getSsid() {
		return this.ssid;
	}

	/**
	 * @return
	 * @uml.property name="bssid"
	 */
	public String getBssid() {
		return this.bssid;
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
	 * @return the projectSite
	 * @uml.property name="projectSite"
	 */
	public ProjectSite getProjectSite() {
		return projectSite;
	}

	/**
	 * @param projectSite
	 *            the projectSite to set
	 * @uml.property name="projectSite"
	 */
	public void setProjectSite(ProjectSite projectSite) {
		this.projectSite = projectSite;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

	/**
	 * @return the calculated
	 * @uml.property name="calculated"
	 */
	public boolean isCalculated() {
		return calculated;
	}

	/**
	 * @param calculated
	 *            the calculated to set
	 * @uml.property name="calculated"
	 */
	public void setCalculated(boolean calculated) {
		this.calculated = calculated;
	}

	/**
	 * @return the id
	 * @uml.property name="id"
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param bssid
	 *            the bssid to set
	 * @uml.property name="bssid"
	 */
	public void setBssid(String bssid) {
		this.bssid = bssid;
	}

	/**
	 * @param ssid
	 *            the ssid to set
	 * @uml.property name="ssid"
	 */
	public void setSsid(String ssid) {
		this.ssid = ssid;
	}
}
