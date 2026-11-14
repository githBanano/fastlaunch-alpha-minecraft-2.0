package dev.fastlaunch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.fabricmc.loader.api.FabricLoader;

/** Configuracao simples em config/fastlaunch.properties. */
public final class FastLaunchConfig {
    private static FastLaunchConfig instance = new FastLaunchConfig();

    /** Remove a animacao de fade-in da tela de titulo. */
    public boolean skipTitleFade = true;

    /** Se true, aplica os ajustes leves de video abaixo ao iniciar (opt-in). */
    public boolean tuneVideoOptions = false;
    public int biomeBlendRadius = 1;
    public boolean entityShadows = false;

    /** Ao entrar num mundo (singleplayer), comeca com distancia de renderizacao baixa e sobe aos poucos ate o valor do jogador. */
    public boolean rampViewDistance = true;
    public int rampStartDistance = 4;
    /** Quantos ticks (20 = 1 segundo) entre cada +1 de distancia. */
    public int rampStepTicks = 30;

    private FastLaunchConfig() {}

    public static FastLaunchConfig get() {
        return instance;
    }

    public static void load() {
        Path file = FabricLoader.getInstance().getConfigDir().resolve("fastlaunch.properties");
        FastLaunchConfig cfg = new FastLaunchConfig();
        Properties p = new Properties();

        if (Files.exists(file)) {
            try (InputStream in = Files.newInputStream(file)) {
                p.load(in);
            } catch (IOException e) {
                FastLaunchClient.LOGGER.warn("Nao foi possivel ler a config, usando padroes", e);
            }
        }

        cfg.skipTitleFade = Boolean.parseBoolean(p.getProperty("skipTitleFade", "true"));
        cfg.tuneVideoOptions = Boolean.parseBoolean(p.getProperty("tuneVideoOptions", "false"));
        cfg.entityShadows = Boolean.parseBoolean(p.getProperty("entityShadows", "false"));
        cfg.biomeBlendRadius = readInt(p, "biomeBlendRadius", 1, 0, 7);
        cfg.rampViewDistance = Boolean.parseBoolean(p.getProperty("rampViewDistance", "true"));
        cfg.rampStartDistance = readInt(p, "rampStartDistance", 4, 2, 32);
        cfg.rampStepTicks = readInt(p, "rampStepTicks", 30, 5, 400);

        instance = cfg;
        save(file, cfg);
    }

    private static int readInt(Properties p, String key, int def, int min, int max) {
        try {
            int v = Integer.parseInt(p.getProperty(key, Integer.toString(def)).trim());
            return Math.max(min, Math.min(max, v));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static void save(Path file, FastLaunchConfig cfg) {
        Properties p = new Properties();
        p.setProperty("skipTitleFade", Boolean.toString(cfg.skipTitleFade));
        p.setProperty("tuneVideoOptions", Boolean.toString(cfg.tuneVideoOptions));
        p.setProperty("biomeBlendRadius", Integer.toString(cfg.biomeBlendRadius));
        p.setProperty("entityShadows", Boolean.toString(cfg.entityShadows));
        p.setProperty("rampViewDistance", Boolean.toString(cfg.rampViewDistance));
        p.setProperty("rampStartDistance", Integer.toString(cfg.rampStartDistance));
        p.setProperty("rampStepTicks", Integer.toString(cfg.rampStepTicks));
        try (OutputStream out = Files.newOutputStream(file)) {
            p.store(out, "FastLaunch - opcoes");
        } catch (IOException e) {
            FastLaunchClient.LOGGER.warn("Nao foi possivel salvar a config", e);
        }
    }
}
