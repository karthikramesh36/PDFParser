package ced.PDFParser.test;

import com.google.common.collect.Range;

import ced.PDFParser.TrapRangeBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.junit.Test;

public class TestPDFBox extends PDFTextStripper {

    private final List<Range<Integer>> ranges = new ArrayList<>();

    private final TrapRangeBuilder trapRangeBuilder = new TrapRangeBuilder();

    public TestPDFBox() throws IOException {
        super.setSortByPosition(true);
    }

    @Test
    public void test() throws IOException {
        String homeDirectory = System.getProperty("user.dir");
        String filePath = Paths.get(homeDirectory, "_Docs", "sample-1.pdf").toString();
        File pdfFile = new File(filePath);
        PDDocument pdDocument = PDDocument.load(pdfFile);
        //PrintTextLocations printer = new PrinTextLocations();
        PDPage page = pdDocument.getPage(0);

        this.processPage(page);
        //Print out all text    
        Collections.sort(ranges, new Comparator<Range<Integer>>() {
            @Override
            public int compare(Range<Integer> o1, Range<Integer> o2) {
                return o1.lowerEndpoint().compareTo(o2.lowerEndpoint());
            }
        });
        for (Range<Integer> range : ranges) {
            System.out.println("> " + range);
        }
        //Print out all ranges
        List<Range<Integer>> trapRanges = trapRangeBuilder.build();
        for (Range<Integer> trapRange : trapRanges) {
            System.out.println("TrapRange: " + trapRange);
        }
    }

    @Override
    protected void processTextPosition(TextPosition text) {
        Range<Integer> range = Range.closed((int) text.getY(), (int) (text.getY() + text.getHeight()));
        System.out.println("Text: " + text.getUnicode());
        trapRangeBuilder.addRange(range);
    }
}
