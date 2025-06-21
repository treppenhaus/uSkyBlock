package us.talabrek.ultimateskyblock.event;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dk.lockfuglsang.minecraft.util.ItemStackUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.talabrek.ultimateskyblock.challenge.Challenge;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.HashMap;
import java.util.Map;

/**
 * Events triggering the tool-menu
 */
@Singleton
public class ToolMenuEvents implements Listener {
    public static final String COMPLETE_CHALLENGE_CMD = "challenges complete ";
    private final uSkyBlock plugin;
    private final ItemStack tool;
    private final Map<String, String> commandMap = new HashMap<>();

    @Inject
    public ToolMenuEvents(@NotNull uSkyBlock plugin) {
        this.plugin = plugin;
        tool = ItemStackUtil.createItemStack(plugin.getConfig().getString("tool-menu.tool", Material.OAK_SAPLING.toString()));
        registerChallenges();
        registerCommands();
    }

    private void registerCommands() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("tool-menu.commands");
        if (section != null) {
            for (String block : section.getKeys(false)) {
                ItemStack item = ItemStackUtil.createItemStack(block);
                if (item.getType().isBlock() && section.isString(block)) {
                    commandMap.put(ItemStackUtil.asString(item), section.getString(block));
                }
            }
        }
    }

    private void registerChallenges() {
        for (String challengeName : plugin.getChallengeLogic().getAllChallengeNames()) {
            Challenge challenge = plugin.getChallengeLogic().getChallenge(challengeName);
            ItemStack displayItem = challenge != null ? challenge.getDisplayItem() : null;
            ItemStack toolItem = challenge != null && challenge.getTool() != null ? ItemStackUtil.createItemStack(challenge.getTool()) : null;
            if (toolItem != null) {
                commandMap.put(ItemStackUtil.asString(toolItem), COMPLETE_CHALLENGE_CMD + challengeName);
            } else if (displayItem != null && displayItem.getType().isBlock()) {
                commandMap.put(ItemStackUtil.asString(displayItem), COMPLETE_CHALLENGE_CMD + challengeName);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockHit(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null || e.getAction() != Action.LEFT_CLICK_BLOCK ||
            e.getPlayer().getGameMode() != GameMode.SURVIVAL) {
            return;
        }
        Player player = e.getPlayer();
        if (!plugin.getWorldManager().isSkyAssociatedWorld(player.getWorld()) || !isTool(e.getItem())) {
            return;
        }

        // We are in a skyworld, a block has been hit, with the tool
        Material block = e.getClickedBlock().getType();
        if (commandMap.containsKey(block.toString())) {
            doCmd(e, player, block.toString());
        }
    }

    private void doCmd(PlayerInteractEvent e, Player player, String itemId) {
        String command = commandMap.get(itemId);
        if (command.startsWith(COMPLETE_CHALLENGE_CMD)) {
            String challengeName = command.substring(COMPLETE_CHALLENGE_CMD.length());
            if (plugin.getChallengeLogic().getAvailableChallengeNames(plugin.getPlayerInfo(player)).contains(challengeName)) {
                e.setCancelled(true);
                plugin.execCommand(player, command, true);
            }
        } else {
            e.setCancelled(true);
            plugin.execCommand(player, command, false);
        }
    }

    /**
     * Checks if the given {@link ItemStack} is the configured tool for the tool menu.
     *
     * @param item ItemStack to check.
     * @return True if it is the configured tool, false otherwise.
     */
    private boolean isTool(ItemStack item) {
        return item != null && tool != null && item.getType() == tool.getType();
    }
}
