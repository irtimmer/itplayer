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

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v17.leanback.app.BackgroundManager;

import java.io.IOException;

public class MainActivity extends Activity {

    public Browser browser;

    private BackgroundManager backgroundManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        backgroundManager = BackgroundManager.getInstance(this);
        backgroundManager.attach(getWindow());

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    browser = Browser.getInstance(Config.mountDirectory);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Create new fragment and transaction
                            final BrowserFragment view = new BrowserFragment();
                            view.setBrowser(browser, Config.rootDirectory);

                            getFragmentManager().beginTransaction().replace(R.id.main_browse_fragment, view).commit();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public BackgroundManager getBackgroundManager() {
        return backgroundManager;
    }

    protected void openDirectory(String path) {
        // Create new fragment and transaction
        final BrowserFragment view = new BrowserFragment();
        view.setBrowser(browser, path);

        getFragmentManager().beginTransaction().replace(R.id.main_browse_fragment, view).addToBackStack(null).commit();
    }
}
