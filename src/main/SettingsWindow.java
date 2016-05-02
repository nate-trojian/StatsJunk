package main;

import helper.CloseListener;
import helper.Util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class SettingsWindow extends SecondaryWindow
{
    private final static int WINDOW_TYPE = 0;
    private File projFile;
    private JFrame frame;
    private JTextArea textBox;

    public SettingsWindow(StatsConsole sc, String name)
    {
        this(sc, name, false);
    }

    public SettingsWindow(StatsConsole sc, String name, boolean clean)
    {
        super(sc, name, WINDOW_TYPE);
        frame = new JFrame(name);
        Point p = (Point) sc.getJFrameProp("location");
        p.translate(100, 100);
        frame.setLocation(p);
        frame.setMinimumSize((Dimension) sc.getJFrameProp("mindimension"));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new CloseListener(sc,this){
            @Override
            public void windowClosed(WindowEvent e)
            {
                ((StatsConsole) getCaller(0)).windowClosed((SecondaryWindow) getCaller(1));
            }
        });

        textBox = new JTextArea();

        JScrollPane scrollPane = new JScrollPane(textBox);
        frame.add(scrollPane, BorderLayout.CENTER);

        frame.pack();
        frame.setVisible(true);

        //Check if name matches previous project
        //If yes, write to textBox
        //If no, have popup ask if they want to create the project folder
        projFile = new File("data"+File.separator+name+File.separator+"settings.ini");
        if(projFile.exists())
        {
            //Write to the Textbox
            if(!readFileToText(clean))
            {
                exit();
            }
        }
        else
        {
            //Bring up popup
            int choice = JOptionPane.showConfirmDialog(frame,
                    "Project does not exist.  Would you like to create a new project with this name?",
                    "New Project", JOptionPane.YES_NO_OPTION);
            if(choice == 0)
            {
                try
                {
                    if(!projFile.getParentFile().exists())
                        projFile.getParentFile().mkdir();
                    projFile.createNewFile();
                    this.console.sendToOutput("New Project created.");
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    this.console.sendToOutput("Failed to create Project file.");
                    exit();
                }
            }
            else
            {
                this.console.sendToOutput(name+" not found. No Project created.");
                exit();
            }
        }
    }

    public boolean readFileToText(boolean clean)
    {
        try
        {
            BufferedReader bfr = new BufferedReader(new FileReader(projFile));
            String line = "";
            while((line = bfr.readLine())!=null)
            {
                if(clean)
                    line = Util.cleanInput(line);
                System.out.println(line);
                textBox.append(line + '\n');
            }
            bfr.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            this.console.sendToOutput("Project file could not be found.");
            return false;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            this.console.sendToOutput("Project file could not be accessed.");
            return false;
        }
        return true;
    }

    public boolean writeTextToFile()
    {
        try
        {
            FileWriter fw = new FileWriter(projFile);
            textBox.write(fw);
            fw.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            this.console.sendToOutput("Project file could not be accessed.");
            return false;
        }
        return true;
    }

    private void exit()
    {
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }
}
