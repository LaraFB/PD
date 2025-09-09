package pt.isec.pd.trabalho_pratico.Server.BackupServer;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class BackUpServer {

    private static final String MULTICAST_ADDRESS = "230.44.44.44";
    private static final int MULTICAST_PORT = 4444;
    private static final int TIMEOUT = 30000;
    private static final int MAX_SIZE = 4000;


    private static String diretoria;
    private int dbVersao;
    private File copiaBD;
    private File ficheiroBDOriginal;


    public BackUpServer(String diretoria) {
        this.diretoria = diretoria;
        this.dbVersao = 0;
        copiaBD = new File(diretoria + "/copiaBD.db");
        ficheiroBDOriginal = new File("BaseDados/database.db");
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

    public void startBackUpSv() {
        clearConsole();
        try
        {
            MulticastSocket socket = new MulticastSocket(MULTICAST_PORT);
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            socket.joinGroup(group);
            socket.setSoTimeout(TIMEOUT);

            byte[] buffer = new byte[MAX_SIZE];

            while(true)
            {
                try{
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String recebido = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("Recebido: " + recebido);

                    processaHeartbeat(recebido);

                } catch (IOException e) {
                    System.out.println("Nenhum heartbeat recebido\n Fechar o servidor :)");
                    break;
                }
            }
            socket.leaveGroup(group);
            socket.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private void processaHeartbeat(String recebido)
    {
        String[] parts = recebido.split("\\|");
        String queryAtualiza = null;
        int novaVersao = -1;
        for (String part : parts) {
            if (part.trim().startsWith("DB Versão:")) {
                novaVersao = Integer.parseInt(part.split(":")[1].trim());
            } else if (part.trim().startsWith("Query:")) {
                queryAtualiza = part.substring("Query:".length()).trim();
            }
        }

        if(novaVersao > dbVersao)
        {
            if(queryAtualiza != null && !queryAtualiza.isEmpty())
            {
                System.out.println("Nova versao com query de autalizacao.");
                atualizaComQuery(queryAtualiza, novaVersao);
            }
            else
            {
                System.out.println("Nova versao sem query de autalizacao.");
                atualizaSemQuery(novaVersao);
            }
            copiadeBaseDados();
        }
        else
        {
            System.out.println("Base de Dados já está atualizada: " + dbVersao);
        }


    }
    private void atualizaComQuery(String queryAtualiza, int novaVersao) {
        try (FileWriter writer = new FileWriter(copiaBD, true))
        {
            if(!copiaBD.exists())
            {
                System.out.println("Criar o ficheiro incial da Base de Dados");
                criarFicheiroBD(novaVersao);
            }
            writer.write("Versão: " + novaVersao + "\n");
            writer.write("Query Aplicada: "+queryAtualiza + "\n");
            System.out.println("Ficheiro atualizadao com a nova versão e query aplicada.");
            dbVersao = novaVersao;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    private void atualizaSemQuery(int novaVersao) {
        try(FileWriter write = new FileWriter(copiaBD, true))
        {
            if(!copiaBD.exists())
            {
                System.out.println("Criar o ficheiro incial da Base de Dados");
                criarFicheiroBD(novaVersao);
            }
            write.write("Atualiado para a versão: " + novaVersao + "\n");
            System.out.println("Versão atualizada para: " + novaVersao);
            dbVersao = novaVersao;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void criarFicheiroBD(int versaoIncial) {
        try(FileWriter writer = new FileWriter(copiaBD, true))
        {
            writer.write("Versão: " + versaoIncial + "\n");
            System.out.println("Ficheiro criado com a versão inicial: " + versaoIncial);
            dbVersao = versaoIncial;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void copiadeBaseDados() {
        try (FileInputStream fileInputStream = new FileInputStream(ficheiroBDOriginal);
             OutputStream out = new FileOutputStream(copiaBD))
        {
            byte[] buffer = new byte[MAX_SIZE];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            System.out.println("Cópia concluida: " + copiaBD.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    // private void updateBackupDatabase(int newVersion) {
    //        // Logic to update your backup database based on the new version
    //        System.out.println("Atualizando a BD de backup para versão: " + newVersion);
    //        // Execute SQL statements here to synchronize or update records in your backup database


}
