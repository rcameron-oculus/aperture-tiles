/**
 * Copyright (c) 2013 Oculus Info Inc. 
 * http://www.oculusinfo.com/
 * 
 * Released under the MIT License.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oculusinfo.binning.impl;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.Collection;

import com.oculusinfo.binning.BinIndex;
import com.oculusinfo.binning.TileIndex;
import com.oculusinfo.binning.TilePyramid;

/**
 * Area-of-Interest tile pyramid
 * 
 * A tile pyramid that represents a predefined area, breaking it up linearly and
 * evenly.
 * 
 * @author Jesse McGeachie
 */
public class AOITilePyramid implements TilePyramid, Serializable {
    private static final long serialVersionUID = 1L;



    double _minX;
    double _maxX;
    double _minY;
    double _maxY;

    public AOITilePyramid(double minX, double minY, double maxX, double maxY){
        _minX = minX;
        _maxX = maxX;
        _minY = minY;
        _maxY = maxY;
    }

    @Override
        public String getProjection () {
        return "EPSG:4326";
    }

    @Override
        public String getTileScheme () {
        return "TMS";
    }

    @Override
    public TileIndex rootToTile (Point2D point, int level) {
        return rootToTile(point.getX(), point.getY(), level, 256);
    }

    @Override
    public TileIndex rootToTile (Point2D point, int level, int bins) {
        return rootToTile(point.getX(), point.getY(), level, bins);
    }

    @Override
    public TileIndex rootToTile (double x, double y, int level) {
        return rootToTile(x, y, level, 256);
    }

    @Override
    public TileIndex rootToTile (double x, double y, int level, int bins) {
        int numDivs = 1 << level;

        int tileX = (int) Math.floor(numDivs*(x-_minX)/(_maxX - _minX));
        int tileY = (int) Math.floor(numDivs*(y-_minY)/(_maxY - _minY));

        return new TileIndex(level, tileX, tileY, bins, bins);
    }

    @Override
    public BinIndex rootToBin (Point2D point, TileIndex tile) {
        return rootToBin(point.getX(), point.getY(), tile);
    }

    @Override
    public BinIndex rootToBin (double x, double y, TileIndex tile) {
        int pow2 = 1 << tile.getLevel();
        double tileXSize = (_maxX-_minX)/pow2;
        double tileYSize = (_maxY-_minY)/pow2;

        double xInTile = x-_minX - tile.getX()*tileXSize;
        double yInTile = y-_minY - tile.getY()*tileYSize;

        int binX = (int) Math.floor(xInTile*tile.getXBins()/tileXSize);
        int binY = (int) Math.floor(yInTile*tile.getYBins()/tileYSize);

        return new BinIndex(binX, tile.getYBins()-1-binY);
    }

    @Override
    public Rectangle2D getTileBounds (TileIndex tile) {
        int pow2 = 1 << tile.getLevel();
        double tileXSize = (_maxX-_minX)/pow2;
        double tileYSize = (_maxY-_minY)/pow2;

        return new Rectangle2D.Double(_minX+tileXSize*tile.getX(),
                                      _minY+tileYSize*tile.getY(),
                                      tileXSize, tileYSize);
    }

    @Override
    public Rectangle2D getBinBounds(TileIndex tile, BinIndex bin) {
        int pow2 = 1 << tile.getLevel();
        double tileXSize = (_maxX-_minX)/pow2;
        double tileYSize = (_maxY-_minY)/pow2;
        double binXSize = tileXSize/tile.getXBins();
        double binYSize = tileYSize/tile.getYBins();

        int adjustedBinY = tile.getYBins()-1-bin.getY();
        return new Rectangle2D.Double(_minX+tileXSize*tile.getX()+binXSize*bin.getX(),
                                      _minY+tileYSize*tile.getY()+binYSize*adjustedBinY,
                                      binXSize, binYSize);
    }

    @Override
    public double getBinOverlap (TileIndex tile, BinIndex bin, Rectangle2D area) {
        Rectangle2D binBounds = getBinBounds(tile, bin);

        // We actually work in lat/lon, so this is what we want.
        double minx = (area.getMinX()-binBounds.getMinX())/binBounds.getWidth();
        double maxx = (area.getMaxX()-binBounds.getMinX())/binBounds.getWidth();
        double miny = (area.getMinY()-binBounds.getMinY())/binBounds.getHeight();
        double maxy = (area.getMaxY()-binBounds.getMinY())/binBounds.getHeight();

        minx = Math.min(Math.max(minx, 0.0), 1.0);
        maxx = Math.min(Math.max(maxx, 0.0), 1.0);
        miny = Math.min(Math.max(miny, 0.0), 1.0);
        maxy = Math.min(Math.max(maxy, 0.0), 1.0);

        return Math.abs((maxx-minx) * (maxy-miny));
    }

    @Override
    public Collection<TileIndex> getBestTiles(Rectangle2D bounds) {
        // TODO Auto-generated method stub
        return null;
    }

}
