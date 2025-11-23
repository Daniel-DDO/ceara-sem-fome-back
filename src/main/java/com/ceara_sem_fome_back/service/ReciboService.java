package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.ReciboDTO;
import com.ceara_sem_fome_back.model.*;
import com.ceara_sem_fome_back.repository.CompraRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
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
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("RECIBO DE COMPRA")
                    .setBold()
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Ceará Sem Fome")
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY));

            document.add(new Paragraph("\n"));

            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 3}));
            infoTable.setWidth(UnitValue.createPercentValue(100));

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
            document.add(new Paragraph("\n"));

            Table tabelaItens = new Table(UnitValue.createPercentArray(new float[]{4, 1.5f, 2, 2.5f}));
            tabelaItens.setWidth(UnitValue.createPercentValue(100));

            tabelaItens.addHeaderCell(new Cell().add(new Paragraph("Produto").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            tabelaItens.addHeaderCell(new Cell().add(new Paragraph("Qtd").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY).setTextAlignment(TextAlignment.CENTER));
            tabelaItens.addHeaderCell(new Cell().add(new Paragraph("Vl. Unit").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY).setTextAlignment(TextAlignment.RIGHT));
            tabelaItens.addHeaderCell(new Cell().add(new Paragraph("Subtotal").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY).setTextAlignment(TextAlignment.RIGHT));

            for (ReciboDTO.ItemCompraDTO item : dto.getItens()) {
                tabelaItens.addCell(new Paragraph(item.getNomeProduto()));
                tabelaItens.addCell(new Paragraph(String.valueOf(item.getQuantidade())).setTextAlignment(TextAlignment.CENTER));
                tabelaItens.addCell(new Paragraph(formatarMoeda(item.getValorUnitario())).setTextAlignment(TextAlignment.RIGHT));
                tabelaItens.addCell(new Paragraph(formatarMoeda(item.getSubtotal())).setTextAlignment(TextAlignment.RIGHT));
            }

            document.add(tabelaItens);

            document.add(new Paragraph("\n"));
            Paragraph totalParagraph = new Paragraph("VALOR TOTAL: " + formatarMoeda(dto.getValorTotal()))
                    .setBold()
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.RIGHT);
            document.add(totalParagraph);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF do recibo", e);
        }
    }

    private void adicionarLinhaInfo(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold()).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph(value != null ? value : "-")).setBorder(Border.NO_BORDER));
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
                dto.setEnderecoEstabelecimentoCompleto("Endereço do Estabelecimento ID: " + est.getId());
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