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
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

public class SrgFile
{
    public enum ClassSearchType
    {
        PACKAGE,
        OBF,
        SRG;
    }
    
    public final Map<String, ClassSrgData>             srgName2ClassData   = new TreeMap<String, ClassSrgData>();            // full/pkg/ClassSrgName -> ClassSrgData
    public final Map<String, FieldSrgData>             srgName2FieldData   = new TreeMap<String, FieldSrgData>();            // field_12345_a -> FieldSrgData
    public final Map<String, MethodSrgData>            srgName2MethodData  = new TreeMap<String, MethodSrgData>();           // func_12345_a -> MethodSrgData
    public final Map<ClassSrgData, Set<MethodSrgData>> class2MethodSet     = new TreeMap<ClassSrgData, Set<MethodSrgData>>();
    public final Map<ClassSrgData, Set<FieldSrgData>>  class2FieldSet      = new TreeMap<ClassSrgData, Set<FieldSrgData>>();
    public final Map<String, ClassSrgData>             srgMethod2ClassData = new TreeMap<String, ClassSrgData>();
    public final Map<String, ClassSrgData>             srgField2ClassData  = new TreeMap<String, ClassSrgData>();
    
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
                    String pkgName = deobf.substring(0, deobf.lastIndexOf('/'));
                    ClassSrgData classData = new ClassSrgData(obf, srgName, pkgName, in.hasNext("#C"));
                    srgName2ClassData.put(pkgName + "/" + srgName, classData);
                    if (!class2MethodSet.containsKey(classData))
                        class2MethodSet.put(classData, new HashSet<MethodSrgData>());
                    if (!class2FieldSet.containsKey(classData))
                        class2FieldSet.put(classData, new HashSet<FieldSrgData>());
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
                    String srgPkg = deobf.substring(0, deobf.lastIndexOf('/'));
                    String srgOwner = getLastComponent(srgPkg);
                    srgPkg = srgPkg.substring(0, srgPkg.lastIndexOf('/'));
                    FieldSrgData fieldData = new FieldSrgData(obfOwner, obfName, srgOwner, srgPkg, srgName, in.hasNext("#C"));
                    srgName2FieldData.put(srgName, fieldData);
                    class2FieldSet.get(srgName2ClassData.get(srgPkg + "/" + srgOwner)).add(fieldData);
                    srgField2ClassData.put(srgName, srgName2ClassData.get(srgPkg + "/" + srgOwner));
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
                    String srgPkg = deobf.substring(0, deobf.lastIndexOf('/'));
                    String srgOwner = getLastComponent(srgPkg);
                    srgPkg = srgPkg.substring(0, srgPkg.lastIndexOf('/'));
                    String srgDescriptor = in.next();
                    MethodSrgData methodData = new MethodSrgData(obfOwner, obfName, obfDescriptor, srgOwner, srgPkg, srgName, srgDescriptor, in.hasNext("#C"));
                    srgName2MethodData.put(srgName, methodData);
                    class2MethodSet.get(srgName2ClassData.get(srgPkg + "/" + srgOwner)).add(methodData);
                    srgMethod2ClassData.put(srgName, srgName2ClassData.get(srgPkg + "/" + srgOwner));
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
