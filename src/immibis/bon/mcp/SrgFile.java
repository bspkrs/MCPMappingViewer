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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class SrgFile
{
    
    public Map<String, String> classes = new HashMap<String, String>(); // name -> name
    public Map<String, String> fields  = new HashMap<String, String>(); // owner/name -> name
    public Map<String, String> methods = new HashMap<String, String>(); // owner/namedesc -> name
                                                                        
    public static String getLastComponent(String s)
    {
        String[] parts = s.split("/");
        return parts[parts.length - 1];
    }
    
    public SrgFile(File f, boolean reverse) throws IOException
    {
        Scanner in = new Scanner(new BufferedReader(new FileReader(f)));
        try
        {
            while (in.hasNextLine())
            {
                if (in.hasNext("CL:"))
                {
                    in.next();
                    String obf = in.next();
                    String deobf = in.next();
                    if (reverse)
                        classes.put(deobf, obf);
                    else
                        classes.put(obf, deobf);
                }
                else if (in.hasNext("FD:"))
                {
                    in.next();
                    String obf = in.next();
                    String deobf = in.next();
                    if (reverse)
                        fields.put(deobf, getLastComponent(obf));
                    else
                        fields.put(obf, getLastComponent(deobf));
                }
                else if (in.hasNext("MD:"))
                {
                    in.next();
                    String obf = in.next();
                    String obfdesc = in.next();
                    String deobf = in.next();
                    String deobfdesc = in.next();
                    if (reverse)
                        methods.put(deobf + deobfdesc, getLastComponent(obf));
                    else
                        methods.put(obf + obfdesc, getLastComponent(deobf));
                }
                else
                {
                    in.nextLine();
                }
            }
        }
        finally
        {
            in.close();
        }
    }
}
