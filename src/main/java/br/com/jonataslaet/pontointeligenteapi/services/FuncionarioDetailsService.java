package br.com.jonataslaet.pontointeligenteapi.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.jonataslaet.pontointeligenteapi.configs.FuncionarioDetalhadoFactory;
import br.com.jonataslaet.pontointeligenteapi.domain.Funcionario;

@Service
public class FuncionarioDetailsService implements UserDetailsService{

	@Autowired
	private FuncionarioServiceInterface funcionarioService;
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Optional<Funcionario> funcionario = funcionarioService.buscarPorEmail(email);
		if(funcionario.isPresent()) {
			return FuncionarioDetalhadoFactory.create(funcionario.get());
		}
		throw new UsernameNotFoundException(email);
	}

}
