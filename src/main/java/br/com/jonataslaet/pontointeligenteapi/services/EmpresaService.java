package br.com.jonataslaet.pontointeligenteapi.services;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.jonataslaet.pontointeligenteapi.domain.Empresa;
import br.com.jonataslaet.pontointeligenteapi.repositories.EmpresaRepository;

@Service
public class EmpresaService implements EmpresaServiceInterface{

	private static final Logger log = LoggerFactory.getLogger(EmpresaService.class);
	
	@Autowired
	private EmpresaRepository empresaRepository;
	
	@Override
	public Optional<Empresa> buscarPorCnpj(String cnpj) {
		log.info("Buscando uma empresa para o CNPJ {}", cnpj);
		return Optional.ofNullable(empresaRepository.findByCnpj(cnpj));
	}

	@Override
	public Empresa persistir(Empresa empresa) {
		log.info("Persistindo empresa: {}", empresa);
		return this.empresaRepository.save(empresa);
	}

}
