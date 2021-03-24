package br.com.jonataslaet.pontointeligenteapi.controllers;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.jonataslaet.pontointeligenteapi.controllers.dtos.AuthenticationDTO;
import br.com.jonataslaet.pontointeligenteapi.controllers.dtos.Response;
import br.com.jonataslaet.pontointeligenteapi.controllers.dtos.TokenDTO;
import br.com.jonataslaet.pontointeligenteapi.security.JWTUtil;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

	private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);
	private static final String TOKEN_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";
	
	@Autowired
	private AuthenticationManager auth;
	
	@Autowired
	private JWTUtil jwtUtil;
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	@PostMapping
	public ResponseEntity<Response<TokenDTO>> gerarTokenJWT(@Valid @RequestBody AuthenticationDTO authenticationDTO, BindingResult result) {
		Response<TokenDTO> response = new Response<TokenDTO>();
		
		if (result.hasErrors()) {
			log.error("Erro ao gerar token: {}", result.getAllErrors());
			result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}
		
		log.info("Gerando token para o email: {}", authenticationDTO.getEmail());
		Authentication authentication = auth.authenticate(new UsernamePasswordAuthenticationToken(authenticationDTO.getEmail(), authenticationDTO.getSenha()));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationDTO.getEmail());
		String token = jwtUtil.obterToken(userDetails);
		response.setData(new TokenDTO(token));
		return ResponseEntity.ok(response);
	}
	
	@PostMapping("/refresh")
	public ResponseEntity<Response<TokenDTO>> gerarRefreshTokenJWT(HttpServletRequest request) {
		log.info("Gerando refresh token");
		Response<TokenDTO> response = new Response<TokenDTO>();
		Optional<String> token = Optional.ofNullable(request.getHeader(TOKEN_HEADER));
		
		if (token.isPresent() && token.get().startsWith(BEARER_PREFIX)) {
			token = Optional.of(token.get().substring(7));
		}
		
		if (!token.isPresent()) {
			response.getErrors().add("Token não informado.");
		}
		else if (!jwtUtil.tokenValido(token.get())) {
			response.getErrors().add("Token inválido ou expirado");
		}
		String refreshedToken = jwtUtil.refreshToken(token.get());
		response.setData(new TokenDTO(refreshedToken));
		return ResponseEntity.ok(response);
	}
	
}
