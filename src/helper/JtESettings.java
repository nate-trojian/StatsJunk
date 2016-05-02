package helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JtESettings
{
    private LinkedList<Rule> rulesSet;
    private String path;
    private boolean OoO;

    public JtESettings(String folder)
    {
        File f = new File("data" + File.separator + folder + File.separator + "settings.ini");
        rulesSet = parseSettings(f);
        System.out.println("Initialize " + rulesSet.size());
    }

    private LinkedList<Rule> parseSettings(File f)
    {
        LinkedList<Rule> temp = new LinkedList<Rule>();
        try
        {
            BufferedReader in = new BufferedReader(new FileReader(f));
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

            Map map = (Map)parser.parse(Util.cleanInput(in), cf);
            Iterator it = map.entrySet().iterator();
            while(it.hasNext())
            {
                /*
                 * Entry is the rule to parse
                 * Column number is entry.key
                 * Entry.value is Map, where we search for specific values
                 */
                 Map.Entry entry = (Map.Entry) it.next();
                         String column = (String) entry.getKey();
                         try
                         {
                             //Test to see if it is the path
                             if(column.equals("data"))
                             {
                                 Map dataEntry = (Map) entry.getValue();
                                 Object p = dataEntry.get("path");
                                 if(p == null || !(p instanceof String))
                                     throw new Exception(column + ".path");
                                 path = String.valueOf(p);
                                 Object ooo;
                                 if((ooo= dataEntry.get("OoO")) != null)
                                 {
                                     if(!(ooo instanceof Boolean))
                                         throw new Exception(column + ".OoO");
                                     OoO = (Boolean)ooo;
                                 }
                                 else
                                 {
                                     OoO = false;
                                 }
                                 continue;
                             }

                             //Get all the values
                             Map rule = (Map) entry.getValue();
                             Object k = rule.get("key");
                             if(k == null || !(k instanceof String))
                                 throw new Exception(column + ".key");
                             Object t = rule.get("title");
                             if(t != null && !(t instanceof String))
                                 throw new Exception(column + ".title");
                             Object ty = rule.get("type");
                             if(ty == null || !(ty instanceof String))
                                 throw new Exception(column + ".type");
                             Object o = rule.get("options");
                             if(o != null && !(o instanceof Map))
                                 throw new Exception(column + ".options");

                             //Handle the values
                             int columnNumber = -1;
                             if(!column.startsWith("filter"))
                             {
                                 columnNumber = Integer.parseInt(column);
                             }
                             String key = String.valueOf(k);
                             String title = String.valueOf(t);
                             JSONCellType cellType = JSONCellType.valueOf(((String) ty).toUpperCase());
                             if(cellType == null)
                                 throw new Exception(column + ".type");
                             Rule r = new Rule(columnNumber, key, title, cellType);
                             System.out.println("Type " + r.cellType);
                             if(o != null)
                             {
                                 //Make the options map
                                 Map optionMap = (Map) o;

                                 //Get default if its there
                                 Object def;
                                 if((def = optionMap.get("def")) != null)
                                     r.setDefault(def);
                                 Object typeObj;
                                 switch(cellType)
                                 {
                                     case INT:
                                         break;
                                     case BOOL:
                                         if((typeObj = optionMap.get("asNum")) != null)
                                         {
                                             if(!(typeObj instanceof Boolean))
                                                 throw new Exception(column + ".options.asNum");
                                             r.setOption(0, (Boolean) typeObj);
                                         }
                                         break;
                                     case DOUBLE:
                                         if((typeObj = optionMap.get("dec")) != null)
                                         {
                                             if(!(typeObj instanceof Integer))
                                                 throw new Exception(column + ".options.dec");
                                             r.setOption(0, (Integer) typeObj);
                                         }
                                         break;
                                     case STRING:
                                         if((typeObj = optionMap.get("case")) != null)
                                         {
                                             if(!(typeObj instanceof String))
                                                 throw new Exception(column + ".options.case");
                                             r.setOption(0, (String) typeObj);
                                         }
                                         break;
                                     case DATE:
                                         if((typeObj = optionMap.get("format")) != null)
                                         {
                                             if(!(typeObj instanceof String))
                                                 throw new Exception(column + ".options.format");
                                             r.setOption(0, (String) typeObj);
                                         }
                                         if((typeObj = optionMap.get("inSec")) != null)
                                         {
                                             if(!(typeObj instanceof Boolean))
                                                 throw new Exception(column + ".options.inSec");
                                             r.setOption(1, (Boolean) typeObj);
                                         }
                                         if((typeObj = optionMap.get("fromGMT")) != null)
                                         {
                                             if(!(typeObj instanceof Boolean))
                                                 throw new Exception(column + ".options.fromGMT");
                                             r.setOption(2, (Boolean) typeObj);
                                         }
                                         break;
                                 }
                             }
                             if(columnNumber == -1)
                                 temp.addFirst(r);
                             else
                                 temp.addLast(r);
                         }
                         catch(NumberFormatException nfe)
                         {
                             nfe.printStackTrace();
                         }
                         catch(Exception e)
                         {
                             System.err.println("Problem in " + e.getMessage());
                         }
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        return temp;
    }

    public void applyRuleset(Map obj, XSSFRow row, XSSFWorkbook workbook)
    {
        Iterator<Rule> ruleIt = rulesSet.iterator();
        System.out.println("Apply " + rulesSet.size());
        while(ruleIt.hasNext())
        {
            Rule rule = ruleIt.next();
            System.out.println(row.getRowNum() + " " + rule.column);
            XSSFCell cell = row.createCell(rule.column);
            if(obj == null)
            {
                cell.setCellValue(rule.rowTitle);
            }
            else
            {
                Object val = obj.get(rule.key);
                if(val == null)
                {
                    val = rule.def;
                }
                switch(rule.cellType)
                {
                    case INT:
                        cell.setCellValue(Integer.valueOf(val.toString()));
                        break;
                    case BOOL:
                        if(rule.options[0] == null || !(Boolean)rule.options[0])
                        {
                            //As boolean value
                            cell.setCellValue(Boolean.valueOf(val.toString()));
                        }
                        else
                        {
                            cell.setCellValue( (Boolean.valueOf(val.toString())?1:0) );
                        }
                        break;
                    case DOUBLE:
                        cell.setCellValue(Double.valueOf(val.toString()));
                        if(rule.options[0] != null)
                        {
                            XSSFDataFormat format = workbook.createDataFormat();
                            XSSFCellStyle style = workbook.createCellStyle();
                            int dec = Integer.valueOf(val.toString());
                            String form = "0";
                            if(dec > 0)
                            {
                                form +=".";
                                for(int i=0; i<dec; i++)
                                    form += "0";
                            }
                            style.setDataFormat(format.getFormat(form));
                            cell.setCellStyle(style);
                        }
                        break;
                    case STRING:
                        if(rule.options[0] == null)
                        {
                            //As is
                            cell.setCellValue(val.toString());
                        }
                        else
                        {
                            //In specific case
                            if(rule.options[0].toString().equals("U"))
                                cell.setCellValue(val.toString().toUpperCase());
                            else if(rule.options[0].toString().equals("L"))
                                cell.setCellValue(val.toString().toLowerCase());
                            else
                                cell.setCellValue(val.toString());
                        }
                        break;
                    case DATE:
                        Date tempDate;
                        SimpleDateFormat dateFormat;
                        Boolean inSec = (rule.options[1] == null?false:Boolean.valueOf(rule.options[1].toString()));
                        Boolean fromGMT = (rule.options[2] == null?false:Boolean.valueOf(rule.options[2].toString()));
                        if(rule.options[0] == null)
                        {
                            try
                            {
                                dateFormat = new SimpleDateFormat();
                                if(fromGMT)
                                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                                tempDate = dateFormat.parse(val.toString());
                                if(inSec)
                                    cell.setCellValue((int)(tempDate.getTime()/1000));
                                else
                                    cell.setCellValue(tempDate);
                            }
                            catch (java.text.ParseException e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else
                        {

                            try
                            {
                                XSSFDataFormat format = workbook.createDataFormat();
                                String strFormat = rule.options[0].toString();
                                dateFormat = new SimpleDateFormat(strFormat);
                                tempDate = dateFormat.parse(val.toString());
                                if(fromGMT)
                                {
                                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                                    String tempDateString = dateFormat.format(tempDate);
                                    //System.out.println(tempDateString);
                                    tempDate = dateFormat.parse(tempDateString);
                                }
                                //System.out.println(tempDate);
                                if(inSec)
                                {
                                    cell.setCellValue((int)(tempDate.getTime()/1000));
                                }
                                else
                                {
                                    XSSFCellStyle style = workbook.createCellStyle();
                                    style.setDataFormat(format.getFormat(strFormat));
                                    cell.setCellStyle(style);
                                    cell.setCellValue(tempDate);
                                }
                            }
                            catch (java.text.ParseException e)
                            {
                                e.printStackTrace();
                            }
                        }
                        break;
                }
            }
        }
    }

    private class Rule
    {
        protected int column;
        protected String key, rowTitle;
        protected JSONCellType cellType;
        protected Object[] options;
        protected Object def = null;

        public Rule(int c, String k, JSONCellType ct)
        {
            this(c, k, k, ct);
        }

        public Rule(int c, String k, String rt, JSONCellType ct)
        {
            column = c;
            key = k;
            if(rt.equals("null"))
                rowTitle = k;
            else
                rowTitle = rt;
            cellType = ct;
            options = new Object[cellType.numOptions()];
        }

        public void setDefault(Object o)
        {
            def = o;
        }

        public void setOption(int num, Object o)
        {
            options[num] = o;
        }
    }

    private enum JSONCellType
    {
        INT(0),
        BOOL(1, "asNum"),
        DOUBLE(1, "dec"),
        STRING(1, "case"),
        DATE(3, "format", "inSec", "fromGMT");

        private int numOptions;
        private String[] optionKey;
        JSONCellType(int o, String... oK)
        {
            numOptions = o;
            optionKey = oK;
        }

        public int numOptions()
        {
            return numOptions;
        }

        public int indexOf(String o)
        {
            for(int i=0; i<numOptions; i++)
                if(optionKey[i].equals(o))
                    return i;
            return -1;
        }
    }

    public LinkedList test()
    {
        return rulesSet;
    }

    public String getPath()
    {
        return path;
    }

    public boolean isObjectofObjects()
    {
        return OoO;
    }
}
