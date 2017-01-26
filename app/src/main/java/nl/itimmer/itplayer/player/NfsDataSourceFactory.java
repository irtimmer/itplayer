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

package nl.itimmer.itplayer.player;

import android.net.Uri;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import nl.itimmer.networkfs.nfs.NfsContext;
import nl.itimmer.networkfs.nfs.NfsFile;
import nl.itimmer.networkfs.nfs.NfsFileInputStream;

public class NfsDataSourceFactory implements DataSource.Factory {

    public NfsContext ctx;

    public NfsDataSourceFactory(NfsContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public DataSource createDataSource() {
        return new NfsDataSource();
    }

    class NfsDataSource implements DataSource {

        private NfsFileInputStream in;
        private long length;
        private InputStream bin;

        private Uri uri;

        @Override
        public long open(DataSpec dataSpec) throws IOException {
            if (in == null) {
                NfsFile file = new NfsFile(ctx, dataSpec.uri.getPath());
                length = file.getSize();

                in = new NfsFileInputStream(ctx, dataSpec.uri.getPath());
            }

            in.seek(dataSpec.position);
            bin = new BufferedInputStream(in);
            this.uri = dataSpec.uri;
            return length;
        }

        @Override
        public int read(byte[] buffer, int offset, int readLength) throws IOException {
            return bin.read(buffer, offset, readLength);
        }

        @Override
        public Uri getUri() {
            return uri;
        }

        @Override
        public void close() throws IOException {
            if (in != null) {
                in.close();
                in = null;
            }
        }

    }
}
