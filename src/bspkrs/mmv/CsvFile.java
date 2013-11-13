/*
 * Copyright (C) 2013 bspkrs
 * Portions Copyright (C) 2013 Alex "immibis" Campbell
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation 
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, 
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bspkrs.mmv;

import immibis.bon.gui.Side;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class CsvFile
{
    private final File                file;
    public final Map<String, CsvData> srgName2CsvData;
    private final Side                side;
    
    public CsvFile(File file, Side side) throws IOException
    {
        this.file = file;
        this.side = side;
        this.srgName2CsvData = new TreeMap<String, CsvData>();
        readFromFile();
    }
    
    public void readFromFile() throws IOException
    {
        Scanner in = new Scanner(new BufferedReader(new FileReader(file)));
        String strSide = String.valueOf(side.intSide);
        try
        {
            in.useDelimiter(",");
            while (in.hasNextLine())
            {
                String srgName = in.next();
                String mcpName = in.next();
                String side = in.next();
                String comment = in.nextLine();
                comment = comment.substring(1); // removes the ','
                if (side.equals(strSide))
                {
                    srgName2CsvData.put(srgName, new CsvData(srgName, mcpName, this.side.intSide, comment));
                }
            }
        }
        finally
        {
            in.close();
        }
    }
    
    public void writeToFile() throws IOException
    {
        PrintWriter out = new PrintWriter(new FileWriter(file));
        
        for (CsvData data : this.srgName2CsvData.values())
            out.println(data.toCsv());
        
        out.close();
        
    }
}
