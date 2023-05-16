package mask.inc.logintime;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class Logintime extends JavaPlugin implements Listener {
    private Map<Player, Long> loginTimes;
    private Map<Player, Long> playtimeRanking;

    @Override
    public void onEnable() {
        loginTimes = new HashMap<>();
        playtimeRanking = new LinkedHashMap<>();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        loginTimes.put(player, System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (loginTimes.containsKey(player)) {
            long loginTime = loginTimes.get(player);
            long playtime = System.currentTimeMillis() - loginTime;
            loginTimes.remove(player);
            playtimeRanking.put(player, playtime);
            updateRanking();
        }
    }

    private void updateRanking() {
        playtimeRanking = playtimeRanking.entrySet()
                .stream()
                .sorted(Map.Entry.<Player, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("login time")) {
            displayLoginTimeRanking(sender);
            return true;
        }
        return false;
    }

    private void displayLoginTimeRanking(CommandSender sender) {
        sender.sendMessage("=== Login Time Ranking ===");
        int rank = 1;
        for (Map.Entry<Player, Long> entry : playtimeRanking.entrySet()) {
            Player player = entry.getKey();
            long playtime = entry.getValue();
            sender.sendMessage(rank + ". " + player.getName() + ": " + formatPlaytime(playtime));
            rank++;
        }
    }

    private String formatPlaytime(long playtime) {
        long totalSeconds = playtime / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
