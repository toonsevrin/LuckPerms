/*
 * Copyright (c) 2016 Lucko (Luck) <luck@lucko.me>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.lucko.luckperms.bungee.migration;

import me.lucko.luckperms.api.event.cause.CreationCause;
import me.lucko.luckperms.common.commands.CommandException;
import me.lucko.luckperms.common.commands.CommandResult;
import me.lucko.luckperms.common.commands.abstraction.SubCommand;
import me.lucko.luckperms.common.commands.impl.migration.MigrationUtils;
import me.lucko.luckperms.common.commands.sender.Sender;
import me.lucko.luckperms.common.constants.Permission;
import me.lucko.luckperms.common.core.NodeFactory;
import me.lucko.luckperms.common.plugin.LuckPermsPlugin;
import me.lucko.luckperms.common.utils.Predicates;
import me.lucko.luckperms.common.utils.ProgressLogger;

import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.Server;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.World;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MigrationBungeePerms extends SubCommand<Object> {
    public MigrationBungeePerms() {
        super("bungeeperms", "Migration from BungeePerms", Permission.MIGRATION, Predicates.alwaysFalse(), null);
    }

    @Override
    public CommandResult execute(LuckPermsPlugin plugin, Sender sender, Object o, List<String> args, String label) throws CommandException {
        ProgressLogger log = new ProgressLogger("BungeePerms");
        log.addListener(plugin.getConsoleSender());
        log.addListener(sender);

        log.log("Starting.");

        // Get BungeePerms instance
        BungeePerms bp = BungeePerms.getInstance();
        if (bp == null) {
            log.logErr("Plugin not loaded.");
            return CommandResult.STATE_ERROR;
        }

        List<Group> groups = bp.getPermissionsManager().getBackEnd().loadGroups();

        log.log("Calculating group weightings.");
        int maxWeight = 0;
        for (Group group : groups) {
            maxWeight = Math.max(maxWeight, group.getRank());
        }
        maxWeight += 5;

        // Migrate all groups.
        log.log("Starting group migration.");
        AtomicInteger groupCount = new AtomicInteger(0);
        for (Group g : groups) {
            int groupWeight = maxWeight - g.getRank();

            // Make a LuckPerms group for the one being migrated
            String groupName = MigrationUtils.standardizeName(g.getName());
            plugin.getStorage().createAndLoadGroup(groupName, CreationCause.INTERNAL).join();
            me.lucko.luckperms.common.core.model.Group group = plugin.getGroupManager().getIfLoaded(groupName);

            MigrationUtils.setGroupWeight(group, groupWeight);

            // Migrate global perms
            for (String perm : g.getPerms()) {
                if (perm.isEmpty()) {
                    continue;
                }

                group.setPermission(MigrationUtils.parseNode(perm, true).build());
            }

            // Migrate per-server perms
            for (Map.Entry<String, Server> e : g.getServers().entrySet()) {
                for (String perm : e.getValue().getPerms()) {
                    if (perm.isEmpty()) {
                        continue;
                    }

                    group.setPermission(MigrationUtils.parseNode(perm, true).setWorld(e.getKey()).build());
                }

                // Migrate per-world perms
                for (Map.Entry<String, World> we : e.getValue().getWorlds().entrySet()) {
                    for (String perm : we.getValue().getPerms()) {
                        if (perm.isEmpty()) {
                            continue;
                        }

                        group.setPermission(MigrationUtils.parseNode(perm, true).setServer(e.getKey()).setWorld(we.getKey()).build());
                    }
                }
            }

            // Migrate any parent groups
            for (String inherit : g.getInheritances()) {
                if (inherit.isEmpty()) {
                    continue;
                }

                group.setPermission(NodeFactory.make("group." + MigrationUtils.standardizeName(inherit)));
            }

            // Migrate prefix and suffix
            String prefix = g.getPrefix();
            String suffix = g.getSuffix();

            if (prefix != null && !prefix.equals("")) {
                group.setPermission(NodeFactory.makePrefixNode(groupWeight, prefix).build());
            }
            if (suffix != null && !suffix.equals("")) {
                group.setPermission(NodeFactory.makeSuffixNode(groupWeight, suffix).build());
            }

            plugin.getStorage().saveGroup(group);
            log.logAllProgress("Migrated {} groups so far.", groupCount.incrementAndGet());
        }
        log.log("Migrated " + groupCount.get() + " groups");

        // Migrate all users.
        log.log("Starting user migration.");
        AtomicInteger userCount = new AtomicInteger(0);

        // Increment the max weight from the group migrations. All user meta should override.
        maxWeight += 5;

        for (User u : bp.getPermissionsManager().getBackEnd().loadUsers()) {
            if (u.getUUID() == null) {
                log.logErr("Could not parse UUID for user: " + u.getName());
                continue;
            }

            // Make a LuckPerms user for the one being migrated.
            plugin.getStorage().loadUser(u.getUUID(), u.getName()).join();
            me.lucko.luckperms.common.core.model.User user = plugin.getUserManager().get(u.getUUID());

            // Migrate global perms
            for (String perm : u.getPerms()) {
                if (perm.isEmpty()) {
                    continue;
                }

                user.setPermission(MigrationUtils.parseNode(perm, true).build());
            }

            // Migrate per-server perms
            for (Map.Entry<String, Server> e : u.getServers().entrySet()) {
                for (String perm : e.getValue().getPerms()) {
                    if (perm.isEmpty()) {
                        continue;
                    }

                    user.setPermission(MigrationUtils.parseNode(perm, true).setWorld(e.getKey()).build());
                }

                // Migrate per-world perms
                for (Map.Entry<String, World> we : e.getValue().getWorlds().entrySet()) {
                    for (String perm : we.getValue().getPerms()) {
                        if (perm.isEmpty()) {
                            continue;
                        }

                        user.setPermission(MigrationUtils.parseNode(perm, true).setServer(e.getKey()).setWorld(we.getKey()).build());
                    }
                }
            }

            // Migrate groups
            for (String group : u.getGroupsString()) {
                if (group.isEmpty()) {
                    continue;
                }

                user.setPermission(NodeFactory.make("group." + MigrationUtils.standardizeName(group)));
            }

            // Migrate prefix & suffix
            String prefix = u.getPrefix();
            String suffix = u.getSuffix();

            if (prefix != null && !prefix.equals("")) {
                user.setPermission(NodeFactory.makePrefixNode(maxWeight, prefix).build());
            }
            if (suffix != null && !suffix.equals("")) {
                user.setPermission(NodeFactory.makeSuffixNode(maxWeight, suffix).build());
            }

            plugin.getStorage().saveUser(user);
            plugin.getUserManager().cleanup(user);

            log.logProgress("Migrated {} users so far.", userCount.incrementAndGet());
        }

        log.log("Migrated " + userCount.get() + " users.");
        log.log("Success! Migration complete.");
        return CommandResult.SUCCESS;
    }
}
