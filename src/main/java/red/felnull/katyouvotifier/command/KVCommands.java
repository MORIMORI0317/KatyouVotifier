package red.felnull.katyouvotifier.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;

public class KVCommands {
    public static void registerCommand(CommandDispatcher<CommandSource> d) {
        KVReloadCommand.register(d);
    }
}
