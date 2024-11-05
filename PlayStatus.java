enum PlayStatus {
    RedWins,
    BlackWins,
    Tie,
    Unfinished;

    public boolean isVictory(Mark m) {
        if (m != Mark.EMPTY) {
            return (this == PlayStatus.RedWins && m == Mark.RED) || 
                   (this == PlayStatus.BlackWins && m == Mark.BLACK);
        } else {
            throw new IllegalArgumentException("Impossible de vérifier si une case vide a gagnée.");
        }
    }
}