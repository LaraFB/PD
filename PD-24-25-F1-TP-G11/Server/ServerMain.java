package pt.isec.pd.trabalho_pratico.Server;
import pt.isec.pd.trabalho_pratico.Server.BackupServer.BackUpServer;

import java.io.File;

public class ServerMain {
    public static void main(String[] args) {
        switch(args[0]) {
            case "server":
                Server server = new Server();
                server.iniciarServer();
                break;
            case "backup":
                if (args.length < 2) {
                    System.out.println("Falta o caminho para o ficheiro de backup da base de dados");
                    return;
                }
                String diretorioPath = args[1];
                File diretorio = new File(diretorioPath);

                if (!diretorio.exists()) {
                    System.out.println("Erro: A pasta especificada não existe. Por favor, crie-a antes de iniciar.");
                    System.exit(1);
                }

                if (!diretorio.isDirectory()) {
                    System.out.println("Erro: O caminho especificado não é uma pasta.");
                    System.exit(1);
                }

                File[] ficheiros = diretorio.listFiles();
                if (ficheiros != null && ficheiros.length > 0) {
                    System.out.println("Erro: A pasta não está vazia. O servidor de backup não pode ser iniciado.");
                    System.exit(1);
                }

                System.out.println("A pasta está vazia. O servidor de backup pode ser iniciado.");
                BackUpServer backUpServer = new BackUpServer(diretorioPath);
                backUpServer.startBackUpSv();

                break;
            default:
                System.out.println("Escolhe o modo de execução: 'server', 'backup'");
        }
    }
}
