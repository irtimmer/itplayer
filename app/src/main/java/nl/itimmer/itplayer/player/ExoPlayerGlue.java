/*
 * This file is part of ITPlayer.
 *
 * Copyright (C) 2016, 2017 Iwan Timmer
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
import android.os.Handler;
import android.support.v17.leanback.media.PlaybackControlGlue;
import android.support.v17.leanback.media.PlaybackGlueHost;
import android.support.v17.leanback.media.SurfaceHolderGlueHost;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.support.v17.leanback.widget.SparseArrayObjectAdapter;
import android.view.SurfaceHolder;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelections;
import com.google.android.exoplayer2.trackselection.TrackSelector;

public class ExoPlayerGlue extends PlaybackControlGlue implements ExoPlayer.EventListener, SurfaceHolder.Callback, Runnable, TrackSelector.EventListener<MappingTrackSelector.MappedTrackInfo> {

    private SparseArrayObjectAdapter primaryActionsAdapter;

    private PlaybackControlsRow.ClosedCaptioningAction closedCaptionAction;

    private SimpleExoPlayer player;
    private MappingTrackSelector trackSelector;

    private Handler handler;

    private int speed;
    private PlaybackSpeedTask playbackSpeed;

    public ExoPlayerGlue(SimpleExoPlayer player, MappingTrackSelector trackSelector, Context context) {
        super(context, new int[] {PLAYBACK_SPEED_FAST_L0, PLAYBACK_SPEED_FAST_L1, PLAYBACK_SPEED_FAST_L2, PLAYBACK_SPEED_FAST_L3, PLAYBACK_SPEED_FAST_L4});
        handler = new Handler();

        this.player = player;
        this.trackSelector = trackSelector;
        player.addListener(this);
        trackSelector.addListener(this);

        playbackSpeed = new PlaybackSpeedTask();
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
        return ACTION_PLAY_PAUSE | ACTION_FAST_FORWARD | ACTION_REWIND;
    }

    @Override
    public int getCurrentSpeedId() {
        return player.getPlayWhenReady() ? speed : PLAYBACK_SPEED_PAUSED;
    }

    @Override
    public int getCurrentPosition() {
        return (int) player.getCurrentPosition();
    }

    @Override
    public void play(int speed) {
        this.speed = speed;
        handler.removeCallbacks(playbackSpeed);
        if (speed < 0 || speed > 1) {
            handler.postDelayed(playbackSpeed, 0);
            System.out.println("Set speed " + speed);
        }

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
    public void enableProgressUpdating(boolean enable) {
        if (enable)
            handler.postDelayed(this, getUpdatePeriod());
        else
            handler.removeCallbacks(this);
    }

    @Override
    protected void onCreatePrimaryActions(SparseArrayObjectAdapter primaryActionsAdapter) {
        this.primaryActionsAdapter = primaryActionsAdapter;

        closedCaptionAction = new PlaybackControlsRow.ClosedCaptioningAction(getContext());
        closedCaptionAction.setIndex(PlaybackControlsRow.ClosedCaptioningAction.ON);

        primaryActionsAdapter.notifyArrayItemRangeChanged(primaryActionsAdapter.indexOf(closedCaptionAction), 1);

        primaryActionsAdapter.set(ACTION_CUSTOM_RIGHT_FIRST, closedCaptionAction);
    }

    @Override
    public void onActionClicked(Action action) {
        if (action == closedCaptionAction) {
            int textRenderIndex = getRenderIndex(C.TRACK_TYPE_TEXT);
            trackSelector.setRendererDisabled(textRenderIndex, !trackSelector.getRendererDisabled(textRenderIndex));
        } else
            super.onActionClicked(action);
    }

    public int getRenderIndex(int renderType) {
        for (int i = 0; i < player.getRendererCount(); i++) {
            if (player.getRendererType(i) == renderType)
                return i;
        }
        return -1;
    }

    @Override
    public void run() {
        updateProgress();
        handler.postDelayed(this, getUpdatePeriod());
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

    @Override
    public void onTrackSelectionsChanged(TrackSelections<? extends MappingTrackSelector.MappedTrackInfo> trackSelections) {
        int textRenderIndex = getRenderIndex(C.TRACK_TYPE_TEXT);
        boolean status = trackSelector.getRendererDisabled(textRenderIndex);
        closedCaptionAction.setIndex(status ? PlaybackControlsRow.ClosedCaptioningAction.ON : PlaybackControlsRow.ClosedCaptioningAction.OFF);
        primaryActionsAdapter.notifyArrayItemRangeChanged(primaryActionsAdapter.indexOf(closedCaptionAction), 1);
    }

    class PlaybackSpeedTask implements Runnable {

        public int getPlayPeriod() {
            //TODO better method to calculate play period
            return 1000;
        }

        public int getSeekPeriod() {
            int rate = speed > 0 ? speed - 8 : speed + 8;
            return (rate * getPlayPeriod()) - getPlayPeriod();
        }

        @Override
        public void run() {
            if (speed < 0 || speed > 1) {
                player.seekTo(player.getCurrentWindowIndex(), player.getCurrentPosition() + getSeekPeriod());
                if (player.getCurrentPosition() > 0)
                    handler.postDelayed(this, getPlayPeriod());
                else
                    play(PLAYBACK_SPEED_NORMAL);
            }
        }
    }
}
