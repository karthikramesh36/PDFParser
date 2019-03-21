package ced.PDFParser;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class AreaExtractor implements PDFLoad<AreaExtractor>{
	
    private final Logger logger = LoggerFactory.getLogger(TableExtractor.class);
	private PDDocument document;
	
    //contains pages that will be extracted table content.
    //If this variable doesn't contain any page, all pages will be extracted
    private final List<Integer> extractedPages = new ArrayList<>();
	private final ArrayList<Integer> exceptedPages = new ArrayList<Integer>();
	
    public AreaExtractor setSource(PDDocument document) {
		this.document = document;
		return this;
	}

    public AreaExtractor setSource(InputStream inputStream) {
    	try {
			return this.setSource(PDDocument.load(inputStream));
		} catch (IOException ex) {
			throw new RuntimeException("Invalid pdf input stream", ex);		
		}
    }
    
	public AreaExtractor setSource(File file) {
        try {
            return this.setSource(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            throw new RuntimeException("Invalid pdf file", ex);
        }
    }
	
	public AreaExtractor setSource(String filepath) {
		try {
			return this.setSource(PDDocument.load(new File(filepath)));
		} catch (IOException ex) {
			throw new RuntimeException("Invalid pdf file path", ex);		
		}
	}

	@Override
	public AreaExtractor addPage(int pageId) {
    	if (!extractedPages.contains(pageId)) {
    		extractedPages.add(pageId);
		}
        return this;
	}
	
	@Override
	public AreaExtractor addPage(int[] pageIds) {
		
		for(int page : pageIds) {
			if(!extractedPages.contains(page))	
				extractedPages.add(page);
		}
		return this;
	}
	
	public AreaExtractor exceptPage(int page) {
		if(!exceptedPages.contains(page))	
			exceptedPages.add(page);
		return this;
	}
	
	public AreaExtractor exceptPage(int[] pages) {
		
		for(int page : pages) {
			if(!exceptedPages.contains(page))	
				exceptedPages.add(page);
		}
		return this;
	}
	

		
	
	/**
	 * assumes that particular area has to be extracted from all pages of the document
	 * @param rect - specify the point coordinates for the rectangle
	 * @return returns a list containing the extracted regions from the document
	 * @throws IOException
	 */
	public ArrayList<String> extract(Rectangle rect) {
		ArrayList<String> result = new ArrayList<String>();
		try {
			for(int pageId = 0; pageId < document.getNumberOfPages(); pageId++) {
                boolean b = !exceptedPages.contains(pageId) && (extractedPages.isEmpty() || extractedPages.contains(pageId));
                if (b) {
    		        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
    		        stripper.setSortByPosition( true );
    		        stripper.addRegion( "rect" + Integer.toString(pageId), rect );
    		        stripper.extractRegions(document.getPage(pageId));
    		        result.add(stripper.getTextForRegion("rect" + Integer.toString(pageId)));
    				logger.info("rect" +Integer.toString(pageId) + " has been extracted");

                }
	        
			}
		}catch (Exception e) {
			//TODO 
			logger.info("Exception while extracting text from area");
		} 
		finally {
			if (this.document != null) {
                try {
                	this.document.close();
                } catch (IOException ex) {
                    logger.error(null, ex);
                }
            }
		}
		return result;

	}
}
