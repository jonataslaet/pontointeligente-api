package br.com.jonataslaet.pontointeligenteapi.controllers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.jonataslaet.pontointeligenteapi.controllers.dtos.LancamentoDTO;
import br.com.jonataslaet.pontointeligenteapi.domain.Funcionario;
import br.com.jonataslaet.pontointeligenteapi.domain.Lancamento;
import br.com.jonataslaet.pontointeligenteapi.domain.enums.TipoLancamentoEnum;
import br.com.jonataslaet.pontointeligenteapi.services.FuncionarioServiceInterface;
import br.com.jonataslaet.pontointeligenteapi.services.LancamentoServiceInterface;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class LancamentoControllerTest {

	@Autowired
	private MockMvc mvc;
	
	@MockBean
	private LancamentoServiceInterface lancamentoService;
	
	@MockBean
	private FuncionarioServiceInterface funcionarioService;
	
	private static final String URL_BASE = "/api/lancamentos/";
	private static final Long ID_FUNCIONARIO = 1L;
	private static final Long ID_LANCAMENTO = 1L;
	private static final String TIPO = TipoLancamentoEnum.INICIO_TRABALHO.name();
	private static final Date DATA = new Date();
	
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Test
	@WithMockUser
	public void testCadastrarLancamento() throws Exception {
		Lancamento lancamento = obterDadosLancamento();
		BDDMockito.given(this.funcionarioService.buscarPorId(Mockito.anyLong())).willReturn(Optional.of(new Funcionario()));
		BDDMockito.given(this.lancamentoService.persistir(Mockito.any(Lancamento.class))).willReturn(lancamento);
		mvc.perform(MockMvcRequestBuilders.post(URL_BASE).content(this.obterJsonRequisicaoPost()).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.id").value(ID_LANCAMENTO))
			.andExpect(jsonPath("$.data.tipo").value(TIPO))
			.andExpect(jsonPath("$.data.data").value(this.dateFormat.format(DATA)))
			.andExpect(jsonPath("$.data.funcionarioId").value(ID_FUNCIONARIO))
			.andExpect(jsonPath("$.errors").isEmpty());
	}
	
	@Test
	@WithMockUser
	public void testCadastrarLancamentoIdInvalido() throws JsonProcessingException, Exception {
		BDDMockito.given(this.funcionarioService.buscarPorId(Mockito.anyLong())).willReturn(Optional.empty());
		
		mvc.perform(MockMvcRequestBuilders.post(URL_BASE).content(this.obterJsonRequisicaoPost()).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors").value("Funcionário não encontrado. ID inexistente."))
			.andExpect(jsonPath("$.data").isEmpty());
		
	}
	
	@Test
	@WithMockUser(username = "jonataslaet@gmail.com", roles = {"ADMIN"})
	public void testRemoverLancamento() throws Exception {
		BDDMockito.given(this.lancamentoService.buscarPorId(Mockito.anyLong())).willReturn(Optional.of(new Lancamento()));
		mvc.perform(MockMvcRequestBuilders.delete(URL_BASE + ID_LANCAMENTO).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
	}
	
	@Test
	@WithMockUser(username = "mariajucilene016@gmail.com", roles = {"USUARIO"})
	public void testRemoverLancamentoAcessoNegado() throws Exception {
		BDDMockito.given(this.lancamentoService.buscarPorId(Mockito.anyLong())).willReturn(Optional.of(new Lancamento()));
		mvc.perform(MockMvcRequestBuilders.delete(URL_BASE + ID_LANCAMENTO).accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
	}
	
	@Test
	@WithMockUser
	public void testBuscarLancamentoIdInvalido() throws Exception {
		String idInvalido = "999";
		BDDMockito.given(this.lancamentoService.buscarPorId(Mockito.anyLong())).willReturn(Optional.empty());
		mvc.perform(MockMvcRequestBuilders.get(URL_BASE + idInvalido).content(this.obterJsonRequisicaoPost())
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.errors").value("Lançamento não encontrado para o ID "+idInvalido));
	}
	
	private Lancamento obterDadosLancamento() {
		Lancamento lancamento = new Lancamento();
		lancamento.setId(ID_LANCAMENTO);
		lancamento.setData(DATA);
		lancamento.setTipo(TipoLancamentoEnum.valueOf(TIPO));
		lancamento.setFuncionario(new Funcionario());
		lancamento.getFuncionario().setId(ID_FUNCIONARIO);
		return lancamento;
	}
	
	private String obterJsonRequisicaoPost() throws JsonProcessingException {
		LancamentoDTO lancamentoDTO = new LancamentoDTO();
		lancamentoDTO.setId(null);
		lancamentoDTO.setData(this.dateFormat.format(DATA));
		lancamentoDTO.setTipo(TIPO);
		lancamentoDTO.setFuncionarioId(ID_FUNCIONARIO);
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(lancamentoDTO);
	}
	
}
