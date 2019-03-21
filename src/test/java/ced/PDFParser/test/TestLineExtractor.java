package ced.PDFParser.test;

import ced.PDFParser.LineExtractor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;



public class TestLineExtractor {
	
    String homeDirectory = System.getProperty("user.dir");
    String sourceDirectory = Paths.get(homeDirectory, "_Docs").toString();
    
    @BeforeClass
    public void setUp() {
        PropertyConfigurator.configure(TestLineExtractor.class.getResource("/ced/PDFExtractor/log4j.properties"));

    }  
    
    @Test
    public void canExtract_AllLines_InAllPages() throws IOException{
        
        List<String> list = (new LineExtractor())
                .setSource(sourceDirectory + File.separator + "sample-2.pdf")
                .extract();
        System.out.println(list.toString());

    }
    
    @Test
    public void canExtract_AllLines_InASinglePage() throws IOException{
        
        List<String> list = (new LineExtractor())
                .setSource(sourceDirectory + File.separator + "sample-2.pdf")
                .addPage(0)
                .extract();
        System.out.println(list.toString());

    } 
    
    @Test
    public void canExtract_AllLines_InMultiplePages() throws IOException{
        
        List<String> list = (new LineExtractor())
                .setSource(sourceDirectory + File.separator + "sample-2.pdf")
                .addPage(new int[] {0,1,5})
                .extract();
        System.out.println(list.toString());

    } 
    
    @Test
    public void canExcept_ASingleLine_InASinglePage() throws IOException{
        
        List<String> list = (new LineExtractor())
                .setSource(sourceDirectory + File.separator + "sample-2.pdf")
                .addPage(0)
                .exceptLine(new int[] {0,1})
                .extract();
        System.out.println(list.toString());
    }
    
    @Test
    public void canExcept_MultipleLines_InMultiplePages() throws IOException{
        
        List<String> list = (new LineExtractor())
                .setSource(sourceDirectory + File.separator + "sample-2.pdf")
                .exceptLine(1,new int[] {0,1})
                .exceptLine(2, new int[] {0,1})
                .extract();
        System.out.println(list.toString());
    }
    
    @Test
    public void canExcept_MultipleLines_InAllPages() throws IOException{
        
        List<String> list = (new LineExtractor())
                .setSource(sourceDirectory + File.separator + "sample-2.pdf")
                .exceptLine(new int[] {0,1})
                .extract();
        System.out.println(list.toString());
    }
}
