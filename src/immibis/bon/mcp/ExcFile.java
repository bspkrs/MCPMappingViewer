/*
 * Copyright (C) 2013 Alex "immibis" Campbell
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
package immibis.bon.mcp;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ExcFile
{
    public Map<String, String[]> exceptions         = new HashMap<String, String[]>();
    
    private static String[]      EMPTY_STRING_ARRAY = new String[0];
    
    // returns internal names, can return null
    // input uses SRG names
    public String[] getExceptionClasses(String clazz, String func, String desc)
    {
        String[] r = exceptions.get(clazz + "/" + func + desc);
        if (r == null)
            return EMPTY_STRING_ARRAY;
        return r;
    }
    
    public ExcFile(File f) throws IOException
    {
        //example line:
        //net/minecraft/src/NetClientHandler.<init>(Lnet/minecraft/client/Minecraft;Ljava/lang/String;I)V=java/net/UnknownHostException,java/io/IOException|p_i42_1_,p_i42_2_,p_i42_3_
        
        Scanner in = new Scanner(new FileReader(f));
        try
        {
            while (in.hasNextLine())
            {
                if (in.hasNext("#"))
                {
                    in.nextLine();
                    continue;
                }
                in.useDelimiter("\\.");
                String clazz = in.next();
                in.useDelimiter("\\(");
                String func = in.next().substring(1);
                in.useDelimiter("=");
                String desc = in.next();
                in.useDelimiter("\\|");
                String excs = in.next().substring(1);
                in.nextLine(); // skip rest of line
                exceptions.put(clazz + "/" + func + desc, excs.split(","));
            }
        }
        finally
        {
            in.close();
        }
    }
}
