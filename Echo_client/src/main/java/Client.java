import java.io.*;
import java.net.*;
import java.util.Scanner;


public class Client extends Thread{
    public  static Socket socket = null;
    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner in = new Scanner(System.in); //сканер для чтения строки введеной пользователем;
        String host;
        int port;
        String connect;
        String adres[];
        while (true){
            System.out.println("Enter host and port");
            connect = in.nextLine();
            adres = connect.split(" ");

            if(adres.length>0 && adres.length<=3 && adres[0].equals("connect") && (adres[1].equals("127.0.0.1") || adres[1].equals("localhost"))){
                try {
                    host = adres[1];
                    port = Integer.parseInt(adres[2]);
                }
                catch (Exception e)
                {
                    System.out.println("falid port");
                    break;
                }
                if(port >=1024 && port <=65535){
                    socket = new Socket(host, port); //создаем сокет
                    Output output = new Output(socket);
                    Input input = new Input(socket);
                    output.join();
                    input.join();
                }
            }
        }
    }
    public static void downService() {
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {}
    }
}

class Output extends Thread{ //поток отправки сообщений на сервер
    private Socket socket;
    private Scanner in;
    private PrintWriter out;

    public Output(Socket s) throws IOException{
        System.out.println("output start");
        socket = s;
        in = new Scanner(System.in); //сканер для чтения строки введеной пользователем
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true); // поток отправки сообщения на сервер
        start();

    }

    public void run(){
        while(!socket.isClosed()){
            String data = in.nextLine(); //читаем сообщение введеное пользователем
            String[] comand =  data.split(" ");
            switch(comand[0]){
                case "send":
                    out.write(data + "\n");
                    break;
                case "logLevel":
                    if(comand.length>=2){
                        switch (comand[1]){
                            case "off":
                            case "fatal":
                            case "error":
                            case "warn":
                            case "info":
                            case "debug":
                            case "trace":
                            case "all":
                                out.write(comand[0]+" " +comand[1] + "\n"); // отправляем на сервер
                                break;
                            default:
                                help();
                                break;
                        }
                    }
                    break;
                case "help":
                    help();
                    break;
                case "quit":
                    if(data.length()!=4){
                        help();
                    }
                    else{
                        out.write("disconnect" + "\n"); // отправляем на сервер
                        out.flush();
                        Client.downService();
                        System.exit(0);
                    }
                    break;
                case "disconnect":
                    if(data.length()!=10){
                        help();
                    }
                    else{
                        out.write("disconnect" + "\n"); // отправляем на сервер
                        out.flush();
                        try {
                            sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Client.downService();
                    }
                    break;
                default:
                    help();
            }
            out.flush();
        }
    }

    void help(){
        System.out.println("[connect] [ip] [port] to connect to the server");
        System.out.println("[disconnect] to disconnect from the server");
        System.out.println("[send] [message] to receive an echo from the server");
        System.out.println("[logLevel] [ALl|DEBUG|INFO|WARN|ERROR|FATAL|OFF] to set the level of logging");
        System.out.println("[quit] to exit the program");
    }
}

class Input extends Thread{ //поток чтения сообщений из сервер
    private Socket socket;
    private BufferedReader in;
    public Input(Socket s) throws IOException{
        System.out.println("input start");
        socket = s;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // поток чтения сообщения из сервер
        start();
    }
    public void run(){
        try {
            while(!socket.isClosed()){
                String data = in.readLine();    //читаем сообщения
                if(data.equals("disconnect"))
                    in.close();
                System.out.println( data);  //выводим его
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}