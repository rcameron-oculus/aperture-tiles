/**
 * Copyright (c) 2013 Oculus Info Inc. 
 * http://www.oculusinfo.com/
 * 
 * Released under the MIT License.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oculusinfo.binning.visualization;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.oculusinfo.binning.io.PyramidIO;
import com.oculusinfo.binning.io.impl.FileSystemPyramidIO;



/**
 * Class to allow a user to specify parameters for a FileSystemPyramidIO, and to
 * create one.
 * 
 * @author nkronenfeld
 */
public class FileSystemPyramidIOSelector extends JPanel implements PyramidIOSelector {
    private static final long   serialVersionUID = 1L;



    private String              _rootPath;
    private String              _extension;
    private FileSystemPyramidIO _io;
    private JTextField          _rootPathField;
    private JTextField          _extensionField;
    private JFileChooser        _fileChooser;



    public FileSystemPyramidIOSelector (JFileChooser chooser) {
        _rootPath = null;
        _extension = "avro";
        _io = null;
        _fileChooser = chooser;

        JLabel rootPathLabel = new JLabel("Root path:");
        rootPathLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        _rootPathField = new JTextField();
        JButton button = new JButton("...");

        JLabel extensionLabel = new JLabel("Tile extension:");
        extensionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        _extensionField = new JTextField(_extension);

        setLayout(new GridBagLayout());
        add(rootPathLabel,   new GridBagConstraints(0, 0, 1, 1, 0.5, 0.0, GridBagConstraints.EAST,   GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        add(_rootPathField,  new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        add(button,          new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        add(extensionLabel,  new GridBagConstraints(0, 1, 1, 1, 0.5, 0.0, GridBagConstraints.EAST,   GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        add(_extensionField, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        _rootPathField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate (DocumentEvent event) {
                setRootPath(_rootPathField.getText());
            }

            @Override
            public void insertUpdate (DocumentEvent event) {
                setRootPath(_rootPathField.getText());
            }

            @Override
            public void changedUpdate (DocumentEvent event) {
                setRootPath(_rootPathField.getText());
            }
        });
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent event) {
                _fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                _fileChooser.setFileFilter(_fileChooser.getAcceptAllFileFilter());
                int returnVal = _fileChooser.showOpenDialog(FileSystemPyramidIOSelector.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = _fileChooser.getSelectedFile();
                    _rootPathField.setText(file.getAbsolutePath()+"/");
                }
            }
        });
        _extensionField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate (DocumentEvent event) {
                setExtension(_extensionField.getText());
            }

            @Override
            public void insertUpdate (DocumentEvent event) {
                setExtension(_extensionField.getText());
            }

            @Override
            public void changedUpdate (DocumentEvent event) {
                setExtension(_extensionField.getText());
            }
        });
    }

    private void setRootPath (String rootPath) {
        if (!objectsEqual(_rootPath, rootPath)) {
            _rootPath = rootPath;
            updatePyramid();
        }
    }

    private void setExtension (String extension) {
        if (!objectsEqual(_extension, extension)) {
            _extension = extension;
            updatePyramid();
        }
    }

    private void updatePyramid () {
        FileSystemPyramidIO oldIO = _io;
        if (null != _rootPath && null != _extension) {
            _io = new FileSystemPyramidIO(_rootPath, _extension);
            firePropertyChange(BinVisualizer.PYRAMID_IO, oldIO, _io);
        } else if (null != _io) {
            _io = null;
            firePropertyChange(BinVisualizer.PYRAMID_IO, oldIO, _io);
        }
    }

    @Override
    public PyramidIO getPyramidIO () {
        return _io;
    }

    @Override
    public JPanel getPanel () {
        return this;
    }

    private static boolean objectsEqual (Object a, Object b) {
        if (null == a) return null == b;
        return a.equals(b);
    }
}
