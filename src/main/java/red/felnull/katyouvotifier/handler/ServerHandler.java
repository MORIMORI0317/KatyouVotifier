package red.felnull.katyouvotifier.handler;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import red.felnull.katyouvotifier.KatyouVotifier;
import red.felnull.katyouvotifier.command.KVReloadCommand;
import red.felnull.katyouvotifier.util.ServerUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ServerHandler {
    private static final Logger LOGGER = LogManager.getLogger(ServerHandler.class);
    private static final Map<Runnable, Integer> TASKS = new HashMap<>();
    private static boolean stop;

    @SubscribeEvent

    public static void onServerStart(FMLServerStartingEvent e) {
        stop = false;
        if (!KatyouVotifier.PLUGIN.loadAndBind()) {
            LOGGER.error("Votifier did not initialize properly!");
        }
    }

    @SubscribeEvent
    public static void onServerStop(FMLServerStoppingEvent e) {
        stop = true;
        TASKS.clear();
        KatyouVotifier.PLUGIN.halt();
        LOGGER.error("Votifier disabled.");
    }

    public static void addServerRepeatTask(Runnable task, int delayTick, long repeatTime, TimeUnit unit) {
        addServerDelayTask(() -> {
            while (!stop) {
                task.run();
                try {
                    unit.sleep(repeatTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, delayTick);
    }

    public static void addServerDelayTask(Runnable task, int delayTick) {
        TASKS.put(task, ServerUtils.getMinecraftServer().getTickCount() + delayTick);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        int tick = ServerUtils.getMinecraftServer().getTickCount();
        TASKS.forEach((n, m) -> {
            if (m <= tick) {
                n.run();
            }
        });
    }

    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent e) {
        KVReloadCommand.register(e.getDispatcher());
    }

}
