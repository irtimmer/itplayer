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

import android.media.session.MediaSession;

import com.google.android.exoplayer2.SimpleExoPlayer;

public class ExoPlayerMediaSessionCalback extends MediaSession.Callback {

    private SimpleExoPlayer player;

    public ExoPlayerMediaSessionCalback(SimpleExoPlayer player) {
        this.player = player;
    }

    @Override
    public void onPlay() {
        player.setPlayWhenReady(true);
    }

    @Override
    public void onPause() {
        player.setPlayWhenReady(false);
    }
}
