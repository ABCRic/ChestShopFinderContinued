package com.cyprias.ChestShopFinder.utils;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class MathUtil {
	//http://forums.bukkit.org/threads/lookat-and-move-functions.26768/#post-1014171
	
	public static final float DEGTORAD = 0.017453293F;
	public static final float RADTODEG = 57.29577951F;
	
	public static float getLookAtYaw(Entity loc, Entity lookat) {
		return getLookAtYaw(loc.getLocation(), lookat.getLocation());
	}

	public static float getLookAtYaw(Block loc, Block lookat) {
		return getLookAtYaw(loc.getLocation(), lookat.getLocation());
	}

	public static float getLookAtYaw(Location loc, Location lookat) {
		// Values of change in distance (make it relative)
		return getLookAtYaw(lookat.getX() - loc.getX(), lookat.getZ() - loc.getZ());
	}

	public static float getLookAtYaw(Vector motion) {
		return getLookAtYaw(motion.getX(), motion.getZ());
	}

	public static float getLookAtYaw(double dx, double dz) {
		float yaw = 0;
		// Set yaw
		if (dx != 0) {
			// Set yaw start value based on dx
			if (dx < 0) {
				yaw = 270;
			} else {
				yaw = 90;
			}
			yaw -= atan(dz / dx);
		} else if (dz < 0) {
			yaw = 180;
		}
		return -yaw - 90;
	}

	public static float getLookAtPitch(double motX, double motY, double motZ) {
		return getLookAtPitch(motY, length(motX, motZ));
	}

	public static float getLookAtPitch(double motY, double motXZ) {
		return -atan(motY / motXZ);
	}

	public static float atan(double value) {
		return RADTODEG * (float) Math.atan(value);
	}

	public static double length(double... values) {
		return Math.sqrt(lengthSquared(values));
	}

	public static double lengthSquared(double... values) {
		double rval = 0;
		for (double value : values) {
			rval += value * value;
		}
		return rval;
	}
	
	public static double degrees(double r) {
		return r * RADTODEG;
	}

	public static double AngleCoordsToCoords(double fX, double fY, double tX, double tY) {
		double dX = tX - fX;
		double dY = fY - tY;

		return degrees(Math.atan2(dX, dY));

	}

	public static String DegToDirection(double deg) {
		if (deg < 0)
			deg += 360;

		if (deg < 45)
			return "North";

		if (deg < 135)
			return "East";

		if (deg < 225)
			return "South";

		if (deg < 315)
			return "West";

		if (deg < 405)
			return "North";

		return String.valueOf(deg);
	}

    public static boolean hasMask(int flags, int mask) {
        return ((flags & mask) == mask);
    }
	
    public static int addMask(int flags, int mask){
    	return (flags |= mask);
    }
    public static int delMask(int flags, int mask){
    	return (flags &= ~mask);
    }
	

    
}
