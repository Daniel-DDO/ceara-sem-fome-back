package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.model.*;
import com.ceara_sem_fome_back.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Anotacao para habilitar o Mockito
@ExtendWith(MockitoExtension.class)
public class CompraServiceTest {

    @InjectMocks
    private CompraService compraService;

    @Mock
    private BeneficiarioRepository beneficiarioRepository;
    @Mock
    private EstabelecimentoRepository estabelecimentoRepository;
    @Mock
    private ProdutoCarrinhoRepository produtoCarrinhoRepository;
    @Mock
    private CompraRepository compraRepository;
    @Mock
    private ItemCompraRepository itemCompraRepository;
    @Mock
    private ContaRepository contaRepository;

    private Beneficiario beneficiario;
    private Comerciante comerciante;
    private Estabelecimento estabelecimento;
    private Carrinho carrinho;
    private ProdutoCarrinho produtoCarrinho;
    private Conta contaBeneficiario;
    private Conta contaComerciante;
    private final String BENEFICIARIO_ID = "ben-test-id";
    private final String ESTABELECIMENTO_ID = "est-test-id";
    private final BigDecimal VALOR_COMPRA = new BigDecimal("50.00"); // 2 * 25.00

    @BeforeEach
    void setUp() {
        //Configura Pessoa e Estabelecimento
        comerciante = new Comerciante();
        comerciante.setId("com-test-id");
        
        estabelecimento = new Estabelecimento();
        estabelecimento.setId(ESTABELECIMENTO_ID);
        estabelecimento.setComerciante(comerciante);

        //Configura Beneficiario e Carrinho
        carrinho = new Carrinho();
        carrinho.setId("car-test-id");
        
        beneficiario = new Beneficiario();
        beneficiario.setId(BENEFICIARIO_ID);
        beneficiario.setCarrinho(carrinho);

        //Configura o Produto no Carrinho
        Produto produto = new Produto();
        produto.setId("prod-test-id");
        produto.setPreco(new BigDecimal("25.00")); // Preco unitario

        produtoCarrinho = new ProdutoCarrinho();
        produtoCarrinho.setProduto(produto);
        produtoCarrinho.setQuantidade(2); // 2 unidades, totalizando 50.00
        produtoCarrinho.setCarrinho(carrinho);

        //Configura as Contas
        contaBeneficiario = new Conta();
        contaBeneficiario.setBeneficiario(beneficiario);

        contaComerciante = new Conta();
        contaComerciante.setComerciante(comerciante);
    }

    //TESTES

    @Test
    void testFinalizarCompra_CenarioDeSucesso() {
        
        //Define saldo SUFICIENTE para a compra de 50.00
        contaBeneficiario.setSaldo(new BigDecimal("100.00")); 
        contaComerciante.setSaldo(new BigDecimal("1000.00"));

        // Simula o que o banco de dados deve retornar
        when(beneficiarioRepository.findById(BENEFICIARIO_ID)).thenReturn(Optional.of(beneficiario));
        when(estabelecimentoRepository.findById(ESTABELECIMENTO_ID)).thenReturn(Optional.of(estabelecimento));
        when(produtoCarrinhoRepository.findByCarrinho(carrinho)).thenReturn(List.of(produtoCarrinho));
        when(contaRepository.findByBeneficiario(beneficiario)).thenReturn(Optional.of(contaBeneficiario));
        when(contaRepository.findByComerciante(comerciante)).thenReturn(Optional.of(contaComerciante));
        
        // Quando 'compraRepository.save' for chamado, ele deve retornar o argumento que recebeu
        when(compraRepository.save(any(Compra.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //Acao
        Compra compraFinalizada = compraService.finalizarCompra(BENEFICIARIO_ID, ESTABELECIMENTO_ID);

        //Verificacao
        assertNotNull(compraFinalizada);
        assertEquals(StatusCompra.FINALIZADA, compraFinalizada.getStatus());
        assertEquals(VALOR_COMPRA.doubleValue(), compraFinalizada.getValorTotal());

        // Verifica se os saldos foram atualizados corretamente
        assertEquals(new BigDecimal("50.00"), contaBeneficiario.getSaldo(), "Saldo do beneficiario (100 - 50)");
        assertEquals(new BigDecimal("1050.00"), contaComerciante.getSaldo(), "Saldo do comerciante (1000 + 50)");

        // Verifica se os metodos de salvamento e delecao foram chamados
        verify(compraRepository, times(1)).save(any(Compra.class));
        verify(itemCompraRepository, times(1)).save(any(ItemCompra.class));
        verify(contaRepository, times(2)).save(any(Conta.class)); // 1 para beneficiario, 1 para comerciante
        verify(produtoCarrinhoRepository, times(1)).deleteAll(List.of(produtoCarrinho));
    }

    @Test
    void testFinalizarCompra_FalhaSaldoInsuficiente() {
        
        // Define saldo INSUFICIENTE 20.00 para a compra de 50.00
        contaBeneficiario.setSaldo(new BigDecimal("20.00")); 

        // Configura os mocks ate o ponto da falha
        when(beneficiarioRepository.findById(BENEFICIARIO_ID)).thenReturn(Optional.of(beneficiario));
        when(estabelecimentoRepository.findById(ESTABELECIMENTO_ID)).thenReturn(Optional.of(estabelecimento));
        when(produtoCarrinhoRepository.findByCarrinho(carrinho)).thenReturn(List.of(produtoCarrinho));
        when(contaRepository.findByBeneficiario(beneficiario)).thenReturn(Optional.of(contaBeneficiario));

        //Acao e Verificacao
        
        // Verifica se a excecao RuntimeException foi lancada
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            compraService.finalizarCompra(BENEFICIARIO_ID, ESTABELECIMENTO_ID);
        });

        // Verifica a mensagem exata da excecao
        assertEquals("Saldo insuficiente para realizar a compra.", exception.getMessage());

        // VERIFICACAO CRUCIAL: Garante que nada foi salvo no banco
        verify(compraRepository, never()).save(any(Compra.class));
        verify(itemCompraRepository, never()).save(any(ItemCompra.class));
        verify(contaRepository, never()).save(any(Conta.class));
        verify(produtoCarrinhoRepository, never()).deleteAll(any());
    }

    @Test
    void testFinalizarCompra_FalhaCarrinhoVazio() {
        when(beneficiarioRepository.findById(BENEFICIARIO_ID)).thenReturn(Optional.of(beneficiario));
        when(estabelecimentoRepository.findById(ESTABELECIMENTO_ID)).thenReturn(Optional.of(estabelecimento));
        
        // Simula o carrinho retornando uma lista vazia
        when(produtoCarrinhoRepository.findByCarrinho(carrinho)).thenReturn(Collections.emptyList());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            compraService.finalizarCompra(BENEFICIARIO_ID, ESTABELECIMENTO_ID);
        });

        assertEquals("O carrinho está vazio. Não é possível finalizar a compra.", exception.getMessage());
    }

    @Test
    void testFinalizarCompra_FalhaContaBeneficiarioNaoEncontrada() {
        when(beneficiarioRepository.findById(BENEFICIARIO_ID)).thenReturn(Optional.of(beneficiario));
        when(estabelecimentoRepository.findById(ESTABELECIMENTO_ID)).thenReturn(Optional.of(estabelecimento));
        when(produtoCarrinhoRepository.findByCarrinho(carrinho)).thenReturn(List.of(produtoCarrinho));
        
        // Simula a conta do beneficiario nao sendo encontrada
        when(contaRepository.findByBeneficiario(beneficiario)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            compraService.finalizarCompra(BENEFICIARIO_ID, ESTABELECIMENTO_ID);
        });

        assertEquals("Conta do beneficiário não encontrada.", exception.getMessage());
    }

    @Test
    void testFinalizarCompra_FalhaContaComercianteNaoEncontrada() {
        contaBeneficiario.setSaldo(new BigDecimal("100.00")); // Saldo suficiente
        
        when(beneficiarioRepository.findById(BENEFICIARIO_ID)).thenReturn(Optional.of(beneficiario));
        when(estabelecimentoRepository.findById(ESTABELECIMENTO_ID)).thenReturn(Optional.of(estabelecimento));
        when(produtoCarrinhoRepository.findByCarrinho(carrinho)).thenReturn(List.of(produtoCarrinho));
        when(contaRepository.findByBeneficiario(beneficiario)).thenReturn(Optional.of(contaBeneficiario));
        
        // Simula a conta do comerciante nao sendo encontrada
        when(contaRepository.findByComerciante(comerciante)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            compraService.finalizarCompra(BENEFICIARIO_ID, ESTABELECIMENTO_ID);
        });

        assertEquals("Conta do comerciante não encontrada.", exception.getMessage());
    }
}