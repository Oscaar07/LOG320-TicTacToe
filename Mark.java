enum Mark {
    RED,
    BLACK,
    EMPTY;
    
    public Mark enemy() {
        if (this != Mark.EMPTY) {
            return this == Mark.RED ? Mark.BLACK : Mark.RED;
        } else {
            throw new RuntimeException("Impossible d'obtenir l'adversaire d'une case vide.");
        }
    }

    // Ajout de méthodes utiles
    public boolean isPlayable() {
        return this == EMPTY;
    }

    public boolean isPlayer() {
        return this != EMPTY;
    }

    @Override
    public String toString() {
        switch(this) {
            case RED: return "R";
            case BLACK: return "B";
            default: return ".";
        }
    }

    // Conversion depuis la représentation numérique du serveur
    public static Mark fromInt(int value) {
        switch(value) {
            case 1: return RED;
            case 2: return BLACK;
            case 0: return EMPTY;
            default: throw new IllegalArgumentException("Valeur invalide: " + value);
        }
    }

    // Conversion vers la représentation numérique du serveur
    public int toInt() {
        switch(this) {
            case RED: return 1;
            case BLACK: return 2;
            case EMPTY: return 0;
            default: throw new IllegalStateException("État invalide");
        }
    }
}