
package ced.PDFParser.entity;


public class TableCell {


    private final String content;
    private final int id;


    public TableCell(int id, String content) {
        this.id = id;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public int getId() {
        return id;
    }
}
