package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.model.Compra;
import com.ceara_sem_fome_back.repository.CompraRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;

@Service
public class ReciboService {

    @Autowired
    private CompraRepository compraRepository;

    @Transactional(readOnly = true)
    public byte[] gerarReciboPDF(String compraId) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new RuntimeException("Compra não encontrada: " + compraId));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Recibo de Compra"));
        document.add(new Paragraph("ID da Compra: " + compra.getId()));
        document.add(new Paragraph("Data: " + compra.getDataHoraCompra()));
        document.add(new Paragraph("Beneficiário: " + compra.getBeneficiario().getNome()));
        //document.add(new Paragraph("Estabelecimento: " + compra.getEstabelecimento().getNome())); // Corrigido
        document.add(new Paragraph("\nItens da Compra:"));

        compra.getItens().forEach(item -> {
            document.add(new Paragraph(
                "- " + item.getProdutoEstabelecimento().getProduto().getNome() +
                " | Qtd: " + item.getQuantidade() +
                " | Vl. Unit.: R$ " + item.getPrecoUnitario() +
                " | Subtotal: R$ " + item.getValorTotalItem()
            ));
        });

        document.add(new Paragraph("\nValor Total: R$ " + compra.getValorTotal()));
        document.close();

        return baos.toByteArray();
    }
}
