package red.felnull.katyouvotifier.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import red.felnull.katyouvotifier.KatyouVotifier;

public class KVReloadCommand {
    public static void register(CommandDispatcher<CommandSource> d) {
        d.register(Commands.literal("kvreload").requires((source) -> source.hasPermission(2)).executes(n -> reload(n.getSource())));
    }

    public static int reload(CommandSource src) {
        src.sendSuccess(new StringTextComponent("Reloading NuVotifier..."), true);
        if (KatyouVotifier.PLUGIN.reload()) {
            src.sendSuccess(new StringTextComponent("NuVotifier has been reloaded!"), true);
        } else {
            src.sendFailure(new StringTextComponent("Looks like there was a problem reloading NuVotifier, check the console!"));
        }
        return 1;
    }
}
