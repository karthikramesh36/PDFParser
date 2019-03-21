
package ced.PDFParser.entity;

import java.util.ArrayList;
import java.util.List;


public class TableRow {

    private final int id;
    private final List<TableCell> cells = new ArrayList<>();


    public TableRow(int id) {
        this.id = id;
    }


    public int getId() {
        return id;
    }

    public List<TableCell> getCells() {
        return cells;
    }

  
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        int lastCellIdx = 0;
        for (TableCell cell : cells) {
            for (int id2 = lastCellIdx; id2 < cell.getId() - 1; id2++) {
                result.append(";");
            }
            if (cell.getId() > 0) {
                result.append(";");
            }
            result.append(cell.getContent());
            lastCellIdx = cell.getId();
        }
        //return
        return result.toString();
    }

}
