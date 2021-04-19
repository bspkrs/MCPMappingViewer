package bspkrs.mmv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class TSrgFile extends SrgFile
{

    public TSrgFile(File f, ExcFile excFile, StaticMethodsFile staticMethods) throws FileNotFoundException
    {
        Scanner in = new Scanner(new BufferedReader(new FileReader(f)));
        try
        {
            String currentPackage = null;
            String currentClass = null;
            String currentObfClass = null;
            while (in.hasNextLine())
            {
                String[] line = in.nextLine().split(" ");

                if (line[0].indexOf("\t") == 0) // Member
                {
                    line[0] = line[0].substring(1); // ignore the leading tab

                    if (line.length == 2) // Field
                    {
                        //	a field_192300_a
                        String obf = line[0];
                        String srgName = line[1];
                        String obfOwner = currentObfClass;
                        String srgPkg = currentPackage;
                        String srgOwner = currentClass;

                        FieldSrgData fieldData = new FieldSrgData(obfOwner, obf, srgOwner, srgPkg, srgName, false);

                        srgFieldName2FieldData.put(srgName, fieldData);
                        class2FieldDataSet.get(srgClassName2ClassData.get(srgPkg + "/" + srgOwner)).add(fieldData);
                        srgFieldName2ClassData.put(srgName, srgClassName2ClassData.get(srgPkg + "/" + srgOwner));
                    }
                    else if (line.length == 3) // Method
                    {
                        //	b (Lhy;)Lu; func_192295_b
                        String obf = line[0];
                        String descriptor = line[1];
                        String srgName = line[2];
                        String obfOwner = currentObfClass;
                        String srgPkg = currentPackage;
                        String srgOwner = currentClass;

                        MethodSrgData methodData = new MethodSrgData(obfOwner, obf, descriptor, srgOwner, srgPkg, srgName, descriptor/*FIXME*/, false);

                        srgMethodName2MethodData.put(srgName, methodData);
                        class2MethodDataSet.get(srgClassName2ClassData.get(srgPkg + "/" + srgOwner)).add(methodData);
                        srgMethodName2ClassData.put(srgName, srgClassName2ClassData.get(srgPkg + "/" + srgOwner));

                        // Hack in the missing parameter data
                        ExcData toAdd = new ExcData(srgOwner, srgName, descriptor/*FIXME*/, new String[0], staticMethods.contains(srgName));
                        ExcData existing = excFile.srgMethodName2ExcData.get(srgName);

                        if ((existing == null) || (existing.getParameters().length < toAdd.getParameters().length))
                        {
                            excFile.srgMethodName2ExcData.put(srgName, toAdd);
                            for (String parameter : toAdd.getParameters())
                                excFile.srgParamName2ExcData.put(parameter, toAdd);
                        }
                    }
                }
                else // Class
                {
                    // u net/minecraft/advancements/DisplayInfo
                    currentObfClass = line[0];
                    currentPackage = line[1].substring(0, line[1].lastIndexOf('/'));
                    currentClass = line[1].substring(line[1].lastIndexOf('/') + 1);

                    ClassSrgData classData = new ClassSrgData(currentObfClass, currentClass, currentPackage, false);

                    if (!srgPkg2ClassDataSet.containsKey(currentPackage))
                        srgPkg2ClassDataSet.put(currentPackage, new TreeSet<ClassSrgData>());
                    srgPkg2ClassDataSet.get(currentPackage).add(classData);

                    srgClassName2ClassData.put(currentPackage + "/" + currentClass, classData);

                    if (!class2MethodDataSet.containsKey(classData))
                        class2MethodDataSet.put(classData, new TreeSet<MethodSrgData>());

                    if (!class2FieldDataSet.containsKey(classData))
                        class2FieldDataSet.put(classData, new TreeSet<FieldSrgData>());
                }
            }
        }
        finally
        {
            in.close();
        }
    }
}
