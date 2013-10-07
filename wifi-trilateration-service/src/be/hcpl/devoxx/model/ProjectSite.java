/*
 * Created on Dec 8, 2011
 * Author: Paul Woelfel
 * Email: frig@frig.at
 */
package be.hcpl.devoxx.model;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * The location where all is set up
 * 
 * @author Paul Woelfel (paul@woelfel.at)
 * @author hanscappelle rem unused properties, replaced tostring
 */
public class ProjectSite {

	/**
	 * @uml.property name="id"
	 */
	protected int id;

	/**
	 * @uml.property name="title"
	 */
	protected String title;

	/**
	 * @uml.property name="width"
	 */
	protected int width;

	/**
	 * @uml.property name="height"
	 */
	protected int height;

	/**
	 * @uml.property name="accessPoints"
	 */
	protected Collection<AccessPoint> accessPoints = new ArrayList<AccessPoint>();

	/**
	 * @uml.property name="scanResults"
	 */
	protected Collection<WifiScanResult> scanResults = new ArrayList<WifiScanResult>();

	/**
	 * @uml.property name="lastLocation"
	 * @uml.associationEnd
	 */
	protected Location lastLocation;

	public ProjectSite() {
		// TODO Auto-generated constructor stub
	}

	public ProjectSite(String title) {
		super();
		this.title = title;
		width = 0;
		height = 0;
	}

	/**
	 * @return
	 * @uml.property name="title"
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 * @uml.property name="title"
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return
	 * @uml.property name="id"
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the accessPoints
	 * @uml.property name="accessPoints"
	 */
	public Collection<AccessPoint> getAccessPoints() {
		return accessPoints;
	}

	/**
	 * @return the scanResults
	 * @uml.property name="scanResults"
	 */
	public Collection<WifiScanResult> getScanResults() {
		return scanResults;
	}

	/**
	 * @return the width
	 * @uml.property name="width"
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return the height
	 * @uml.property name="height"
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * set the size of the project site
	 * 
	 * @param width
	 * @param height
	 */
	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
	}

	/**
	 * @return the last known Location
	 * @uml.property name="lastLocation"
	 */
	public Location getLastLocation() {
		return lastLocation;
	}

	/**
	 * @param lastLocation
	 *            the lastLocation to set
	 * @uml.property name="lastLocation"
	 */
	public void setLastLocation(Location lastLocation) {
		this.lastLocation = lastLocation;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

}
