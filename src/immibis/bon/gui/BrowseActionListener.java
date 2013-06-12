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
package immibis.bon.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

public class BrowseActionListener implements ActionListener
{
    
    private JTextField      textbox;
    private boolean         isOpen;
    private Component       parent;
    private JFileChooser    jfc;
    private Reference<File> defaultDir;
    
    public BrowseActionListener(JTextField inputField, boolean isOpen, Component parent, boolean dirOnly, Reference<File> defaultDir)
    {
        
        this.defaultDir = defaultDir;
        this.textbox = inputField;
        this.isOpen = isOpen;
        this.parent = parent;
        
        jfc = new JFileChooser();
        jfc.setFileSelectionMode(dirOnly ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);
        
        if (!dirOnly)
            jfc.addChoosableFileFilter(new FileFilter()
            {
                @Override
                public String getDescription()
                {
                    return "Jars and zips only";
                }
                
                @Override
                public boolean accept(File arg0)
                {
                    String fn = arg0.getName();
                    return arg0.isDirectory() || fn.endsWith(".jar") || fn.endsWith(".zip");
                }
            });
    }
    
    @Override
    public void actionPerformed(ActionEvent arg0)
    {
        int rv;
        jfc.setCurrentDirectory(defaultDir.val);
        if (isOpen)
            rv = jfc.showOpenDialog(parent);
        else
            rv = jfc.showSaveDialog(parent);
        
        if (rv == JFileChooser.APPROVE_OPTION)
        {
            File f = jfc.getSelectedFile();
            
            try
            {
                textbox.setText(f.getCanonicalPath());
            }
            catch (IOException e)
            {
                textbox.setText(f.getAbsolutePath());
            }
        }
        
        defaultDir.val = jfc.getCurrentDirectory();
    }
    
}
