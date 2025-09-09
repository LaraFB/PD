package pt.isec.pd.trabalho_pratico.Api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.isec.pd.trabalho_pratico.Rmi.ServerRMI;

import java.rmi.RemoteException;

@RestController
@RequestMapping("/api/grupos")
public class GroupController {

    @GetMapping("/listGrupos")
    public ResponseEntity<String> listarGrupos(Authentication authentication) throws RemoteException {
        String username = authentication.getName();
        ServerRMI serverRMI = ServerRMI.getInstance();
        return ResponseEntity.ok(serverRMI.getListGruposPertence(username));
    }
}
