public class Square {
    private int redThreatValue;
    private int blackThreatValue;
    private Mark mark = Mark.EMPTY;
    private int index;
    private int ingameRow;
    private char ingameCol;
    private int gridRow;
    private int gridCol;

    private boolean sequenceHorizontale = false;
    private boolean sequenceVerticale = false;
    private boolean sequenceDiagGauche = false;
    private boolean sequenceDiagDroite = false;

    private boolean activeSquare = false;

    public boolean isSequenceHorizontale() {
        return sequenceHorizontale;
    }

    public void setSequenceHorizontale(boolean sequenceHorizontale) {
        this.sequenceHorizontale = sequenceHorizontale;
    }

    public boolean isSequenceVerticale() {
        return sequenceVerticale;
    }

    public void setSequenceVerticale(boolean sequenceVerticale) {
        this.sequenceVerticale = sequenceVerticale;
    }

    public boolean isSequenceDiagGauche() {
        return sequenceDiagGauche;
    }

    public void setSequenceDiagGauche(boolean sequenceDiagGauche) {
        this.sequenceDiagGauche = sequenceDiagGauche;
    }

    public boolean isSequenceDiagDroite() {
        return sequenceDiagDroite;
    }

    public void setSequenceDiagDroite(boolean sequenceDiagDroite) {
        this.sequenceDiagDroite = sequenceDiagDroite;
    }

    public Square(int index){
        gridRow = index / 15;
        gridCol = index % 15;
        ingameRow = Math.abs(gridRow - 15);
        ingameCol = (char)('A' + gridCol);
        mark = Mark.EMPTY;
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
