package org.polyfrost.example.game;

import net.minecraft.util.AxisAlignedBB;

public class Gate {
    private final int x;   // world X * 10
    private final int y;   // world Y * 10
    private final int z;   // world Z * 10
    private final int yaw; // degrees, 0–359

    private float width = 3.0f;
    private float height = 1.5f;
    private float depth = 0.2f;

    // For Gson deserialization
    private Gate() {
        this(0, 0, 0, 0);
    }

    /** Construct from raw integer values (already scaled 10x / integer degrees) */
    public Gate(int x, int y, int z, int yaw) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
    }

    /** Construct from world coordinates (snaps to 0.1-block precision and integer degrees) */
    public Gate(double worldX, double worldY, double worldZ, float worldYaw) {
        this.x = (int) Math.round(worldX * 10);
        this.y = (int) Math.round(worldY * 10);
        this.z = (int) Math.round(worldZ * 10);
        this.yaw = Math.round(worldYaw) % 360;
    }

    // World-coordinate getters (used by renderer, collision, etc.)

    public double getX() { return x / 10.0; }
    public double getY() { return y / 10.0; }
    public double getZ() { return z / 10.0; }
    public float getYaw() { return yaw; }

    // Raw integer getters (used by RouteCodec)

    public int getRawX() { return x; }
    public int getRawY() { return y; }
    public int getRawZ() { return z; }
    public int getRawYaw() { return yaw; }

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
        double gx = getX();
        double gy = getY() + halfHeight;
        double gz = getZ();

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
