package ced.PDFParser.test;

import ced.PDFParser.TableExtractor;
import ced.PDFParser.entity.Table;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;



public class TestTableExtractor {

    String homeDirectory = System.getProperty("user.dir");
    String sourceDirectory = Paths.get(homeDirectory, "_Docs").toString();
    
    @BeforeClass
    public static void setUp() {
        PropertyConfigurator.configure(TestTableExtractor.class.getResource("/ced/PDFExtractor/log4j.properties"));

    }
    
    @Test
    public void canExtractTable() throws IOException {
  
        for (int idx = 0; idx < 5; idx++) {
        	TableExtractor extractor = (new TableExtractor())
                    .setSource(sourceDirectory + File.separator + "sample-" + (idx + 1) + ".pdf");
            switch (idx) {
                case 0: {
                    extractor.exceptLine(new int[]{0, 1, -1}); //get table from document except these lines
                    break;
                }
                case 1: {
                    extractor.addPage(0); // get table from document but include only page 0
                    break;
                }
                case 2: {
                    extractor.exceptPage(0)
                            .exceptLine(new int[]{0}); // get table from document but first remove page 0 from document 
                    								//  after that eliminate line 0 from remaining pages
                    break;
                }
                case 3: {
                    extractor.exceptLine(new int[]{0}); // extract table from document but do not consider line 0 in all pages
                    break;
                }
                case 4: {
                    extractor.exceptLine(0, new int[]{0, 1}); // extract table from document , but do not consider line 0 and 1 from page 0.
                    break;
                }
            }
            List<Table> tables = extractor.extract();
            for (Table table : tables) {
                    System.out.println(table.toString());
                }

            tables.toString();
        }
    }    
}
