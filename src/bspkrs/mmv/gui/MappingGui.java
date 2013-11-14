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
package bspkrs.mmv.gui;

import immibis.bon.IProgressListener;
import immibis.bon.gui.Reference;
import immibis.bon.gui.Side;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.ScrollPaneConstants;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import bspkrs.mmv.McpMappingLoader;
import bspkrs.mmv.McpMappingLoader.CantLoadMCPMappingException;
import bspkrs.mmv.version.AppVersionChecker;

public class MappingGui extends JFrame
{
    public static final String            VERSION_NUMBER        = "0.2.0";
    private static final long             serialVersionUID      = 1L;
    private final Preferences             prefs                 = Preferences.userNodeForPackage(MappingGui.class);
    private JFrame                        frmMcpMappingViewer;
    private JButton                       btnRefreshTables;
    private JComboBox<Side>               cmbSide;
    private JComboBox<String>             cmbMCPDirPath;
    private JCheckBox                     chkForceRefresh;
    private JPanel                        pnlProgress;
    private JProgressBar                  progressBar;
    private JPanel                        pnlFilter;
    private JTextField                    edtFilter;
    private JButton                       btnSearch;
    private final static String           PREFS_KEY_MCPDIR      = "mcpDir";
    private final static String           PREFS_KEY_SIDE        = "side";
    private final static String           PREFS_KEY_CLASS_SORT  = "classSort";
    private final static String           PREFS_KEY_METHOD_SORT = "methodSort";
    private final static String           PREFS_KEY_FIELD_SORT  = "fieldSort";
    private List<RowSorter.SortKey>       classSort             = new ArrayList<RowSorter.SortKey>();
    private List<RowSorter.SortKey>       methodSort            = new ArrayList<RowSorter.SortKey>();
    private List<RowSorter.SortKey>       fieldSort             = new ArrayList<RowSorter.SortKey>();
    private final Reference<File>         mcpBrowseDir          = new Reference<File>();
    private JTable                        tblClasses;
    private JTable                        tblMethods;
    private JTable                        tblFields;
    private Thread                        curTask               = null;
    private Map<String, McpMappingLoader> mcpInstances          = new HashMap<>();
    private McpMappingLoader              currentLoader;
    private AppVersionChecker             versionChecker;
    private final String                  versionURL            = "http://dl.dropboxusercontent.com/u/20748481/Minecraft/MMV/MMV.version";
    private final String                  mcfTopic              = "http://www.minecraftforum.net/topic/2115030-";
    private DefaultTableModel             classesDefaultModel   = new DefaultTableModel(
                                                                        new Object[][] {
                                                                        {},
                                                                        },
                                                                        new String[] {
                                                                        "Pkg name", "SRG name", "Obf name", "Client Only"
                                                                        }
                                                                        )
                                                                        {
                                                                            private static final long serialVersionUID = 1L;
                                                                            boolean[]                 columnEditables  = new boolean[] {
                                                                                                                       false, false, false, false
                                                                                                                       };
                                                                            @SuppressWarnings("rawtypes")
                                                                            Class[]                   columnTypes      = new Class[] {
                                                                                                                       String.class, String.class, String.class, Boolean.class
                                                                                                                       };
                                                                            
                                                                            @SuppressWarnings({ "unchecked", "rawtypes" })
                                                                            @Override
                                                                            public Class getColumnClass(int columnIndex)
                                                                            {
                                                                                return columnTypes[columnIndex];
                                                                            }
                                                                            
                                                                            @Override
                                                                            public boolean isCellEditable(int row, int column)
                                                                            {
                                                                                return columnEditables[column];
                                                                            }
                                                                        };
    private DefaultTableModel             methodsDefaultModel   = new DefaultTableModel(
                                                                        new Object[][] {
                                                                        {},
                                                                        },
                                                                        new String[] {
                                                                        "MCP Name", "SRG Name", "Obf Name", "SRG Descriptor", "Comment", "Client Only"
                                                                        }
                                                                        )
                                                                        {
                                                                            private static final long serialVersionUID = 1L;
                                                                            boolean[]                 columnEditables  = new boolean[] {
                                                                                                                       false, false, false, false, false, false
                                                                                                                       };
                                                                            @SuppressWarnings("rawtypes")
                                                                            Class[]                   columnTypes      = new Class[] {
                                                                                                                       String.class, String.class, String.class, String.class, String.class, Boolean.class
                                                                                                                       };
                                                                            
                                                                            @SuppressWarnings({ "unchecked", "rawtypes" })
                                                                            @Override
                                                                            public Class getColumnClass(int columnIndex)
                                                                            {
                                                                                return columnTypes[columnIndex];
                                                                            }
                                                                            
                                                                            @Override
                                                                            public boolean isCellEditable(int row, int column)
                                                                            {
                                                                                return columnEditables[column];
                                                                            }
                                                                        };
    private DefaultTableModel             fieldsDefaultModel    = new DefaultTableModel(
                                                                        new Object[][] {
                                                                        {},
                                                                        },
                                                                        new String[] {
                                                                        "MCP Name", "SRG Name", "Obf Name", "Comment", "Client Only"
                                                                        }
                                                                        )
                                                                        {
                                                                            private static final long serialVersionUID = 1L;
                                                                            boolean[]                 columnEditables  = new boolean[] {
                                                                                                                       false, false, false, false, false
                                                                                                                       };
                                                                            @SuppressWarnings("rawtypes")
                                                                            Class[]                   columnTypes      = new Class[] {
                                                                                                                       String.class, String.class, String.class, String.class, Boolean.class
                                                                                                                       };
                                                                            
                                                                            @SuppressWarnings({ "unchecked", "rawtypes" })
                                                                            @Override
                                                                            public Class getColumnClass(int columnIndex)
                                                                            {
                                                                                return columnTypes[columnIndex];
                                                                            }
                                                                            
                                                                            @Override
                                                                            public boolean isCellEditable(int row, int column)
                                                                            {
                                                                                return columnEditables[column];
                                                                            }
                                                                        };
    
    private void savePrefs()
    {
        for (int i = 0; i < cmbMCPDirPath.getItemCount(); i++)
            prefs.put(PREFS_KEY_MCPDIR + i, cmbMCPDirPath.getItemAt(i));
        
        prefs.put(PREFS_KEY_SIDE, cmbSide.getSelectedItem().toString());
        
        if (tblClasses.getRowSorter().getSortKeys().size() > 0)
        {
            int i = tblClasses.getRowSorter().getSortKeys().get(0).getColumn() + 1;
            SortOrder order = tblClasses.getRowSorter().getSortKeys().get(0).getSortOrder();
            prefs.putInt(PREFS_KEY_CLASS_SORT, order == SortOrder.DESCENDING ? i * -1 : i);
        }
        else
            prefs.putInt(PREFS_KEY_CLASS_SORT, 1);
        
        if (tblMethods.getRowSorter().getSortKeys().size() > 0)
        {
            int i = tblMethods.getRowSorter().getSortKeys().get(0).getColumn() + 1;
            SortOrder order = tblMethods.getRowSorter().getSortKeys().get(0).getSortOrder();
            prefs.putInt(PREFS_KEY_METHOD_SORT, order == SortOrder.DESCENDING ? i * -1 : i);
        }
        else
            prefs.putInt(PREFS_KEY_METHOD_SORT, 1);
        
        if (tblFields.getRowSorter().getSortKeys().size() > 0)
        {
            int i = tblFields.getRowSorter().getSortKeys().get(0).getColumn() + 1;
            SortOrder order = tblFields.getRowSorter().getSortKeys().get(0).getSortOrder();
            prefs.putInt(PREFS_KEY_FIELD_SORT, order == SortOrder.DESCENDING ? i * -1 : i);
        }
        else
            prefs.putInt(PREFS_KEY_FIELD_SORT, 1);
    }
    
    private void loadPrefs()
    {
        for (int i = 0; i < 8; i++)
        {
            String item = prefs.get(PREFS_KEY_MCPDIR + i, "");
            if (!item.equals(""))
            {
                DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) cmbMCPDirPath.getModel();
                if (model.getIndexOf(item) == -1)
                    cmbMCPDirPath.addItem(item);
            }
        }
        
        if (cmbMCPDirPath.getItemCount() > 0)
        {
            btnRefreshTables.setEnabled(true);
            cmbMCPDirPath.setSelectedIndex(0);
        }
        else
            btnRefreshTables.setEnabled(false);
        
        Side side = Side.valueOf(prefs.get(PREFS_KEY_SIDE, Side.Universal.toString()));
        cmbSide.setSelectedItem(side);
        
        classSort.clear();
        methodSort.clear();
        fieldSort.clear();
        
        int i = prefs.getInt(PREFS_KEY_CLASS_SORT, 1);
        classSort.add(new RowSorter.SortKey(Math.abs(i) - 1, (i > 0 ? SortOrder.ASCENDING : SortOrder.DESCENDING)));
        tblClasses.getRowSorter().setSortKeys(classSort);
        
        i = prefs.getInt(PREFS_KEY_METHOD_SORT, 1);
        methodSort.add(new RowSorter.SortKey(Math.abs(i) - 1, (i > 0 ? SortOrder.ASCENDING : SortOrder.DESCENDING)));
        tblMethods.getRowSorter().setSortKeys(methodSort);
        
        i = prefs.getInt(PREFS_KEY_FIELD_SORT, 1);
        fieldSort.add(new RowSorter.SortKey(Math.abs(i) - 1, (i > 0 ? SortOrder.ASCENDING : SortOrder.DESCENDING)));
        tblFields.getRowSorter().setSortKeys(fieldSort);
    }
    
    private void checkForUpdates()
    {
        versionChecker = new AppVersionChecker("MCP Mapping Viewer", VERSION_NUMBER, versionURL, mcfTopic,
                new String[] { "{appName} {oldVer} is out of date! Visit {updateURL} to download the latest release ({newVer})." },
                new String[] { "{appName} {oldVer} is out of date! <br/><br/>Download the latest release ({newVer}) from <a href=\"{updateURL}\">{updateURL}</a>." }, 5000);
        if (!versionChecker.isCurrentVersion())
        {
            showHTMLDialog(MappingGui.this, versionChecker.getDialogMessage()[0], "An update is available", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Launch the application.
     */
    public static void main(String[] args)
    {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    MappingGui window = new MappingGui();
                    window.frmMcpMappingViewer.setVisible(true);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }
    
    private static String getPrintableStackTrace(Throwable e, Set<StackTraceElement> stopAt)
    {
        String s = e.toString();
        int numPrinted = 0;
        for (StackTraceElement ste : e.getStackTrace())
        {
            boolean stopHere = false;
            if (stopAt.contains(ste) && numPrinted > 0)
                stopHere = true;
            else
            {
                s += "\n    at " + ste.toString();
                numPrinted++;
                if (ste.getClassName().startsWith("javax.swing."))
                    stopHere = true;
            }
            
            if (stopHere)
            {
                int numHidden = e.getStackTrace().length - numPrinted;
                s += "\n    ... " + numHidden + " more";
                break;
            }
        }
        return s;
    }
    
    private static String getStackTraceMessage(String prefix, Throwable e)
    {
        String s = prefix;
        
        s += "\n" + getPrintableStackTrace(e, Collections.<StackTraceElement> emptySet());
        while (e.getCause() != null)
        {
            Set<StackTraceElement> stopAt = new HashSet<StackTraceElement>(Arrays.asList(e.getStackTrace()));
            e = e.getCause();
            s += "\nCaused by: " + getPrintableStackTrace(e, stopAt);
        }
        return s;
    }
    
    /**
     * Create the application.
     */
    public MappingGui()
    {
        initialize();
        checkForUpdates();
    }
    
    /**
     * Initialize the contents of the frame.
     */
    private void initialize()
    {
        {
            String mcpDirString = prefs.get(PREFS_KEY_MCPDIR + 0, "");
            
            if (!mcpDirString.equals(""))
                mcpBrowseDir.val = new File(mcpDirString);
            else
                mcpBrowseDir.val = new File(".");
        }
        
        frmMcpMappingViewer = new JFrame();
        frmMcpMappingViewer.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent arg0)
            {
                savePrefs();
            }
        });
        frmMcpMappingViewer.setTitle("MCP Mapping Viewer");
        frmMcpMappingViewer.setBounds(100, 100, 925, 621);
        frmMcpMappingViewer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmMcpMappingViewer.getContentPane().setLayout(new BorderLayout(0, 0));
        
        JSplitPane splitMain = new JSplitPane();
        splitMain.setDividerSize(3);
        splitMain.setResizeWeight(0.5);
        splitMain.setContinuousLayout(true);
        splitMain.setMinimumSize(new Dimension(179, 80));
        splitMain.setPreferredSize(new Dimension(179, 80));
        splitMain.setOrientation(JSplitPane.VERTICAL_SPLIT);
        
        JScrollPane scrlpnClasses = new JScrollPane();
        scrlpnClasses.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        splitMain.setLeftComponent(scrlpnClasses);
        
        tblClasses = new JTable();
        tblClasses.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        scrlpnClasses.setViewportView(tblClasses);
        tblClasses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblClasses.getSelectionModel().addListSelectionListener(new ClassTableSelectionListener(tblClasses));
        tblClasses.setAutoCreateRowSorter(true);
        tblClasses.setEnabled(false);
        tblClasses.setModel(classesDefaultModel);
        tblClasses.setFillsViewportHeight(true);
        tblClasses.setCellSelectionEnabled(true);
        frmMcpMappingViewer.getContentPane().add(splitMain, BorderLayout.CENTER);
        
        JSplitPane splitMembers = new JSplitPane();
        splitMembers.setDividerSize(3);
        splitMembers.setResizeWeight(0.5);
        splitMembers.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitMain.setRightComponent(splitMembers);
        
        JScrollPane scrlpnMethods = new JScrollPane();
        scrlpnMethods.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        splitMembers.setLeftComponent(scrlpnMethods);
        
        tblMethods = new JTable();
        tblMethods.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblMethods.setCellSelectionEnabled(true);
        tblMethods.setFillsViewportHeight(true);
        tblMethods.setAutoCreateRowSorter(true);
        tblMethods.setEnabled(false);
        tblMethods.setModel(methodsDefaultModel);
        scrlpnMethods.setViewportView(tblMethods);
        
        JScrollPane scrlpnFields = new JScrollPane();
        scrlpnFields.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        splitMembers.setRightComponent(scrlpnFields);
        
        tblFields = new JTable();
        tblFields.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblFields.setAutoCreateRowSorter(true);
        tblFields.setEnabled(false);
        tblFields.setModel(fieldsDefaultModel);
        tblFields.setFillsViewportHeight(true);
        scrlpnFields.setViewportView(tblFields);
        
        JPanel pnlHeader = new JPanel();
        frmMcpMappingViewer.getContentPane().add(pnlHeader, BorderLayout.NORTH);
        pnlHeader.setLayout(new BorderLayout(0, 0));
        
        JPanel pnlControls = new JPanel();
        pnlHeader.add(pnlControls, BorderLayout.NORTH);
        pnlControls.setSize(new Dimension(0, 40));
        pnlControls.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));
        
        JLabel lblSide = new JLabel("Side");
        pnlControls.add(lblSide);
        
        cmbSide = new JComboBox<Side>();
        cmbSide.setModel(new DefaultComboBoxModel(Side.values()));
        cmbSide.addItem(Side.Client);
        cmbSide.addItem(Side.Server);
        cmbSide.addItem(Side.Universal);
        pnlControls.add(cmbSide);
        
        cmbMCPDirPath = new JComboBox<String>(new DefaultComboBoxModel<String>());
        cmbMCPDirPath.setPreferredSize(new Dimension(320, 20));
        cmbMCPDirPath.addItemListener(new ComboItemChanged());
        
        JLabel lblMCPFolder = new JLabel("MCP folder");
        pnlControls.add(lblMCPFolder);
        cmbMCPDirPath.setEditable(true);
        pnlControls.add(cmbMCPDirPath);
        
        JButton btnBrowseFile = new JButton("Browse");
        btnBrowseFile.addActionListener(new BrowseActionListener(cmbMCPDirPath, true, btnBrowseFile, true, mcpBrowseDir));
        pnlControls.add(btnBrowseFile);
        
        btnRefreshTables = new JButton("Load from conf");
        btnRefreshTables.setEnabled(false);
        btnRefreshTables.addActionListener(new RefreshActionListener());
        pnlControls.add(btnRefreshTables);
        
        chkForceRefresh = new JCheckBox("Force reload");
        chkForceRefresh.setToolTipText("Force a reload from the MCP conf folder files instead of the session cache.");
        pnlControls.add(chkForceRefresh);
        
        pnlProgress = new JPanel();
        pnlProgress.setVisible(false);
        pnlHeader.add(pnlProgress, BorderLayout.SOUTH);
        pnlProgress.setLayout(new BorderLayout(0, 0));
        
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("");
        progressBar.setForeground(UIManager.getColor("ProgressBar.foreground"));
        pnlProgress.add(progressBar);
        
        pnlFilter = new JPanel();
        FlowLayout flowLayout = (FlowLayout) pnlFilter.getLayout();
        flowLayout.setVgap(2);
        flowLayout.setAlignment(FlowLayout.LEFT);
        pnlFilter.setVisible(true);
        pnlHeader.add(pnlFilter, BorderLayout.CENTER);
        
        JLabel lblFilter = new JLabel("Search");
        pnlFilter.add(lblFilter);
        
        edtFilter = new JTextField();
        edtFilter.addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusGained(FocusEvent e)
            {
                edtFilter.select(0, edtFilter.getText().length());
            }
        });
        edtFilter.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    btnSearch.doClick();
            }
        });
        pnlFilter.add(edtFilter);
        edtFilter.setColumns(40);
        
        btnSearch = new JButton("Go");
        btnSearch.setToolTipText("");
        btnSearch.addActionListener(new SearchActionListener());
        pnlFilter.add(btnSearch);
        edtFilter.setEnabled(false);
        btnSearch.setEnabled(false);
        
        JLabel lblSearchInfo = new JLabel("A note on search");
        lblSearchInfo.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                String message = "Search is global and returns a set of classes that contain a match for the input. \n" +
                        "Search is case sensitive!\n\nData elements searched on:\n" +
                        "Classes:\n    ~ Pkg Name\n    ~ SRG Name\n    ~ Obf Name\n" +
                        "Methods/Fields:\n    ~ SRG Name\n    ~ Obf Name\n    ~ MCP Name\n    ~ Comment";
                JOptionPane.showMessageDialog(MappingGui.this, message, "Search Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        lblSearchInfo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblSearchInfo.setForeground(Color.BLUE);
        pnlFilter.add(lblSearchInfo);
        
        JSeparator separator = new JSeparator();
        separator.setPreferredSize(new Dimension(1, 12));
        separator.setOrientation(SwingConstants.VERTICAL);
        pnlFilter.add(separator);
        
        JLabel lblAbout = new JLabel("About");
        pnlFilter.add(lblAbout);
        lblAbout.setForeground(Color.BLUE);
        lblAbout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblAbout.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                String message = "<center>MCP Mapping Viewer v" + VERSION_NUMBER + "<br/>" +
                        "Copyright (C) 2013 bspkrs<br/>" +
                        "Portions Copyright (C) 2013 Alex \"immibis\" Campbell<br/><br/>" +
                        "Author: bspkrs<br/>" +
                        "Credits: immibis (for <a href=\"https://github.com/immibis/bearded-octo-nemesis\">BON</a> code), " +
                        "Searge et al (for <a href=\"http://mcp.ocean-labs.de\">MCP</a>)<br/><br/>" +
                        "<a href=\"" + mcfTopic + "\">MCF Thread</a><br/>" +
                        "<a href=\"https://github.com/bspkrs/MCPMappingViewer\">Github Repo</a><br/>" +
                        "<a href=\"https://github.com/bspkrs/MCPMappingViewer/blob/master/change.log\">Change Log</a><br/>" +
                        "<a href=\"http://bspk.rs/MC/MCPMappingViewer/index.html\">Binary Downloads</a><br/>" +
                        "<a href=\"https://raw.github.com/bspkrs/MCPMappingViewer/master/LICENSE\">License</a><br/>" +
                        "<a href=\"https://twitter.com/bspkrs\">bspkrs on Twitter</a></center>";
                showHTMLDialog(MappingGui.this, message, "About MCP Mapping Viewer", JOptionPane.PLAIN_MESSAGE);
            }
        });
        
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                savePrefs();
            }
        });
        loadPrefs();
    }
    
    class ComboItemChanged implements ItemListener
    {
        @Override
        public void itemStateChanged(ItemEvent e)
        {
            if (e.getStateChange() == ItemEvent.SELECTED)
            {
                @SuppressWarnings("unchecked")
                JComboBox<String> cmb = (JComboBox<String>) e.getSource();
                String path = (String) cmb.getSelectedItem();
                if (!path.isEmpty())
                    mcpBrowseDir.val = new File(path);
                else
                    mcpBrowseDir.val = new File(".");
                
                btnRefreshTables.setEnabled(cmb.getItemCount() > 0);
            }
        }
    }
    
    class ClassTableSelectionListener implements ListSelectionListener
    {
        private final JTable table;
        
        public ClassTableSelectionListener(JTable table)
        {
            this.table = table;
        }
        
        @Override
        public void valueChanged(ListSelectionEvent e)
        {
            if (!e.getValueIsAdjusting() && !table.getModel().equals(classesDefaultModel))
            {
                int i = table.getSelectedRow();
                if (i > -1)
                {
                    savePrefs();
                    String pkg = (String) table.getModel().getValueAt(table.convertRowIndexToModel(i), 0);
                    String name = (String) table.getModel().getValueAt(table.convertRowIndexToModel(i), 1);
                    tblMethods.setModel(currentLoader.getMethodModel(pkg + "/" + name));
                    tblMethods.setEnabled(true);
                    tblFields.setModel(currentLoader.getFieldModel(pkg + "/" + name));
                    tblFields.setEnabled(true);
                    new TableColumnAdjuster(tblMethods).adjustColumns();
                    new TableColumnAdjuster(tblFields).adjustColumns();
                    loadPrefs();
                }
                else
                {
                    tblMethods.setModel(methodsDefaultModel);
                    tblMethods.setEnabled(false);
                    tblFields.setModel(fieldsDefaultModel);
                    tblFields.setEnabled(false);
                }
            }
        }
    }
    
    class SearchActionListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (curTask != null && curTask.isAlive())
                return;
            
            savePrefs();
            
            edtFilter.setEnabled(false);
            btnSearch.setEnabled(false);
            pnlProgress.setVisible(true);
            tblClasses.setModel(classesDefaultModel);
            tblClasses.setEnabled(false);
            tblMethods.setModel(methodsDefaultModel);
            tblMethods.setEnabled(false);
            tblFields.setModel(fieldsDefaultModel);
            tblFields.setEnabled(false);
            
            curTask = new Thread()
            {
                @Override
                public void run()
                {
                    boolean crashed = false;
                    
                    try
                    {
                        IProgressListener progress = new IProgressListener()
                        {
                            private String currentText;
                            
                            @Override
                            public void start(final int max, final String text)
                            {
                                currentText = text.equals("") ? " " : text;
                                SwingUtilities.invokeLater(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        progressBar.setString(currentText);
                                        if (max >= 0)
                                            progressBar.setMaximum(max);
                                        progressBar.setValue(0);
                                    }
                                });
                            }
                            
                            @Override
                            public void set(final int value)
                            {
                                SwingUtilities.invokeLater(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        progressBar.setValue(value);
                                    }
                                });
                            }
                            
                            @Override
                            public void setMax(final int max)
                            {
                                SwingUtilities.invokeLater(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        progressBar.setMaximum(max);
                                    }
                                });
                            }
                        };
                        
                        progress.start(0, "Searching MCP objects for input");
                        tblClasses.setModel(currentLoader.getSearchResults(edtFilter.getText(), progress));
                        tblClasses.setEnabled(true);
                        new TableColumnAdjuster(tblClasses).adjustColumns();
                        loadPrefs();
                    }
                    catch (Exception e)
                    {
                        String s = getStackTraceMessage("An error has occurred - give bspkrs this stack trace (which has been copied to the clipboard)\n", e);
                        
                        System.err.println(s);
                        
                        crashed = true;
                        
                        final String errMsg = s;
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                progressBar.setString(" ");
                                progressBar.setValue(0);
                                
                                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(errMsg), null);
                                JOptionPane.showMessageDialog(MappingGui.this, errMsg, "MMV - Error", JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    }
                    finally
                    {
                        if (!crashed)
                        {
                            SwingUtilities.invokeLater(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    progressBar.setString(" ");
                                    progressBar.setValue(0);
                                    edtFilter.setEnabled(true);
                                }
                            });
                        }
                        pnlProgress.setVisible(false);
                        edtFilter.setEnabled(true);
                        btnSearch.setEnabled(true);
                    }
                }
            };
            
            curTask.start();
        }
    }
    
    class RefreshActionListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (curTask != null && curTask.isAlive())
                return;
            
            final Side side = (Side) cmbSide.getSelectedItem();
            
            final File mcpDir = mcpBrowseDir.val;
            final File confDir = new File(mcpDir, "conf");
            
            String error = null;
            
            if (!mcpDir.isDirectory())
                error = "MCP folder not found (at " + mcpDir + ")";
            else if (!confDir.isDirectory())
                error = "'conf' folder not found in MCP folder (at " + confDir + ")";
            
            if (error != null)
            {
                JOptionPane.showMessageDialog(MappingGui.this, error, "MMV - Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (cmbMCPDirPath.getSelectedIndex() != 0)
            {
                String selItem = (String) cmbMCPDirPath.getSelectedItem();
                DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) cmbMCPDirPath.getModel();
                
                if (model.getIndexOf(selItem) != -1)
                    model.removeElement(selItem);
                
                cmbMCPDirPath.insertItemAt(selItem, 0);
                cmbMCPDirPath.setSelectedItem(selItem);
            }
            
            savePrefs();
            
            pnlFilter.setVisible(false);
            pnlProgress.setVisible(true);
            tblClasses.setModel(classesDefaultModel);
            tblClasses.setEnabled(false);
            tblMethods.setModel(methodsDefaultModel);
            tblMethods.setEnabled(false);
            tblFields.setModel(fieldsDefaultModel);
            tblFields.setEnabled(false);
            
            curTask = new Thread()
            {
                @Override
                public void run()
                {
                    boolean crashed = false;
                    
                    try
                    {
                        IProgressListener progress = new IProgressListener()
                        {
                            private String currentText;
                            
                            @Override
                            public void start(final int max, final String text)
                            {
                                currentText = text.equals("") ? " " : text;
                                SwingUtilities.invokeLater(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        progressBar.setString(currentText);
                                        if (max >= 0)
                                            progressBar.setMaximum(max);
                                        progressBar.setValue(0);
                                    }
                                });
                            }
                            
                            @Override
                            public void set(final int value)
                            {
                                SwingUtilities.invokeLater(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        progressBar.setValue(value);
                                    }
                                });
                            }
                            
                            @Override
                            public void setMax(final int max)
                            {
                                SwingUtilities.invokeLater(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        progressBar.setMaximum(max);
                                    }
                                });
                            }
                        };
                        
                        if (!mcpInstances.containsKey(mcpDir.getAbsolutePath() + " " + side) || chkForceRefresh.isSelected())
                        {
                            progress.start(0, "Reading MCP configuration");
                            currentLoader = new McpMappingLoader(side, mcpDir, progress);
                            mcpInstances.put(mcpDir.getAbsolutePath() + " " + side, currentLoader);
                            chkForceRefresh.setSelected(false);
                        }
                        else
                            currentLoader = mcpInstances.get(mcpDir.getAbsolutePath() + " " + side);
                        
                        tblClasses.setModel(currentLoader.getClassModel());
                        tblClasses.setEnabled(true);
                        new TableColumnAdjuster(tblClasses).adjustColumns();
                        loadPrefs();
                    }
                    catch (CantLoadMCPMappingException e)
                    {
                        String s = getStackTraceMessage("", e);
                        
                        System.err.println(s);
                        
                        crashed = true;
                        
                        final String errMsg = s;
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                progressBar.setString(" ");
                                progressBar.setValue(0);
                                
                                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(errMsg), null);
                                JOptionPane.showMessageDialog(MappingGui.this, errMsg, "MMV - Error", JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    }
                    catch (Exception e)
                    {
                        String s = getStackTraceMessage("An error has occurred - give bspkrs this stack trace (which has been copied to the clipboard)\n", e);
                        
                        System.err.println(s);
                        
                        crashed = true;
                        
                        final String errMsg = s;
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                progressBar.setString(" ");
                                progressBar.setValue(0);
                                
                                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(errMsg), null);
                                JOptionPane.showMessageDialog(MappingGui.this, errMsg, "MMV - Error", JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    }
                    finally
                    {
                        if (!crashed)
                        {
                            SwingUtilities.invokeLater(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    progressBar.setString(" ");
                                    progressBar.setValue(0);
                                    edtFilter.setEnabled(true);
                                }
                            });
                        }
                        pnlProgress.setVisible(false);
                        pnlFilter.setVisible(true);
                        edtFilter.setEnabled(true);
                        btnSearch.setEnabled(true);
                    }
                }
            };
            
            curTask.start();
        }
    }
    
    public static void showHTMLDialog(Component parentComponent,
            Object message, String title, int messageType)
    {
        JLabel label = new JLabel();
        Font font = label.getFont();
        
        StringBuffer style = new StringBuffer("font-family:" + font.getFamily() + ";");
        style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
        style.append("font-size:" + font.getSize() + "pt;");
        
        JEditorPane ep = new JEditorPane("text/html", "<html><body style=\"" + style + "\">" + message.toString() + "</body></html>");
        
        ep.addHyperlinkListener(new HyperlinkListener()
        {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e)
            {
                if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
                    try
                    {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    }
                    catch (Throwable ignore)
                    {}
            }
        });
        ep.setEditable(false);
        ep.setBackground(label.getBackground());
        JOptionPane.showMessageDialog(parentComponent, ep, title, messageType);
    }
}
