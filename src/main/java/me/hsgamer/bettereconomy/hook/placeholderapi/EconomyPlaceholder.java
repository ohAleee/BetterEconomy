package me.hsgamer.bettereconomy.hook.placeholderapi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.hsgamer.bettereconomy.BetterEconomy;
import me.hsgamer.bettereconomy.config.MainConfig;
import me.hsgamer.bettereconomy.holder.EconomyHolder;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;

public class EconomyPlaceholder extends PlaceholderExpansion {
    private final BetterEconomy instance;

    public EconomyPlaceholder(BetterEconomy instance) {
        this.instance = instance;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "bettereconomy";
    }

    @Override
    public @NotNull String getAuthor() {
        return Arrays.toString(instance.getDescription().getAuthors().toArray());
    }

    @Override
    public @NotNull String getVersion() {
        return instance.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        EconomyHolder holder = instance.get(EconomyHolder.class);

        String lower = params.toLowerCase(Locale.ROOT);

        if (player == null) return null;

        return switch (lower) {
            case "balance" -> String.valueOf(holder.get(player.getUniqueId()));
            case "balance_formatted" -> instance.get(MainConfig.class).format(holder.get(player.getUniqueId()));
            default -> null;
        };

    }
}
