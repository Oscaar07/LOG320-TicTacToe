import java.util.ArrayList;
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

    private final int[] numbersAroundIndex = {-1, -16, -15, -14, 1, 16, 15, 14};
    private final int[] rowNumbersAroundIndex = {0, -1, -1, -1, 0, 1, 1, 1};
    private final int[] colNumbersAroundIndex = {-1, -1, 0, 1, 1, 1, 0, -1};

    private static final int WIN_SCORE = 1000000;
    private static final int LOSE_SCORE = -1000000;
    private static final int FIVE_IN_A_ROW = 1000000;
    private static final int FOUR_OPEN = 9000;
    private static final int FOUR_CLOSED = 2000;
    private static final int THREE_OPEN = 1050;
    private static final int THREE_CLOSED = 10;
    private static final int TWO = 5;
    private static final int TWO_CLOSED = 2;
    private static final int CAPTURE_VALUE = 300;

    private static final int ONE_CAPTURE = 100;
    private static final int TWO_CAPTURE = 200;
    private static final int THREE_CAPTURE = 300;
    private static final int FOUR_CAPTURE = 800;
    private static final int FIVE_CAPTURE = 1000000;

    private static final int VALEUR_ENLIGNE = 1;

    private static final int AJOUT_PIECE = 1;


    private static final int NBLIGNES = 15;
    private static final int NBCOLONNES = 15;

    private int nbCapturesRouge = 0;
    private int nbCapturesNoir = 0;


    private Square[][] board;


    // Ne pas changer la signature de cette méthode
    public Board() {
        board = new Square[NBLIGNES][NBCOLONNES];
        letterToNumber = new HashMap<>();
        numberToLetter = new HashMap<>();


        for (int i = 0; i < NBLIGNES; i++){
            for (int j = 0; j < NBCOLONNES; j++){
                board[i][j] = new Square(i, j);
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

        board = new Square[NBLIGNES][NBCOLONNES];

        for (int i = 0; i < NBLIGNES; i++){
            for (int j = 0; j < NBCOLONNES; j++){
                if (existing.board[i][j].getMark() == Mark.RED){
                    board[i][j] = new Square(i, j);
                    board[i][j].setMark(Mark.RED);
                } else if (existing.board[i][j].getMark() == Mark.BLACK) {
                    board[i][j] = new Square(i, j);
                    board[i][j].setMark(Mark.BLACK);
                }
                else board[i][j] = new Square(i, j);

                board[i][j].setRedThreatValue(existing.board[i][j].getRedThreatValue());
                board[i][j].setBlackThreatValue(existing.board[i][j].getBlackThreatValue());
                //peut etre set l index

            }
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

        nbCapturesRouge = existing.getNbCapturesRouge();
        nbCapturesNoir = existing.getNbCapturesNoir();

    }




    // Ajout de getters nécessaires
    public Mark getMark(int row, int col) {
        if (isValidPosition(row, col)) {
            return board[row][col].getMark();
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

            if (isValidPosition(row, col) && board[row][col].getMark() == Mark.EMPTY) {
                board[row][col].setMark(mark);
                addValue(row, col, mark, AJOUT_PIECE);
                ArrayList<Integer> directionArr = checkCaptureDirection(mark, m);    //check pour les captures serait ici
                removeCapturedPieces(directionArr, row, col, mark);
                ArrayList<Sequence> sequenceArrayList = searchSequences();
                updateBoard(sequenceArrayList, mark);
                moveList.add(m);
            } else {
                throw new RuntimeException("Cette case est déjà occupée ou position invalide");
            }
        } else {
            throw new IllegalArgumentException("Il n'est pas possible d'effacer la marque");
        }
    }

    //Ajoute des valeurs de menace autour de la piece placee
    public void addValue(int row, int col,  Mark mark, int value){
        for (int j = 0; j < 8; j++){      //pour tous les nombres autour de celui qon a place
            int rowNumber = rowNumbersAroundIndex[j];
            int colNumber = colNumbersAroundIndex[j];
            if (isValidPosition(row + rowNumber, col + colNumber)){ //si la case autour est dans la grille

                board[row + rowNumber][col + colNumber].addValeur(mark, value);
            }

        }
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
            if (!isValidPosition(startRow + i * rowDir, startCol + i * colDir) || board[startRow + i * rowDir][startCol + i * colDir].getMark() != mark) {
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
        return board[row][col].getMark() == Mark.EMPTY;
    }



    public Square[][] getBoard() {
        return board;
    }

    public ArrayList<Move> getMoveList() {
        return moveList;
    }

    //Trouver les sequences de 2, 3, 4 ou 5 et les identifier
    public ArrayList<Sequence> searchSequences(){
        ArrayList<Sequence> sequenceArrayList = new ArrayList<>();
        //sequences horizontales
        for (int i = 0; i < NBLIGNES; i++){
            int counterMarkEnSuite = 0;
            int colDebut = 0;
            Mark mark = null;
            for (int j = 0; j < NBCOLONNES; j++){
                if (board[i][j].getMark() == Mark.RED || board[i][j].getMark() == Mark.BLACK){
                    if (board[i][j].getMark() == mark){
                        counterMarkEnSuite++;
                    }
                    else{
                        if (counterMarkEnSuite >= 2){
                            Sequence seq = new Sequence(i, i, colDebut, j -1 , mark, 1, 0, counterMarkEnSuite);
                            sequenceArrayList.add(seq);
                        }
                        mark = board[i][j].getMark();
                        counterMarkEnSuite = 1;
                        colDebut = j;
                    }
                }
                else{
                    if (counterMarkEnSuite >= 2){
                        Sequence seq2 = new Sequence(i , i , colDebut, j - 1, mark, 1, 0, counterMarkEnSuite);
                        sequenceArrayList.add(seq2);
                        mark = null;
                        counterMarkEnSuite = 0;
                    }
                    mark = null;
                    counterMarkEnSuite = 0;
                }
            }
            if (counterMarkEnSuite >= 2){
                Sequence seq3 = new Sequence(i, i, colDebut, NBCOLONNES - 1, mark, 1, 0, counterMarkEnSuite);
                sequenceArrayList.add(seq3);
            }

        }

        //sequences verticales
        for (int i = 0; i < NBCOLONNES ; i++){  //represente colonnes
            int counterMarkEnSuite = 0;
            int ligneDebut = 0;
            Mark mark = null;
            int j;
            for (j = 0; j < NBLIGNES; j++){   //represente lignes
                if (board[j][i].getMark() == Mark.RED || board[j][i].getMark() == Mark.BLACK){
                    if (board[j][i].getMark() == mark){
                        counterMarkEnSuite++;
                    }
                    else{
                        if (counterMarkEnSuite >= 2){
                            Sequence seq = new Sequence(ligneDebut, j - 1, i, i, mark, 0, 1, counterMarkEnSuite);
                            sequenceArrayList.add(seq);
                        }
                        mark = board[j][i].getMark();
                        counterMarkEnSuite = 1;
                        ligneDebut = j;
                    }
                }
                else{
                    if (counterMarkEnSuite >= 2){
                        Sequence seq2 = new Sequence(ligneDebut, j - 1, i, i, mark, 0, 1, counterMarkEnSuite);
                        sequenceArrayList.add(seq2);
                    }
                    mark = null;
                    counterMarkEnSuite = 0;
                }

            }
            if (counterMarkEnSuite >= 2){
                Sequence seq3 = new Sequence(ligneDebut, NBLIGNES - 1, i, i, mark, 0, 1, counterMarkEnSuite);
                sequenceArrayList.add(seq3);
            }
        }

        //sequences diagonales haut-gauche a bas-droite (x:+1, y:+1)
        //diagonale inferieure
        for (int m = 0; m < 12; m++){       //lignes d ou part la recherche
            int counterMarkEnSuite = 0;
            Mark mark = null;
            int ligneDebut = 0;
            int colDebut = 0;
            for (int i = m, j = 0; j < NBLIGNES; i++, j++){ //i et j sont lignes et colonnes
                if (!isValidPosition(i, j)){
                    break;
                }
                if (board[i][j].getMark() == Mark.RED || board[i][j].getMark() == Mark.BLACK){
                    if (board[i][j].getMark() == mark){
                        counterMarkEnSuite++;
                    }
                    else{
                        if (counterMarkEnSuite >= 2){
                            Sequence seq = new Sequence(ligneDebut, i - 1, colDebut, j - 1, mark, 1, 1, counterMarkEnSuite);
                            sequenceArrayList.add(seq);
                        }
                        mark = board[i][j].getMark();
                        counterMarkEnSuite = 1;
                        ligneDebut = i;
                        colDebut = j;
                    }
                }
                else{
                    if (counterMarkEnSuite >= 2){
                        Sequence seq2 = new Sequence(ligneDebut, i - 1, colDebut, j - 1, mark, 1, 1, counterMarkEnSuite);
                        sequenceArrayList.add(seq2);
                    }
                    mark = null;
                    counterMarkEnSuite = 0;
                }
            }
            if (counterMarkEnSuite >= 2){
                Sequence seq3 = new Sequence(ligneDebut, NBLIGNES - 1, colDebut, NBCOLONNES - 1 - m, mark, 1, 1, counterMarkEnSuite);
                sequenceArrayList.add(seq3);
            }
        }



        //sequences diagonales haut-gauche a bas-droite (x:+1, y:+1)
        //diagonale superieure
        for (int m = 1; m < 12; m++){       //col d ou part la recherche
            int counterMarkEnSuite = 0;
            Mark mark = null;
            int ligneDebut = 0;
            int colDebut = 0;
            for (int i = 0, j = m; i < NBLIGNES; i++, j++){ //i et j sont lignes et colonnes
                if (!isValidPosition(i, j)){
                    break;
                }
                if (board[i][j].getMark() == Mark.RED || board[i][j].getMark() == Mark.BLACK){
                    if (board[i][j].getMark() == mark){
                        counterMarkEnSuite++;
                    }
                    else{
                        if (counterMarkEnSuite >= 2){
                            Sequence seq = new Sequence(ligneDebut, i - 1, colDebut, j - 1, mark, 1, 1, counterMarkEnSuite);
                            sequenceArrayList.add(seq);
                        }
                        mark = board[i][j].getMark();
                        counterMarkEnSuite = 1;
                        ligneDebut = i;
                        colDebut = j;
                    }
                }
                else{
                    if (counterMarkEnSuite >= 2){
                        Sequence seq2 = new Sequence(ligneDebut, i - 1, colDebut, j - 1, mark, 1, 1, counterMarkEnSuite);
                        sequenceArrayList.add(seq2);
                    }
                    mark = null;
                    counterMarkEnSuite = 0;
                }
            }
            if (counterMarkEnSuite >= 2){
                Sequence seq3 = new Sequence(ligneDebut, NBCOLONNES - 1 - m, colDebut, NBCOLONNES - 1, mark, 1, 1, counterMarkEnSuite);
                sequenceArrayList.add(seq3);
            }
        }

        //sequences diagonales haut-droite a bas-gauche (x:-1, y:+1)
        //diagonale superieure
        for (int m = 3; m < NBCOLONNES; m++){       //col d ou part la recherche
            int counterMarkEnSuite = 0;
            Mark mark = null;
            int ligneDebut = 0;
            int colDebut = 0;
            for (int i = 0, j = m; i < NBLIGNES; i++, j--){ //i et j sont lignes et colonnes
                if (!isValidPosition(i, j)){
                    break;
                }
                if (board[i][j].getMark() == Mark.RED || board[i][j].getMark() == Mark.BLACK){
                    if (board[i][j].getMark() == mark){
                        counterMarkEnSuite++;
                    }
                    else{
                        if (counterMarkEnSuite >= 2){
                            Sequence seq = new Sequence(ligneDebut, i - 1, colDebut, j + 1, mark, -1, 1, counterMarkEnSuite);
                            sequenceArrayList.add(seq);
                        }
                        mark = board[i][j].getMark();
                        counterMarkEnSuite = 1;
                        ligneDebut = i;
                        colDebut = j;
                    }
                }
                else{
                    if (counterMarkEnSuite >= 2){
                        Sequence seq2 = new Sequence(ligneDebut, i - 1, colDebut, j + 1, mark, -1, 1, counterMarkEnSuite);
                        sequenceArrayList.add(seq2);
                    }
                    mark = null;
                    counterMarkEnSuite = 0;
                }
            }
            if (counterMarkEnSuite >= 2){
                Sequence seq3 = new Sequence(ligneDebut, m, colDebut, 0, mark, -1, 1, counterMarkEnSuite);
                sequenceArrayList.add(seq3);
            }
        }

        //sequences diagonales haut-droite a bas-gauche (x:-1, y:+1)
        //diagonale inferieure
        for (int m = 1; m < 12; m++){       //lignes d ou part la recherche
            int counterMarkEnSuite = 0;
            Mark mark = null;
            int ligneDebut = 0;
            int colDebut = 0;
            for (int i = m, j = NBCOLONNES - 1; i < NBLIGNES; i++, j--){ //i et j sont lignes et colonnes
                if (!isValidPosition(i, j)){
                    break;
                }
                if (board[i][j].getMark() == Mark.RED || board[i][j].getMark() == Mark.BLACK){
                    if (board[i][j].getMark() == mark){
                        counterMarkEnSuite++;
                    }
                    else{
                        if (counterMarkEnSuite >= 2){
                            Sequence seq = new Sequence(ligneDebut, i - 1, colDebut, j + 1, mark, -1, 1, counterMarkEnSuite);
                            sequenceArrayList.add(seq);
                        }
                        mark = board[i][j].getMark();
                        counterMarkEnSuite = 1;
                        ligneDebut = i;
                        colDebut = j;
                    }
                }
                else{
                    if (counterMarkEnSuite >= 2){
                        Sequence seq2 = new Sequence(ligneDebut, i - 1, colDebut, j + 1, mark, -1, 1, counterMarkEnSuite);
                        sequenceArrayList.add(seq2);
                    }
                    mark = null;
                    counterMarkEnSuite = 0;
                }
            }
            if (counterMarkEnSuite >= 2){
                Sequence seq3 = new Sequence(ligneDebut, NBLIGNES - 1, colDebut, m, mark, -1, 1, counterMarkEnSuite);
                sequenceArrayList.add(seq3);
            }
        }

        return sequenceArrayList;
    }

    public void update2ndMoveRed(){
        if (getMoveList().get(2).toString().equals("E11")){
            board[6][6].addValeur(Mark.RED, VALEUR_ENLIGNE);
            board[5][5].addValeur(Mark.RED, VALEUR_ENLIGNE);
            board[3][3].addValeur(Mark.RED, VALEUR_ENLIGNE-1);
        } else if (getMoveList().get(2).toString().equals("E5")) {
            board[8][6].addValeur(Mark.RED, VALEUR_ENLIGNE);
            board[9][5].addValeur(Mark.RED, VALEUR_ENLIGNE);
            board[11][3].addValeur(Mark.RED, VALEUR_ENLIGNE-1);
        } else if (getMoveList().get(2).toString().equals("K11")) {
            board[6][8].addValeur(Mark.RED, VALEUR_ENLIGNE);
            board[5][9].addValeur(Mark.RED, VALEUR_ENLIGNE);
            board[3][11].addValeur(Mark.RED, VALEUR_ENLIGNE-1);
        } else if (getMoveList().get(2).toString().equals("K5")) {
            board[8][8].addValeur(Mark.RED, VALEUR_ENLIGNE);
            board[9][9].addValeur(Mark.RED, VALEUR_ENLIGNE);
            board[11][11].addValeur(Mark.RED, VALEUR_ENLIGNE-1);
        }
    }

    public void clearValuesFromBoard(){
        for (int i = 0; i < NBLIGNES; i++){
            for (int j = 0; j < NBCOLONNES; j++){
                board[i][j].setRedThreatValue(0);
                board[i][j].setBlackThreatValue(0);

            }
        }
        for (int i = 0; i < NBLIGNES; i++){
            for (int j = 0; j < NBCOLONNES; j++){
                if (board[i][j].getMark() != Mark.EMPTY)
                {
                    Mark mark = board[i][j].getMark();
                    addValue(i, j, mark, AJOUT_PIECE);
                }
            }
        }

    }

    public void updateBoard(ArrayList<Sequence> sequenceArrayList, Mark mark){
        clearValuesFromBoard();

        for (Sequence seq : sequenceArrayList){
            int length = seq.getLength();
            int points = 0;
            boolean twoInARowWithThirdSpacedAvant = false;
            boolean twoInARowWithThirdSpacedApres = false;
            boolean threeInARowWithFourthSpacedAvant = false;
            boolean threeInARowWithFourthSpacedApres = false;

            boolean caseAvantValide = false;
            boolean caseApresValide = false;
            boolean caseDeuxAvantValide = false;
            boolean caseDeuxApresValide = false;

            int caseAvantRow = seq.getRowDebut() - seq.getRowDirection();
            int caseAvantCol = seq.getColDebut() - seq.getColDirection();

            int caseApresRow = seq.getRowFin() + seq.getRowDirection();
            int caseApresCol = seq.getColFin() + seq.getColDirection();

            int caseDeuxAvantRow = seq.getRowDebut() - 2 * seq.getRowDirection();
            int caseDeuxAvantCol = seq.getColDebut() - 2 * seq.getColDirection();

            int caseDeuxApresRow = seq.getRowFin() + 2 * seq.getRowDirection();
            int caseDeuxApresCol = seq.getColFin() + 2 * seq.getColDirection();

            if (isValidPosition(caseAvantRow, caseAvantCol)){
                caseAvantValide = true;
            }
            if (isValidPosition(caseApresRow, caseApresCol)){
                caseApresValide = true;
            }
            if (isValidPosition(caseDeuxAvantRow, caseDeuxAvantCol)){
                caseDeuxAvantValide = true;
            }
            if (isValidPosition(caseDeuxApresRow, caseDeuxApresCol)){
                caseDeuxApresValide = true;
            }

            switch(length){
                case 2:
                    if (caseAvantValide ^ caseApresValide){  //xor operator pour savoir quand une des cases est correcte et l autre non
                        points = TWO_CLOSED;
                        break;
                    }

                    if (caseDeuxAvantValide && board[caseDeuxAvantRow][caseDeuxAvantCol].getMark() == seq.getMark() && board[caseAvantRow][caseAvantCol].getMark() == Mark.EMPTY){
                        twoInARowWithThirdSpacedAvant = true;
                    }

                    if (caseDeuxApresValide && board[caseDeuxApresRow][caseDeuxApresCol].getMark() == seq.getMark() && board[caseApresRow][caseApresCol].getMark() == Mark.EMPTY){
                        twoInARowWithThirdSpacedApres = true;
                    }

                    if (caseAvantValide && caseApresValide){
                        if (board[caseAvantRow][caseAvantCol].getMark() == mark.enemy() && board[caseApresRow][caseApresCol].getMark() == Mark.EMPTY){
                            board[caseApresRow][caseApresCol].addValeur(mark.enemy(), CAPTURE_VALUE);
                        }
                        if (board[caseAvantRow][caseAvantCol].getMark() == Mark.EMPTY && board[caseApresRow][caseApresCol].getMark() == mark.enemy()) {
                            board[caseAvantRow][caseAvantCol].addValeur(mark.enemy(), CAPTURE_VALUE);
                        }
                        points = TWO;
                    }

                    break;
                case 3:
                    if (caseAvantValide ^ caseApresValide){
                        points = THREE_CLOSED;
                        break;
                    }

                    if (caseDeuxAvantValide && board[caseDeuxAvantRow][caseDeuxAvantCol].getMark() == seq.getMark() && board[caseAvantRow][caseAvantCol].getMark() == Mark.EMPTY){
                        threeInARowWithFourthSpacedAvant = true;
                    }

                    if (caseDeuxApresValide && board[caseDeuxApresRow][caseDeuxApresCol].getMark() == seq.getMark() && board[caseApresRow][caseApresCol].getMark() == Mark.EMPTY){
                        threeInARowWithFourthSpacedApres = true;
                    }

                    if (caseAvantValide && caseApresValide){
                        if (board[caseAvantRow][caseAvantCol].getMark() == Mark.EMPTY && board[caseApresRow][caseApresCol].getMark() == Mark.EMPTY){
                            points = THREE_OPEN;
                        }else points = THREE_CLOSED;
                    }
                    break;
                case 4:
                    if (caseAvantValide ^ caseApresValide){
                        points = FOUR_CLOSED;
                        break;
                    }

                    if (caseAvantValide && caseApresValide){
                        if (board[caseAvantRow][caseAvantCol].getMark() == Mark.EMPTY && board[caseApresRow][caseApresCol].getMark() == Mark.EMPTY){
                            points = FOUR_OPEN;
                        }else points = FOUR_CLOSED;
                    }
                    break;
                case 5:
                    break;
            }

            if (caseAvantValide && board[caseAvantRow][caseAvantCol].getMark() == Mark.EMPTY){
                board[caseAvantRow][caseAvantCol].addValeur(seq.getMark(), points);
            }
            if (caseApresValide && board[caseApresRow][caseApresCol].getMark() == Mark.EMPTY){
                board[caseApresRow][caseApresCol].addValeur(seq.getMark(), points);
            }

            if (twoInARowWithThirdSpacedAvant){
                board[caseAvantRow][caseAvantCol].addValeur(seq.getMark(), THREE_OPEN);
            }
            if (twoInARowWithThirdSpacedApres){
                board[caseApresRow][caseApresCol].addValeur(seq.getMark(), THREE_OPEN);
            }
            if (threeInARowWithFourthSpacedAvant){
                board[caseAvantRow][caseAvantCol].addValeur(seq.getMark(), FOUR_OPEN);
            }
            if (threeInARowWithFourthSpacedApres){
                board[caseApresRow][caseApresCol].addValeur(seq.getMark(), FOUR_OPEN);
            }


        }
    }

    //Quand un coup est joue, on regarde si il y a eu une capture dans les 8 directions possible
    //Si oui, on retourne la direction de la capture
    private ArrayList<Integer> checkCaptureDirection(Mark mark, Move move) {
        ArrayList<Integer> directionsCapture = new ArrayList<>();

        int startingRow = move.getGridRow();
        int startingCol = move.getGridCol();

        for (int i = 0; i < rowNumbersAroundIndex.length; i++){
            int firstNextSquareRow = startingRow + rowNumbersAroundIndex[i];
            int firstNextSquareCol = startingCol + colNumbersAroundIndex[i];

            int secondNextSquareRow = startingRow + 2 * rowNumbersAroundIndex[i];
            int secondNextSquareCol = startingCol + 2 * colNumbersAroundIndex[i];

            int thirdNextSquareRow = startingRow + 3 * rowNumbersAroundIndex[i];
            int thirdNextSquareCol = startingCol + 3 * colNumbersAroundIndex[i];

            if (isValidPosition(firstNextSquareRow, firstNextSquareCol) && isValidPosition(secondNextSquareRow, secondNextSquareCol)
                    && isValidPosition(thirdNextSquareRow, thirdNextSquareCol)){
                if (board[firstNextSquareRow][firstNextSquareCol].getMark() == mark.enemy() && board[secondNextSquareRow][secondNextSquareCol].getMark() == mark.enemy()){
                    if (board[thirdNextSquareRow][thirdNextSquareCol].getMark() == mark){
                        directionsCapture.add(rowNumbersAroundIndex[i]);        //si on peut capturer, on ajoute la direction de ligne et de col
                        directionsCapture.add(colNumbersAroundIndex[i]);
                    }
                }
            }
        }

        return directionsCapture;
    }

    public void removeCapturedPieces(ArrayList<Integer> captureDirections, int startingRow, int startingCol, Mark mark){

        for (int i = 0; i < captureDirections.size() / 2; i += 2){
            board[startingRow + captureDirections.get(i)][startingCol + captureDirections.get(i + 1)].setMark(Mark.EMPTY);
            addValue(startingRow + captureDirections.get(i), startingCol + captureDirections.get(i + 1), mark.enemy(), -AJOUT_PIECE);

            board[startingRow + 2 * captureDirections.get(i)][startingCol + 2 * captureDirections.get(i + 1)].setMark(Mark.EMPTY);
            addValue(startingRow + 2 * captureDirections.get(i), startingCol + 2 * captureDirections.get(i + 1), mark.enemy(), -AJOUT_PIECE);

        }

        if (mark == Mark.RED){
            nbCapturesRouge++;
        }
        else{
            nbCapturesNoir++;
        }
    }


    public int getEvalCapturesNoir() {
        return switch (nbCapturesNoir) {
            case 1 -> ONE_CAPTURE;
            case 2 -> TWO_CAPTURE;
            case 3 -> THREE_CAPTURE;
            case 4 -> FOUR_CAPTURE;
            case 5 -> FIVE_CAPTURE;
            default -> 0;
        };
    }

    public int getEvalCapturesRouge() {
        return switch (nbCapturesRouge) {
            case 1 -> ONE_CAPTURE;
            case 2 -> TWO_CAPTURE;
            case 3 -> THREE_CAPTURE;
            case 4 -> FOUR_CAPTURE;
            case 5 -> FIVE_CAPTURE;
            default -> 0;
        };
    }

    public int getNbCapturesRouge() {
        return nbCapturesRouge;
    }

    public int getNbCapturesNoir() {
        return nbCapturesNoir;
    }
}
