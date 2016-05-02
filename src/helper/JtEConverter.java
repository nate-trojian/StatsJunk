package helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JtEConverter
{
    public JtEConverter()
    {
    }

    private Collection parseJSON(String folder, String path)
    {
        BufferedReader in = null;
        if(path == null || path.equals("this"))
        {
            //JSON data is folder, name same as folder, text file
            try
            {
                File f = new File("data"+File.separator+folder+File.separator+Util.getLastToken(folder, File.separator)+".txt");
                in = new BufferedReader(new FileReader(f));
            }
            catch(FileNotFoundException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            if(path.startsWith("http"))
            {
                //Is URL
                try
                {
                    URL url = new URL(path);
                    in = new BufferedReader(new InputStreamReader(url.openStream()));
                }
                catch(MalformedURLException e)
                {
                    e.printStackTrace();
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                try
                {
                    File f = new File(path);
                    in = new BufferedReader(new FileReader(f));
                }
                catch(FileNotFoundException e)
                {
                    e.printStackTrace();
                }
            }
        }

        if(in == null)
        {
            try
            {
                throw new Exception("Reader not set");
            }
            catch(Exception e)
            {
                System.err.println(e.getMessage());
                return null;
            }
        }

        JSONParser parser = new JSONParser();
        ContainerFactory cf = new ContainerFactory(){

            @Override
            public List creatArrayContainer()
            {
                return new LinkedList();
            }

            @Override
            public Map createObjectContainer()
            {
                return new LinkedHashMap();
            }
        };

        Collection c = null;
        try
        {
            c = (Collection) parser.parse(in, cf);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        return c;
    }

    public XSSFWorkbook convertToExcel(String folder)
    {
        JtESettings settings = new JtESettings(folder);
        Collection c = parseJSON(folder, settings.getPath());

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(Util.getLastToken(folder, File.separator));

        settings.applyRuleset(null, sheet.createRow(0), workbook);

        if(c instanceof LinkedList)
        {
            //Iterate through list
            //Each element in list is object to apply ruleset to
            System.out.println("LinkedList");
            LinkedList list = (LinkedList) c;
            Iterator it = list.iterator();
            int rowIndex = 1;
            while(it.hasNext())
            {
                Map entry = (Map)it.next();
                settings.applyRuleset(entry, sheet.createRow(rowIndex), workbook);
                rowIndex++;
            }
        }
        else if(c instanceof LinkedHashMap)
        {
            //Objects are given name
            //Iterate through the objects
            System.out.println("HashMap");
            LinkedHashMap map = (LinkedHashMap) c;
            if(settings.isObjectofObjects())
            {
                //Iterate through objects in map
                Iterator keyIt = map.keySet().iterator();
                int rowIndex = 1;
                while(keyIt.hasNext())
                {
                    Map entry = (Map)map.get(keyIt.next());
                    settings.applyRuleset(entry, sheet.createRow(rowIndex), workbook);
                    rowIndex++;
                }
            }
            else
            {
                //Just give the map as it is
                settings.applyRuleset(map, sheet.createRow(1), workbook);
            }
        }
        else
        {
            //Well something went wrong
            //Umm
            return null;
        }

        try
        {
            FileOutputStream fos = new FileOutputStream("data" + File.separator + folder + File.separator + Util.getLastToken(folder, File.separator) + ".xlsx");
            workbook.write(fos);
            fos.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return null;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }

        return workbook;
    }
}
