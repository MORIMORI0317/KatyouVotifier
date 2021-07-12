package red.felnull.katyouvotifier.util;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

public class ServerUtils {
    public static MinecraftServer getMinecraftServer() {
        try {
            return LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        } catch (Throwable ex) {
            return null;
        }
    }
}
