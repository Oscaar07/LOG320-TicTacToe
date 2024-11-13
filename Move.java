class Move {
    private int row;
    private char col;
    private boolean isInvalid; // Pour gérer le cas spécial A0
    private int index;

    public Move() {
        row = -1;
        col = 'Z';
        isInvalid = true;
        index = -1;
    }

    public Move(char c, int r) {
        // Validation des limites
        if (r < 1 || r > 15) {
            throw new IllegalArgumentException(
                "Ligne invalide: " + r + " (doit être entre 1 et 15)");
        }
        
        char upperC = Character.toUpperCase(c);
        if (upperC < 'A' || upperC > 'O') {
            throw new IllegalArgumentException(
                "Colonne invalide: " + c + " (doit être entre A et O)");
        }
        
        this.row = r;
        this.col = upperC;
    }

    public static Move createInvalidMove() {
        return new Move('A', 0);
    }

    public boolean isValid() {
        return !isInvalid;
    }

    public int getIngameRow() {
        return row;
    }

    public char getIngameCol() {
        return col;
    }

    @Override
    public String toString() {
        if (isInvalid) {
            return "A0";
        }
        return String.format("%c%d", col, row);
    }
    public int getIndex(){
        int gridRow = Math.abs(row - 15);
        int gridCol = col - 'A';
        return gridRow * 15 + gridCol;
    }
}