package pt.isec.pd.trabalho_pratico.Api.Securit;


import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import pt.isec.pd.trabalho_pratico.Rmi.ServerRMI;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

@Component
public class UserAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        ServerRMI serverRMI;
        try {
            serverRMI = ServerRMI.getInstance();
        } catch (RemoteException e) {
            throw new RuntimeException("Failed to get server instance",e);
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        if(username == null || password == null|| username.equals("\n") || password.equals("\n"))
            throw new BadCredentialsException("Required fields not filled.");

        String mensagem = serverRMI.verificaLogin(username, password);

        if(mensagem.contains("Welcome ")){
            serverRMI.notificar(username + " logged in!");
            return new UsernamePasswordAuthenticationToken(username, password, authorities);
        }
        serverRMI.notificar(username + " tried to login!");

        throw new BadCredentialsException(mensagem);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}
