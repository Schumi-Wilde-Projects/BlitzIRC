package org.schumiwildeprojects.kck1.cli.states;

import org.schumiwildeprojects.kck1.cli.BasicLateInitWindow;
import org.schumiwildeprojects.kck1.cli.ConnectingProgressWindow;

import java.io.IOException;

public class ConnectingProgressState extends State {
    private ConnectingProgressWindow window;

    public ConnectingProgressState() throws IOException {
        window = ConnectingProgressWindow.getInstance();
    }

    @Override
    public BasicLateInitWindow getWindow() {
        return ConnectingProgressWindow.getInstance();
    }

    @Override
    public void onClose() throws IOException {
        terminal.changeState(new ExitState());
    }

    @Override
    public void onSubmit() throws IOException {
        terminal.changeState(new MainCommState());
    }
}
