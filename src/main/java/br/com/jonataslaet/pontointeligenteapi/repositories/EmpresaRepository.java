package br.com.jonataslaet.pontointeligenteapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.com.jonataslaet.pontointeligenteapi.domain.Empresa;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long>{

	@Transactional(readOnly=true)
	Empresa findByCnpj(String cnpj);
}
