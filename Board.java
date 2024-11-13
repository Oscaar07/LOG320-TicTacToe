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

    private final int[] rowNumbersAroundIndex = {0, -1, -1, -1, 0, 1, 1, 1};
    private final int[] colNumbersAroundIndex = {-1, -1, 0, 1, 1, 1, 0, -1};



    private Square[] board;

    // Ne pas changer la signature de cette méthode
    public Board() {
        board = new Square[225];
        letterToNumber = new HashMap<>();
        numberToLetter = new HashMap<>();
        
        for (int i = 0; i < 225; i++) {
            board[i] = new Square(i);
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
        board = new Square[225];

        for (int i = 0; i < 225; i++) {
            if (existing.getBoard()[i].getMark() == Mark.RED){
                board[i] = new Square(i);
                board[i].setMark(Mark.RED);
            } else if (existing.getBoard()[i].getMark() == Mark.BLACK) {
                board[i] = new Square(i);
                board[i].setMark(Mark.BLACK);
            }
            else{
                board[i] = new Square(i);
            }
            board[i].setRedThreatValue(existing.getBoard()[i].getRedThreatValue());
            board[i].setBlackThreatValue(existing.getBoard()[i].getBlackThreatValue());
            board[i].setIndex(i);

        }
        moveList = new ArrayList<>();
        for (Move move : existing.getMoveList()){
            int row = move.getIngameRow();
            char c = move.getIngameCol();
            Move newMove = new Move(c, row);
            moveList.add(newMove);
        }

        letterToNumber = new HashMap<>();
        numberToLetter = new HashMap<>();

        for (int i = 0; i < 15; i++) {
            char letter = (char) ('A' + i);
            letterToNumber.put(letter, i);
        }
        for (int i = 0; i < 15; i++) {
            char letter = (char) ('A' + i);
            numberToLetter.put(i, letter);
        }


    }

    // Ajout de getters nécessaires
    public Mark getMark(int row, int col) {
        if (isValidPosition(row, col)) {
            return board[15 * row + col].getMark();
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
            

            int row = Math.abs(m.getIngameRow() - 15);
            Integer col = letterToNumber.get(normalizedCol);
            
            if (col == null) {
                throw new IllegalArgumentException("Colonne invalide: " + normalizedCol);
            }

            if (isValidPosition(row, col) && board[m.getIndex()].getMark()== Mark.EMPTY) {
                board[m.getIndex()].setMark(mark);
                /*
                for (int number : numbersAroundIndex){
                    int valeurPrecedente = board[m.getIndex()].getValue(mark);
                    board[m.getIndex() + number].setValeur(mark, valeurPrecedente + 1); //quand 2 alignes, valeur erronee
                }
                 */
                addValue(m, mark);
                moveList.add(m);
            } else {
                throw new RuntimeException("Cette case est déjà occupée ou position invalide");
            }
        } else {
            throw new IllegalArgumentException("Il n'est pas possible d'effacer la marque");
        }
    }

    public void addValue(Move m, Mark mark){
        int row = m.getIndex() / 15;
        int col = m.getIndex() % 15;
        for (int j = 0; j < 8; j++){      //pour tous les nombres autour de celui qon a place
            int rowNumber = rowNumbersAroundIndex[j];
            int colNumber = colNumbersAroundIndex[j];
            if (isValidPosition(row + rowNumber, col + colNumber)){ //si la case autour est dans la grille

                if (board[(row + rowNumber) * 15 + (col + colNumber)].getMark() == mark){   //si la case autour est de la mm marque que la marque placee
                    for (int i = 2; i < 5; i++){  //pourrait avoir un case (ex: 100pts pr 4, 50pts pr 3, etc)
                        int counter = 2;
                        int caseActuelle = (row + i * rowNumber) * 15 + (col + i * colNumber);
                        if (isValidPosition(row + i * rowNumber, col + i * colNumber)){

                            if (board[caseActuelle].getMark() != mark){
                                int valeurPrecedente = board[caseActuelle].getValue(mark);
                                board[caseActuelle].setValeur(mark, valeurPrecedente + 1);
                                if (isValidPosition((row - rowNumber),col-colNumber)){
                                    board[(row - rowNumber) * 15 + (col - colNumber)].setValeur(mark, valeurPrecedente + 1);
                                }
                                break;
                            }
                        }
                    }
                }
                else{
                    int valeurPrecedente = board[(row + rowNumber) * 15 + (col + colNumber)].getValue(mark);
                    board[(row + rowNumber)* 15 + (col + colNumber)].setValeur(mark, valeurPrecedente + 1);
                }

            }

        }
    }
    public void checkSequence(Square[] board){
        //check horizontal lines
        for (int i = 0; i < 15; i++){
            for (int j = 0; j < 15; j++){

            }
        }
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

        if (board[15 * (row + rowDir) + (col + colDir)].getMark() == enemy &&
            board[15 * (row + 2 * rowDir) + (col + 2 * colDir)].getMark() == enemy &&
            board[15 * (row + 3 * rowDir) + (col + 3 * colDir)].getMark() == mark) {
            // Effectue la capture
            board[15 * (row + rowDir) + (col + colDir)].setMark(Mark.EMPTY);
            board[15 * (row + 2 * rowDir) + (col + 2 * colDir)].setMark(Mark.EMPTY);
            return 1;
        }
        return 0;
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < 15 && col >= 0 && col < 15;
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
            
            if (!isValidPosition(row, col) || board[row * 15 + col].getMark() != mark) {
                return false;
            }
        }
        return true;
    }

    public List<Move> getPossibleMoves() {
        List<Move> moves = new ArrayList<>();

        // Parcourir le plateau avec des coordonnées valides (1-15)
        for (char col = 'A'; col <= 'O'; col++) {
            for (int row = 1; row <= 15; row++) {  // Commencer à 1, pas à 0
                try {
                    Move move = new Move(col, row);
                    if (isEmptyPosition(move)) {
                        moves.add(move);
                    }
                } catch (IllegalArgumentException e) {
                    continue;
                }
            }
        }
        
        return moves;
    }

    private boolean isEmptyPosition(Move move) {
        int row = Math.abs(move.getIngameRow() - 15);
        int col = letterToNumber.get(move.getIngameCol());
        return board[row * 15 + col].getMark() == Mark.EMPTY;
    }

    private boolean isValidSecondRedMove(Move move) {
        return move.getIngameRow() < 6 || move.getIngameRow() > 10 || move.getIngameCol() < 'F' || move.getIngameCol() > 'J';
    }

    // Méthode utilitaire pour vérifier si un coup est dans les limites
    public boolean isValidPosition(Move move) {
        int row = move.getIngameRow();
        char col = move.getIngameCol();
        
        return row >= 1 && row <= 15 &&  // Vérifier les limites des lignes
               col >= 'A' && col <= 'O';  // Vérifier les limites des colonnes
    }

    public Square[] getBoard() {
        return board;
    }

    public ArrayList<Move> getMoveList() {
        return moveList;
    }
}
