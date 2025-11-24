package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.ReciboDTO;
import com.ceara_sem_fome_back.model.*;
import com.ceara_sem_fome_back.repository.CompraRepository;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
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

    private static final Color COR_PRIMARIA = new DeviceRgb(40, 167, 69);
    private static final Color COR_CINZA_CLARO = new DeviceRgb(240, 240, 240);

    @Transactional(readOnly = true)
    public byte[] gerarReciboPDF(String compraId) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new RuntimeException("Compra não encontrada: " + compraId));

        ReciboDTO reciboDTO = converterParaDTO(compra);

        return criarDocumentoPdf(reciboDTO);
    }

    private byte[] criarDocumentoPdf(ReciboDTO dto) {
        try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            pdf.setDefaultPageSize(PageSize.A4);
            Document document = new Document(pdf);
            document.setMargins(20, 20, 20, 20);

            Table headerTable = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
            Cell headerCell = new Cell()
                    .add(new Paragraph("CEARÁ RAIZ").setFontSize(24).setBold())
                    .add(new Paragraph("Comprovante de Compra").setFontSize(12))
                    .setBackgroundColor(COR_PRIMARIA)
                    .setFontColor(ColorConstants.WHITE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(Border.NO_BORDER)
                    .setPadding(20);
            headerTable.addCell(headerCell);
            document.add(headerTable);

            document.add(new Paragraph("\n"));

            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{2, 5})).useAllAvailableWidth();

            adicionarLinhaInfo(infoTable, "ID da Compra: ", dto.getCompraId());
            adicionarLinhaInfo(infoTable, "Data Emissão: ", dto.getDataCompra().format(DATA_FORMAT));
            adicionarLinhaInfo(infoTable, "Beneficiário: ", dto.getNomeBeneficiario());

            if (dto.getNomeEstabelecimento() != null) {
                adicionarLinhaInfo(infoTable, "Estabelecimento:", dto.getNomeEstabelecimento());
            }
            if (dto.getEnderecoEstabelecimentoCompleto() != null) {
                adicionarLinhaInfo(infoTable, "Endereço:", dto.getEnderecoEstabelecimentoCompleto());
            }

            document.add(infoTable);
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("ITENS DA COMPRA").setBold().setFontColor(COR_PRIMARIA).setFontSize(14));

            Table tabelaItens = new Table(UnitValue.createPercentArray(new float[]{4, 1.5f, 2, 2.5f})).useAllAvailableWidth();

            tabelaItens.addHeaderCell(criarCelulaCabecalho("Produto", TextAlignment.LEFT));
            tabelaItens.addHeaderCell(criarCelulaCabecalho("Qtd", TextAlignment.CENTER));
            tabelaItens.addHeaderCell(criarCelulaCabecalho("Preço unitário", TextAlignment.RIGHT));
            tabelaItens.addHeaderCell(criarCelulaCabecalho("Subtotal", TextAlignment.RIGHT));

            boolean linhaPar = false;
            for (ReciboDTO.ItemCompraDTO item : dto.getItens()) {
                Color bg = linhaPar ? COR_CINZA_CLARO : ColorConstants.WHITE;

                tabelaItens.addCell(criarCelulaItem(item.getNomeProduto(), TextAlignment.LEFT, bg));
                tabelaItens.addCell(criarCelulaItem(String.valueOf(item.getQuantidade()), TextAlignment.CENTER, bg));
                tabelaItens.addCell(criarCelulaItem(formatarMoeda(item.getValorUnitario()), TextAlignment.RIGHT, bg));
                tabelaItens.addCell(criarCelulaItem(formatarMoeda(item.getSubtotal()), TextAlignment.RIGHT, bg));

                linhaPar = !linhaPar;
            }

            document.add(tabelaItens);

            Table totalTable = new Table(UnitValue.createPercentArray(new float[]{1})).useAllAvailableWidth();
            totalTable.setMarginTop(10);

            Cell totalCell = new Cell()
                    .add(new Paragraph("VALOR TOTAL: " + formatarMoeda(dto.getValorTotal())))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBackgroundColor(COR_PRIMARIA)
                    .setFontColor(ColorConstants.WHITE)
                    .setFontSize(16)
                    .setBold()
                    .setPadding(10)
                    .setBorder(Border.NO_BORDER);

            totalTable.addCell(totalCell);
            document.add(totalTable);

            document.add(new Paragraph("\n\n"));
            Paragraph footer = new Paragraph("Obrigado pela preferência!\nDocumento gerado em " +
                    LocalDateTime.now().format(DATA_FORMAT))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(9)
                    .setFontColor(ColorConstants.GRAY)
                    .setItalic();

            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF do recibo", e);
        }
    }

    private void adicionarLinhaInfo(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold().setFontSize(10))
                .setBorder(Border.NO_BORDER)
                .setPaddingBottom(5));

        table.addCell(new Cell().add(new Paragraph(value != null ? value : "-").setFontSize(10))
                .setBorder(Border.NO_BORDER)
                .setPaddingBottom(5));
    }

    private Cell criarCelulaCabecalho(String texto, TextAlignment alinhamento) {
        return new Cell()
                .add(new Paragraph(texto).setBold())
                .setBackgroundColor(COR_PRIMARIA)
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(alinhamento)
                .setPadding(5)
                .setBorder(new SolidBorder(ColorConstants.WHITE, 1));
    }

    private Cell criarCelulaItem(String texto, TextAlignment alinhamento, Color background) {
        return new Cell()
                .add(new Paragraph(texto).setFontSize(10))
                .setBackgroundColor(background)
                .setTextAlignment(alinhamento)
                .setPadding(5)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));
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