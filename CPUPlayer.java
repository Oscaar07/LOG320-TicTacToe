import java.util.ArrayList;
import java.util.List;
import java.util.function.IntBinaryOperator;

class CPUPlayer {
    private final int MAX_DEPTH = 4;
    private int numExploredNodes;
    private Mark cpuMark;
    private int nombreCaptures = 0;
    private int nombreCapturesEnemy = 0;

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

    public CPUPlayer(Mark cpu) {
        cpuMark = cpu;
    }

    public int getNumOfExploredNodes() {
        return numExploredNodes;
    }

    public ArrayList<Move> getNextMoveMinMax(Board board) {
        numExploredNodes = 0;
        ArrayList<Move> bestMoves = new ArrayList<>();
        int bestValue = Integer.MIN_VALUE;

        // Gestion des cas spéciaux (premier et deuxième coup)
        if (isFirstMove(board) && cpuMark == Mark.RED) {
            bestMoves.add(new Move('H', 8));  // Centre du plateau
            return bestMoves;
        }

        // Pour chaque coup possible
        for (Move move : board.getPossibleMoves()) {
            Board simulatedBoard = new Board(board);
            simulatedBoard.play(move, cpuMark);
            
            // Vérifier si ce coup gagne immédiatement
            if (simulatedBoard.checkFor5inARow(cpuMark) || 
                simulatedBoard.checkCapture(move, cpuMark)) {
                bestMoves.clear();
                bestMoves.add(move);
                return bestMoves;
            }

            int moveValue = minMax(simulatedBoard, MAX_DEPTH - 1, false);
            
            if (moveValue > bestValue) {
                bestValue = moveValue;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (moveValue == bestValue) {
                bestMoves.add(move);
            }
        }
        return bestMoves;
    }

    private int minMax(Board board, int depth, boolean isMaximizing) {
        numExploredNodes++;

        if (depth == 0 || isTerminalNode(board)) {
            return evaluatePosition(board);
        }

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : board.getPossibleMoves()) {
                Board childBoard = new Board(board);
                childBoard.play(move, cpuMark);
                int eval = minMax(childBoard, depth - 1, false);
                maxEval = Math.max(maxEval, eval);
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move move : board.getPossibleMoves()) {
                Board childBoard = new Board(board);
                childBoard.play(move, cpuMark.enemy());
                int eval = minMax(childBoard, depth - 1, true);
                minEval = Math.min(minEval, eval);
            }
            return minEval;
        }
    }

    public ArrayList<Move> getNextMoveAB(Board board) {
        numExploredNodes = 0;
        ArrayList<Move> moves = new ArrayList<>();

        // Cas spéciaux pour les premiers coups
        if (board.getMoveCount() == 0 && cpuMark == Mark.RED) {
            moves.add(new Move('H', 8));
            return moves;
        }
        if (board.getMoveCount() == 2 && cpuMark == Mark.RED) {
            return generateSecondRedMove(board);
        }

        // Stratégie normale pour le reste de la partie
        int bestValue = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        // Obtenir et trier les coups possibles
        List<Move> possibleMoves = orderMoves(board.getPossibleMoves(), board);

        for (Move move : possibleMoves) {
            // Vérifier d'abord si on peut gagner immédiatement
            if (isWinningMove(board, move, cpuMark)) {
                moves.clear();
                moves.add(move);
                return moves;
            }

            // Vérifier si on doit bloquer une victoire adverse
            if (isWinningMove(board, move, cpuMark.enemy())) {
                moves.clear();
                moves.add(move);
                return moves;
            }

            Board simulatedBoard = new Board(board);
            simulatedBoard.play(move, cpuMark);
            
            int value = alphaBeta(simulatedBoard, MAX_DEPTH - 1, alpha, beta, false);
            
            if (value > bestValue) {
                bestValue = value;
                moves.clear();
                moves.add(move);
            } else if (value == bestValue) {
                moves.add(move);
            }
            alpha = Math.max(alpha, value);
        }

        return moves;
    }

    private boolean isWinningMove(Board board, Move move, Mark mark) {
        Board tempBoard = new Board(board);
        tempBoard.play(move, mark);
        
        // Vérifier victoire par 5 alignés
        if (tempBoard.checkFor5inARow(mark)) {
            return true;
        }
        
        // Vérifier victoire par capture
        return tempBoard.checkForFiveCaptures(mark);
    }

    private int alphaBeta(Board board, int depth, int alpha, int beta, boolean isMaximizing) {
        numExploredNodes++;

        if (depth == 0 || isTerminalNode(board)) {
            return evaluatePosition(board);
        }

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            List<Move> orderedMoves = orderMoves(board.getPossibleMoves(), board);
            
            for (Move move : orderedMoves) {
                Board childBoard = new Board(board);
                childBoard.play(move, cpuMark);
                int eval = alphaBeta(childBoard, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha)
                    break; // Élagage Alpha
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            List<Move> orderedMoves = orderMoves(board.getPossibleMoves(), board);
            
            for (Move move : orderedMoves) {
                Board childBoard = new Board(board);
                childBoard.play(move, cpuMark.enemy());
                int eval = alphaBeta(childBoard, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha)
                    break; // Élagage Beta
            }
            return minEval;
        }
    }

    private List<Move> orderMoves(List<Move> moves, Board board) {
        moves.sort((m1, m2) -> {
            int score1 = quickEvaluateMove(m1, board);
            int score2 = quickEvaluateMove(m2, board);
            return Integer.compare(score2, score1);
        });
        return moves;
    }

    private int quickEvaluateMove(Move move, Board board) {
        int score = 0;
        
        if (board.checkCapture(move, cpuMark)) {
            score += CAPTURE_VALUE;
        }
        
        int centerDistance = Math.abs(move.getIngameRow() - 8) + Math.abs(move.getIngameCol() - 'H');
        score += (15 - centerDistance) * 10;
        
        score += evaluateProximity(move, board) * 5;
        
        return score;
    }

    private int evaluateProximity(Move move, Board board) {
        int proximity = 0;
        int row = move.getIngameRow();
        char col = move.getIngameCol();
        
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                
                int newRow = row + i;
                char newCol = (char)(col + j);
                
                if (newRow >= 1 && newRow <= 15 && newCol >= 'A' && newCol <= 'O') {
                    Mark mark = board.getMark(newRow - 1, newCol - 'A');
                    if (mark != Mark.EMPTY) {
                        proximity++;
                    }
                }
            }
        }
        return proximity;
    }

    private int evaluatePosition(Board board) {
        int score = 0;
        
        // 1. Vérification des conditions de victoire/défaite
        if (isWinningState(board, cpuMark)) return WIN_SCORE;
        if (isWinningState(board, cpuMark.enemy())) return LOSE_SCORE;
        
        // 2. Évaluation des captures
        score += evaluateCaptures(board);
        
        // 3. Évaluation des alignements
        score += evaluateSequences(board);
        
        // 4. Évaluation du contrôle du centre
        score += evaluateCenterControl(board);
        
        // 5. Évaluation des menaces
        score += evaluateThreats(board, cpuMark);
        
        return score;
    }

    private int evaluateCaptures(Board board) {
        int score = 0;
        int cpuCaptures = countCaptures(board, cpuMark);
        int enemyCaptures = countCaptures(board, cpuMark.enemy());
        
        score += cpuCaptures * CAPTURE_VALUE;
        score -= enemyCaptures * CAPTURE_VALUE;
        
        if (cpuCaptures >= 3) score += CAPTURE_VALUE * 2;
        if (enemyCaptures >= 3) score -= CAPTURE_VALUE * 2;
        
        return score;
    }

    private int evaluateSequences(Board board) {
        int score = 0;
        
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                score += evaluateDirection(board, i, j, 1, 0);  // horizontal
                score += evaluateDirection(board, i, j, 0, 1);  // vertical
                score += evaluateDirection(board, i, j, 1, 1);  // diagonal \
                score += evaluateDirection(board, i, j, 1, -1); // diagonal /
            }
        }
        
        return score;
    }
    
    private int evaluateDirection(Board board, int startRow, int startCol, int dRow, int dCol) {
        Mark[] sequence = new Mark[5];
        int score = 0;
        
        for (int i = 0; i < 5; i++) {
            int row = startRow + i * dRow;
            int col = startCol + i * dCol;
            if (!isValidPosition(row, col)) return 0;
            sequence[i] = board.getMark(row, col);
        }
        
        int cpu = 0, enemy = 0, empty = 0;
        for (Mark mark : sequence) {
            if (mark == cpuMark) cpu++;
            else if (mark == cpuMark.enemy()) enemy++;
            else empty++;
        }
        
        if (cpu > 0 && enemy == 0) {
            switch (cpu) {
                case 5: return FIVE_IN_A_ROW;
                case 4: return empty == 1 ? FOUR_OPEN : FOUR_CLOSED;
                case 3: return empty == 2 ? THREE_OPEN : THREE_CLOSED;
                case 2: return empty == 3 ? TWO_OPEN : 0;
            }
        } else if (enemy > 0 && cpu == 0) {
            switch (enemy) {
                case 5: return -FIVE_IN_A_ROW;
                case 4: return empty == 1 ? -FOUR_OPEN : -FOUR_CLOSED;
                case 3: return empty == 2 ? -THREE_OPEN : -THREE_CLOSED;
                case 2: return empty == 3 ? -TWO_OPEN : 0;
            }
        }
        
        return 0;
    }
    
    private int evaluateCenterControl(Board board) {
        int score = 0;
        int centerX = 7, centerY = 7;
        
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                Mark mark = board.getMark(i, j);
                if (mark != Mark.EMPTY) {
                    int distance = Math.max(Math.abs(i - centerX), Math.abs(j - centerY));
                    int value = Math.max(0, 8 - distance) * CENTER_VALUE;
                    
                    if (mark == cpuMark) score += value;
                    else score -= value;
                }
            }
        }
        
        return score;
    }
    
    private int evaluateThreats(Board board, Mark mark) {
        int score = 0;
        
        // Vérifier les menaces de capture
        List<Move> possibleMoves = board.getPossibleMoves();
        for (Move move : possibleMoves) {
            if (canLeadToCapture(board, move, mark)) {
                score += 500;  // Points pour une menace de capture
                
                // Bonus pour les captures multiples
                if (canLeadToMultipleCaptures(board, move, mark)) {
                    score += 1000;
                }
            }
        }
        
        // Vérifier les alignements menaçants
        score += evaluateThreateningPatterns(board, mark);
        
        return score;
    }
    
    private boolean canLeadToCapture(Board board, Move move, Mark mark) {
        try {
            // Directions possibles de capture
            int[][] directions = {
                {0, 1},   // horizontal
                {1, 0},   // vertical
                {1, 1},   // diagonal \
                {1, -1}   // diagonal /
            };

            int row = move.getIngameRow() - 1;
            int col = move.getIngameCol() - 'A';

            for (int[] dir : directions) {
                // Vérifier dans les deux sens pour chaque direction
                if (checkCaptureDirection(board, row, col, dir[0], dir[1], mark) ||
                    checkCaptureDirection(board, row, col, -dir[0], -dir[1], mark)) {
                    return true;
                }
            }
        } catch (Exception e) {
            // En cas d'erreur, retourner false de manière sécurisée
            return false;
        }
        return false;
    }

private boolean isCapturePotential(Mark[] line, Mark mark) {
        Mark enemy = mark.enemy();
        
        // Pattern: [mark][empty][enemy][enemy]
        if (line[0] == mark && line[1] == Mark.EMPTY && 
            line[2] == enemy && line[3] == enemy) return true;
            
        // Pattern: [enemy][enemy][empty][mark]
        if (line[0] == enemy && line[1] == enemy && 
            line[2] == Mark.EMPTY && line[3] == mark) return true;
            
        return false;
    }
    
    private boolean createsFourInARow(Board board, Move move, Mark mark) {
        int[][] directions = {
            {1, 0}, {0, 1}, {1, 1}, {1, -1}
        };
        
        int row = move.getIngameRow() - 1;
        int col = move.getIngameCol() - 'A';
        
        for (int[] dir : directions) {
            int count = 1; // Compter le pion qu'on vient de placer
            
            // Compter dans une direction
            int r = row + dir[0];
            int c = col + dir[1];
            while (isValidPosition(r, c) && board.getMark(r, c) == mark) {
                count++;
                r += dir[0];
                c += dir[1];
            }
            
            // Compter dans la direction opposée
            r = row - dir[0];
            c = col - dir[1];
            while (isValidPosition(r, c) && board.getMark(r, c) == mark) {
                count++;
                r -= dir[0];
                c -= dir[1];
            }
            
            if (count >= 4) return true;
        }
        return false;
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < 15 && col >= 0 && col < 15;
    }

    private boolean isFirstMove(Board board) {
        return board.getPossibleMoves().size() == 0; // 15x15
    }

    private boolean isSecondMove(Board board) {
        return board.getPossibleMoves().size() == 1; // Un seul coup joué
    }

    public ArrayList<Move> generateSecondRedMove(Board board) {
        ArrayList<Move> validMoves = new ArrayList<>();
        int centerRow = 7;  // H
        int centerCol = 7;  // 8
        
        // Parcourir le plateau et ajouter les coups valides
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                // Calculer la distance Manhattan du centre
                int distance = Math.abs(i - centerRow) + Math.abs(j - centerCol);
                if (distance >= 3 && board.getMark(i, j) == Mark.EMPTY) {
                    validMoves.add(new Move((char)('A' + j), i + 1));
                }
            }
        }
        return validMoves;
    }

    private boolean isWinningState(Board board, Mark mark) {
        return board.checkFor5inARow(mark) || countCaptures(board, mark) >= 5;
    }

    private int countCaptures(Board board, Mark mark) {
        // Cette méthode doit être implémentée selon la logique du jeu
        // Elle doit retourner le nombre de captures réalisées par le joueur
        int captures = 0;
        // TODO: Implémenter la logique de comptage des captures
        return captures;
    }

    private boolean isTerminalNode(Board board) {
        return board.checkFor5inARow(cpuMark) || 
               board.checkFor5inARow(cpuMark.enemy()) ||
               board.getPossibleMoves().isEmpty() ||
               countCaptures(board, cpuMark) >= 5 ||
               countCaptures(board, cpuMark.enemy()) >= 5;
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

    private boolean canLeadToMultipleCaptures(Board board, Move move, Mark mark) {
        try {
            Board tempBoard = new Board(board);
            tempBoard.play(move, mark);
            int captureCount = 0;
            
            int[][] directions = {{0,1}, {1,0}, {1,1}, {1,-1}};
            for (int[] dir : directions) {
                if (checkCaptureDirection(tempBoard, 
                    move.getIngameRow()-1, 
                    move.getIngameCol()-'A', 
                    dir[0], dir[1], mark)) {
                    captureCount++;
                }
                if (checkCaptureDirection(tempBoard, 
                    move.getIngameRow()-1, 
                    move.getIngameCol()-'A', 
                    -dir[0], -dir[1], mark)) {
                    captureCount++;
                }
            }
            return captureCount > 1;
        } catch (Exception e) {
            return false;
        }
    }

    private int evaluateThreateningPatterns(Board board, Mark mark) {
        int score = 0;
        try {
            // Évaluer les patterns menaçants (comme 3 ou 4 pions alignés)
            for (int row = 0; row < 15; row++) {
                for (int col = 0; col < 15; col++) {
                    score += evaluatePatternAtPosition(board, row, col, mark);
                }
            }
        } catch (Exception e) {
            // En cas d'erreur, retourner un score neutre
            return 0;
        }
        return score;
    }

    private int evaluatePatternAtPosition(Board board, int row, int col, Mark mark) {
        if (!isValidPosition(row, col) || board.getMark(row, col) != mark) {
            return 0;
        }

        int score = 0;
        int[][] directions = {{0,1}, {1,0}, {1,1}, {1,-1}};
        
        for (int[] dir : directions) {
            int count = countConsecutive(board, row, col, dir[0], dir[1], mark);
            score += getPatternScore(count);
        }
        
        return score;
    }

    private int countConsecutive(Board board, int row, int col, int dRow, int dCol, Mark mark) {
        int count = 1;
        int r = row + dRow;
        int c = col + dCol;
        
        while (isValidPosition(r, c) && board.getMark(r, c) == mark) {
            count++;
            r += dRow;
            c += dCol;
        }
        
        return count;
    }

    private int getPatternScore(int count) {
        switch (count) {
            case 4: return 1000;  // Quatre alignés
            case 3: return 100;   // Trois alignés
            case 2: return 10;    // Deux alignés
            default: return 0;
        }
    }
}