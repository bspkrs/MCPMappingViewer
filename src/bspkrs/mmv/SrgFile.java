/*
 * Copyright (C) 2013 Alex "immibis" Campbell, bspkrs
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
 * 
 * Modified version of SrgFile.java from BON
 */
package bspkrs.mmv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class SrgFile
{
    
    public final Map<String, ClassSrgData>             classes      = new HashMap<String, ClassSrgData>();
    public final Map<String, FieldSrgData>             fields       = new HashMap<String, FieldSrgData>();
    public final Map<String, MethodSrgData>            methods      = new HashMap<String, MethodSrgData>();
    public final Map<ClassSrgData, Set<MethodSrgData>> classMethods = new HashMap<ClassSrgData, Set<MethodSrgData>>();
    public final Map<ClassSrgData, Set<FieldSrgData>>  classFields  = new HashMap<ClassSrgData, Set<FieldSrgData>>();
    
    public static String getLastComponent(String s)
    {
        String[] parts = s.split("/");
        return parts[parts.length - 1];
    }
    
    public SrgFile(File f) throws IOException
    {
        Scanner in = new Scanner(new BufferedReader(new FileReader(f)));
        try
        {
            while (in.hasNextLine())
            {
                if (in.hasNext("CL:"))
                {
                    // CL: a net/minecraft/util/EnumChatFormatting
                    in.next(); // skip CL:
                    String obf = in.next();
                    String deobf = in.next();
                    String srgName = getLastComponent(deobf);
                    String pkgName = deobf.substring(0, deobf.indexOf(srgName) - 1);
                    ClassSrgData classData = new ClassSrgData(obf, srgName, pkgName, in.hasNext("#C"));
                    classes.put(srgName, classData);
                    if (!classMethods.containsKey(classData))
                        classMethods.put(classData, new HashSet<MethodSrgData>());
                    if (!classFields.containsKey(classData))
                        classFields.put(classData, new HashSet<FieldSrgData>());
                }
                else if (in.hasNext("FD:"))
                {
                    // FD: aql/c net/minecraft/block/BlockStoneBrick/field_94408_c #C
                    in.next(); // skip FD:
                    String[] obf = in.next().split("/");
                    String obfOwner = obf[0];
                    String obfName = obf[1];
                    String deobf = in.next();
                    String srgName = getLastComponent(deobf);
                    String srgOwner = getLastComponent(deobf.substring(0, deobf.indexOf(srgName) - 1));
                    FieldSrgData fieldData = new FieldSrgData(obfOwner, obfName, srgOwner, srgName, in.hasNext("#C"));
                    fields.put(srgName, fieldData);
                    classFields.get(classes.get(srgOwner)).add(fieldData);
                }
                else if (in.hasNext("MD:"))
                {
                    // MD: aor/a (Lmt;)V net/minecraft/block/BlockHay/func_94332_a (Lnet/minecraft/client/renderer/texture/IconRegister;)V #C
                    in.next(); // skip MD:
                    String[] obf = in.next().split("/");
                    String obfOwner = obf[0];
                    String obfName = obf[1];
                    String obfDescriptor = in.next();
                    String deobf = in.next();
                    String srgName = getLastComponent(deobf);
                    String srgOwner = getLastComponent(deobf.substring(0, deobf.indexOf(srgName) - 1));
                    String srgDescriptor = in.next();
                    MethodSrgData methodData = new MethodSrgData(obfOwner, obfName, obfDescriptor, srgOwner, srgName, srgDescriptor, in.hasNext("#C"));
                    methods.put(srgName, methodData);
                    classMethods.get(classes.get(srgOwner)).add(methodData);
                }
                else
                    in.nextLine();
            }
        }
        finally
        {
            in.close();
        }
    }
}
