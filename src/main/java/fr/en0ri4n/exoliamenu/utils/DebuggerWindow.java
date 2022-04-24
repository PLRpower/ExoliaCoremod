package fr.en0ri4n.exoliamenu.utils;

import javax.swing.*;

public class DebuggerWindow
{
    public static final JTextField textField = new JTextField();
    public static void start()
    {
        JFrame frame = new JFrame("Debugger");
        frame.setAlwaysOnTop(true);
        frame.setResizable(false);
        frame.setSize(800, 100);
        textField.setSize(800, 100);
        frame.getContentPane().add(textField);
        frame.setVisible(true);
    }
}
