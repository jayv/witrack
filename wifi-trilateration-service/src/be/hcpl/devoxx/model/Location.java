/*
 * Created on Dec 8, 2011
 * Author: Paul Woelfel
 * Email: frig@frig.at
 */
package be.hcpl.devoxx.model;

import java.util.Date;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * @author Paul Woelfel
 * @author hanscappelle repl tostring, really needed?? check diff with pointf
 */
public class Location {

	/**
	 * @uml.property name="id"
	 */
	protected int id;

	/**
	 * @uml.property name="x"
	 */
	protected float x;

	/**
	 * @uml.property name="y"
	 */
	protected float y;

	// protected float z;

	/**
	 * @uml.property name="accurancy"
	 */
	protected float accurancy;

	/**
	 * @uml.property name="provider"
	 */
	protected String provider;

	/**
	 * @uml.property name="timestamp"
	 */
	protected Date timestamp;

	/**
	 * @uml.property name="timestampmilis"
	 */
	protected long timestampmilis;

	public Location() {
		this(0, 0, -1, null);
	}

	/**
	 * 
	 */
	public Location(float x, float y) {
		this(x, y, -1, null);
	}

	public Location(float x, float y, float accurancy) {
		this(x, y, -1, null);
	}

	public Location(float x, float y, float accurancy, Date timestamp) {
		this.x = x;
		this.y = y;
		this.accurancy = accurancy;
		if (timestamp == null) {
			this.timestamp = new Date();
		} else {
			this.timestamp = timestamp;
		}
	}

	/**
	 * copy constructor
	 * 
	 * @param copy
	 */
	public Location(Location copy) {
		x = copy.x;
		y = copy.y;
		// z=copy.z;
		accurancy = copy.accurancy;
		provider = copy.provider;
		timestamp = copy.timestamp;
		timestampmilis = copy.timestampmilis;
	}

	/**
	 * @return
	 * @uml.property name="x"
	 */
	public float getX() {
		return x;
	}

	/**
	 * @param x
	 * @uml.property name="x"
	 */
	public void setX(float x) {
		this.x = x;
	}

	/**
	 * @return
	 * @uml.property name="y"
	 */
	public float getY() {
		return y;
	}

	/**
	 * @param y
	 * @uml.property name="y"
	 */
	public void setY(float y) {
		this.y = y;
	}

	/**
	 * @return
	 * @uml.property name="accurancy"
	 */
	public float getAccurancy() {
		return accurancy;
	}

	/**
	 * @param accurancy
	 * @uml.property name="accurancy"
	 */
	public void setAccurancy(float accurancy) {
		this.accurancy = accurancy;
	}

	/**
	 * @return
	 * @uml.property name="provider"
	 */
	public String getProvider() {
		return provider;
	}

	/**
	 * @param provider
	 * @uml.property name="provider"
	 */
	public void setProvider(String provider) {
		this.provider = provider;
	}

	/**
	 * @return
	 * @uml.property name="timestamp"
	 */
	public Date getTimestamp() {
		if (timestamp == null) {
			timestamp = new Date(timestampmilis);
		}
		return timestamp;
	}

	/**
	 * @param timestamp
	 * @uml.property name="timestamp"
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
		timestampmilis = timestamp.getTime();
	}

	/**
	 * @return the timestampmilis
	 * @uml.property name="timestampmilis"
	 */
	public long getTimestampmilis() {
		return timestampmilis;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

	/**
	 * @return the id
	 * @uml.property name="id"
	 */
	public int getId() {
		return id;
	}

}
