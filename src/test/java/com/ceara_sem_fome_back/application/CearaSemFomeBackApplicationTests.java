package com.ceara_sem_fome_back.application;

import com.ceara_sem_fome_back.service.ProdutoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import com.ceara_sem_fome_back.dto.ItemCarrinhoRequestDTO;
import com.ceara_sem_fome_back.exception.EstoqueInsuficienteException;
import com.ceara_sem_fome_back.exception.SaldoInsuficienteException;
import com.ceara_sem_fome_back.model.*;
import com.ceara_sem_fome_back.repository.*;
import com.ceara_sem_fome_back.service.CarrinhoService;
import com.ceara_sem_fome_back.service.CompraService;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.ceara_sem_fome_back.service.CadastroService;
import com.ceara_sem_fome_back.service.NotificacaoService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=none"
})
@Transactional
class CearaSemFomeBackApplicationTests {

    @Autowired
    private ProdutoService produtoService;
    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CadastroService cadastroService;
    @MockBean
    private NotificacaoService notificacaoService;
    @Autowired
    private VerificationTokenRepository tokenRepository;
    @Autowired
    private BeneficiarioRepository beneficiarioRepository;
    @Autowired
    private CompraService compraService;
    @Autowired
    private CarrinhoService carrinhoService;
    @Autowired
    private ContaRepository contaRepository;
    @Autowired
    private EstabelecimentoRepository estabelecimentoRepository;
    @Autowired
    private ProdutoCarrinhoRepository produtoCarrinhoRepository;
    @Autowired
    private CarrinhoRepository carrinhoRepository;

    @Test
    void contextLoads() {
    }

    /**
     * RODA ANTES DE CADA TESTE
     * Limpa os dados "sujos" do data.sql
     */
    @BeforeEach
    void limparCarrinhosDeTeste() {
        // Limpa os itens de "pc-1" e "pc-2"
        produtoCarrinhoRepository.deleteAllById(List.of("pc-1", "pc-2"));

        // Reseta o subtotal E A LISTA de produtos dos carrinhos do data.sql
        carrinhoRepository.findById("car-1").ifPresent(c -> {
            c.setSubtotal(BigDecimal.ZERO);
            c.getProdutos().clear();
            carrinhoRepository.save(c);
        });
        carrinhoRepository.findById("car-2").ifPresent(c -> {
            c.setSubtotal(BigDecimal.ZERO);
            c.getProdutos().clear();
            carrinhoRepository.save(c);
        });
    }

    // --- TESTE ORIGINAL DE PRODUTO ---
    @Test
    void testProdutoSoftDelete() {
        String produtoId = "prod-1";

        //VERIFICACAO INICIAL
        Optional<Produto> produtoAntes = produtoRepository.findById(produtoId);
        assertTrue(produtoAntes.isPresent(), "Produto 'prod-1' deveria ser encontrado antes do delete");
        assertEquals(StatusProduto.AUTORIZADO, produtoAntes.get().getStatus());

        //Executar o soft delete (removerProduto)
        produtoService.removerProduto(produtoId);

        //O findById nao deve mais encontrar o produto
        Optional<Produto> produtoDepois = produtoRepository.findById(produtoId);
        assertFalse(produtoDepois.isPresent(), "Produto NAO deveria ser encontrado pelo findById padrao apos o soft delete");

        //findByIdIgnoringStatus deve encontrar o produto
        Optional<Produto> produtoIgnorandoStatus = produtoRepository.findById(produtoId);
        assertTrue(produtoIgnorandoStatus.isPresent(), "Produto DEVE ser encontrado pelo findByIdIgnoringStatus");

        //O status do produto no banco deve ser DESATIVADO
        assertEquals(StatusProduto.DESATIVADO, produtoIgnorandoStatus.get().getStatus(), "O status do produto no banco deve ser DESATIVADO");
    }

    // --- TESTE DA TASK DE NOTIFICACAO ---
    @Test
    void testVerificarEFinalizarCadastro_CriaNotificacao() {
        // --- 1. PREPARACAO (Arrange) ---
        Mockito.reset(notificacaoService);
        String tokenString = UUID.randomUUID().toString();
        String userEmail = "teste.notificacao@email.com";
        String userCpf = "11122233344";
        String primeiroNome = "Usuario";

        // Cria um token de cadastro valido
        VerificationToken token = new VerificationToken(
                tokenString,
                primeiroNome + " Teste",
                userCpf,
                userEmail,
                "senhaCriptografada",
                LocalDate.of(2000, 1, 1),
                "85999998888",
                "OUTRO",
                TipoPessoa.BENEFICIARIO,
                true
        );
        // Garante que o token nao esta expirado
        token.setExpiryDate(LocalDateTime.now().plusHours(1));
        tokenRepository.save(token);

        // --- 2. EXECUCAO (Act) ---
        boolean resultado = cadastroService.verificarEFinalizarCadastro(tokenString);

        // --- 3. VERIFICACAO (Assert) ---
        assertTrue(resultado, "O metodo verificarEFinalizarCadastro deveria retornar true");

        // Verifica se o usuario foi realmente salvo
        Optional<Beneficiario> usuarioSalvo = beneficiarioRepository.findByEmail(userEmail);
        assertTrue(usuarioSalvo.isPresent(), "O novo beneficiario deveria ter sido salvo no banco");

        //findByIdIgnoringStatus deve encontrar o produto
        //Optional<Produto> produtoIgnorandoStatus = produtoRepository.findById(produtoId);
        //assertTrue(produtoIgnorandoStatus.isPresent(), "Produto DEVE ser encontrado pelo findByIdIgnoringStatus");

        // **A VERIFICACAO PRINCIPAL DA NOSSA TASK**
        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> mensagemCaptor = ArgumentCaptor.forClass(String.class);

        // Verifica se o metodo 'criarNotificacao' foi chamado EXATAMENTE 1 vez
        Mockito.verify(notificacaoService, Mockito.times(1)).criarNotificacao(
                idCaptor.capture(),
                mensagemCaptor.capture()
        );

        // Verifica se os argumentos corretos foram passados
        assertEquals(usuarioSalvo.get().getId(), idCaptor.getValue(), "O ID do destinatario da notificacao esta incorreto");

        String mensagemEsperada = "Seja bem-vindo(a), " + primeiroNome + "! Seu cadastro foi confirmado com sucesso.";
        assertEquals(mensagemEsperada, mensagemCaptor.getValue(), "A mensagem de boas-vindas esta incorreta");
    }

    /**
     * Teste do "Caminho Feliz":
     * 1. Adiciona item ao carrinho.
     * 2. Finaliza a compra.
     * 3. Verifica se o estoque foi baixado.
     * 4. Verifica se o saldo do beneficiario foi debitado.
     * 5. Verifica se o saldo do comerciante foi creditado.
     * 6. Verifica se o local de retirada (Endereco) esta correto na Compra.
     * 7. Verifica se o item foi removido do carrinho.
     */
    @Test
    void testFinalizarCompra_CaminhoFeliz() {
        //PREPARACAO
        // IDs e dados do data.sql
        String beneficiarioEmail = "maria.souza@gmail.com";
        String beneficiarioId = "ben-1";
        String estabelecimentoId = "est-1";
        String produtoId = "prod-1";
        String contaBeneficiarioId = "cont-1";
        String contaComercianteId = "cont-3";

        // Pega estados iniciais do data.sql
        Produto prodInicial = produtoRepository.findById(produtoId).get();
        int estoqueInicial = prodInicial.getQuantidadeEstoque(); // 50

        Conta contaBenInicial = contaRepository.findById(contaBeneficiarioId).get();
        BigDecimal saldoBenInicial = contaBenInicial.getSaldo(); // 250.00

        Conta contaComInicial = contaRepository.findById(contaComercianteId).get();
        BigDecimal saldoComInicial = contaComInicial.getSaldo(); // 300.00

        int qtdComprada = 2;
        BigDecimal precoProduto = prodInicial.getPreco(); // 25.90
        BigDecimal valorCompra = precoProduto.multiply(BigDecimal.valueOf(qtdComprada)); // 51.80

        //Adiciona o item ao carrinho
        ItemCarrinhoRequestDTO dto = new ItemCarrinhoRequestDTO();
        dto.setProdutoId(produtoId);
        dto.setQuantidade(qtdComprada);
        carrinhoService.adicionarItem(beneficiarioEmail, dto);

        //EXECUCAO
        Compra compra = compraService.finalizarCompra(beneficiarioId, estabelecimentoId);

        //VERIFICACAO
        assertNotNull(compra);

        //Verifica Estoque
        Produto prodFinal = produtoRepository.findById(produtoId).get();
        assertEquals(estoqueInicial - qtdComprada, prodFinal.getQuantidadeEstoque()); // 50 - 2 = 48

        //Verifica Saldo Beneficiario
        Conta contaBenFinal = contaRepository.findById(contaBeneficiarioId).get();
        assertEquals(0, saldoBenInicial.subtract(valorCompra).compareTo(contaBenFinal.getSaldo()));

        //Verifica Saldo Comerciante
        Conta contaComFinal = contaRepository.findById(contaComercianteId).get();
        assertEquals(0, saldoComInicial.add(valorCompra).compareTo(contaComFinal.getSaldo()));

        //Verifica Local de Retirada
        Estabelecimento est = estabelecimentoRepository.findById(estabelecimentoId).get();
        assertNotNull(compra.getEndereco());
        assertEquals(est.getEndereco().getId(), compra.getEndereco().getId());

        //Verifica Limpeza do Carrinho
        Carrinho carrinhoFinal = carrinhoService.verMeuCarrinho(beneficiarioEmail);
        assertTrue(carrinhoFinal.getProdutos().isEmpty(), "O carrinho deveria estar vazio (ou sem o prod-1) apos a compra");
    }

    /**
     * Testa se o CompraService lanca EstoqueInsuficienteException
     * caso o estoque seja alterado DEPOIS de adicionar ao carrinho (Race Condition).
     */
    @Test
    void testFinalizarCompra_FalhaEstoqueRaceCondition() {
        //Adiciona 2 itens (Estoque inicial e 50, entao passa)
        ItemCarrinhoRequestDTO dto = new ItemCarrinhoRequestDTO();
        dto.setProdutoId("prod-1");
        dto.setQuantidade(2);
        carrinhoService.adicionarItem("maria.souza@gmail.com", dto);

        //Simula o "Race Condition": Alguem compra o estoque enquanto o item esta no carrinho
        Produto prod = produtoRepository.findById("prod-1").get();
        prod.setQuantidadeEstoque(1); // Baixa o estoque para 1
        produtoRepository.save(prod);

        //Tenta finalizar a compra (de 2 itens, mas so tem 1)
        // Espera que o CompraService lance a excecao
        assertThrows(EstoqueInsuficienteException.class, () -> {
            compraService.finalizarCompra("ben-1", "est-1");
        });
    }

    /**
     * Testa se o CompraService lanca SaldoInsuficienteException
     * caso o valor total da compra seja maior que o saldo do beneficiario.
     */
    @Test
    void testFinalizarCompra_FalhaSaldoInsuficiente() {
        //Saldo do ben-1 e 250.00. Preco do prod-1 e 25.90.
        //Vamos comprar 10 unidades = 259.00

        //Garante que o estoque e suficiente
        Produto prod = produtoRepository.findById("prod-1").get();
        prod.setQuantidadeEstoque(20);
        produtoRepository.save(prod);

        //Adiciona 10 itens
        ItemCarrinhoRequestDTO dto = new ItemCarrinhoRequestDTO();
        dto.setProdutoId("prod-1");
        dto.setQuantidade(10);
        carrinhoService.adicionarItem("maria.souza@gmail.com", dto);

        //Tenta finalizar a compra (Saldo 250.00 < Valor 259.00)
        assertThrows(SaldoInsuficienteException.class, () -> {
            compraService.finalizarCompra("ben-1", "est-1");
        });
    }
}