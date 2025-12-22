package me.hsgamer.bettereconomy.holder;

import io.github.projectunified.minelib.plugin.base.Loadable;
import io.github.projectunified.minelib.scheduler.async.AsyncScheduler;
import lombok.Getter;
import me.hsgamer.bettereconomy.BetterEconomy;
import me.hsgamer.bettereconomy.config.MainConfig;
import me.hsgamer.bettereconomy.database.MySqlDataStorageSupplier;
import me.hsgamer.topper.agent.core.Agent;
import me.hsgamer.topper.agent.core.AgentHolder;
import me.hsgamer.topper.agent.core.DataEntryAgent;
import me.hsgamer.topper.agent.snapshot.SnapshotAgent;
import me.hsgamer.topper.agent.storage.StorageAgent;
import me.hsgamer.topper.data.core.DataEntry;
import me.hsgamer.topper.data.simple.SimpleDataHolder;
import me.hsgamer.topper.spigot.agent.runnable.SpigotRunnableAgent;
import me.hsgamer.topper.storage.core.DataStorage;
import me.hsgamer.topper.storage.sql.converter.NumberSqlValueConverter;
import me.hsgamer.topper.storage.sql.converter.UUIDSqlValueConverter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class EconomyHolder extends SimpleDataHolder<UUID, Double> implements AgentHolder<UUID, Double>, Loadable {
    private final BetterEconomy instance;
    private final List<Agent> agents = new ArrayList<>();
    private final List<DataEntryAgent<UUID, Double>> entryAgents = new ArrayList<>();

    @Getter
    private StorageAgent<UUID, Double> storageAgent;
    @Getter
    private SnapshotAgent<UUID, Double> snapshotAgent;

    public EconomyHolder(BetterEconomy instance) {
        this.instance = instance;
        entryAgents.add(new DataEntryAgent<>() {
            @Override
            public void onCreate(DataEntry<UUID, Double> entry) {
                entry.setValue(instance.get(MainConfig.class).getStartAmount(), true);
            }
        });
    }

    private DataStorage<UUID, Double> getStorage() {
        UUIDSqlValueConverter sqlKeyConverter = new UUIDSqlValueConverter("uuid");
        NumberSqlValueConverter<Double> sqlValueConverter = new NumberSqlValueConverter<>("balance", true, Number::doubleValue);

        MySqlDataStorageSupplier supplier = new MySqlDataStorageSupplier();
        return supplier.getStorage("economy", sqlKeyConverter, sqlValueConverter);
    }

    @Override
    public List<Agent> getAgents() {
        return agents;
    }

    @Override
    public List<DataEntryAgent<UUID, Double>> getEntryAgents() {
        return entryAgents;
    }

    @Override
    public void load() {
        storageAgent = new StorageAgent<>(getStorage());
        agents.add(storageAgent);
        entryAgents.add(storageAgent);
        agents.add(storageAgent.getLoadAgent(this));
        agents.add(new SpigotRunnableAgent(storageAgent, AsyncScheduler.get(instance), instance.get(MainConfig.class).getSaveFilePeriod()));

        snapshotAgent = SnapshotAgent.create(this);
        snapshotAgent.setComparator(Comparator.reverseOrder());
        snapshotAgent.setFilter(entry -> entry.getValue() != null);
        agents.add(snapshotAgent);
        agents.add(new SpigotRunnableAgent(snapshotAgent, AsyncScheduler.get(instance), instance.get(MainConfig.class).getUpdateBalanceTopPeriod()));
    }

    @Override
    public void enable() {
        register();
    }

    @Override
    public void disable() {
        unregister();
    }

    public double get(UUID uuid) {
        return getOrCreateEntry(uuid).getValue();
    }

    public boolean hasAccount(UUID uuid) {
        return getOrCreateEntry(uuid).getValue() != null;
    }

    public boolean createAccount(UUID uuid) {
        if (hasAccount(uuid)) {
            return false;
        }
        getOrCreateEntry(uuid).setValue(instance.get(MainConfig.class).getStartAmount());
        return true;
    }

    public boolean deleteAccount(UUID uuid) {
        if (!hasAccount(uuid)) {
            return false;
        }
        getOrCreateEntry(uuid).setValue((Double) null);
        return true;
    }

    public boolean has(UUID uuid, double amount) {
        return get(uuid) >= amount;
    }

    public boolean set(UUID uuid, double amount) {
        if (amount < instance.get(MainConfig.class).getMinimumAmount()) {
            return false;
        }
        getOrCreateEntry(uuid).setValue(amount);
        return true;
    }

    public boolean withdraw(UUID uuid, double amount) {
        return set(uuid, get(uuid) - amount);
    }

    public boolean deposit(UUID uuid, double amount) {
        return set(uuid, get(uuid) + amount);
    }
}
