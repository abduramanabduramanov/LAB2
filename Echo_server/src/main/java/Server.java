import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.*;


public class Server {
    public static int PORT = 8080;
    Logger log = Logger.getRootLogger(); //подключаем log4j
    public static void main(String[] args) {
        if (args.length>0){
            int NewPort=Integer.parseInt(args[0]);
            if(NewPort>=1024 && NewPort<=65535)
                PORT = NewPort;
        }
        new Server().go();
    }

    public void go() {
        log.info("server start"); //запись о старте сервера
        log.info("server port " + PORT);
        try {
            ServerSocket server = new ServerSocket(PORT); //порт на котором запустится сервер
            System.out.println("Server start.");

            while (true) {
                Socket clientSocket = server.accept();  //принимаем запрос на подключения от клиента
                log.debug("client accept " + clientSocket.toString());
                Thread thread = new Thread(new Talker(clientSocket));//выделяем поток для работы сервера с коиентом
                thread.start(); //запускаем поток
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex);
        }
    }

    class Talker implements Runnable {

        BufferedReader breader;
        Socket sock;

        Talker(Socket clientSocket) {
            try {
                sock = clientSocket;
                breader = new BufferedReader(new InputStreamReader(sock.getInputStream())); //создаем потк чтения из сокета клиента
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public void run() {
            String message;
            try {
                while (!sock.isClosed()) { //слушаем пока клиент не напишит
                    message = breader.readLine();
                            System.out.println("serv read " + message);
                    String[] comand =  message.split(" ");
                    switch (comand[0]){
                        case "disconnect":
                            log.debug("client diconnect" + sock.toString());
                            tellEveryone("disconnect");
                            breader.close();
                            sock.close();
                            break;
                        case "send":
                            log.info("client send "+message.substring(5));
                            tellEveryone(message.substring(5));
                            break;
                        case "logLevel":
                            log.info("logLevel set" + comand[1]);
                            switch (comand[1]){
                                case "off":
                                    log.setLevel(Level.OFF);
                                    break;
                                case "fatal":
                                    log.setLevel(Level.FATAL);
                                    break;
                                case "error":
                                    log.setLevel(Level.ERROR);
                                    break;
                                case "warn":
                                    log.setLevel(Level.WARN);
                                    break;
                                case "info":
                                    log.setLevel(Level.INFO);
                                    break;
                                case "debug":
                                    log.setLevel(Level.DEBUG);
                                    break;
                                case "trace":
                                    log.setLevel(Level.TRACE);
                                    break;
                                case "all":
                                    log.setLevel(Level.ALL);
                                    break;
                                default:
                            }
                            break;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public void tellEveryone(String message) {
            try {
                PrintWriter pw = new PrintWriter(sock.getOutputStream());
                pw.println(message); //отправляем клиентy
                pw.flush();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

        }
    }
}