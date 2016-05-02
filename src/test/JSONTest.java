package test;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONValue;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONTest
{
    public JSONTest()
    {
    }

    public void test1()
    {
        String jsonText = "{\"first\": {\"id1\":\"Hello World\", \"data\":[{\"test\":2},3,5]}, \"second\": [4, 5, 6], \"third\": true}";
          JSONParser parser = new JSONParser();
          ContainerFactory containerFactory = new ContainerFactory(){
            public List creatArrayContainer() {
              return new LinkedList();
            }

            public Map createObjectContainer() {
              return new LinkedHashMap();
            }

          };

          try{
            Map json = (Map)parser.parse(jsonText, containerFactory);
            Iterator iter = json.entrySet().iterator();
            System.out.println("==iterate result==");
            while(iter.hasNext()){
              Map.Entry entry = (Map.Entry)iter.next();
              System.out.print(entry.getKey() + "=>" + entry.getValue());
              //System.out.println(" " + (entry.getValue() instanceof Map || entry.getValue() instanceof List));
              System.out.println(" " + entry.getValue().getClass());
            }

            System.out.println("==toJSONString()==");
            System.out.println(JSONValue.toJSONString(json));
          }
          catch(ParseException pe){
            System.out.println(pe);
          }
    }

    public static void main(String[] args)
    {
        JSONTest jt = new JSONTest();
        jt.test1();
    }
}
