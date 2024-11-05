
enum Mark{
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
    }

