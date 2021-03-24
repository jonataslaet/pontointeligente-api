package br.com.jonataslaet.pontointeligenteapi.controllers.dtos;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

public class AuthenticationDTO {

	@NotEmpty(message = "Email não pode ser vazio.")
	@Email(message = "Email inválido")
	String email;

	@NotEmpty(message = "Senha não pode ser vazia.")
	String senha;

	public AuthenticationDTO() {
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

}
