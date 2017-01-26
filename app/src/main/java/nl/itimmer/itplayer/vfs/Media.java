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

package nl.itimmer.itplayer.vfs;

import java.io.Serializable;

public class Media implements Serializable {

    private String name;
    private String path;

    private String cardImagePath;
    private String backgroundImagePath;

    private boolean isFile;

    public Media(String name, String path, boolean isFile) {
        this.name = name;
        this.path = path;
        this.isFile = isFile;
    }

    public Media(String name, String path) {
        this(name, path, false);
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public boolean isFile() {
        return isFile;
    }

    public void setCardImagePath(String cardImagePath) {
        this.cardImagePath = cardImagePath;
    }

    public String getCardImagePath() {
        return cardImagePath;
    }

    public void setBackgroundImagePath(String backgroundImagePath) {
        this.backgroundImagePath = backgroundImagePath;
    }

    public String getBackgroundImagePath() {
        return backgroundImagePath;
    }
}
