package us.talabrek.ultimateskyblock.command.island;

import com.google.inject.Inject;
import dk.lockfuglsang.minecraft.command.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;

/**
 * Does either CreateCommand or HomeCommand depending on state.
 */
public class AutoCommand extends AbstractCommand {
    private final uSkyBlock plugin;
    private final CreateCommand create;
    private final HomeCommand home;

    @Inject
    public AutoCommand(@NotNull uSkyBlock plugin, @NotNull CreateCommand create, @NotNull HomeCommand home) {
        super("auto", "usb.island.create", marktr("teleports you to your island (or create one)"));
        this.plugin = plugin;
        this.create = create;
        this.home = home;
    }

    @Override
    public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
        if (sender instanceof Player) {
            PlayerInfo playerInfo = plugin.getPlayerInfo((Player) sender);
            if (playerInfo != null && playerInfo.getHasIsland()) {
                return home.execute(sender, "home", null);
            } else {
                return create.execute(sender, "create", null);
            }
        }
        return false;
    }
}
