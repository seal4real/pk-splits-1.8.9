package org.polyfrost.example.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import org.polyfrost.example.game.Gate;
import org.polyfrost.example.game.Route;

public class GateRenderer {

    private Route route;
    private boolean enabled = true;

    public GateRenderer(Route route) {
        this.route = route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!enabled || route.isEmpty()) return;

        Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
        if (viewer == null) return;

        // Interpolated camera position for smooth rendering
        float pt = event.partialTicks;
        double cx = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * pt;
        double cy = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * pt;
        double cz = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * pt;

        GlStateManager.pushMatrix();
        GlStateManager.translate(-cx, -cy, -cz);

        // Set up GL state for transparent unlit quads, visible from both sides
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glPolygonOffset(-1f, -10f); // Prevents z-fighting with the ground

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        for (Gate g : route.getStartGates()) {
            renderGate(wr, g, 0.3f, 0.8f, 0.2f, 0.4f);    // green
        }
        for (Gate g : route.getCheckpoints()) {
            renderGate(wr, g, 0.1f, 0.4f, 0.9f, 0.4f);    // blue
        }
        for (Gate g : route.getFinishGates()) {
            renderGate(wr, g, 0.95f, 0.15f, 0.15f, 0.4f); // red
        }

        tessellator.draw();

        // Restore GL state
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glPolygonOffset(0f, 0f);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();

        GlStateManager.popMatrix();
    }

    private void renderGate(WorldRenderer wr, Gate gate, float r, float g, float b, float a) {
        float yawRad = (float) Math.toRadians(gate.getYaw());

        float fx = (float) -Math.sin(yawRad); // Facing axis (into gate)
        float fz = (float)  Math.cos(yawRad);
        float rx = (float)  Math.cos(yawRad); // Right axis (along length of gate)
        float rz = (float)  Math.sin(yawRad);

        float cx = (float) gate.getX();
        float y  = (float) gate.getY();
        float cz = (float) gate.getZ();

        float hw = gate.getHalfWidth();
        float hd = gate.getHalfDepth();

        // Flat ground quad showing the gate's footprint
        wr.pos(cx - hw*rx - hd*fx, y, cz - hw*rz - hd*fz).color(r, g, b, a).endVertex();
        wr.pos(cx + hw*rx - hd*fx, y, cz + hw*rz - hd*fz).color(r, g, b, a).endVertex();
        wr.pos(cx + hw*rx + hd*fx, y, cz + hw*rz + hd*fz).color(r, g, b, a).endVertex();
        wr.pos(cx - hw*rx + hd*fx, y, cz - hw*rz + hd*fz).color(r, g, b, a).endVertex();
    }
}
