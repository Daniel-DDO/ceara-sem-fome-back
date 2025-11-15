package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.ReciboDTO;
import com.ceara_sem_fome_back.exception.EstoqueInsuficienteException;
import com.ceara_sem_fome_back.exception.RecursoNaoEncontradoException;
import com.ceara_sem_fome_back.model.*;
import com.ceara_sem_fome_back.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ceara_sem_fome_back.exception.SaldoInsuficienteException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CompraService {

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    @Autowired
    private CarrinhoRepository carrinhoRepository;

    @Autowired
    private ProdutoCarrinhoRepository produtoCarrinhoRepository;

    @Autowired
    private EstabelecimentoRepository estabelecimentoRepository;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private ItemCompraRepository itemCompraRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Transactional
    public Compra finalizarCompra(String beneficiarioId, String estabelecimentoId) {
        Beneficiario beneficiario = beneficiarioRepository.findById(beneficiarioId)
                .orElseThrow(() -> new RuntimeException("Beneficiário não encontrado."));

        Estabelecimento estabelecimento = estabelecimentoRepository.findById(estabelecimentoId)
                .orElseThrow(() -> new RuntimeException("Estabelecimento não encontrado."));

        Carrinho carrinho = beneficiario.getCarrinho();
        if (carrinho == null) {
            throw new RuntimeException("Carrinho não encontrado para este beneficiário.");
        }

        //Pega TODOS os itens do carrinho
        List<ProdutoCarrinho> todosProdutosCarrinho = produtoCarrinhoRepository.findByCarrinho(carrinho);
        if (todosProdutosCarrinho.isEmpty()) {
            throw new RuntimeException("O carrinho está vazio.");
        }

        //passando os itens que foram selecionados
        // Filtra apenas os itens que pertencem ao Comerciante do Estabelecimento selecionado
        String comercianteId = estabelecimento.getComerciante().getId();
        List<ProdutoCarrinho> produtosParaComprar = todosProdutosCarrinho.stream()
                .filter(pc -> pc.getProduto().getComerciante().getId().equals(comercianteId))
                .collect(Collectors.toList());

        if (produtosParaComprar.isEmpty()) {
            throw new RuntimeException("Nenhum item no carrinho corresponde a este estabelecimento.");
        }

        //não permitir compra de produto com o estoque zerado
        BigDecimal valorTotal = BigDecimal.ZERO;
        List<ItemCompra> itensDaCompra = new ArrayList<>();

        for (ProdutoCarrinho pc : produtosParaComprar) {
            Produto produto = produtoRepository.findById(pc.getProduto().getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Produto " + pc.getProduto().getNome() + " nao encontrado."));

            if (produto.getQuantidadeEstoque() < pc.getQuantidade()) {
                throw new EstoqueInsuficienteException(
                    String.format("Estoque insuficiente para %s. Pedido: %d, Disponível: %d",
                            produto.getNome(), pc.getQuantidade(), produto.getQuantidadeEstoque())
                );
            }
            
            BigDecimal subtotalItem = produto.getPreco()
                                        .multiply(BigDecimal.valueOf(pc.getQuantidade()));
            valorTotal = valorTotal.add(subtotalItem);
            
            ItemCompra item = new ItemCompra();
            item.setId(UUID.randomUUID().toString());
            item.setProduto(produto);
            item.setQuantidade(pc.getQuantidade());
            item.setPrecoUnitario(produto.getPreco());
            itensDaCompra.add(item);
        }

        //Verifica Saldo e Transfere
        Conta contaBeneficiario = contaRepository.findByBeneficiario(beneficiario)
                .orElseThrow(() -> new RuntimeException("Conta do beneficiário não encontrada."));

        if (contaBeneficiario.getSaldo().compareTo(valorTotal) < 0) {
            // Lança a exceção de negócio específica
            throw new SaldoInsuficienteException(
                String.format("Saldo atual: R$ %.2f, Valor da compra: R$ %.2f",
                        contaBeneficiario.getSaldo(), valorTotal)
            );
        }

        Comerciante comerciante = estabelecimento.getComerciante();
        Conta contaComerciante = contaRepository.findByComerciante(comerciante)
                .orElseThrow(() -> new RuntimeException("Conta do comerciante não encontrada."));

        contaBeneficiario.setSaldo(contaBeneficiario.getSaldo().subtract(valorTotal));
        contaComerciante.setSaldo(contaComerciante.getSaldo().add(valorTotal));

        contaRepository.save(contaBeneficiario);
        contaRepository.save(contaComerciante);
        
        //Decrementar estoque
        // (Chamado apos a confirmacao de pagamento, dentro da mesma transacao)
        produtoService.decrementarEstoque(itensDaCompra);

        //Salva a Compra
        Compra compra = new Compra();
        compra.setId(UUID.randomUUID().toString());
        compra.setDataHoraCompra(LocalDateTime.now());
        compra.setStatus(StatusCompra.FINALIZADA);
        compra.setBeneficiario(beneficiario);
        compra.setEstabelecimento(estabelecimento);
        
        //informações do local aonde se deve buscar os produtos
        compra.setEndereco(estabelecimento.getEndereco()); // Salva o endereco DO ESTABELECIMENTO
        
        compra.setValorTotal(valorTotal.doubleValue());
        compraRepository.save(compra); //Salva a Compra primeiro

        //Salva os Itens da Compra
        for (ItemCompra item : itensDaCompra) {
            item.setCompra(compra); // Associa ao ID da Compra salva
            itemCompraRepository.save(item);
        }

        //transformar uma instância de um carrinho em uma compra
        // Limpa APENAS os itens que foram comprados
        produtoCarrinhoRepository.deleteAll(produtosParaComprar);

        return compra;
    }
    public List<Compra> listarTodas() {
        return compraRepository.findAll();
    }

    public List<Compra> listarPorBeneficiario(String beneficiarioId) {
        Beneficiario beneficiario = beneficiarioRepository.findById(beneficiarioId)
                .orElseThrow(() -> new RuntimeException("Beneficiário não encontrado."));
        return compraRepository.findByBeneficiario(beneficiario);
    }

    public List<ItemCompra> listarItensDaCompra(String compraId) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new RuntimeException("Compra não encontrada."));
        return itemCompraRepository.findByCompra(compra);
    }

    public String gerarComprovante(String compraId) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new RuntimeException("Compra não encontrada."));

        List<ItemCompra> itens = itemCompraRepository.findByCompra(compra);

        StringBuilder sb = new StringBuilder();
        sb.append("=== COMPROVANTE DE COMPRA ===\n");
        sb.append("Beneficiário: ").append(compra.getBeneficiario().getNome()).append("\n");
        sb.append("Estabelecimento: ").append(compra.getEstabelecimento().getNome()).append("\n"); // Corrigido
        sb.append("Data: ").append(compra.getDataHoraCompra()).append("\n\n");

        sb.append("Itens:\n");
        for (ItemCompra item : itens) {
            sb.append("- ").append(item.getProduto().getNome())
                    .append(" | Qtd: ").append(item.getQuantidade())
                    .append(" | Preço: R$ ").append(item.getPrecoUnitario()).append("\n");
        }

        sb.append("\nTotal: R$ ").append(compra.getValorTotal()).append("\n");
        sb.append("===============================");

        return sb.toString();
    }

    @Transactional(readOnly = true)
    public ReciboDTO obterReciboDTO(String compraId) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new RuntimeException("Compra não encontrada: " + compraId));

        List<ReciboDTO.ItemCompraDTO> itensDTO = compra.getItens().stream()
                .map(item -> new ReciboDTO.ItemCompraDTO(
                        item.getProduto().getNome(),
                        item.getQuantidade(),
                        item.getPrecoUnitario(),
                        item.getValorTotalItem()
                )).collect(Collectors.toList());

        String enderecoCompleto = "Endereco nao cadastrado";
        Double lat = null;
        Double lon = null;
        
        if (compra.getEndereco() != null) {
            Endereco end = compra.getEndereco()
            enderecoCompleto = String.format("%s, %s - %s, %s",
                    end.getLogradouro(),
                    end.getNumero(),
                    end.getBairro(),
                    end.getMunicipio()
            );
            lat = end.getLatitude();
            lon = end.getLongitude();
        }       

        return new ReciboDTO(
                compra.getId(),
                compra.getDataHoraCompra(),
                compra.getBeneficiario().getNome(),
                compra.getBeneficiario().getId(),
                compra.getEstabelecimento().getComerciante().getNome(),
                compra.getEstabelecimento().getNome(), // Corrigido
                enderecoCompleto,
                lat,
                lon,
                itensDTO,
                BigDecimal.valueOf(compra.getValorTotal())
        );
    }

    public List<Compra> listarPorEstabelecimentoEStatus(String estabelecimentoId, String status) {
        log.info("[SERVIÇO] Buscando compras para Estabelecimento ID: {} com Status: {}", estabelecimentoId, status);

        // Converte a string de status para o Enum
        StatusCompra statusEnum = StatusCompra.valueOf(status.toUpperCase());

        return compraRepository.findByEstabelecimentoIdAndStatus(estabelecimentoId, statusEnum);
    }
}
