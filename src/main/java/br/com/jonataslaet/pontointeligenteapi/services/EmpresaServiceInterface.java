package br.com.jonataslaet.pontointeligenteapi.services;

import java.util.Optional;

import br.com.jonataslaet.pontointeligenteapi.domain.Empresa;

public interface EmpresaServiceInterface {

	Optional<Empresa> buscarPorCnpj(String cnpj);
	Empresa persistir(Empresa empresa);
}
