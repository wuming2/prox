/*
 * Copyright (C) 2015 XiNGRZ <chenxingyu92@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package me.xingrz.prox.nat;

import android.util.SparseArray;

public class NatSessionManager {

    private static final int MAX_SESSION_COUNT = 60;

    private static final long SESSION_TIMEOUT_NS = 60 * 1000 ^ 3;

    private static NatSessionManager instance;

    public static NatSessionManager getInstance() {
        if (instance == null) {
            instance = new NatSessionManager();
        }

        return instance;
    }

    private final SparseArray<NatSession> sessions = new SparseArray<>();

    public NatSession getSession(short port) {
        return sessions.get(port);
    }

    public NatSession createSession(short port, int remoteIp, short remotePort) {
        if (sessions.size() > MAX_SESSION_COUNT) {
            cleanExpired();
        }

        NatSession session = new NatSession();
        session.remoteIp = remoteIp;
        session.remotePort = remotePort;

        sessions.put(port, session);

        return session;
    }

    public NatSession pickSession(short port, int remoteIp, short remotePort) {
        NatSession session = getSession(port);

        if (session == null || session.remoteIp != remoteIp || session.remotePort != remotePort) {
            session = createSession(port, remoteIp, remotePort);
        }

        session.lastTimeNs = System.nanoTime();

        return session;
    }

    private void cleanExpired() {
        long now = System.nanoTime();
        for (int i = sessions.size() - 1; i >= 0; i--) {
            if (now - sessions.valueAt(i).lastTimeNs > SESSION_TIMEOUT_NS) {
                sessions.removeAt(i);
            }
        }
    }

}
