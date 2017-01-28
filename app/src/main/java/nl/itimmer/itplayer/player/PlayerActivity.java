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

import android.app.Activity;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.VideoFragment;
import android.support.v17.leanback.app.VideoFragmentGlueHost;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextRenderer;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.MimeTypes;

import java.io.IOException;
import java.util.List;

import nl.itimmer.itplayer.Browser;
import nl.itimmer.itplayer.Config;
import nl.itimmer.itplayer.R;
import nl.itimmer.itplayer.vfs.MediaFile;

public class PlayerActivity extends Activity implements ExoPlayer.EventListener, TextRenderer.Output {

    private static final String TAG = "PlayerActivity";

    public static final String MEDIA = "MEDIA";

    private SubtitleView subtitleView;
    private SimpleExoPlayer player;
    private ExoPlayerGlue glueHelper;

    private MediaFile media;
    private Browser browser;

    private VideoFragment videoFragment;

    private MediaSession session;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        media = (MediaFile) getIntent().getSerializableExtra(MEDIA);

        videoFragment = (VideoFragment) getFragmentManager().findFragmentById(R.id.playback_fragment);
        VideoFragmentGlueHost glueHost = new VideoFragmentGlueHost(videoFragment);

        subtitleView = (SubtitleView) findViewById(R.id.subtitle_view);
        if (subtitleView != null) {
            subtitleView.setUserDefaultStyle();
            subtitleView.setUserDefaultTextSize();
        }

        Handler mainHandler = new Handler();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(mainHandler, videoTrackSelectionFactory);
        LoadControl loadControl = new DefaultLoadControl();
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
        player.addListener(this);
        player.setTextOutput(this);

        glueHelper = new ExoPlayerGlue(player, this);
        glueHelper.setHost(glueHost);

        session = new MediaSession(this, "ITPlayer");
        session.setCallback(new ExoPlayerMediaSessionCalback(player));
        session.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        session.setActive(true);

        MediaMetadata.Builder metaBuilder = new MediaMetadata.Builder();
        session.setMetadata(metaBuilder.build());

        new SourceTask().execute(media);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (player.getPlayWhenReady() && player.getPlaybackState() == ExoPlayer.STATE_READY) {
            if (!requestVisibleBehind(true))
                player.setPlayWhenReady(false);
        } else
            requestVisibleBehind(false);
    }

    @Override
    public void onVisibleBehindCanceled() {
        player.setPlayWhenReady(false);
        super.onVisibleBehindCanceled();
    }

    @Override
    public void onResume() {
        super.onResume();
        session.setActive(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        session.release();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.release();
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        PlaybackState.Builder stateBuilder = new PlaybackState.Builder();
        stateBuilder.setState(playWhenReady ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED, player.getCurrentPosition(), 1.0f);

        long actions = PlaybackState.ACTION_PLAY;
        if (playWhenReady)
            actions |= PlaybackState.ACTION_PAUSE;

        stateBuilder.setActions(actions);
        session.setPlaybackState(stateBuilder.build());
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onCues(List<Cue> cues) {
        if (subtitleView != null)
            subtitleView.onCues(cues);
    }

    class SourceTask extends AsyncTask<MediaFile, Void, MediaSource[]> {

        @Override
        protected MediaSource[] doInBackground(MediaFile... media) {
            try {
                browser = Browser.getInstance(Config.mountDirectory);

                DataSource.Factory dataSourceFactory = new NfsDataSourceFactory(browser.getContext());
                ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                MediaSource videoSource = new ExtractorMediaSource(Uri.parse("nfs://host/" + media[0].getPath()), dataSourceFactory, extractorsFactory, null, null);

                if (media[0].getSubtitlePath() != null) {
                    Format subtitleFormat = Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP, null, Format.NO_VALUE, Format.NO_VALUE, "en", null);
                    MediaSource subtitleSource = new SingleSampleMediaSource(Uri.parse("nfs://host/" + media[0].getSubtitlePath()), dataSourceFactory, subtitleFormat, Long.MAX_VALUE, 0);

                    return new MediaSource[]{videoSource, subtitleSource};
                } else
                    return new MediaSource[]{videoSource};
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(MediaSource... sources) {
            if (sources != null) {
                player.prepare(sources.length == 1 ? sources[0] : new MergingMediaSource(sources));
                player.setPlayWhenReady(true);
            }
        }
    }
}
