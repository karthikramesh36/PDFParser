package ced.PDFParser.entity;

import java.util.ArrayList;
import java.util.List;


public class Table {

    private final int pageId;
    private final List<TableRow> rows = new ArrayList<>();
    private final int columnsCount;

    public Table(int id, int columnsCount) {
        this.pageId = id;
        this.columnsCount = columnsCount;
    }

  
    public int getPageId() {
        return pageId;
    }

    public List<TableRow> getRows() {
        return rows;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (TableRow row : rows) {
        	if (result.length() > 0) {
                result.append("\n");
            }
            int cellId = 0;//pointer of row.cells
            int columnId = 0;//pointer of columns
            while (columnId < columnsCount) {
                if (cellId < row.getCells().size()) {
                    TableCell cell = row.getCells().get(cellId);
                    if (cell.getId() == columnId) {
                    	if (cell.getId() != 0) {
                            result.append(";");
                        }
                        result.append(cell.getContent());
                        cellId++;
                        columnId++;
                    } else if (columnId < cellId) {                    	                                    	
                        if(columnId != 0) {
                            result.append(";");
                        }
                        columnId++;
                    } else {
                        throw new RuntimeException("Invalid state");
                    }
                } else {
                     if (columnId != 0) {
                        result.append(";");
                    }
                    columnId++;
                }
            }
        }
        return result.toString();
    }
}
