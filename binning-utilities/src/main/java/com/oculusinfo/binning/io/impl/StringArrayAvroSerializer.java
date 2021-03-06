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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.avro.generic.GenericRecord;

import com.oculusinfo.binning.io.GenericAvroArraySerializer;

public class StringArrayAvroSerializer extends GenericAvroArraySerializer<String> {
    private static final long serialVersionUID = 4109155349465641129L;



    public static final Map<String,String> META;
    static {
        Map<String,String> map = new HashMap<String, String>();
        map.put("source", "Oculus Binning Utilities");
        map.put("data-type", "string array");
        META = Collections.unmodifiableMap(map);
    }



    public StringArrayAvroSerializer () {
        super();
    }

    @Override
    protected String getEntrySchemaFile() {
    	return "stringEntry.avsc";
    }

    @Override
    protected Map<String, String> getTileMetaData () {
        return META;
    }

    @Override
    protected String getEntryValue(GenericRecord entry) {
    	return entry.get("value").toString();
    }

    @Override
    protected void setEntryValue(GenericRecord avroEntry, String rawEntry)
    		throws IOException {
    	avroEntry.put("value", rawEntry);
    }
}
