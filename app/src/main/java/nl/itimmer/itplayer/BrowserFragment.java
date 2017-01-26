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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.VerticalGridFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.FocusHighlight;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.VerticalGridPresenter;
import android.util.DisplayMetrics;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import nl.itimmer.itplayer.glide.StreamNfsLoader;
import nl.itimmer.itplayer.player.PlayerActivity;
import nl.itimmer.itplayer.vfs.Media;

public class BrowserFragment extends VerticalGridFragment implements OnItemViewClickedListener, OnItemViewSelectedListener {
    private static final String TAG = "BrowserFragment";

    private static final int BACKGROUND_UPDATE_DELAY = 300;

    private ArrayObjectAdapter rowsAdapter;
    private Browser browser;
    private String path;

    private Handler mainHandler;
    private Timer backgroundTimer;
    private BackgroundManager backgroundManager;
    private DisplayMetrics metrics;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        VerticalGridPresenter gridPresenter = new VerticalGridPresenter(FocusHighlight.ZOOM_FACTOR_MEDIUM);
        gridPresenter.setNumberOfColumns(4);
        setGridPresenter(gridPresenter);

        setOnItemViewClickedListener(this);
        setOnItemViewSelectedListener(this);

        backgroundManager = ((MainActivity) getActivity()).getBackgroundManager();
        metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        mainHandler = new Handler(getContext().getMainLooper());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    loadData(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setBrowser(Browser browser, String path) {
        this.browser = browser;
        this.path = path;
    }

    protected void loadData(String path) throws IOException {
        List<Media> list = browser.listFiles(path);
        rowsAdapter = new ArrayObjectAdapter(new CardPresenter(browser));
        rowsAdapter.addAll(0, list);

        //Update list on ui thread
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setAdapter(rowsAdapter);
            }
        });
    }

    @Override
    public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        if (item instanceof Media) {
            Media media = (Media) item;

            if (null != backgroundTimer) {
                backgroundTimer.cancel();
            }
            backgroundTimer = new Timer();
            backgroundTimer.schedule(new UpdateBackgroundTask(media.getBackgroundImagePath()), BACKGROUND_UPDATE_DELAY);
        }
    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        if (item instanceof Media) {
            final Media media = (Media) item;
            if (!media.isFile()) {
                MainActivity activity = (MainActivity) getActivity();
                activity.openDirectory(media.getPath());
            } else {
                Intent intent = new Intent(getActivity(), PlayerActivity.class);
                intent.putExtra(PlayerActivity.MEDIA, media);
                startActivity(intent);
            }
        }
    }

    private class UpdateBackgroundTask extends TimerTask {

        private String url;

        public UpdateBackgroundTask(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    Glide.with(getActivity())
                        .using(new StreamNfsLoader(browser.getContext()))
                        .load(url)
                        .centerCrop()
                        .into(new SimpleTarget<GlideDrawable>(metrics.widthPixels, metrics.heightPixels) {
                            @Override
                            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                                backgroundManager.setDrawable(resource);
                            }
                        });
                }
            });
        }
    }
}
