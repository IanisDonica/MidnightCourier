package de.tum.cit.fop.maze.system;

import de.tum.cit.fop.maze.system.progression.DrinkSpeedIUpgrade;
import de.tum.cit.fop.maze.system.progression.DrinkSpeedIIUpgrade;
import de.tum.cit.fop.maze.system.progression.HealthBoostIIUpgrade;
import de.tum.cit.fop.maze.system.progression.HealthBoostIIIUpgrade;
import de.tum.cit.fop.maze.system.progression.HealthUpgrade;
import de.tum.cit.fop.maze.system.progression.MasterUpgrade;
import de.tum.cit.fop.maze.system.progression.NewGlassesUpgrade;
import de.tum.cit.fop.maze.system.progression.PotholImunityUpgrade;
import de.tum.cit.fop.maze.system.progression.RegenUpgrade;
import de.tum.cit.fop.maze.system.progression.RootUpgrade;
import de.tum.cit.fop.maze.system.progression.SpeedIIUpgrade;
import de.tum.cit.fop.maze.system.progression.SpeedIIIUpgrade;
import de.tum.cit.fop.maze.system.progression.SpeedUpgrade;
import de.tum.cit.fop.maze.system.progression.StealthUpgrade;
import de.tum.cit.fop.maze.system.progression.Upgrade;
import de.tum.cit.fop.maze.system.AchievementManager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages progression points and upgrades.
 */
public class ProgressionManager {
    /** Registered upgrades keyed by name. */
    private final Map<String, Upgrade> upgradesByName = new HashMap<>();
    /** Names of owned upgrades. */
    private final Set<String> ownedUpgrades = new HashSet<>();
    /** Current progression points. */
    private int points;

    /**
     * Creates a progression manager with default upgrades and zero points.
     */
    public ProgressionManager() {
        registerDefaults();
    }

    /**
     * Creates a progression manager with default upgrades and starting points.
     *
     * @param startingPoints initial points
     */
    public ProgressionManager(int startingPoints) {
        this.points = startingPoints;
        registerDefaults();
    }

    /**
     * Creates a progression manager with a custom upgrade set.
     *
     * @param upgrades upgrades to register
     * @param startingPoints initial points
     */
    public ProgressionManager(Collection<? extends Upgrade> upgrades, int startingPoints) {
        this.points = startingPoints;
        for (Upgrade upgrade : upgrades) {
            upgradesByName.put(upgrade.getName(), upgrade);
        }
    }

    /**
     * Attempts to purchase an upgrade by name.
     *
     * @param upgradeName upgrade identifier
     * @return {@code true} if the upgrade was purchased
     */
    public boolean buyUpgrade(String upgradeName) {
        Upgrade upgrade = upgradesByName.get(upgradeName);
        if (upgrade == null) {
            return false;
        }
        if (ownedUpgrades.contains(upgradeName)) {
            return false;
        }
        if (!hasAllPrerequisites(upgrade)) {
            return false;
        }
        if (points < upgrade.getCost()) {
            return false;
        }
        points -= upgrade.getCost();
        ownedUpgrades.add(upgradeName);
        AchievementManager.incrementProgress("first_upgrade", 1);
        if ("master".equals(upgradeName)) {
            AchievementManager.incrementProgress("mastery_upgrade", 1);
        }
        return true;
    }

    /**
     * Checks if the upgrade is already owned.
     *
     * @param upgradeName upgrade identifier
     * @return {@code true} if owned
     */
    public boolean hasUpgrade(String upgradeName) {
        return ownedUpgrades.contains(upgradeName);
    }

    /**
     * Determines whether an upgrade can currently be purchased.
     *
     * @param upgradeName upgrade identifier
     * @return {@code true} if purchasable
     */
    public boolean canPurchase(String upgradeName) {
        Upgrade upgrade = upgradesByName.get(upgradeName);
        if (upgrade == null) {
            return false;
        }
        if (ownedUpgrades.contains(upgradeName)) {
            return false;
        }
        return hasAllPrerequisites(upgrade) && points >= upgrade.getCost();
    }

    /**
     * Returns current progression points.
     *
     * @return current points
     */
    public int getPoints() {
        return points;
    }

    /**
     * Adds points to the current total.
     *
     * @param amount non-negative amount to add
     */
    public void addPoints(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be non-negative");
        }
        points += amount;
    }

    /**
     * Sets the current points total.
     *
     * @param points new points total
     */
    public void setPoints(int points) {
        this.points = points;
    }

    /**
     * Returns all registered upgrades.
     *
     * @return unmodifiable map of upgrades
     */
    public Map<String, Upgrade> getUpgrades() {
        return Collections.unmodifiableMap(upgradesByName);
    }

    /**
     * Returns the set of owned upgrades.
     *
     * @return unmodifiable set of owned upgrade names
     */
    public Set<String> getOwnedUpgrades() {
        return Collections.unmodifiableSet(ownedUpgrades);
    }

    /**
     * Replaces the set of owned upgrades.
     *
     * @param upgrades collection of upgrade names
     */
    public void setOwnedUpgrades(Collection<String> upgrades) {
        ownedUpgrades.clear();
        if (upgrades != null) {
            ownedUpgrades.addAll(upgrades);
        }
    }

    /**
     * Looks up a registered upgrade by name.
     *
     * @param upgradeName upgrade identifier
     * @return upgrade instance, or {@code null} if not found
     */
    public Upgrade getUpgrade(String upgradeName) {
        return upgradesByName.get(upgradeName);
    }

    /**
     * Checks whether all prerequisites for an upgrade are owned.
     *
     * @param upgrade upgrade to check
     * @return {@code true} if all prerequisites are owned
     */
    private boolean hasAllPrerequisites(Upgrade upgrade) {
        for (String prereq : upgrade.getPrerequisites()) {
            if (!ownedUpgrades.contains(prereq)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Registers the default upgrades into the manager.
     */
    private void registerDefaults() {
        upgradesByName.put("root", new RootUpgrade());
        upgradesByName.put("speed", new SpeedUpgrade());
        upgradesByName.put("health", new HealthUpgrade());
        upgradesByName.put("stealth", new StealthUpgrade());
        upgradesByName.put("speed_2", new SpeedIIUpgrade());
        upgradesByName.put("health_2", new HealthBoostIIUpgrade());
        upgradesByName.put("speed_3", new SpeedIIIUpgrade());
        upgradesByName.put("health_3", new HealthBoostIIIUpgrade());
        upgradesByName.put("new_glasses", new NewGlassesUpgrade());
        upgradesByName.put("regen", new RegenUpgrade());
        upgradesByName.put("master", new MasterUpgrade());
        upgradesByName.put("drink_speed_1", new DrinkSpeedIUpgrade());
        upgradesByName.put("drink_speed_2", new DrinkSpeedIIUpgrade());
        upgradesByName.put("pothol_imunity", new PotholImunityUpgrade());
    }
}
