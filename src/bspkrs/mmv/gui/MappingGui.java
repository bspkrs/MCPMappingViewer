package bspkrs.mmv.gui;

/*
 * This is mainly just a mock-up of the actual mapping GUI, so it is subject to change
 */

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.AbstractListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;

public class MappingGui
{
    
    private JFrame frmMcpMappingViewer;
    
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
        frmMcpMappingViewer = new JFrame();
        frmMcpMappingViewer.setTitle("MCP Mapping Viewer");
        frmMcpMappingViewer.setBounds(100, 100, 450, 300);
        frmMcpMappingViewer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] { 0, 0 };
        gridBagLayout.rowHeights = new int[] { 0, 0 };
        gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
        gridBagLayout.rowWeights = new double[] { 1.0, 1.0 };
        frmMcpMappingViewer.getContentPane().setLayout(gridBagLayout);
        
        JPanel panel = new JPanel();
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.insets = new Insets(0, 0, 5, 0);
        gbc_panel.fill = GridBagConstraints.BOTH;
        gbc_panel.gridx = 0;
        gbc_panel.gridy = 0;
        frmMcpMappingViewer.getContentPane().add(panel, gbc_panel);
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[] { 0 };
        gbl_panel.rowHeights = new int[] { 0, 0 };
        gbl_panel.columnWeights = new double[] { 1.0 };
        gbl_panel.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
        panel.setLayout(gbl_panel);
        
        JSplitPane splitPane = new JSplitPane();
        GridBagConstraints gbc_splitPane = new GridBagConstraints();
        gbc_splitPane.fill = GridBagConstraints.BOTH;
        gbc_splitPane.gridx = 0;
        gbc_splitPane.gridy = 0;
        panel.add(splitPane, gbc_splitPane);
        
        JSplitPane splitPane_1 = new JSplitPane();
        splitPane_1.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane.setRightComponent(splitPane_1);
        
        JList listMCPMethods = new JList();
        listMCPMethods.setModel(new AbstractListModel()
        {
            String[] values = new String[] { "setBlock", "setBlockMetadata", "removeBlock" };
            
            @Override
            public int getSize()
            {
                return values.length;
            }
            
            @Override
            public Object getElementAt(int index)
            {
                return values[index];
            }
        });
        listMCPMethods.setSelectedIndex(0);
        splitPane_1.setLeftComponent(listMCPMethods);
        
        JList listMCPFields = new JList();
        listMCPFields.setModel(new AbstractListModel()
        {
            String[] values = new String[] { "blockList", "blockID", "metadata" };
            
            @Override
            public int getSize()
            {
                return values.length;
            }
            
            @Override
            public Object getElementAt(int index)
            {
                return values[index];
            }
        });
        listMCPFields.setSelectedIndex(0);
        splitPane_1.setRightComponent(listMCPFields);
        
        JList listMCPClasses = new JList();
        listMCPClasses.setModel(new AbstractListModel()
        {
            String[] values = new String[] { "Block", "Item", "World", "ItemInWorldManager" };
            
            @Override
            public int getSize()
            {
                return values.length;
            }
            
            @Override
            public Object getElementAt(int index)
            {
                return values[index];
            }
        });
        listMCPClasses.setSelectedIndex(0);
        splitPane.setLeftComponent(listMCPClasses);
        
        JPanel panel_1 = new JPanel();
        GridBagConstraints gbc_panel_1 = new GridBagConstraints();
        gbc_panel_1.insets = new Insets(0, 0, 5, 0);
        gbc_panel_1.fill = GridBagConstraints.BOTH;
        gbc_panel_1.gridx = 0;
        gbc_panel_1.gridy = 1;
        frmMcpMappingViewer.getContentPane().add(panel_1, gbc_panel_1);
        GridBagLayout gbl_panel_1 = new GridBagLayout();
        gbl_panel_1.columnWidths = new int[] { 0 };
        gbl_panel_1.rowHeights = new int[] { 0 };
        gbl_panel_1.columnWeights = new double[] { 1.0 };
        gbl_panel_1.rowWeights = new double[] { 1.0 };
        panel_1.setLayout(gbl_panel_1);
        
        JSplitPane splitPane_2 = new JSplitPane();
        GridBagConstraints gbc_splitPane_2 = new GridBagConstraints();
        gbc_splitPane_2.fill = GridBagConstraints.BOTH;
        gbc_splitPane_2.gridx = 0;
        gbc_splitPane_2.gridy = 0;
        panel_1.add(splitPane_2, gbc_splitPane_2);
        
        JSplitPane splitPane_3 = new JSplitPane();
        splitPane_3.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane_2.setRightComponent(splitPane_3);
        
        JList listObfMethods = new JList();
        listObfMethods.setModel(new AbstractListModel()
        {
            String[] values = new String[] { "a", "b", "e", "d" };
            
            @Override
            public int getSize()
            {
                return values.length;
            }
            
            @Override
            public Object getElementAt(int index)
            {
                return values[index];
            }
        });
        listObfMethods.setSelectedIndex(0);
        splitPane_3.setLeftComponent(listObfMethods);
        
        JList listObfFields = new JList();
        listObfFields.setModel(new AbstractListModel()
        {
            String[] values = new String[] { "a", "b", "e", "h" };
            
            @Override
            public int getSize()
            {
                return values.length;
            }
            
            @Override
            public Object getElementAt(int index)
            {
                return values[index];
            }
        });
        listObfFields.setSelectedIndex(0);
        listObfFields.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        splitPane_3.setRightComponent(listObfFields);
        
        JList listObfClasses = new JList();
        listObfClasses.setModel(new AbstractListModel()
        {
            String[] values = new String[] { "aja", "qr", "lr", "asdf" };
            
            @Override
            public int getSize()
            {
                return values.length;
            }
            
            @Override
            public Object getElementAt(int index)
            {
                return values[index];
            }
        });
        listObfClasses.setSelectedIndex(0);
        splitPane_2.setLeftComponent(listObfClasses);
    }
    
}
