
enum Mark{
        X,
        O,
        EMPTY;
        
        public Mark enemy() {
            if (this != Mark.EMPTY) {
                return this == Mark.X ? Mark.O : Mark.X;
            } else {
                throw new RuntimeException("Impossible d'obtenir l'adversaire d'une case vide.");
            }
        }
    }

