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
    private static final int THREE_CLOSED = 5;
    private static final int TWO = 5;
    private static final int TWO_CLOSED = 2;
    private static final int CAPTURE_VALUE = 5;      //1000

    private static final int ONE_CAPTURE = 10;        //1000
    private static final int TWO_CAPTURE = 10;        //2000
    private static final int THREE_CAPTURE = 10;      //4000
    private static final int FOUR_CAPTURE = 8000;       //8000
    private static final int FIVE_CAPTURE = 1000000;    //1000000

    private static final int VALEUR_ENLIGNE = 2;

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

        for (int i = 0; i < NBLIGNES; i++){
            for (int j = 0; j < NBCOLONNES; j++){
                if (existing.)
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
                addValue(m, mark);
                ArrayList<Integer> directionArr = checkCaptureDirection(mark, m);    //check pour les captures serait ici
                removeCapturedPieces(directionArr, m.getIndex(), mark);
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

    public void addValue(Move m, Mark mark){
        int row = m.getIndex() / 15;
        int col = m.getIndex() % 15;
        for (int j = 0; j < 8; j++){      //pour tous les nombres autour de celui qon a place
            int rowNumber = rowNumbersAroundIndex[j];
            int colNumber = colNumbersAroundIndex[j];
            if (isValidPosition(row + rowNumber, col + colNumber)){ //si la case autour est dans la grille

                int valeurPrecedente = board[(row + rowNumber) * 15 + (col + colNumber)].getValue(mark);
                board[(row + rowNumber)* 15 + (col + colNumber)].setValeur(mark, valeurPrecedente + 1);

            }

        }
    }

    public void removeValue(int index, Mark mark){
        int row = index / 15;
        int col = index % 15;
        for (int j = 0; j < 8; j++){      //pour tous les nombres autour de celui qon a place
            int rowNumber = rowNumbersAroundIndex[j];
            int colNumber = colNumbersAroundIndex[j];
            if (isValidPosition(row + rowNumber, col + colNumber)){ //si la case autour est dans la grille

                int valeurPrecedente = board[(row + rowNumber) * 15 + (col + colNumber)].getValue(mark);
                board[(row + rowNumber)* 15 + (col + colNumber)].setValeur(mark, valeurPrecedente - 1);

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



    public Square[] getBoard() {
        return board;
    }

    public ArrayList<Move> getMoveList() {
        return moveList;
    }

    public ArrayList<Sequence> searchSequences(){
        ArrayList<Sequence> sequenceArrayList = new ArrayList<>();
        //sequences horizontales
        for (int i = 0; i < 15; i++){
            int counterMarkEnSuite = 0;
            int indexDebut = 0;
            Mark mark = null;
            for (int j = 0; j < 15; j++){   //changer j = 1 pour j = 0
                if (getBoard()[i * 15 + j].getMark() == Mark.RED || getBoard()[i * 15 + j].getMark() == Mark.BLACK){
                    if (getBoard()[i * 15 + j].getMark() == mark){
                        counterMarkEnSuite++;
                    }
                    else{
                        if (counterMarkEnSuite >= 2){
                            Sequence seq = new Sequence((i * 15 + indexDebut), i * 15 + j - 1, mark, 1, 0, counterMarkEnSuite);
                            sequenceArrayList.add(seq);
                        }
                        mark = getBoard()[i * 15 + j].getMark();
                        counterMarkEnSuite = 1;
                        indexDebut = j;
                    }
                }
                else{
                    if (counterMarkEnSuite >= 2){
                        Sequence seq2 = new Sequence(i * 15 + indexDebut, i * 15 + j - 1, mark, 1, 0, counterMarkEnSuite);
                        sequenceArrayList.add(seq2);
                        mark = null;
                        counterMarkEnSuite = 0;
                    }
                    mark = null;
                    counterMarkEnSuite = 0;
                }
            }
            if (counterMarkEnSuite >= 2){
                Sequence seq3 = new Sequence(i * 15 + indexDebut, 14, mark, 1, 0, counterMarkEnSuite);
                sequenceArrayList.add(seq3);
            }

        }

        //sequences verticales
        for (int i = 0; i < 15 ; i++){  //represente colonnes
            int counterMarkEnSuite = 0;
            int indexDebut = 0;
            Mark mark = null;
            int j;
            for (j = 0; j < 15; j++){   //represente lignes
                int indexActuel = j * 15 + i;
                if (getBoard()[indexActuel].getMark() == Mark.RED || getBoard()[indexActuel].getMark() == Mark.BLACK){
                    if (getBoard()[indexActuel].getMark() == mark){
                        counterMarkEnSuite++;
                    }
                    else{
                        if (counterMarkEnSuite >= 2){
                            Sequence seq = new Sequence((indexDebut * 15 + i), indexActuel - 15, mark, 0, 1, counterMarkEnSuite);   //erreur ?
                            sequenceArrayList.add(seq);
                        }
                        mark = getBoard()[indexActuel].getMark();
                        counterMarkEnSuite = 1;
                        indexDebut = j;
                    }
                }
                else{
                    if (counterMarkEnSuite >= 2){
                        Sequence seq2 = new Sequence(indexDebut * 15 + i, indexActuel - 15, mark, 0, 1, counterMarkEnSuite);
                        sequenceArrayList.add(seq2);
                    }
                    mark = null;
                    counterMarkEnSuite = 0;
                }

            }
            if (counterMarkEnSuite >= 2){
                Sequence seq3 = new Sequence(indexDebut * 15 + i, 14 * 15 + i, mark, 0, 1, counterMarkEnSuite);
                sequenceArrayList.add(seq3);
            }
        }

        //sequences diagonales haut-gauche a bas-droite (x:+1, y:+1)
        //diagonale inferieure
        int i = 0;  //lignes
        int j = 0;
        for (int m = 0; m < 12; m++){
            int counterMarkEnSuite = 0;
            int indexDebut = 0;
            Mark mark = null;
            while (isValidPosition(i, j)){
                int indexActuel = (m * 15) + (i * 16);
                if (getBoard()[indexActuel].getMark() == Mark.RED || getBoard()[indexActuel].getMark() == Mark.BLACK){
                    if (getBoard()[indexActuel].getMark() == mark){
                        counterMarkEnSuite++;
                    }
                    else{
                        if (counterMarkEnSuite >= 2){
                            Sequence seq = new Sequence((m * 15 + indexDebut * 16), indexActuel - 16, mark, 1, 1, counterMarkEnSuite);
                            sequenceArrayList.add(seq);
                        }
                        mark = getBoard()[indexActuel].getMark();
                        counterMarkEnSuite = 1;
                        indexDebut = j;
                    }
                }
                else{
                    if (counterMarkEnSuite >= 2){
                        Sequence seq2 = new Sequence(m * 15 + indexDebut * 16, indexActuel - 16, mark, 1, 1, counterMarkEnSuite);
                        sequenceArrayList.add(seq2);
                    }
                    mark = null;
                    counterMarkEnSuite = 0;
                }

                i++;
                j++;
            }
            if (counterMarkEnSuite >= 2){
                Sequence seq3 = new Sequence(indexDebut * 15 + i, 14 * 15 + i, mark, 1, 1,counterMarkEnSuite);
                sequenceArrayList.add(seq3);
            }
        }

        //diagonale haut-gauche a bas-droite
        //diagonale superieure
        j = 0;
        for (int m = 1; m < 13; m++){
            int counterMarkEnSuite = 0;
            int indexDebut = 0;
            Mark mark = null;
            i = m;
            j = 0;
            while (isValidPosition(j, i)){
                int indexActuel = j * 16 + m;
                if (getBoard()[indexActuel].getMark() == Mark.RED || getBoard()[indexActuel].getMark() == Mark.BLACK){
                    if (getBoard()[indexActuel].getMark() == mark){
                        counterMarkEnSuite++;
                    }
                    else{
                        if (counterMarkEnSuite >= 2){
                            Sequence seq = new Sequence((indexDebut * 16 + m), indexActuel - 16, mark, 1, 1, counterMarkEnSuite);
                            sequenceArrayList.add(seq);
                        }
                        mark = getBoard()[indexActuel].getMark();
                        counterMarkEnSuite = 1;
                        indexDebut = j;
                    }
                }
                else{
                    if (counterMarkEnSuite >= 2){
                        Sequence seq2 = new Sequence(m + indexDebut * 16, indexActuel - 16, mark, 1, 1, counterMarkEnSuite);
                        sequenceArrayList.add(seq2);
                    }
                    mark = null;
                    counterMarkEnSuite = 0;
                }

                i++;
                j++;
            }
            if (counterMarkEnSuite >= 2){
                Sequence seq3 = new Sequence(indexDebut * 16 + m, indexDebut * 16 + 14, mark, 1, 1,counterMarkEnSuite);
                sequenceArrayList.add(seq3);
            }
        }

        //diagonale haut-droite a bas-gauche
        //diagonale inferieure
        i = 1;  //runningLine
        //m: starting line
        j = 0;
        for (int m = 0; m < 12; m++){
            int counterMarkEnSuite = 0;
            int indexDebut = 0;
            Mark mark = null;
            i = 1;
            j = m;
            while (isValidPosition(j, j)){
                int indexActuel = (m * 15) + (i * 14);  //m: lignes i: colonnes en partant de la droite
                if (getBoard()[indexActuel].getMark() == Mark.RED || getBoard()[indexActuel].getMark() == Mark.BLACK){
                    if (getBoard()[indexActuel].getMark() == mark){
                        counterMarkEnSuite++;
                    }
                    else{
                        if (counterMarkEnSuite >= 2){
                            Sequence seq = new Sequence((m * 15 + indexDebut * 14), indexActuel - 14, mark, -1, 1, counterMarkEnSuite);
                            sequenceArrayList.add(seq);
                        }
                        mark = getBoard()[indexActuel].getMark();
                        counterMarkEnSuite = 1;
                        indexDebut = i;
                    }
                }
                else{
                    if (counterMarkEnSuite >= 2){
                        Sequence seq2 = new Sequence(m * 15 + indexDebut * 14, indexActuel - 14, mark, -1, 1,counterMarkEnSuite);
                        sequenceArrayList.add(seq2);
                    }
                    mark = null;
                    counterMarkEnSuite = 0;
                }

                i++;
                j++;
            }
            if (counterMarkEnSuite >= 2){
                Sequence seq3 = new Sequence(m * 15 + indexDebut * 14, 14 * 15 + m, mark, -1, 1, counterMarkEnSuite);
                sequenceArrayList.add(seq3);
            }
        }

        //diagonale haut-droite a bas-gauche
        //diagonale superieure
        j = 0;
        for (int m = 2; m < 14; m++){
            int counterMarkEnSuite = 0;
            int indexDebut = 0;
            Mark mark = null;
            i = m;  //colonnes
            j = 0;  //lignes
            while (isValidPosition(j, i)){
                int indexActuel = j * 15 + i;
                if (getBoard()[indexActuel].getMark() == Mark.RED || getBoard()[indexActuel].getMark() == Mark.BLACK){
                    if (getBoard()[indexActuel].getMark() == mark){
                        counterMarkEnSuite++;
                    }
                    else{
                        if (counterMarkEnSuite >= 2){
                            Sequence seq = new Sequence((indexDebut * 14 + i), indexActuel - 14, mark, -1, 1, counterMarkEnSuite);
                            sequenceArrayList.add(seq);
                        }
                        mark = getBoard()[indexActuel].getMark();
                        counterMarkEnSuite = 1;
                        indexDebut = j;
                    }
                }
                else{
                    if (counterMarkEnSuite >= 2){
                        Sequence seq2 = new Sequence((indexDebut * 14 + i), indexActuel - 14, mark, -1, 1, counterMarkEnSuite);
                        sequenceArrayList.add(seq2);
                    }
                    mark = null;
                    counterMarkEnSuite = 0;
                }

                i--;
                j++;
            }
            if (counterMarkEnSuite >= 2){
                Sequence seq3 = new Sequence(indexDebut * 14 + i, i + 14 * i, mark, 1, 1, counterMarkEnSuite);
                sequenceArrayList.add(seq3);
            }
        }


        return sequenceArrayList;
    }

    public void update2ndMoveRed(){
        if (getMoveList().get(2).toString().equals("E11")){
            getBoard()[80].addValeur(Mark.RED, VALEUR_ENLIGNE);
            getBoard()[96].addValeur(Mark.RED, VALEUR_ENLIGNE);
            getBoard()[48].addValeur(Mark.RED, VALEUR_ENLIGNE);
        } else if (getMoveList().get(2).toString().equals("E5")) {
            getBoard()[140].addValeur(Mark.RED, VALEUR_ENLIGNE);
            getBoard()[126].addValeur(Mark.RED, VALEUR_ENLIGNE);
            getBoard()[168].addValeur(Mark.RED, VALEUR_ENLIGNE);
        } else if (getMoveList().get(2).toString().equals("K11")) {
            getBoard()[84].addValeur(Mark.RED, VALEUR_ENLIGNE);
            getBoard()[98].addValeur(Mark.RED, VALEUR_ENLIGNE);
            getBoard()[56].addValeur(Mark.RED, VALEUR_ENLIGNE);
        } else if (getMoveList().get(2).toString().equals("K5")) {
            getBoard()[128].addValeur(Mark.RED, VALEUR_ENLIGNE);
            getBoard()[144].addValeur(Mark.RED, VALEUR_ENLIGNE);
            getBoard()[176].addValeur(Mark.RED, VALEUR_ENLIGNE);
        }
    }
    public void updateBoard(ArrayList<Sequence> sequenceArrayList, Mark mark){

        for (Sequence seq : sequenceArrayList){
            String direction = seq.getSequenceDirection();
            int indexCaseAvant = -1;
            int indexCaseApres = -1;

            int indexCaseDeuxAvant = -1;
            int indexCaseDeuxApres = -1;

            int row;
            boolean caseAvantDeborde = false;
            boolean caseApresDeborde = false;
            boolean caseDeuxAvantDeborde = false;
            boolean caseDeuxApresDeborde = false;
            switch (direction){
                case "verticale":
                    indexCaseAvant = seq.getIndexDebut() - 15;
                    indexCaseApres = seq.getIndexFin() + 15;
                    indexCaseDeuxAvant = seq.getIndexDebut() - 30;
                    indexCaseDeuxApres = seq.getIndexFin() + 30;
                    break;
                case "horizontale":
                    row = seq.getIndexDebut() / 15;
                    if ((seq.getIndexDebut() - 1) / 15 == row){
                        indexCaseAvant = seq.getIndexDebut() - 1;       //peut deborder sur autre ligne
                    }else caseAvantDeborde = true;
                    if ((seq.getIndexFin() + 1) / 15 == row){
                        indexCaseApres = seq.getIndexFin() + 1;       //peut deborder sur autre ligne
                    }else caseApresDeborde = true;
                    if ((seq.getIndexDebut() - 2) / 15 == row){
                        indexCaseDeuxAvant = seq.getIndexDebut() - 2;       //peut deborder sur autre ligne
                    }else caseDeuxAvantDeborde = true;
                    if ((seq.getIndexFin() + 2) / 15 == row){
                        indexCaseDeuxApres = seq.getIndexFin() + 2;       //peut deborder sur autre ligne
                    }else caseDeuxApresDeborde = true;
                    break;
                case "diagonale sud-est":
                    indexCaseAvant = seq.getIndexDebut() - 16;
                    indexCaseApres = seq.getIndexFin() + 16;
                    indexCaseDeuxAvant = seq.getIndexDebut() - 32;
                    indexCaseDeuxApres = seq.getIndexFin() + 32;
                    break;
                case "diagonale sud-ouest":
                    indexCaseAvant = seq.getIndexDebut() - 14;
                    indexCaseApres = seq.getIndexFin() + 14;
                    indexCaseDeuxAvant = seq.getIndexDebut() - 28;
                    indexCaseDeuxApres = seq.getIndexFin() + 28;
                    break;
            }
            int length = seq.getLength();
            int points = 0;
            boolean threeInARowWithFourthSpaced = false;
            switch(length){
                case 2:
                    if (caseAvantDeborde || caseApresDeborde){
                        points = TWO_CLOSED;
                    }
                    if (indexCaseAvant >= 0 && indexCaseAvant < 225 && indexCaseApres >= 0 && indexCaseApres < 225){
                        if (getBoard()[indexCaseAvant].getMark() == mark.enemy() && getBoard()[indexCaseApres].getMark() == Mark.EMPTY){
                            getBoard()[indexCaseApres].setValeur(mark.enemy(), CAPTURE_VALUE);
                        } else if (getBoard()[indexCaseAvant].getMark() == Mark.EMPTY && getBoard()[indexCaseApres].getMark() == mark.enemy()) {
                            getBoard()[indexCaseAvant].setValeur(mark.enemy(), CAPTURE_VALUE);
                        }
                        points = TWO;
                    }

                    break;
                case 3:
                    if (caseAvantDeborde || caseApresDeborde){
                        points = THREE_CLOSED;

                        if (indexCaseDeuxAvant >= 0 && indexCaseDeuxAvant < 225 && !caseDeuxAvantDeborde){
                            if (getBoard()[indexCaseDeuxAvant].getMark() == seq.getMark() ){
                                threeInARowWithFourthSpaced = true;
                            }
                        } else if (indexCaseDeuxApres >= 0 && indexCaseDeuxApres < 225 && !caseDeuxApresDeborde) {
                            if (getBoard()[indexCaseDeuxApres].getMark() == seq.getMark()){
                                threeInARowWithFourthSpaced = true;
                            }
                        }
                    }
                    if (indexCaseAvant >= 0 && indexCaseAvant < 225 && indexCaseApres >= 0 && indexCaseApres < 225){
                        if (getBoard()[indexCaseAvant].getMark() == mark.enemy() || getBoard()[indexCaseApres].getMark() == mark.enemy()){
                            points = THREE_CLOSED;
                        }else {     //prendre en compte extremites
                            if (indexCaseDeuxAvant >= 0 && indexCaseDeuxAvant < 225 && !caseDeuxAvantDeborde){
                                if (getBoard()[indexCaseDeuxAvant].getMark() == seq.getMark() ){
                                    threeInARowWithFourthSpaced = true;
                                }
                            } else if (indexCaseDeuxApres >= 0 && indexCaseDeuxApres < 225 && !caseDeuxApresDeborde) {
                                if (getBoard()[indexCaseDeuxApres].getMark() == seq.getMark()){
                                    threeInARowWithFourthSpaced = true;
                                }
                            }

                                points = THREE_OPEN;
                        }
                    }
                    break;
                case 4:
                    if (caseAvantDeborde || caseApresDeborde){
                        points = FOUR_CLOSED;
                    }
                    if (indexCaseAvant >= 0 && indexCaseAvant < 225 && indexCaseApres >= 0 && indexCaseApres < 225){
                        if (getBoard()[indexCaseAvant].getMark() == mark.enemy() || getBoard()[indexCaseApres].getMark() == mark.enemy()){
                            points = FOUR_CLOSED;
                        }else points = FOUR_OPEN;
                    }
                    break;
                case 5:
                    points = FIVE_IN_A_ROW;
            }
            //rendre les deux if en dessous une fonction qui prend la valeur a set, les cases a set et l index(?)
            if (indexCaseAvant >= 0 && indexCaseAvant < 225 && !caseAvantDeborde){
                if (seq.getMark() == mark){
                    getBoard()[indexCaseAvant].setValeur(mark, points);
                }
                else{
                    getBoard()[indexCaseAvant].setValeur(mark.enemy(), points);
                }
            }
            if (indexCaseApres >= 0 && indexCaseApres < 225 && !caseApresDeborde){
                if (seq.getMark() == mark){
                    getBoard()[indexCaseApres].setValeur(mark, points);
                }
                else {
                    getBoard()[indexCaseApres].setValeur(mark.enemy(), points);
                }
            }

            if (threeInARowWithFourthSpaced){   //devrait etre mis dans update board
                if (indexCaseAvant >= 0 && indexCaseAvant < 225 && indexCaseApres >= 0 && indexCaseApres < 225){
                    if (getBoard()[indexCaseDeuxAvant].getMark() == seq.getMark() && !caseDeuxAvantDeborde){
                        getBoard()[indexCaseAvant].setValeur(seq.getMark(), FOUR_OPEN);
                    } else if (getBoard()[indexCaseDeuxApres].getMark() == seq.getMark() && !caseDeuxApresDeborde) {
                        getBoard()[indexCaseApres].setValeur(seq.getMark(), FOUR_OPEN);
                    }
                }
            }
        }
    }

    private ArrayList<Integer> checkCaptureDirection(Mark mark, Move move) {
        ArrayList<Integer> directionsCapture = new ArrayList<>();

        int startingIndex = move.getIndex();

        for (int firstNext : numbersAroundIndex) {
            int secondNext = firstNext * 2;
            int thirdNext = firstNext * 3;
            if (isValidIndex(startingIndex + firstNext) && isValidIndex(startingIndex + secondNext) && isValidIndex(startingIndex + thirdNext)) {
                if (getBoard()[startingIndex + firstNext].getMark() == mark.enemy() && getBoard()[startingIndex + secondNext].getMark() == mark.enemy()) {
                    if (getBoard()[startingIndex + thirdNext].getMark() == mark) {
                        directionsCapture.add(firstNext);
                    }
                }
            }
        }
        return directionsCapture;
    }

    public void removeCapturedPieces(ArrayList<Integer> captureDirections, int startingIndex, Mark mark){
        for (int direction : captureDirections){
            getBoard()[startingIndex + direction].setMark(Mark.EMPTY);
            removeValue(startingIndex + direction, mark.enemy());

            getBoard()[startingIndex + 2 * direction].setMark(Mark.EMPTY);
            removeValue(startingIndex + 2 * direction, mark.enemy());

            if (mark == Mark.RED){
                nbCapturesRouge++;
            }
            else{
                nbCapturesNoir++;
            }
        }
    }


    private boolean isValidIndex(int index){
        return index >= 0 && index < 225;
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
