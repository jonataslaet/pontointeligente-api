package br.com.jonataslaet.pontointeligenteapi.services;

import java.util.Optional;

import br.com.jonataslaet.pontointeligenteapi.domain.Funcionario;

public interface FuncionarioServiceInterface {

	Funcionario persistir(Funcionario funcionario);
	
	Optional<Funcionario> buscarPorCpf(String cpf);
	
	Optional<Funcionario> buscarPorEmail(String email);
	
	Optional<Funcionario> buscarPorId(Long id);
}
