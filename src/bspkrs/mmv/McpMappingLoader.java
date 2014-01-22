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
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import bspkrs.mmv.McpBotCommand.MemberType;

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
    
    private final File                       mcpDir;
    private final File                       srgFile;
    private final Side                       side;
    private SrgFile                          srgFileData;
    private CsvFile                          csvFieldData, csvMethodData;
    private final Map<String, McpBotCommand> commandMap            = new TreeMap<String, McpBotCommand>(); // srgName -> McpBotCommand
                                                                                                            
    public final Map<MethodSrgData, CsvData> srgMethodData2CsvData = new TreeMap<MethodSrgData, CsvData>();
    public final Map<FieldSrgData, CsvData>  srgFieldData2CsvData  = new TreeMap<FieldSrgData, CsvData>();
    
    public McpMappingLoader(Side side, File mcpDir, IProgressListener progress) throws IOException, CantLoadMCPMappingException
    {
        this.mcpDir = mcpDir;
        this.side = side;
        
        String loadFailureReason = "";
        switch (side)
        {
            case Universal:
                if (new File(mcpDir, "conf/packaged.srg").exists())
                    srgFile = new File(mcpDir, "conf/packaged.srg");
                else
                    srgFile = new File(mcpDir, "conf/joined.srg");
                loadFailureReason = "Unable to find packaged.srg or joined.srg. Try using side Client or Server.";
                break;
            
            case Client:
                srgFile = new File(mcpDir, "conf/client.srg");
                loadFailureReason = "Unable to find client.srg. If using Forge, use side Universal.";
                break;
            
            case Server:
                srgFile = new File(mcpDir, "conf/server.srg");
                loadFailureReason = "Unable to find server.srg. If using Forge, use side Universal.";
                break;
            
            default:
                throw new AssertionError("side is " + side);
        }
        
        if (!srgFile.exists())
            throw new CantLoadMCPMappingException(loadFailureReason);
        
        if (progress != null)
            progress.setMax(3);
        if (progress != null)
            progress.set(0);
        loadCSVMapping();
        if (progress != null)
            progress.set(1);
        loadSRGMapping();
        if (progress != null)
            progress.set(2);
        linkSrgDataToCsvData();
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
        for (Entry<String, MethodSrgData> methodData : srgFileData.srgName2MethodData.entrySet())
        {
            if (!srgMethodData2CsvData.containsKey(methodData.getValue()) && csvMethodData.srgName2CsvData.containsKey(methodData.getKey()))
            {
                srgMethodData2CsvData.put(methodData.getValue(), csvMethodData.srgName2CsvData.get(methodData.getKey()));
            }
            else if (srgMethodData2CsvData.containsKey(methodData.getValue()))
                System.out.println("SRG method " + methodData.getKey() + " has multiple entries in CSV file!");
        }
        
        for (Entry<String, FieldSrgData> fieldData : srgFileData.srgName2FieldData.entrySet())
        {
            if (!srgFieldData2CsvData.containsKey(fieldData.getValue()) && csvFieldData.srgName2CsvData.containsKey(fieldData.getKey()))
            {
                srgFieldData2CsvData.put(fieldData.getValue(), csvFieldData.srgName2CsvData.get(fieldData.getKey()));
            }
            else if (srgFieldData2CsvData.containsKey(fieldData.getValue()))
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
    
    private CsvData processDataEdit(MemberType type, Map<String, ? extends MemberSrgData> srg2MemberData, Map<? extends MemberSrgData, CsvData> memberData2CsvData,
            String srgName, String mcpName, String comment)
    {
        MemberSrgData memberData = srg2MemberData.get(srgName);
        CsvData csvData = null;
        if (memberData != null)
        {
            boolean isForced = memberData2CsvData.containsKey(memberData);
            if (isForced)
            {
                csvData = memberData2CsvData.get(memberData);
                csvData.setMcpName(mcpName);
                csvData.setComment(comment);
            }
            else
            {
                csvData = new CsvData(srgName, mcpName, side.intSide[0], comment);
            }
            
            McpBotCommand command;
            
            if (!commandMap.containsKey(srgName))
                command = new McpBotCommand(McpBotCommand.getCommand(type, side, isForced),
                        csvData.getSrgName(), csvData.getMcpName(), csvData.getComment());
            else
                command = new McpBotCommand(commandMap.get(srgName).getCommand(),
                        csvData.getSrgName(), csvData.getMcpName(), csvData.getComment());
            
            commandMap.put(srgName, command);
        }
        
        return csvData;
    }
    
    public String getBotCommands(boolean clear)
    {
        String r = "";
        
        for (McpBotCommand command : commandMap.values())
            r += command.toString() + "\n";
        
        if (clear)
            commandMap.clear();
        
        return r;
    }
    
    public void saveCSVs(IProgressListener progress) throws IOException
    {
        if (progress != null)
        {
            progress.setMax(2);
            progress.set(0);
        }
        
        csvFieldData.writeToFile();
        
        if (progress != null)
            progress.set(1);
        
        csvMethodData.writeToFile();
        
        if (progress != null)
            progress.set(2);
    }
    
    public File getMcpDir()
    {
        return this.mcpDir;
    }
    
    public TableModel getSearchResults(String input, IProgressListener progress)
    {
        if (input == null || input.trim().isEmpty())
            return getClassModel();
        
        if (progress != null)
        {
            progress.setMax(3);
            progress.set(0);
        }
        
        Set<ClassSrgData> results = new TreeSet<ClassSrgData>();
        
        // Search Class objects
        for (ClassSrgData classData : srgFileData.srgName2ClassData.values())
            if (classData.contains(input))
                results.add(classData);
        
        if (progress != null)
            progress.set(1);
        
        // Search Methods
        for (Entry<ClassSrgData, Set<MethodSrgData>> entry : srgFileData.class2MethodDataSet.entrySet())
        {
            if (!results.contains(entry.getKey()))
            {
                for (MethodSrgData methodData : entry.getValue())
                {
                    CsvData csv = this.srgMethodData2CsvData.get(methodData);
                    if (methodData.contains(input) || (csv != null && csv.contains(input)))
                    {
                        results.add(entry.getKey());
                        break;
                    }
                }
            }
        }
        
        if (progress != null)
            progress.set(2);
        
        // Search Fields
        for (Entry<ClassSrgData, Set<FieldSrgData>> entry : srgFileData.class2FieldDataSet.entrySet())
        {
            if (!results.contains(entry.getKey()))
            {
                for (FieldSrgData fieldData : entry.getValue())
                {
                    CsvData csv = this.srgFieldData2CsvData.get(fieldData);
                    if (fieldData.contains(input) || (csv != null && csv.contains(input)))
                    {
                        results.add(entry.getKey());
                        break;
                    }
                }
            }
        }
        
        return new ClassModel(results);
    }
    
    public TableModel getClassModel()
    {
        return new ClassModel(this.srgFileData.srgName2ClassData.values());
    }
    
    public TableModel getMethodModel(String srgPkgAndOwner)
    {
        ClassSrgData classData = srgFileData.srgName2ClassData.get(srgPkgAndOwner);
        Set<MethodSrgData> methods = srgFileData.class2MethodDataSet.get(classData);
        return new MethodModel(methods);
    }
    
    public TableModel getFieldModel(String srgPkgAndOwner)
    {
        ClassSrgData classData = srgFileData.srgName2ClassData.get(srgPkgAndOwner);
        Set<FieldSrgData> fields = srgFileData.class2FieldDataSet.get(classData);
        return new FieldModel(fields);
    }
    
    @SuppressWarnings("rawtypes")
    public class ClassModel extends AbstractTableModel
    {
        private static final long              serialVersionUID    = 1L;
        public final String[]                  uniColumnNames      = { "Pkg name", "SRG name", "Obf name", "Client Only" };
        public final String[]                  columnNames         = { "Pkg name", "SRG name", "Obf name" };
        private final Class[]                  uniColumnTypes      = { String.class, String.class, String.class, Boolean.class };
        private final Class[]                  columnTypes         = { String.class, String.class, String.class };
        private final boolean[]                uniIsColumnEditable = { false, false, false, false };
        private final boolean[]                isColumnEditable    = { false, false, false };
        private final Object[][]               data;
        private final Collection<ClassSrgData> collectionRef;
        
        public ClassModel(Collection<ClassSrgData> map)
        {
            collectionRef = map;
            
            if (side.equals(Side.Universal))
                data = new Object[collectionRef.size()][uniColumnNames.length];
            else
                data = new Object[collectionRef.size()][columnNames.length];
            
            int i = 0;
            
            for (ClassSrgData classData : collectionRef)
            {
                data[i][0] = classData.getSrgPkgName();
                data[i][1] = classData.getSrgName();
                data[i][2] = classData.getObfName();
                
                if (side.equals(Side.Universal))
                    data[i][3] = classData.isClientOnly();
                
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
            if (side.equals(Side.Universal))
                return uniColumnNames.length;
            else
                return columnNames.length;
        }
        
        @Override
        public String getColumnName(int columnIndex)
        {
            if (side.equals(Side.Universal))
            {
                if (columnIndex < uniColumnNames.length && columnIndex >= 0)
                    return uniColumnNames[columnIndex];
            }
            else
            {
                if (columnIndex < columnNames.length && columnIndex >= 0)
                    return columnNames[columnIndex];
            }
            
            return "";
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            if (side.equals(Side.Universal))
            {
                if (columnIndex < uniColumnTypes.length && columnIndex >= 0)
                    return uniColumnTypes[columnIndex];
            }
            else
            {
                if (columnIndex < columnTypes.length && columnIndex >= 0)
                    return columnTypes[columnIndex];
            }
            
            return String.class;
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            if (side.equals(Side.Universal))
            {
                if (columnIndex < uniIsColumnEditable.length && columnIndex >= 0)
                    return uniIsColumnEditable[columnIndex];
            }
            else
            {
                if (columnIndex < isColumnEditable.length && columnIndex >= 0)
                    return isColumnEditable[columnIndex];
            }
            
            return false;
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            return data[rowIndex][Math.min(columnIndex, data[rowIndex].length - 1)];
        }
    }
    
    @SuppressWarnings("rawtypes")
    public class MethodModel extends AbstractTableModel
    {
        private static final long        serialVersionUID    = 1L;
        private final String[]           uniColumnNames      = { "MCP Name", "SRG Name", "Obf Name", "SRG Descriptor", "Comment", "Client Only" };
        private final String[]           columnNames         = { "MCP Name", "SRG Name", "Obf Name", "SRG Descriptor", "Comment" };
        private final Class[]            uniColumnTypes      = { String.class, String.class, String.class, String.class, String.class, Boolean.class };
        private final Class[]            columnTypes         = { String.class, String.class, String.class, String.class, String.class };
        private final boolean[]          uniIsColumnEditable = { true, false, false, false, true, false };
        private final boolean[]          isColumnEditable    = { true, false, false, false, true };
        private final Object[][]         data;
        private final Set<MethodSrgData> setRef;
        
        public MethodModel(Set<MethodSrgData> srgMethodSet)
        {
            setRef = srgMethodSet;
            
            if (side.equals(Side.Universal))
                data = new Object[setRef.size()][uniColumnNames.length];
            else
                data = new Object[setRef.size()][columnNames.length];
            
            int i = 0;
            
            for (MethodSrgData methodData : setRef)
            {
                CsvData csvData = srgMethodData2CsvData.get(methodData);
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
                
                if (side.equals(Side.Universal))
                    data[i][5] = methodData.isClientOnly();
                
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
            if (side.equals(Side.Universal))
                return uniColumnNames.length;
            else
                return columnNames.length;
        }
        
        @Override
        public String getColumnName(int columnIndex)
        {
            if (side.equals(Side.Universal))
            {
                if (columnIndex < uniColumnNames.length && columnIndex >= 0)
                    return uniColumnNames[columnIndex];
            }
            else
            {
                if (columnIndex < columnNames.length && columnIndex >= 0)
                    return columnNames[columnIndex];
            }
            
            return "";
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            if (side.equals(Side.Universal))
            {
                if (columnIndex < uniColumnTypes.length && columnIndex >= 0)
                    return uniColumnTypes[columnIndex];
            }
            else
            {
                if (columnIndex < columnTypes.length && columnIndex >= 0)
                    return columnTypes[columnIndex];
            }
            
            return String.class;
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            if (side.equals(Side.Universal))
            {
                if (columnIndex < uniIsColumnEditable.length && columnIndex >= 0)
                    return uniIsColumnEditable[columnIndex];
            }
            else
            {
                if (columnIndex < isColumnEditable.length && columnIndex >= 0)
                    return isColumnEditable[columnIndex];
            }
            
            return false;
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            return data[rowIndex][Math.min(columnIndex, data[rowIndex].length - 1)];
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            data[rowIndex][columnIndex] = aValue;
            
            if (columnIndex == 4 && aValue != null && (data[rowIndex][0] == null || data[rowIndex][0].toString().trim().isEmpty()))
                return; // if only the comment has been set, don't bother adding a command
                
            String srgName = (String) data[rowIndex][1];
            String mcpName = (String) data[rowIndex][0];
            String comment = (String) data[rowIndex][4];
            CsvData result = processDataEdit(MemberType.METHOD, srgFileData.srgName2MethodData, srgMethodData2CsvData, srgName, mcpName, comment);
            
            if (result != null)
            {
                csvMethodData.srgName2CsvData.put(srgName, result);
                srgMethodData2CsvData.put(srgFileData.srgName2MethodData.get(srgName), result);
            }
        }
    }
    
    @SuppressWarnings("rawtypes")
    public class FieldModel extends AbstractTableModel
    {
        private static final long       serialVersionUID    = 1L;
        private final String[]          uniColumnNames      = { "MCP Name", "SRG Name", "Obf Name", "Comment", "Client Only" };
        private final String[]          columnNames         = { "MCP Name", "SRG Name", "Obf Name", "Comment" };
        private final Class[]           uniColumnTypes      = { String.class, String.class, String.class, String.class, Boolean.class };
        private final Class[]           columnTypes         = { String.class, String.class, String.class, String.class };
        private final boolean[]         uniIsColumnEditable = { true, false, false, true, false };
        private final boolean[]         isColumnEditable    = { true, false, false, true };
        private final Object[][]        data;
        private final Set<FieldSrgData> setRef;
        
        public FieldModel(Set<FieldSrgData> srgFieldSet)
        {
            setRef = srgFieldSet;
            
            if (side.equals(Side.Universal))
                data = new Object[setRef.size()][uniColumnNames.length];
            else
                data = new Object[setRef.size()][columnNames.length];
            
            int i = 0;
            
            for (FieldSrgData fieldData : setRef)
            {
                CsvData csvData = srgFieldData2CsvData.get(fieldData);
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
                
                if (side.equals(Side.Universal))
                    data[i][4] = fieldData.isClientOnly();
                
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
            if (side.equals(Side.Universal))
                return uniColumnNames.length;
            else
                return columnNames.length;
        }
        
        @Override
        public String getColumnName(int columnIndex)
        {
            if (side.equals(Side.Universal))
            {
                if (columnIndex < uniColumnNames.length && columnIndex >= 0)
                    return uniColumnNames[columnIndex];
            }
            else
            {
                if (columnIndex < columnNames.length && columnIndex >= 0)
                    return columnNames[columnIndex];
            }
            
            return "";
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            if (side.equals(Side.Universal))
            {
                if (columnIndex < uniColumnTypes.length && columnIndex >= 0)
                    return uniColumnTypes[columnIndex];
            }
            else
            {
                if (columnIndex < columnTypes.length && columnIndex >= 0)
                    return columnTypes[columnIndex];
            }
            
            return String.class;
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            if (side.equals(Side.Universal))
            {
                if (columnIndex < uniIsColumnEditable.length && columnIndex >= 0)
                    return uniIsColumnEditable[columnIndex];
            }
            else
            {
                if (columnIndex < isColumnEditable.length && columnIndex >= 0)
                    return isColumnEditable[columnIndex];
            }
            
            return false;
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            return data[rowIndex][Math.min(columnIndex, data[rowIndex].length - 1)];
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            data[rowIndex][columnIndex] = aValue;
            
            if (columnIndex == 3 && aValue != null && (data[rowIndex][0] == null || data[rowIndex][0].toString().trim().isEmpty()))
                return; // if only the comment has been set, don't bother adding a command
                
            String srgName = (String) data[rowIndex][1];
            String mcpName = (String) data[rowIndex][0];
            String comment = (String) data[rowIndex][3];
            CsvData result = processDataEdit(MemberType.FIELD, srgFileData.srgName2FieldData, srgFieldData2CsvData, srgName, mcpName, comment);
            
            if (result != null)
            {
                csvFieldData.srgName2CsvData.put(srgName, result);
                srgFieldData2CsvData.put(srgFileData.srgName2FieldData.get(srgName), result);
            }
        }
    }
}
