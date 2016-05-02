package main;

import helper.CloseListener;
import helper.Util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ComboWindow extends SecondaryWindow
{
    private final static int WINDOW_TYPE = 2;
    private JFrame frame;
    private File projFile;
    private Dimension conDim;

    public ComboWindow(StatsConsole sc, String n)
    {
        super(sc, n, WINDOW_TYPE);
        frame = new JFrame(name);
        Point p = (Point) sc.getJFrameProp("location");
        p.translate(100, 100);
        frame.setLocation(p);
        conDim = (Dimension) sc.getJFrameProp("mindimension");
        frame.setMinimumSize(Util.addToDim(conDim, 20, 27));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new CloseListener(sc,this) {
            @Override
            public void windowClosed(WindowEvent e)
            {
                ((StatsConsole) getCaller(0)).windowClosed((SecondaryWindow) getCaller(1));
            }
        });
        JPanel layoutPanel = new JPanel(new GridBagLayout());
        JScrollPane scrollPane = new JScrollPane(layoutPanel);
        JPanel dataPanel = new JPanel();
        dataPanel.setMinimumSize(Util.scaleDim(conDim, 1f, 0.5f));
        dataPanel.setPreferredSize(dataPanel.getMinimumSize());
        JButton addRowButton = new JButton();
        addRowButton.addActionListener(new ActionListener(){
            JPanel layoutPanel;
            int index;
            public ActionListener init(JPanel panel)
            {
                layoutPanel = panel;
                index = 0;
                return this;
            }

            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                GridBagConstraints c = new GridBagConstraints();
                c.anchor = GridBagConstraints.FIRST_LINE_START;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.weightx = 1.0d;
                c.weighty = 0.0d;
                c.gridx = 0;
                c.gridy = ++index;
                System.out.println(index);
                layoutPanel.add(addSettingsRow(), c);
                layoutPanel.revalidate();
                layoutPanel.repaint();
                System.out.println(layoutPanel.getComponent(0).getBounds());
                System.out.println(layoutPanel.getComponent(0).getMinimumSize());
            }
        }.init(layoutPanel));
        dataPanel.add(addRowButton, BorderLayout.EAST);
        dataPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0d;
        c.weighty = 0.0d;
        c.gridx = 0;
        c.gridy = 0;
        layoutPanel.add(dataPanel, c);

        frame.add(scrollPane);

        frame.pack();
        frame.setVisible(true);

        projFile = new File("data"+File.separator+name+File.separator+"settings.ini");
        if(!projFile.exists())
        {
            try
            {
                if(!projFile.getParentFile().exists())
                    projFile.getParentFile().mkdir();
                projFile.createNewFile();
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
                this.console.sendToOutput("Failed to create Project file.");
                exit();
            }
        }
    }

    private JPanel addSettingsRow()
    {
        JPanel settingsRow = new JPanel();
        settingsRow.setMinimumSize(Util.scaleDim(conDim, 1f, 0.5f));
        settingsRow.setPreferredSize(settingsRow.getMinimumSize());
        settingsRow.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        return settingsRow;
    }

    private void exit()
    {
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }
}
