package pt.isec.pd.trabalho_pratico.Api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pt.isec.pd.trabalho_pratico.Api.Securit.TokenService;
import pt.isec.pd.trabalho_pratico.Rmi.ServerRMI;

import java.rmi.RemoteException;
import java.util.Map;

@RestController
public class AuthController {
    private final TokenService tokenService;

    public AuthController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GetMapping("api/utilizadores/login")
    public String login(Authentication authentication) {
        return tokenService.generateToken(authentication);
    }

    @GetMapping("/authorization")
    public String authorization(Authentication authentication) {
        return authentication.getAuthorities().toString();
    }

    @PostMapping("api/utilizadores/register")
    public ResponseEntity<String> register(@RequestBody Map<String, Object> user) throws RemoteException {
        ServerRMI serverRMI = ServerRMI.getInstance();
        String nome = (String) user.get("nome");
        String telefone = (String) user.get("telefone");
        String email = (String) user.get("email");
        String password = (String) user.get("password");

        if(nome == null || telefone == null || email == null || password == null)
            return ResponseEntity.badRequest().body("Required fields not filled.");

        String mensagem = serverRMI.verificaSignUp(email,telefone,password,nome);

        if(mensagem.contains("Welcome ")){
            serverRMI.notificar(email + " signed up!");
            return ResponseEntity.ok(mensagem);
        }
        serverRMI.notificar(email + " tried signed up!");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mensagem);
    }
}