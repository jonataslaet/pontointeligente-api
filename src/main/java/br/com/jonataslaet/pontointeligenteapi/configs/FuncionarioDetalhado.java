package br.com.jonataslaet.pontointeligenteapi.configs;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class FuncionarioDetalhado implements UserDetails {
	private static final long serialVersionUID = 1L;
	
	private Long id;
	private String username;
	private String password;
	private Collection<? extends GrantedAuthority> authorities;
	
	public FuncionarioDetalhado(Long id2, String email, String senha, List<GrantedAuthority> mapToGrantedAuthorities) {
		this.id = id2;
		this.username = email;
		this.password = senha;
		this.authorities = mapToGrantedAuthorities;
	}

	public Long getId() {
		return this.id;
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}
