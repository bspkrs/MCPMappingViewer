/*
 * Copyright (C) 2015 bspkrs
 * Portions Copyright (C) 2014 Alex "immibis" Campbell
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

import java.io.File;
import java.io.IOException;
import java.security.DigestException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import bspkrs.mmv.McpBotCommand.MemberType;
import bspkrs.mmv.gui.MappingGui;

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

    private final File                                   baseDir                 = new File(new File(System.getProperty("user.home")), ".cache/MCPMappingViewer");
    private final String                                 baseSrgDir              = "{mc_ver}";
    private final String                                 baseMappingDir          = "{mc_ver}/{channel}_{map_ver}";
    private final String                                 baseMappingUrl          = "http://export.mcpbot.bspk.rs/mcp_{channel}/{map_ver}-{mc_ver}/mcp_{channel}-{map_ver}-{mc_ver}.zip";
    private final String                                 newBaseSrgUrl           = "https://files.minecraftforge.net/maven/de/oceanlabs/mcp/mcp_config/{mc_ver}/mcp_config-{mc_ver}.zip";
    private final String                                 oldBaseSrgUrl           = "http://export.mcpbot.bspk.rs/mcp/{mc_ver}/mcp-{mc_ver}-srg.zip";

    private final File                                   srgDir;
    private final File                                   mappingDir;
    private final File                                   srgFile;
    private final File                                   excFile;
    private final File                                   staticMethodsFile;
    private SrgFile                                      srgFileData;
    private ExcFile                                      excFileData;
    private StaticMethodsFile                            staticMethods;
    private CsvFile                                      csvFieldData, csvMethodData;
    private ParamCsvFile                                 csvParamData;
    private final MappingGui                             parentGui;
    private final Map<String, McpBotCommand>             commandMap              = new TreeMap<String, McpBotCommand>();                                                                // srgName -> McpBotCommand
    public final Map<MethodSrgData, CsvData>             srgMethodData2CsvData   = new TreeMap<MethodSrgData, CsvData>();
    public final Map<FieldSrgData, CsvData>              srgFieldData2CsvData    = new TreeMap<FieldSrgData, CsvData>();
    public final Map<ExcData, Map<String, ParamCsvData>> excData2MapParamCsvData = new TreeMap<ExcData, Map<String, ParamCsvData>>();

    public McpMappingLoader(MappingGui parentGui, String mappingString, IProgressListener progress) throws IOException, CantLoadMCPMappingException, NoSuchAlgorithmException, DigestException
    {
        progress.setMax(6);
        progress.set(0);
        this.parentGui = parentGui;

        // mappingString: <mc>_<channel>_<ver>, eg, 1.8_snapshot_20151118
        String[] tokens = mappingString.split("_");
        if (tokens.length < 3)
            throw new CantLoadMCPMappingException("Invalid mapping string specified.");

        boolean isNew = tokens[0].compareTo("1.13") >= 0;
        String baseSrgUrl = isNew ? newBaseSrgUrl : oldBaseSrgUrl;
        String srgFileName = isNew ? "config/joined.tsrg" : "joined.srg";
        String excFileName = isNew ? "config/exceptions.txt" : "joined.exc";
        String staticMethodsFileName = isNew ? "config/static_methods.txt" : "static_methods.txt";

        progress.set(0, "Fetching SRG data");
        srgDir = getSubDirForZip(tokens, baseSrgUrl, baseSrgDir);
        progress.set(1, "Fetching CSV data");
        mappingDir = getSubDirForZip(tokens, baseMappingUrl, baseMappingDir);

        srgFile = new File(srgDir, srgFileName);
        excFile = new File(srgDir, excFileName);
        staticMethodsFile = new File(srgDir, staticMethodsFileName);

        if (!srgFile.exists())
            throw new CantLoadMCPMappingException("Unable to find joined.srg. Your MCP conf folder may be corrupt.");

        if (!excFile.exists())
            throw new CantLoadMCPMappingException("Unable to find joined.exc. Your MCP conf folder may be corrupt.");

        if (!staticMethodsFile.exists())
            throw new CantLoadMCPMappingException("Unable to find static_methods.txt. Your MCP conf folder may be corrupt.");

        progress.set(2, "Loading CSV data");
        loadCsvMapping();
        progress.set(3, "Loading SRG data");
        loadSrgMapping(isNew);
        progress.set(4, "Linking SRG data with CSV data");
        linkSrgDataToCsvData();
        progress.set(5, "Linking EXC data with CSV data");
        linkExcDataToSetParamCsvData();
    }

    private File getSubDirForZip(String[] tokens, String baseZipUrl, String baseSubDir) throws CantLoadMCPMappingException, NoSuchAlgorithmException, DigestException, IOException
    {
        if (!baseDir.exists() && !baseDir.mkdirs())
            throw new CantLoadMCPMappingException("Application data folder does not exist and cannot be created.");

        File subDir = new File(baseDir, replaceTokens(baseSubDir, tokens));
        if (!subDir.exists() && !subDir.mkdirs())
            throw new CantLoadMCPMappingException("Data folder does not exist and cannot be created.");

        RemoteZipHandler rzh = new RemoteZipHandler(replaceTokens(baseZipUrl, tokens), subDir, "SHA1");
        rzh.checkRemoteZip();

        return subDir;
    }

    private String replaceTokens(String s, String[] tokens)
    {
        return s.replace("{mc_ver}", tokens[0]).replace("{channel}", tokens[1]).replace("{map_ver}", tokens[2]);
    }

    private void loadSrgMapping(boolean newFormat) throws IOException
    {
        staticMethods = new StaticMethodsFile(staticMethodsFile);
        excFileData = new ExcFile(excFile);
        srgFileData = newFormat
                ? new TSrgFile(srgFile, excFileData, staticMethods)
                : new SrgFile(srgFile, excFileData, staticMethods);
    }

    private void loadCsvMapping() throws IOException
    {
        csvFieldData = new CsvFile(new File(mappingDir, "fields.csv"));
        csvMethodData = new CsvFile(new File(mappingDir, "methods.csv"));
        csvParamData = new ParamCsvFile(new File(mappingDir, "params.csv"));
    }

    private void linkSrgDataToCsvData()
    {
        for (Entry<String, MethodSrgData> methodData : srgFileData.srgMethodName2MethodData.entrySet())
        {
            if (!srgMethodData2CsvData.containsKey(methodData.getValue()) && csvMethodData.hasCsvDataForKey(methodData.getKey()))
            {
                srgMethodData2CsvData.put(methodData.getValue(), csvMethodData.getCsvDataForKey(methodData.getKey()));
            }
            else if (srgMethodData2CsvData.containsKey(methodData.getValue()))
                System.out.println("SRG method " + methodData.getKey() + " has multiple entries in CSV file!");
        }

        for (Entry<String, FieldSrgData> fieldData : srgFileData.srgFieldName2FieldData.entrySet())
        {
            if (!srgFieldData2CsvData.containsKey(fieldData.getValue()) && csvFieldData.hasCsvDataForKey(fieldData.getKey()))
            {
                srgFieldData2CsvData.put(fieldData.getValue(), csvFieldData.getCsvDataForKey(fieldData.getKey()));
            }
            else if (srgFieldData2CsvData.containsKey(fieldData.getValue()))
                System.out.println("SRG field " + fieldData.getKey() + " has multiple entries in CSV file!");
        }
    }

    private void linkExcDataToSetParamCsvData()
    {
        for (Entry<String, ExcData> excData : excFileData.srgMethodName2ExcData.entrySet())
        {
            if (!excData2MapParamCsvData.containsKey(excData.getValue()) && excData.getValue().getParameters().length > 0)
            {
                TreeMap<String, ParamCsvData> params = new TreeMap<String, ParamCsvData>();
                for (String srgName : excData.getValue().getParameters())
                    if (csvParamData.hasCsvDataForKey(srgName))
                        params.put(srgName, csvParamData.getCsvDataForKey(srgName));

                excData2MapParamCsvData.put(excData.getValue(), params);
            }
            else if (excData2MapParamCsvData.containsKey(excData.getValue()))
                System.out.println("EXC method param " + excData.getKey() + " has multiple entries in CSV file!");
        }
    }

    private CsvData processMemberDataEdit(MemberType type, Map<String, ? extends MemberSrgData> srg2MemberData,
            Map<? extends MemberSrgData, CsvData> memberData2CsvData,
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
                if (!mcpName.trim().equals(csvData.getMcpName()) || !comment.trim().equals(csvData.getComment()))
                {
                    csvData.setMcpName(mcpName.trim());
                    csvData.setComment(comment.trim());
                }
                else
                    return null;
            }
            else
            {
                csvData = new CsvData(srgName, mcpName.trim(), 2, comment.trim());
            }

            commandMap.put(srgName, McpBotCommand.getMcpBotCommand(type, isForced, csvData.getSrgName(), csvData.getMcpName(), csvData.getComment()));
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

    public boolean hasPendingCommands()
    {
        return !commandMap.isEmpty();
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
        for (ClassSrgData classData : srgFileData.srgClassName2ClassData.values())
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
                    CsvData csv = srgMethodData2CsvData.get(methodData);
                    if (methodData.contains(input) || csv != null && csv.contains(input))
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
                    CsvData csv = srgFieldData2CsvData.get(fieldData);
                    if (fieldData.contains(input) || csv != null && csv.contains(input))
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
        return new ClassModel(srgFileData.srgClassName2ClassData.values());
    }

    public TableModel getMethodModel(String srgPkgAndOwner)
    {
        ClassSrgData classData = srgFileData.srgClassName2ClassData.get(srgPkgAndOwner);
        return new MethodModel(srgFileData.class2MethodDataSet.get(classData));
    }

    public TableModel getParamModel(String srgMethodName)
    {
        if (excFileData.srgMethodName2ExcData.containsKey(srgMethodName))
            return new ParamModel(excFileData.srgMethodName2ExcData.get(srgMethodName));
        else
            return MappingGui.paramsDefaultModel;
    }

    public TableModel getFieldModel(String srgPkgAndOwner)
    {
        ClassSrgData classData = srgFileData.srgClassName2ClassData.get(srgPkgAndOwner);
        return new FieldModel(srgFileData.class2FieldDataSet.get(classData));
    }

    @SuppressWarnings("rawtypes")
    public class ClassModel extends AbstractTableModel
    {
        private static final long              serialVersionUID = 1L;
        public final String[]                  columnNames      = { "Pkg name", "SRG name", "Obf name" };
        private final Class[]                  columnTypes      = { String.class, String.class, String.class };
        private final boolean[]                isColumnEditable = { false, false, false };
        private final Object[][]               data;
        private final Collection<ClassSrgData> collectionRef;

        public ClassModel(Collection<ClassSrgData> map)
        {
            collectionRef = map;

            data = new Object[collectionRef.size()][columnNames.length];

            int i = 0;

            for (ClassSrgData classData : collectionRef)
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

            return "";
        }

        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            if (columnIndex < columnTypes.length && columnIndex >= 0)
                return columnTypes[columnIndex];

            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            if (columnIndex < isColumnEditable.length && columnIndex >= 0)
                return isColumnEditable[columnIndex];

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
        private static final long        serialVersionUID = 1L;
        private final String[]           columnNames      = { "MCP Name", "SRG Name", "Obf Name", "SRG Descriptor", "Comment" };
        private final Class[]            columnTypes      = { String.class, String.class, String.class, String.class, String.class };
        private final boolean[]          isColumnEditable = { true, false, false, false, true };
        private final Object[][]         data;
        private final Set<MethodSrgData> setRef;

        public MethodModel(Set<MethodSrgData> srgMethodSet)
        {
            setRef = srgMethodSet;

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

            return "";
        }

        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            if (columnIndex < columnTypes.length && columnIndex >= 0)
                return columnTypes[columnIndex];

            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            if (columnIndex < isColumnEditable.length && columnIndex >= 0)
                return isColumnEditable[columnIndex];

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

            if (mcpName.trim().isEmpty())
                return;

            CsvData result = processMemberDataEdit(MemberType.METHOD, srgFileData.srgMethodName2MethodData, srgMethodData2CsvData, srgName, mcpName, comment);

            if (result != null)
            {
                csvMethodData.updateCsvDataForKey(srgName, result);
                srgMethodData2CsvData.put(srgFileData.srgMethodName2MethodData.get(srgName), result);
                parentGui.setCsvFileEdited(true);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public class ParamModel extends AbstractTableModel
    {
        private static final long serialVersionUID = 1L;
        private final String[]    columnNames      = { "MCP Name", "SRG Name", "Type" };
        private final Class[]     columnTypes      = { String.class, String.class, String.class };
        private final boolean[]   isColumnEditable = { true, false, false };
        private final Object[][]  data;

        public ParamModel(ExcData excData)
        {
            data = new Object[excData.getParameters().length][columnNames.length];

            for (int i = 0; i < excData.getParameters().length; i++)
            {
                if (excData2MapParamCsvData.containsKey(excData) && excData2MapParamCsvData.get(excData).containsKey(excData.getParameters()[i]))
                    data[i][0] = excData2MapParamCsvData.get(excData).get(excData.getParameters()[i]).getMcpName();
                else
                    data[i][0] = "";

                data[i][1] = excData.getParameters()[i];
                data[i][2] = excData.getParamTypes()[i];
                //i++;
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

            return "";
        }

        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            if (columnIndex < columnTypes.length && columnIndex >= 0)
                return columnTypes[columnIndex];

            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            if (columnIndex < isColumnEditable.length && columnIndex >= 0)
                return isColumnEditable[columnIndex];

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

            String srgName = (String) data[rowIndex][1];
            String mcpName = (String) data[rowIndex][0];

            if (mcpName.trim().isEmpty())
                return;

            ExcData excData = excFileData.srgParamName2ExcData.get(srgName);
            ParamCsvData csvData = null;
            boolean isForced = csvParamData.hasCsvDataForKey(srgName);

            if (isForced)
            {
                csvData = csvParamData.getCsvDataForKey(srgName);
                if (!mcpName.trim().equals(csvData.getMcpName()))
                    csvData.setMcpName(mcpName.trim());
                else
                    return;
            }
            else
            {
                csvData = new ParamCsvData(srgName, mcpName, 2);
                excData2MapParamCsvData.get(excData).put(srgName, csvData);
            }

            commandMap.put(srgName, McpBotCommand.getMcpBotCommand(MemberType.PARAM, isForced, csvData.getSrgName(), csvData.getMcpName(), ""));

            csvParamData.updateCsvDataForKey(srgName, csvData);
            parentGui.setCsvFileEdited(true);
        }
    }

    @SuppressWarnings("rawtypes")
    public class FieldModel extends AbstractTableModel
    {
        private static final long       serialVersionUID = 1L;
        private final String[]          columnNames      = { "MCP Name", "SRG Name", "Obf Name", "Comment" };
        private final Class[]           columnTypes      = { String.class, String.class, String.class, String.class };
        private final boolean[]         isColumnEditable = { true, false, false, true };
        private final Object[][]        data;
        private final Set<FieldSrgData> setRef;

        public FieldModel(Set<FieldSrgData> srgFieldSet)
        {
            setRef = srgFieldSet;

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

            return "";
        }

        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            if (columnIndex < columnTypes.length && columnIndex >= 0)
                return columnTypes[columnIndex];

            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            if (columnIndex < isColumnEditable.length && columnIndex >= 0)
                return isColumnEditable[columnIndex];

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

            if (mcpName.trim().isEmpty())
                return;

            CsvData result = processMemberDataEdit(MemberType.FIELD, srgFileData.srgFieldName2FieldData, srgFieldData2CsvData, srgName, mcpName, comment);

            if (result != null)
            {
                csvFieldData.updateCsvDataForKey(srgName, result);
                srgFieldData2CsvData.put(srgFileData.srgFieldName2FieldData.get(srgName), result);
                parentGui.setCsvFileEdited(true);
            }
        }
    }
}
