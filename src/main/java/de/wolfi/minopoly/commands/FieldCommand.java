package de.wolfi.minopoly.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import de.wolfi.minopoly.Main;
import de.wolfi.minopoly.components.Minopoly;
import de.wolfi.minopoly.components.Player;
import de.wolfi.minopoly.components.fields.Field;
import de.wolfi.utils.ItemBuilder;
import de.wolfi.utils.inventory.InventoryConfirmation;
import de.wolfi.utils.inventory.InventorySelector;

public class FieldCommand extends CommandInterface implements InventoryHolder {

    public static final ItemStack fieldGUI = new ItemBuilder(Material.WATER_LILY).setName("�aFelder Managen").build();

    private final String title = "Felder Managen";


    private static final ItemStack sellItem = new ItemBuilder(Material.ARROW).setName("�aVerkaufen").build();
    private static final ItemStack moveItem = new ItemBuilder(Material.PISTON_BASE).setName("�aVerschicken").build();
    private static final ItemStack buyItem = new ItemBuilder(Material.PAPER).setName("�aKaufen").build();

    public FieldCommand(Main plugin) {
        super(plugin, 1, true);
    }

    @Override
    public List<String> onTabComplete(final CommandSender paramCommandSender, final Command paramCommand,
                                      final String paramString, final String[] paramArrayOfString) {
        // TODO
        return null;
    }

    @Override
    protected void executeCommand(Minopoly board, Player player, String[] args) {
        switch (args[0]) {
            case "gui": {
                Inventory inv = this.getInventory();
                for (Field f : board.getFieldManager().getFields()) {
                    if (player.getLocation().equals(f)) {
                        inv.addItem(new ItemBuilder(Material.STAINED_GLASS_PANE).setName("").build(),
                                board.getFieldManager().createFieldInfo(player, f),
                                new ItemBuilder(Material.STAINED_GLASS_PANE).setName(" ").build());
                    }
                }
                player.getHook().openInventory(inv);
            }
            break;
            case "buy":
                if (!player.getLocation().isOwned())
                    player.getLocation().buy(player);
                break;
            case "sell":
                if (player.getLocation().isOwnedBy(player))
                    player.getLocation().sell();
                break;
            case "move":
                Player pr = board.getByPlayerName(args[1]);
                if (player.getLocation().isOwnedBy(player))
                    player.getLocation().moveProperty(pr);
                break;
            default:
                break;
        }
    }

    @EventHandler
    public void onItemUse(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_AIR
                || e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getItem() != null) {
            if (e.getItem().equals(FieldCommand.fieldGUI)) {
                e.setCancelled(true);
                Bukkit.dispatchCommand(Main.getMain().getMinopoly(e.getPlayer().getWorld()),
                        "field " + e.getPlayer().getName() + " gui");
            }
        }
    }


    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() == this) {
            e.setCancelled(true);
            if (e.getCurrentItem() != null) {
                Minopoly game = Main.getMain().getMinopoly(e.getWhoClicked().getWorld());
                Player p = game.getByBukkitPlayer((org.bukkit.entity.Player) e.getWhoClicked());
                if (FieldCommand.buyItem.isSimilar(e.getCurrentItem())) {
                    InventoryConfirmation confirm = new InventoryConfirmation(
                            "M�chtest du wirkich die Stra�e " + e.getInventory().getTitle() + " kaufen?");
                    confirm.setCallback((i) -> {
                        if (!confirm.isCancelled())
                            Bukkit.dispatchCommand(e.getWhoClicked(), "field " + e.getWhoClicked().getName() + " buy");
                        return true;
                    });
                    confirm.open((org.bukkit.entity.Player) e.getWhoClicked());
                } else if (FieldCommand.sellItem.isSimilar(e.getCurrentItem())) {
                    InventoryConfirmation confirm = new InventoryConfirmation(
                            "M�chtest du wirkich die Stra�e " + e.getInventory().getTitle() + " verkaufen?");
                    confirm.setCallback((i) -> {
                        if (!confirm.isCancelled())
                            Bukkit.dispatchCommand(e.getWhoClicked(), "field " + e.getWhoClicked().getName() + " sell");
                        return true;
                    });
                    confirm.open((org.bukkit.entity.Player) e.getWhoClicked());

                } else if (FieldCommand.moveItem.isSimilar(e.getCurrentItem())) {
                    InventorySelector sel = this.createPlayerSelector(game, "Wer soll die Stra�e bekommen?");
                    sel.setCallback((i) -> {
                        String name = ((SkullMeta) i.getItemMeta()).getOwner();
                        if (e.getWhoClicked().getName().equals(name))
                            return false;
                        Field f = game.getFieldManager().getFieldByString(null,
                                e.getInventory().getItem(0).getItemMeta().getDisplayName());
                        Player selected = game.getByPlayerName(name);
                        InventoryConfirmation confirm = new InventoryConfirmation(
                                p.getDisplay() + " m�chte dir seine Stra�e " + f.toString() + " �berlassen o/");
                        confirm.setCallback((x) -> {
                            if (!confirm.isCancelled())
                                Bukkit.dispatchCommand(e.getWhoClicked(),
                                        "field " + e.getWhoClicked().getName() + " move " + selected.getName());
                            return true;
                        });
                        confirm.open(selected.getHook());
                        return true;
                    });
                    sel.open((org.bukkit.entity.Player) e.getWhoClicked());
                } else {
                    Field f = game.getFieldManager().getFieldByString(null,
                            e.getCurrentItem().getItemMeta().getDisplayName());
                    e.getWhoClicked().openInventory(this.createFieldInv(p, f));
                }
            }
        }
    }

    private InventorySelector createPlayerSelector(Minopoly game, String title) {
        InventorySelector selector = new InventorySelector(title);
        for (Player p : game.getPlayingPlayers())
            selector.addEntry(ItemBuilder.skullFromPlayer(p.getName()).addLore(p.getFigure().getDisplay()).build());
        return selector;
    }

    private Inventory createFieldInv(Player player, Field f) {
        Inventory inv = Bukkit.createInventory(this, InventoryType.HOPPER, f.toString());
        inv.addItem(f.getGame().getFieldManager().createFieldInfo(player, f));
        if (!f.isOwned())
            if (player.getLocation().equals(f))
                inv.addItem(FieldCommand.buyItem);
            else
                inv.addItem(new ItemBuilder(FieldCommand.buyItem).enchant(Enchantment.THORNS, 1)
                        .addLore("�aDu musst auf der Stra�e stehen, um sie zu erwerben!").build());
        else if (f.isOwnedBy(player))
            inv.addItem(FieldCommand.sellItem, FieldCommand.moveItem);
        return inv;
    }

    @Override
    public Inventory getInventory() {
        return Bukkit.createInventory(this, 9 * 6, title);
    }

}
