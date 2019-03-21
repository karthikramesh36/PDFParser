package ced.PDFParser.test;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;

import ced.PDFParser.AreaExtractor;

/**
 * The Tests here do not assert for content in the returned value 
 * it serves as an example for all possible usages of AreaExtractor class
 *
 */
public class TestAreaExtractor {
    
    String homeDirectory = System.getProperty("user.dir");
    String sourceDirectory = Paths.get(homeDirectory, "_Docs").toString();

    @BeforeClass
    public void setUp() {
        PropertyConfigurator.configure(TestAreaExtractor.class.getResource("/ced/PDFExtractor/log4j.properties"));
    }
    
    @Test
    public void canExtractRectangleInAllPages() throws IOException{
        
        List<String> list = (new AreaExtractor())
                .setSource(sourceDirectory + File.separator + "sample-2.pdf")
                .extract(new Rectangle(50,50,40,40));
        list.toString();

    }
    
    @Test
    public void canExtractRectangleInOnePage() throws IOException{
        
        List<String> list = (new AreaExtractor())
                .setSource(sourceDirectory + File.separator + "sample-2.pdf")
                .addPage(0)
                .extract(new Rectangle(50,50,40,40));
        list.toString();

    } 
    @Test
    public void canExtractRectangleMultiplePages() throws IOException{
        
        List<String> list = (new AreaExtractor())
                .setSource(sourceDirectory + File.separator + "sample-2.pdf")
                .addPage(new int[] {0,1,2,-1})
                .extract(new Rectangle(50,50,40,40));
        list.toString();
    }
    
    @Test
    public void canExtractRectangleExceptOnePage() throws IOException{
        
        List<String> list = (new AreaExtractor())
                .setSource(sourceDirectory + File.separator + "sample-2.pdf")
                .exceptPage(1)
                .extract(new Rectangle(50,50,40,40));
        list.toString();
    }
    
    @Test
    public void canExtractRectangleExceptMultiplePages() throws IOException{
        
        List<String> list = (new AreaExtractor())
                .setSource(sourceDirectory + File.separator + "sample-2.pdf")
                .exceptPage(new int[] {0,1,2,-1})
                .extract(new Rectangle(50,50,40,40));
        list.toString();
    }
    
}
