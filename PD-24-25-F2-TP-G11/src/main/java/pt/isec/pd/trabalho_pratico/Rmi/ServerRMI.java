package pt.isec.pd.trabalho_pratico.Rmi;

import org.springframework.boot.SpringApplication;
import pt.isec.pd.trabalho_pratico.BaseDados.BDController;
import pt.isec.pd.trabalho_pratico.Api.TrabalhoPraticoApplication;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class ServerRMI extends UnicastRemoteObject implements ServerInterface {
    private final BDController bdControler;
    private static ServerRMI instance;
    List<ObserverInterface> observers = new ArrayList<>();

    private ServerRMI() throws RemoteException {
        super();
        bdControler = new BDController();
        instance = this;
    }

    public static void main(String[] args) throws RemoteException {
        ServerRMI serverRMI = new ServerRMI();
        String registry = "localhost";

        if(instance == null) {
            System.out.println("[ERROR] Lauching server");
            System.exit(1);
        }else{
            if (args.length >= 1)
                registry = args[0];
            serverRMI.iniciarServer(registry);
            SpringApplication.run(TrabalhoPraticoApplication.class, args);
        }
    }

    private void iniciarServer(String registry) throws RemoteException {
        try {
            String registration = "rmi://" + registry + "/ServerService";
            try {
                LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
            } catch (RemoteException e) {
                System.out.println("[ERROR] RMI Registry already exists or cannot be created: " + e.getMessage());
            }
            Naming.rebind(registration, this);
        } catch (Exception e) {
            System.out.println("[ERROR] Lauching server " + e.getMessage());
            System.exit(1);
        }
    }

    public static ServerRMI getInstance() throws RemoteException {
        if(instance==null)
            instance = new ServerRMI();
        return instance;
    }

    //Get from BD
    private List<String> getGruposTodos (){
      return bdControler.getListBDQuery("SELECT nome_grupo FROM Grupos");
    }
    private List<String> getUsernames (){
        return bdControler.getListBDQuery("SELECT email FROM Utilizador");
    }
    private List<String> getGruposPertence(String username){
        return bdControler.getListBDQuery("SELECT Nome FROM Elementos_Grupo WHERE Nome_Elementos = '" + username + "'");
    }
    private List<String> getExpensesPorGrupo(String grupo){
        return bdControler.getListDespesasBD("SELECT descricao,valor,data,quem_pagou_total,dividido_por,valor_individual FROM Despesas WHERE nome_grupo = '" + grupo + "' ");
    }
    private String getNome(String username){
        return bdControler.getBDQuery("SELECT nome FROM Utilizador WHERE email = '" + username.toLowerCase() + "'");
    }
    private String getPassword(String username){
        return bdControler.getBDQuery("SELECT password FROM Utilizador WHERE email = '" + username + "'");
    }
    private boolean isThereEmail(String email){
        return bdControler.getBDQuery("SELECT email FROM Utilizador WHERE email = '" + email + "'").equals(email);
    }
    private boolean isThereGroup(String grupo){
        return bdControler.getBDQuery("SELECT nome_grupo FROM Grupos WHERE nome_grupo = '" + grupo + "'").equals(grupo);
    }
    private boolean isThereExpense(String descricao, String grupo){
        return !bdControler.getBDQuery("SELECT descricao FROM Despesas WHERE descricao = '" +descricao + "' AND nome_grupo = '" + grupo + "'").isEmpty();
    }

    //Verificações
    public String verificaSignUp(String email, String telemovel, String password, String nome) {
        if (telemovel.length() != 9 || (!telemovel.startsWith("91") && !telemovel.startsWith("92") && !telemovel.startsWith("93") && !telemovel.startsWith("96")))
            return "Invalid phone number!";
        if (getNome(email.toLowerCase()).isEmpty()) {
            bdControler.updateBD("INSERT INTO Utilizador (email,telefone,password,nome) VALUES ('" + email.toLowerCase() + "','" + telemovel + "','" + password + "','" + nome + "')");
            return "Welcome " + getNome(email.toLowerCase()) + "!";
        }
        return "This email already exists, please use another email!";
    }
    public String verificaLogin (String username, String password) {
        if(!isThereEmail(username.toLowerCase()))
            return "There's no such email!";
        else if(getPassword(username.toLowerCase()).equals(password))
            return "Welcome " + getNome(username.toLowerCase()) + "!";
        else
            return "Password incorreta.";
    }
    public String apagarDespesa (String descricao, String grupo, String email) {
        if(!isThereGroup(grupo))
            return "There's no such group!";
        if(!getGruposPertence(email.toLowerCase()).contains(grupo))
            return "You don't belong to this group!";
        if(!isThereExpense(descricao,grupo))
            return "There's no such expense!";
        bdControler.updateBD("DELETE FROM Despesas WHERE nome_grupo = '" + grupo + "' AND descricao = '" + descricao + "'");
        return "Expense deleted";
    }
    public String getListGruposPertence (String email) {
        StringBuilder gruposFormatado = new StringBuilder();

        for (String grupo : getGruposPertence(email.toLowerCase())) {
            gruposFormatado.append(grupo);
            gruposFormatado.append("\n");
        }
        return gruposFormatado.toString();
    }
    public String getListExpensesPertence (String email, String grupo) {
        StringBuilder expensesFormatado = new StringBuilder();
        if(!isThereGroup(grupo))
            return "There's no such group!";
        if(!getGruposPertence(email.toLowerCase()).contains(grupo))
            return "You don't belong to this group!";

        List <String> expenses = getExpensesPorGrupo(grupo);
        if(expenses.isEmpty())
            return "There's no expenses in this group!";

        for (String expense : expenses)
            expensesFormatado.append(expense).append(" ");
        return expensesFormatado.toString();
    }
    public String adicionarDespesa(String username,String grupo,String descricao, Float valor, String data, String quemPagouTotal,String divididoPor){
        if(valor < 0)
            return "Invalid value!";
        if(!verificaData(data))
            return "Invalid data!";
        if(!isThereGroup(grupo))
            return "There's no such group!";
        if(isThereExpense(descricao,grupo))
            return "This expense already exists!";
        if(!getGruposPertence(username.toLowerCase()).contains(grupo))
            return "You don't belong to this group!";
        if(!getGruposPertence(quemPagouTotal.toLowerCase()).contains(grupo))
            return quemPagouTotal + " doesn't belong to this group!";
        for (String pessoa : divididoPor.split(" ")) {
            if(!getGruposPertence(pessoa.toLowerCase()).contains(grupo))
                return pessoa + "  doesn't belong to the group!";
        }
        float valorIndividual = valor/divididoPor.split(" ").length;
        bdControler.updateBD("INSERT INTO Despesas (nome_grupo,descricao,valor,data,quem_pagou_total,dividido_por,pago_por,valor_individual) VALUES ('" + grupo + "','"+ descricao + "','" + valor + "','" + data + "','"+ quemPagouTotal + "','"+ divididoPor + "','" + quemPagouTotal +"','"+ valorIndividual +"')" );
        return "Expense added successfully!";
    }

    private boolean verificaData(String data){
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        try {
            LocalDate.parse(data, format);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    //notificar
    public void notificar(String msg) {
        List<ObserverInterface> disconnectedObservers = new ArrayList<>();
        for (ObserverInterface observer : observers) {
            try {
                observer.stateChanged(msg);
            } catch (RemoteException e) {
                System.out.println("Observer disconnected");
                disconnectedObservers.add(observer);
            }
        }
        observers.removeAll(disconnectedObservers);
    }

    // ServerInterface
    @Override
    public List<String> getUsers() throws RemoteException {
        return getUsernames();
    }
    @Override
    public List<String> getGroups() throws RemoteException {
        return getGruposTodos();
    }
    @Override
    public void addObserver(ObserverInterface observer) throws RemoteException {
        observers.add(observer);
    }
}