package org.polyfrost.example;

import cc.polyfrost.oneconfig.libs.universal.UChat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.polyfrost.example.game.Gate;
import org.polyfrost.example.game.Route;
import org.polyfrost.example.game.RunResult;
import org.polyfrost.example.game.RunTracker;
import org.polyfrost.example.hud.SplitHud;
import org.polyfrost.example.render.GateRenderer;
import org.polyfrost.example.utils.JsonRepository;
import org.polyfrost.example.utils.RouteCodec;
import org.polyfrost.example.utils.TimeUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Pattern;

public class SplitsManager {

    private static final String DEFAULT_ROUTE = "default";
    private static final Pattern VALID_NAME = Pattern.compile("[a-z0-9_-]+");

    private final File configDir;

    private JsonRepository<Route> routeRepository;
    private JsonRepository<RunResult> pbRepository;

    private String currentRouteName;
    private Route route;
    private RunTracker runTracker;
    private RunResult personalBest;
    private RunResult previousRun;

    public final SplitHud splitHud;
    public final GateRenderer gateRenderer;

    private boolean wasInsideStart = false;
    private boolean enabled = true;

    public SplitsManager(SplitHud splitHud) {
        this.configDir = new File(Minecraft.getMinecraft().mcDataDir, "config/pk-splits");
        this.splitHud = splitHud;

        this.currentRouteName = loadActiveRouteName();

        this.routeRepository = new JsonRepository<>(
                routeFile(currentRouteName).toPath(), Route.class);
        this.pbRepository = new JsonRepository<>(
                pbFile(currentRouteName).toPath(), RunResult.class);

        this.route = routeRepository.load();
        if (route == null) route = new Route();

        this.personalBest = pbRepository.load();
        this.runTracker = new RunTracker(route);
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

        runTracker.tick();

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
                    int elapsed = runTracker.getCheckpointSplitTicks().get(i);
                    if (personalBest != null && i < personalBest.getCheckpointSplitTicks().size()) {
                        int delta = elapsed - personalBest.getCheckpointSplitTicks().get(i);
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
                    int elapsed = result.getTotalTimeTicks();
                    if (personalBest != null) {
                        int delta = elapsed - personalBest.getTotalTimeTicks();
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
    // Gate command handlers
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
        UChat.chat("Active route: " + currentRouteName);
        String prev = previousRun == null ? "None" : TimeUtils.formatTicks(previousRun.getTotalTimeTicks());
        String pb   = personalBest == null ? "None" : TimeUtils.formatTicks(personalBest.getTotalTimeTicks());
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
    // Route management command handlers
    // -------------------------------------------------------------------------

    public void createRoute(String name) {
        name = name.toLowerCase();
        if (!VALID_NAME.matcher(name).matches()) {
            UChat.chat("Invalid route name. Use only a-z, 0-9, hyphens, and underscores.");
            return;
        }
        if (routeFile(name).exists()) {
            UChat.chat("Route '" + name + "' already exists.");
            return;
        }

        // Save an empty route to create the file
        new JsonRepository<>(routeFile(name).toPath(), Route.class).save(new Route());

        // Auto-checkout the new route
        switchToRoute(name);
        UChat.chat("Created and switched to route: " + name);
    }

    public void removeRoute(String name) {
        name = name.toLowerCase();
        if (!routeFile(name).exists()) {
            UChat.chat("Route '" + name + "' does not exist.");
            return;
        }
        if (name.equals(currentRouteName)) {
            UChat.chat("Cannot delete the active route. Checkout a different route first.");
            return;
        }

        routeFile(name).delete();
        pbFile(name).delete();
        UChat.chat("Deleted route: " + name);
    }

    public void checkoutRoute(String name) {
        name = name.toLowerCase();
        if (!routeFile(name).exists()) {
            UChat.chat("Route '" + name + "' does not exist.");
            return;
        }
        if (name.equals(currentRouteName)) {
            UChat.chat("Already on route: " + name);
            return;
        }

        switchToRoute(name);
        UChat.chat("Switched to route: " + name);
    }

    public void shareRoute() {
        int gateCount = route.getStartGates().size()
                + route.getCheckpoints().size()
                + route.getFinishGates().size();
        if (gateCount == 0) {
            UChat.chat("Current route has no gates to share.");
            return;
        }

        String code = RouteCodec.encode(route);
        GuiScreen.setClipboardString(code);

        ChatComponentText message = new ChatComponentText("Route code copied to clipboard: ");
        ChatComponentText codeText = new ChatComponentText(code);
        codeText.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY));
        message.appendSibling(codeText);
        Minecraft.getMinecraft().thePlayer.addChatMessage(message);
    }

    public void loadRoute(String name, String code) {
        name = name.toLowerCase();
        if (!VALID_NAME.matcher(name).matches()) {
            UChat.chat("Invalid route name. Use only a-z, 0-9, hyphens, and underscores.");
            return;
        }
        if (routeFile(name).exists()) {
            UChat.chat("Route '" + name + "' already exists. Choose a different name.");
            return;
        }

        Route decoded;
        try {
            decoded = RouteCodec.decode(code);
        } catch (IllegalArgumentException e) {
            UChat.chat(e.getMessage());
            return;
        }

        // Save the decoded route and switch to it
        new JsonRepository<>(routeFile(name).toPath(), Route.class).save(decoded);
        switchToRoute(name);
        int gates = decoded.getStartGates().size()
                + decoded.getCheckpoints().size()
                + decoded.getFinishGates().size();
        UChat.chat("Loaded route '" + name + "' with " + gates + " gates.");
    }

    public void listRoutes() {
        File routesDir = new File(configDir, "routes");
        if (!routesDir.isDirectory()) {
            UChat.chat("No routes found.");
            return;
        }

        File[] files = routesDir.listFiles((dir, n) -> n.endsWith(".json"));
        if (files == null || files.length == 0) {
            UChat.chat("No routes found.");
            return;
        }

        UChat.chat("Routes:");
        for (File f : files) {
            String name = f.getName().replace(".json", "");
            String marker = name.equals(currentRouteName) ? " *" : "";
            UChat.chat("  " + name + marker);
        }
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private void switchToRoute(String name) {
        this.currentRouteName = name;
        saveActiveRouteName(name);

        this.routeRepository = new JsonRepository<>(routeFile(name).toPath(), Route.class);
        this.pbRepository = new JsonRepository<>(pbFile(name).toPath(), RunResult.class);

        this.route = routeRepository.load();
        if (this.route == null) this.route = new Route();

        this.personalBest = pbRepository.load();
        this.previousRun = null;
        this.wasInsideStart = false;

        this.runTracker = new RunTracker(route);
        this.gateRenderer.setRoute(route);
    }

    private void onRouteModified() {
        routeRepository.save(route);
        personalBest = null;
        pbRepository.clear();
        runTracker.reset();
    }

    private File routeFile(String name) {
        return new File(configDir, "routes/" + name + ".json");
    }

    private File pbFile(String name) {
        return new File(configDir, "pb/" + name + ".json");
    }

    private String loadActiveRouteName() {
        File file = new File(configDir, "active-route.txt");
        if (file.exists()) {
            try {
                String name = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).trim();
                if (!name.isEmpty()) return name;
            } catch (IOException e) {
                PksMod.LOGGER.error("Failed to read active route.", e);
            }
        }
        return DEFAULT_ROUTE;
    }

    private void saveActiveRouteName(String name) {
        File file = new File(configDir, "active-route.txt");
        try {
            Files.createDirectories(file.getParentFile().toPath());
            Files.write(file.toPath(), name.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            PksMod.LOGGER.error("Failed to save active route.", e);
        }
    }
}
