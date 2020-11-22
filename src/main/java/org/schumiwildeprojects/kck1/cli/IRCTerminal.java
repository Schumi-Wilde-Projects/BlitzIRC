package org.schumiwildeprojects.kck1.cli;

import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import org.schumiwildeprojects.kck1.backend.ConnectionState;
import org.schumiwildeprojects.kck1.backend.Main;
import org.schumiwildeprojects.kck1.backend.Server;
import org.schumiwildeprojects.kck1.cli.states.LoginState;
import org.schumiwildeprojects.kck1.cli.states.State;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

// singleton (tylko jedno okno główne na aplikację)
public class IRCTerminal {
    private static IRCTerminal instance;
    private WindowBasedTextGUI textGUI;
    private final Terminal terminal;
    private final Screen screen;
    public static String currentChannel;
    public static BufferedReader reader;
    public static BufferedWriter writer;
    private Thread serverThread, writeThread;
    private ConnectionThread connectionRunnable;
    private Thread connectionThread, resultThread;
    private Socket socket;
    private State state;
    public static volatile boolean appIsOpen = true;

    class ConnectionThread implements Runnable {

        private final String nick;
        private final String login;
        private final String fullName;
        private final String channel;
        private ConnectionState state;

        public ConnectionThread(String nick, String login, String fullName, String channel) {
            this.nick = nick;
            this.login = login;
            this.fullName = fullName;
            this.channel = channel;
        }
        @Override
        public void run() {
            try {
                state = connect(nick, login, fullName, channel);
            } catch (IOException e) {
                Logger.getLogger(LoginWindow.class.getName()).log(Level.SEVERE, null, e);
                System.exit(-1);
            }
        }

        public ConnectionState getResult() {
            return state;
        }
    }

    private static class WriteThread implements Runnable {

        @Override
        public void run() {
            String line1 = " ";
            Scanner sc = new Scanner(System.in);
            while (appIsOpen) {
                /*
                line1 = sc.nextLine();
                if (line1.charAt(0) == '/') {
                    try {
                        writer.write( line1.substring(1) + "\r\n");
                        writer.flush();
                    } catch (IOException e) {
                        Logger.getLogger(IRCTerminal.class.getName()).log(Level.SEVERE, null, e);
                    }
                } else {
                    try {
                        writer.write("PRIVMSG " + currentChannel +" :" + line1 + "\r\n");
                        writer.flush();
                    } catch (IOException e) {
                        Logger.getLogger(IRCTerminal.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
                */
                try {
                    //noinspection BusyWait
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public ConnectionState connect(String nick, String login, String name, String channel) throws IOException {
        Server server = new Server();
        serverThread = new Thread(server);

        String pass = ""; // do identyfikacji
        Scanner sc = new Scanner(System.in);

        // Łączenie z serwerem
        socket = new Socket(Main.SERVER_URL, Main.PORT);
        writer = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream()));
        reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

        // Logowanie się do serwera
        writer.write("NICK " + nick + "\r\n");
        writer.write("USER " + login + " 8 * : " + name + "\r\n");
        writer.write("msg nickserv identify " + pass + "\r\n");
        writer.flush();

        // Read lines from the server until it tells us we have connected.
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("004")) { // Kod oznaczający połączenie
                // Jesteśmy zalogowani
                break;
            }
            else if (line.contains("433")) { // Nazwa użytkownika jest już zajęta
                System.out.println("Nickname is already in use.");
                return ConnectionState.USERNAME_EXISTS;
            }
        }

        // Wyślij wiadomość do serwera o dołączenie
        writer.write("JOIN " + channel + "\r\n");
        writer.flush( );

        currentChannel = channel;

        serverThread.start();
        writeThread = new Thread(new WriteThread());
        writeThread.start();

        return ConnectionState.SUCCESSFUL;
    }

    public static IRCTerminal getInstance() throws IOException {
        if(instance == null)
            instance = new IRCTerminal();
        return instance;
    }

    private IRCTerminal() throws IOException {
        DefaultTerminalFactory factory = new DefaultTerminalFactory();
        terminal = factory.createTerminal();
        terminal.enterPrivateMode();
        terminal.setCursorVisible(false);
        screen = new TerminalScreen(terminal);
        screen.startScreen();
        textGUI = new MultiWindowTextGUI(screen);
    }

    private static class TimeoutTask extends TimerTask {

        @Override
        public void run() {
            System.exit(0);
        }
    }

    // zwraca terminal Lanterna który jest wyświetlany w oknie
    public Terminal getTerminal() {
        return terminal;
    }

    public void start() throws IOException {
        changeState(new LoginState());
        while(state.getWindow() != null) {
            show();
        }
        close();
        appIsOpen = false;
        Timer timer = new Timer();
        timer.schedule(new TimeoutTask(), 5000);
        while(writeThread.isAlive() || serverThread.isAlive() || connectionThread.isAlive() || resultThread.isAlive()) {
            try {
                //noinspection BusyWait
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        timer.cancel();

        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Could not close socket");
            System.exit(-1);
        }
    }

    public void changeState(State state) {
        this.state = state;
    }

    void show() throws IOException {
        textGUI = new MultiWindowTextGUI(screen);
        textGUI.addWindow(state.getWindow());
        BasicLateInitWindow window = state.getWindow();
        state.getWindow().start();
        textGUI.waitForWindowToClose(state.getWindow());
        // zwraca jedyne okno w TextGUI
        int retVal = window.returnValue();
        switch (retVal) {
            case 0 -> state.onClose();
            case 1 -> state.onSubmit();
            default -> throw new IllegalStateException("Wrong window return value");
        }
    }

    public ConnectionThread getConnectionRunnable() {
        return connectionRunnable;
    }

    public Thread getConnectionThread() {
        return connectionThread;
    }

    public void initializeConnectionThread(String nick, String login, String fullName, String channel) {
        currentChannel = channel;
        connectionRunnable = new ConnectionThread(nick, login, fullName, channel);
        connectionThread = new Thread(connectionRunnable);
        connectionThread.start();
    }

    public void initializeResultThread(Runnable func) {
        resultThread = new Thread(func);
        resultThread.start();
    }

    public void close() throws IOException {
        terminal.exitPrivateMode();
        terminal.close();
    }
}
