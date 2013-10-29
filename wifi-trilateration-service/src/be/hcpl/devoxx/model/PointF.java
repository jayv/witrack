package be.hcpl.devoxx.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * 
 * @author hanscappelle created this stub, holds x, y coord
 * 
 */
public class PointF {

	public float x;
	public float y;

	public PointF(float x, float y) {
		super();
		this.x = x;
		this.y = y;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

}
