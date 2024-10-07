enum PlayStatus {
    XWins,
    OWins,
    Tie,
    Unfinished;

    public boolean isVictory(Mark m) {
        if (m != Mark.EMPTY) {
            return (this == PlayStatus.XWins && m == Mark.X) || (this == PlayStatus.OWins && m == Mark.O);
        } else {
            throw new IllegalArgumentException("Impossible de vérifier si une case vide a gagnée.");
        }
    }
}
