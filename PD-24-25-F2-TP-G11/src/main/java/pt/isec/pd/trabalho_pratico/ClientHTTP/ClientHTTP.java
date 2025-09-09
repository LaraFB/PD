package pt.isec.pd.trabalho_pratico.ClientHTTP;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Scanner;

public class ClientHTTP {
    public static String sendrequest(String uri, String verb, String authorizationValue, String contentType, byte[] body) {
        String responseBody = "";
        HttpURLConnection connection = null;

        try {
            URL url = new URL(uri);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(verb);

            if (authorizationValue != null)
                connection.setRequestProperty("Authorization", authorizationValue);

            if (body != null && ((verb.equals("POST") || verb.equals("PUT")))) {
                connection.setDoOutput(true);
                if (contentType != null)
                    connection.setRequestProperty("Content-Type", contentType);

               // System.out.println("Request Body: " + new String(body, StandardCharsets.UTF_8));
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(body);
                    os.flush();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to write request body for " + verb + " request to " + uri,e);
                }
            }
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                System.out.println("Unauthorized request : Invalid credentials");
            }else {
                InputStream inputStream = connection.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST
                        ? connection.getErrorStream()
                        : connection.getInputStream();
                try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
                    responseBody = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : null;
                }
            }

            return responseBody;

        } catch (SocketTimeoutException e) {
            throw new RuntimeException("Request timed out: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("Request failed: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String token = "";
        boolean exit = false;

        while (!exit) {
            try{
                if (token.isEmpty())
                    token = loginRegistar(scanner);
                else
                    exit = menu(scanner, token);
            } catch (Exception e) {
                System.out.println("Error server left :( ");
                System.exit(1);
            }

        }
        System.out.println("Leaving...");
    }

    private static String loginRegistar(Scanner scanner){
        String token = "";
        boolean login = false;

        while (!login) {
            System.out.println("----------------------------\n Choose an option:\n 1 - Login\n 2 - Register new user\n ----------------------------");
            int escolha = Integer.parseInt(scanner.nextLine());
            String email, password;
            byte[] body;
            switch (escolha) {
                case 1:
                    System.out.println("Enter email: ");
                    email = scanner.nextLine();
                    System.out.println("Enter password: ");
                    password = scanner.nextLine();

                    if(email.isEmpty() || password.isEmpty()) {
                       System.out.println("Empty email or password");
                        break;
                    }
                    String credentials = Base64.getEncoder().encodeToString((email + ":" + password).getBytes());
                    body = ("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}").getBytes();
                    token = sendrequest("http://localhost:8080/api/utilizadores/login", "GET", "Basic " + credentials, "application/json", body);
                    login = (token != null);
                    break;
                case 2:
                    System.out.println("Enter name: ");
                    String name = scanner.nextLine();
                    System.out.println("Enter phone number: +351");
                    String phone = scanner.nextLine();
                    System.out.println("Enter password: ");
                    password = scanner.nextLine();
                    System.out.println("Enter email: ");
                    email = scanner.nextLine();

                    body = ("{\"nome\":\"" + name + "\",\"telefone\":\"" + phone + "\",\"email\":\"" + email + "\",\"password\":\"" + password + "\"}").getBytes();
                    String response = sendrequest("http://localhost:8080/api/utilizadores/reg", "POST", null, "application/json", body);
                    System.out.println(response);
                    break;
                default:
                    System.out.println("Invalid option!");
                    break;
            }
        }
        return token;
    }
    private static boolean menu(Scanner scanner, String token) {
        System.out.println("----------------------------");
        System.out.println("Choose an option: ");
        System.out.println("1 - List Groups");
        System.out.println("2 - Add Expenses");
        System.out.println("3 - List Expenses");
        System.out.println("4 - Delete Expenses");
        System.out.println("5 - Leave");
        System.out.println("----------------------------");

        int escolha = Integer.parseInt(scanner.nextLine());
        boolean exit = false;
        byte[] body;
        String grupo,uri, description;
        switch (escolha) {
            case 1:
                System.out.println("Groups:");
                System.out.println(sendrequest("http://localhost:8080/api/grupos/listGrupos", "GET", "Bearer " + token, null, null));
                break;
            case 2:
                System.out.println("Enter group name: ");
                grupo = scanner.nextLine();
                System.out.println("Enter description: ");
                description = scanner.nextLine();
                System.out.println("Enter expense value: ");
                String valor = scanner.nextLine();
                System.out.println("Enter date(YYYY/MM/DD): ");
                String data = scanner.nextLine();
                System.out.println("Enter who paid: ");
                String paidBy = scanner.nextLine();
                System.out.println("Enter split with: ");
                String splitBy = scanner.nextLine();

                body = ("{\"descricao\":\"" + description + "\",\"valor\":\"" + valor + "\",\"data\":\"" + data + "\",\"quem_pagou_total\":\"" + paidBy + "\",\"dividido_por\":\"" + splitBy + "\"}").getBytes();
                uri = "http://localhost:8080/api/despesas/" + URLEncoder.encode(grupo, StandardCharsets.UTF_8).replace("+", "%20") + "/addDespesa";
                System.out.println(sendrequest(uri, "POST", "Bearer " + token, "application/json", body));
                break;
            case 3:
                System.out.println("Enter group name: ");
                grupo = scanner.nextLine();
                if(grupo.isEmpty()) {
                    System.out.println("Empty group name!");
                    break;
                }
                uri = "http://localhost:8080/api/despesas/" + URLEncoder.encode(grupo, StandardCharsets.UTF_8).replace("+", "%20") ;
                System.out.println(sendrequest(uri, "GET", "Bearer " + token, null, null));
                break;
            case 4:
                System.out.println("Enter group name: ");
                grupo = scanner.nextLine();
                System.out.println("Enter description: ");
                description = scanner.nextLine();

                body = ("{\"grupo\":\"" + grupo + "\",\"descricao\":\"" + description + "\"}").getBytes();
                uri = "http://localhost:8080/api/despesas/" + URLEncoder.encode(grupo, StandardCharsets.UTF_8).replace("+", "%20") + "/delDespesa/" + URLEncoder.encode(description, StandardCharsets.UTF_8).replace("+", "%20") ;
                System.out.println(sendrequest(uri, "DELETE", "Bearer " + token, "application/json", body));
                break;
            case 5:
                exit = true;
                break;
            default:
                System.out.println("Invalid option!");
                break;
        }
        return exit;
    }
}