package br.com.jonataslaet.pontointeligenteapi.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;

import javax.validation.Valid;

import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.jonataslaet.pontointeligenteapi.controllers.dtos.LancamentoDTO;
import br.com.jonataslaet.pontointeligenteapi.controllers.dtos.Response;
import br.com.jonataslaet.pontointeligenteapi.domain.Funcionario;
import br.com.jonataslaet.pontointeligenteapi.domain.Lancamento;
import br.com.jonataslaet.pontointeligenteapi.domain.enums.TipoLancamentoEnum;
import br.com.jonataslaet.pontointeligenteapi.services.FuncionarioServiceInterface;
import br.com.jonataslaet.pontointeligenteapi.services.LancamentoServiceInterface;

@RestController
@RequestMapping(value = "/api/lancamentos")
public class LancamentoController {
	private static final Logger log = LoggerFactory.getLogger(LancamentoController.class);
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Autowired
	private FuncionarioServiceInterface funcionarioService;
	
	@Autowired
	private LancamentoServiceInterface lancamentoService;
	
	@Value("${paginacao.qtd_por_pagina}")
	private int qtdPorPagina;
	
	public LancamentoController() {
		
	}
	
	@GetMapping("/funcionarios/{funcionarioId}")
	public ResponseEntity<Response<Page<LancamentoDTO>>> listaPorFuncionarioId(
			@PathVariable("funcionarioId") Long idFuncionario,
			@RequestParam(value="pag", defaultValue="0") int pag,
			@RequestParam(value="ord", defaultValue="id") String ord,
			@RequestParam(value="dir", defaultValue="DESC") String dir
		){
		
		log.info("Buscando lançamentos por ID do Funcionário: {}, página: {}", idFuncionario, pag);
		Response<Page<LancamentoDTO>> response = new Response<Page<LancamentoDTO>>();
		
		PageRequest pageRequest = PageRequest.of(pag, this.qtdPorPagina, Direction.valueOf(dir), ord);
		Page<Lancamento> lancamentos = this.lancamentoService.buscarPorFuncionarioId(idFuncionario, pageRequest);
		Page<LancamentoDTO> lancamentosDTO = lancamentos.map(lancamento -> converterParaLancamentoDTO(lancamento));
		
		response.setData(lancamentosDTO);
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Response<LancamentoDTO>> buscarPorId(@PathVariable("id") Long id){
		log.info("Buscando lançamento por ID: {}, página: {}", id);
		Response<LancamentoDTO> response = new Response<LancamentoDTO>();
		
		Optional<Lancamento> lancamento = lancamentoService.buscarPorId(id);
		
		if (!lancamento.isPresent()) {
			log.info("Lançamento não encontrado para o ID: {}", id);
			response.getErrors().add("Lançamento não encontrado para o ID " + id);
			return ResponseEntity.badRequest().body(response);
		}
		
		response.setData(this.converterParaLancamentoDTO(lancamento.get()));
		return ResponseEntity.ok(response);
		
	}
	
	@PostMapping
	public ResponseEntity<Response<LancamentoDTO>> cadastrar(@Valid @RequestBody LancamentoDTO lancamentoDTO, BindingResult result) throws ParseException{
		log.info("Cadastrando lançamento: {}", result.getAllErrors());
		Response<LancamentoDTO> response = new Response<LancamentoDTO>();
		validarFuncionario(lancamentoDTO, result);
		Lancamento lancamento = this.converterLancamentoDTOparaLancamento(lancamentoDTO, result);
		
		if (result.hasErrors()) {
			log.info("Erro ao validar lançamento: {}", result.getAllErrors());
			result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}
		
		lancamento = this.lancamentoService.persistir(lancamento);
		response.setData(this.converterParaLancamentoDTO(lancamento));
		return ResponseEntity.ok(response);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Response<LancamentoDTO>> atualizar(@PathVariable("id") Long id, 
			@Valid @RequestBody LancamentoDTO lancamentoDTO, BindingResult result) throws ParseException{
		log.info("Atualizando lançamento: {}", lancamentoDTO.toString());
		Response<LancamentoDTO> response = new Response<LancamentoDTO>();
		validarFuncionario(lancamentoDTO, result);
		lancamentoDTO.setId(Optional.of(id));
		Lancamento lancamento = this.converterLancamentoDTOparaLancamento(lancamentoDTO, result);
		
		if (result.hasErrors()) {
			log.info("Erro ao validar lançamento: {}", result.getAllErrors());
			result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}
		
		lancamento = this.lancamentoService.persistir(lancamento);
		response.setData(this.converterParaLancamentoDTO(lancamento));
		return ResponseEntity.ok(response);
	} 
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Response<String>> deletar(@PathVariable("id") Long id){
		log.info("Removendo o lançamento de ID: {}", id);
		Response<String> response = new Response<String>();
		
		Optional<Lancamento> lancamento = lancamentoService.buscarPorId(id);
		
		if (!lancamento.isPresent()) {
			log.info("Erro ao remover lançamento de ID: {}", id);
			response.getErrors().add("Erro ao remover lançamento de ID: " + id);
			return ResponseEntity.badRequest().body(response);
		}
		
		this.lancamentoService.remover(id);
		return ResponseEntity.ok(new Response<String>());
		
	}
	
	private Lancamento converterLancamentoDTOparaLancamento(@Valid LancamentoDTO lancamentoDTO, BindingResult result) throws ParseException {
		Lancamento lancamento = new Lancamento();

		if (lancamentoDTO.getId().isPresent()) {
			Optional<Lancamento> lanc = this.lancamentoService.buscarPorId(lancamentoDTO.getId().get());
			if (lanc.isPresent()) {
				lancamento = lanc.get();
			} else {
				result.addError(new ObjectError("lancamento", "Lançamento não encontrado."));
			}
		} else {
			lancamento.setFuncionario(new Funcionario());
			lancamento.getFuncionario().setId(lancamentoDTO.getFuncionarioId());
		}

		lancamento.setDescricao(lancamentoDTO.getDescricao());
		lancamento.setLocalizacao(lancamentoDTO.getLocalizacao());
		lancamento.setData(this.dateFormat.parse(lancamentoDTO.getData()));

		if (EnumUtils.isValidEnum(TipoLancamentoEnum.class, lancamentoDTO.getTipo())) {
			lancamento.setTipo(TipoLancamentoEnum.valueOf(lancamentoDTO.getTipo()));
		} else {
			result.addError(new ObjectError("tipo", "Tipo inválido."));
		}

		return lancamento;
	}

	private void validarFuncionario(LancamentoDTO lancamentoDTO, BindingResult result) {
		if (lancamentoDTO.getFuncionarioId() == null) {
			result.addError(new ObjectError("funcionario", "Funcionário não informado."));
			return;
		}
		
		log.info("Validando funcionário de ID {}", lancamentoDTO.getFuncionarioId());
		Optional<Funcionario> funcionario = this.funcionarioService.buscarPorId(lancamentoDTO.getFuncionarioId());
		if (!funcionario.isPresent()) {
			result.addError(new ObjectError("funcionario", "Funcionário não encontrado. ID inexistente."));
		}
	}
	
	private LancamentoDTO converterParaLancamentoDTO(Lancamento lancamento) {
		LancamentoDTO lancamentoDTO = new LancamentoDTO();
		lancamentoDTO.setId(Optional.of(lancamento.getId()));
		lancamentoDTO.setData(this.dateFormat.format(lancamento.getData()));
		lancamentoDTO.setTipo(lancamento.getTipo().toString());
		lancamentoDTO.setDescricao(lancamento.getDescricao());
		lancamentoDTO.setLocalizacao(lancamento.getLocalizacao());
		lancamentoDTO.setFuncionarioId(lancamento.getFuncionario().getId());
		return lancamentoDTO;
	}

}
