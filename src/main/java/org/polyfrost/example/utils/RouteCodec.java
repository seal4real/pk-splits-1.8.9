package org.polyfrost.example.utils;

import org.polyfrost.example.game.Gate;
import org.polyfrost.example.game.Route;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Encodes and decodes routes as compact, shareable strings.
 *
 * <p>Binary format (v1):
 * <pre>
 *   [version: 1 byte]
 *   [startCount: 1 byte] [checkpointCount: 1 byte] [finishCount: 1 byte]
 *   [gate 0] [gate 1] ... [gate N]
 * </pre>
 *
 * <p>Each gate is 12 bytes:
 * <pre>
 *   x: int32  (world coordinate × 10, giving 0.1-block precision)
 *   y: int16  (world coordinate × 10)
 *   z: int32  (world coordinate × 10)
 *   yaw: int16 (degrees, 0–359)
 * </pre>
 *
 * <p>The raw bytes are DEFLATE-compressed and then Base64url-encoded (no padding).
 */
public final class RouteCodec {

    private static final byte VERSION = 1;
    private static final int BYTES_PER_GATE = 12; // 4 + 2 + 4 + 2

    private RouteCodec() {}

    // -------------------------------------------------------------------------
    // Encode
    // -------------------------------------------------------------------------

    public static String encode(Route route) {
        List<Gate> starts = route.getStartGates();
        List<Gate> checkpoints = route.getCheckpoints();
        List<Gate> finishes = route.getFinishGates();

        int gateCount = starts.size() + checkpoints.size() + finishes.size();
        int headerSize = 4; // version + 3 counts
        ByteBuffer buf = ByteBuffer.allocate(headerSize + gateCount * BYTES_PER_GATE);
        buf.order(ByteOrder.BIG_ENDIAN);

        // Header
        buf.put(VERSION);
        buf.put((byte) starts.size());
        buf.put((byte) checkpoints.size());
        buf.put((byte) finishes.size());

        // Gates in order: starts, checkpoints, finishes
        for (Gate g : starts)      writeGate(buf, g);
        for (Gate g : checkpoints) writeGate(buf, g);
        for (Gate g : finishes)    writeGate(buf, g);

        byte[] raw = buf.array();
        byte[] compressed = deflate(raw);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(compressed);
    }

    private static void writeGate(ByteBuffer buf, Gate gate) {
        buf.putInt(gate.getRawX());
        buf.putShort((short) gate.getRawY());
        buf.putInt(gate.getRawZ());
        buf.putShort((short) gate.getRawYaw());
    }

    // -------------------------------------------------------------------------
    // Decode
    // -------------------------------------------------------------------------

    public static Route decode(String encoded) throws IllegalArgumentException {
        byte[] compressed;
        try {
            compressed = Base64.getUrlDecoder().decode(encoded);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid route code: bad encoding.");
        }

        byte[] raw;
        try {
            raw = inflate(compressed);
        } catch (DataFormatException e) {
            throw new IllegalArgumentException("Invalid route code: corrupt data.");
        }

        if (raw.length < 4) {
            throw new IllegalArgumentException("Invalid route code: too short.");
        }

        ByteBuffer buf = ByteBuffer.wrap(raw);
        buf.order(ByteOrder.BIG_ENDIAN);

        byte version = buf.get();
        if (version != VERSION) {
            throw new IllegalArgumentException("Unsupported route code version: " + version);
        }

        int startCount = buf.get() & 0xFF;
        int checkpointCount = buf.get() & 0xFF;
        int finishCount = buf.get() & 0xFF;

        int expectedBytes = 4 + (startCount + checkpointCount + finishCount) * BYTES_PER_GATE;
        if (raw.length != expectedBytes) {
            throw new IllegalArgumentException("Invalid route code: unexpected length.");
        }

        Route route = new Route();
        for (int i = 0; i < startCount; i++)      route.addStartGate(readGate(buf));
        for (int i = 0; i < checkpointCount; i++)  route.addCheckpoint(readGate(buf));
        for (int i = 0; i < finishCount; i++)       route.addFinishGate(readGate(buf));

        return route;
    }

    private static Gate readGate(ByteBuffer buf) {
        int x = buf.getInt();
        int y = buf.getShort();
        int z = buf.getInt();
        int yaw = buf.getShort();
        return new Gate(x, y, z, yaw);
    }

    // -------------------------------------------------------------------------
    // Compression helpers
    // -------------------------------------------------------------------------

    private static byte[] deflate(byte[] data) {
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        deflater.setInput(data);
        deflater.finish();

        ByteArrayOutputStream out = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[256];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            out.write(buffer, 0, count);
        }
        deflater.end();
        return out.toByteArray();
    }

    private static byte[] inflate(byte[] compressed) throws DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(compressed);

        ByteArrayOutputStream out = new ByteArrayOutputStream(compressed.length * 2);
        byte[] buffer = new byte[256];
        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                if (count == 0 && inflater.needsInput()) break;
                out.write(buffer, 0, count);
            }
        } finally {
            inflater.end();
        }
        return out.toByteArray();
    }
}
