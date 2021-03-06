package com.oculusinfo.binning.io.impl;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import com.oculusinfo.binning.TileData;
import com.oculusinfo.binning.TileIndex;
import com.oculusinfo.binning.TilePyramid;
import com.oculusinfo.binning.impl.WebMercatorTilePyramid;
import com.oculusinfo.binning.io.Pair;
import com.oculusinfo.binning.io.impl.JDBCPyramidIO;
import com.oculusinfo.binning.io.impl.SQLitePyramidIO;
import com.oculusinfo.binning.io.impl.StringIntPairArrayJSONSerializer;

/**
 * test SQLite implementation
 * This will only work on a machine with SQLite already installed; since we 
 * don't want to mandate SQLite installation just to build properly, the 
 * default state of these tests is to ignore them.
 */
@Ignore
public class SQLitePyramidIOTest {

    private static final String PYRAMID_ID = "testPyramid";
    JDBCPyramidIO sqlitePyramidIO = null;


    @Before
    public void setUp() throws Exception {
        try {
            sqlitePyramidIO = new SQLitePyramidIO("test.db");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @After
    public void tearDown() throws Exception {
        try {
            sqlitePyramidIO.shutdown();
            File dbFile = new File("test.db");
            if (!dbFile.delete()) fail("Failed to delete test database.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testInitializeForWrite() {
        try {
            sqlitePyramidIO.initializeForWrite(PYRAMID_ID);

            if (!sqlitePyramidIO.tableExists(PYRAMID_ID)) fail("Tile pyramid table wasn't created.");
            if (!sqlitePyramidIO.tableExists("metadata")) fail("Metadata table wasn't created.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSingleTile() {
        try {
            sqlitePyramidIO.initializeForWrite(PYRAMID_ID);

            // XXX: This test is coupled to some implementations of other
            // classes--ideally, these objects should be mocked.

            TilePyramid tilePyramid = new WebMercatorTilePyramid();
            TileIndex tileDef = new TileIndex(0, 0, 0, 1, 1);
            StringIntPairArrayJSONSerializer serializer = new StringIntPairArrayJSONSerializer();

            TileData<List<Pair<String, Integer>>> tileToWrite = new TileData<List<Pair<String, Integer>>>(tileDef);
            List<Pair<String, Integer>> binVals = new ArrayList<Pair<String,Integer>>();
            Pair<String, Integer> binVal = new Pair<String, Integer>("name", 1);
            binVals.add(binVal);
            tileToWrite.setBin(0, 0, binVals);

            sqlitePyramidIO.writeTiles(PYRAMID_ID, tilePyramid, serializer, Collections.singletonList(tileToWrite));

            List<TileData<List<Pair<String, Integer>>>> readResult =
                sqlitePyramidIO.readTiles(PYRAMID_ID, serializer,
                                          Collections.singletonList(tileDef));
            Assert.assertTrue(readResult.size()==1);

            TileData<List<Pair<String, Integer>>> readTileData = readResult.get(0);
            Assert.assertTrue(readTileData.getData().equals(tileToWrite.getData()));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testMetadata() {
        try {
            sqlitePyramidIO.initializeForWrite(PYRAMID_ID);

            String metadata = "Some metadata.";
            sqlitePyramidIO.writeMetaData(PYRAMID_ID, metadata);

            Assert.assertTrue(sqlitePyramidIO.readMetaData(PYRAMID_ID).equals(metadata));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
