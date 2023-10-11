package br.com.ruanvictor.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.ruanvictor.todolist.user.IUserRepository;
import br.com.ruanvictor.todolist.user.UserModel;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

  @Autowired
  IUserRepository userRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    var authorization = request.getHeader("Authorization");
    var auth_encoded = authorization.substring("Basic".length()).trim();
    byte[] auth_decoded = Base64.getDecoder().decode(auth_encoded);
    String auth_string = new String(auth_decoded);
    String[] credentials = auth_string.split(":");
    String username = credentials[0];
    String password = credentials[1];
    UserModel user = this.userRepository.findByUsername(username);

    if (user == null) {
      response.sendError(Integer.valueOf(HttpStatus.UNAUTHORIZED.value()));
    } else {
      var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
      if (passwordVerify.verified) {
        filterChain.doFilter(request, response);
      } else {
        response.sendError(Integer.valueOf(HttpStatus.UNAUTHORIZED.value()));
      }
    }
  }

}
