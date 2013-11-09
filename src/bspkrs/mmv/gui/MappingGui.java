package bspkrs.mmv.gui;

/*
 * This is mainly just a mock-up of the actual mapping GUI, so it is subject to change
 */

import immibis.bon.gui.Reference;
import immibis.bon.gui.Side;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;

public class MappingGui extends JFrame
{
    private static final long     serialVersionUID = 1L;
    private final Preferences     prefs            = Preferences.userNodeForPackage(MappingGui.class);
    private JFrame                frmMcpMappingViewer;
    private JComboBox<Side>       cmbSide;
    private JComboBox<String>     cmbMCPDirPath;
    private final static String   PREFS_KEY_MCPDIR = "mcpDir";
    private final static String   PREFS_KEY_SIDE   = "side";
    private final Reference<File> mcpBrowseDir     = new Reference<File>();
    private JTable                tblClasses;
    private JTable                tblMethods;
    private JTable                tblFields;
    private Thread                curTask          = null;
    private static MappingGui     instance;
    
    private void savePrefs()
    {
        for (int i = 0; i < instance.cmbMCPDirPath.getItemCount(); i++)
            instance.prefs.put(PREFS_KEY_MCPDIR + i, instance.cmbMCPDirPath.getItemAt(i));
        
        instance.prefs.put(PREFS_KEY_SIDE, instance.cmbSide.getSelectedItem().toString());
    }
    
    private void loadPrefs()
    {
        for (int i = 0; i < 8; i++)
        {
            String item = instance.prefs.get(PREFS_KEY_MCPDIR + i, "");
            if (!item.equals(""))
            {
                DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) instance.cmbMCPDirPath.getModel();
                if (model.getIndexOf(item) == -1)
                    instance.cmbMCPDirPath.addItem(item);
            }
        }
        
        instance.cmbMCPDirPath.setSelectedIndex(0);
        
        Side side = Side.valueOf(instance.prefs.get(PREFS_KEY_SIDE, Side.Universal.toString()));
        instance.cmbSide.setSelectedItem(side);
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
                    new MappingGui();
                    instance.frmMcpMappingViewer.setVisible(true);
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
        instance = this;
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
        
        instance.frmMcpMappingViewer = new JFrame();
        instance.frmMcpMappingViewer.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent arg0)
            {
                savePrefs();
            }
        });
        instance.frmMcpMappingViewer.setTitle("MCP Mapping Viewer");
        instance.frmMcpMappingViewer.setBounds(100, 100, 1051, 618);
        instance.frmMcpMappingViewer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel pnlHeader = new JPanel();
        pnlHeader.setSize(new Dimension(0, 20));
        pnlHeader.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        JLabel lblSide = new JLabel("Side");
        pnlHeader.add(lblSide);
        
        instance.cmbSide = new JComboBox<Side>();
        instance.cmbSide.addItem(Side.Client);
        instance.cmbSide.addItem(Side.Server);
        instance.cmbSide.addItem(Side.Universal);
        pnlHeader.add(instance.cmbSide);
        
        JLabel lblMCPFolder = new JLabel("MCP folder");
        pnlHeader.add(lblMCPFolder);
        
        instance.cmbMCPDirPath = new JComboBox<String>(new DefaultComboBoxModel<String>());
        instance.cmbMCPDirPath.addItemListener(new ComboItemChanged());
        instance.cmbMCPDirPath.setEditable(true);
        pnlHeader.add(instance.cmbMCPDirPath);
        
        JButton btnBrowseFile = new JButton("Browse");
        btnBrowseFile.addActionListener(new BrowseActionListener(instance.cmbMCPDirPath, true, btnBrowseFile, true, instance.mcpBrowseDir));
        instance.frmMcpMappingViewer.getContentPane().setLayout(new BorderLayout(0, 0));
        
        JSplitPane splitMain = new JSplitPane();
        splitMain.setResizeWeight(0.3);
        splitMain.setContinuousLayout(true);
        splitMain.setMinimumSize(new Dimension(179, 80));
        splitMain.setPreferredSize(new Dimension(179, 80));
        splitMain.setOrientation(JSplitPane.VERTICAL_SPLIT);
        
        JScrollPane scrlpnClasses = new JScrollPane();
        scrlpnClasses.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        splitMain.setLeftComponent(scrlpnClasses);
        
        instance.tblClasses = new JTable();
        scrlpnClasses.setViewportView(instance.tblClasses);
        instance.tblClasses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        instance.tblClasses.setEnabled(false);
        instance.tblClasses.setModel(new DefaultTableModel(
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
        instance.tblClasses.setFillsViewportHeight(true);
        instance.tblClasses.setCellSelectionEnabled(true);
        instance.frmMcpMappingViewer.getContentPane().add(splitMain, BorderLayout.CENTER);
        
        JSplitPane splitMembers = new JSplitPane();
        splitMembers.setResizeWeight(0.5);
        splitMembers.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitMain.setRightComponent(splitMembers);
        
        JScrollPane scrlpnMethods = new JScrollPane();
        scrlpnMethods.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        splitMembers.setLeftComponent(scrlpnMethods);
        
        instance.tblMethods = new JTable();
        instance.tblMethods.setCellSelectionEnabled(true);
        instance.tblMethods.setFillsViewportHeight(true);
        instance.tblMethods.setEnabled(false);
        instance.tblMethods.setModel(new DefaultTableModel(
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
        scrlpnMethods.setViewportView(instance.tblMethods);
        
        JScrollPane scrlpnFields = new JScrollPane();
        scrlpnFields.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        splitMembers.setRightComponent(scrlpnFields);
        
        instance.tblFields = new JTable();
        instance.tblFields.setEnabled(false);
        instance.tblFields.setModel(new DefaultTableModel(
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
        instance.tblFields.setFillsViewportHeight(true);
        scrlpnFields.setViewportView(instance.tblFields);
        pnlHeader.add(btnBrowseFile);
        instance.frmMcpMappingViewer.getContentPane().add(pnlHeader, BorderLayout.NORTH);
        
        JButton btnRefreshTables = new JButton("Load from conf");
        btnRefreshTables.addActionListener(new RefreshActionListener());
        pnlHeader.add(btnRefreshTables);
        
        instance.addWindowListener(new WindowAdapter()
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
                    instance.mcpBrowseDir.val = new File(path);
                else
                    instance.mcpBrowseDir.val = new File(".");
            }
        }
    }
    
    class RefreshActionListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            
            if (instance.curTask != null && instance.curTask.isAlive())
                return;
            
            savePrefs();
            
            final Side side = (Side) instance.cmbSide.getSelectedItem();
            
            final File mcpDir = instance.mcpBrowseDir.val;
            final File confDir = new File(mcpDir, "conf");
            final String[] refPathList = side.referencePath.split(File.pathSeparator);
            
            String error = null;
            
            if (!mcpDir.isDirectory())
                error = "MCP folder not found (at " + mcpDir + ")";
            else if (!confDir.isDirectory())
                error = "'conf' folder not found in MCP folder (at " + confDir + ")";
            
            if (error != null)
            {
                JOptionPane.showMessageDialog(instance, error, "MMV - Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            curTask = new Thread()
            {
                @Override
                public void run()
                {
                    //              boolean crashed = false;
                    //              
                    //              try {
                    //                  
                    //                  IProgressListener progress = new IProgressListener() {
                    //                      private String currentText;
                    //                      
                    //                      @Override
                    //                      public void start(final int max, final String text) {
                    //                          currentText = text.equals("") ? " " : text;
                    //                          SwingUtilities.invokeLater(new Runnable() {
                    //                              public void run() {
                    //                                  progressLabel.setText(currentText);
                    //                                  if(max >= 0)
                    //                                      progressBar.setMaximum(max);
                    //                                  progressBar.setValue(0);
                    //                              }
                    //                          });
                    //                      }
                    //                      
                    //                      @Override
                    //                      public void set(final int value) {
                    //                          SwingUtilities.invokeLater(new Runnable() {
                    //                              public void run() {
                    //                                  progressBar.setValue(value);
                    //                              }
                    //                          });
                    //                      }
                    //                      
                    //                      @Override
                    //                      public void setMax(final int max) {
                    //                          SwingUtilities.invokeLater(new Runnable() {
                    //                              public void run() {
                    //                                  progressBar.setMaximum(max);
                    //                              }
                    //                          });
                    //                      }
                    //                  };
                    //                  
                    //                  
                    //                  
                    //                  File inputFile = new File(inputField.getText());
                    //                  File outputFile = new File(outputField.getText());
                    //                  
                    //                  String mcVer = MappingLoader_MCP.getMCVer(mcpDir);
                    //                  
                    //                  NameSet refNS = new NameSet(NameSet.Type.MCP, side.nsside, mcVer);
                    //                  Map<String, ClassCollection> refCCList = new HashMap<>();
                    //                  
                    //                  for(String s : refPathList) {
                    //                      File refPathFile = new File(mcpDir, s);
                    //                      
                    //                      progress.start(0, "Reading "+s);
                    //                      refCCList.put(s, ClassCollectionFactory.loadClassCollection(refNS, refPathFile, progress));
                    //                      
                    //                      //progress.start(0, "Remapping "+s);
                    //                      //refs.add(Remapper.remap(mcpRefCC, inputNS, Collections.<ClassCollection>emptyList(), progress));
                    //                  }
                    //                  
                    //                  NameSet inputNS = new NameSet(NameSet.Type.OBF, side.nsside, mcVer);
                    //                  
                    //                  progress.start(0, "Reading "+inputFile.getName());
                    //                  ClassCollection inputCC = ClassCollectionFactory.loadClassCollection(inputNS, inputFile, progress);
                    //                  
                    //                  progress.start(0, "Reading MCP configuration");
                    //                  MappingFactory.registerMCPInstance(mcVer, side.nsside, mcpDir, progress);
                    //                  
                    //                  
                    //                  
                    //                  /*                       MCP reference
                    //                   *                       |           |
                    //                   *                       |           |
                    //                   *                       |           |
                    //                   *                       V           V
                    //                   *             OBF reference       SRG reference
                    //                   *                 |                     |
                    //                   *                 |                     |
                    //                   *                 V                     V
                    //                   * OBF input -----------> SRG input -----------> MCP input (output file)
                    //                   */
                    //                  
                    //                  
                    //                  
                    //                  // remap to obf names from searge names, then searge names to MCP names, in two steps
                    //                  // the first will be a no-op if the mod uses searge names already
                    //                  for(NameSet.Type outputType : new NameSet.Type[] {NameSet.Type.SRG, NameSet.Type.MCP}) {
                    //                      NameSet outputNS = new NameSet(outputType, side.nsside, mcVer);
                    //                      
                    //                      List<ClassCollection> remappedRefs = new ArrayList<>();
                    //                      for(Map.Entry<String, ClassCollection> e : refCCList.entrySet()) {
                    //                          progress.start(0, "Remapping "+e.getKey()+" to "+outputType+" names");
                    //                          remappedRefs.add(Remapper.remap(e.getValue(), inputCC.getNameSet(), Collections.<ClassCollection>emptyList(), progress));
                    //                      }
                    //                      
                    //                      progress.start(0, "Remapping "+inputFile.getName()+" to "+outputType+" names");
                    //                      inputCC = Remapper.remap(inputCC, outputNS, remappedRefs, progress);
                    //                  }
                    //                  
                    //                  progress.start(0, "Writing "+outputFile.getName());
                    //                  JarWriter.write(outputFile, inputCC, progress);
                    //                  
                    //              } catch(Exception e) {
                    //                  String s = getStackTraceMessage(e);
                    //                  
                    //                  /*if(!new File(confDir, side.nsside.srg_name).exists()) {
                    //                      s = side.mcpside.srg_name+" not found in conf directory. \n";
                    //                      switch(side) {
                    //                      case Client:
                    //                      case Server:
                    //                          s += "If you're using Forge, set the side to Universal (1.4.6+) or Universal_old (1.4.5 and earlier)";
                    //                          break;
                    //                      case Universal:
                    //                          s += "If you're not using Forge, set the side to Client or Server.\n";
                    //                          s += "If you're using Forge on 1.4.5 or earlier, set the side to Universal_old.";
                    //                          break;
                    //                      case Universal_old:
                    //                          s += "If you're not using Forge, set the side to Client or Server.\n";
                    //                          break;
                    //                      }
                    //                  }*/
                    //                  
                    //                  System.err.println(s);
                    //                  
                    //                  crashed = true;
                    //                  
                    //                  final String errMsg = s;
                    //                  SwingUtilities.invokeLater(new Runnable() {
                    //                      @Override
                    //                      public void run() {
                    //                          progressLabel.setText(" ");
                    //                          progressBar.setValue(0);
                    //                          
                    //                          Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(errMsg), null);
                    //                          JOptionPane.showMessageDialog(GuiMain.this, errMsg, "BON - Error", JOptionPane.ERROR_MESSAGE);
                    //                      }
                    //                  });
                    //              } finally {
                    //                  if(!crashed) {
                    //                      SwingUtilities.invokeLater(new Runnable() {
                    //                          @Override
                    //                          public void run() {
                    //                              progressLabel.setText(" ");
                    //                              progressBar.setValue(0);
                    //                              
                    //                              JOptionPane.showMessageDialog(GuiMain.this, "Done!", "BON", JOptionPane.INFORMATION_MESSAGE);
                    //                          }
                    //                      });
                    //                  }
                    //              }
                }
            };
            
            instance.curTask.start();
        }
    }
}
