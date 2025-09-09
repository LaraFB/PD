package pt.isec.pd.trabalho_pratico.BaseDados;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BDController {
    private Connection connection;
    private int dbVersao;

    public BDController() {
        try{
//            String caminho = "C:\\Users\\diogo\\Desktop\\pd2\\trabalho_pratico\\src\\main\\java\\pt\\isec\\pd\\trabalho_pratico\\BaseDados\\database.db";
       //     String caminho = "C:\\Users\\Usuario\\OneDrive\\Ambiente de Trabalho\\ISEC\\3Ano1Semestre\\PD\\pd-tp2\\trabalho_pratico\\src\\main\\java\\pt\\isec\\pd\\trabalho_pratico\\BaseDados\\database.db";
            String caminho = "C:\\Users\\laraf\\Desktop\\PD-24-25-F2-TP-G11\\src\\main\\java\\pt\\isec\\pd\\trabalho_pratico\\BaseDados\\database.db";
            String url = "jdbc:sqlite:" + caminho;

            File dbFile = new File(caminho);
            boolean dbExists = dbFile.exists();
            connection = DriverManager.getConnection(url);

            if(!dbExists) {
                System.out.println("Não existe base de dados!\n A criar BD...");
                criarTabelas();
            }
            iniciarVersaoBD();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void criarTabelas() {
        try(Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS VersaoBD (versao_n INTEGER PRIMARY KEY)");
            statement.execute("CREATE TABLE IF NOT EXISTS Utilizador (email TEXT PRIMARY KEY, telefone INTEGER(9), password TEXT, nome VARCHAR(255))");
            statement.execute("CREATE TABLE IF NOT EXISTS Grupos (id INTEGER PRIMARY KEY AUTOINCREMENT, nome_grupo TEXT)");
            statement.execute("CREATE TABLE IF NOT EXISTS Elementos_Grupo (Nome TEXT, Nome_Elementos INTEGER, FOREIGN KEY (Nome_Elementos) REFERENCES Utilizador(email))");
            statement.execute("CREATE TABLE IF NOT EXISTS Despesas (nome_grupo INTEGER, descricao TEXT, valor REAL, data DATE, quem_pagou_total TEXT, dividido_por TEXT, pago_por TEXT, valor_individual float, FOREIGN KEY (nome_grupo) REFERENCES Elementos_Grupo(Nome), FOREIGN KEY (quem_pagou_total) REFERENCES Utilizador(email), FOREIGN KEY (dividido_por) REFERENCES Utilizador(email))");
            statement.execute("CREATE TABLE IF NOT EXISTS Convites_Grupos (nome_grupo INTEGER, nome_convidado TEXT, FOREIGN KEY (nome_grupo) REFERENCES Elementos_Grupo(Nome), FOREIGN KEY (nome_convidado) REFERENCES Utilizador(email))");
            statement.execute("INSERT INTO VersaoBD (versao_n) VALUES (1)");

            System.out.println("Nova base de dados criada com sucesso!");
        }catch (SQLException e){
            throw new RuntimeException(e);
        }

    }

    private void iniciarVersaoBD() {
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT versao_n FROM VersaoBD LIMIT 1");

            if (resultSet.next()) {
                dbVersao = resultSet.getInt("versao_n");
                System.out.println("Versão da base de dados carregada: " + dbVersao);
            } else {
                System.out.println("Tabela versão está vazia, vou usar a versão padrão (1).");
                dbVersao = 1;
                statement.execute("INSERT INTO VersaoBD (versao_n) VALUES (1)");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao obter a versão da BD: " + e.getMessage());
            dbVersao = 1;
        }
    }

    public void updateBD(String query) {
        try(Statement statement = connection.createStatement()) {
            statement.execute(query);
            System.out.println("Base de Dados atualizada");
            updateBDversion();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private void updateBDversion() {
        try (Statement statement = connection.createStatement()){
            statement.execute("UPDATE VersaoBD SET versao_n = versao_n + 1");
            ResultSet resultSet = statement.executeQuery("SELECT versao_n FROM VersaoBD LIMIT 1");

            if(resultSet.next()) {
                dbVersao = resultSet.getInt("versao_n");
                System.out.println("Versão da Base de Dados atualizada para: " + dbVersao);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar a versão da BD");

        }
    }

    //Gets bd
    // for one result
    public String getBDQuery (String query){
        StringBuilder string = new StringBuilder();
        try (Statement statement = connection.createStatement()){
            ResultSet result = statement.executeQuery(query);

            while (result.next()){
                int colunas = result.getMetaData().getColumnCount();
                for (int i = 1; i <= colunas; i++){
                    string.append(result.getString(1));
                    if (i < colunas) {
                        string.append(";");
                    }
                }
            }
        }catch (SQLException e){
            return "Erro ao tentar aceder à BD";
        }
        return string.toString();
    }


    public List<String> getListBDQuery (String query){
        List<String> lista = new ArrayList<>();

        try (Statement statement = connection.createStatement()) {
            ResultSet result = statement.executeQuery(query);

            while (result.next())
                for (int i = 1; i <= result.getMetaData().getColumnCount(); i++)
                    lista.add(result.getString(i));
        } catch (SQLException e) {
            return null;
        }
        return lista;
    }
    public List<String> getListDespesasBD (String query){
        List<String> lista = new ArrayList<>();

        try (Statement statement = connection.createStatement()) {
            ResultSet result = statement.executeQuery(query);

            while (result.next())
                for (int i = 1; i <= result.getMetaData().getColumnCount(); i++) {
                    if( i%6 == 0 )
                        lista.add(result.getString(i) +"\n");
                    else
                        lista.add(result.getString(i));
                }
        } catch (SQLException e) {
            return null;
        }
        return lista;
    }
}
