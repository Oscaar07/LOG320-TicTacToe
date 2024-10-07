import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// IMPORTANT: Il ne faut pas changer la signature des méthodes
// de cette classe, ni le nom de la classe.
// Vous pouvez par contre ajouter d'autres méthodes (ça devrait 
// être le cas)
class Board {
    private final int[][] GAME_WIN_CONDITIONS = {
        // 2 en diagonale
            { 0, 4, 8 }, { 2, 4, 6 },
        // 3 à l'horizontale
            { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 },
        // 3 à la verticale
            { 0, 3, 6 }, { 1, 4, 7 }, { 2, 5, 8 },
    };

    // tableau de taille 9, indexer en utilisant row * 3 + column
    private Mark[] board;

    // Ne pas changer la signature de cette méthode
    public Board() {
        board = new Mark[9];
        Arrays.fill(board, Mark.EMPTY);
    }

    private Board(Board existing) {
        board = existing.board.clone();
    }

    public Board clone() {
        return new Board(this);
    }

    // Place la pièce 'mark' sur le plateau, à la
    // position spécifiée dans Move
    //
    // Ne pas changer la signature de cette méthode
    public void play(Move m, Mark mark) {
        if (mark != Mark.EMPTY) {
            int index = m.getRow() * 3 + m.getCol();
            if (board[index] == Mark.EMPTY) {
                board[index] = mark;
            } else {
                throw new RuntimeException("Cette case est déjà occupée");
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
        if (checkForVictory(Mark.X)) {
            return PlayStatus.XWins;
        } else if (checkForVictory(Mark.O)) {
            return PlayStatus.OWins;
        } else if (Arrays.stream(board).anyMatch(m -> m == Mark.EMPTY)) {
            return PlayStatus.Unfinished;
        } else {
            return PlayStatus.Tie;
        }
    }

    private boolean checkForVictory(Mark mark) {
        return Arrays.stream(GAME_WIN_CONDITIONS)
                .anyMatch(combination -> Arrays.stream(combination)
                        .allMatch(i -> board[i] == mark));
    }

    public List<Move> getPossibleMoves() {
        List<Move> moves = new ArrayList<>();
        for (int i = 0; i < board.length; ++i) {
            if (board[i] == Mark.EMPTY) {
                moves.add(new Move(i / 3, i % 3));
            }
        }

        return moves;
    }
}
