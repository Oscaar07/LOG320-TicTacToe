public class Square {
    private int redThreatValue;
    private int blackThreatValue;
    private Mark mark = Mark.EMPTY;
    private int index;
    private int ingameRow;
    private char ingameCol;
    private int gridRow;
    private int gridCol;
    private boolean activeSquare = false;

    public Square(int row, int col){
        this.gridRow = row;
        this.gridCol = col;
        ingameRow = Math.abs(gridRow - 15);
        ingameCol = (char)('A' + gridCol);
        redThreatValue = 0;
        blackThreatValue = 0;
    }

    public int getValue(Mark mark) {
        if (mark == Mark.RED){
            return redThreatValue;
        }
        else return blackThreatValue;
    }

    public void setValeur(Mark mark, int value) {       //peut-etre update ceux autour directement
        if (mark == Mark.RED){
            this.redThreatValue = value;
        }
        else this.blackThreatValue = value;
        activeSquare = true;
    }
    
    public void addValeur(Mark mark, int value){
        if (mark == Mark.RED){
            int precedent = this.redThreatValue;
            this.redThreatValue = precedent + value;
        }
        else{
            int precedent = this.blackThreatValue;
            this.blackThreatValue = precedent + value;
        }
        this.activeSquare = true;
    }

    public Mark getMark() {
        return mark;
    }

    public void setMark(Mark mark) {
        this.mark = mark;
        activeSquare = true;
    }

    public boolean isActiveSquare() {
        return activeSquare;
    }

    public int getRedThreatValue() {
        return redThreatValue;
    }

    public void setRedThreatValue(int redThreatValue) {
        this.redThreatValue = redThreatValue;
    }

    public void setActiveSquare(boolean activeSquare) {
        this.activeSquare = activeSquare;
    }

    public int getGridCol() {
        return gridCol;
    }

    public void setGridCol(int gridCol) {
        this.gridCol = gridCol;
    }

    public int getGridRow() {
        return gridRow;
    }

    public void setGridRow(int gridRow) {
        this.gridRow = gridRow;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getBlackThreatValue() {
        return blackThreatValue;
    }

    public void setBlackThreatValue(int blackThreatValue) {
        this.blackThreatValue = blackThreatValue;
    }
}
