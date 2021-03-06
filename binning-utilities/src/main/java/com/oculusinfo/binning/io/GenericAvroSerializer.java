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
package com.oculusinfo.binning.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.file.CodecFactory;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;

import com.oculusinfo.binning.TileData;
import com.oculusinfo.binning.TileIndex;
import com.oculusinfo.binning.TilePyramid;

abstract public class GenericAvroSerializer<T> implements TileSerializer<T> {
    private static final long serialVersionUID = 5775555328063499845L;



    protected GenericAvroSerializer () {
    }

    abstract protected String getRecordSchemaFile ();
    abstract protected Map<String, String> getTileMetaData ();
    abstract protected T getValue (GenericRecord bin);
    abstract protected void setValue (GenericRecord bin, T value) throws IOException ;
    
    public String getFileExtension(){
    	return "avro";
    }
    
    protected Schema getRecordSchema () throws IOException {
        return new AvroSchemaComposer().addResource(getRecordSchemaFile()).resolved();
    }
    protected Schema getTileSchema () throws IOException {
        return new AvroSchemaComposer().add(getRecordSchema()).addResource("tile.avsc").resolved();
    }

    @Override
    public TileData<T> deserialize (TileIndex index, InputStream stream) throws IOException {

    	DatumReader<GenericRecord> reader = new GenericDatumReader<GenericRecord>();
    	DataFileStream<GenericRecord> dataFileReader = new DataFileStream<GenericRecord>(stream, reader);
    	
        try {
        	
        	GenericRecord r = dataFileReader.next();
        	
            int xBins = (Integer) r.get("xBinCount");
            int yBins = (Integer) r.get("yBinCount");
                    
            @SuppressWarnings("unchecked")
            GenericData.Array<GenericRecord> bins = (GenericData.Array<GenericRecord>) r.get("values");

            // Warning suppressed because Array.newInstance definitionally returns 
            // something of the correct type, or throws an exception 
            List<T> data = new ArrayList<T>(xBins*yBins);
            int i = 0;
            for (GenericRecord bin: bins) {
                data.add(getValue(bin));
                if (i >= xBins*yBins) break;
            }
            TileIndex newTileIndex = new TileIndex(index.getLevel(), index.getX(), index.getY(), xBins, yBins);
            TileData<T> newTile = new TileData<T>(newTileIndex, data);
            return newTile;
        } finally {
        	dataFileReader.close();
        	stream.close();
        }
    }

    @Override
    public void serialize (TileData<T> tile, TilePyramid pyramid, OutputStream stream) throws IOException {
        Schema recordSchema = getRecordSchema();
        Schema tileSchema = getTileSchema();

        List<GenericRecord> bins = new ArrayList<GenericRecord>();

        for (T value: tile.getData()){
        	if (value == null)continue;
            GenericRecord bin = new GenericData.Record(recordSchema);
            setValue(bin, value);
            bins.add(bin);
        }

        GenericRecord tileRecord = new GenericData.Record(tileSchema);
        TileIndex idx = tile.getDefinition();
        tileRecord.put("level", idx.getLevel());
        tileRecord.put("xIndex", idx.getX());
        tileRecord.put("yIndex", idx.getY());
        tileRecord.put("xBinCount", idx.getXBins());
        tileRecord.put("yBinCount", idx.getYBins());
        tileRecord.put("values", bins);
        tileRecord.put("default", null);
        tileRecord.put("meta", getTileMetaData());

        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<GenericRecord>(tileSchema);
		DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<GenericRecord>(datumWriter);
		try {
			dataFileWriter.setCodec(CodecFactory.bzip2Codec());
			dataFileWriter.create(tileSchema, stream);
			dataFileWriter.append(tileRecord);
			dataFileWriter.close();
			stream.close();
		} catch (IOException e) {throw new RuntimeException("Error serializing",e);}	
    }
}
