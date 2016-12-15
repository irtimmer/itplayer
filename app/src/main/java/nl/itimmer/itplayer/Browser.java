/*
 * This file is part of ITPlayer.
 *
 * Copyright (C) 2016 Iwan Timmer
 *
 * ITPlayer is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * ITPlayer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ITPlayer; if not, see <http://www.gnu.org/licenses/>.
 */

package nl.itimmer.itplayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import nl.itimmer.networkfs.nfs.NfsContext;
import nl.itimmer.networkfs.nfs.NfsFile;

public class Browser {

    private NfsContext ctx;
    private static WeakHashMap<String, Browser> instances;

    protected Browser(String path) throws IOException {
        ctx = new NfsContext();
        ctx.mount(Config.nfsServer, path);
    }

    public static Browser getInstance(String path) throws IOException {
        if (instances == null)
            instances = new WeakHashMap<>();

        if (!instances.containsKey(path)) {
            Browser browser = new Browser(path);
            instances.put(path, browser);
            return browser;
        } else
            return instances.get(path);
    }

    public List<MediaFile> listFiles(String path) throws IOException {
        NfsFile dir = new NfsFile(ctx, path);
        NfsFile[] files = dir.listFiles();
        List<MediaFile> list = new ArrayList<>();
        for (NfsFile file:files) {
            System.out.println(" Add " + file.getName());
            if (!file.getName().startsWith(".")) {
                MediaFile media = new MediaFile(file.getName(), file.getPath(), file.getSize(), file.isFile());
                media.setCardImagePath(file.getPath() + "/landscape.jpg");
                media.setBackgroundImagePath(file.getPath() + "/fanart.jpg");
                list.add(media);
            }
        }

        return list;
    }

    public NfsContext getContext() {
        return ctx;
    }
}
