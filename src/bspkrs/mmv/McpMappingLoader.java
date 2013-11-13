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

import immibis.bon.IProgressListener;
import immibis.bon.gui.Side;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

public class McpMappingLoader
{
    
    public static class CantLoadMCPMappingException extends Exception
    {
        private static final long serialVersionUID = 1;
        
        public CantLoadMCPMappingException(String reason)
        {
            super(reason);
        }
    }
    
    private final Side                  side;
    @SuppressWarnings("unused")
    private final String                mcVer;
    private final File                  mcpDir;
    private final File                  srgFile;
    //    private final File                  excFile;
    private SrgFile                     srgFileData;
    private CsvFile                     csvFieldData, csvMethodData;
    
    private Map<MethodSrgData, CsvData> srg2csvMethods = new TreeMap<MethodSrgData, CsvData>();
    private Map<FieldSrgData, CsvData>  srg2csvFields  = new TreeMap<FieldSrgData, CsvData>();
    
    //    @SuppressWarnings("unused")
    //    private ExcFile                     excFileData;
    
    public McpMappingLoader(String mcVer, Side side, File mcpDir, IProgressListener progress) throws IOException, CantLoadMCPMappingException
    {
        this.mcVer = mcVer;
        this.mcpDir = mcpDir;
        this.side = side;
        
        switch (side)
        {
            case Universal:
                if (new File(mcpDir, "conf/packaged.srg").exists())
                {
                    srgFile = new File(mcpDir, "conf/packaged.srg");
                    //excFile = new File(mcpDir, "conf/packaged.exc");
                }
                else
                {
                    srgFile = new File(mcpDir, "conf/joined.srg");
                    //excFile = new File(mcpDir, "conf/joined.exc");
                }
                break;
            
            case Client:
                srgFile = new File(mcpDir, "conf/client.srg");
                //excFile = new File(mcpDir, "conf/client.exc");
                break;
            
            case Server:
                srgFile = new File(mcpDir, "conf/server.srg");
                //excFile = new File(mcpDir, "conf/server.exc");
                break;
            
            default:
                throw new AssertionError("side is " + side);
        }
        
        if (progress != null)
            progress.setMax(4);
        if (progress != null)
            progress.set(0);
        loadEXCFile();
        if (progress != null)
            progress.set(1);
        loadCSVMapping();
        if (progress != null)
            progress.set(2);
        loadSRGMapping();
        if (progress != null)
            progress.set(3);
        linkSrgDataToCsvData();
    }
    
    private void loadEXCFile() throws IOException
    {
        //        excFileData = new ExcFile(excFile);
    }
    
    private void loadSRGMapping() throws IOException
    {
        srgFileData = new SrgFile(srgFile);
    }
    
    private void loadCSVMapping() throws IOException
    {
        csvFieldData = new CsvFile(new File(mcpDir, "conf/fields.csv"), side);
        csvMethodData = new CsvFile(new File(mcpDir, "conf/methods.csv"), side);
    }
    
    private void linkSrgDataToCsvData()
    {
        for (Entry<String, MethodSrgData> methodData : srgFileData.methods.entrySet())
        {
            if (!srg2csvMethods.containsKey(methodData.getValue()) && csvMethodData.data.containsKey(methodData.getKey()))
            {
                srg2csvMethods.put(methodData.getValue(), csvMethodData.data.get(methodData.getKey()));
            }
            else if (srg2csvMethods.containsKey(methodData.getValue()))
                System.out.println("SRG method " + methodData.getKey() + " has multiple entries in CSV file!");
        }
        
        for (Entry<String, FieldSrgData> fieldData : srgFileData.fields.entrySet())
        {
            if (!srg2csvFields.containsKey(fieldData.getValue()) && csvFieldData.data.containsKey(fieldData.getKey()))
            {
                srg2csvFields.put(fieldData.getValue(), csvFieldData.data.get(fieldData.getKey()));
            }
            else if (srg2csvFields.containsKey(fieldData.getValue()))
                System.out.println("SRG field " + fieldData.getKey() + " has multiple entries in CSV file!");
        }
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
    
    public File getMcpDir()
    {
        return this.mcpDir;
    }
    
    public TableModel getClassModel()
    {
        return new ClassModel(this.srgFileData.classes);
    }
    
    public TableModel getMethodModel(String srgPkgAndOwner)
    {
        ClassSrgData classData = srgFileData.classes.get(srgPkgAndOwner);
        Set<MethodSrgData> methods = srgFileData.classMethods.get(classData);
        return new MethodModel(methods);
    }
    
    public TableModel getFieldModel(String srgPkgAndOwner)
    {
        ClassSrgData classData = srgFileData.classes.get(srgPkgAndOwner);
        Set<FieldSrgData> fields = srgFileData.classFields.get(classData);
        return new FieldModel(fields);
    }
    
    public class ClassModel extends AbstractTableModel
    {
        private static final long               serialVersionUID = 1L;
        private final String[]                  columnNames      = { "Pkg name", "SRG name", "Obf name" };
        @SuppressWarnings("rawtypes")
        private final Class[]                   columnTypes      = { String.class, String.class, String.class };
        private final Object[][]                data;
        private final Map<String, ClassSrgData> mapRef;
        
        public ClassModel(Map<String, ClassSrgData> map)
        {
            mapRef = map;
            data = new Object[mapRef.size()][columnNames.length];
            int i = 0;
            
            for (ClassSrgData classData : mapRef.values())
            {
                data[i][0] = classData.getSrgPkgName();
                data[i][1] = classData.getSrgName();
                data[i][2] = classData.getObfName();
                i++;
            }
        }
        
        @Override
        public int getRowCount()
        {
            return data.length;
        }
        
        @Override
        public int getColumnCount()
        {
            return columnNames.length;
        }
        
        @Override
        public String getColumnName(int columnIndex)
        {
            if (columnIndex < columnNames.length && columnIndex >= 0)
                return columnNames[columnIndex];
            else
                return "";
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return columnTypes[columnIndex];
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return false;
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            return data[rowIndex][columnIndex];
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            // TODO
        }
    }
    
    public class MethodModel extends AbstractTableModel
    {
        private static final long        serialVersionUID = 1L;
        private final String[]           columnNames      = { "MCP Name", "SRG Name", "Obf Name", "Descriptor", "Comment" };
        @SuppressWarnings("rawtypes")
        private final Class[]            columnTypes      = { String.class, String.class, String.class, String.class, String.class };
        private final Object[][]         data;
        private final Set<MethodSrgData> setRef;
        
        public MethodModel(Set<MethodSrgData> srgMethodSet)
        {
            setRef = srgMethodSet;
            data = new Object[setRef.size()][columnNames.length];
            int i = 0;
            
            for (MethodSrgData methodData : setRef)
            {
                CsvData csvData = srg2csvMethods.get(methodData);
                if (csvData != null)
                {
                    data[i][0] = csvData.getMcpName();
                    data[i][4] = csvData.getComment();
                }
                else
                {
                    data[i][0] = "";
                    data[i][4] = "";
                }
                data[i][1] = methodData.getSrgName();
                data[i][2] = methodData.getObfName();
                data[i][3] = methodData.getSrgDescriptor();
                i++;
            }
        }
        
        @Override
        public int getRowCount()
        {
            return data.length;
        }
        
        @Override
        public int getColumnCount()
        {
            return columnNames.length;
        }
        
        @Override
        public String getColumnName(int columnIndex)
        {
            if (columnIndex < columnNames.length && columnIndex >= 0)
                return columnNames[columnIndex];
            else
                return "";
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return columnTypes[columnIndex];
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return false;
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            return data[rowIndex][columnIndex];
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            // TODO
        }
    }
    
    public class FieldModel extends AbstractTableModel
    {
        private static final long       serialVersionUID = 1L;
        private final String[]          columnNames      = { "MCP Name", "SRG Name", "Obf Name", "Comment" };
        @SuppressWarnings("rawtypes")
        private final Class[]           columnTypes      = { String.class, String.class, String.class, String.class };
        private final Object[][]        data;
        private final Set<FieldSrgData> setRef;
        
        public FieldModel(Set<FieldSrgData> srgFieldSet)
        {
            setRef = srgFieldSet;
            data = new Object[setRef.size()][columnNames.length];
            int i = 0;
            
            for (FieldSrgData fieldData : setRef)
            {
                CsvData csvData = srg2csvFields.get(fieldData);
                if (csvData != null)
                {
                    data[i][0] = csvData.getMcpName();
                    data[i][3] = csvData.getComment();
                }
                else
                {
                    data[i][0] = "";
                    data[i][3] = "";
                }
                data[i][1] = fieldData.getSrgName();
                data[i][2] = fieldData.getObfName();
                i++;
            }
        }
        
        @Override
        public int getRowCount()
        {
            return data.length;
        }
        
        @Override
        public int getColumnCount()
        {
            return columnNames.length;
        }
        
        @Override
        public String getColumnName(int columnIndex)
        {
            if (columnIndex < columnNames.length && columnIndex >= 0)
                return columnNames[columnIndex];
            else
                return "";
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return columnTypes[columnIndex];
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return false;
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            return data[rowIndex][columnIndex];
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            // TODO
        }
    }
}
