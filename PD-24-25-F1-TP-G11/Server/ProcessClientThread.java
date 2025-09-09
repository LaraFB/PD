package pt.isec.pd.trabalho_pratico.Server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.SocketTimeoutException;
import java.util.Arrays;

public class ProcessClientThread extends Thread {
    private final Socket clientsocket;
    private final Server server;
    private static final int TIMEOUT = 60000;
    private boolean login = false;
    private BufferedReader in;
    private PrintStream out;
    private String username;

    public ProcessClientThread(Socket clientsocket, Server server) {
        this.clientsocket = clientsocket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            clientsocket.setSoTimeout(TIMEOUT);
            in = new BufferedReader(new InputStreamReader(clientsocket.getInputStream()));
            out = new PrintStream(clientsocket.getOutputStream());

            String recievedMsg;
            do{
                while ((recievedMsg = in.readLine()) != null) {
                    //Só para ver se o cliente está fixe
                    if(recievedMsg.equals("A estabelecer ligação com o Server.")){
                        out.println("Conexão estabelecida!");
                        break;
                    }

                    //Login/SignUp
                    String[] args = recievedMsg.split(" ");
                    if(args.length == 2){
                        System.out.println("[Recebeu] Username: " + args[0] + " Password: " + args[1]);

                        if(verificaLogin(args[0].toLowerCase(),args[1])) {
                            String result = server.getBDQuery("SELECT nome FROM Utilizador WHERE email = '" + args[0].toLowerCase() + "'");
                            enviar("Bem vindo " + result + "!");
                            username = args[0].toLowerCase();
                            login = true;
                        } else enviar("Invalid Login!");

                    }else{
                        System.out.println("[Recebeu] Username: " + args[0] + " Telemovel: " + args[1] + " Password: " + args[2] + " Nome: " + args[3]);
                        int erro = verificaSignUp(args[0].toLowerCase(),args[1],args[2],args[3]);

                        if(erro == 1){
                            String result = server.getBDQuery("SELECT nome FROM Utilizador WHERE email = '" + args[0].toLowerCase() + "'");
                            enviar("Bem vindo " + result + "!");
                            username = args[0].toLowerCase();
                            login = true;
                        }
                        else if(erro == 2) enviar("This email already exists, please use another email!");
                        else if(erro == 3) enviar("Invalid phone number!");
                        else if(erro == 0) enviar("Please fill in the required fields.");
                    }
                    break;
                }
            }while(!login);

            clientsocket.setSoTimeout(0);

            while(!clientsocket.isClosed()){
                while ((recievedMsg = in.readLine()) != null) {
                    System.out.println("[Recebeu] " + recievedMsg);

                    if(recievedMsg.contains("Criar grupo ")){
                        recievedMsg = recievedMsg.substring(12); // retirar a parte q n interessa do criar grupo

                        if(server.getBDQuery("SELECT Nome FROM Elementos_Grupo WHERE Nome = '" + recievedMsg + "'").isEmpty()){
                            server.updateBD("INSERT INTO Elementos_Grupo (Nome, Nome_elementos) VALUES ('" + recievedMsg + "', '" + username + "');");
                            server.updateBD("INSERT INTO Grupos (nome_grupo) VALUES ('" + recievedMsg + "');");
                            enviar("Grupo criado com sucesso!");
                        } else enviar("Esse grupo já existe!");

                    }else if(recievedMsg.contains("Criar convites ")){
                        recievedMsg = recievedMsg.substring(15);
                        String grupos = server.getBDQuery("SELECT Nome FROM Elementos_Grupo WHERE Nome_elementos = '" + username + "'");
                        String[] args = recievedMsg.split("-");

                        if(grupos.contains(args[0])){
                            if(server.getBDQuery("SELECT nome_grupo FROM Convites_Grupos WHERE nome_grupo = '" + args[0] + "' AND nome_convidado = '" + args[1] +"'").isEmpty()){
                                if(server.getBDQuery("SELECT nome FROM Elementos_Grupo WHERE Nome_elementos = '" + args[1] + "' AND Nome = '" + args[0] + "'").isEmpty()){
                                    server.updateBD("INSERT INTO Convites_Grupos (nome_grupo,nome_convidado) VALUES('" + args[0] + "', '" + args[1] + "');");
                                    enviar("Convite Enviado!");
                                } else enviar(" O "+ args[1] + " já pertence ao grupo " + args[0]);
                            } else enviar(args[1] + " já foi convidado para o grupo " + args[0]);
                        }

                    }else if(recievedMsg.equals("Ver convites pendentes"))
                        enviar(server.getListarBDQuery("SELECT nome_grupo FROM Convites_Grupos WHERE nome_convidado ='" + username + "'"));

                    else if(recievedMsg.equals("Lista de grupos"))
                        enviar(server.getListarBDQuery("SELECT Nome FROM Elementos_Grupo WHERE Nome_elementos = '" + username + "'"));

                    else if(recievedMsg.contains("Editar grupo ")){
                        recievedMsg = recievedMsg.substring(13);
                        String [] args = recievedMsg.split("-");

                        if(!server.getBDQuery("SELECT Nome_elementos FROM Elementos_Grupo WHERE Nome_elementos = '" + username + "' AND Nome = '" + args[0] + "'").isEmpty()){
                            if(server.getBDQuery("SELECT Nome FROM Elementos_Grupo WHERE Nome= '" + args[1] + "'").isEmpty()){
                                server.updateBD("UPDATE Elementos_Grupo SET Nome = '" + args[1] + "' WHERE Nome = '" + args[0] + "';");
                                server.updateBD("UPDATE Despesas SET nome_grupo = '" + args[1] + "' WHERE nome_grupo = '" + args[0] + "';");
                                server.updateBD("UPDATE Convites_Grupos SET nome_grupo = '" + args[1] + "' WHERE nome_grupo = '" + args[0] + "';");
                                server.updateBD("UPDATE Grupos SET nome_grupo = '" + args[1] + "' WHERE nome_grupo = '" + args[0] + "';");

                                enviar("Nome do grupo mudado com sucesso!");
                            } else enviar("ERRO");
                        }

                    }else if(recievedMsg.contains("Adicionar Despesa ")){
                        recievedMsg = recievedMsg.substring(18);
                        int escolha = validaDespesa(recievedMsg);

                        if (escolha == 1) enviar("Expense added successfully!");
                        else if(escolha == 2) enviar("Invalid value!");
                        else if(escolha == 0) enviar("Expense already exists!");

                    }else if (recievedMsg.contains("Despesas por grupo ")){
                        recievedMsg = recievedMsg.substring(19);
                       if(!server.getListarDespesasBDQuery("SELECT descricao,valor,data,quem_pagou_total,dividido_por,valor_individual FROM Despesas WHERE nome_grupo = '" + recievedMsg + "'").isEmpty())
                           enviar(server.getListarDespesasBDQuery("SELECT descricao,valor,data,quem_pagou_total,dividido_por,valor_individual FROM Despesas WHERE nome_grupo = '" + recievedMsg + "' ORDER BY data ASC"));
                       else
                           enviar("Nao existe despesas!");

                    }else if (recievedMsg.contains("Eliminar despesa ")){
                        recievedMsg = recievedMsg.substring(17);
                        String [] args = recievedMsg.split("-");

                        if(args[4].equalsIgnoreCase("you")) args[4] = username;
                        if(server.getBDQuery("SELECT nome_grupo FROM Despesas WHERE nome_grupo = '" + args[0] +"' AND descricao = '" + args[1] + "' AND valor = '"
                                + args[2]+"' AND data = '" + args[3]+"' AND (dividido_por LIKE '%" + args[5] + "%' OR dividido_por = '" + args[5] + "');").isEmpty())
                            enviar("Não existe essa despesa");
                        else{
                            server.updateBD("DELETE FROM Despesas WHERE nome_grupo = '" + args[0] +"' AND descricao = '" + args[1] + "' AND valor = '"
                                    + args[2]+"' AND data = '" + args[3]+"' AND (dividido_por LIKE '%" + args[5] + "%' OR dividido_por = '" + args[5] + "');");
                            enviar("Despesa Eliminada");
                        }

                    } else if (recievedMsg.contains("Pagar despesa ")) {
                        recievedMsg = recievedMsg.substring(14);
                        String[] args = recievedMsg.split("-");

                        server.updateBD("UPDATE Despesas SET pago_por = CONCAT(pago_por, ' " + username + "') WHERE nome_grupo = '" + args[0] + "' AND descricao = '" + args[1] + "' AND valor = '" + args[2] + "' AND data = '" + args[3] + "'");
                        enviar("Expense paid successfully");

                    } else if (recievedMsg.contains("Cancelar pagamento ")) {
                        recievedMsg = recievedMsg.substring(19);
                        String[] args = recievedMsg.split("-");

                        String pago_por = server.getBDQuery("SELECT pago_por FROM Despesas WHERE nome_grupo = '" + args[0] + "' AND descricao = '" + args[1] + "' AND valor = '" + args[2] + "' AND data = '" + args[3] + "'");

                        if (pago_por.contains(username))
                            pago_por = pago_por.replace(username, "").trim();

                        server.updateBD("UPDATE Despesas SET pago_por = '" + pago_por + "' WHERE nome_grupo = '" + args[0] + "' AND descricao = '" + args[1] + "' AND valor = '" + args[2] + "' AND data = '" + args[3] + "'");
                        enviar("Payment canceled successfully");

                    }
                    else if(recievedMsg.contains("Total gasto ")){
                        recievedMsg = recievedMsg.substring(12);
                        float soma = 0;
                        String []valores = server.getListarBDQuery("SELECT valor FROM Despesas WHERE nome_grupo = '" + recievedMsg +"'").split(",");

                        for (String valor : valores)
                            if(!valor.isEmpty())
                                soma += Float.parseFloat(valor);

                        enviar(String.valueOf(soma));

                    }else if( recievedMsg.contains("Delete grupo ")){
                        recievedMsg = recievedMsg.substring(13);
                        if(!server.getBDQuery("SELECT Nome_elementos FROM Elementos_Grupo WHERE Nome_elementos = '" + username +"' AND Nome = '"+recievedMsg+"'").isEmpty()){
                            if(server.getListarBDQuery("SELECT * FROM Despesas WHERE nome_grupo = '" + recievedMsg +"' AND  LENGTH(REPLACE(dividido_por, ' ', '')) != LENGTH(REPLACE(pago_por, ' ', ''))").isEmpty()
                                    || server.getBDQuery("SELECT nome_grupo FROM Despesas WHERE nome_grupo = '" + recievedMsg +"'").isEmpty()) {
                                server.updateBD("DELETE FROM Despesas  WHERE nome_grupo ='" +recievedMsg +"';");
                                server.updateBD("DELETE FROM Elementos_Grupo  WHERE nome ='" +recievedMsg +"';");
                                server.updateBD("DELETE FROM Grupos  WHERE nome_grupo ='" +recievedMsg +"';");
                                server.updateBD("DELETE FROM Convites_Grupos WHERE nome_grupo = '"+recievedMsg+"'");
                                enviar("Grupo apagado");
                            }else{
                                enviar("Ainda há despesas por pagar!");
                            }
                        }

                    }else if (recievedMsg.contains("Sair do grupo ")){
                        recievedMsg = recievedMsg.substring(14);

                        if(server.getBDQuery("SELECT Nome FROM Elementos_Grupo WHERE Nome_elementos = '" + username + "' AND Nome ='" + recievedMsg +"'").equals(recievedMsg)){
                            if(totalowe(recievedMsg) == 0f){
                                server.updateBD("DELETE FROM Elementos_Grupo WHERE nome ='" + recievedMsg +"' AND nome_elementos = '" + username + "';");
                                if(server.getBDQuery("SELECT Nome_Elementos FROM Elementos_Grupo WHERE nome = '"+recievedMsg+"'").isEmpty() &&
                                    server.getBDQuery("SELECT nome_convidado FROM Convites_Grupos WHERE nome_grupo = '"+recievedMsg+"'").isEmpty()){
                                    server.updateBD("DELETE FROM Despesas WHERE nome_grupo ='" +recievedMsg +"';");
                                    server.updateBD("DELETE FROM Elementos_Grupo  WHERE nome ='" +recievedMsg +"';");
                                    server.updateBD("DELETE FROM Grupos  WHERE nome_grupo ='" +recievedMsg +"';");
                                }
                                enviar("Saiu do grupo!");
                            } else enviar("Pague as despesas primeiro!");
                        }

                    }else if (recievedMsg.equals("Account Info")) enviar(server.getListarBDQuery("SELECT * FROM Utilizador WHERE email = '" + username + "'"));

                    else if (recievedMsg.contains("Edit Account: ")){
                        recievedMsg = recievedMsg.substring(14);
                        String[] parts = recievedMsg.split(" ");


                        if(username.equalsIgnoreCase(parts[0]) || server.getBDQuery("SELECT email FROM Utilizador WHERE email = '" + parts[0] + "'").isEmpty()) {
                            if(parts.length <4 ) enviar("Falta infromação");
                            else{
                                server.updateBD("UPDATE Utilizador SET email = '" + parts[0].toLowerCase() + "', telefone = '" + parts[1] + "', password = '" + parts[2] + "', nome = '" + parts[3] + "' WHERE email = '" + username + "';");
                                server.updateBD("UPDATE Convites_Grupos SET nome_convidado = '" + parts[0].toLowerCase() + "' WHERE nome_convidado = '" + username + "';" );
                                server.updateBD("UPDATE Elementos_Grupo SET Nome_elementos = '" + parts[0].toLowerCase() + "' WHERE Nome_elementos = '" + username + "';");
                                server.updateBD("UPDATE Despesas SET pago_por = REPLACE(pago_por, '" + username + "', '" + parts[0].toLowerCase() + "') WHERE pago_por LIKE '%" + username + "%'");
                                server.updateBD("UPDATE Despesas SET dividido_por = REPLACE(dividido_por, '" + username + "', '" + parts[0].toLowerCase() + "') WHERE dividido_por LIKE '%" + username + "%'");
                                server.updateBD("UPDATE Despesas SET quem_pagou_total = '" + parts[0].toLowerCase() + "' WHERE quem_pagou_total LIKE '%" + username + "%'");

                                username = parts[0].toLowerCase();
                                enviar("Account info changed");
                            }
                        }else enviar("Já existe um utilizador com esse email.");

                    }else if(recievedMsg.contains("Nomes users")) enviar(server.getListarBDQuery("SELECT email FROM Utilizador WHERE email != '" + username + "'"));
                    else if (recievedMsg.contains("Todos nomes users")) enviar(server.getListarBDQuery("SELECT email FROM Utilizador"));
                    else if (recievedMsg.contains("NomesPessoasPorgrupo ")){
                        recievedMsg = recievedMsg.substring(21);
                        enviar(pessoasPertencentesGrupo(recievedMsg));
                    }
                    else if (recievedMsg.contains("Aceitar convite ")) {
                        recievedMsg = recievedMsg.substring(16);

                        server.updateBD("DELETE FROM Convites_Grupos WHERE nome_grupo = '" + recievedMsg + "' AND nome_convidado = '" + username + "'");
                        server.updateBD("INSERT INTO Elementos_Grupo (Nome, Nome_elementos) VALUES ('" + recievedMsg + "', '" + username + "')");
                        enviar("true");

                    } else if (recievedMsg.contains("Rejeitar convite ")) {
                        recievedMsg = recievedMsg.substring(17);
                        server.updateBD("DELETE FROM Convites_Grupos WHERE nome_grupo = '" + recievedMsg + "' AND nome_convidado = '" + username + "'");
                        if(server.getBDQuery("SELECT Nome_Elementos FROM Elementos_Grupo WHERE nome = '"+recievedMsg+"'").isEmpty() &&
                                server.getBDQuery("SELECT nome_convidado FROM Convites_Grupos WHERE nome_grupo = '"+recievedMsg+"'").isEmpty()){
                            server.updateBD("DELETE FROM Despesas WHERE nome_grupo ='" +recievedMsg +"';");
                            server.updateBD("DELETE FROM Elementos_Grupo  WHERE nome ='" +recievedMsg +"';");
                            server.updateBD("DELETE FROM Grupos  WHERE nome_grupo ='" +recievedMsg +"';");
                        }
                        enviar("true");

                    }else if(recievedMsg.contains("Towe ")) {
                        recievedMsg = recievedMsg.substring(5);
                        enviar(String.valueOf(totalowe(recievedMsg)));

                    }else if(recievedMsg.contains("Owed ")){
                        recievedMsg = recievedMsg.substring(5);
                        enviar(String.valueOf(totalowed(recievedMsg)));
                    }else if (recievedMsg.contains("IsYou ")){
                        recievedMsg = recievedMsg.substring(6);
                        if(server.getBDQuery("SELECT email FROM Utilizador WHERE email = '" + recievedMsg + "'").equals(username)) enviar("true");
                        else enviar("false");
                    }else if (recievedMsg.contains("Exportar despesas ")){
                        recievedMsg = recievedMsg.substring(18);
                        if(exportarDespesas(recievedMsg))enviar("File created successfully!");
                        else enviar("Error creating file!");

                    }else if (recievedMsg.contains("ExpenseIsPaid ")){
                        recievedMsg = recievedMsg.substring(14);
                        String [] args = recievedMsg.split("-");

                        if(args[4].equalsIgnoreCase("you")) args[4] = username;
                        if( args[5].equalsIgnoreCase("equally")) {
                            args[5] = server.getListarBDQuery("SELECT Nome_elementos FROM Elementos_Grupo WHERE nome = '" + args[0] + "'");
                            args[5] = args[5].replace(","," ");
                        }else
                            args[5] = args[5].replace(", "," ");

                        enviar(server.getBDQuery("SELECT nome_grupo FROM Despesas WHERE nome_grupo = '" + args[0] + "' AND descricao = '" + args[1] + "' AND valor = '" + args[2] +"' AND data = '" + args[3] + "' AND quem_pagou_total = '" + args[4] + "' AND LENGTH(REPLACE(dividido_por, ' ', '')) == LENGTH(REPLACE(pago_por, ' ', ''))").isEmpty() ? "false" : "true");

                    } else if (recievedMsg.contains("YouPaid ")) {
                        recievedMsg = recievedMsg.substring(8);
                        String [] args = recievedMsg.split("-");

                        if(args[4].equalsIgnoreCase("you")) args[4] = username;
                        if( args[5].equalsIgnoreCase("equally")) {
                            args[5] = server.getListarBDQuery("SELECT Nome_elementos FROM Elementos_Grupo WHERE nome = '" + args[0] + "'");
                            args[5] = args[5].replace(","," ");
                        }else
                            args[5] = args[5].replace(", "," ");

                        enviar(server.getBDQuery("SELECT nome_grupo FROM Despesas WHERE nome_grupo = '" + args[0] + "' AND descricao = '" + args[1] + "' AND valor = '" + args[2] +"' AND data = '" + args[3] + "' AND quem_pagou_total = '" + args[4] + "' AND dividido_por LIKE '%"+ username+"%' AND pago_por LIKE '%" +username+"%'").isEmpty() ? "false" : "true");

                    } else if(recievedMsg.contains("Home expense ")){
                        recievedMsg = recievedMsg.substring(13);
                        String [] args = recievedMsg.split(",");

                        if(args[0].equalsIgnoreCase("all")){
                            if(args[1].equalsIgnoreCase("owe"))
                                enviar(server.getListarBDQuery("SELECT descricao,valor_individual FROM Despesas WHERE dividido_por LIKE '%" + username +"%' AND pago_por NOT LIKE '%" + username +"%'"));
                            else
                                enviar(server.getListarBDQuery("SELECT descricao,valor_individual FROM Despesas WHERE quem_pagou_total LIKE '%" + username +"%' AND LENGTH(REPLACE(dividido_por, ' ', '')) != LENGTH(REPLACE(pago_por, ' ', ''))"));
                            }
                        else
                            if(args[1].equalsIgnoreCase("owe"))
                                enviar(server.getListarBDQuery("SELECT descricao,valor_individual FROM Despesas WHERE nome_grupo = '" + args[0] +"' AND dividido_por LIKE '%" + username +"%' AND pago_por NOT LIKE '%" + username +"%'"));
                            else
                                enviar(server.getListarBDQuery("SELECT descricao,valor_individual FROM Despesas WHERE nome_grupo = '" + args[0] +"' AND quem_pagou_total = '" + username + "' AND LENGTH(REPLACE(dividido_por, ' ', '')) != LENGTH(REPLACE(pago_por, ' ', ''))"));

                    }else if(recievedMsg.contains("History expense ")){
                        recievedMsg = recievedMsg.substring(16);
                        String [] args = recievedMsg.split(",");

                        if (args[0].equalsIgnoreCase("all")) {
                            if (args[1].equalsIgnoreCase("Paid you"))
                                enviar(server.getListarBDQuery("SELECT descricao, valor_individual, nome_grupo FROM Despesas WHERE quem_pagou_total = '" + username + "' AND (pago_por NOT LIKE '%" + username + "%' OR pago_por LIKE '% %')"));
                            else
                                enviar(server.getListarBDQuery("SELECT descricao, valor_individual, nome_grupo FROM Despesas WHERE pago_por LIKE '%" + username + "%' AND quem_pagou_total NOT LIKE '" + username + "'"));

                        }else {
                            if (args[1].equalsIgnoreCase("Paid you"))
                                enviar(server.getListarBDQuery("SELECT descricao, valor_individual, nome_grupo FROM Despesas WHERE nome_grupo = '" + args[0] + "' AND quem_pagou_total = '" + username + "' AND (pago_por NOT LIKE '%" + username + "%' OR pago_por LIKE '% %')"));
                            else
                                enviar(server.getListarBDQuery("SELECT descricao, valor_individual, nome_grupo FROM Despesas WHERE nome_grupo = '" + args[0] + "' AND pago_por LIKE '%" + username + "%' AND quem_pagou_total NOT LIKE '" + username + "'"));
                        }
                    }else if (recievedMsg.contains("Editar despesa ")){
                        recievedMsg = recievedMsg.substring(15);
                        String [] despesas = recievedMsg.split(";");
                        String [] oldArgs = despesas[0].split("-");
                        float preco = Float.parseFloat(oldArgs[2])/(oldArgs[5].split(" ").length);

                        if(oldArgs[4].equalsIgnoreCase("you"))
                            oldArgs[4] = username;

                        if(!server.getBDQuery("SELECT nome_grupo FROM Despesas WHERE nome_grupo = '" + oldArgs[0] + "' AND descricao = '" + oldArgs[1] +"' AND valor = '"+ oldArgs[2] + "' AND data = '" + oldArgs[3] + "' AND quem_pagou_total = '" + oldArgs[4] +"' AND dividido_por = '" + oldArgs[5] + "' AND valor_individual = '" + preco +"'").isEmpty()){
                            String [] newArgs = despesas[1].split("-");

                            if( newArgs[4].equalsIgnoreCase("equally")) {
                                newArgs[4] = server.getListarBDQuery("SELECT Nome_elementos FROM Elementos_Grupo WHERE nome = '" + oldArgs[0] + "'");
                                newArgs[4] = newArgs[4].replace(","," ");
                            }else
                                newArgs[4] = newArgs[4].replace(", "," ");

                            String pago_por = server.getBDQuery("SELECT pago_por FROM Despesas WHERE nome_grupo = '" + oldArgs[0] + "' AND descricao = '" + oldArgs[1] +"' AND valor = '"+ oldArgs[2] + "' AND data = '" + oldArgs[3] + "' AND quem_pagou_total = '" + oldArgs[4] +"' AND dividido_por = '" + oldArgs[5] + "' AND valor_individual = '" + preco +"'");
                            float newpreco = Float.parseFloat(newArgs[1])/ (newArgs[4].split(" ").length);

                            if(!oldArgs[4].equalsIgnoreCase(newArgs[3])) //quem_pagou
                                if(!pago_por.contains(newArgs[4]))
                                    pago_por = pago_por + " " + newArgs[3];

                            String[] nomes = pago_por.split(" ");
                            for(String nome : nomes)
                                if(!newArgs[4].contains(nome))
                                    pago_por = pago_por.replace(nome,"");

                            if(server.getBDQuery("SELECT nome_grupo FROM Despesas WHERE nome_grupo = '"+ oldArgs[0] + "' AND descricao = '" + newArgs[0] +"' AND valor = '"+ newArgs[1] + "' AND data = '" + newArgs[2] + "' AND quem_pagou_total = '" + newArgs[3] +"' AND dividido_por = '" + newArgs[4] + "' AND valor_individual = '" + preco +"'").isEmpty()){
                                server.updateBD("UPDATE Despesas SET descricao = '" + newArgs[0] + "', valor = '" + newArgs[1] + "', data = '" + newArgs[2] + "', quem_pagou_total ='" + newArgs[3] + "', dividido_por = '" + newArgs[4] + "', valor_individual = '" + newpreco +
                                        "', pago_por = '" + pago_por + "' WHERE nome_grupo = '" + oldArgs[0] + "' AND descricao = '" + oldArgs[1] +"' AND valor = '"+ oldArgs[2] + "' AND data = '" + oldArgs[3] + "' AND quem_pagou_total = '" + oldArgs[4] +"' AND dividido_por = '" + oldArgs[5] + "' AND valor_individual = '" + preco +"'");
                                enviar("Expense edited successfully!");
                            }
                            else
                                enviar("Expense already exists!");
                        }else enviar("There's no such expense!");
                    }else if(recievedMsg.contains("ID grupo ")){
                        recievedMsg = recievedMsg.substring(9);
                        String id = server.getBDQuery("SELECT id FROM Grupos WHERE nome_grupo = '" + recievedMsg +"'");
                        if(id.isEmpty())
                            enviar("0");
                        else enviar(id);
                    } else if (recievedMsg.contains("NomeGrupoID: ")) {
                        recievedMsg = recievedMsg.substring(13);
                        String grupo = server.getBDQuery("SELECT nome_grupo FROM Grupos WHERE id = '" + recievedMsg +"'");
                        if(grupo.isEmpty())
                            enviar("There's no such grupo!");
                        else enviar(grupo);
                    }else if(recievedMsg.equals("LogOut"))
                        clientsocket.close();
                }
            }

            clientsocket.close();
        } catch (SocketTimeoutException e) {
            System.out.println("[Timeout] Cliente inativo por 60 segundos. A encerrar conexão.");
            try {
                clientsocket.close();
            } catch (IOException ex) {
                System.out.println("Erro a fechar o socket.");
            }
        } catch (IOException e) {
            System.out.println("Nenhuma resposta enviada");
        }
    }

    private float totalowed(String recievedMsg) { //errado
        float somaTotal = 0, somai=0,soma=0;
        String []valoresi,valoresTotal;
        if(recievedMsg.equalsIgnoreCase("all")) {
            valoresi = server.getListarBDQuery("SELECT valor_individual FROM Despesas WHERE quem_pagou_total = '" + username + "' AND LENGTH(REPLACE(dividido_por, ' ', '')) != LENGTH(REPLACE(pago_por, ' ', ''))").split(",");
            valoresTotal = server.getListarBDQuery("SELECT valor FROM Despesas WHERE quem_pagou_total = '" + username + "' AND LENGTH(REPLACE(dividido_por, ' ', '')) != LENGTH(REPLACE(pago_por, ' ', ''))").split(",");
        }else{
            valoresi = server.getListarBDQuery("SELECT valor_individual FROM Despesas WHERE nome_grupo = '" + recievedMsg + "'AND quem_pagou_total = '" + username + "'AND LENGTH(REPLACE(dividido_por, ' ', '')) != LENGTH(REPLACE(pago_por, ' ', ''))").split(",");
            valoresTotal = server.getListarBDQuery("SELECT valor FROM Despesas WHERE nome_grupo = '" + recievedMsg + "'AND quem_pagou_total = '" + username + "'AND LENGTH(REPLACE(dividido_por, ' ', '')) != LENGTH(REPLACE(pago_por, ' ', ''))").split(",");
        }
        for (int i = 0; i <valoresi.length; i++) {
            if(!valoresi[i].isEmpty() && !valoresTotal[i].isEmpty()){
                somaTotal += Float.parseFloat(valoresTotal[i]);
                somai += Float.parseFloat(valoresi[i]);
                soma += (somaTotal-somai);
            }
        }
        return somaTotal-somai;
    }

    private float totalowe(String recievedMsg) {
        float soma = 0;
        String []valores;
        if(recievedMsg.equalsIgnoreCase("all"))
            valores = server.getListarBDQuery("SELECT valor_individual FROM Despesas WHERE dividido_por LIKE '%" + username + "%' AND pago_por NOT LIKE '%" + username + "%'").split(",");
        else
            valores = server.getListarBDQuery("SELECT valor_individual FROM Despesas WHERE nome_grupo = '" + recievedMsg + "' AND dividido_por LIKE '%" + username + "%' AND pago_por NOT LIKE '%" + username + "%'").split(",");
        for (String valor : valores)
            if (!valor.isEmpty())
                soma += Float.parseFloat(valor);
        return soma;
    }

    private void enviar(String msg){
        out.println(msg);
        out.flush();
    }

    private boolean exportarDespesas(String grupo){
        String csvFilePath = "files/" + grupo.replace(" ","_") + "_despesas.csv";

        File file = new File(csvFilePath);
        if (file.exists()) {
            if (file.delete())
                System.out.println("Existing file deleted: " + csvFilePath);
            else{
                System.out.println("Failed to delete existing file: " + csvFilePath);
                return false;
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath))) {
            Path directoryPath = Paths.get("files");
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            writer.write("Nome do grupo: " + grupo + "\n\nElementos do grupo: \n");

            String elementos = server.getListaCSVBDQuery("SELECT Nome_elementos FROM Elementos_Grupo WHERE Nome = '" + grupo + "'");
            writer.write(elementos + "\n\n\nDescrição    Data    Valor    Pago por    A dividir com\n\n\n");

            String despesas = server.getListarBDQuery("SELECT descricao,data, valor, quem_pagou_total, dividido_por FROM Despesas WHERE nome_grupo = '" + grupo + "' ORDER BY data");
            if(!despesas.isEmpty()) {
                for (int i = 0 ; i < despesas.split(",").length; i+=5) {
                    String[] despesa = despesas.split(",");
                    writer.write(String.format("\"%s\"    \"%s\"    \"%s\"    \"%s\"    \"%s\"\n\n", despesa[i], despesa[i+1], despesa[i+2], despesa[i+3], despesa[i+4]));
                }
            }else
                writer.write("No expenses found!");

            System.out.println("Arquivo CSV criado com sucesso em " + csvFilePath);
            return true;
        } catch (IOException e) {
            System.out.println("Erro ao criar o arquivo CSV: " + e.getMessage());
            return false;
        }
    }

    private int verificaSignUp(String email, String telemovel, String password, String nome) {
        if (telemovel.isEmpty() || password.isEmpty() || nome.isEmpty()|| email.isEmpty()) {
            return 0;
        }
        if(telemovel.length() != 9 || (!telemovel.startsWith("91") && !telemovel.startsWith("92") && !telemovel.startsWith("93") && !telemovel.startsWith("96")))
            return 3;

        String result = server.getBDQuery("SELECT email FROM Utilizador WHERE email = '" + email + "'");

        if(result.isEmpty()){
            String query = "INSERT INTO Utilizador (email,telefone,password,nome) VALUES ('" + email.toLowerCase() + "','" + telemovel + "','" + password + "','" + nome + "')";
            server.updateBD(query);
            return 1;
        }
        else return 2;
    }

    private boolean verificaLogin (String username, String password) {
        if(username.isEmpty() || password.isEmpty())
            return false;

        String result = server.getBDQuery("SELECT password FROM Utilizador WHERE email = '" + username + "'");
        return result.equals(password);
    }

    private int validaDespesa (String parametros){
        String [] args = parametros.split("-");

        if(args[5].equalsIgnoreCase("Equally"))
            args[5] = pessoasPertencentesGrupo(args[0]).replace(",", " ");
        else
            args[5] = args[5].replace(",", "");

        if(Float.parseFloat(args[2])<0) return 3;
        float preco_individual = Float.parseFloat(args[2])/(args[5].split(" ").length);
        if(!server.getBDQuery("SELECT nome_grupo FROM Despesas WHERE nome_grupo = '"+ args[0] +"' AND descricao = '"+ args[1] +"' AND valor = '" + args[2] +"' AND data = '"+ args[3]  +"' AND quem_pagou_total = '" +args[4] + "' AND dividido_por LIKE '%" +  args[5] + "%' AND valor_individual = '" + preco_individual + "'").isEmpty())
            return 0;
        else
            server.updateBD("INSERT INTO Despesas (nome_grupo,descricao,valor,data,quem_pagou_total,dividido_por,pago_por,valor_individual) VALUES ('" + args[0] + "','"+ args[1] + "','" + args[2] + "','" + args[3] + "','"+ args[4] + "','"+ args[5] + "','" + args[4] +"','"+preco_individual +"')" );
        return 1;
    }
    private String pessoasPertencentesGrupo (String grupo) {
        return server.getListarBDQuery("SELECT Nome_Elementos FROM Elementos_Grupo WHERE Nome = '" + grupo +"'");
    }
}
