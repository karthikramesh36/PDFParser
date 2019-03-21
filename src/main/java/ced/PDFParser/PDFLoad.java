package ced.PDFParser;

import java.io.File;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;

public interface PDFLoad<T> {
   
	/**
	 * sets the source of the PDF document to be extracted
	 * @param doc - input object of type PDDocument
	 * @return - returns an object of the class type
	 */
	public PDFLoad<T> setSource(PDDocument doc);
	
	
    /**
     * sets the source of the PDF document to be extracted
     * @param inputStream - input stream of PDF file
     * @return - returns an object of the class type
     */
    public PDFLoad<T> setSource(InputStream inputStream); 
    
    
    /**
     * sets the source of the PDF document to be extracted
     * @param file - input PDF file object 
     * @return - returns an object of the class type
     */
    public PDFLoad<T> setSource(File file);
    

    /**
     * sets the source of the PDF document to be extracted
     * @param filePath - input filePath of PDF as string
     * @return - returns an object of the class type
     */
    public PDFLoad<T> setSource(String filePath);


    /**
     * This page will be added to extracted pages list to be then extracted. if nothing is added to the list then  
     * extraction takes place in all pages,if pageId provided then its added to the list of pages to be extracted from.
     * @param pageIdx - indicates the page number to be added 
     * @return - returns an object of the class type
     */
    public PDFLoad<T> addPage(int pageId);
    
    
    /**
     * These pages will be added to extracted pages list to be then extracted. if nothing is added to the list then  
     * extraction takes place in all pages,if pageIds are provided then its added to the list of pages to be extracted from.
     * @param pageIdx - indicates the page number to be added 
     * @return - returns an object of the class type
     */
    public PDFLoad<T> addPage(int[] pageIds);
    
    
    /**
     * This page will be removed from pages to be extracted. 
     * it is done by adding the pageId into a list that keeps track of pages to be excepted.
     *
     * @param pageIdx - pageId to be removed 
     * @return - returns an object of the class type
     */
    public PDFLoad<T> exceptPage(int pageId);
    
    
    /**
     * These pages will be removed from pages to be extracted. 
     * it is done by adding the pageIds into a list that keeps track of pages to be excepted.
     * @param pageIds - pageIds to be removed 
     * @return - returns an object of the class type
     */
    public PDFLoad<T> exceptPage(int[] pageIds);

}
