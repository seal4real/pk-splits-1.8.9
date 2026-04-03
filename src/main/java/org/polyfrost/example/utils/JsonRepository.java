package org.polyfrost.example.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.polyfrost.example.ParkourSplits;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonRepository<T> {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path saveFile;
    private final Type type;

    public JsonRepository(Path saveFile, Type type) {
        this.saveFile = saveFile;
        this.type = type;
    }

    public void save(T value) {
        try {
            Files.createDirectories(saveFile.getParent());

            try (Writer writer = Files.newBufferedWriter(saveFile)) {
                GSON.toJson(value, writer);
            }
        } catch (IOException e) {
            ParkourSplits.LOGGER.error("Failed to save.", e);
        }
    }

    public T load() {
        if (!Files.exists(saveFile)) {
            return null;
        }

        try (Reader reader = Files.newBufferedReader(saveFile)) {
            return GSON.fromJson(reader, type);
        } catch (IOException e) {
            ParkourSplits.LOGGER.error("Failed to load.", e);
            return null;
        }
    }

    public void clear() {
        try {
            Files.deleteIfExists(saveFile);
        } catch (IOException e) {
            ParkourSplits.LOGGER.error("Failed to clear save.", e);
        }
    }
}
