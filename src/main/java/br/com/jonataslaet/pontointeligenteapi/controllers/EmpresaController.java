package br.com.jonataslaet.pontointeligenteapi.controllers;

import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.jonataslaet.pontointeligenteapi.controllers.dtos.EmpresaDTO;
import br.com.jonataslaet.pontointeligenteapi.controllers.dtos.EmpresaNewDTO;
import br.com.jonataslaet.pontointeligenteapi.controllers.dtos.Response;
import br.com.jonataslaet.pontointeligenteapi.domain.Empresa;
import br.com.jonataslaet.pontointeligenteapi.domain.Funcionario;
import br.com.jonataslaet.pontointeligenteapi.domain.enums.PerfilEnum;
import br.com.jonataslaet.pontointeligenteapi.security.PasswordUtils;
import br.com.jonataslaet.pontointeligenteapi.services.EmpresaServiceInterface;
import br.com.jonataslaet.pontointeligenteapi.services.FuncionarioServiceInterface;

@RestController
@RequestMapping(value = "/api/empresas")
public class EmpresaController {
	private static final Logger log = LoggerFactory.getLogger(EmpresaController.class);
	
	@Autowired
	private FuncionarioServiceInterface funcionarioService;
	
	@Autowired
	private EmpresaServiceInterface empresaService;
	
	public EmpresaController() {
		
	}
	
	@GetMapping(value="/{cnpj}")
	public ResponseEntity<Response<EmpresaDTO>> buscarPorCnpj(@PathVariable("cnpj") String cnpj){
		
		log.info("Buscando empresa por CNPJ: {}", cnpj);
		
		Response<EmpresaDTO> response = new Response<EmpresaDTO>();
		Optional<Empresa> empresa = empresaService.buscarPorCnpj(cnpj);
		
		if (!empresa.isPresent()) {
			log.info("Empresa n??o encontrada para o CNPJ: {}", cnpj);
			response.getErrors().add("Empresa n??o encontrada para o CNPJ " + cnpj);
			return ResponseEntity.badRequest().body(response);
		}
		
		response.setData(this.converterEmpresaDTO(empresa.get()));
		return ResponseEntity.ok(response);
	}
	
	@PostMapping
	public ResponseEntity<Response<EmpresaNewDTO>> cadastrar(@Valid @RequestBody EmpresaNewDTO cadastroPJDto, BindingResult result){
		
		log.info("Cadastrando Pessoa Jur??dica: {}", cadastroPJDto.toString());
		Response<EmpresaNewDTO> response = new Response<EmpresaNewDTO>();
		
		validarDadosExistentes(cadastroPJDto, result);
		
		Empresa empresa = this.converterDtoParaEmpresa(cadastroPJDto);
		Funcionario funcionario = this.converterDtoParaFuncionario(cadastroPJDto, result);
		
		if (result.hasErrors()) {
			log.error("Erro ao validar dados de cadastro de pessoa jur??dica: {}", result.getAllErrors());
			result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}
		
		this.empresaService.persistir(empresa);
		funcionario.setEmpresa(empresa);
		this.funcionarioService.persistir(funcionario);
		
		response.setData(this.converterPessoaJuridicaNewDTO(funcionario));
		return ResponseEntity.ok(response);
	}

	private EmpresaNewDTO converterPessoaJuridicaNewDTO(Funcionario funcionario) {
		EmpresaNewDTO cadastroPJDto = new EmpresaNewDTO();
		cadastroPJDto.setId(funcionario.getId());
		cadastroPJDto.setNome(funcionario.getNome());
		cadastroPJDto.setEmail(funcionario.getEmail());
		cadastroPJDto.setCpf(funcionario.getCpf());
		cadastroPJDto.setRazaoSocial(funcionario.getEmpresa().getRazaoSocial());
		cadastroPJDto.setCnpj(funcionario.getEmpresa().getCnpj());
		
		return cadastroPJDto;
	}

	private void validarDadosExistentes(@Valid EmpresaNewDTO cadastroPJDto, BindingResult result) {
		this.empresaService.buscarPorCnpj(cadastroPJDto.getCnpj()).ifPresent(emp -> result.addError(new ObjectError("empresa", "Empresa j?? existente.")));
		this.funcionarioService.buscarPorCpf(cadastroPJDto.getCpf()).ifPresent(pf -> result.addError(new ObjectError("funcionario", "CPF j?? existente.")));
		this.funcionarioService.buscarPorEmail(cadastroPJDto.getEmail()).ifPresent(pf -> result.addError(new ObjectError("funcionario", "Email j?? existente.")));
	}
	
	private Empresa converterDtoParaEmpresa(EmpresaNewDTO empresaDto) {
		Empresa empresa = new Empresa();
		empresa.setCnpj(empresaDto.getCnpj());
		empresa.setRazaoSocial(empresaDto.getRazaoSocial());
		return empresa;
	}
	
	private Funcionario converterDtoParaFuncionario(EmpresaNewDTO empresaDto, BindingResult result) {
		Funcionario funcionario = new Funcionario();
		funcionario.setNome(empresaDto.getNome());
		funcionario.setEmail(empresaDto.getEmail());
		funcionario.setCpf(empresaDto.getCpf());
		funcionario.setPerfilEnum(PerfilEnum.ROLE_ADMIN);
		funcionario.setSenha(PasswordUtils.gerarBCrypt(empresaDto.getSenha()));
		
		return funcionario;
	}
	
	private EmpresaDTO converterEmpresaDTO(Empresa empresa) {
		EmpresaDTO empresaDTO = new EmpresaDTO();
		empresaDTO.setId(empresa.getId());
		empresaDTO.setCnpj(empresa.getCnpj());
		empresaDTO.setRazaoSocial(empresa.getRazaoSocial());
		
		return empresaDTO;
	}

}
