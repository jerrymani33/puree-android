package com.cookpad.puree;

import com.google.gson.Gson;

import com.cookpad.puree.internal.LogDumper;
import com.cookpad.puree.outputs.PureeOutput;
import com.cookpad.puree.storage.PureeSQLiteStorage;
import com.cookpad.puree.storage.PureeStorage;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class PureeConfiguration {

    private final Context context;

    private final Gson gson;

    private final Map<Class<?>, List<PureeOutput>> sourceOutputMap;

    private final PureeStorage storage;

    public Context getContext() {
        return context;
    }

    public Gson getGson() {
        return gson;
    }

    public Map<Class<?>, List<PureeOutput>> getSourceOutputMap() {
        return sourceOutputMap;
    }

    public PureeStorage getStorage() {
        return storage;
    }

    public List<PureeOutput> getRegisteredOutputPlugins(Class<? extends PureeLog> logClass) {
        return sourceOutputMap.get(logClass);
    }

    public PureeLogRegistry createPureeLogRegistry() {
        return new PureeLogRegistry(sourceOutputMap, gson, storage);
    }

    PureeConfiguration(Context context, Gson gson, Map<Class<?>, List<PureeOutput>> sourceOutputMap, PureeStorage storage) {
        this.context = context;
        this.gson = gson;
        this.sourceOutputMap = sourceOutputMap;
        this.storage = storage;
    }

    /**
     * Print mapping of SOURCE -> FILTER... OUTPUT.
     */
    public void printMapping() {
        LogDumper.out(sourceOutputMap);
    }

    public static class Builder {
        private Context context;

        private Gson gson;

        private Map<Class<?>, List<PureeOutput>> sourceOutputMap = new HashMap<>();

        private PureeStorage storage;

        /**
         * Start building a new {@link com.cookpad.puree.PureeConfiguration} instance.
         */
        public Builder(Context context) {
            this.context = context.getApplicationContext();
        }

        /**
         * Specify the {@link com.google.gson.Gson} to serialize logs.
         */
        public Builder gson(Gson gson) {
            this.gson = gson;
            return this;
        }

        /**
         * Specify a source class of logs, which returns {@link Source} an
         * {@link Source#to(PureeOutput)} must be called to register an output plugin.
         */
        public Source source(Class<? extends PureeLog> logClass) {
            return new Source(this, logClass);
        }

        public Builder register(Class<?> logClass, PureeOutput output) {
            List<PureeOutput> outputs = sourceOutputMap.get(logClass);
            if (outputs == null) {
                outputs = new ArrayList<>();
            }
            outputs.add(output);
            sourceOutputMap.put(logClass, outputs);
            return this;
        }

        public Builder storage(PureeStorage storage) {
            this.storage = storage;
            return this;
        }

        /**
         * Create the {@link com.cookpad.puree.PureeConfiguration} instance.
         */
        public PureeConfiguration build() {
            if (gson == null) {
                gson = new Gson();
            }
            if (storage == null) {
                storage = new PureeSQLiteStorage(context);
            }
            return new PureeConfiguration(context, gson, sourceOutputMap, storage);
        }
    }
}
