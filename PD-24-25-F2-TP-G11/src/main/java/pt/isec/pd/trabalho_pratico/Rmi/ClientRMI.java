package pt.isec.pd.trabalho_pratico.Rmi;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Scanner;

public class ClientRMI extends UnicastRemoteObject implements ObserverInterface {
    protected ClientRMI () throws RemoteException {
    }

    @Override
    public void stateChanged(String update) throws RemoteException {
       System.out.println("\n-->" + update);
       System.out.print("Enter your choice : ");
    }

    public static void main(String[] args) {
        clearConsole();
        List<String> mensagem = List.of();
        String registration;
        String registry= "localhost";

        if (args.length >= 1)
            registry = args[0];

        registration = "rmi://" + registry + "/ServerService";

        try {
            //pedidos
            ServerInterface serverService = (ServerInterface) Naming.lookup(registration);
            //observer
            serverService.addObserver(new ClientRMI());

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("=======================================");
                System.out.println("              MAIN MENU                ");
                System.out.println("=======================================");
                System.out.println("  1. Groups List                       ");
                System.out.println("  2. Users List                        ");
                System.out.println("  3. Exit                              ");
                System.out.println("=======================================");
                System.out.print("Enter your choice : ");
                String command = scanner.nextLine();

                switch (command) {
                    case "1" -> {
                        //clearConsole();
                        System.out.println("\nList of Groups : \n");
                        mensagem = serverService.getGroups();
                    }
                    case "2" -> {
                        //clearConsole();
                        System.out.println("\nList of Users : \n");
                        mensagem = serverService.getUsers();
                    }
                    case "3" -> {
                        //clearConsole();
                        System.out.println("Exiting...");
                        System.exit(0);
                    }
                    default -> System.out.println("Invalid command. Try again.");
                }
                mensagem.forEach(System.out::println);
                System.out.println("\n\n");
            }

        } catch (Exception e) {
            System.out.println("Error connecting to server: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void clearConsole() {
        try {
            if (System.getProperty("os.name").contains("Windows")) new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            else new ProcessBuilder("clear").inheritIO().start().waitFor();
        } catch (Exception e) {
            System.out.println("Erro ao clear Console: " + e.getMessage());
        }
    }
}
