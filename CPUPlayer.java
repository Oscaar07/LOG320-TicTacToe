import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CPUPlayer {
    // Constantes pour la gestion de la profondeur
    private static final int EARLY_GAME_DEPTH = 4;
    private static final int MID_GAME_DEPTH = 6;
    private static final int LATE_GAME_DEPTH = 8;
    private static final int EARLY_GAME_MOVES = 60;
    private static final int LATE_GAME_MOVES = 80;



    // Gestion du temps
    private static final long TIME_LIMIT = 4800; // 4.8 secondes
    private long startTime;

    // Cache et profondeur
    private int currentMaxDepth;
    private Mark cpuMark;

    private static final int NBLIGNES = 15;
    private static final int NBCOLONNES = 15;



    public CPUPlayer(Mark mark) {
        this.cpuMark = mark;
        this.currentMaxDepth = EARLY_GAME_DEPTH;
    }


    // Méthode principale pour obtenir le prochain coup
    public ArrayList<Move> getNextMoveAB(Game board) {

        adjustDepth(board);
        ArrayList<Move> moves = new ArrayList<>();

        try {
            moves = findBestMove(board);
        } catch (TimeoutException e) {
            // Si temps dépassé, retourner le meilleur coup trouvé jusqu'ici
            System.out.print("aucun move trouve");
        }

        return moves;
    }

    private ArrayList<Move> findBestMove(Game board) throws TimeoutException {
        ArrayList<Move> bestMoves = new ArrayList<>();
        int bestValue;
        if (cpuMark == Mark.RED){
            bestValue = Integer.MIN_VALUE;
        } else bestValue = Integer.MAX_VALUE;

        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        List<Move> possibleMoves = orderMoves(board.getPossibleMoves(), board);

        for (Move move : possibleMoves) {

            Game nextBoard = new Game(board);
            nextBoard.play(move, cpuMark);

            int value;

            if (cpuMark == Mark.RED){
                value = alphaBeta(nextBoard, currentMaxDepth - 1, alpha, beta, true);
            }else{
                value = alphaBeta(nextBoard, currentMaxDepth - 1, alpha, beta, false);
            }

            if (cpuMark == Mark.RED){
                if (value > bestValue) {
                    bestValue = value;
                    bestMoves.clear();
                    bestMoves.add(move);
                } else if (value == bestValue) {
                    bestMoves.add(move);
                }

                alpha = Math.max(alpha, value);
            }else{
                if (value < bestValue) {
                    bestValue = value;
                    bestMoves.clear();
                    bestMoves.add(move);
                } else if (value == bestValue) {
                    bestMoves.add(move);
                }

                beta = Math.min(alpha, value);
            }


        }

        return bestMoves;
    }

    private int alphaBeta(Game game, int depth, int alpha, int beta, boolean isAiPlayingRed)
            throws TimeoutException {


        if (game.checkFor5inARow(Mark.RED) || game.getNbCapturesRouge() == 5){
            return 10000000;
        }
        else if (game.checkFor5inARow(Mark.BLACK) || game.getNbCapturesNoir() == 5){
            return -10000000;
        }

        if (depth == 0) {
            int bestBlackMove = -10000;
            int bestRedMove = -10000;
            for (int i = 0; i < NBLIGNES; i++){
                for (int j = 0; j < NBCOLONNES; j++){
                    if (game.getBoard()[i][j].getBlackThreatValue() > bestBlackMove && game.getBoard()[i][j].getMark() == Mark.EMPTY){
                        bestBlackMove = game.getBoard()[i][j].getBlackThreatValue();
                    }
                    if (game.getBoard()[i][j].getRedThreatValue() > bestRedMove && game.getBoard()[i][j].getMark() == Mark.EMPTY){
                        bestRedMove = game.getBoard()[i][j].getRedThreatValue();
                    }
                }
            }

            int moveEval;
            int captureEval;

            moveEval = (bestRedMove - bestBlackMove);
            captureEval = (game.getEvalCapturesRouge() - game.getEvalCapturesNoir());

            return moveEval + captureEval;
        }

        List<Move> moves = orderMoves(game.getPossibleMoves(), game);

        boolean notreTour = false;
        if (depth % 2 == 1){
            notreTour = true;
        }

        boolean isMaximizing = notreTour == isAiPlayingRed;

        int value = isMaximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;



        for (Move move : moves) {
            Game nextBoard = new Game(game);
            nextBoard.play(move, notreTour ? cpuMark : cpuMark.enemy());
            
            int evalValue = alphaBeta(nextBoard, depth - 1, alpha, beta, isAiPlayingRed);
            
            if (isMaximizing) {
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


    private void adjustDepth(Game board) {
        int moveCount = board.getMoveCount();
        if (moveCount < EARLY_GAME_MOVES) {
            currentMaxDepth = EARLY_GAME_DEPTH;
        } else if (moveCount < LATE_GAME_MOVES) {
            currentMaxDepth = MID_GAME_DEPTH;
        } else {
            currentMaxDepth = LATE_GAME_DEPTH;
        }
    }

    private List<Move> orderMoves(List<Move> moves, Game game) {
        // Utiliser une map pour stocker les scores
        Map<Move, Integer> moveScores = new HashMap<>();
        
        for (Move move : moves) {
            int score = quickEvaluateMove(move, game);
            moveScores.put(move, score);
        }
        
        // Trier les coups selon leurs scores
        moves.sort((m1, m2) -> moveScores.get(m2).compareTo(moveScores.get(m1)));
        moves.removeIf(move -> moveScores.get(move) == 0 && !game.getBoard()[move.getGridRow()][move.getGridCol()].isActiveSquare());
        return moves;
    }

    private int quickEvaluateMove(Move move, Game game) {
        int score = 0;
        

        score += game.getBoard()[move.getGridRow()][move.getGridCol()].getValue(cpuMark);
        score += game.getBoard()[move.getGridRow()][move.getGridCol()].getValue(cpuMark.enemy());
        
        return score;
    }


    // Méthodes utilitaires

    private boolean isTerminalNode(Game game) {
        return game.checkFor5inARow(cpuMark) ||
               game.checkFor5inARow(cpuMark.enemy()) ||
               game.getPossibleMoves().isEmpty();
               //captures sont dans une variable
    }

    // Exception personnalisée pour la gestion du timeout
    private static class TimeoutException extends Exception {
        public TimeoutException() {
            super("Time limit exceeded");
        }
    }







































}