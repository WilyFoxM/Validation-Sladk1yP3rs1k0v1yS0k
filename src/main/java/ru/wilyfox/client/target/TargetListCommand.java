package ru.wilyfox.client.target;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

public final class TargetListCommand {
    private static final String COMMAND = "/fhtarget";

    private TargetListCommand() {
    }

    public static boolean handleOutgoingCommand(String rawInput, boolean addToHistory) {
        if (rawInput == null) {
            return false;
        }

        String normalized = rawInput.trim();
        if (!normalized.toLowerCase(Locale.ROOT).startsWith(COMMAND)) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (addToHistory && minecraft.gui != null) {
            minecraft.gui.getChat().addRecentChat(normalized);
        }

        String args = normalized.length() > COMMAND.length()
                ? normalized.substring(COMMAND.length()).trim()
                : "";

        if (args.isBlank()) {
            showLocalMessage("Usage: /fhtarget <add|remove|list> [nick]");
            return true;
        }

        String[] parts = args.split("\\s+", 2);
        String action = parts[0].toLowerCase(Locale.ROOT);

        switch (action) {
            case "add" -> {
                String name = parts.length > 1 ? parts[1].trim() : "";
                if (name.isBlank()) {
                    showLocalMessage("Usage: /fhtarget add <nick>");
                    return true;
                }

                if (TargetListStore.add(name)) {
                    showLocalMessage("Target added: " + name);
                } else if (TargetListStore.isTarget(name)) {
                    showLocalMessage("Target already exists: " + name);
                } else {
                    showLocalMessage("Invalid target name.");
                }
                return true;
            }
            case "remove" -> {
                String name = parts.length > 1 ? parts[1].trim() : "";
                if (name.isBlank()) {
                    showLocalMessage("Usage: /fhtarget remove <nick>");
                    return true;
                }

                if (TargetListStore.remove(name)) {
                    showLocalMessage("Target removed: " + name);
                } else {
                    showLocalMessage("Target not found: " + name);
                }
                return true;
            }
            case "list" -> {
                List<String> targets = TargetListStore.getTargets();
                if (targets.isEmpty()) {
                    showLocalMessage("Target list is empty.");
                } else {
                    showLocalMessage("Targets: " + String.join(", ", targets));
                }
                return true;
            }
            default -> {
                showLocalMessage("Usage: /fhtarget <add|remove|list> [nick]");
                return true;
            }
        }
    }

    private static void showLocalMessage(String message) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gui != null) {
            minecraft.gui.getChat().addMessage(Component.literal(message));
        }
    }
}
