package pt.isec.pd.trabalho_pratico.BaseDados;

import java.io.File;
import java.sql.*;

public class BDController {

    private Connection connection;
    private int dbVersao;

    public void ligarBD()
    {
        try{
            //sempre que for para mudar o caminho ir a database.db e copiar o absolute path
            //String caminho = "src/pt/isec/pd/splitwise/BD/database.db";
            //String caminho = "C:\\Users\\Usuario\\OneDrive\\Ambiente de Trabalho\\ISEC\\3Ano1Semestre\\PD\\tp_pd\\Tp\\src\\pt\\isec\\pd\\trabalho_pratico\\BaseDados\\database.db";
            //String caminho = "D:\\Engenharia_Informatica\\3_ano\\PD\\BasePD\\TPBase\\src\\pt\\isec\\pd\\trabalho_pratico\\BaseDados\\database.db";
            //String caminho = "C:\\Users\\diogo\\Desktop\\Material Estudo\\3ºAno\\1ºSemestre\\PD\\tp_pd\\Tp\\src\\pt\\isec\\pd\\trabalho_pratico\\BaseDados\\database.db";
            //String caminho = "D:\\Engenharia_Informatica\\3_ano\\PD\\BasePD\\TPBase\\src\\pt\\isec\\pd\\trabalho_pratico\\BaseDados\\database.db";
            String caminho = "BaseDados/database.db";
            String url = "jdbc:sqlite:" + caminho;

            File dbFile = new File(caminho);
            boolean dbExists = dbFile.exists();

            connection = DriverManager.getConnection(url);

            if(!dbExists) {
                System.out.println("BD não criada, A criar BD...");
                criarTabelas();
            }
            else {
                System.out.println("BD já criada");
            }

            iniciarVersaoBD();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private void criarTabelas() {
        try(Statement statement = connection.createStatement())
        {
            String tabelaVersaoBD = "CREATE TABLE IF NOT EXISTS VersaoBD (versao_n INTEGER PRIMARY KEY)";
            String tabelaUtilizadores = "CREATE TABLE IF NOT EXISTS Utilizador (email TEXT PRIMARY KEY, telefone INTEGER(9), password TEXT, nome VARCHAR(255))";
            String tabelaGrupos = "CREATE TABLE IF NOT EXISTS Grupos (id INTEGER PRIMARY KEY AUTOINCREMENT, nome_grupo TEXT)";
            String tabelaElementosGrupo = "CREATE TABLE IF NOT EXISTS Elementos_Grupo (Nome TEXT, Nome_Elementos INTEGER, FOREIGN KEY (Nome_Elementos) REFERENCES Utilizador(email))";
            String tabelaDespesas = "CREATE TABLE IF NOT EXISTS Despesas (nome_grupo INTEGER, descricao TEXT, valor REAL, data DATE, quem_pagou_total TEXT, dividido_por TEXT, pago_por TEXT, valor_individual float, FOREIGN KEY (nome_grupo) REFERENCES Elementos_Grupo(Nome), FOREIGN KEY (quem_pagou_total) REFERENCES Utilizador(email), FOREIGN KEY (dividido_por) REFERENCES Utilizador(email))";
            String tabelaConvitesGrupos = "CREATE TABLE IF NOT EXISTS Convites_Grupos (nome_grupo INTEGER, nome_convidado TEXT, FOREIGN KEY (nome_grupo) REFERENCES Elementos_Grupo(Nome), FOREIGN KEY (nome_convidado) REFERENCES Utilizador(email))";

            statement.execute(tabelaVersaoBD);
            statement.execute(tabelaUtilizadores);
            statement.execute(tabelaGrupos);
            statement.execute(tabelaElementosGrupo);
            statement.execute(tabelaDespesas);
            statement.execute(tabelaConvitesGrupos);

            String inciaVersao = "INSERT INTO VersaoBD (versao_n) VALUES (1)";
            statement.execute(inciaVersao);

            System.out.println("Estrutura inicial da BD criada");
        }catch (SQLException e){
            throw new RuntimeException(e);
        }

    }

    private void iniciarVersaoBD() {
        try (Statement statement = connection.createStatement()) {
            String queryGetVersao = "SELECT versao_n FROM VersaoBD LIMIT 1";
            ResultSet resultSet = statement.executeQuery(queryGetVersao);

            if (resultSet.next()) {
                dbVersao = resultSet.getInt("versao_n");
                System.out.println("Versão da BD carregada: " + dbVersao);
            } else {
                System.out.println("Tabela VersaoBD está vazia. Usando versão padrão (1).");
                dbVersao = 1;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao obter a versão da BD: " + e.getMessage());
            dbVersao = 1;
        }
    }


    public void updateBD(String query)
    {
        try(Statement statement = connection.createStatement())
        {
            statement.execute(query);
            System.out.println("BD atualizada");

            updateBDversion();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateBDversion()
    {

        try (Statement statement = connection.createStatement())
        {
            String queryUpdateVersao = "UPDATE VersaoBD SET versao_n = versao_n + 1";
            statement.execute(queryUpdateVersao);

            String queryGetVersao = "SELECT versao_n FROM VersaoBD LIMIT 1";
            ResultSet resultSet = statement.executeQuery(queryGetVersao);

            if(resultSet.next())
            {
                dbVersao = resultSet.getInt("versao_n"); // atualiza a versão da BD para os Heartbeats
                System.out.println("Versão da BD atualizada para: " + dbVersao);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar a versão da BD");

        }
    }

    public String getBDQuery (String query){
        StringBuilder string = new StringBuilder();
        try (Statement statement = connection.createStatement()){
            ResultSet result = statement.executeQuery(query);

            while (result.next()){
                int colunas = result.getMetaData().getColumnCount();
                for (int i = 1; i <= colunas; i++){
                    string.append(result.getString(i));
                    if (i < colunas) {
                        string.append(";"); //;// " "
                    }
                }
            }
        }catch (SQLException e){
            return "Erro ao tentar aceder à BD";
        }
        return string.toString();
    }
    public String getListarBDQuery (String query){
        StringBuilder string = new StringBuilder();
        try (Statement statement = connection.createStatement()){
            ResultSet result = statement.executeQuery(query);

            while (result.next()){
                int colunas = result.getMetaData().getColumnCount();

                for (int i = 1; i <= colunas; i++){
                    string.append(result.getString(i));
                    string.append(","); //, // -
                }
            }
        }catch (SQLException e){
            return "Erro ao tentar aceder à BD";
        }
        return string.toString();
    }
    public String getListarDespesasBDQuery (String query){
        StringBuilder string = new StringBuilder();
        try (Statement statement = connection.createStatement()){
            ResultSet result = statement.executeQuery(query);

            while (result.next()){
                int colunas = result.getMetaData().getColumnCount();

                for (int i = 1; i <= colunas; i++){
                    string.append(result.getString(i) == null ? "NULL" : result.getString(i));
                    string.append(";"); // ; // _
                    if(i % 6 == 0)
                        string.append("|");
                }
            }
        }catch (SQLException e){
            return "Erro ao tentar aceder à BD";
        }
        return string.toString();
    }

    public String getListaCSVBDQuery(String query) {
        StringBuilder string = new StringBuilder();
        try (Statement statement = connection.createStatement()) {
            ResultSet result = statement.executeQuery(query);

            boolean isFirstRow = true;
            while (result.next()) {
                if (!isFirstRow) {
                    string.append("\";\"");
                }
                isFirstRow = false;

                int colunas = result.getMetaData().getColumnCount();
                for (int i = 1; i <= colunas; i++) {
                    string.append(result.getString(i));
                    if (i < colunas) {
                        string.append("\";\"");
                    }
                }
            }
        } catch (SQLException e) {
            return "Erro ao tentar aceder à BD";
        }
        return string.toString();
    }

    public int getDbVersao() {
        return dbVersao;
    }

}
