package dev.fastlaunch;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;

/**
 * Ao entrar num mundo, comeca com poucas chunks em volta do jogador e vai aumentando
 * a distancia aos poucos ate o valor escolhido. No singleplayer o servidor interno
 * acompanha essa opcao, entao as chunks distantes so sao geradas depois.
 */
public final class ViewDistanceRamp {
    private static boolean active;
    private static int target;
    private static int ticks;

    private ViewDistanceRamp() {}

    public static void start(MinecraftClient client) {
        FastLaunchConfig cfg = FastLaunchConfig.get();
        if (!cfg.rampViewDistance || !client.isInSingleplayer()) {
            return;
        }
        SimpleOption<Integer> option = client.options.getViewDistance();
        int wanted = option.getValue();
        int start = Math.min(cfg.rampStartDistance, wanted);
        if (wanted <= start) {
            return;
        }
        target = wanted;
        ticks = 0;
        active = true;
        option.setValue(start);
        FastLaunchClient.LOGGER.info("Distancia de renderizacao: {} -> {} aos poucos", start, wanted);
    }

    public static void tick(MinecraftClient client) {
        if (!active) {
            return;
        }
        if (++ticks % FastLaunchConfig.get().rampStepTicks != 0) {
            return;
        }
        SimpleOption<Integer> option = client.options.getViewDistance();
        int next = option.getValue() + 1;
        if (next >= target) {
            option.setValue(target);
            active = false;
        } else {
            option.setValue(next);
        }
    }

    /** Devolve o valor original do jogador (ao sair do mundo ou fechar o jogo no meio da subida). */
    public static void restore(MinecraftClient client) {
        if (active) {
            client.options.getViewDistance().setValue(target);
            active = false;
        }
    }
}
