package ced.PDFParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

public class TextPositionExtractor extends PDFTextStripper {

    private final List<TextPosition> textPositions = new ArrayList<>();
    private final int pageId;

    public TextPositionExtractor(PDDocument document, int pageId) throws IOException {
        super();
        super.setSortByPosition(true);
        super.document = document;
        this.pageId = pageId;
    }

    public void stripPage(int pageId) throws IOException {
        this.setStartPage(pageId + 1);
        this.setEndPage(pageId + 1);
        try (Writer writer = new OutputStreamWriter(new ByteArrayOutputStream())) {
            writeText(document, writer);
        }
    }

    @Override
    protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
        this.textPositions.addAll(textPositions);
    }

    /**
     * and order by textPosition.getY() ASC
     *
     * @return
     * @throws IOException
     */
    public List<TextPosition> extract() throws IOException {
        this.stripPage(pageId);
        //sort
        Collections.sort(textPositions, new Comparator<TextPosition>() {
            @Override
            public int compare(TextPosition o1, TextPosition o2) {
                int result = 0;
                if (o1.getY() < o2.getY()) {
                    result = -1;
                } else if (o1.getY() > o2.getY()) {
                    result = 1;
                }
                return result;

            }
        });
        return this.textPositions;
    }
}