package org.schumiwildeprojects.kck1.cli.states;

import com.googlecode.lanterna.gui2.BasicWindow;
import org.schumiwildeprojects.kck1.backend.ConnectionState;
import org.schumiwildeprojects.kck1.cli.BasicLateInitWindow;
import org.schumiwildeprojects.kck1.cli.LoginWindow;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

// Logowanie
public class LoginState extends State {
    private LoginWindow window;

    public LoginState() throws IOException {
        super();
        window = LoginWindow.getInstance();
    }

    @Override
    public BasicLateInitWindow getWindow() throws IOException {
        return LoginWindow.getInstance();
    }

    @Override
    public void onClose() throws IOException {
        terminal.changeState(new ExitState());
    }

    @Override
    public void onSubmit() throws IOException {
        String nick = window.getNickname();
        String login = window.getLogin();
        String fullName = window.getFullName();
        String channel = window.getChannel();
        terminal.initializeConnectionThread(nick, login, fullName, channel);
        terminal.changeState(new ConnectingProgressState());
    }
}
