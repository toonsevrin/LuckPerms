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

package me.lucko.luckperms.common.commands.impl.user;

import me.lucko.luckperms.api.Node;
import me.lucko.luckperms.common.commands.Arg;
import me.lucko.luckperms.common.commands.CommandException;
import me.lucko.luckperms.common.commands.CommandResult;
import me.lucko.luckperms.common.commands.abstraction.SubCommand;
import me.lucko.luckperms.common.commands.sender.Sender;
import me.lucko.luckperms.common.constants.Message;
import me.lucko.luckperms.common.constants.Permission;
import me.lucko.luckperms.common.core.NodeFactory;
import me.lucko.luckperms.common.core.model.User;
import me.lucko.luckperms.common.plugin.LuckPermsPlugin;
import me.lucko.luckperms.common.utils.Predicates;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class UserBulkChange extends SubCommand<User> {
    public UserBulkChange() {
        super("bulkchange", "Applies a bulk permission change to the user's permissions", Permission.USER_BULKCHANGE, Predicates.not(3),
                Arg.list(
                        Arg.create("server|world", true, "if the bulk change is modifying a 'server' or a 'world'"),
                        Arg.create("from", true, "the server/world to be changed from. can be 'global' or 'null' respectively"),
                        Arg.create("to", true, "the server/world to replace 'from' (can be 'null')")
                )
        );
    }

    @Override
    public CommandResult execute(LuckPermsPlugin plugin, Sender sender, User user, List<String> args, String label) throws CommandException {
        String type = args.get(0).toLowerCase();
        String from = args.get(1);
        String to = args.get(2);
        if (to.equals("null")) {
            to = null;
        }

        Set<Node> toAdd = new HashSet<>();
        Set<Node> toRemove = new HashSet<>();

        if (!type.equals("world") && !type.equals("server")) {
            Message.BULK_CHANGE_TYPE_ERROR.send(sender);
            return CommandResult.FAILURE;
        }

        Iterator<Node> iterator = user.getNodes().values().iterator();
        if (type.equals("world")) {
            while (iterator.hasNext()) {
                Node element = iterator.next();

                if (element.isGroupNode()) {
                    continue;
                }

                String world = element.getWorld().orElse("null");
                if (!world.equals(from)) {
                    continue;
                }

                toRemove.add(element);
                toAdd.add(NodeFactory.builderFromExisting(element).setWorld(to).build());
            }
        } else {
            while (iterator.hasNext()) {
                Node element = iterator.next();

                if (element.isGroupNode()) {
                    continue;
                }

                String server = element.getServer().orElse("global");
                if (!server.equals(from)) {
                    continue;
                }

                toRemove.add(element);
                toAdd.add(NodeFactory.builderFromExisting(element).setServer(to).build());
            }
        }

        toRemove.forEach(user::unsetPermission);
        toAdd.forEach(user::setPermission);

        save(user, sender, plugin);
        Message.BULK_CHANGE_SUCCESS.send(sender, toAdd.size());
        return CommandResult.SUCCESS;
    }
}
