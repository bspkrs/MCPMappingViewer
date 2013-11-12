package bspkrs.mmv.gui;

/*
 * This is mainly just a mock-up of the actual mapping GUI, so it is subject to change
 */

import immibis.bon.IProgressListener;
import immibis.bon.gui.Reference;
import immibis.bon.gui.Side;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import bspkrs.mmv.McpMappingLoader;

public class MappingGui extends JFrame
{
    private static final long             serialVersionUID = 1L;
    private final Preferences             prefs            = Preferences.userNodeForPackage(MappingGui.class);
    private JFrame                        frmMcpMappingViewer;
    private JComboBox<Side>               cmbSide;
    private JComboBox<String>             cmbMCPDirPath;
    private JProgressBar                  progressBar;
    private final static String           PREFS_KEY_MCPDIR = "mcpDir";
    private final static String           PREFS_KEY_SIDE   = "side";
    private final Reference<File>         mcpBrowseDir     = new Reference<File>();
    private JTable                        tblClasses;
    private JTable                        tblMethods;
    private JTable                        tblFields;
    private Thread                        curTask          = null;
    private Map<String, McpMappingLoader> mcpInstances     = new HashMap<>();
    private McpMappingLoader              currentLoader;
    
    private void savePrefs()
    {
        for (int i = 0; i < cmbMCPDirPath.getItemCount(); i++)
            prefs.put(PREFS_KEY_MCPDIR + i, cmbMCPDirPath.getItemAt(i));
        
        prefs.put(PREFS_KEY_SIDE, cmbSide.getSelectedItem().toString());
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
        
        cmbMCPDirPath.setSelectedIndex(0);
        
        Side side = Side.valueOf(prefs.get(PREFS_KEY_SIDE, Side.Universal.toString()));
        cmbSide.setSelectedItem(side);
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
    
    private static String getStackTraceMessage(Throwable e)
    {
        String s = "An error has occurred - give bspkrs this stack trace (which has been copied to the clipboard)\n";
        
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
        frmMcpMappingViewer.setBounds(100, 100, 866, 624);
        frmMcpMappingViewer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmMcpMappingViewer.getContentPane().setLayout(new BorderLayout(0, 0));
        
        JSplitPane splitMain = new JSplitPane();
        splitMain.setResizeWeight(0.3);
        splitMain.setContinuousLayout(true);
        splitMain.setMinimumSize(new Dimension(179, 80));
        splitMain.setPreferredSize(new Dimension(179, 80));
        splitMain.setOrientation(JSplitPane.VERTICAL_SPLIT);
        
        JScrollPane scrlpnClasses = new JScrollPane();
        scrlpnClasses.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        splitMain.setLeftComponent(scrlpnClasses);
        
        tblClasses = new JTable();
        scrlpnClasses.setViewportView(tblClasses);
        tblClasses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblClasses.setEnabled(false);
        tblClasses.setModel(new DefaultTableModel(
                new Object[][] {
                        { null, null, null },
                },
                new String[] {
                        "MCP name", "Obf name", "Pkg name"
                }
                )
                {
                    /**
                     * 
                     */
                    private static final long serialVersionUID = 1L;
                    @SuppressWarnings("rawtypes")
                    Class[]                   columnTypes      = new Class[] {
                                                                       String.class, String.class, String.class
                                                               };
                    
                    @SuppressWarnings({ "unchecked", "rawtypes" })
                    @Override
                    public Class getColumnClass(int columnIndex)
                    {
                        return columnTypes[columnIndex];
                    }
                });
        tblClasses.setFillsViewportHeight(true);
        tblClasses.setCellSelectionEnabled(true);
        frmMcpMappingViewer.getContentPane().add(splitMain, BorderLayout.CENTER);
        
        JSplitPane splitMembers = new JSplitPane();
        splitMembers.setResizeWeight(0.5);
        splitMembers.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitMain.setRightComponent(splitMembers);
        
        JScrollPane scrlpnMethods = new JScrollPane();
        scrlpnMethods.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        splitMembers.setLeftComponent(scrlpnMethods);
        
        tblMethods = new JTable();
        tblMethods.setCellSelectionEnabled(true);
        tblMethods.setFillsViewportHeight(true);
        tblMethods.setEnabled(false);
        tblMethods.setModel(new DefaultTableModel(
                new Object[][] {
                        { null, null, null, null, null, null },
                },
                new String[] {
                        "MCP Name", "SRG Name", "Obf. Name", "Return Type", "Descriptor", "Comment"
                }
                )
                {
                    /**
                     * 
                     */
                    private static final long serialVersionUID = 1L;
                    boolean[]                 columnEditables  = new boolean[] {
                                                                       false, false, false, false, false, false
                                                               };
                    
                    @Override
                    public boolean isCellEditable(int row, int column)
                    {
                        return columnEditables[column];
                    }
                });
        scrlpnMethods.setViewportView(tblMethods);
        
        JScrollPane scrlpnFields = new JScrollPane();
        scrlpnFields.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        splitMembers.setRightComponent(scrlpnFields);
        
        tblFields = new JTable();
        tblFields.setEnabled(false);
        tblFields.setModel(new DefaultTableModel(
                new Object[][] {
                        { null, null, null, null, null },
                },
                new String[] {
                        "MCP Name", "SRG Name", "Obf. Name", "Type", "Comment"
                }
                )
                {
                    /**
                     * 
                     */
                    private static final long serialVersionUID = 1L;
                    boolean[]                 columnEditables  = new boolean[] {
                                                                       false, false, false, false, false
                                                               };
                    
                    @Override
                    public boolean isCellEditable(int row, int column)
                    {
                        return columnEditables[column];
                    }
                });
        tblFields.setFillsViewportHeight(true);
        scrlpnFields.setViewportView(tblFields);
        
        JPanel pnlHeader = new JPanel();
        frmMcpMappingViewer.getContentPane().add(pnlHeader, BorderLayout.NORTH);
        pnlHeader.setLayout(new BorderLayout(0, 0));
        
        JPanel pnlControls = new JPanel();
        pnlHeader.add(pnlControls, BorderLayout.NORTH);
        pnlControls.setSize(new Dimension(0, 40));
        pnlControls.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        
        JLabel lblSide = new JLabel("Side");
        pnlControls.add(lblSide);
        
        cmbSide = new JComboBox<Side>();
        cmbSide.addItem(Side.Client);
        cmbSide.addItem(Side.Server);
        cmbSide.addItem(Side.Universal);
        pnlControls.add(cmbSide);
        
        cmbMCPDirPath = new JComboBox<String>(new DefaultComboBoxModel<String>());
        cmbMCPDirPath.addItemListener(new ComboItemChanged());
        
        JLabel lblMCPFolder = new JLabel("MCP folder");
        pnlControls.add(lblMCPFolder);
        cmbMCPDirPath.setEditable(true);
        pnlControls.add(cmbMCPDirPath);
        
        JButton btnBrowseFile = new JButton("Browse");
        btnBrowseFile.addActionListener(new BrowseActionListener(cmbMCPDirPath, true, btnBrowseFile, true, mcpBrowseDir));
        pnlControls.add(btnBrowseFile);
        
        JButton btnRefreshTables = new JButton("Load from conf");
        btnRefreshTables.addActionListener(new RefreshActionListener());
        pnlControls.add(btnRefreshTables);
        
        JPanel pnlProgress = new JPanel();
        pnlHeader.add(pnlProgress, BorderLayout.SOUTH);
        pnlProgress.setLayout(new BorderLayout(0, 0));
        
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("");
        progressBar.setForeground(UIManager.getColor("ProgressBar.foreground"));
        pnlProgress.add(progressBar);
        
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
            }
        }
    }
    
    class RefreshActionListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (curTask != null && curTask.isAlive())
                return;
            
            savePrefs();
            
            final Side side = (Side) cmbSide.getSelectedItem();
            
            final File mcpDir = mcpBrowseDir.val;
            final File confDir = new File(mcpDir, "conf");
            final String[] refPathList = side.referencePath.split(File.pathSeparator);
            
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
                        
                        String mcVer = McpMappingLoader.getMCVer(mcpDir);
                        
                        if (!mcpInstances.containsKey(mcpDir.getAbsolutePath() + " " + side))
                        {
                            progress.start(0, "Reading MCP configuration");
                            currentLoader = new McpMappingLoader(mcVer, side, mcpDir, progress);
                            mcpInstances.put(mcVer + " " + side, currentLoader);
                        }
                        else
                            currentLoader = mcpInstances.get(mcVer + " " + side);
                        
                        // TODO: actually populate the JTables with the data!
                        
                    }
                    catch (Exception e)
                    {
                        String s = getStackTraceMessage(e);
                        
                        /*if(!new File(confDir, side.nsside.srg_name).exists()) {
                            s = side.mcpside.srg_name+" not found in conf directory. \n";
                            switch(side) {
                            case Client:
                            case Server:
                                s += "If you're using Forge, set the side to Universal (1.4.6+) or Universal_old (1.4.5 and earlier)";
                                break;
                            case Universal:
                                s += "If you're not using Forge, set the side to Client or Server.\n";
                                s += "If you're using Forge on 1.4.5 or earlier, set the side to Universal_old.";
                                break;
                            case Universal_old:
                                s += "If you're not using Forge, set the side to Client or Server.\n";
                                break;
                            }
                        }*/
                        
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
                                    
                                    JOptionPane.showMessageDialog(MappingGui.this, "Done!", "MMV", JOptionPane.INFORMATION_MESSAGE);
                                }
                            });
                        }
                    }
                }
            };
            
            curTask.start();
        }
    }
}
