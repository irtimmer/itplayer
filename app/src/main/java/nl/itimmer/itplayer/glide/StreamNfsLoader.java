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

package nl.itimmer.itplayer.glide;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.stream.StreamModelLoader;

import java.io.InputStream;

import nl.itimmer.networkfs.nfs.NfsContext;
import nl.itimmer.networkfs.nfs.NfsFileInputStream;

public class StreamNfsLoader implements StreamModelLoader<String> {

    private NfsContext ctx;

    public StreamNfsLoader(NfsContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public DataFetcher<InputStream> getResourceFetcher(final String model, int width, int height) {
        return new DataFetcher<InputStream>() {
            @Override
            public InputStream loadData(Priority priority) throws Exception {
                return new NfsFileInputStream(ctx, model);
            }

            @Override
            public void cleanup() { }

            @Override
            public String getId() {
                return model;
            }

            @Override
            public void cancel() { }
        };
    }
}
