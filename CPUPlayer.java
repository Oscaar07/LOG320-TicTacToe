import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntBinaryOperator;

class CPUPlayer {
    // Constantes pour la gestion de la profondeur
    private static final int EARLY_GAME_DEPTH = 4;
    private static final int MID_GAME_DEPTH = 3;
    private static final int LATE_GAME_DEPTH = 5;
    private static final int EARLY_GAME_MOVES = 10;
    private static final int LATE_GAME_MOVES = 30;

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

    // Gestion du temps
    private static final long TIME_LIMIT = 4800; // 4.8 secondes
    private long startTime;

    // Cache et profondeur
    private Map<Long, Integer> transpositionTable;
    private int currentMaxDepth;
    private int numExploredNodes;
    private Mark cpuMark;

    public CPUPlayer(Mark mark) {
        this.cpuMark = mark;
        this.transpositionTable = new HashMap<>();
        this.currentMaxDepth = EARLY_GAME_DEPTH;
    }

    public int getNumOfExploredNodes() {
        return numExploredNodes;
    }

    // Méthode principale pour obtenir le prochain coup
    public ArrayList<Move> getNextMoveAB(Board board) {
        startTime = System.currentTimeMillis();
        numExploredNodes = 0;
        transpositionTable.clear();  // Vider le cache pour chaque nouveau coup
        
        adjustDepth(board);
        ArrayList<Move> moves = new ArrayList<>();

        // Cas spéciaux pour les premiers coups
        if (isFirstMove(board)) {
            if (cpuMark == Mark.RED) {
                moves.add(new Move('H', 8));
                return moves;
            }
        }

        if (isSecondRedMove(board)) {
            return generateSecondRedMove(board);
        }

        try {
            moves = findBestMove(board);
        } catch (TimeoutException e) {
            // Si temps dépassé, retourner le meilleur coup trouvé jusqu'ici
            if (moves.isEmpty()) {
                moves = generateSafeMoves(board);
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
            if (isTimeUp()) throw new TimeoutException();

            // Vérifier d'abord les coups gagnants
            if (isWinningMove(board, move)) {
                bestMoves.clear();
                bestMoves.add(move);
                return bestMoves;
            }

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
        numExploredNodes++;
        
        if (isTimeUp()) throw new TimeoutException();

        // Vérifier le cache
        long boardHash = getBoardHash(board);
        if (transpositionTable.containsKey(boardHash) && depth < currentMaxDepth - 1) {
            return transpositionTable.get(boardHash);
        }

        if (depth == 0 || isTerminalNode(board)) {
            int value = evaluatePosition(board);
            transpositionTable.put(boardHash, value);
            return value;
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

        transpositionTable.put(boardHash, value);
        return value;
    }
private long getBoardHash(Board board) {
        // TODO: Implémenter une fonction de hachage efficace pour le plateau
        return board.hashCode();
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
        return moves;
    }

    private int quickEvaluateMove(Move move, Board board) {
        int score = 0;
        
        // Vérifier les victoires immédiates
        if (isWinningMove(board, move)) {
            return Integer.MAX_VALUE;
        }
        
        // Vérifier les captures possibles
        if (canLeadToCapture(board, move, cpuMark)) {
            score += CAPTURE_VALUE;
        }
        
        // Vérifier la proximité du centre
        score += evaluateProximity(move, board);
        
        // Bonus pour le contrôle du centre
        int centerDistance = Math.abs(move.getIngameRow() - 8) + 
                           Math.abs(move.getIngameCol() - 'H');
        score += (15 - centerDistance) * CENTER_VALUE;
        
        return score;
    }

    private boolean isWinningMove(Board board, Move move) {
        Board tempBoard = new Board(board);
        tempBoard.play(move, cpuMark);
        
        return tempBoard.checkFor5inARow(cpuMark) || 
               tempBoard.checkCapture(move, cpuMark);
    }

    private int evaluatePosition(Board board) {
        int score = 0;
        
        // 1. Vérifier les conditions de victoire/défaite
        if (board.checkFor5inARow(cpuMark)) return WIN_SCORE;
        if (board.checkFor5inARow(cpuMark.enemy())) return LOSE_SCORE;
        
        // 2. Évaluer les captures
        score += evaluateCaptures(board);
        
        // 3. Évaluer les séquences
        score += evaluateSequences(board);
        
        // 4. Évaluer le contrôle du territoire
        score += evaluateTerritory(board);
        
        // 5. Évaluer les menaces
        score += evaluateThreats(board);
        
        return score;
    }

    private int evaluateSequences(Board board) {
        int score = 0;
        
        // Directions à vérifier
        int[][] directions = {{1,0}, {0,1}, {1,1}, {1,-1}};
        
        for (int row = 0; row < 15; row++) {
            for (int col = 0; col < 15; col++) {
                for (int[] dir : directions) {
                    score += evaluateSequenceInDirection(board, row, col, dir[0], dir[1]);
                }
            }
        }
        
        return score;
    }

    private int evaluateSequenceInDirection(Board board, int startRow, int startCol, 
                                          int dRow, int dCol) {
        Mark[] sequence = new Mark[5];
        
        // Extraire la séquence
        for (int i = 0; i < 5; i++) {
            int row = startRow + i * dRow;
            int col = startCol + i * dCol;
            if (!isValidPosition(row, col)) return 0;
            sequence[i] = board.getMark(row, col);
        }
        
        return evaluateSequencePattern(sequence);
    }

    private int evaluateSequencePattern(Mark[] sequence) {
        int score = 0;
        int cpuCount = 0;
        int enemyCount = 0;
        int emptyCount = 0;
        
        for (Mark mark : sequence) {
            if (mark == cpuMark) cpuCount++;
            else if (mark == cpuMark.enemy()) enemyCount++;
            else emptyCount++;
        }
        
        // Évaluer les séquences de l'IA
        if (enemyCount == 0) {
            switch (cpuCount) {
                case 5: return FIVE_IN_A_ROW;
                case 4: return emptyCount == 1 ? FOUR_OPEN : FOUR_CLOSED;
                case 3: return emptyCount == 2 ? THREE_OPEN : THREE_CLOSED;
                case 2: return emptyCount == 3 ? TWO_OPEN : 0;
            }
        }
        // Évaluer les séquences de l'adversaire
        else if (cpuCount == 0) {
            switch (enemyCount) {
                case 5: return -FIVE_IN_A_ROW;
                case 4: return -(emptyCount == 1 ? FOUR_OPEN : FOUR_CLOSED);
                case 3: return -(emptyCount == 2 ? THREE_OPEN : THREE_CLOSED);
                case 2: return -(emptyCount == 3 ? TWO_OPEN : 0);
            }
        }
        
        return score;
    }

    private int evaluateTerritory(Board board) {
        int score = 0;
        int centerX = 7, centerY = 7;
        
        for (int row = 0; row < 15; row++) {
            for (int col = 0; col < 15; col++) {
                Mark mark = board.getMark(row, col);
                if (mark != Mark.EMPTY) {
                    int distance = Math.max(Math.abs(row - centerX), 
                                         Math.abs(col - centerY));
                    int value = Math.max(0, 8 - distance) * CENTER_VALUE;
                    score += mark == cpuMark ? value : -value;
                }
            }
        }
        
        return score;
    }

    private int evaluateThreats(Board board) {
        int score = 0;
        List<Move> moves = board.getPossibleMoves();
        
        for (Move move : moves) {
            if (canLeadToCapture(board, move, cpuMark)) {
                score += 500;
                if (canLeadToMultipleCaptures(board, move, cpuMark)) {
                    score += 1000;
                }
            }
            if (canLeadToCapture(board, move, cpuMark.enemy())) {
                score -= 400;
            }
        }
        
        return score;
    }

    private int evaluateProximity(Move move, Board board) {
        int score = 0;
        int row = move.getIngameRow() - 1;
        int col = move.getIngameCol() - 'A';
        
        // Vérifier les 8 directions
        int[][] directions = {
            {-1,-1}, {-1,0}, {-1,1},
            {0,-1},          {0,1},
            {1,-1},  {1,0},  {1,1}
        };
        
        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            
            if (isValidPosition(newRow, newCol)) {
                Mark mark = board.getMark(newRow, newCol);
                if (mark == cpuMark) {
                    score += 10;
                } else if (mark == cpuMark.enemy()) {
                    score += 5;
                }
            }
        }
        
        return score;
    }

    // Méthodes utilitaires
    private boolean isTimeUp() {
        return System.currentTimeMillis() - startTime > TIME_LIMIT;
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < 15 && col >= 0 && col < 15;
    }

    private boolean isFirstMove(Board board) {
        return board.getMoveCount() == 0;
    }

    private boolean isSecondRedMove(Board board) {
        return board.getMoveCount() == 2 && cpuMark == Mark.RED;
    }

    private ArrayList<Move> generateSafeMoves(Board board) {
        ArrayList<Move> moves = new ArrayList<>();
        // Générer quelques coups sûrs si on manque de temps
        Move move = findSafeMove(board);
        if (move != null) {
            moves.add(move);
        }
        return moves;
    }

    private Move findSafeMove(Board board) {
        // Trouver un coup sûr près du centre
        int[] preferredRows = {7, 8, 6, 9, 5, 10};
        char[] preferredCols = {'H', 'G', 'I', 'F', 'J', 'E', 'K'};
        
        for (int row : preferredRows) {
            for (char col : preferredCols) {
                Move move = new Move(col, row);
                if (board.getMark(row-1, col-'A') == Mark.EMPTY) {
                    return move;
                }
            }
        }
        return null;
    }

    private boolean checkCaptureDirection(Board board, int row, int col, int dRow, int dCol, Mark mark) {
        try {
            // Vérifier si les positions sont valides
            for (int i = 1; i <= 3; i++) {
                int newRow = row + (dRow * i);
                int newCol = col + (dCol * i);
                if (!isValidPosition(newRow, newCol)) {
                    return false;
                }
            }

            // Vérifier le pattern de capture
            Mark enemy = mark.enemy();
            Mark pos1 = board.getMark(row + dRow, col + dCol);
            Mark pos2 = board.getMark(row + (2 * dRow), col + (2 * dCol));
            Mark pos3 = board.getMark(row + (3 * dRow), col + (3 * dCol));

            return pos1 == enemy && pos2 == enemy && pos3 == mark;
        } catch (Exception e) {
            return false;
        }
    }

    public ArrayList<Move> generateSecondRedMove(Board board) {
        ArrayList<Move> validMoves = new ArrayList<>();
        int centerValue = Integer.MIN_VALUE;
        Move bestMove = null;

        // Parcourir tout le plateau
        for (char col = 'A'; col <= 'O'; col++) {
            for (int row = 1; row <= 15; row++) {
                // Calculer la distance Manhattan du centre
                int distanceFromCenter = Math.abs(row - 8) + 
                                       Math.abs(col - 'H');
                
                // Le coup doit être à au moins 3 cases du centre
                if (distanceFromCenter >= 3) {
                    Move move = new Move(col, row);
                    if (isValidMove(board, move)) {
                        // Évaluer la position
                        Board tempBoard = new Board(board);
                        tempBoard.play(move, cpuMark);
                        int value = evaluatePosition(tempBoard);
                        
                        if (value > centerValue) {
                            centerValue = value;
                            bestMove = move;
                        }
                        validMoves.add(move);
                    }
                }
            }
        }

        // Retourner le meilleur coup trouvé
        ArrayList<Move> result = new ArrayList<>();
        if (bestMove != null) {
            result.add(bestMove);
        }
        return result;
    }



    // Méthode pour vérifier si une séquence peut mener à une capture
    private boolean canFormCaptureThreat(Board board, int row, int col, Mark mark) {
        // Vérifier les séquences qui pourraient devenir des captures
        Mark enemy = mark.enemy();
        int[][] directions = {{1,0}, {0,1}, {1,1}, {1,-1}};
        
        for (int[] dir : directions) {
            // Pattern: [vide][ennemi][ennemi][nous]
            if (checkSequence(board, row, col, dir[0], dir[1], 
                new Mark[]{Mark.EMPTY, enemy, enemy, mark})) {
                return true;
            }
            // Pattern: [nous][ennemi][ennemi][vide]
            if (checkSequence(board, row, col, dir[0], dir[1], 
                new Mark[]{mark, enemy, enemy, Mark.EMPTY})) {
                return true;
            }
        }
        
        return false;
    }

    private boolean checkSequence(Board board, int startRow, int startCol, 
                                int dRow, int dCol, Mark[] sequence) {
        for (int i = 0; i < sequence.length; i++) {
            int row = startRow + (i * dRow);
            int col = startCol + (i * dCol);
            
            if (!isValidPosition(row, col)) return false;
            
            Mark currentMark = board.getMark(row, col);
            if (currentMark != sequence[i]) return false;
        }
        return true;
    }

    private boolean isValidMove(Board board, Move move) {
        try {
            Board tempBoard = new Board(board);
            tempBoard.play(move, cpuMark);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private int evaluateCaptures(Board board) {
        int score = 0;
        
        // Compter les captures pour les deux joueurs
        int cpuCaptures = countCaptures(board, cpuMark);
        int enemyCaptures = countCaptures(board, cpuMark.enemy());
        
        score += cpuCaptures * CAPTURE_VALUE;
        score -= enemyCaptures * CAPTURE_VALUE;
        
        // Bonus pour être proche de la victoire par captures
        if (cpuCaptures >= 3) score += CAPTURE_VALUE * 2;
        if (enemyCaptures >= 3) score -= CAPTURE_VALUE * 2;
        
        return score;
    }

    private boolean canLeadToCapture(Board board, Move move, Mark mark) {
        // Simuler le coup
        Board tempBoard = new Board(board);
        tempBoard.play(move, mark);
        
        int row = move.getIngameRow() - 1;
        int col = move.getIngameCol() - 'A';
        Mark enemy = mark.enemy();

        // Toutes les directions possibles de capture
        int[][] directions = {
            {1, 0},   // vertical
            {0, 1},   // horizontal
            {1, 1},   // diagonale \
            {1, -1}   // diagonale /
        };

        for (int[] dir : directions) {
            // Vérifier dans les deux sens de la direction
            if (checkCapturePattern(tempBoard, row, col, dir[0], dir[1], mark, enemy) ||
                checkCapturePattern(tempBoard, row, col, -dir[0], -dir[1], mark, enemy)) {
                return true;
            }
        }
        
        return false;
    }

    private boolean checkCapturePattern(Board board, int row, int col, 
                                      int dRow, int dCol, Mark mark, Mark enemy) {
        // Vérifier les patterns de capture possibles

        // Pattern 1: [notre_coup][ennemi][ennemi][nous]
        if (checkPattern(board, row, col, dRow, dCol, new Mark[]{mark, enemy, enemy, mark})) {
            return true;
        }

        // Pattern 2: [nous][ennemi][ennemi][notre_coup]
        if (checkPattern(board, row, col, dRow, dCol, new Mark[]{mark, enemy, enemy, mark})) {
            return true;
        }

        // Pattern 3: [ennemi][notre_coup][ennemi][nous]
        if (checkPattern(board, row, col, dRow, dCol, new Mark[]{enemy, mark, enemy, mark})) {
            return true;
        }

        return false;
    }

    private boolean checkPattern(Board board, int startRow, int startCol, 
                               int dRow, int dCol, Mark[] pattern) {
        // Vérifier chaque position du pattern
        for (int i = 0; i < pattern.length; i++) {
            int row = startRow + (i * dRow);
            int col = startCol + (i * dCol);
            
            // Vérifier si la position est valide
            if (!isValidPosition(row, col)) {
                return false;
            }
            
            // Vérifier si la marque correspond au pattern
            Mark currentMark = board.getMark(row, col);
            if (currentMark != pattern[i]) {
                return false;
            }
        }
        
        return true;
    }

    // Méthode pour vérifier les captures multiples possibles
    private boolean canLeadToMultipleCaptures(Board board, Move move, Mark mark) {
        Board tempBoard = new Board(board);
        tempBoard.play(move, mark);
        
        int captureCount = 0;
        int row = move.getIngameRow() - 1;
        int col = move.getIngameCol() - 'A';
        Mark enemy = mark.enemy();

        // Vérifier toutes les directions
        int[][] directions = {
            {1, 0}, {0, 1}, {1, 1}, {1, -1},
            {-1, 0}, {0, -1}, {-1, -1}, {-1, 1}
        };

        for (int[] dir : directions) {
            if (checkCapturePattern(tempBoard, row, col, dir[0], dir[1], mark, enemy)) {
                captureCount++;
            }
        }

        return captureCount >= 2;  // True si au moins 2 captures sont possibles
    }

    private int countCaptures(Board board, Mark mark) {
        int captures = 0;
        for (Move move : board.getPossibleMoves()) {
            Board tempBoard = new Board(board);
            tempBoard.play(move, mark);
            if (tempBoard.checkCapture(move, mark)) {
                captures++;
            }
        }
        return captures;
    }

    private boolean isTerminalNode(Board board) {
        return board.checkFor5inARow(cpuMark) || 
               board.checkFor5inARow(cpuMark.enemy()) ||
               board.getPossibleMoves().isEmpty() ||
               countCaptures(board, cpuMark) >= 5 ||
               countCaptures(board, cpuMark.enemy()) >= 5;
    }

    // Exception personnalisée pour la gestion du timeout
    private static class TimeoutException extends Exception {
        public TimeoutException() {
            super("Time limit exceeded");
        }
    }
}