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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.app.PlaybackFragmentGlueHost;
import android.support.v17.leanback.media.PlaybackControlGlue;
import android.support.v17.leanback.media.PlaybackGlueHost;
import android.support.v17.leanback.media.SurfaceHolderGlueHost;
import android.view.SurfaceHolder;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;

public class ExoPlayerGlue extends PlaybackControlGlue implements ExoPlayer.EventListener, SurfaceHolder.Callback {

    private SimpleExoPlayer player;

    public ExoPlayerGlue(SimpleExoPlayer player, Context context) {
        super(context, new int[] {PLAYBACK_SPEED_NORMAL});
        this.player = player;
        player.addListener(this);
    }

    @Override
    public boolean hasValidMedia() {
        return player.getPlaybackState() != ExoPlayer.STATE_IDLE;
    }

    @Override
    public boolean isMediaPlaying() {
        return player.getPlayWhenReady() && player.getPlaybackState() == ExoPlayer.STATE_READY;
    }

    @Override
    public CharSequence getMediaTitle() {
        return player.getCurrentManifest() != null ? player.getCurrentManifest().toString() : null;
    }

    @Override
    public CharSequence getMediaSubtitle() {
        return null;
    }

    @Override
    public int getMediaDuration() {
        return (int) player.getDuration();
    }

    @Override
    public Drawable getMediaArt() {
        return null;
    }

    @Override
    public long getSupportedActions() {
        return ACTION_PLAY_PAUSE;
    }

    @Override
    public int getCurrentSpeedId() {
        return player.getPlayWhenReady() ? PLAYBACK_SPEED_NORMAL : PLAYBACK_SPEED_PAUSED;
    }

    @Override
    public int getCurrentPosition() {
        return (int) player.getCurrentPosition();
    }

    @Override
    public void play(int speed) {
        player.setPlayWhenReady(true);
    }

    @Override
    public void pause() {
        player.setPlayWhenReady(false);
    }

    @Override
    protected void onAttachedToHost(PlaybackGlueHost host) {
        super.onAttachedToHost(host);
        if (host instanceof SurfaceHolderGlueHost) {
            ((SurfaceHolderGlueHost) host).setSurfaceHolderCallback(this);
        }
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        onStateChanged();
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        onStateChanged();
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        onMetadataChanged();
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        player.setVideoSurfaceHolder(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        player.setVideoSurfaceHolder(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        player.setVideoSurfaceHolder(null);
    }
}
