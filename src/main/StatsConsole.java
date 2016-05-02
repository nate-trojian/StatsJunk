package main;

import helper.JtEConverter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class StatsConsole
{
    private final int WINDOW_WIDTH = 400, WINDOW_HEIGHT = 300;
    private final Dimension oneLine = new Dimension(WINDOW_WIDTH, 28);
    private final String DATA_HEADER = "data"+File.separator;
    private File dir = new File("data");
    private JtEConverter converter;

    private JFrame frame;
    private JTextField commandLine, outLine;
    private ArrayList<SecondaryWindow> windowsOpen;

    public StatsConsole()
    {
        windowsOpen = new ArrayList<SecondaryWindow>();
        frame = new JFrame("Stats Console");
        frame.setMinimumSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        commandLine = new JTextField();
        frame.add(commandLine, BorderLayout.NORTH);

        DefaultListModel lm = new DefaultListModel();
        //lm.addElement(new String[]{"This", "is", "a", "test"});
        //lm.addElement("Hello World");
        JList list = new JList(lm);
        JScrollPane listScrollPane = new JScrollPane(list);
        listScrollPane.setMinimumSize(oneLine);
        listScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        //frame.add(listScrollPane, BorderLayout.CENTER);

        outLine = new JTextField();
        outLine.setEditable(false);
        outLine.setMinimumSize(oneLine);
        //frame.add(out, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, listScrollPane, outLine);
        splitPane.setResizeWeight(1.0);
        frame.add(splitPane, BorderLayout.CENTER);

        //Define behaviors using listeners
        commandLine.addFocusListener(new FocusListener(){
            private JList list;

            public FocusListener init(JList l)
            {
                list = l;
                return this;
            }

            @Override
            public void focusGained(FocusEvent arg0)
            {
                list.clearSelection();
            }

            @Override
            public void focusLost(FocusEvent arg0)
            {
            }

        }.init(list));

        commandLine.addActionListener(new ActionListener(){
            JList list;

            public ActionListener init(JList l)
            {
                list = l;
                return this;
            }

            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                String command = ((JTextField)arg0.getSource()).getText();
                parseCommand(command);
                ((DefaultListModel)list.getModel()).insertElementAt(command, 0);
            }
        }.init(list));

        list.addMouseListener(new MouseListener(){
            JTextField text;

            public MouseListener init(JTextField t)
            {
                text = t;
                return this;
            }

            @Override
            public void mouseClicked(MouseEvent arg0)
            {
                JList list = (JList) arg0.getSource();
                if(arg0.getClickCount() == 2)
                {
                    text.setText(list.getSelectedValue().toString());
                }
            }

            @Override
            public void mouseEntered(MouseEvent arg0)
            {
            }

            @Override
            public void mouseExited(MouseEvent arg0)
            {
            }

            @Override
            public void mousePressed(MouseEvent arg0)
            {
            }

            @Override
            public void mouseReleased(MouseEvent arg0)
            {
            }
        }.init(commandLine));

        frame.pack();
        frame.setVisible(true);

        converter = new JtEConverter();
    }

    private void parseCommand(String c)
    {
        String[] commParts = c.split(" ");
        String command = commParts[0];
        if(commParts.length == 1)
        {
            this.sendToOutput("No Command Parameters Given");
            return;
        }

        //Different Commands
        if(command.equals("settings"))
        {
            if(commParts[1].equals("show"))
            {
                if(commParts.length == 2)
                {
                    this.sendToOutput("No Project Name Given");
                    return;
                }
                String name = "";
                boolean clean = false;
                if(commParts.length == 4)
                {
                    if(commParts[2].equals("clean"))
                    {
                        clean = true;
                        name = commParts[3];
                    }
                    else
                    {
                        this.sendToOutput("Command parameters not recognized");
                        return;
                    }
                }
                else if(commParts.length == 3)
                {
                    name = commParts[2];
                }
                else
                {
                    this.sendToOutput("Incorrect number of parameters");
                    return;
                }
                SettingsWindow sw = new SettingsWindow(this, name, clean);
                windowsOpen.add(sw);
            }
            else if(commParts[1].equals("save"))
            {
                if(commParts.length == 2)
                {
                    this.sendToOutput("No Project Name Given");
                    return;
                }
                String name = commParts[2];
                for(SecondaryWindow sw : windowsOpen)
                {
                    if(sw.windowType == 0 && sw.name.equals(name))
                    {
                        if(((SettingsWindow)sw).writeTextToFile())
                        {
                            this.sendToOutput("Settings for " + name + " have been saved");
                            return;
                        }
                    }
                }
                this.sendToOutput("Window not found");
            }
            else if(commParts[1].equals("generate"))
            {
                //We get to make custom combo boxes WOH
                if(commParts.length == 2)
                {
                    this.sendToOutput("No Project Name Given");
                    return;
                }
                String name = commParts[2];
                ComboWindow cw = new ComboWindow(this, name);
                windowsOpen.add(cw);
            }
            else
            {
                this.sendToOutput("Command Not Recognized");
            }
        }
        else if(command.equals("project"))
        {
            if(commParts[1].equals("list"))
            {
                String output = "Created Projects {";
                String[] projList = dir.list(new FilenameFilter(){
                    @Override
                    public boolean accept(File f, String name)
                    {
                        return (name.indexOf('.') == -1);
                    }
                });
                for(int i=0; i<projList.length-1; i++)
                {
                    output+=projList[i]+",";
                }
                output+=projList[projList.length-1]+"}";
                this.sendToOutput(output);
            }
            else if(commParts[1].equals("create"))
            {
                String projToCreate = "";
                if(commParts.length == 2)
                {
                    //No project name
                    this.sendToOutput("No Project name given");
                }
                else
                {
                    projToCreate = commParts[2];
                    File creProj = new File(DATA_HEADER+projToCreate+File.separator+"settings.ini");
                    try
                    {
                        if(!creProj.getParentFile().exists())
                            creProj.getParentFile().mkdir();
                        if(!creProj.createNewFile())
                            throw new IOException("Project files already exist");
                        this.sendToOutput("New Project created.");
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        this.sendToOutput("Failed to create Project file.");
                    }
                }
            }
            else if(commParts[1].equals("grid"))
            {
                String projToGrid = "";
                if(commParts.length == 2)
                {
                    //No project name
                    this.sendToOutput("No Project name given");
                }
                else
                {
                    projToGrid = commParts[2];
                    File gridProj = new File(DATA_HEADER+projToGrid+File.separator+projToGrid+".xlsx");
                    if(gridProj.exists())
                    {
                        GridWindow gw = new GridWindow(this, projToGrid);
                        windowsOpen.add(gw);
                    }
                    else
                    {
                        //Project does not exist or have settings file
                        this.sendToOutput("Project does not exist or does not contain an Excel file");
                    }
                }
            }
            else if(commParts[1].equals("import"))
            {
                String projToImport = "";
                if(commParts.length == 2)
                {
                    //No project name
                    this.sendToOutput("No Project name given");
                }
                else
                {
                    projToImport = commParts[2];
                    File importProj = new File(DATA_HEADER+projToImport+File.separator+"settings.ini");
                    if(importProj.exists())
                    {
                        if(converter.convertToExcel(projToImport) == null)
                            this.sendToOutput("Conversion Failed");
                        else
                            this.sendToOutput("Conversion Successful");
                    }
                    else
                    {
                        //Project does not exist or have settings file
                        this.sendToOutput("Project does not exist or does not contain settings file");
                    }
                }
            }
            else
            {
                this.sendToOutput("Command Not Recognized");
            }
        }
        else
        {
            //Command not recognized
            this.sendToOutput("Command Not Recognized");
        }
    }

    public void windowClosed(SecondaryWindow sw)
    {
        for(int i=0; i<windowsOpen.size(); i++)
        {
            SecondaryWindow tempWindow = windowsOpen.get(i);
            if(tempWindow.name.equals(sw.name) && tempWindow.windowType == sw.windowType)
            {
                windowsOpen.remove(i);
                System.out.println("Window Closed");
                break;
            }
        }
    }

    public Object getJFrameProp(String prop)
    {
        if(frame == null)
        {
        }
        else if(prop.equalsIgnoreCase("dimension"))
        {
            return frame.getSize();
        }
        else if(prop.equalsIgnoreCase("width"))
        {
            return frame.getWidth();
        }
        else if(prop.equalsIgnoreCase("height"))
        {
            return frame.getHeight();
        }
        else if(prop.equalsIgnoreCase("mindimension"))
        {
            return new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);
        }
        else if(prop.equalsIgnoreCase("location"))
        {
            return frame.getLocationOnScreen();
        }

        return null;
    }

    public void sendToOutput(String text)
    {
        outLine.setText(text);
    }

    public static void main(String[] args)
    {
        StatsConsole sc = new StatsConsole();
    }
}
