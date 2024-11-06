import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// IMPORTANT: Il ne faut pas changer la signature des méthodes
// de cette classe, ni le nom de la classe.
// Vous pouvez par contre ajouter d'autres méthodes (ça devrait 
// être le cas)
class Board {

    private Map<Character, Integer> letterToNumber;
    private Map<Integer, Character> numberToLetter;

    private ArrayList<Move> moveList;

    // tableau de taille 15 x 15
    private Mark[][] board;

    // Ne pas changer la signature de cette méthode
    public Board() {
        board = new Mark[15][15];
        letterToNumber = new HashMap<>();
        numberToLetter = new HashMap<>();
        
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                board[i][j] = Mark.EMPTY;
            }
        }

        for (int i = 0; i < 15; i++) {
            char letter = (char) ('A' + i); 
            letterToNumber.put(letter, i);  
        }
        for (int i = 0; i < 15; i++) {
            char letter = (char) ('A' + i); 
            numberToLetter.put(i, letter);  
        }
        moveList = new ArrayList<>();
    }

    public Board(Board existing) {
        // Correction du clone
        this.board = new Mark[15][15];
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                this.board[i][j] = existing.board[i][j];
            }
        }
        this.letterToNumber = new HashMap<>(existing.letterToNumber);
        this.numberToLetter = new HashMap<>(existing.numberToLetter);
        this.moveList = new ArrayList<>(existing.moveList);
    }

    // Ajout de getters nécessaires
    public Mark getMark(int row, int col) {
        if (isValidPosition(row, col)) {
            return board[row][col];
        }
        throw new IllegalArgumentException("Position invalide");
    }

    public int getMoveCount() {
        return moveList.size();
    }

    public void play(Move m, Mark mark) {
        if (mark != Mark.EMPTY) {
            // Normaliser en majuscules
            char normalizedCol = Character.toUpperCase(m.getIngameCol());
            
            // Vérifier si c'est le premier coup rouge
            if (moveList.isEmpty() && mark == Mark.RED) {
                if (normalizedCol != 'H' || m.getIngameRow() != 8) {
                    throw new IllegalArgumentException("Le premier coup rouge doit être au centre (H8)");
                }
            }
            
            // Vérifier si c'est le deuxième coup rouge
            if (moveList.size() == 2 && mark == Mark.RED) {
                int distanceFromCenter = Math.abs(m.getIngameRow() - 8) +   //rendrait J10 valide
                                       Math.abs(normalizedCol - 'H');
                if (distanceFromCenter < 3) {
                    throw new IllegalArgumentException("Le deuxième coup rouge doit être à distance 3 ou plus du centre");
                }
            }

            int row = Math.abs(m.getIngameRow() - 15);
            Integer col = letterToNumber.get(normalizedCol);
            
            if (col == null) {
                throw new IllegalArgumentException("Colonne invalide: " + normalizedCol);
            }

            if (isValidPosition(row, col) && board[row][col] == Mark.EMPTY) {
                board[row][col] = mark;
                moveList.add(m);
            } else {
                throw new RuntimeException("Cette case est déjà occupée ou position invalide");
            }
        } else {
            throw new IllegalArgumentException("Il n'est pas possible d'effacer la marque");
        }
    }

    // retourne 100 pour une victoire
    // -100 pour une défaite
    // 0 pour un match nul
    // Ne pas changer la signature de cette méthode
    public int evaluate(Mark mark) {
        if (mark != Mark.EMPTY) {
            if (checkForVictory(mark)) {
                return 100;
            } else if (checkForVictory(mark.enemy())) {
                return -100;
            } else {
                return 0;
            }
        } else {
            throw new IllegalArgumentException("Veuillez passer Mark.X ou Mark.O à evaluate");
        }
    }

    public PlayStatus getPlayStatus() {
        if (checkFor5inARow(Mark.RED)) {
            return PlayStatus.RedWins;
        } else if (checkFor5inARow(Mark.BLACK)) {
            return PlayStatus.BlackWins;
        } else if (moveList.size() == 225) { // 15x15
            return PlayStatus.Tie;
        } else {
            return PlayStatus.Unfinished;
        }
    }

    private boolean checkForVictory(Mark mark) {
        return checkFor5inARow(mark) || checkForFiveCaptures(mark);
    }

    public boolean checkCapture(Move m, Mark mark) {
        int row = Math.abs(m.getIngameRow() - 15);
        int col = letterToNumber.get(m.getIngameCol());
        Mark enemy = (mark == Mark.RED) ? Mark.BLACK : Mark.RED;
        int captures = 0;

        // Vérification horizontale
        captures += checkCaptureDirection(row, col, 0, 1, mark, enemy);
        captures += checkCaptureDirection(row, col, 0, -1, mark, enemy);
        
        // Vérification verticale
        captures += checkCaptureDirection(row, col, 1, 0, mark, enemy);
        captures += checkCaptureDirection(row, col, -1, 0, mark, enemy);
        
        // Vérification diagonale /
        captures += checkCaptureDirection(row, col, 1, 1, mark, enemy);
        captures += checkCaptureDirection(row, col, -1, -1, mark, enemy);
        
        // Vérification diagonale \
        captures += checkCaptureDirection(row, col, 1, -1, mark, enemy);
        captures += checkCaptureDirection(row, col, -1, 1, mark, enemy);

        return captures > 0;
    }

    private int checkCaptureDirection(int row, int col, int rowDir, int colDir, Mark mark, Mark enemy) {
        // Vérifie si on peut capturer dans une direction donnée
        if (!isValidPosition(row + rowDir, col + colDir) || 
            !isValidPosition(row + 2*rowDir, col + 2*colDir) || 
            !isValidPosition(row + 3*rowDir, col + 3*colDir)) {
            return 0;
        }

        if (board[row + rowDir][col + colDir] == enemy &&
            board[row + 2*rowDir][col + 2*colDir] == enemy &&
            board[row + 3*rowDir][col + 3*colDir] == mark) {
            // Effectue la capture
            board[row + rowDir][col + colDir] = Mark.EMPTY;
            board[row + 2*rowDir][col + 2*colDir] = Mark.EMPTY;
            return 1;
        }
        return 0;
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < 15 && col >= 0 && col < 15;
    }

    public boolean checkForFiveCaptures(Mark mark) {
        int captureCount = 0;
        // Parcourir l'historique des mouvements pour compter les captures
        for (Move move : moveList) {
            if (board[Math.abs(move.getIngameRow() - 15)][letterToNumber.get(move.getIngameCol())] == mark) {
                if (checkCapture(move, mark)) {
                    captureCount++;
                }
                if (captureCount >= 5) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkFor5inARow(Mark mark) {
        // Vérifier horizontal
        for (int row = 0; row < 15; row++) {
            for (int col = 0; col <= 10; col++) {  // Limite à 10 pour éviter le débordement
                if (checkLine(row, col, 0, 1, mark, 5)) {
                    return true;
                }
            }
        }

        // Vérifier vertical
        for (int row = 0; row <= 10; row++) {  // Limite à 10 pour éviter le débordement
            for (int col = 0; col < 15; col++) {
                if (checkLine(row, col, 1, 0, mark, 5)) {
                    return true;
                }
            }
        }

        // Vérifier diagonale \
        for (int row = 0; row <= 10; row++) {
            for (int col = 0; col <= 10; col++) {
                if (checkLine(row, col, 1, 1, mark, 5)) {
                    return true;
                }
            }
        }

        // Vérifier diagonale /
        for (int row = 4; row < 15; row++) {
            for (int col = 0; col <= 10; col++) {
                if (checkLine(row, col, -1, 1, mark, 5)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean checkLine(int startRow, int startCol, int rowDir, int colDir, Mark mark, int count) {
        for (int i = 0; i < count; i++) {
            int row = startRow + (i * rowDir);
            int col = startCol + (i * colDir);
            
            if (!isValidPosition(row, col) || board[row][col] != mark) {
                return false;
            }
        }
        return true;
    }

    public List<Move> getPossibleMoves() {
        List<Move> moves = new ArrayList<>();
        
        // Cas spécial : premier coup
        if (moveList.isEmpty()) {
            moves.add(new Move('H', 8));
            return moves;
        }

        // Pour le second coup rouge (à distance 3+ du centre)
        boolean isSecondRedMove = moveList.size() == 1;
        
        // Parcourir le plateau avec des coordonnées valides (1-15)
        for (char col = 'A'; col <= 'O'; col++) {
            for (int row = 1; row <= 15; row++) {  // Commencer à 1, pas à 0
                try {
                    Move move = new Move(col, row);
                    if (isEmptyPosition(move)) {
                        if (isSecondRedMove) {
                            // Vérifier la distance du centre pour le second coup rouge
                            if (isValidSecondRedMove(move)) {
                                moves.add(move);
                            }
                        } else {
                            moves.add(move);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    continue; // Ignorer les positions invalides
                }
            }
        }
        
        return moves;
    }

    private boolean isEmptyPosition(Move move) {
        int row = Math.abs(move.getIngameRow() - 15);
        int col = letterToNumber.get(move.getIngameCol());
        return board[row][col] == Mark.EMPTY;
    }

    private boolean isValidSecondRedMove(Move move) {
        int distanceFromCenter = Math.abs(move.getIngameRow() - 8) + 
                               Math.abs(move.getIngameCol() - 'H');
        return distanceFromCenter >= 3;
    }

    // Méthode utilitaire pour vérifier si un coup est dans les limites
    public boolean isValidPosition(Move move) {
        int row = move.getIngameRow();
        char col = move.getIngameCol();
        
        return row >= 1 && row <= 15 &&  // Vérifier les limites des lignes
               col >= 'A' && col <= 'O';  // Vérifier les limites des colonnes
    }
}
