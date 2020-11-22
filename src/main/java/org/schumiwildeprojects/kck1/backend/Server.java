package org.schumiwildeprojects.kck1.backend;

import org.schumiwildeprojects.kck1.cli.IRCTerminal;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements Runnable {

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
                } else {
                    // Wyświetl linię otrzymaną przez serwer
                    System.out.println(line);
                }

            }
        } catch (Exception ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
