import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntBinaryOperator;

class CPUPlayer {
    // Constantes pour la gestion de la profondeur
    private static final int EARLY_GAME_DEPTH = 2;
    private static final int MID_GAME_DEPTH = 3;
    private static final int LATE_GAME_DEPTH = 5;
    private static final int EARLY_GAME_MOVES = 70;
    private static final int LATE_GAME_MOVES = 80;

    // Constantes pour l'évaluation
    private static final int WIN_SCORE = 1000000;
    private static final int LOSE_SCORE = -1000000;
    private static final int FIVE_IN_A_ROW = 100000;
    private static final int FOUR_OPEN = 10000;
    private static final int FOUR_CLOSED = 1000;
    private static final int THREE_OPEN = 500;
    private static final int THREE_CLOSED = 100;
    private static final int TWO_OPEN = 50;
    private static final int CAPTURE_VALUE = 2000;
    private static final int CENTER_VALUE = 30;

    private int nbCapturesRouge = 0;
    private int nbCapturesNoir = 0;

    // Gestion du temps
    private static final long TIME_LIMIT = 4800; // 4.8 secondes
    private long startTime;

    // Cache et profondeur
    private int currentMaxDepth;
    private Mark cpuMark;

    public CPUPlayer(Mark mark) {
        this.cpuMark = mark;
        this.currentMaxDepth = EARLY_GAME_DEPTH;
    }


    // Méthode principale pour obtenir le prochain coup
    public ArrayList<Move> getNextMoveAB(Board board) {

        adjustDepth(board);
        ArrayList<Move> moves = new ArrayList<>();

        try {
            moves = findBestMove(board);
        } catch (TimeoutException e) {
            // Si temps dépassé, retourner le meilleur coup trouvé jusqu'ici
            if (moves.isEmpty()) {
                System.out.print("aucun move trouve");
            }
        }

        return moves;
    }

    private ArrayList<Move> findBestMove(Board board) throws TimeoutException {
        ArrayList<Move> bestMoves = new ArrayList<>();
        int bestValue = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        List<Move> possibleMoves = orderMoves(board.getPossibleMoves(), board);

        for (Move move : possibleMoves) {

            Board nextBoard = new Board(board);
            nextBoard.play(move, cpuMark);
            
            int value = alphaBeta(nextBoard, currentMaxDepth - 1, alpha, beta, false);
            
            if (value > bestValue) {
                bestValue = value;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (value == bestValue) {
                bestMoves.add(move);
            }
            
            alpha = Math.max(alpha, value);
        }

        return bestMoves;
    }

    private int alphaBeta(Board board, int depth, int alpha, int beta, boolean maximizing) 
            throws TimeoutException {


        if (depth == 0 || isTerminalNode(board)) {
            int bestBlackMove = -10000;
            int bestRedMove = -10000;
            for (int i = 0; i < 225; i++){
                if (board.getBoard()[i].getBlackThreatValue() > bestBlackMove && board.getBoard()[i].getMark() == Mark.EMPTY){
                    bestBlackMove = board.getBoard()[i].getBlackThreatValue();
                }
                if (board.getBoard()[i].getRedThreatValue() > bestRedMove && board.getBoard()[i].getMark() == Mark.EMPTY){
                    bestRedMove = board.getBoard()[i].getRedThreatValue();
                }
            }
            if (cpuMark == Mark.RED){
                return bestRedMove - bestBlackMove;
            }
            else{
                return bestBlackMove - bestRedMove;
            }
        }

        List<Move> moves = orderMoves(board.getPossibleMoves(), board);
        int value = maximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (Move move : moves) {
            Board nextBoard = new Board(board);
            nextBoard.play(move, maximizing ? cpuMark : cpuMark.enemy());
            
            int evalValue = alphaBeta(nextBoard, depth - 1, alpha, beta, !maximizing);
            
            if (maximizing) {
                value = Math.max(value, evalValue);
                alpha = Math.max(alpha, value);
            } else {
                value = Math.min(value, evalValue);
                beta = Math.min(beta, value);
            }
            
            if (beta <= alpha) break;
        }

        return value;
    }


    private void adjustDepth(Board board) {
        int moveCount = board.getMoveCount();
        if (moveCount < EARLY_GAME_MOVES) {
            currentMaxDepth = EARLY_GAME_DEPTH;
        } else if (moveCount < LATE_GAME_MOVES) {
            currentMaxDepth = MID_GAME_DEPTH;
        } else {
            currentMaxDepth = LATE_GAME_DEPTH;
        }
    }

    private List<Move> orderMoves(List<Move> moves, Board board) {
        // Utiliser une map pour stocker les scores
        Map<Move, Integer> moveScores = new HashMap<>();
        
        for (Move move : moves) {
            int score = quickEvaluateMove(move, board);
            moveScores.put(move, score);
        }
        
        // Trier les coups selon leurs scores
        moves.sort((m1, m2) -> moveScores.get(m2).compareTo(moveScores.get(m1)));
        moves.removeIf(move -> moveScores.get(move) == 0 && !board.getBoard()[move.getIndex()].isActiveSquare());
        return moves;
    }

    private int quickEvaluateMove(Move move, Board board) {
        int score = 0;
        

        score += board.getBoard()[move.getIndex()].getValue(cpuMark);
        score += board.getBoard()[move.getIndex()].getValue(cpuMark.enemy());
        
        return score;
    }


    // Méthodes utilitaires

    private boolean isTerminalNode(Board board) {
        return board.checkFor5inARow(cpuMark) || 
               board.checkFor5inARow(cpuMark.enemy()) ||
               board.getPossibleMoves().isEmpty();
               //captures sont dans une variable
    }

    // Exception personnalisée pour la gestion du timeout
    private static class TimeoutException extends Exception {
        public TimeoutException() {
            super("Time limit exceeded");
        }
    }







































}