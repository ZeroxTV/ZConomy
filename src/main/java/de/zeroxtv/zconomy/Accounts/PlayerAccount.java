package de.zeroxtv.zconomy.Accounts;

import de.zeroxtv.zcore.OtherUtil.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by ZeroxTV
 */
public class PlayerAccount {
    public static HashMap<UUID, PlayerAccount> loadedAccounts = new HashMap<UUID, PlayerAccount>();

    private UUID owner;
    private YamlConfiguration config;
    private File configFile;


    /**
     * Get the player account of a UUID
     * @param uuid
     */
    private PlayerAccount(UUID uuid) {
        this.owner = uuid;
        load();
    }


    public static PlayerAccount getPlayerAccount(Player player) {
        return getPlayerAccount(player.getUniqueId());
    }

    public static PlayerAccount getPlayerAccount(String name) {
        return getPlayerAccount(Bukkit.getPlayer(name).getUniqueId());
    }

    public static PlayerAccount getPlayerAccount(UUID uuid) {
        if (loadedAccounts.containsKey(uuid)) {
            return loadedAccounts.get(uuid);
        }
        PlayerAccount account = new PlayerAccount(uuid);
        loadedAccounts.put(uuid, account);
        return account;
    }

    public static void saveAll() {
        for(Map.Entry<UUID, PlayerAccount> entry : loadedAccounts.entrySet()) {
            entry.getValue().save();
        }
    }

    /**
     * Load a player account from file or create one
     * @return boolean if the account had to be created
     */
    public boolean load() {
        try {
            boolean toReturn;

            File pathFile = new File("ZConomy/accounts/");
            if (!pathFile.exists()) pathFile.mkdirs();
            File configFile = new File("ZConomy/accounts/" + owner + ".yml");

            if (!configFile.exists()) {
                configFile.createNewFile();

                YamlConfiguration config = new YamlConfiguration();
                config.load(configFile);
                this.config = config;
                this.configFile = configFile;

                create();
                toReturn = true;
            } else {
                YamlConfiguration config = new YamlConfiguration();
                config.load(configFile);
                this.config = config;
                this.configFile = configFile;
                toReturn = false;
                setBalance(NumberUtils.parseDouble(getBalance(), 2));
            }
            return toReturn;

        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Save the account configuration file
     */
    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a player account with a starting balance of 0
     */
    public void create() {
        config.addDefault("balance", 0);
        config.options().copyDefaults(true);
        save();
    }

    /**
     * @return Amount of money this account has stored
     */
    public Double getBalance() {
        return config.getDouble("balance");
    }

    /**
     * Set the amount of money this account has stored
     * @param amount
     */
    public void setBalance(Number amount) {
        config.set("balance", amount);
        save();
    }

    /**
     * Add money to the current balance
     * @param amount
     */
    public void deposit(Number amount) {
        setBalance(amount.doubleValue() + getBalance());
        save();
    }

    /**
     * Withdraw money from the current balance
     * @param amount
     */
    public boolean withdraw(Number amount) {
        if (getBalance() - amount.doubleValue() >= 0) {
            setBalance(getBalance() - amount.doubleValue());
            save();
            return true;
        }
        return false;
    }

    public boolean transact(Player playerTo, Number amount) {
        if (withdraw(amount)) {
            getPlayerAccount(playerTo).deposit(amount);
            return true;
        }
        return false;
    }
}
