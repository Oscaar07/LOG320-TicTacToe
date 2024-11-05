import java.util.ArrayList;
import java.util.Arrays;
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
        Arrays.fill(board, Mark.EMPTY);

        for (int i = 0; i < 15; i++) {
            char letter = (char) ('a' + i);
            letterToNumber.put(letter, i + 1);
        }
        for (int i = 0; i < 15; i++) {
            char letter = (char) ('a' + i);
            numberToLetter.put(i + 1, letter);
        }
        moveList = new ArrayList<>();

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
            int row = Math.abs(m.getIngameRow() - 15);
            int col = letterToNumber.get(m.getIngameCol());

            if (board[row][col] == Mark.EMPTY) {
                board[row][col] = mark;
                moveList.add(m);
            } else {
                throw new RuntimeException("Cette case est déjà occupée");
            }
        } else {
            throw new IllegalArgumentException("Il n'est pas possible d'effacer la marque");
        }
    }

    public boolean checkCapture(Move m, Mark mark){
        //vertical
        if (m.getIngameRow() + 3 <= 15 && m.getIngameRow() >= 0){
            for (int i = 0; i >= Math.abs((m.getIngameRow() + 3)-15); i++){
                if (i == 0){
                    if (board[Math.abs((m.getIngameRow() + 3)-15)])
                }

            };
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

    }

    private boolean checkFor5inARow(Mark mark){
        ArrayList<Move> movesByMark = (ArrayList<Move>) moveList.clone();
        if (mark == Mark.RED){
            movesByMark.removeIf(move -> movesByMark.indexOf(move) % 2 == 1);
        }
        else{
            movesByMark.removeIf(move -> movesByMark.indexOf(move) % 2 == 0);
        }

        for (Move m : movesByMark){

            //diagonale gauche
            int rowActu = Math.abs((m.getIngameRow() - 4) - 15);
            char colActu = (char) (m.getIngameCol() - 4);
            while (rowActu <= Math.abs((m.getIngameRow()) - 15) && rowActu >= 0 && rowActu < 15 && colActu >= 'A' && colActu <= 'O'){
                if (board[rowActu][colActu] != mark){
                    break;
                }
                if (rowActu == Math.abs(m.getIngameRow()-15)){
                    return true;
                }
                rowActu++;
                colActu++;
            }
            rowActu = Math.abs((m.getIngameRow() + 4) - 15);
            colActu = (char) (m.getIngameCol() + 4);
            while (rowActu >= Math.abs((m.getIngameRow() + 4) - 15) && rowActu >= 0 && rowActu < 15 && colActu >= 'A' && colActu <= 'O'){
                if (board[rowActu][colActu] != mark){
                    break;
                }
                if (rowActu == Math.abs(m.getIngameRow()-15)){
                    return true;
                }
                rowActu--;
                colActu--;
            }

            //diagonale droite
            rowActu = Math.abs((m.getIngameRow() - 4) - 15);
            colActu = (char) (m.getIngameCol() + 4);
            while (rowActu <= Math.abs((m.getIngameRow()) - 15) && rowActu >= 0 && rowActu < 15 && colActu >= 'A' && colActu <= 'O'){
                if (board[rowActu][colActu] != mark){
                    break;
                }
                if (rowActu == Math.abs(m.getIngameRow()-15)){
                    return true;
                }
                rowActu++;
                colActu--;
            }

            rowActu = Math.abs((m.getIngameRow() + 4) - 15);
            colActu = (char) (m.getIngameCol() - 4);
            while (rowActu >= Math.abs((m.getIngameRow()) - 15) && rowActu >= 0 && rowActu < 15 && colActu >= 'A' && colActu <= 'O'){
                if (board[rowActu][colActu] != mark){
                    break;
                }
                if (rowActu == Math.abs(m.getIngameRow()-15)){
                    return true;
                }
                rowActu--;
                colActu++;
            }

            //horizontale
            rowActu = Math.abs((m.getIngameRow()) - 15);
            colActu = (char) (m.getIngameCol() - 4);
            while (colActu <= (char) m.getIngameCol() && colActu >= 'A' && colActu <= 'O') {
                if (board[rowActu][colActu] != mark){
                    break;
                }
                if (colActu == m.getIngameCol()){
                    return true;
                }
                colActu++;
            }
            colActu = (char) (m.getIngameCol() + 4);
            while (colActu >= (char) m.getIngameCol() && colActu >= 'A' && colActu <= 'O') {
                if (board[rowActu][colActu] != mark){
                    break;
                }
                if (colActu == m.getIngameCol()){
                    return true;
                }
                colActu--;
            }

            //verticale
            colActu = (char) (m.getIngameCol());
            rowActu = Math.abs(m.getIngameRow() - 4);
            while (rowActu <= Math.abs(m.getIngameRow()) && rowActu >= 0 && rowActu < 15) {
                if (board[rowActu][colActu] != mark){
                    break;
                }
                if (colActu == m.getIngameCol()){
                    return true;
                }
                rowActu++;
            }

            rowActu = Math.abs(m.getIngameRow() + 4);
            while (rowActu >= Math.abs(m.getIngameRow()) && rowActu >= 0 && rowActu < 15) {
                if (board[rowActu][colActu] != mark){
                    break;
                }
                if (colActu == m.getIngameCol()){
                    return true;
                }
                rowActu--;
            }


        }
        return false;
    }

    public List<Move> getPossibleMoves() {
        List<Move> moves = new ArrayList<>();
        int counter = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++){
                if (board[i][j] == Mark.EMPTY) {
                    char col = numberToLetter.get(j);
                    int row = Math.abs(i - 14);     //le board est "a l'envers"
                    Move move = new Move(col, row);
                    moves.add(move);
                    counter++;
                }
            }
        }
        if (counter == 0){
            Move move = new Move('h', 8);
            moves.clear();
            moves.add(move);
        } else if (counter == 2) {
            moves.removeIf(m -> m.getIngameRow() <= 10 && m.getIngameRow() >= 6 && m.getIngameCol() >= 'f' && m.getIngameCol() <= 'j');
        }

        return moves;
    }
}
