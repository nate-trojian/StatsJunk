package helper;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public abstract class CloseListener implements WindowListener
{
    private Object[] callers;

    public CloseListener(Object... callers)
    {
        this.callers = callers;
    }

    public Object getCaller(int index)
    {
        return callers[index];
    }

    @Override
    public void windowActivated(WindowEvent e)
    {
    }

    public abstract void windowClosed(WindowEvent e);

    @Override
    public void windowClosing(WindowEvent e)
    {
    }

    @Override
    public void windowDeactivated(WindowEvent e)
    {
    }

    @Override
    public void windowDeiconified(WindowEvent e)
    {
    }

    @Override
    public void windowIconified(WindowEvent e)
    {
    }

    @Override
    public void windowOpened(WindowEvent e)
    {
    }
}
