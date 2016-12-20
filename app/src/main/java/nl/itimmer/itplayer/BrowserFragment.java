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

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v17.leanback.app.VerticalGridFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.FocusHighlight;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.VerticalGridPresenter;
import android.util.Log;

public class BrowserFragment extends VerticalGridFragment implements OnItemViewClickedListener {
    private static final String TAG = "BrowserFragment";

    private ArrayObjectAdapter rowsAdapter;
    private Browser browser;
    private String path;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        VerticalGridPresenter gridPresenter = new VerticalGridPresenter(FocusHighlight.ZOOM_FACTOR_MEDIUM);
        gridPresenter.setNumberOfColumns(4);
        setGridPresenter(gridPresenter);
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
        List<MediaFile> list = browser.listFiles(path);
        rowsAdapter = new ArrayObjectAdapter(new CardPresenter(browser));
        rowsAdapter.addAll(0, list);

        setOnItemViewClickedListener(this);

        //Update list on ui thread
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setAdapter(rowsAdapter);
            }
        });
    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        if (item instanceof MediaFile) {
            final MediaFile media = (MediaFile) item;
            if (!media.isFile()) {
                MainActivity activity = (MainActivity) getActivity();
                activity.openDirectory(media.getPath());
            }
        }
    }
}
