import java.util.ArrayList;
import java.util.function.IntBinaryOperator;

// IMPORTANT: Il ne faut pas changer la signature des méthodes
// de cette classe, ni le nom de la classe.
// Vous pouvez par contre ajouter d'autres méthodes (ça devrait 
// être le cas)
class CPUPlayer {
    // tic tac toe est simple donc on peut tout évaluer.
    private final int MAX_DEPTH = 9;

    // Contient le nombre de noeuds visités (le nombre
    // d'appel à la fonction MinMax ou Alpha Beta)
    // Normalement, la variable devrait être incrémentée
    // au début de votre MinMax ou Alpha Beta.
    private int numExploredNodes;

    private Mark cpuMark;

    private int nombreCaptures = 0;
    private int nombreCapturesEnemy = 0;

    // Le constructeur reçoit en paramètre le
    // joueur MAX (X ou O)
    public CPUPlayer(Mark cpu) {
        cpuMark = cpu;
    }

    // Ne pas changer cette méthode
    public int getNumOfExploredNodes() {
        return numExploredNodes;
    }

    // Retourne la liste des coups possibles. Cette liste contient
    // plusieurs coups possibles si et seuleument si plusieurs coups
    // ont le même score.
    public ArrayList<Move> getNextMoveMinMax(Board board) {
        numExploredNodes = 0;
        ArrayList<Move> moves = new ArrayList<>();
        int value = Integer.MIN_VALUE;

        for (Move m : board.getPossibleMoves()) {
            //numExploredNodes++;     /// erreur ??   pas ++ si depth max ??
            Board simulatedBoard = board.clone();
            simulatedBoard.play(m, cpuMark);

            int childValue = minMax(simulatedBoard, 1, false);
            if (childValue == value) {
                moves.add(m);   // Ajouter un coup si la valeur est la meme que le meilleur coup actuel
            } 

            else if (childValue > value) {
                value = childValue;
                moves.clear();
                moves.add(m);   // Replacer avec le nouveau meilleur coup
            }
        }
        return moves;
    }

    // Retourne la liste des coups possibles. Cette liste contient
    // plusieurs coups possibles si et seuleument si plusieurs coups
    // ont le même score.
    public ArrayList<Move> getNextMoveAB(Board board) {
        numExploredNodes = 0;
    
        ArrayList<Move> moves = new ArrayList<>();
        int value = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
    
        for (Move m : board.getPossibleMoves()) {
            //numExploredNodes++;
            Board simulatedBoard = board.clone();
            simulatedBoard.play(m, cpuMark);

            int childValue = alphaBeta(simulatedBoard, 1, alpha, beta, false);
            if (childValue == value) {
                moves.add(m);
            } else if (childValue > value) {
                value = childValue;
                moves.clear();
                moves.add(m);
            }
            // update alpha avec la meilleure valeur trouvée jusqu'à présent
            alpha = Math.max(alpha, value);
        }   
        return moves;
    }

    private int minMax(Board node, int depth, boolean max) {
        numExploredNodes++;

        PlayStatus state = node.getPlayStatus();
        if (depth == MAX_DEPTH || state != PlayStatus.Unfinished) { ///problem ?
            if (state == PlayStatus.Tie) {
                return 0;
            } else {
                return state.isVictory(cpuMark) ? 100 : -100;
            }
        }

        Mark currentMark = max ? cpuMark : cpuMark.enemy();
        IntBinaryOperator comparator = max ? Math::max : Math::min;
        int value = max ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        // Explorer toutes les possibilitées 
        for (Move move : node.getPossibleMoves()) {
            Board childBoard = node.clone();
            childBoard.play(move, currentMark);
            int childValue = minMax(childBoard, depth + 1, !max);
            value = comparator.applyAsInt(value, childValue);
        }
        return value;
    }


    private int alphaBeta(Board node, int depth, int alpha, int beta, boolean noeudMax) {
        numExploredNodes++;
    
        PlayStatus state = node.getPlayStatus();
        if (depth == MAX_DEPTH || state != PlayStatus.Unfinished) {


            if (state == PlayStatus.Tie) {
                return 0;
            } else {
                return state.isVictory(cpuMark) ? 100 : -100;
            }
        }

        Mark currentMark;
        if (noeudMax){
            currentMark = cpuMark;
        }
        else{
            currentMark = cpuMark.enemy();
        }

        int value;
        if (noeudMax){
            value = Integer.MIN_VALUE;
        }
        else{
            value = Integer.MAX_VALUE;
        }

        // Explorer toutes les possibilitées 
        for (Move move : node.getPossibleMoves()) {
            Board childBoard = node.clone();
            childBoard.play(move, currentMark);


            if (noeudMax) {

                value = Math.max(value, alphaBeta(childBoard, depth + 1, alpha, beta, false));
                alpha = Math.max(alpha, value);
                if (alpha >= beta){
                    break;  // Stop si la valeur est supérieure ou égale à beta
                }
            } else {

                value = Math.min(value, alphaBeta(childBoard, depth + 1, alpha, beta, true));
                beta = Math.min(beta, value);
                if (alpha >= beta){
                    break;  // Stop si la valeur est inférieure ou égale à alpha
                }
            }
        }
        return value;
    }


}