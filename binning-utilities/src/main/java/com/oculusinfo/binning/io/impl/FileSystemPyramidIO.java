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
package com.oculusinfo.binning.io.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import com.oculusinfo.binning.TileIndex;
import com.oculusinfo.binning.TilePyramid;
import com.oculusinfo.binning.TileData;
import com.oculusinfo.binning.io.PyramidIO;
import com.oculusinfo.binning.io.TileSerializer;

public class FileSystemPyramidIO implements PyramidIO {
    private String _rootPath;
    private String _extension;



	public FileSystemPyramidIO (String rootPath, String extension){
		_rootPath = rootPath;
		_extension = extension;
	}

    public Object getRootPath () {
        return _rootPath;
    }

	private File getTileFile (String basePath, TileIndex tile) {
        return new File(String.format("%s/" + PyramidIO.TILES_FOLDERNAME
                                              + "/%d/%d/%d." + _extension,
                                      _rootPath + basePath,
                                      tile.getLevel(), tile.getX(), tile.getY()));
    }

    private File getMetaDataFile (String basePath) {
        return new File(_rootPath + basePath+"/"+PyramidIO.METADATA_FILENAME);
    }

    @Override
    public void initializeForWrite (String basePath) throws IOException {
    }

    @Override
    public <T> void writeTiles (String basePath, TilePyramid tilePyramid, TileSerializer<T> serializer,
                                Iterable<TileData<T>> data) throws IOException {
        for (TileData<T> tile: data) {
            File tileFile = getTileFile(basePath, tile.getDefinition());
            File parent = tileFile.getParentFile();
            if (!parent.exists()) parent.mkdirs();

            FileOutputStream fileStream = new FileOutputStream(tileFile);
            serializer.serialize(tile, tilePyramid, fileStream);
            fileStream.close();
        }
    }

    @Override
    public void writeMetaData (String basePath, String metaData) throws IOException {
        FileOutputStream stream = new FileOutputStream(getMetaDataFile(basePath));
        stream.write(metaData.getBytes());
        stream.close();
    }

    @Override
    public <T> List<TileData<T>> readTiles (String basePath,
                                            TileSerializer<T> serializer,
                                            Iterable<TileIndex> tiles) throws IOException {
        List<TileData<T>> results = new LinkedList<TileData<T>>();
        for (TileIndex tile: tiles) {
            File tileFile = getTileFile(basePath, tile);

            if (tileFile.exists() && tileFile.isFile()) {
                FileInputStream stream = new FileInputStream(tileFile);
                TileData<T> data = serializer.deserialize(tile, stream);
                results.add(data);
                stream.close();
            }
        }
        return results;
    }

    @Override
    public InputStream getTileStream (String basePath, TileIndex tile) throws IOException {
        File tileFile = getTileFile(basePath, tile);

        if (tileFile.exists() && tileFile.isFile()) {
            return new FileInputStream(tileFile);
        } else {
            return null;
        }
    }

    @Override
    public String readMetaData (String basePath) throws IOException {
        File metaDataFile = getMetaDataFile(basePath);
        if (!metaDataFile.exists()) return null;

        FileInputStream stream = new FileInputStream(metaDataFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String rawMetaData = "";
        String line;
        while (null != (line = reader.readLine())) {
            rawMetaData = rawMetaData + line;
        }
        reader.close();
        return rawMetaData;
    }
}
