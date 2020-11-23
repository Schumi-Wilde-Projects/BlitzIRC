package org.schumiwildeprojects.kck1.cli.states;

import org.schumiwildeprojects.kck1.cli.BasicLateInitWindow;
import org.schumiwildeprojects.kck1.cli.MainCommWindow;

import java.io.IOException;

public class MainCommState extends State {
    private final MainCommWindow window;

    public MainCommState() throws IOException {
        super();
        window = MainCommWindow.getInstance();
    }

    @Override
    public BasicLateInitWindow getWindow() {
        return MainCommWindow.getInstance();
    }

    @Override
    public void onClose() throws IOException {
        window.leaveChannel();
        terminal.changeState(new ExitState());
    }

    @Override
    public void onSubmit() {

    }
}
