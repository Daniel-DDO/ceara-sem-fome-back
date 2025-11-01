package com.ceara_sem_fome_back.application;

import com.ceara_sem_fome_back.model.Produto;
import com.ceara_sem_fome_back.model.StatusProduto;
import com.ceara_sem_fome_back.repository.ProdutoRepository;
import com.ceara_sem_fome_back.service.ProdutoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CearaSemFomeBackApplicationTests {

	@Autowired
	private ProdutoService produtoService;

	@Autowired
	private ProdutoRepository produtoRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void testProdutoSoftDelete() {
		
		String produtoId = "prod-1";

		//VERIFICAÇÃO INICIAL
		Optional<Produto> produtoAntes = produtoRepository.findById(produtoId);
		assertTrue(produtoAntes.isPresent(), "Produto 'prod-1' deveria ser encontrado antes do delete");
		assertEquals(StatusProduto.AUTORIZADO, produtoAntes.get().getStatus());

		//Executar o soft delete (removerProduto)
		produtoService.removerProduto(produtoId);

		//O findById não deve mais encontrar o produto
		Optional<Produto> produtoDepois = produtoRepository.findById(produtoId);
		assertFalse(produtoDepois.isPresent(), "Produto NÃO deveria ser encontrado pelo findById padrão após o soft delete");

		//findByIdIgnoringStatus deve encontrar o produto
		Optional<Produto> produtoIgnorandoStatus = produtoRepository.findByIdIgnoringStatus(produtoId);
		assertTrue(produtoIgnorandoStatus.isPresent(), "Produto DEVE ser encontrado pelo findByIdIgnoringStatus");

		//O status do produto no banco deve ser DESATIVADO
		assertEquals(StatusProduto.DESATIVADO, produtoIgnorandoStatus.get().getStatus(), "O status do produto no banco deve ser DESATIVADO");
	}

}