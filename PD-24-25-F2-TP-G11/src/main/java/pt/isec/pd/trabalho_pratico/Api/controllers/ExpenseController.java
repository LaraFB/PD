package pt.isec.pd.trabalho_pratico.Api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.isec.pd.trabalho_pratico.Rmi.ServerRMI;

import java.rmi.RemoteException;
import java.util.Map;

@RestController
@RequestMapping("/api/despesas")
public class ExpenseController {

    @GetMapping("/{grupo}")
    public ResponseEntity<String> listarDespesas(@PathVariable String grupo, Authentication authentication) throws RemoteException {
        String username = authentication.getName();
        ServerRMI serverRMI = ServerRMI.getInstance();
        String mensagem = serverRMI.getListExpensesPertence(username,grupo);

        if(grupo == null || grupo.isEmpty())
            return ResponseEntity.badRequest().body("Required fields not filled.");
        if(mensagem.equals("There's no such group!") || mensagem.equals("You don't belong to this group!"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mensagem);
        if(mensagem.equals("There's no expense in this group!"))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mensagem);
        return ResponseEntity.ok(mensagem);
    }

    @PostMapping("/{grupo}")
    public ResponseEntity<String> adicionarDespesa(@PathVariable String grupo, Authentication authentication, @RequestBody Map<String, Object> despesa) throws RemoteException {
        ServerRMI serverRMI = ServerRMI.getInstance();

        String username = authentication.getName();
        String descricao = (String) despesa.get("descricao");
        String data = (String) despesa.get("data");
        String quemPagouTotal = (String) despesa.get("quem_pagou_total");
        String divididoPor = (String) despesa.get("dividido_por");
        float valord;
        
        try {
            String valor = (String) despesa.get("valor");
            valord = Float.parseFloat(valor);
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Invalid valor");
        }

        if(username == null || descricao == null || quemPagouTotal == null || divididoPor == null || data == null || valord == 0)
            return ResponseEntity.badRequest().body("Required fields not filled.");

        String mensagem = serverRMI.adicionarDespesa(username,grupo,descricao,valord,data,quemPagouTotal,divididoPor);

        if(mensagem.equals("There's no such group!"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mensagem);
        if(mensagem.equals("Expense added successfully!")) {
            serverRMI.notificar(username + " added an expense to " + grupo);
            return ResponseEntity.ok(mensagem);
        }
        System.out.println(mensagem);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mensagem);
    }


    @DeleteMapping("/{grupo}/despesa/{descricao}")
    public ResponseEntity<String> eliminarDespesa(@PathVariable String grupo, @PathVariable String descricao, Authentication authentication) throws RemoteException {
        String username = authentication.getName();
        ServerRMI serverRMI = ServerRMI.getInstance();

        if(grupo == null || descricao == null || username == null)
            return ResponseEntity.badRequest().body("Required fields not filled.");

        String mensagem = serverRMI.apagarDespesa(descricao, grupo,username);
        if(mensagem.equals("Expense deleted")) {
            serverRMI.notificar(username + " deleted an expense from " + grupo);
            return ResponseEntity.ok(mensagem);
        }
        else if(mensagem.equals("There's no such group!"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mensagem);
        else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mensagem);
    }

}
