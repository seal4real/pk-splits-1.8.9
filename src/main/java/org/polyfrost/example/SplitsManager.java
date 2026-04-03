package org.polyfrost.example;

import cc.polyfrost.oneconfig.libs.universal.UChat;
import net.minecraft.client.Minecraft;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.polyfrost.example.game.Gate;
import org.polyfrost.example.game.Route;
import org.polyfrost.example.game.RunResult;
import org.polyfrost.example.game.RunTracker;
import org.polyfrost.example.hud.SplitHud;
import org.polyfrost.example.render.GateRenderer;
import org.polyfrost.example.utils.JsonRepository;
import org.polyfrost.example.utils.TimeUtils;

import java.io.File;
import java.util.List;

public class SplitsManager {

    private final JsonRepository<Route> routeRepository;
    private final JsonRepository<RunResult> pbRepository;

    private Route route;
    private final RunTracker runTracker;
    private RunResult personalBest;
    private RunResult previousRun;

    public final SplitHud splitHud;
    public final GateRenderer gateRenderer;

    private boolean wasInsideStart = false;
    private boolean enabled = true;

    public SplitsManager(SplitHud splitHud) {
        File configDir = new File(Minecraft.getMinecraft().mcDataDir, "config/pk-splits");

        this.routeRepository = new JsonRepository<>(
                new File(configDir, "routes.json").toPath(), Route.class);
        this.pbRepository = new JsonRepository<>(
                new File(configDir, "pb.json").toPath(), RunResult.class);

        this.route = routeRepository.load();
        if (route == null) route = new Route();

        this.personalBest = pbRepository.load();
        this.runTracker = new RunTracker(route);
        this.splitHud = splitHud;
        this.gateRenderer = new GateRenderer(route);
    }

    // -------------------------------------------------------------------------
    // Tick handler
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (!enabled || mc.thePlayer == null || mc.theWorld == null) return;

        AxisAlignedBB playerBox = mc.thePlayer.getEntityBoundingBox();

        // 1. Start gates — rising edge triggers a new run
        boolean insideStart = false;
        for (Gate g : route.getStartGates()) {
            if (g.intersects(playerBox)) { insideStart = true; break; }
        }
        if (insideStart && !wasInsideStart) {
            runTracker.reset();
            runTracker.hitStart();
            splitHud.showSplit(0);
        }
        wasInsideStart = insideStart;

        // 2. Checkpoint gates — only while run is active
        if (runTracker.isActive() && !runTracker.isFinished()) {
            List<Gate> checkpoints = route.getCheckpoints();
            for (int i = 0; i < checkpoints.size(); i++) {
                if (!runTracker.isCheckpointHit(i) && checkpoints.get(i).intersects(playerBox)) {
                    runTracker.hitCheckpoint(i);
                    long elapsed = runTracker.getCheckpointSplitTimesMillis().get(i);
                    if (personalBest != null && i < personalBest.getCheckpointSplitTimesMillis().size()) {
                        long delta = elapsed - personalBest.getCheckpointSplitTimesMillis().get(i);
                        splitHud.showSplit(elapsed, delta);
                    } else {
                        splitHud.showSplit(elapsed);
                    }
                }
            }
        }

        // 3. Finish gates — only once all checkpoints are hit
        if (runTracker.isActive() && !runTracker.isFinished() && runTracker.canHitFinish()) {
            for (Gate g : route.getFinishGates()) {
                if (g.intersects(playerBox)) {
                    runTracker.hitFinish();
                    RunResult result = runTracker.toResult();
                    long elapsed = result.getTotalTimeMillis();
                    if (personalBest != null) {
                        long delta = elapsed - personalBest.getTotalTimeMillis();
                        splitHud.showSplit(elapsed, delta);
                    } else {
                        splitHud.showSplit(elapsed);
                    }
                    if (personalBest == null || result.isFasterThan(personalBest)) {
                        personalBest = result;
                        pbRepository.save(personalBest);
                    }
                    previousRun = result;
                    runTracker.reset();
                    break;
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Command handlers
    // -------------------------------------------------------------------------

    public void addStart() {
        Minecraft mc = Minecraft.getMinecraft();
        route.addStartGate(new Gate(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw));
        onRouteModified();
        UChat.chat("Start gate placed.");
    }

    public void addCheckpoint() {
        Minecraft mc = Minecraft.getMinecraft();
        route.addCheckpoint(new Gate(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw));
        onRouteModified();
        UChat.chat("Checkpoint placed.");
    }

    public void addFinish() {
        Minecraft mc = Minecraft.getMinecraft();
        route.addFinishGate(new Gate(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw));
        onRouteModified();
        UChat.chat("Finish gate placed.");
    }

    public void removeGateAtPlayer() {
        AxisAlignedBB playerBox = Minecraft.getMinecraft().thePlayer.getEntityBoundingBox();

        List<Gate> startGates = route.getStartGates();
        for (int i = 0; i < startGates.size(); i++) {
            if (startGates.get(i).intersects(playerBox)) {
                route.removeStartAt(i);
                onRouteModified();
                UChat.chat("Removed start gate.");
                return;
            }
        }

        List<Gate> checkpoints = route.getCheckpoints();
        for (int i = 0; i < checkpoints.size(); i++) {
            if (checkpoints.get(i).intersects(playerBox)) {
                route.removeCheckpointAt(i);
                onRouteModified();
                UChat.chat("Removed checkpoint.");
                return;
            }
        }

        List<Gate> finishGates = route.getFinishGates();
        for (int i = 0; i < finishGates.size(); i++) {
            if (finishGates.get(i).intersects(playerBox)) {
                route.removeFinishAt(i);
                onRouteModified();
                UChat.chat("Removed finish gate.");
                return;
            }
        }

        UChat.chat("Not inside any gate.");
    }

    public void stats() {
        String prev = previousRun == null ? "None" : TimeUtils.formatMillis(previousRun.getTotalTimeMillis());
        String pb   = personalBest == null ? "None" : TimeUtils.formatMillis(personalBest.getTotalTimeMillis());
        UChat.chat("Previous run: " + prev);
        UChat.chat("Personal best: " + pb);
    }

    public void toggle() {
        enabled = !enabled;
        gateRenderer.setEnabled(enabled);
        runTracker.reset();
        UChat.chat("Splits " + (enabled ? "enabled." : "disabled."));
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private void onRouteModified() {
        routeRepository.save(route);
        personalBest = null;
        pbRepository.clear();
        runTracker.reset();
    }
}
