package pt.isec.pd.trabalho_pratico.Client;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client {
    private PrintStream out;
    private BufferedReader in;
    private Socket socket;
    private int port;
    private static final int TIMEOUT = 60000;
    private boolean login = false;
    private String serverResponse;
    private static Client instance;
    private BooleanProperty update = new SimpleBooleanProperty(false);

    public static Client getInstance() {
        if (instance == null) {
            instance = new Client();
        }
        return instance;
    }

    public void setPort(int port) {
        this.port = port;
    }
    public boolean connect() {
        try{
            socket = new Socket("localhost", port);
            socket.setSoTimeout(TIMEOUT);
            out = new PrintStream(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("A estabelecer ligação com o Server.");
            out.flush();
            System.out.println(in.readLine());
            serverUpdatesThread();
            return true;
        } catch (IOException e) {
            System.out.println("[ERRO] Ao estabelecer conexão com o servidor!");
            return false;
        }
    }
    private void serverUpdatesThread() {
        new Thread(() -> {
            try (MulticastSocket socket = new MulticastSocket(4444)) {
                InetAddress group = InetAddress.getByName("230.44.44.44");
                socket.joinGroup(group);
                byte[] buffer = new byte[256];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                    if (message.equals("Updated BD")) {
                        update.set(true);
                    }
                }
            } catch (IOException e) {
                System.out.println("[ERRRO] Ao atualizar a UI!");
            }
        }).start();
    }
    public BooleanProperty needsUpdate() {
        return update;
    }
    public void resetUpdate() {
        update.set(false);
    }

    public String receberEnviar (String sendRequest){
        try{
            out.println(sendRequest);
            out.flush();
            serverResponse = in.readLine();
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
        return serverResponse;
    }
    public String receberEnviarLoginSignUp (String sendRequest){
        try{
            out.println(sendRequest);
            out.flush();
            serverResponse = in.readLine();
        }catch (Exception e) {
            return "Conexão terminada com o servidor.";

        }
        return serverResponse;
    }

    public boolean isLogin () {
        return login;
    }
    public boolean isYou (String nome){
        return receberEnviar("IsYou " + nome).equals("true");
    }
    public boolean expenseIsPaid(String atual) {
        return receberEnviar("ExpenseIsPaid " + atual).equals("true");
    }
    public boolean youPaid(String atual) {
        return receberEnviar("YouPaid " + atual).equals("true");
    }

    public boolean mensagemAceitarInvite (String ngrupo){
        return receberEnviar(("Aceitar convite " + ngrupo)).equals("true");
    }
    public boolean mensagemRejeitarConvite (String ngrupo){
        return receberEnviar(("Rejeitar convite " + ngrupo)).equals("true");
    }
    public boolean mensagemChangeName(String old_name, String new_name){
        serverResponse = receberEnviar("Editar grupo " + old_name + "-" + new_name);
        return serverResponse.contains("Nome do grupo mudado com sucesso!");
    }
    public String mensagemLogin(String email, String password) {
        serverResponse = receberEnviarLoginSignUp((email + " " + password));
        login = serverResponse.contains("Bem vindo ");
        return serverResponse;
    }
    public String mensagemSigUp(String email, String telemovel, String password, String nome) {
        serverResponse = receberEnviarLoginSignUp((email + " " +  telemovel + " " + password + " " + nome));
        login = serverResponse.contains("Bem vindo ");
        return serverResponse;
    }
    public String mensagemEditAccount(String email, String telemovel, String password, String nome) {
        return receberEnviar(("Edit Account: "+ email + " " +  telemovel + " " + password + " " + nome));
    }
    public String mensagemAddGrupo (String nome){
        return receberEnviar(("Criar grupo " + nome));
    }
    public String mensagemInvite (String ngrupo, String npessoa){
        return receberEnviar(("Criar convites " + ngrupo + '-' + npessoa));
    }
    public String mensagemLeaveGroup(String grupo_nome){
        return receberEnviar("Sair do grupo " + grupo_nome);
    }
    public String mensagemDeleteGroup(String grupo_nome){
        return receberEnviar("Delete grupo " + grupo_nome);
    }
    public String mensagemAddExpense(String grupo_nome, String descricao, float preco, String data, String paidBy, String pessoasSplit){
        return receberEnviar("Adicionar Despesa " + grupo_nome + "-" + descricao + "-" + preco + "-" + data + "-" + paidBy + "-" + pessoasSplit);
    }
    public String mensagemEditExpense(String oldexpense,String descricao, String valor, String data, String paidby, String spliby) {
        return receberEnviar("Editar despesa " + oldexpense + ";" + descricao + "-" + valor + "-" + data + "-" + paidby + "-" + spliby);
    }
    public String mensagemDeleteExpense(String atualExpense) {
        return receberEnviar("Eliminar despesa " + atualExpense);
    }
    public String mensagemPagarDespesa(String atualExpense) {
        return receberEnviar("Pagar despesa " + atualExpense);
    }

    //Gets
    public String[] accountInfo(){
        return receberEnviar(("Account Info")).split(",");
    }
    public String getNomesGrupoPertence(){
        return receberEnviar(("Lista de grupos"));
    }
    public String getNomesPessoas(){
        return receberEnviar("Nomes users");
    } // sem username eu
    public String getTodosNomesPessoas(){
        return receberEnviar("Todos nomes users");
    }
    public String getNomesPesoasGrupos(String grupo){
        return receberEnviar("NomesPessoasPorgrupo " + grupo);
    }
    public String getInvites(){
        return receberEnviar("Ver convites pendentes");
    }
    public String getGrupoTotal (String grupo_nome){
        return receberEnviar("Total gasto " + grupo_nome);
    }
    public String getGrupoDividas (String grupo_nome){
         return receberEnviar("Towe " + grupo_nome);
    }
    public String getGrupoPorReceber(String grupo_nome){
        return receberEnviar("Owed " + grupo_nome);
    }
    public String getDespesasPorGrupo (String grupo_nome){
       return receberEnviar("Despesas por grupo " + grupo_nome);
    }
    public String getDespesasHistorico (String grupo_nome, String tipo){
        return receberEnviar("History expense " + grupo_nome + "," + tipo);
    }
    public String getDespesasHome (String grupo_nome, String tipo){
        return receberEnviar("Home expense " + grupo_nome + "," + tipo);
    }
    public String downloadDespesas (String grupo_nome){
        return receberEnviar("Exportar despesas "+ grupo_nome);
    }
    public String cancelarPagamento(String atualExpense) {
        return receberEnviar("Cancelar pagamento " + atualExpense);
    }
    public String getNomeGrupoById(int id) {
        return receberEnviar("NomeGrupoID: " + id);
    }
    public int getIDGrupo(String groupName) {
        return Integer.parseInt(receberEnviar("ID grupo " + groupName));
    }

    public void logOut() {
        out.println("LogOut");
        out.flush();
        System.exit(0);
    }
}

