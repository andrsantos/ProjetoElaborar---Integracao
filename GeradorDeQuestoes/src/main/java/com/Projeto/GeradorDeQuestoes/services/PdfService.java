package com.Projeto.GeradorDeQuestoes.services;

import com.Projeto.GeradorDeQuestoes.dto.Prova;
import com.Projeto.GeradorDeQuestoes.dto.Questao;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@Service
public class PdfService {

    /**
     * Helper de escrita de texto.
     * Escreve texto com quebra de linha automática e retorna a NOVA POSIÇÃO Y.
     *
     * @param content O stream de conteúdo da página
     * @param x Posição X inicial
     * @param y Posição Y inicial (o topo da linha)
     * @param text O texto a ser escrito
     * @param font A fonte a ser usada
     * @param fontSize O tamanho da fonte
     * @param maxWidth A largura máxima antes de quebrar
     * @return O novo valor de Y (float) após escrever todo o texto
     * @throws IOException
     */
    private float addText(PDPageContentStream content, float x, float y, String text, 
                         PDType1Font font, int fontSize, float maxWidth) throws IOException {
        
        float lineHeight = fontSize * 1.5f; 
        float currentY = y; 

        String[] lines = text.split("\n"); 
        for (String line : lines) {
            String[] words = line.split(" ");
            StringBuilder sb = new StringBuilder();
            float width = 0;
            
            for (String word : words) {
                float wordWidth = font.getStringWidth(word + " ") * fontSize / 1000f;
                if (width + wordWidth < maxWidth) {
                    sb.append(word).append(" ");
                    width += wordWidth;
                } else {
                    content.beginText();
                    content.setFont(font, fontSize);
                    content.newLineAtOffset(x, currentY); 
                    content.showText(sb.toString());
                    content.endText();
                    
                    currentY -= lineHeight; 
                    
                    sb = new StringBuilder(word + " ");
                    width = wordWidth;
                }
            }
            
            content.beginText();
            content.setFont(font, fontSize);
            content.newLineAtOffset(x, currentY);
            content.showText(sb.toString());
            content.endText();

            currentY -= lineHeight; 
        }
        return currentY; 
    }

    public byte[] gerarPdfProva(Prova prova) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            
            float margin = 50;
            float y = page.getMediaBox().getHeight() - margin; 
            float x = margin;
            float contentWidth = page.getMediaBox().getWidth() - 2 * margin;

            PDPageContentStream content = new PDPageContentStream(document, page);

            try {
                content.beginText();
                content.setFont(fontBold, 16);
                content.newLineAtOffset(x, y);
                content.showText("Prova de Redes de Computadores");
                content.endText();
                y -= 40; 

                int questaoNum = 1;
                for (Questao q : prova.getQuestoes()) {
                    
                    if (y < (margin + 3 * (12 * 1.5f))) { 
                        content.close(); 
                        page = new PDPage(); 
                        document.addPage(page);
                        content = new PDPageContentStream(document, page); 
                        y = page.getMediaBox().getHeight() - margin; 
                    }
                    
                    String enunciado = questaoNum + ". " + q.getEnunciado();
                    y = addText(content, x, y, enunciado, fontBold, 12, contentWidth);

                    float altX = x + 20; 
                    float altWidth = contentWidth - 20; 
                    
                    for (Map.Entry<String, String> alt : q.getAlternativas().entrySet()) {
                        String alternativa = alt.getKey() + ") " + alt.getValue();
                        y = addText(content, altX, y, alternativa, font, 12, altWidth);
                    }
                    
                    y -= 18; 
                    questaoNum++;
                }
            } finally {
                content.close(); 
            }
            
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            document.save(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }
}