class Move
{
    private int row;
    private char col;

    public Move(){
        row = -1;
        col = 'z';
    }

    public Move(char c, int r){
        row = r;
        col = c;
    }

    public int getIngameRow(){
        return row;
    }

    public char getIngameCol(){
        return col;
    }

    public int getGridRow(){
        return Math.abs(row - 15);
    }
    public int getGetGridCol(){
        return 
    }

    public void setRow(int r){
        row = r;
    }

    public void setCol(char c){
        col = c;
    }
}
