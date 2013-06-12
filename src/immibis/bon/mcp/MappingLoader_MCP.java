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

import immibis.bon.IProgressListener;
import immibis.bon.Mapping;
import immibis.bon.NameSet;
import immibis.bon.NameSet.Side;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class MappingLoader_MCP
{
    
    public static class CantLoadMCPMappingException extends Exception
    {
        private static final long serialVersionUID = 1;
        
        public CantLoadMCPMappingException(String reason)
        {
            super(reason);
        }
    }
    
    @SuppressWarnings("unused")
    private final Side               side;
    @SuppressWarnings("unused")
    private final String             mcVer;
    private final File               mcpDir;
    private final int                sideNumber;
    private final File               srgFile, excFile;
    
    // forward: obf -> searge -> mcp
    // reverse: mcp -> searge -> obf
    private Mapping                  forwardSRG, reverseSRG, forwardCSV, reverseCSV;
    
    private Map<String, String>      srgMethodDescriptors = new HashMap<>();        // SRG name -> SRG descriptor
    private Map<String, Set<String>> srgMethodOwners      = new HashMap<>();        // SRG name -> SRG owners
    private Map<String, Set<String>> srgFieldOwners       = new HashMap<>();        // SRG name -> SRG owners
                                                                                     
    private ExcFile                  excFileData;
    
    public MappingLoader_MCP(String mcVer, Side side, File mcpDir, IProgressListener progress) throws IOException, CantLoadMCPMappingException
    {
        this.mcVer = mcVer;
        this.mcpDir = mcpDir;
        this.side = side;
        
        switch (side)
        {
            case UNIVERSAL:
                sideNumber = 2;
                if (new File(mcpDir, "conf/packaged.srg").exists())
                {
                    srgFile = new File(mcpDir, "conf/packaged.srg");
                    excFile = new File(mcpDir, "conf/packaged.exc");
                }
                else
                {
                    srgFile = new File(mcpDir, "conf/joined.srg");
                    excFile = new File(mcpDir, "conf/joined.exc");
                }
                break;
            
            case CLIENT:
                sideNumber = 0;
                srgFile = new File(mcpDir, "conf/client.srg");
                excFile = new File(mcpDir, "conf/client.exc");
                break;
            
            case SERVER:
                sideNumber = 1;
                srgFile = new File(mcpDir, "conf/server.srg");
                excFile = new File(mcpDir, "conf/server.exc");
                break;
            
            default:
                throw new AssertionError("side is " + side);
        }
        
        NameSet obfNS = new NameSet(NameSet.Type.OBF, side, mcVer);
        NameSet srgNS = new NameSet(NameSet.Type.SRG, side, mcVer);
        NameSet mcpNS = new NameSet(NameSet.Type.MCP, side, mcVer);
        
        forwardSRG = new Mapping(obfNS, srgNS);
        reverseSRG = new Mapping(srgNS, obfNS);
        
        forwardCSV = new Mapping(srgNS, mcpNS);
        reverseCSV = new Mapping(mcpNS, srgNS);
        
        if (progress != null)
            progress.setMax(3);
        if (progress != null)
            progress.set(0);
        loadEXCFile();
        if (progress != null)
            progress.set(1);
        loadSRGMapping();
        if (progress != null)
            progress.set(2);
        loadCSVMapping();
    }
    
    private void loadEXCFile() throws IOException
    {
        excFileData = new ExcFile(excFile);
    }
    
    private void loadSRGMapping() throws IOException, CantLoadMCPMappingException
    {
        SrgFile srg = new SrgFile(srgFile, false);
        
        forwardSRG.setDefaultPackage("net/minecraft/src/");
        reverseSRG.addPrefix("net/minecraft/src/", "");
        
        for (Map.Entry<String, String> entry : srg.classes.entrySet())
        {
            String obfClass = entry.getKey();
            String srgClass = entry.getValue();
            
            forwardSRG.setClass(obfClass, srgClass);
            reverseSRG.setClass(srgClass, obfClass);
        }
        
        for (Map.Entry<String, String> entry : srg.fields.entrySet())
        {
            String obfOwnerAndName = entry.getKey();
            String srgName = entry.getValue();
            
            String obfOwner = obfOwnerAndName.substring(0, obfOwnerAndName.lastIndexOf('/'));
            String obfName = obfOwnerAndName.substring(obfOwnerAndName.lastIndexOf('/') + 1);
            
            String srgOwner = srg.classes.get(obfOwner);
            
            // Enum values don't use the CSV and don't start with field_
            if (srgName.startsWith("field_"))
            {
                if (srgFieldOwners.containsKey(srgName))
                    System.out.println("SRG field " + srgName + " appears in multiple classes (at least " + srgFieldOwners.get(srgName) + " and " + srgOwner + ")");
                
                Set<String> owners = srgFieldOwners.get(srgName);
                if (owners == null)
                    srgFieldOwners.put(srgName, owners = new HashSet<String>());
                owners.add(srgOwner);
            }
            
            forwardSRG.setField(obfOwner, obfName, srgName);
            reverseSRG.setField(srgOwner, srgName, obfName);
        }
        
        for (Map.Entry<String, String> entry : srg.methods.entrySet())
        {
            String obfOwnerNameAndDesc = entry.getKey();
            String srgName = entry.getValue();
            
            String obfOwnerAndName = obfOwnerNameAndDesc.substring(0, obfOwnerNameAndDesc.indexOf('('));
            String obfOwner = obfOwnerAndName.substring(0, obfOwnerAndName.lastIndexOf('/'));
            String obfName = obfOwnerAndName.substring(obfOwnerAndName.lastIndexOf('/') + 1);
            String obfDesc = obfOwnerNameAndDesc.substring(obfOwnerNameAndDesc.indexOf('('));
            
            String srgDesc = forwardSRG.mapMethodDescriptor(obfDesc);
            String srgOwner = srg.classes.get(obfOwner);
            
            srgMethodDescriptors.put(srgName, srgDesc);
            
            Set<String> srgMethodOwnersThis = srgMethodOwners.get(srgName);
            if (srgMethodOwnersThis == null)
                srgMethodOwners.put(srgName, srgMethodOwnersThis = new HashSet<>());
            srgMethodOwnersThis.add(srgOwner);
            
            forwardSRG.setMethod(obfOwner, obfName, obfDesc, srgName);
            reverseSRG.setMethod(srgOwner, srgName, srgDesc, obfName);
            
            String[] srgExceptions = excFileData.getExceptionClasses(srgOwner, srgName, srgDesc);
            if (srgExceptions.length > 0)
            {
                List<String> obfExceptions = new ArrayList<>();
                for (String s : srgExceptions)
                    obfExceptions.add(reverseSRG.getClass(s));
                forwardSRG.setExceptions(obfOwner, obfName, obfDesc, obfExceptions);
            }
        }
    }
    
    private void loadCSVMapping() throws IOException, CantLoadMCPMappingException
    {
        Map<String, String> fieldNames = CsvFile.read(new File(mcpDir, "conf/fields.csv"), sideNumber);
        Map<String, String> methodNames = CsvFile.read(new File(mcpDir, "conf/methods.csv"), sideNumber);
        
        for (Map.Entry<String, String> entry : fieldNames.entrySet())
        {
            String srgName = entry.getKey();
            String mcpName = entry.getValue();
            
            if (srgFieldOwners.get(srgName) == null)
                System.out.println("Field exists in CSV but not in SRG: " + srgName + " (CSV name: " + mcpName + ")");
            else
            {
                for (String srgOwner : srgFieldOwners.get(srgName))
                {
                    String mcpOwner = srgOwner;
                    
                    forwardCSV.setField(srgOwner, srgName, mcpName);
                    reverseCSV.setField(mcpOwner, mcpName, srgName);
                }
            }
        }
        
        for (Map.Entry<String, String> entry : methodNames.entrySet())
        {
            String srgName = entry.getKey();
            String mcpName = entry.getValue();
            
            if (srgMethodOwners.get(srgName) == null)
            {
                System.out.println("Method exists in CSV but not in SRG: " + srgName + " (CSV name: " + mcpName + ")");
            }
            else
            {
                for (String srgOwner : srgMethodOwners.get(srgName))
                {
                    String srgDesc = srgMethodDescriptors.get(srgName);
                    String mcpOwner = srgOwner;
                    String mcpDesc = srgDesc;
                    
                    forwardCSV.setMethod(srgOwner, srgName, srgDesc, mcpName);
                    reverseCSV.setMethod(mcpOwner, mcpName, mcpDesc, srgName);
                }
            }
        }
    }
    
    public Mapping getReverseSRG()
    {
        return reverseSRG;
    }
    
    public Mapping getReverseCSV()
    {
        return reverseCSV;
    }
    
    public Mapping getForwardSRG()
    {
        return forwardSRG;
    }
    
    public Mapping getForwardCSV()
    {
        return forwardCSV;
    }
    
    public static String getMCVer(File mcpDir) throws IOException
    {
        try (Scanner in = new Scanner(new File(mcpDir, "conf/version.cfg")))
        {
            while (in.hasNextLine())
            {
                String line = in.nextLine();
                if (line.startsWith("ClientVersion"))
                    return line.split("=")[1].trim();
            }
            return "unknown";
        }
    }
}
