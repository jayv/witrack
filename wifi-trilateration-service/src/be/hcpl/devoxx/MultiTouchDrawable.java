package be.hcpl.devoxx;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * 
 * @author hanscappelle created this stub, needed only for x,y grid props
 * 
 */
public class MultiTouchDrawable {

	/**
	 * @uml.property name="gridSpacingX"
	 */
	protected static float gridSpacingX = 30;

	/**
	 * @uml.property name="gridSpacingY"
	 */
	protected static float gridSpacingY = 30;

	public static float getGridSpacingX() {
		return gridSpacingX;
	}

	public static void setGridSpacingX(float gridSpacingX) {
		MultiTouchDrawable.gridSpacingX = gridSpacingX;
	}

	public static float getGridSpacingY() {
		return gridSpacingY;
	}

	public static void setGridSpacingY(float gridSpacingY) {
		MultiTouchDrawable.gridSpacingY = gridSpacingY;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

}
