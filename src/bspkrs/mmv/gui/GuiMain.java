package bspkrs.mmv.gui;

import immibis.bon.gui.Reference;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class GuiMain extends JFrame
{
    private static final long     serialVersionUID = 1;
    
    // The Java Preferences API is used to store the last directory the user was browsing
    // for the input/output files (PREFS_KEY_BROWSEDIR)
    // and the selected MCP directory (PREFS_KEY_MCPDIR).
    // Prefs are saved when the user clicks "Go" or closes the window.
    private final Preferences     prefs            = Preferences.userNodeForPackage(GuiMain.class);
    private final static String   PREFS_KEY_MCPDIR = "mcpDir";
    private JButton               goButton;
    private JProgressBar          progressBar;
    private JLabel                progressLabel;
    
    private Thread                curTask          = null;
    
    // the last directory the user was browsing, for the MCP directory
    private final Reference<File> mcpBrowseDir     = new Reference<File>();
    
    private void savePrefs()
    {
        //prefs.put(PREFS_KEY_MCPDIR, mcpField.getText());
    }
    
    synchronized void goButtonPressed()
    {
        
        if (curTask != null && curTask.isAlive())
            return;
        
        savePrefs();
        
        //final Operation op = (Operation)opSelect.getSelectedItem();
        //final Side side = (Side) sideSelect.getSelectedItem();
        
        //        final File mcpDir = new File(mcpField.getText());
        //        final File confDir = new File(mcpDir, "conf");
        //final String[] refPathList = side.referencePath.split(File.pathSeparator);
        
        String error = null;
        
        //        if (!mcpDir.isDirectory())
        //            error = "MCP folder not found (at " + mcpDir + ")";
        //        else if (!confDir.isDirectory())
        //            error = "'conf' folder not found in MCP folder (at " + confDir + ")";
        
        if (error != null)
        {
            JOptionPane.showMessageDialog(this, error, "MMV - Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        progressBar.setValue(0);
        
        curTask = new Thread()
        {
            @Override
            public void run()
            {
                //				boolean crashed = false;
                //				
                //				try {
                //					
                //					IProgressListener progress = new IProgressListener() {
                //						private String currentText;
                //						
                //						@Override
                //						public void start(final int max, final String text) {
                //							currentText = text.equals("") ? " " : text;
                //							SwingUtilities.invokeLater(new Runnable() {
                //								public void run() {
                //									progressLabel.setText(currentText);
                //									if(max >= 0)
                //										progressBar.setMaximum(max);
                //									progressBar.setValue(0);
                //								}
                //							});
                //						}
                //						
                //						@Override
                //						public void set(final int value) {
                //							SwingUtilities.invokeLater(new Runnable() {
                //								public void run() {
                //									progressBar.setValue(value);
                //								}
                //							});
                //						}
                //						
                //						@Override
                //						public void setMax(final int max) {
                //							SwingUtilities.invokeLater(new Runnable() {
                //								public void run() {
                //									progressBar.setMaximum(max);
                //								}
                //							});
                //						}
                //					};
                //					
                //					
                //					
                //					File inputFile = new File(inputField.getText());
                //					File outputFile = new File(outputField.getText());
                //					
                //					String mcVer = MappingLoader_MCP.getMCVer(mcpDir);
                //					
                //					NameSet refNS = new NameSet(NameSet.Type.MCP, side.nsside, mcVer);
                //					Map<String, ClassCollection> refCCList = new HashMap<>();
                //					
                //					for(String s : refPathList) {
                //						File refPathFile = new File(mcpDir, s);
                //						
                //						progress.start(0, "Reading "+s);
                //						refCCList.put(s, ClassCollectionFactory.loadClassCollection(refNS, refPathFile, progress));
                //						
                //						//progress.start(0, "Remapping "+s);
                //						//refs.add(Remapper.remap(mcpRefCC, inputNS, Collections.<ClassCollection>emptyList(), progress));
                //					}
                //					
                //					NameSet inputNS = new NameSet(NameSet.Type.OBF, side.nsside, mcVer);
                //					
                //					progress.start(0, "Reading "+inputFile.getName());
                //					ClassCollection inputCC = ClassCollectionFactory.loadClassCollection(inputNS, inputFile, progress);
                //					
                //					progress.start(0, "Reading MCP configuration");
                //					MappingFactory.registerMCPInstance(mcVer, side.nsside, mcpDir, progress);
                //					
                //					
                //					
                //					/*                       MCP reference
                //					 *                       |           |
                //					 *                       |           |
                //					 *                       |           |
                //					 *                       V           V
                //					 *             OBF reference       SRG reference
                //					 *                 |                     |
                //					 *                 |                     |
                //					 *                 V                     V
                //					 * OBF input -----------> SRG input -----------> MCP input (output file)
                //					 */
                //					
                //					
                //					
                //					// remap to obf names from searge names, then searge names to MCP names, in two steps
                //					// the first will be a no-op if the mod uses searge names already
                //					for(NameSet.Type outputType : new NameSet.Type[] {NameSet.Type.SRG, NameSet.Type.MCP}) {
                //						NameSet outputNS = new NameSet(outputType, side.nsside, mcVer);
                //						
                //						List<ClassCollection> remappedRefs = new ArrayList<>();
                //						for(Map.Entry<String, ClassCollection> e : refCCList.entrySet()) {
                //							progress.start(0, "Remapping "+e.getKey()+" to "+outputType+" names");
                //							remappedRefs.add(Remapper.remap(e.getValue(), inputCC.getNameSet(), Collections.<ClassCollection>emptyList(), progress));
                //						}
                //						
                //						progress.start(0, "Remapping "+inputFile.getName()+" to "+outputType+" names");
                //						inputCC = Remapper.remap(inputCC, outputNS, remappedRefs, progress);
                //					}
                //					
                //					progress.start(0, "Writing "+outputFile.getName());
                //					JarWriter.write(outputFile, inputCC, progress);
                //					
                //				} catch(Exception e) {
                //					String s = getStackTraceMessage(e);
                //					
                //					/*if(!new File(confDir, side.nsside.srg_name).exists()) {
                //						s = side.mcpside.srg_name+" not found in conf directory. \n";
                //						switch(side) {
                //						case Client:
                //						case Server:
                //							s += "If you're using Forge, set the side to Universal (1.4.6+) or Universal_old (1.4.5 and earlier)";
                //							break;
                //						case Universal:
                //							s += "If you're not using Forge, set the side to Client or Server.\n";
                //							s += "If you're using Forge on 1.4.5 or earlier, set the side to Universal_old.";
                //							break;
                //						case Universal_old:
                //							s += "If you're not using Forge, set the side to Client or Server.\n";
                //							break;
                //						}
                //					}*/
                //					
                //					System.err.println(s);
                //					
                //					crashed = true;
                //					
                //					final String errMsg = s;
                //					SwingUtilities.invokeLater(new Runnable() {
                //						@Override
                //						public void run() {
                //							progressLabel.setText(" ");
                //							progressBar.setValue(0);
                //							
                //							Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(errMsg), null);
                //							JOptionPane.showMessageDialog(GuiMain.this, errMsg, "BON - Error", JOptionPane.ERROR_MESSAGE);
                //						}
                //					});
                //				} finally {
                //					if(!crashed) {
                //						SwingUtilities.invokeLater(new Runnable() {
                //							@Override
                //							public void run() {
                //								progressLabel.setText(" ");
                //								progressBar.setValue(0);
                //								
                //								JOptionPane.showMessageDialog(GuiMain.this, "Done!", "BON", JOptionPane.INFORMATION_MESSAGE);
                //							}
                //						});
                //					}
                //				}
            }
        };
        
        curTask.start();
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
    
    public GuiMain()
    {
        JPanel contentPane = new JPanel();
        GridBagConstraints gbc;
        
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.gridy = 1;
        GridBagLayout gbl_contentPane = new GridBagLayout();
        gbl_contentPane.columnWeights = new double[] { 0.0 };
        gbl_contentPane.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0 };
        contentPane.setLayout(gbl_contentPane);
        
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 1;
        gbc.gridy = 2;
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.ipadx = 200;
        gbc.gridy = 1;
        gbc.gridy = 2;
        gbc.gridy = 3;
        //contentPane.add(sideSelect, gbc.clone());
        //        gbc.gridy = 4;
        //        contentPane.add(opSelect, gbc.clone());
        
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        gbc.gridy = 6;
        
        gbc.gridy = 7;
        
        setContentPane(contentPane);
        
        //sideSelect = new JComboBox<Side>(Side.values());
        //        opSelect = new JComboBox<Operation>(Operation.values());
        
        progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
        GridBagConstraints gbc_progressBar = new GridBagConstraints();
        gbc_progressBar.insets = new Insets(5, 5, 5, 5);
        gbc_progressBar.gridx = 0;
        gbc_progressBar.gridy = 2;
        contentPane.add(progressBar, gbc_progressBar);
        progressLabel = new JLabel(" ", SwingConstants.LEFT);
        GridBagConstraints gbc_progressLabel = new GridBagConstraints();
        gbc_progressLabel.insets = new Insets(0, 0, 5, 5);
        gbc_progressLabel.gridx = 0;
        gbc_progressLabel.gridy = 3;
        contentPane.add(progressLabel, gbc_progressLabel);
        
        goButton = new JButton("Go");
        GridBagConstraints gbc_goButton = new GridBagConstraints();
        gbc_goButton.fill = GridBagConstraints.HORIZONTAL;
        gbc_goButton.anchor = GridBagConstraints.NORTH;
        gbc_goButton.insets = new Insets(0, 0, 0, 5);
        gbc_goButton.gridx = 0;
        gbc_goButton.gridy = 1;
        contentPane.add(goButton, gbc_goButton);
        
        goButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                goButtonPressed();
            }
        });
        pack();
        
        {
            String mcpDirString = prefs.get(PREFS_KEY_MCPDIR, ".");
            
            if (!mcpDirString.equals(""))
                mcpBrowseDir.val = new File(mcpDirString);
            else
                mcpBrowseDir.val = new File(".");
        }
        
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                savePrefs();
            }
        });
        
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("MCP Mapping Viewer");
    }
    
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                new GuiMain().setVisible(true);
            }
        });
    }
}
