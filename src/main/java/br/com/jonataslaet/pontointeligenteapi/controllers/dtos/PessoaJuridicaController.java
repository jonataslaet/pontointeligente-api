package br.com.jonataslaet.pontointeligenteapi.controllers.dtos;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.jonataslaet.pontointeligenteapi.PasswordUtils;
import br.com.jonataslaet.pontointeligenteapi.domain.Empresa;
import br.com.jonataslaet.pontointeligenteapi.domain.Funcionario;
import br.com.jonataslaet.pontointeligenteapi.domain.enums.PerfilEnum;
import br.com.jonataslaet.pontointeligenteapi.services.EmpresaServiceInterface;
import br.com.jonataslaet.pontointeligenteapi.services.FuncionarioServiceInterface;

@RestController
@RequestMapping(value = "/api/empresas")
public class PessoaJuridicaController {
	private static final Logger log = LoggerFactory.getLogger(PessoaJuridicaController.class);
	
	@Autowired
	private FuncionarioServiceInterface funcionarioService;
	
	@Autowired
	private EmpresaServiceInterface empresaService;
	
	public PessoaJuridicaController() {
		
	}
	
	@PostMapping
	public ResponseEntity<Response<PessoaJuridicaNewDTO>> cadastrar(@Valid @RequestBody PessoaJuridicaNewDTO cadastroPJDto, BindingResult result){
		
		log.info("Cadastrando Pessoa Jurídica: {}", cadastroPJDto.toString());
		Response<PessoaJuridicaNewDTO> response = new Response<PessoaJuridicaNewDTO>();
		
		validarDadosExistentes(cadastroPJDto, result);
		
		Empresa empresa = this.converterDtoParaEmpresa(cadastroPJDto);
		Funcionario funcionario = this.converterDtoParaFuncionario(cadastroPJDto, result);
		
		if (result.hasErrors()) {
			log.error("Erro ao validar dados de cadastro de pessoa jurídica: {}", result.getAllErrors());
			result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}
		
		this.empresaService.persistir(empresa);
		funcionario.setEmpresa(empresa);
		this.funcionarioService.persistir(funcionario);
		
		response.setData(this.converterPessoaJuridicaNewDTO(funcionario));
		return ResponseEntity.ok(response);
	}

	private PessoaJuridicaNewDTO converterPessoaJuridicaNewDTO(Funcionario funcionario) {
		PessoaJuridicaNewDTO cadastroPJDto = new PessoaJuridicaNewDTO();
		cadastroPJDto.setId(funcionario.getId());
		cadastroPJDto.setNome(funcionario.getNome());
		cadastroPJDto.setEmail(funcionario.getEmail());
		cadastroPJDto.setCpf(funcionario.getCpf());
		cadastroPJDto.setRazaoSocial(funcionario.getEmpresa().getRazaoSocial());
		cadastroPJDto.setCnpj(funcionario.getEmpresa().getCnpj());
		
		return cadastroPJDto;
	}

	private void validarDadosExistentes(@Valid PessoaJuridicaNewDTO cadastroPJDto, BindingResult result) {
		this.empresaService.buscarPorCnpj(cadastroPJDto.getCnpj()).ifPresent(emp -> result.addError(new ObjectError("empresa", "Empresa já existente.")));
		this.funcionarioService.buscarPorCpf(cadastroPJDto.getCpf()).ifPresent(pf -> result.addError(new ObjectError("funcionario", "CPF já existente.")));
		this.funcionarioService.buscarPorEmail(cadastroPJDto.getEmail()).ifPresent(pf -> result.addError(new ObjectError("funcionario", "Email já existente.")));
	}
	
	private Empresa converterDtoParaEmpresa(PessoaJuridicaNewDTO empresaDto) {
		Empresa empresa = new Empresa();
		empresa.setCnpj(empresaDto.getCnpj());
		empresa.setRazaoSocial(empresaDto.getRazaoSocial());
		return empresa;
	}
	
	private Funcionario converterDtoParaFuncionario(PessoaJuridicaNewDTO empresaDto, BindingResult result) {
		Funcionario funcionario = new Funcionario();
		funcionario.setNome(empresaDto.getNome());
		funcionario.setEmail(empresaDto.getEmail());
		funcionario.setCpf(empresaDto.getCpf());
		funcionario.setPerfilEnum(PerfilEnum.ROLE_ADMIN);
		funcionario.setSenha(PasswordUtils.gerarBCrypt(empresaDto.getSenha()));
		
		return funcionario;
	}

}
