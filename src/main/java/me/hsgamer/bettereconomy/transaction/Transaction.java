package me.hsgamer.bettereconomy.transaction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.UUID;

public record Transaction(
        @NotNull UUID sender,
        @Nullable UUID receiver,
        double amount,
        @NotNull Type type,
        Timestamp timestamp
) {

    public enum Type {
        DEPOSIT,
        WITHDRAWAL,
        TRANSFER
    }

}
