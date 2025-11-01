package net.miarma.mkernel.config;

import java.util.Optional;

public class CommandWrapper {
    private final String name;
    private final String description;
    private final String[] aliases;
    private final String usage;
    private final PermissionWrapper permission;
    private final String[] messages;
    private final CommandWrapper[] subcommands;

    private CommandWrapper(Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.aliases = builder.aliases;
        this.usage = builder.usage;
        this.permission = builder.permission;
        this.messages = builder.messages;
        this.subcommands = builder.subcommands;
    }

    public static Builder command(String name) {
        return new Builder(name);
    }

    public static class Builder {
        private final String name;
        private String description;
        private String[] aliases;
        private String usage;
        private PermissionWrapper permission;
        private String[] messages;
        private CommandWrapper[] subcommands;

        private Builder(String name) {
            this.name = name;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withAliases(String... aliases) {
            this.aliases = aliases;
            return this;
        }

        public Builder withUsage(String usage) {
            this.usage = usage;
            return this;
        }

        public Builder withPermission(PermissionWrapper permission) {
            this.permission = permission;
            return this;
        }

        public Builder withMessages(String... messages) {
            this.messages = messages;
            return this;
        }

        public Builder withSubcommands(CommandWrapper... subcommands) {
            this.subcommands = subcommands;
            return this;
        }

        public CommandWrapper build() {
            return new CommandWrapper(this);
        }
    }

    public Optional<String> usageOptional() {
        return Optional.ofNullable(usage);
    }

    public Optional<String[]> messagesOptional() {
        return Optional.ofNullable(messages);
    }

    public Optional<CommandWrapper[]> subcommandsOptional() {
        return Optional.ofNullable(subcommands);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String[] getAliases() {
        return aliases;
    }

    public String getUsage() {
        return usage;
    }

    public PermissionWrapper getPermission() {
        return permission;
    }

    public String[] getMessages() {
        return messages;
    }

    public CommandWrapper[] getSubcommands() {
        return subcommands;
    }
}
