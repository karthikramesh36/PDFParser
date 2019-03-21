
package ced.PDFParser;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;

import ced.PDFParser.entity.Table;
import ced.PDFParser.entity.TableCell;
import ced.PDFParser.entity.TableRow;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;

import org.apache.pdfbox.text.TextPosition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableExtractor implements PDFLoad<TableExtractor> {

    private final Logger logger = LoggerFactory.getLogger(TableExtractor.class);
    
    //contains pages that will be extracted table content.
    //If this variable doesn't contain any page, all pages will be extracted
    private  List<Integer> extractedPages = new ArrayList<>();
    private  List<Integer> exceptedPages = new ArrayList<>();
    
    //contains avoided line ids for each page,
    //if this multimap contains only one element and key of this element equals -1
    //then all lines in extracted pages contains in multi-map value will be avoided
    private  Multimap<Integer, Integer> pageNExceptedLinesMap = HashMultimap.create();

    private PDDocument document;
    
    @Override
	public TableExtractor setSource(PDDocument doc) {
		this.document = doc;
		return this;
	}
	
    @Override
    public TableExtractor setSource(InputStream inputStream) {
    	try {
			return this.setSource(PDDocument.load(inputStream));
		} catch (IOException ex) {
			throw new RuntimeException("Invalid pdf input stream", ex);		
		}
    }
        
    public TableExtractor setSource(InputStream inputStream,String password) {
    	try {
			return this.setSource(PDDocument.load(inputStream,password));
		} catch (IOException ex) {
			throw new RuntimeException("Invalid pdf input stream", ex);		
		}
    }
    
    @Override
    public TableExtractor setSource(File file) {
        try {
            return this.setSource(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            throw new RuntimeException("Invalid pdf file", ex);
        }
    }
    
    public TableExtractor setSource(File file,String password) {
        try {
            return this.setSource(new FileInputStream(file),password);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException("Invalid pdf file", ex);
        }
    }
    
    @Override
    public TableExtractor setSource(String filePath) {
    	try {
    		return this.setSource(new File(filePath));
		} catch (Exception ex) {
            throw new RuntimeException("Invalid pdf file path", ex);
		}
    }    
    
    public TableExtractor setSource(String filePath,String password) {
    	try {
            return this.setSource(new File(filePath),password);
		} catch (Exception ex) {
            throw new RuntimeException("Invalid pdf file path", ex);
		}
    }

    @Override
    public TableExtractor addPage(int pageId) {
    	if (!extractedPages.contains(pageId)) {
    		extractedPages.add(pageId);
		}
        return this;
    }
    
	@Override
	public TableExtractor addPage(int[] pageIds) {
		
		for(int page : pageIds) {
			if(!extractedPages.contains(page))	extractedPages.add(page);
		}
		return this;
	}
		
    @Override
    public TableExtractor exceptPage(int pageId) {
		if(!exceptedPages.contains(pageId))	exceptedPages.add(pageId);
		return this;
    }

	@Override
	public TableExtractor exceptPage(int[] pageIds) {
		
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
    public TableExtractor exceptLine(int pageId, int[] lineIds) {
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
    public TableExtractor exceptLine(int[] lineIds) {
        this.exceptLine(-1, lineIds);
        return this;
    }
    
    public List<Table> extract() {
        List<Table> result = new ArrayList<>();
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
            //Calculate columnRanges
            List<Range<Integer>> columnRanges = getColumnRanges(pageIdNTextsMap.values());
        	logger.info("Found " + columnRanges.size() + " Column ranges in Total");

            for (int pageId : pageIdNTextsMap.keySet()) {
                Table table = buildTable(pageId, (List<TextPosition>) pageIdNTextsMap.get(pageId), 
                		(List<Range<Integer>>) pageIdNLineRangesMap.get(pageId), columnRanges);
                result.add(table);
                //debug
                logger.debug("Found " + table.getRows().size() + " row(s) and " + columnRanges.size()
                        + " column(s) of a table in page " + pageId);
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
        //return
        return result;
    }

    /**
     * Texts in tableContent have been ordered by .getY() ASC
     *
     * @param pageId
     * @param tableContent
     * @param rowTrapRanges
     * @param columnTrapRanges
     * @return
     */
    private Table buildTable(int pageId, List<TextPosition> tableContent,
            List<Range<Integer>> rowTrapRanges, List<Range<Integer>> columnTrapRanges) {
        Table result = new Table(pageId, columnTrapRanges.size());
        int id = 0;
        int rowId = 0;
        List<TextPosition> rowContent = new ArrayList<>();
        while (id < tableContent.size()) {
            TextPosition textPosition = tableContent.get(id);
            Range<Integer> rowTrapRange = rowTrapRanges.get(rowId);
            Range<Integer> textRange = Range.closed((int) textPosition.getY(),
                    (int) (textPosition.getY() + textPosition.getHeight()));
            if (rowTrapRange.encloses(textRange)) {
                rowContent.add(textPosition);
                id++;
            } else {
                TableRow row = buildRow(rowId, rowContent, columnTrapRanges);
                result.getRows().add(row);
                //next row: clear rowContent
                rowContent.clear();
                rowId++;
            }
        }
        //last row
        if (!rowContent.isEmpty() && rowId < rowTrapRanges.size()) {
            TableRow row = buildRow(rowId, rowContent, columnTrapRanges);
            result.getRows().add(row);
        }
        //return
        return result;
    }

    /**
     *
     * @param rowIdx
     * @param rowContent
     * @param columnTrapRanges
     * @return
     */
    private TableRow buildRow(int rowIdx, List<TextPosition> rowContent, List<Range<Integer>> columnTrapRanges) {
        TableRow result = new TableRow(rowIdx);
        //Sort rowContent
        Collections.sort(rowContent, new Comparator<TextPosition>() {
            @Override
            public int compare(TextPosition o1, TextPosition o2) {
                int result = 0;
                if (o1.getX() < o2.getX()) {
                    result = -1;
                } else if (o1.getX() > o2.getX()) {
                    result = 1;
                }
                return result;
            }
        });
        int id = 0;
        int columnId = 0;
        List<TextPosition> cellContent = new ArrayList<>();
        while (id < rowContent.size()) {
            TextPosition textPosition = rowContent.get(id);
            Range<Integer> columnTrapRange = columnTrapRanges.get(columnId);
            Range<Integer> textRange = Range.closed((int) textPosition.getX(),
                    (int) (textPosition.getX() + textPosition.getWidth()));
            if (columnTrapRange.encloses(textRange)) {
                cellContent.add(textPosition);
                id++;
            } else {
                TableCell cell = buildCell(columnId, cellContent);
                result.getCells().add(cell);
                //next column: clear cell content
                cellContent.clear();
                columnId++;
            }
        }
        if (!cellContent.isEmpty() && columnId < columnTrapRanges.size()) {
            TableCell cell = buildCell(columnId, cellContent);
            result.getCells().add(cell);
        }
        //return
        return result;
    }

    private TableCell buildCell(int columnId, List<TextPosition> cellContent) {
        Collections.sort(cellContent, new Comparator<TextPosition>() {
            @Override
            public int compare(TextPosition o1, TextPosition o2) {
                int result = 0;
                if (o1.getX() < o2.getX()) {
                    result = -1;
                } else if (o1.getX() > o2.getX()) {
                    result = 1;
                }
                return result;
            }
        });
        //String cellContentString = Joiner.on("").join(cellContent.stream().map(e -> e.getCharacter()).iterator());
        StringBuilder cellContentBuilder = new StringBuilder();
        for (TextPosition textPosition : cellContent) {
            cellContentBuilder.append(textPosition.getUnicode());
        }
        String cellContentString = cellContentBuilder.toString();
        return new TableCell(columnId, cellContentString);
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
        boolean result = this.pageNExceptedLinesMap.containsEntry(pageId, lineId)
                || this.pageNExceptedLinesMap.containsEntry(-1, lineId);
        return result;
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

    /**
     * in a given page identifies the range of possible columns
     * using given  x coordinate text positions of texts in that page and grouping them.
     * @param texts
     * @return- list of all possible column ranges in that page
     */
    private List<Range<Integer>> getColumnRanges(Collection<TextPosition> texts) {
        TrapRangeBuilder rangesBuilder = new TrapRangeBuilder();
        for (TextPosition text : texts) {
            Range<Integer> range = Range.closed((int) text.getX(), (int) (text.getX() + text.getWidth()));
            rangesBuilder.addRange(range);
        }
        return rangesBuilder.build();
    }

    /**
     * in a given page identifies the range of a line  in y coordinates
     * using given text positions in that page
     * @param pageId
     * @param pageContent
     * @return - list of all line ranges in that page
     */
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

    private List<Range<Integer>> removeExceptedLines(int pageId, List<Range<Integer>> lineTrapRanges) {
        List<Range<Integer>> result = new ArrayList<>();
        for (int lineId = 0; lineId < lineTrapRanges.size(); lineId++) {
            boolean isExceptedLine = isExceptedLine(pageId, lineId)
                    || isExceptedLine(pageId, lineId - lineTrapRanges.size());
            if (!isExceptedLine) {
                result.add(lineTrapRanges.get(lineId));
            }
        }
        return result;
    }
}
