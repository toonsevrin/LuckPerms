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

package me.lucko.luckperms.common.commands.impl.migration;

import lombok.experimental.UtilityClass;

import me.lucko.luckperms.api.Node;
import me.lucko.luckperms.common.core.NodeFactory;
import me.lucko.luckperms.common.core.model.Group;

@UtilityClass
public class MigrationUtils {

    public static Node.Builder parseNode(String permission, boolean value) {
        if (permission.startsWith("-") || permission.startsWith("!")) {
            permission = permission.substring(1);
            value = false;
        } else if (permission.startsWith("+")) {
            permission = permission.substring(1);
            value = true;
        }

        return NodeFactory.newBuilder(permission).setValue(value);
    }

    public static void setGroupWeight(Group group, int weight) {
        group.removeIf(n -> n.getPermission().startsWith("weight."));
        group.setPermission(NodeFactory.make("weight." + weight));
    }

    public static String standardizeName(String string) {
        return string.trim().replace(':', '-').replace(' ', '-').replace('.', '-').toLowerCase();
    }

}
