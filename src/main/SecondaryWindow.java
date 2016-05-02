package main;

public abstract class SecondaryWindow
{
    protected StatsConsole console;
    protected int windowType;
    protected String name;
    public SecondaryWindow(StatsConsole sc, String n, int wt)
    {
        console = sc;
        name = n;
        windowType = wt;
    }
}
