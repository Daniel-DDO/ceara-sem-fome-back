package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.ReciboDTO;
import com.ceara_sem_fome_back.model.*;
import com.ceara_sem_fome_back.repository.CompraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class ReciboService {

    @Autowired
    private CompraRepository compraRepository;

    private static final NumberFormat MOEDA_FORMAT = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private static final DateTimeFormatter DATA_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Transactional(readOnly = true)
    public byte[] gerarReciboPDF(String compraId) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new RuntimeException("Compra não encontrada: " + compraId));

        ReciboDTO reciboDTO = converterParaDTO(compra);

        return criarDocumentoPdf(reciboDTO);
    }

    private byte[] criarDocumentoPdf(ReciboDTO dto) {
        try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
            com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(baos);
            com.itextpdf.kernel.pdf.PdfDocument pdf = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdf);

            document.add(new com.itextpdf.layout.element.Paragraph("RECIBO DE COMPRA")
                    .setBold()
                    .setFontSize(18)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));

            document.add(new com.itextpdf.layout.element.Paragraph("Ceará Sem Fome")
                    .setFontSize(12)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                    .setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY));

            document.add(new com.itextpdf.layout.element.Paragraph("\n"));

            com.itextpdf.layout.element.Table infoTable = new com.itextpdf.layout.element.Table(com.itextpdf.layout.properties.UnitValue.createPercentArray(new float[]{1, 3}));
            infoTable.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));

            adicionarLinhaInfo(infoTable, "ID da Compra:", dto.getCompraId());
            adicionarLinhaInfo(infoTable, "Data:", dto.getDataCompra().format(DATA_FORMAT));
            adicionarLinhaInfo(infoTable, "Beneficiário:", dto.getNomeBeneficiario());

            if (dto.getNomeEstabelecimento() != null) {
                adicionarLinhaInfo(infoTable, "Estabelecimento:", dto.getNomeEstabelecimento());
            }
            if (dto.getEnderecoEstabelecimentoCompleto() != null) {
                adicionarLinhaInfo(infoTable, "Endereço:", dto.getEnderecoEstabelecimentoCompleto());
            }

            document.add(infoTable);
            document.add(new com.itextpdf.layout.element.Paragraph("\n"));

            com.itextpdf.layout.element.Table tabelaItens = new com.itextpdf.layout.element.Table(com.itextpdf.layout.properties.UnitValue.createPercentArray(new float[]{4, 1.5f, 2, 2.5f}));
            tabelaItens.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));

            tabelaItens.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph("Produto").setBold()).setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
            tabelaItens.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph("Qtd").setBold()).setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
            tabelaItens.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph("Vl. Unit").setBold()).setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
            tabelaItens.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph("Subtotal").setBold()).setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));

            for (ReciboDTO.ItemCompraDTO item : dto.getItens()) {
                tabelaItens.addCell(new com.itextpdf.layout.element.Paragraph(item.getNomeProduto()));
                tabelaItens.addCell(new com.itextpdf.layout.element.Paragraph(String.valueOf(item.getQuantidade())).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
                tabelaItens.addCell(new com.itextpdf.layout.element.Paragraph(formatarMoeda(item.getValorUnitario())).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
                tabelaItens.addCell(new com.itextpdf.layout.element.Paragraph(formatarMoeda(item.getSubtotal())).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
            }

            document.add(tabelaItens);

            document.add(new com.itextpdf.layout.element.Paragraph("\n"));
            com.itextpdf.layout.element.Paragraph totalParagraph = new com.itextpdf.layout.element.Paragraph("VALOR TOTAL: " + formatarMoeda(dto.getValorTotal()))
                    .setBold()
                    .setFontSize(14)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT);
            document.add(totalParagraph);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF do recibo", e);
        }
    }

    private void adicionarLinhaInfo(com.itextpdf.layout.element.Table table, String label, String value) {
        table.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(label).setBold()).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        table.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(value != null ? value : "-")).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
    }

    private String formatarMoeda(BigDecimal valor) {
        return valor != null ? MOEDA_FORMAT.format(valor) : "R$ 0,00";
    }

    private ReciboDTO converterParaDTO(Compra compra) {
        ReciboDTO dto = new ReciboDTO();
        dto.setCompraId(compra.getId());
        dto.setDataCompra(compra.getDataHoraCompra());
        dto.setValorTotal(BigDecimal.valueOf(compra.getValorTotal()));

        if (compra.getBeneficiario() != null) {
            dto.setNomeBeneficiario(compra.getBeneficiario().getNome());
            dto.setBeneficiarioId(compra.getBeneficiario().getId());
        }

        if (compra.getItens() != null && !compra.getItens().isEmpty()) {
            ProdutoCompra primeiroItem = compra.getItens().get(0);
            ProdutoEstabelecimento produtoEstabelecimento = primeiroItem.getProdutoEstabelecimento();

            if (produtoEstabelecimento != null && produtoEstabelecimento.getEstabelecimento() != null) {
                Estabelecimento est = produtoEstabelecimento.getEstabelecimento();
                dto.setNomeEstabelecimento(est.getNome());
                dto.setNomeComerciante(est.getComerciante() != null ? est.getComerciante().getNome() : "");

                if (est.getEndereco() != null) {
                    dto.setEnderecoEstabelecimentoCompleto(
                            est.getEndereco().getLogradouro() + ", " +
                                    est.getEndereco().getNumero() + " - " +
                                    est.getEndereco().getBairro() + ", " +
                                    est.getEndereco().getMunicipio()
                    );
                } else {
                    dto.setEnderecoEstabelecimentoCompleto("Endereço não disponível");
                }
            }
        }

        List<ReciboDTO.ItemCompraDTO> itensDto = compra.getItens().stream().map(item -> {
            ReciboDTO.ItemCompraDTO itemDto = new ReciboDTO.ItemCompraDTO();
            itemDto.setQuantidade(item.getQuantidade());
            itemDto.setValorUnitario(item.getPrecoUnitario());
            itemDto.setSubtotal(item.getValorTotalItem());

            if (item.getProdutoEstabelecimento() != null && item.getProdutoEstabelecimento().getProduto() != null) {
                itemDto.setNomeProduto(item.getProdutoEstabelecimento().getProduto().getNome());
            }
            return itemDto;
        }).collect(Collectors.toList());

        dto.setItens(itensDto);
        return dto;
    }
}