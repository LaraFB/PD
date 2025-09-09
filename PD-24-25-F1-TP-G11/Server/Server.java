package pt.isec.pd.trabalho_pratico.Server;

import pt.isec.pd.trabalho_pratico.BaseDados.BDController;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final String MULTICAST_ADDRESS = "230.44.44.44";
    private static final int PORT = 4444;
    private static final int HEARTBEAT = 10000;
    private final BDController bdControler;

    public Server() {
        bdControler = new BDController();
    }

    public void iniciarServer(){
        clearConsole();
        bdControler.ligarBD();
        iniciarHeartBeats();
        try(ServerSocket serverSocket = new ServerSocket(1234 ))
        {
            while(true)
            {
                Socket clientSocket = serverSocket.accept();
                Thread t = new ProcessClientThread(clientSocket,this);
                t.start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clearConsole() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateBD(String query)
    {
        System.out.println("Atualizar BD " + query);
        try {
            bdControler.updateBD(query);
            enviarHeartBeat();
            updateClient();
        } catch (Exception e) {
            System.out.println("Erro ao atualizar a BD ou a enviar o Heartbeat: " + e.getMessage());
        }

    }

    private void updateClient() {
        try (MulticastSocket socket = new MulticastSocket(PORT)) {
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            byte[] buffer = "Updated BD".getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);
        } catch (IOException e) {
            System.out.println("Erro ao enviar atualizacao ao cliente: " + e.getMessage());
        }
    }

    public void iniciarHeartBeats(){
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(this::enviarHeartBeat, 0, HEARTBEAT, TimeUnit.MILLISECONDS);
    }

    private void enviarHeartBeat()
    {
        try(MulticastSocket socket = new MulticastSocket(PORT))
        {
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            String message = "Heartbeat | DB Versão: " + bdControler.getDbVersao() + " | TCP Port: " + 1234;
            byte[] buffer = message.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);
            System.out.println("Heartbeat enviado");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getBDQuery (String query){
        //System.out.println("\nVou buscar: " + query);
        return bdControler.getBDQuery(query);
    }
    public String getListarBDQuery (String query){
        //System.out.println("\nVou listar: " + query);
        return bdControler.getListarBDQuery(query);
    }
    public String getListarDespesasBDQuery (String query){
        //System.out.println("\nVou listar Despesas: " + query);
        return bdControler.getListarDespesasBDQuery(query);
    }
    public String getListaCSVBDQuery(String query) {
        //System.out.println("Vou listar CSV: " + query);
        return bdControler.getListaCSVBDQuery(query);
    }
}

