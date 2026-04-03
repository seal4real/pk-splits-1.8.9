package org.polyfrost.example.game;

import net.minecraft.util.AxisAlignedBB;

public class Gate {
    private final double x;
    private final double y;
    private final double z;
    private final float yaw; // degrees, Minecraft convention (0 = south, 90 = west)

    private float width = 3.0f;
    private float height = 1.5f;
    private float depth = 0.2f;

    // For Gson deserialization
    private Gate() {
        this(0, 0, 0, 0f);
    }

    public Gate(double x, double y, double z, float yaw) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }

    public float getHalfWidth()  { return width / 2; }
    public float getHalfHeight() { return height / 2; }
    public float getHalfDepth()  { return depth / 2; }

    public boolean intersects(AxisAlignedBB player) {
        float yawRad = (float) Math.toRadians(yaw);

        double rx = Math.cos(yawRad);
        double rz = Math.sin(yawRad);
        double fx = -Math.sin(yawRad);
        double fz = Math.cos(yawRad);

        float halfWidth = getHalfWidth();
        float halfHeight = getHalfHeight();
        float halfDepth = getHalfDepth();

        // Gate centre
        double gx = x;
        double gy = y + halfHeight;
        double gz = z;

        // Player AABB centre and half-extents
        double pcx = (player.minX + player.maxX) * 0.5;
        double pcy = (player.minY + player.maxY) * 0.5;
        double pcz = (player.minZ + player.maxZ) * 0.5;
        double phx = (player.maxX - player.minX) * 0.5;
        double phy = (player.maxY - player.minY) * 0.5;
        double phz = (player.maxZ - player.minZ) * 0.5;

        // Vector between centres
        double dx = pcx - gx;
        double dy = pcy - gy;
        double dz = pcz - gz;

        // World X axis
        if (Math.abs(dx) > phx + Math.abs(halfWidth * rx) + Math.abs(halfDepth * fx)) return false;
        // World Y axis
        if (Math.abs(dy) > phy + halfHeight) return false;
        // World Z axis
        if (Math.abs(dz) > phz + Math.abs(halfWidth * rz) + Math.abs(halfDepth * fz)) return false;
        // Gate right axis
        if (Math.abs(dx * rx + dz * rz) > halfWidth + phx * Math.abs(rx) + phz * Math.abs(rz)) return false;
        // Gate facing axis
        if (Math.abs(dx * fx + dz * fz) > halfDepth + phx * Math.abs(fx) + phz * Math.abs(fz)) return false;

        return true;
    }
}