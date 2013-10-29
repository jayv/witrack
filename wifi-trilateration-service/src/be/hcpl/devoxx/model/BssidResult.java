/*
 * Created on Jan 20, 2012
 * Author: Paul Woelfel
 * Email: frig@frig.at
 */
package be.hcpl.devoxx.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * @author Paul Woelfel (paul@woelfel.at)
 * @author hanscappelle limited to min req data
 */
public class BssidResult {

	/**
	 * @uml.property name="id"
	 */
	protected int id;

	/**
	 * @uml.property name="bssid"
	 */
	protected String bssid; // mac address

	/**
	 * @uml.property name="ssid"
	 */
	protected String ssid; // name

	/**
	 * @uml.property name="level"
	 */
	protected int level;

	/**
	 * @uml.property name="scanResult"
	 * @uml.associationEnd
	 */
	protected WifiScanResult scanResult;

	public BssidResult() {

	}

	public BssidResult(final String bssid, final String ssid, final int level, final WifiScanResult result) {
		this.bssid = bssid;
		this.ssid = ssid;
		this.level = level;
		scanResult = result;
	}

	/**
	 * @return the bssid
	 * @uml.property name="bssid"
	 */
	public String getBssid() {
		return bssid;
	}

	/**
	 * @param bssid
	 *            the bssid to set
	 * @uml.property name="bssid"
	 */
	public void setBssid(final String bssid) {
		this.bssid = bssid;
	}

	/**
	 * @return the ssid
	 * @uml.property name="ssid"
	 */
	public String getSsid() {
		return ssid;
	}

	/**
	 * @param ssid
	 *            the ssid to set
	 * @uml.property name="ssid"
	 */
	public void setSsid(final String ssid) {
		this.ssid = ssid;
	}

	/**
	 * @return the level
	 * @uml.property name="level"
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @param level
	 *            the level to set
	 * @uml.property name="level"
	 */
	public void setLevel(final int level) {
		this.level = level;
	}

	/**
	 * @return the id
	 * @uml.property name="id"
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the scanResult
	 * @uml.property name="scanResult"
	 */
	public WifiScanResult getScanResult() {
		return scanResult;
	}

	/**
	 * @param scanResult
	 *            the scanResult to set
	 * @uml.property name="scanResult"
	 */
	public void setScanResult(final WifiScanResult scanResult) {
		this.scanResult = scanResult;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

}
