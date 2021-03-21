package br.com.jonataslaet.pontointeligenteapi.controllers.dtos;

import java.math.BigDecimal;
import java.util.Optional;

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
@RequestMapping(value="/api/funcionarios")
public class PessoaFisicaController {

	private static final Logger log = LoggerFactory.getLogger(PessoaFisicaController.class);
	
	@Autowired
	private EmpresaServiceInterface empresaService;
	
	@Autowired
	private FuncionarioServiceInterface funcionarioService;

	public PessoaFisicaController() {
	}
	
	@PostMapping
	public ResponseEntity<Response<PessoaFisicaNewDTO>> cadastrar(@Valid @RequestBody PessoaFisicaNewDTO cadastroPF, BindingResult result) {
		
		log.info("Cadastrando pessoa física: {}", cadastroPF.toString());
		Response<PessoaFisicaNewDTO> response = new Response<PessoaFisicaNewDTO>();
		
		validarDadosExistentes(cadastroPF, result);
		Funcionario funcionario = this.converterDtoParaFuncionario(cadastroPF, result);
		
		if (result.hasErrors()) {
			log.error("Erro validando dados ao cadastrar pessoa física: {}", result.getAllErrors());
			result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}
		 
		Optional<Empresa> empresa = this.empresaService.buscarPorCnpj(cadastroPF.getCnpj());
		empresa.ifPresent(emp -> funcionario.setEmpresa(emp));
		this.funcionarioService.persistir(funcionario);
		
		response.setData(this.converterPessoaFisicaNewDTO(funcionario));
		return ResponseEntity.ok(response);
	}
	
	private void validarDadosExistentes(@Valid PessoaFisicaNewDTO cadastroPF, BindingResult result) {
		Optional<Empresa> empresa = this.empresaService.buscarPorCnpj(cadastroPF.getCnpj());
		if (!empresa.isPresent()) {
			result.addError(new ObjectError("empresa", "Empresa não cadastrada"));
		}
		this.funcionarioService.buscarPorCpf(cadastroPF.getCpf()).ifPresent(pf -> result.addError(new ObjectError("funcionario", "CPF já existente.")));
		this.funcionarioService.buscarPorEmail(cadastroPF.getEmail()).ifPresent(pf -> result.addError(new ObjectError("funcionario", "Email já existente.")));
	}
	
	private Funcionario converterDtoParaFuncionario(PessoaFisicaNewDTO pessoaFisicaDTO, BindingResult result) {
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
	
	private PessoaFisicaNewDTO converterPessoaFisicaNewDTO(Funcionario funcionario) {
		PessoaFisicaNewDTO pessoaFisicaDTO = new PessoaFisicaNewDTO();
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
}
