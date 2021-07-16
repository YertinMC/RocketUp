package top.yertinmc.rocketup;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main extends JavaPlugin {

    public final List<UUID> glidingToggleCanceler = new ArrayList<>();
    public final Map<UUID, Integer> asyncCounter = new HashMap<>();
    public Material[] materialFilter = new Material[]{Material.FIREWORK_ROCKET};
    public int startDelay;
    public int asyncLimit = 0;
    public String asyncWarn = null;
    public String lorePrefix = null;
    public int teleportSpeed;
    public boolean enableDownFly;
    public boolean suffocationProtection;
    public Map<String, RocketConfig> presets = Collections.emptyMap();

    @Override
    public void onEnable() {
        super.onEnable();
        saveDefaultConfig();
        if (getConfig().contains("materials")) {
            List<String> materials = getConfig().getStringList("materials");
            materialFilter = new Material[materials.size()];
            for (int i = 0; i < materials.size(); i++) {
                materialFilter[i] = Material.matchMaterial(materials.get(i), false);
            }
        }
        startDelay = getConfig().getInt("startDelay", 0);
        assert startDelay >= 0;
        if (getConfig().contains("asyncLimit"))
            asyncLimit = getConfig().getInt("asyncLimit");
        if (getConfig().contains("asyncWarn"))
            asyncWarn = getConfig().getString("asyncWarn");
        if (getConfig().contains("prefix"))
            lorePrefix = Objects.requireNonNull(getConfig().getString("prefix")).concat("#");
        teleportSpeed = getConfig().getInt("teleportSpeed", 1);
        assert teleportSpeed >= 1;
        enableDownFly = getConfig().getBoolean("enableDownFly", false);
        suffocationProtection = getConfig().getBoolean("suffocationProtection", true);
        if (getConfig().contains("presets")) {
            presets = new HashMap<>();
            ConfigurationSection config = Objects.requireNonNull(getConfig().getConfigurationSection("presets"));
            for (String key : config.getKeys(false)) {
                ConfigurationSection preset = Objects.requireNonNull(config.getConfigurationSection(key));
                Particle particle = null;
                if (preset.contains("particle"))
                    try {
                        particle = Particle.valueOf(preset.getString("particle"));
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                presets.put(key, new RocketConfig(preset.getInt("time"),
                        new Vector(preset.getDouble("directionX"), preset.getDouble("directionY"),
                                preset.getDouble("directionZ")), preset.getDouble("distanceScale"),
                        particle, preset.getInt("particleCount", 3),
                        preset.getDouble("particleOffsetX", 0),
                        preset.getDouble("particleOffsetY", 0.8),
                        preset.getDouble("particleOffsetZ", 0)));
            }
        }
        getServer().getPluginManager().registerEvents(new EventListener(), this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        glidingToggleCanceler.clear();
        asyncCounter.clear();
        Bukkit.getScheduler().cancelTasks(this);
    }

    public class EventListener implements Listener {

        @EventHandler
        public void onPlayerInteract(PlayerInteractEvent event) {
            Player player = event.getPlayer();
            if (!event.hasBlock() && event.hasItem() && player.hasPermission("rocketup.use")) {
                // Use only item
                Material itemMaterial = Objects.requireNonNull(event.getItem()).getType();
                for (Material material : materialFilter) {
                    if (itemMaterial == material) {
                        List<String> lore = Objects.requireNonNull(Objects.requireNonNull(
                                event.getItem().getItemMeta()).getLore());
                        if (lore.size() >= 1 && lore.get(0).contains("#")) {
                            if (lorePrefix != null && !lore.get(0).startsWith(lorePrefix))
                                return;
                            getLogger().info("Player ".concat(player.getUniqueId().toString())
                                    .concat(" used one ").concat(itemMaterial.name()));
                            if (asyncLimit != 0
                                    && asyncCounter.containsKey(player.getUniqueId())
                                    && asyncCounter.get(player.getUniqueId()) >= asyncLimit) {
                                getLogger().info("Player ".concat(player.getUniqueId().toString())
                                        .concat(" trying to use too many super rocket in one time."));
                                if (asyncWarn != null)
                                    player.sendMessage(asyncWarn);
                                return;
                            }
                            String configName = lore.get(0).substring(lore.get(0).indexOf('#') + 1);
                            RocketConfig config = presets.get(configName);
                            if (config == null) {
                                getLogger().info("Rocket config ".concat(configName).concat(" not found."));
                                return;
                            }
                            AtomicInteger taskId = new AtomicInteger(0);
                            AtomicInteger duringTime = new AtomicInteger();
                            final double offsetXpt = Math.sin((config.direction.getX()
                                    + player.getLocation().getYaw()) % 360) * config.distanceScale * teleportSpeed;
                            final double offsetYpt = Math.sin((config.direction.getY()
                                    + player.getLocation().getPitch()) % 360) * config.distanceScale * teleportSpeed;
                            final double offsetZpt = Math.sin((config.direction.getZ()
                                    - player.getLocation().getYaw()) % 360) * config.distanceScale * teleportSpeed;
                            if (offsetYpt <= 0 && !enableDownFly) {
                                getLogger().info("Player ".concat(player.getUniqueId().toString())
                                        .concat(" trying to fly downer."));
                                return;
                            }
                            event.getItem().setAmount(event.getItem().getAmount() - 1);
                            player.setGravity(false);
                            if (asyncCounter.containsKey(player.getUniqueId()))
                                asyncCounter.put(player.getUniqueId(), asyncCounter.get(player.getUniqueId()) + 1);
                            else
                                asyncCounter.put(player.getUniqueId(), 1);
                            taskId.set(Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.this, () -> {
                                player.teleport(player.getLocation().add(offsetXpt, offsetYpt, offsetZpt));
                                if (config.particle != null)
                                    player.getWorld().spawnParticle(config.particle,
                                            player.getLocation().add(config.particleOffsetX,
                                                    config.particleOffsetY, config.particleOffsetZ), config.particleCount);
                                duringTime.getAndAdd(teleportSpeed);
                                if (duringTime.get() > config.time) {
                                    // End
                                    player.setGravity(true);
                                    // Suffocation Protection
                                    if (suffocationProtection
                                            && !player.getWorld().getBlockAt(player.getLocation().add(0, 1, 0))
                                            .getType().isAir()) {
                                        getLogger().info("Suffocation protecting for player "
                                                .concat(player.getUniqueId().toString()));
                                        player.teleport(player.getWorld().getHighestBlockAt(player.getLocation())
                                                .getLocation().add(0, 1, 0));
                                    } else {
                                        glidingToggleCanceler.add(player.getUniqueId());
                                        player.setGliding(true);
                                    }
                                    if (asyncLimit != 0) {
                                        asyncCounter.put(player.getUniqueId(), asyncCounter.get(player.getUniqueId()) - 1);
                                        if (asyncCounter.get(player.getUniqueId()) == 0)
                                            asyncCounter.remove(player.getUniqueId());
                                    }
                                    Bukkit.getScheduler().cancelTask(taskId.get());
                                }
                            }, startDelay, teleportSpeed));
                        }
                        return;
                    }
                }
            }
        }

        /**
         * Force gliding
         */
        @EventHandler(priority = EventPriority.LOW)
        public void onEntityToggleGlide(EntityToggleGlideEvent event) {
            if (!event.isGliding() && event.getEntity() instanceof Player
                    && glidingToggleCanceler.contains(event.getEntity().getUniqueId())) {
                if (event.getEntity().getWorld().getBlockAt(event.getEntity().getLocation().add(0, -1, 0))
                        .getType().isAir())
                    event.setCancelled(true);
                else
                    glidingToggleCanceler.remove(event.getEntity().getUniqueId());
            }
        }

    }

    public static class RocketConfig {

        public int time;
        public Vector direction;
        public double distanceScale;
        public Particle particle;
        public int particleCount;
        public double particleOffsetX;
        public double particleOffsetY;
        public double particleOffsetZ;

        public RocketConfig(int time, Vector direction, double distanceScale, Particle particle,
                            int particleCount, double particleOffsetX, double particleOffsetY,
                            double particleOffsetZ) {
            this.time = time;
            this.direction = direction;
            this.distanceScale = distanceScale;
            this.particle = particle;
            this.particleCount = particleCount;
            this.particleOffsetX = particleOffsetX;
            this.particleOffsetY = particleOffsetY;
            this.particleOffsetZ = particleOffsetZ;
        }

    }

}
