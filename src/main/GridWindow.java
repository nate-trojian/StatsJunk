package main;

import helper.CloseListener;
import helper.Util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class GridWindow extends SecondaryWindow
{
    private final static int WINDOW_TYPE = 1;
    private JFrame frame;
    private XSSFWorkbook excelFile;
    public GridWindow(StatsConsole sc, String n)
    {
        super(sc, n, WINDOW_TYPE);
        frame = new JFrame(n);
        frame.setMinimumSize((Dimension) sc.getJFrameProp("mindimension"));
        frame.setMaximumSize(Util.scaleDim(frame.getMinimumSize(), 2));
        Point p = (Point) sc.getJFrameProp("location");
        p.translate(100, 100);
        frame.setLocation(p);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new CloseListener(sc,this){
            @Override
            public void windowClosed(WindowEvent e)
            {
                ((StatsConsole) getCaller(0)).windowClosed((SecondaryWindow) getCaller(1));
            }
        });
        try
        {
            excelFile = (XSSFWorkbook) WorkbookFactory.create(new File("data"+File.separator+n+File.separator+n+".xlsx"));
        }
        catch (InvalidFormatException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        XSSFSheet sheet = excelFile.getSheetAt(0);
        GridLayout gridLayout = new GridLayout(sheet.getPhysicalNumberOfRows(), sheet.getRow(0).getPhysicalNumberOfCells());
        JPanel gridPanel = new JPanel(gridLayout);
        JScrollPane scrollPane = new JScrollPane(gridPanel);
        JLabel gridLabel;
        for(Row row : sheet)
        {
            for(Cell cell : row)
            {
                gridLabel = new JLabel();
                switch(cell.getCellType())
                {
                    case Cell.CELL_TYPE_STRING:
                        gridLabel.setText(cell.getRichStringCellValue().getString());
                        break;
                    case Cell.CELL_TYPE_NUMERIC:
                        if(DateUtil.isCellDateFormatted(cell))
                        {
                            gridLabel.setText(cell.getDateCellValue().toString());
                        }
                        else
                        {
                            gridLabel.setText(String.valueOf(cell.getNumericCellValue()));
                        }
                        break;
                    case Cell.CELL_TYPE_BOOLEAN:
                        gridLabel.setText(String.valueOf(cell.getBooleanCellValue()));
                        break;
                    case Cell.CELL_TYPE_BLANK:
                    default:
                        gridLabel.setText("");
                }
                gridLabel.setBorder(BorderFactory.createLineBorder(Color.black));
                gridLabel.setHorizontalAlignment(SwingConstants.CENTER);
                gridPanel.add(gridLabel);
            }
        }
        frame.add(scrollPane);

        frame.pack();
        frame.setVisible(true);
    }
}
