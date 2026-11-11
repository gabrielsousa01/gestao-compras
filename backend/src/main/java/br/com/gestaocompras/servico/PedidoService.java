package br.com.gestaocompras.servico;

import br.com.gestaocompras.api.Dtos.PedidoDto;
import br.com.gestaocompras.api.Dtos.PedidoResumoDto;
import br.com.gestaocompras.dominio.NaoEncontradoException;
import br.com.gestaocompras.dominio.PedidoCompra;
import br.com.gestaocompras.dominio.PedidoItem;
import br.com.gestaocompras.repositorio.PedidoCompraRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color CINZA = new Color(235, 238, 242);

    private final PedidoCompraRepository pedidos;

    public PedidoService(PedidoCompraRepository pedidos) {
        this.pedidos = pedidos;
    }

    @Transactional(readOnly = true)
    public List<PedidoResumoDto> listar() {
        return pedidos.findAllByOrderByIdDesc().stream().map(PedidoResumoDto::de).toList();
    }

    @Transactional(readOnly = true)
    public PedidoDto detalhar(Long id) {
        return PedidoDto.de(buscar(id));
    }

    /** PDF do pedido para enviar ao fornecedor, com as entregas separadas por unidade. */
    @Transactional(readOnly = true)
    public byte[] gerarPdf(Long id) {
        PedidoCompra pedido = buscar(id);
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(documento, saida);
        documento.open();

        Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font negrito = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

        documento.add(new Paragraph("Pedido de Compra nº " + pedido.getId(), titulo));
        documento.add(new Paragraph("Fornecedor: " + pedido.getFornecedor().getNome()
                + " (" + pedido.getFornecedor().getEmail() + ")", normal));
        documento.add(new Paragraph("Emitido em: " + pedido.getCriadoEm().format(DATA_HORA)
                + "   ·   Requisição nº " + pedido.getRequisicao().getId(), normal));
        documento.add(new Paragraph(" "));

        PdfPTable tabela = new PdfPTable(new float[] {3.2f, 2.6f, 1.1f, 1.6f, 1.5f, 1.7f});
        tabela.setWidthPercentage(100);
        for (String cabecalho : List.of("Unidade de entrega", "Item", "Caixas", "Quantidade", "Preço unit.", "Total")) {
            PdfPCell celula = new PdfPCell(new Phrase(cabecalho, negrito));
            celula.setBackgroundColor(CINZA);
            celula.setPadding(5);
            tabela.addCell(celula);
        }
        for (PedidoItem item : pedido.getItens()) {
            tabela.addCell(celula(item.getUnidadeEntrega().getNome(), normal, Element.ALIGN_LEFT));
            tabela.addCell(celula(item.getItem().getNome(), normal, Element.ALIGN_LEFT));
            tabela.addCell(celula(String.valueOf(item.getCaixas()), normal, Element.ALIGN_RIGHT));
            tabela.addCell(celula(numero(item.getQuantidade()) + " " + item.getItem().getUnidadeMedida(), normal, Element.ALIGN_RIGHT));
            tabela.addCell(celula(moeda(item.getPrecoUnitario()), normal, Element.ALIGN_RIGHT));
            tabela.addCell(celula(moeda(item.getValorTotal()), normal, Element.ALIGN_RIGHT));
        }
        PdfPCell rotuloTotal = celula("Total do pedido", negrito, Element.ALIGN_RIGHT);
        rotuloTotal.setColspan(5);
        tabela.addCell(rotuloTotal);
        tabela.addCell(celula(moeda(pedido.getValorTotal()), negrito, Element.ALIGN_RIGHT));
        documento.add(tabela);

        documento.add(new Paragraph(" "));
        documento.add(new Paragraph("Entregar somente caixas fechadas, conforme o contrato, em cada unidade indicada.", normal));
        documento.close();
        return saida.toByteArray();
    }

    private PedidoCompra buscar(Long id) {
        return pedidos.findById(id).orElseThrow(() -> new NaoEncontradoException("Pedido " + id + " não encontrado."));
    }

    private static PdfPCell celula(String texto, Font fonte, int alinhamento) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, fonte));
        celula.setHorizontalAlignment(alinhamento);
        celula.setPadding(5);
        return celula;
    }

    private static String moeda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(valor);
    }

    private static String numero(BigDecimal valor) {
        NumberFormat formato = NumberFormat.getNumberInstance(PT_BR);
        formato.setMaximumFractionDigits(3);
        return formato.format(valor);
    }
}
