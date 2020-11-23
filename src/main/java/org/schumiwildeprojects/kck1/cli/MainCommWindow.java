package org.schumiwildeprojects.kck1.cli;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import org.schumiwildeprojects.kck1.cli.widgets.MessageInputTextBox;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainCommWindow extends BasicLateInitWindow {
    private TextBox messages;
    private IRCTerminal terminal;
    private MessageInputTextBox msgInput;
    private TextBox userList;
    private static MainCommWindow instance;
    public static MainCommWindow getInstance() {
        if(instance == null)
            restartWindow();
        return instance;
    }

    private class Server implements Runnable {

        String line;

        public String readLines() throws Exception {
            return (IRCTerminal.reader.readLine());
        }

        public void run() {
            try {
                // Keep reading lines from the server.
                while (IRCTerminal.appIsOpen && (line = readLines()) != null) {
                    if (line.toLowerCase().startsWith("ping ")) {
                        try {
                            // Odpowiedź na pingi ze strony serwera
                            IRCTerminal.writer.write("PONG " + line.substring(5) + "\r\n");
                            IRCTerminal.writer.write("PRIVMSG " + IRCTerminal.currentChannel + " :I got pinged!\r\n");
                            IRCTerminal.writer.flush();
                        } catch (IOException ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else if(line.split(" ")[1].toLowerCase().equals("join")) {
                        messages.addLine("User " + line.substring(1, line.indexOf("!~")) + " joined the chat");
                    } else if(line.split(" ")[1].toLowerCase().equals("353")) {
                        userList.setText("");
                        String[] splits  = line.split(" ");
                        for(int i = 5; i < splits.length; i++) {
                            int j = 0;
                            while(splits[i].substring(j, j+1).matches(".*[:@+].*")) {
                                j++;
                            }
                            userList.addLine(splits[i].substring(j));
                        }
                    } else if(line.split(" ")[1].toLowerCase().equals("privmsg")){
                        String username = line.substring(1, line.indexOf("!~"));
                        messages.addLine(username + ": " + line.split(" ")[3].substring(1));
                    } else if(line.split(" ")[1].toLowerCase().equals("quit")) {
                        String username = line.substring(1, line.indexOf("!~"));
                        messages.addLine("User " + username + " left the chat");
                        IRCTerminal.writer.write("NAMES " + IRCTerminal.currentChannel + "\r\n");
                        IRCTerminal.writer.flush();
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }


    static void restartWindow() {
        instance = new MainCommWindow("Komunikacja na " + IRCTerminal.currentChannel);
    }

    private MainCommWindow(String title) {
        super(title);
        setFixedSize(new TerminalSize(180, 42));
    }

    @Override
    public void start() throws IOException {
        TerminalSize termSize = getTextGUI().getScreen().getTerminalSize();
        setPosition(new TerminalPosition((termSize.getColumns() - getSize().getColumns()) / 2, (termSize.getRows() - getSize().getRows()) / 2));
        terminal = IRCTerminal.getInstance();
        Panel contentPanel = new Panel(new LinearLayout(Direction.VERTICAL));

        Panel mainClientPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        Panel mainMessagePanel = new Panel(new LinearLayout(Direction.VERTICAL));
        messages = new TextBox(new TerminalSize(160, 38));
        messages.setReadOnly(true);
        msgInput = new MessageInputTextBox(new TerminalSize(160, 1));
        msgInput.addSubmitListener(() -> {
            String line1 = msgInput.getText();
            if(line1.equals("")) return;
            if (line1.charAt(0) == '/') {
                try {
                    IRCTerminal.writer.write( line1.substring(1) + "\r\n");
                    IRCTerminal.writer.flush();
                } catch (IOException e) {
                    Logger.getLogger(IRCTerminal.class.getName()).log(Level.SEVERE, null, e);
                }
            } else {
                try {
                    IRCTerminal.writer.write("PRIVMSG " + IRCTerminal.currentChannel +" :" + line1 + "\r\n");
                    IRCTerminal.writer.flush();
                    messages.addLine(IRCTerminal.nickname + ": " + line1);
                } catch (IOException e) {
                    Logger.getLogger(IRCTerminal.class.getName()).log(Level.SEVERE, null, e);
                }
            }
            msgInput.setText("");
        });

        mainMessagePanel.addComponent(messages.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill)));
        mainMessagePanel.addComponent(new EmptySpace());
        mainMessagePanel.addComponent(msgInput.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill)));
        mainClientPanel.addComponent(mainMessagePanel.setLayoutData(GridLayout.createHorizontallyFilledLayoutData()));

        userList = new TextBox(new TerminalSize(20, 40));
        userList.setReadOnly(true);
        mainClientPanel.addComponent(userList.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill)));

        contentPanel.addComponent(mainClientPanel.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill)));
        contentPanel.addComponent(new EmptySpace());

        Button buttonExit = new Button("Wyjście", this::close);
        contentPanel.addComponent(buttonExit.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Center)));

        setComponent(contentPanel);

        messages.addLine("[BlitzIRC] Witaj na kanale " + IRCTerminal.currentChannel + ". Wiadomości zaczynające się od ukośnika");
        messages.addLine("[BlitzIRC] to komendy serwera IRC. Używaj ich tylko wtedy, kiedy wiesz, co robisz!");

        terminal.initializeServerThread(new Server());

        returnVal = 0;
    }

    public void leaveChannel() throws IOException {
        IRCTerminal.writer.write("QUIT :Left the channel\r\n");
        IRCTerminal.writer.flush();
    }
}
