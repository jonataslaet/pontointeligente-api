package br.com.jonataslaet.pontointeligenteapi.utils;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import br.com.jonataslaet.pontointeligenteapi.PasswordUtils;

public class PasswordUtilsTest {

	private static final String SENHA = "123456";
	private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
	
	@Test
	public void testSenhaNula() throws Exception{
		assertNull(PasswordUtils.gerarBCrypt(null));
	}
	
	@Test
	public void testGerarHashSenha() throws Exception {
		String hash = PasswordUtils.gerarBCrypt(SENHA);
		assertTrue(bCryptPasswordEncoder.matches(SENHA, hash));
	}
}
