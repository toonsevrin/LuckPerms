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

package me.lucko.luckperms.common.api.delegates;

import lombok.AllArgsConstructor;
import lombok.NonNull;

import me.lucko.luckperms.api.Log;
import me.lucko.luckperms.api.LogEntry;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.UUID;

import static me.lucko.luckperms.common.api.ApiUtils.checkName;

/**
 * Provides a link between {@link Log} and {@link me.lucko.luckperms.common.data.Log}
 */
@AllArgsConstructor
public class LogDelegate implements Log {
    private final me.lucko.luckperms.common.data.Log master;

    @Override
    public SortedSet<LogEntry> getContent() {
        return master.getContent();
    }

    @Override
    public SortedSet<LogEntry> getRecent() {
        return master.getRecent();
    }

    @Override
    public SortedMap<Integer, LogEntry> getRecent(int pageNo) {
        return master.getRecent(pageNo);
    }

    @Override
    public int getRecentMaxPages() {
        return master.getRecentMaxPages();
    }

    @Override
    public SortedSet<LogEntry> getRecent(@NonNull UUID actor) {
        return master.getRecent(actor);
    }

    @Override
    public SortedMap<Integer, LogEntry> getRecent(int pageNo, @NonNull UUID actor) {
        return master.getRecent(pageNo, actor);
    }

    @Override
    public int getRecentMaxPages(@NonNull UUID actor) {
        return master.getRecentMaxPages(actor);
    }

    @Override
    public SortedSet<LogEntry> getUserHistory(@NonNull UUID uuid) {
        return master.getUserHistory(uuid);
    }

    @Override
    public SortedMap<Integer, LogEntry> getUserHistory(int pageNo, @NonNull UUID uuid) {
        return master.getUserHistory(pageNo, uuid);
    }

    @Override
    public int getUserHistoryMaxPages(@NonNull UUID uuid) {
        return master.getUserHistoryMaxPages(uuid);
    }

    @Override
    public SortedSet<LogEntry> getGroupHistory(@NonNull String name) {
        return master.getGroupHistory(checkName(name));
    }

    @Override
    public SortedMap<Integer, LogEntry> getGroupHistory(int pageNo, @NonNull String name) {
        return master.getGroupHistory(pageNo, checkName(name));
    }

    @Override
    public int getGroupHistoryMaxPages(@NonNull String name) {
        return master.getGroupHistoryMaxPages(checkName(name));
    }

    @Override
    public SortedSet<LogEntry> getTrackHistory(@NonNull String name) {
        return master.getTrackHistory(checkName(name));
    }

    @Override
    public SortedMap<Integer, LogEntry> getTrackHistory(int pageNo, @NonNull String name) {
        return master.getTrackHistory(pageNo, checkName(name));
    }

    @Override
    public int getTrackHistoryMaxPages(@NonNull String name) {
        return master.getTrackHistoryMaxPages(checkName(name));
    }

    @Override
    public SortedSet<LogEntry> getSearch(@NonNull String query) {
        return master.getSearch(query);
    }

    @Override
    public SortedMap<Integer, LogEntry> getSearch(int pageNo, @NonNull String query) {
        return master.getSearch(pageNo, query);
    }

    @Override
    public int getSearchMaxPages(@NonNull String query) {
        return master.getSearchMaxPages(query);
    }
}
