package org.schumiwildeprojects.kck1.cli;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;

public class MainCommWindow extends BasicLateInitWindow {
    private static MainCommWindow instance;
    public static MainCommWindow getInstance() {
        if(instance == null)
            restartWindow();
        return instance;
    }

    static void restartWindow() {
        instance = new MainCommWindow("Komunikacja na " + IRCTerminal.currentChannel);
    }

    private MainCommWindow(String title) {
        super(title);
        setFixedSize(new TerminalSize(60, 10));
    }

    @Override
    public void start() {
        TerminalSize termSize = getTextGUI().getScreen().getTerminalSize();
        setPosition(new TerminalPosition((termSize.getColumns() - getSize().getColumns()) / 2, (termSize.getRows() - getSize().getRows()) / 2));
        returnVal = 0;
    }
}
