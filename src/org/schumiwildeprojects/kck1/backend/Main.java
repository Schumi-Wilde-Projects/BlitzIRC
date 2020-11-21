package org.schumiwildeprojects.kck1.backend;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static BufferedReader reader;
    public static BufferedWriter writer;
    public static String channel;
    public Socket socket;

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        String serverURL = "irc.freenode.net";
        int port = 6667;

        System.out.print("Wpisz nick: ");
        String nick = scanner.nextLine();

        System.out.print("Wpisz login: ");
        String login = scanner.nextLine();

        System.out.print("Wpisz pełne imię: ");
        String name = scanner.nextLine();

        Server server = new Server();
        Thread serverThread = new Thread(server);

        String pass = ""; // do identyfikacji
        String line1 = " ";
        Scanner sc = new Scanner(System.in);

        // Nazwa kanału
        System.out.print("Wpisz nazwę kanału (poprzedzoną hashtagiem): ");
        channel = scanner.nextLine();

        // Łączenie z serwerem
        Socket socket = new Socket(serverURL, port);
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
                return;
            }
        }

        // Wyślij wiadomość do serwera o dołączenie
        writer.write("JOIN " + channel + "\r\n");
        writer.flush( );

        serverThread.start();
        while (true) {
            line1 = sc.nextLine();
            if (line1.charAt(0) == '/') {
                writer.write( line1.substring(1) + "\r\n");
                writer.flush();
            } else {
                writer.write("PRIVMSG " + channel +" :" + line1 + "\r\n");
                writer.flush();
            }
        }
    }
    @Override
    @SuppressWarnings("FinalizeDeclaration")
    protected void finalize() throws Throwable {
        super.finalize();

        try{
            socket.close();
        } catch (IOException e) {
            System.out.println("Could not close socket");
            System.exit(-1);
        }
    }
}

class Server implements Runnable {

    String line;
    Main mainClass = new Main();

    public String readLines() throws Exception {
        return (Main.reader.readLine());
    }

    public void run() {
        try {
            // Keep reading lines from the server.
            while ((line = readLines()) != null) {
                if (line.toLowerCase().startsWith("PING ")) {
                    try {
                        // Odpowiedź na pingi ze strony serwera
                        Main.writer.write("PONG " + line.substring(5) + "\r\n");
                        Main.writer.write("PRIVMSG " + Main.channel + " :I got pinged!\r\n");
                        Main.writer.flush();
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