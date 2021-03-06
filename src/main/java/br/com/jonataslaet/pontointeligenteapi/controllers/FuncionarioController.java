package br.com.jonataslaet.pontointeligenteapi.controllers;

import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.jonataslaet.pontointeligenteapi.controllers.dtos.FuncionarioDTO;
import br.com.jonataslaet.pontointeligenteapi.controllers.dtos.FuncionarioNewDTO;
import br.com.jonataslaet.pontointeligenteapi.controllers.dtos.FuncionarioUpdateDTO;
import br.com.jonataslaet.pontointeligenteapi.controllers.dtos.Response;
import br.com.jonataslaet.pontointeligenteapi.domain.Empresa;
import br.com.jonataslaet.pontointeligenteapi.domain.Funcionario;
import br.com.jonataslaet.pontointeligenteapi.domain.enums.PerfilEnum;
import br.com.jonataslaet.pontointeligenteapi.security.PasswordUtils;
import br.com.jonataslaet.pontointeligenteapi.services.EmpresaServiceInterface;
import br.com.jonataslaet.pontointeligenteapi.services.FuncionarioServiceInterface;

@RestController
@RequestMapping(value="/api/funcionarios")
public class FuncionarioController {

	private static final Logger log = LoggerFactory.getLogger(FuncionarioController.class);
	
	@Autowired
	private EmpresaServiceInterface empresaService;
	
	@Autowired
	private FuncionarioServiceInterface funcionarioService;

	public FuncionarioController() {
	}
	
	@GetMapping(value="/{id}")
	public ResponseEntity<Response<FuncionarioDTO>> buscarPorId(@PathVariable("id") Long id){
		
		log.info("Buscando funcion??rio de ID: {}", id);
		
		Response<FuncionarioDTO> response = new Response<FuncionarioDTO>();
		Optional<Funcionario> funcionario = funcionarioService.buscarPorId(id);
		
		if (!funcionario.isPresent()) {
			log.info("Funcion??rio n??o encontrado para o ID: {}", id);
			response.getErrors().add("Funcion??rio n??o encontrado para o ID " + id);
			return ResponseEntity.badRequest().body(response);
		}
		
		response.setData(this.converterFuncionarioDTO(funcionario.get()));
		return ResponseEntity.ok(response);
	}
	
	@PutMapping(value="/{id}")
	public ResponseEntity<Response<FuncionarioUpdateDTO>> atualizar(@PathVariable("id") Long id, 
			@Valid @RequestBody FuncionarioUpdateDTO funcionarioUpdateDTO, BindingResult result){
		log.info("Atualizando funcion??rio: {}", funcionarioUpdateDTO.toString());
		Response<FuncionarioUpdateDTO> response = new Response<FuncionarioUpdateDTO>();
		
		Optional<Funcionario> funcionario = this.funcionarioService.buscarPorId(id);
		
		if (!funcionario.isPresent()) {
			result.addError(new ObjectError("funcionario", "Funcion??rio n??o encontrado."));
		}
		
		this.atualizarDadosfuncionario(funcionario.get(), funcionarioUpdateDTO, result);
		if (result.hasErrors()) {
			log.error("Erro de valida????o ao atualizar funcion??rio: {}", result.getAllErrors());
			result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}
		
		this.funcionarioService.persistir(funcionario.get());
		response.setData(this.converterPessoaFisicaUpdateDTO(funcionario.get()));
		
		return ResponseEntity.ok(response);
	}
	
	private void atualizarDadosfuncionario(Funcionario funcionario, @Valid FuncionarioUpdateDTO funcionarioUpdateDTO,
			BindingResult result) {
		funcionario.setNome(funcionarioUpdateDTO.getNome());
		
		if (!funcionario.getEmail().equals(funcionarioUpdateDTO.getEmail())) {
			this.funcionarioService.buscarPorEmail(funcionarioUpdateDTO.getEmail())
				.ifPresent(func -> result.addError(new ObjectError("email", "Email j?? existente")));
			funcionario.setEmail(funcionarioUpdateDTO.getEmail());
		}
		
		funcionario.setQtdHorasAlmoco(null);
		funcionarioUpdateDTO.getQtdHorasAlmoco().ifPresent(qtdHorasAlmoco -> funcionario.setQtdHorasAlmoco(Float.valueOf(qtdHorasAlmoco)));
		
		funcionario.setQtdHorasTrabalhoDia(null);
		funcionarioUpdateDTO.getQtdHorasTrabalhoDia().ifPresent(qtdHorasTrabalhoDia -> funcionario.setQtdHorasTrabalhoDia(Float.valueOf(qtdHorasTrabalhoDia)));
		
		funcionario.setValorHora(null);
		funcionarioUpdateDTO.getValorHora().ifPresent(valorHora -> funcionario.setValorHora(new BigDecimal(valorHora)));
		
		if (funcionarioUpdateDTO.getSenha().isPresent()) {
			funcionario.setSenha(PasswordUtils.gerarBCrypt(funcionarioUpdateDTO.getSenha().get()));
		}
	}

	@PostMapping
	public ResponseEntity<Response<FuncionarioNewDTO>> cadastrar(@Valid @RequestBody FuncionarioNewDTO cadastroPF, BindingResult result) {
		
		log.info("Cadastrando pessoa f??sica: {}", cadastroPF.toString());
		Response<FuncionarioNewDTO> response = new Response<FuncionarioNewDTO>();
		
		validarDadosExistentes(cadastroPF, result);
		Funcionario funcionario = this.converterDtoParaFuncionario(cadastroPF, result);
		
		if (result.hasErrors()) {
			log.error("Erro validando dados ao cadastrar pessoa f??sica: {}", result.getAllErrors());
			result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}
		 
		Optional<Empresa> empresa = this.empresaService.buscarPorCnpj(cadastroPF.getCnpj());
		empresa.ifPresent(emp -> funcionario.setEmpresa(emp));
		this.funcionarioService.persistir(funcionario);
		
		response.setData(this.converterPessoaFisicaNewDTO(funcionario));
		return ResponseEntity.ok(response);
	}
	
	private void validarDadosExistentes(@Valid FuncionarioNewDTO cadastroPF, BindingResult result) {
		Optional<Empresa> empresa = this.empresaService.buscarPorCnpj(cadastroPF.getCnpj());
		if (!empresa.isPresent()) {
			result.addError(new ObjectError("empresa", "Empresa n??o cadastrada"));
		}
		this.funcionarioService.buscarPorCpf(cadastroPF.getCpf()).ifPresent(pf -> result.addError(new ObjectError("funcionario", "CPF j?? existente.")));
		this.funcionarioService.buscarPorEmail(cadastroPF.getEmail()).ifPresent(pf -> result.addError(new ObjectError("funcionario", "Email j?? existente.")));
	}
	
	private Funcionario converterDtoParaFuncionario(FuncionarioNewDTO pessoaFisicaDTO, BindingResult result) {
		Funcionario funcionario = new Funcionario();
		funcionario.setNome(pessoaFisicaDTO.getNome());
		funcionario.setEmail(pessoaFisicaDTO.getEmail());
		funcionario.setCpf(pessoaFisicaDTO.getCpf());
		funcionario.setPerfilEnum(PerfilEnum.ROLE_USUARIO);
		funcionario.setSenha(PasswordUtils.gerarBCrypt(pessoaFisicaDTO.getSenha()));
		
		pessoaFisicaDTO.getQtdHorasAlmoco().ifPresent(qtdHorasAlmoco -> funcionario.setQtdHorasAlmoco(Float.valueOf(qtdHorasAlmoco)));
		pessoaFisicaDTO.getQtdHorasTrabalhoDia().ifPresent(qtdHorasTrabalhoDia -> funcionario.setQtdHorasTrabalhoDia(Float.valueOf(qtdHorasTrabalhoDia)));
		pessoaFisicaDTO.getValorHora().ifPresent(valorHora -> funcionario.setValorHora(new BigDecimal(valorHora)));
		
		return funcionario;
	}
	
	private FuncionarioNewDTO converterPessoaFisicaNewDTO(Funcionario funcionario) {
		FuncionarioNewDTO pessoaFisicaDTO = new FuncionarioNewDTO();
		pessoaFisicaDTO.setId(funcionario.getId());
		pessoaFisicaDTO.setNome(funcionario.getNome());
		pessoaFisicaDTO.setEmail(funcionario.getEmail());
		pessoaFisicaDTO.setCpf(funcionario.getCpf());
		pessoaFisicaDTO.setCnpj(funcionario.getEmpresa().getCnpj());
		
		funcionario.getQtdHorasAlmocOpt().ifPresent(qtdHorasAlmoco -> pessoaFisicaDTO.setQtdHorasAlmoco(Optional.of(Float.toString(qtdHorasAlmoco))));
		funcionario.getQtdHorasTrabalhoDiaOpt().ifPresent(qtdHorasTrabalhoDia -> pessoaFisicaDTO.setQtdHorasTrabalhoDia(Optional.of(Float.toString(qtdHorasTrabalhoDia))));
		funcionario.getValorHoraOpt().ifPresent(valorHora -> pessoaFisicaDTO.setValorHora(Optional.of(valorHora.toString())));
		
		return pessoaFisicaDTO;
	}
	
	private FuncionarioUpdateDTO converterPessoaFisicaUpdateDTO(Funcionario funcionario) {
		FuncionarioUpdateDTO pessoaFisicaDTO = new FuncionarioUpdateDTO();
		pessoaFisicaDTO.setId(funcionario.getId());
		pessoaFisicaDTO.setNome(funcionario.getNome());
		pessoaFisicaDTO.setEmail(funcionario.getEmail());
		
		funcionario.getQtdHorasAlmocOpt().ifPresent(qtdHorasAlmoco -> pessoaFisicaDTO.setQtdHorasAlmoco(Optional.of(Float.toString(qtdHorasAlmoco))));
		funcionario.getQtdHorasTrabalhoDiaOpt().ifPresent(qtdHorasTrabalhoDia -> pessoaFisicaDTO.setQtdHorasTrabalhoDia(Optional.of(Float.toString(qtdHorasTrabalhoDia))));
		funcionario.getValorHoraOpt().ifPresent(valorHora -> pessoaFisicaDTO.setValorHora(Optional.of(valorHora.toString())));
		
		return pessoaFisicaDTO;
	}
	
	private FuncionarioDTO converterFuncionarioDTO(Funcionario funcionario) {
		FuncionarioDTO funcionarioDTO = new FuncionarioDTO();
		funcionarioDTO.setId(funcionario.getId());
		funcionarioDTO.setNome(funcionario.getNome());
		funcionarioDTO.setCpf(funcionario.getCpf());
		
		return funcionarioDTO;
	}
}
