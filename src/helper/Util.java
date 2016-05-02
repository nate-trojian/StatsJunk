package helper;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Util
{
    public static String getLastToken(String s, String t)
    {
        int i = s.lastIndexOf(t);
        if(i == -1)
            return s;
        else
            return s.substring(i+1);
    }

    public static Integer getNumberOfOccur(String base, String tester)
    {
        return getNumberOfOccur(base, tester, 0, base.length());
    }

    public static Integer getNumberOfOccur(String base, String tester, int startIndex, int endIndex)
    {
        int curIndex = startIndex, newIndex, ret = 0;
        while((newIndex = base.indexOf(tester, curIndex)) != -1 && newIndex < endIndex)
        {
            ret++;
            curIndex = newIndex+1;
        }
        return ret;
    }

    public static String cleanInput(BufferedReader in)
    {
        String ret = "";
        try
        {
            String line;
            while((line = in.readLine()) != null)
            {
                byte[] lineArr = line.getBytes();
                ArrayList<Byte> newLineArrList = new ArrayList<Byte>();
                int numInRow = 0;
                for(int i=0; i<lineArr.length; i++)
                {
                    if(lineArr[i] == 63)
                    {
                        numInRow++;
                    }
                    else
                    {
                        while(numInRow > 0)
                        {
                            newLineArrList.add((byte) 63);
                            numInRow--;
                        }
                        newLineArrList.add(lineArr[i]);
                    }
                    if(numInRow == 3)
                    {
                        newLineArrList.add((byte) 34);
                        numInRow = 0;
                    }
                }
                byte[] newLineArr = new byte[newLineArrList.size()];
                for(int i=0; i<newLineArr.length; i++)
                    newLineArr[i] = newLineArrList.get(i);
                String test = new String(newLineArr);
                System.out.println(test);
                ret += test + '\n';
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return ret;
    }

    public static String cleanInput(String line)
    {
        byte[] lineArr = line.getBytes();
        ArrayList<Byte> newLineArrList = new ArrayList<Byte>();
        int numInRow = 0;
        for(int i=0; i<lineArr.length; i++)
        {
            if(lineArr[i] == 63)
            {
                numInRow++;
            }
            else
            {
                while(numInRow > 0)
                {
                    newLineArrList.add((byte) 63);
                    numInRow--;
                }
                newLineArrList.add(lineArr[i]);
            }
            if(numInRow == 3)
            {
                newLineArrList.add((byte) 34);
                numInRow = 0;
            }
        }
        byte[] newLineArr = new byte[newLineArrList.size()];
        for(int i=0; i<newLineArr.length; i++)
            newLineArr[i] = newLineArrList.get(i);
        String test = new String(newLineArr);
        return test;
    }

    public static Dimension scaleDim(Dimension d, float scale)
    {
        return scaleDim(d, scale, scale);
    }

    public static Dimension scaleDim(Dimension d, float scaleW, float scaleH)
    {
        return new Dimension((int)(d.getWidth()*scaleW), (int)(d.getHeight()*scaleH));
    }

    public static Dimension addToDim(Dimension d, Integer value)
    {
        return addToDim(d, value, value);
    }

    public static Dimension addToDim(Dimension d, Integer vW, Integer vH)
    {
        return new Dimension((int)d.getWidth() + vW, (int)d.getHeight() + vH);
    }

    public static void main(String args[])
    {
        System.out.println(Util.getLastToken("a/b/c", File.separator));
    }
}
