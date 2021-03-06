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
package com.oculusinfo.binning;

import java.io.Serializable;

/**
 * Simple immutable tile index representation
 * 
 * @author nkronenfeld
 */
public class TileIndex implements Serializable, Comparable<TileIndex> {
    private static final long serialVersionUID = -1L;

    private int               _level;
    private int               _x;
    private int               _y;
    private int               _xBins;
    private int               _yBins;

    /**
     * Create a tile index representation
     * 
     * @param level
     *            The level of the tile
     * @param x
     *            The x coordinate of the tile
     * @param y
     *            The y coordinate of the tile
     */
    public TileIndex (int level, int x, int y) {
        _level = level;
        _x = x;
        _y = y;
        _xBins = 256;
        _yBins = 256;
    }

    /**
     * Create a tile index representation
     * 
     * @param level
     *            The level of the tile
     * @param x
     *            The x coordinate of the tile
     * @param y
     *            The y coordinate of the tile
     * @param xBins
     *            the number of bins in this tile in the x direction
     * @param yBins
     *            the number of bins in this tile in the y direction
     */
    public TileIndex (int level, int x, int y, int xBins, int yBins) {
        _level = level;
        _x = x;
        _y = y;
        _xBins = xBins;
        _yBins = yBins;
    }

    /**
     * Create a new tile index representation, identical but for the number of
     * bins in the tile
     * 
     * @param base
     *            The base tile representation being copied
     * @param xBins
     *            The new number of bins to use in the x direction
     * @param yBins
     *            The new number of bins to use in the y direction
     */
    public TileIndex (TileIndex base, int xBins, int yBins) {
        _level = base.getLevel();
        _x = base.getX();
        _y = base.getY();
        _xBins = xBins;
        _yBins = yBins;
    }

    /**
     * Get the tile level
     */
    public int getLevel () {
        return _level;
    }

    /**
     * Get the x coordinate of the tile
     */
    public int getX () {
        return _x;
    }

    /**
     * Get the y coordinate of the tile
     */
    public int getY () {
        return _y;
    }

    /**
     * Get the number of bins this tile should have in the x direction
     */
    public int getXBins () {
        return _xBins;
    }

    /**
     * Get the number of bins this tile should have in the y direction
     */
    public int getYBins () {
        return _yBins;
    }

    /**
     * Translates from a bin relative to the root position of this tile, to a
     * bin relative to the root position of the entire data set.
     * 
     * @param tile
     *            The tile in which this bin falls
     * @param bin
     *            The bin relative to the root position of this tile (with
     *            coordinates [0 to getXBins(), 0 to getYBins()])
     * @return The bin relative to the root position of the entire data set
     *         (with coordinates [0 to getXBins()*2^level, 0 to
     *         getYBins()*2^level])
     */
    public static BinIndex tileBinIndexToUniversalBinIndex (TileIndex tile, BinIndex bin) {
        // Tiles go from lower left to upper right
        // Bins go from upper left to lower right
        int pow2 = 1 << tile.getLevel();
        int tileLeft = tile.getX() * tile.getXBins();
        int tileTop = (pow2 - tile.getY() - 1) * tile.getYBins();
        return new BinIndex(tileLeft + bin.getX(), tileTop + bin.getY());
    }

    /**
     * Translates from the root position of the entire data set to a bin
     * relative to the root position of this tile, to a bin relative.
     * 
     * @param sampleTile
     *            a sample tile specifying the level and number of x and y bins
     *            per tile required
     * @param bin
     *            The bin relative to the root position of the entire data set
     *            (with coordinates [0 to getXBins()*2^level, 0 to
     *            getYBins()*2^level])
     * @return The tile (with level, xbins, and ybins matching the input sample
     *         tile), and bin relative to the root position of this tile (with
     *         coordinates [0 to getXBins(), 0 to getYBins()])
     */
    public static TileAndBinIndices universalBinIndexToTileBinIndex (TileIndex sampleTile,
                                                              BinIndex bin) {
        // Tiles go from lower left to upper right
        // Bins go from upper left to lower right
        int level = sampleTile.getLevel();
        int pow2 = 1 << level;

        int xBins = sampleTile.getXBins();
        int tileX = bin.getX()/xBins;
        int tileLeft = tileX * xBins;

        int yBins = sampleTile.getYBins();
        int tileY = pow2 - bin.getY()/yBins - 1;
        int tileTop = (pow2 - tileY - 1) * yBins;

        BinIndex tileBin = new BinIndex(bin.getX()-tileLeft, bin.getY()-tileTop);
        TileIndex tile = new TileIndex(level, tileX, tileY, xBins, yBins);

        return new TileAndBinIndices(tile, tileBin);
    }

    @Override
    public int compareTo (TileIndex t) {
        if (_level < t._level)
            return -1;
        if (_level > t._level)
            return 1;

        if (_x < t._x)
            return -1;
        if (_x > t._x)
            return 1;

        if (_y < t._y)
            return -1;
        if (_y > t._y)
            return 1;

        return 0;
    }

    @Override
    public boolean equals (Object obj) {
        if (this == obj)
            return true;
        if (null == obj)
            return false;
        if (!(obj instanceof TileIndex))
            return false;

        TileIndex t = (TileIndex) obj;
        if (t._level != _level)
            return false;
        if (t._x != _x)
            return false;
        if (t._y != _y)
            return false;
        if (t._xBins != _xBins)
            return false;
        if (t._yBins != _yBins)
            return false;
        return true;
    }

    @Override
    public int hashCode () {
        // Shouldn't often be comparing tiles of different bin sizes; ignoring
        // bin size for hash code calculation.
        int pow2 = 1 << _level;
        return _x * pow2 + _y;
    }

    @Override
    public String toString () {
        return String.format("[%d / %d, %d / %d, lvl %d]", _x, _xBins, _y,
                             _yBins, _level);
    }

    public static TileIndex fromString (String string) {
        try {
            int a = string.indexOf('[') + 1;
            int b = string.indexOf('/', a);
            int x = Integer.parseInt(string.substring(a, b).trim());
            a = b + 1;
            b = string.indexOf(',', a);
            int xBins = Integer.parseInt(string.substring(a + 1, b).trim());
            a = b + 1;
            b = string.indexOf('/', a);
            int y = Integer.parseInt(string.substring(a + 1, b).trim());
            a = b + 1;
            b = string.indexOf(", lvl", a);
            int yBins = Integer.parseInt(string.substring(a + 1, b).trim());
            a = b + 5;
            b = string.indexOf(']', a);
            int level = Integer.parseInt(string.substring(a + 1, b).trim());

            return new TileIndex(level, x, y, xBins, yBins);
        } catch (NumberFormatException e) {
            return null;
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }
}
