package br.com.jonataslaet.pontointeligenteapi.controllers.dtos;

public class FuncionarioDTO {

	private Long id;
	private String nome;
	private String cpf;
	
	public FuncionarioDTO() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}
	
}
