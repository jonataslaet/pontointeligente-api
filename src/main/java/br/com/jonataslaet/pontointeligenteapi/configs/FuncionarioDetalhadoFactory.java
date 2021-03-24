package br.com.jonataslaet.pontointeligenteapi.configs;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import br.com.jonataslaet.pontointeligenteapi.domain.Funcionario;
import br.com.jonataslaet.pontointeligenteapi.domain.enums.PerfilEnum;

public class FuncionarioDetalhadoFactory {

	public static FuncionarioDetalhado create(Funcionario funcionario) {
		return new FuncionarioDetalhado(funcionario.getId(), funcionario.getEmail(), funcionario.getSenha(), mapToGrantedAuthorities(funcionario.getPerfilEnum()));
	}
	
	private static List<GrantedAuthority> mapToGrantedAuthorities(PerfilEnum perfilEnum){
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority(perfilEnum.toString()));
		return authorities;
	}
}
