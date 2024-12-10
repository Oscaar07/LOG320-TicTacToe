public class Sequence {
    private Mark mark;
    private int directionX;
    private int directionY;
    private int length;
    private int rowDebut;
    private int rowFin;
    private int colDebut;
    private int colFin;

    public Sequence(int rowDebut, int rowFin, int colDebut, int colFin, Mark mark, int x, int y, int length){
        this.rowDebut = rowDebut;
        this.rowFin = rowFin;
        this.colDebut = colDebut;
        this.colFin = colFin;
        this.mark = mark;
        this.directionX = x;
        this.directionY = y;
        this.length = length;
    }

    public String getSequenceDirection(){
        if (directionX == 1 && directionY == 0){
            return "horizontale";
        } else if (directionY == 1 && directionX == 0) {
            return "verticale";
        } else if (directionX == 1 && directionY == 1) {
            return "diagonale sud-est";
        }
        else{
            return "diagonale sud-ouest";
        }
    }

    public int getColDirection() {
        return directionX;
    }

    public int getRowDirection() {
        return directionY;
    }

    public int getRowFin() {
        return rowFin;
    }

    public int getColDebut() {
        return colDebut;
    }

    public int getColFin() {
        return colFin;
    }

    public int getRowDebut(){
        return rowDebut;
    }
    public int getLength(){
        return length;
    }

    public Mark getMark(){
        return mark;
    }
}
