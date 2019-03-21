package ced.PDFParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;

import org.apache.pdfbox.text.TextPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;



public class LineExtractor implements PDFLoad<LineExtractor> {
	
    private final Logger logger = LoggerFactory.getLogger(TableExtractor.class);
	private PDDocument document;
    //contains pages that will be extracted table content.
    //If this variable doesn't contain any page, all pages will be extracted
    private List<Integer> extractedPages = new ArrayList<>();
    private List<Integer> exceptedPages = new ArrayList<>();
    private  Multimap<Integer, Integer> pageNExceptedLinesMap = HashMultimap.create();

	public LineExtractor setSource(PDDocument document) {
		this.document = document;
		return this;
	}

    public LineExtractor setSource(InputStream inputStream) {
    	try {
			return this.setSource(PDDocument.load(inputStream));
		} catch (IOException ex) {
			throw new RuntimeException("Invalid pdf input stream", ex);		
		}
    }
    
	public LineExtractor setSource(File file) {
        try {
            return this.setSource(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            throw new RuntimeException("Invalid pdf file", ex);
        }
    }
	
	public LineExtractor setSource(String filepath) {
		try {
			return this.setSource(PDDocument.load(new File(filepath)));
		} catch (IOException ex) {
			throw new RuntimeException("Invalid pdf file path", ex);		
		}
	}

	@Override
	public LineExtractor addPage(int pageId) {
    	if (!extractedPages.contains(pageId)) {
    		extractedPages.add(pageId);
		}
        return this;
	}
	
	@Override
	public LineExtractor addPage(int[] pageIds) {
		
		for(int page : pageIds) {
			if(!extractedPages.contains(page))	
				extractedPages.add(page);
		}
		return this;
	}
	
	@Override
	public LineExtractor exceptPage(int pageId) {
		if(!exceptedPages.contains(pageId))	exceptedPages.add(pageId);
		return this;
	}

	@Override
	public LineExtractor exceptPage(int[] pageIds) {
		for(int page : pageIds) {
			if(!exceptedPages.contains(page))	exceptedPages.add(page);
		}
		return this;
	}
	
    /**
     * Setting the lineIds allow us to eliminate the given lines from extraction in pa rticulae page
     * denoted by pageId parameter
     * @param pageId - pageId to be excepted from extraction
     * @param lineIds - lineIds to be excepted from extraction
     * @return - returns an object of the class type
     */
    public LineExtractor exceptLine(int pageId, int[] lineIds) {
        for (int lineId : lineIds) {
            pageNExceptedLinesMap.put(pageId, lineId);
        }
        return this;
    }


    /**
     * Setting the lineIds allow us to eliminate the given lines from extraction in all pages
     * @param lineIds - lineIds to be excepted from extraction
     * @return - returns an object of the class type
     */
    public LineExtractor exceptLine(int[] lineIds) {
        this.exceptLine(-1, lineIds);
        return this;
    }
	
	
	
	/**
	 * @return returns a list containing all the lines in the document ordered by line number
	 */
	public List<String> extract() {
        List<String> result = new ArrayList<>();
        Multimap<Integer, Range<Integer>> pageIdNLineRangesMap = LinkedListMultimap.create();
        Multimap<Integer, TextPosition> pageIdNTextsMap = LinkedListMultimap.create();
        try {
        	if(this.document == null ) {
        		logger.info("the Source of document is not set yet");
                throw new FileNotFoundException("Check the source of the PDF file");  		
        	}
            for (int pageId = 0; pageId < document.getNumberOfPages(); pageId++) {
                boolean validPage = !exceptedPages.contains(pageId) && 
                		(extractedPages.isEmpty() || extractedPages.contains(pageId));
                if (validPage) {
                	logger.info("Accessing page " + pageId+ " to start processing");
                    List<TextPosition> texts = extractTextPositions(pageId);	//sorted by .getY() ASC
                    
                    //extract line ranges
                    List<Range<Integer>> lineRanges = getLineRanges(pageId, texts);
                	logger.info("Found " + lineRanges.size() + " line/ row ranges");

                    //extract column ranges
                    List<TextPosition> textsByLineRanges = getTextsByLineRanges(lineRanges, texts);

                    pageIdNLineRangesMap.putAll(pageId, lineRanges);
                    pageIdNTextsMap.putAll(pageId, textsByLineRanges);
                }
            }

            for (int pageId : pageIdNTextsMap.keySet()) {
            	result = buildList((List<TextPosition>) pageIdNTextsMap.get(pageId), 
            			(List<Range<Integer>>) pageIdNLineRangesMap.get(pageId));
                logger.info("Found " + result.size() + " line(s) in page " + pageId);
            }
        } 
        catch (IOException ex) {
            throw new RuntimeException("Parse pdf file fail", ex);
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
	
    /**
     * Texts in documentContent have been ordered by .getY() ASC
     * identifies all texts that fall under the same y coordinate point and groups them as a single line
     * @param documentContent - list of document content along with their text positions
     * @param rowTrapRanges
     * @return
     */
    private ArrayList<String> buildList(List<TextPosition> documentContent,
            List<Range<Integer>> rowTrapRanges) {
    	ArrayList<String> result = new ArrayList<String>();
        int id = 0;
        int rowId = 0;
        List<TextPosition> rowContent = new ArrayList<>();
        while (id < documentContent.size()) {
            TextPosition textPosition = documentContent.get(id);
            Range<Integer> rowTrapRange = rowTrapRanges.get(rowId);
            Range<Integer> textRange = Range.closed((int) textPosition.getY(),
                    (int) (textPosition.getY() + textPosition.getHeight()));
            if (rowTrapRange.encloses(textRange)) {
                rowContent.add(textPosition);
                id++;
            } else {


                result.add(buildListElement(rowContent));
                //next row: clear rowContent
                rowContent.clear();
                rowId++;
            }
        }
        //last row
        if (!rowContent.isEmpty() && rowId < rowTrapRanges.size()) {
            result.add(buildListElement(rowContent));
        }
        return result;
    }
	
    /**
     * given input converts it into a string
     * @param lineContent - list of texts in a row along with their positions
     * @return - a string with line content
     */
    private String buildListElement(List<TextPosition> lineContent) {
    	
    	Collections.sort(lineContent, new Comparator<TextPosition>() {
            @Override
            public int compare(TextPosition o1, TextPosition o2) {
                int retVal = 0;
                if (o1.getX() < o2.getX()) {
                    retVal = -1;
                } else if (o1.getX() > o2.getX()) {
                    retVal = 1;
                }
                return retVal;
            }
        });
        StringBuilder lineContentBuilder = new StringBuilder();
        for (TextPosition textPosition : lineContent) {
            lineContentBuilder.append(textPosition.getUnicode());
        }
        String lineContentString = lineContentBuilder.toString();
		return lineContentString;
	}

	private List<TextPosition> extractTextPositions(int pageId) throws IOException {
        TextPositionExtractor extractor = new TextPositionExtractor(document, pageId);
        return extractor.extract();
    }
    
    /**
     * given a pageId and lineId , checks if that particular line in page should be extracted.
     * @param pageIdx 
     * @param lineIdx
     * @return
     */
    private boolean isExceptedLine(int pageId, int lineId) {
        boolean retVal = this.pageNExceptedLinesMap.containsEntry(pageId, lineId)
                || this.pageNExceptedLinesMap.containsEntry(-1, lineId);
        return retVal;
    }
    
    /**
    *
    * Remove all texts in excepted lines 
    *
    * TexPositions are sorted by .getY() ASC
    *
    * @param lineRanges
    * @param textPositions
    * @return - list of text along with its positions that should be extracted.
    */
    private List<TextPosition> getTextsByLineRanges(List<Range<Integer>> lineRanges, List<TextPosition> textPositions) {
        List<TextPosition> result = new ArrayList<>();
        int id = 0;
        int lineId = 0;
        while (id < textPositions.size() && lineId < lineRanges.size()) {
            TextPosition textPosition = textPositions.get(id);
            Range<Integer> textRange = Range.closed((int) textPosition.getY(),
                    (int) (textPosition.getY() + textPosition.getHeight()));
            Range<Integer> lineRange = lineRanges.get(lineId);
            if (lineRange.encloses(textRange)) {
                result.add(textPosition);
                id++;
            } else if (lineRange.upperEndpoint() < textRange.lowerEndpoint()) {
                lineId++;
            } else {
                id++;
            }
        }
        //return
        return result;
    }
   
    private List<Range<Integer>> removeExceptedLines(int pageId, List<Range<Integer>> lineTrapRanges) {
        List<Range<Integer>> result = new ArrayList<>();
        for (int lineId = 0; lineId < lineTrapRanges.size(); lineId++) {
            boolean isExceptedLine = isExceptedLine(pageId, lineId)
                    || isExceptedLine(pageId, lineId - lineTrapRanges.size());
            if (!isExceptedLine) {
                result.add(lineTrapRanges.get(lineId));
            }
        }
        //return
        return result;
    }
    private List<Range<Integer>> getLineRanges(int pageId, List<TextPosition> pageContent) {
        TrapRangeBuilder lineTrapRangeBuilder = new TrapRangeBuilder();
        for (TextPosition textPosition : pageContent) {
            Range<Integer> lineRange = Range.closed((int) textPosition.getY(),
                    (int) (textPosition.getY() + textPosition.getHeight()));
            //add to builder
            lineTrapRangeBuilder.addRange(lineRange);
        }
        List<Range<Integer>> lineTrapRanges = lineTrapRangeBuilder.build();
        List<Range<Integer>> result = removeExceptedLines(pageId, lineTrapRanges);
        return result;
    }
}
