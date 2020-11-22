package org.schumiwildeprojects.kck1.cli;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;

import java.io.IOException;

// Ekran logowania
public class LoginWindow extends BasicLateInitWindow {
    private TextBox nicknameBox;
    private TextBox loginBox;
    private TextBox fullNameBox;
    private TextBox channelBox;
    private final IRCTerminal terminal = IRCTerminal.getInstance();
    private static LoginWindow instance;
    public static LoginWindow getInstance() throws IOException {
        if(instance == null)
            restartWindow();
        return instance;
    }

    static void restartWindow() throws IOException {
        instance = new LoginWindow("Logowanie");
    }

    private LoginWindow(String title) throws IOException {
        super(title);
        setFixedSize(new TerminalSize(50, 7));
    }

    @Override
    public void start() {
        TerminalSize termSize = getTextGUI().getScreen().getTerminalSize();
        setPosition(new TerminalPosition((termSize.getColumns() - getSize().getColumns()) / 2, (termSize.getRows() - getSize().getRows()) / 2));
        Panel contentPanel = new Panel(new GridLayout(2));

        Panel nicknamePanel = new Panel(new LinearLayout(Direction.VERTICAL));
        Label nicknameLabel = new Label("Nick:");
        nicknamePanel.addComponent(nicknameLabel);
        nicknameBox = new TextBox(new TerminalSize((int)((float)getSize().getColumns() / 2.5), 1));
        nicknamePanel.addComponent(nicknameBox);
        contentPanel.addComponent(nicknamePanel.setLayoutData(GridLayout.createHorizontallyFilledLayoutData()));

        Panel loginPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        Label loginLabel = new Label("Login:");
        loginPanel.addComponent(loginLabel);
        loginBox = new TextBox(new TerminalSize((int)((float)getSize().getColumns() / 2.5), 1));
        loginPanel.addComponent(loginBox);
        contentPanel.addComponent(loginPanel.setLayoutData(GridLayout.createHorizontallyFilledLayoutData()));

        Panel fullNamePanel = new Panel(new LinearLayout(Direction.VERTICAL));
        Label fullNameLabel = new Label("Pełne imię:");
        fullNamePanel.addComponent(fullNameLabel);
        fullNameBox = new TextBox(new TerminalSize((int)((float)getSize().getColumns() / 2.5), 1));
        fullNamePanel.addComponent(fullNameBox);
        contentPanel.addComponent(fullNamePanel.setLayoutData(GridLayout.createHorizontallyFilledLayoutData()));

        Panel channelPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        Label channelLabel = new Label("Kanał: ");
        channelPanel.addComponent(channelLabel);
        channelBox = new TextBox(new TerminalSize((int)((float)getSize().getColumns() / 2.5), 1));
        channelPanel.addComponent(channelBox);
        contentPanel.addComponent(channelPanel.setLayoutData(GridLayout.createHorizontallyFilledLayoutData()));

        contentPanel.addComponent(new EmptySpace().setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2)));

        Panel buttonPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        Button exitButton = new Button("Wyjście", this::close);
        buttonPanel.addComponent(exitButton);
        Button submitButton = new Button("Połącz", () -> {
            returnVal = 1;
            close();
        });
        buttonPanel.addComponent(submitButton);
        contentPanel.addComponent(buttonPanel
                .setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.END, GridLayout.Alignment.CENTER,
                        true, false,
                        2, 1)));

        setComponent(contentPanel);

        returnVal = 0;
    }

    public String getNickname() {
        return nicknameBox.getText();
    }

    public String getLogin() {
        return loginBox.getText();
    }

    public String getFullName() {
        return fullNameBox.getText();
    }

    public String getChannel() {
        return channelBox.getText();
    }
}
