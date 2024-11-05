import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

class Client {
    private static Board gameBoard;
    private static CPUPlayer ai;
    private static Mark playerColor;
    private static final boolean DEBUG_MODE = true;
    private static Set<String> invalidMoves = new HashSet<>();
    private static Set<String> occupiedPositions = new HashSet<>();
    private static final String[] SAFE_MOVES = {
        "E8", "K8", "H4", "H12", "D8", "L8", 
        "E12", "K12", "E4", "K4", "C8", "M8"
    };
    private static int safeMovesIndex = 0;

    public static void main(String[] args) {
        try {
            Socket MyClient = new Socket("localhost", 8888);
            BufferedInputStream input = new BufferedInputStream(MyClient.getInputStream());
            BufferedOutputStream output = new BufferedOutputStream(MyClient.getOutputStream());
            
            while (true) {
                char cmd = (char)input.read();
                debugPrint("Commande reçue: " + cmd);

                switch (cmd) {
                    case '1': // Nous sommes Rouge (Premier joueur)
                        initializeGame(input, output, Mark.RED);
                        break;
                        
                    case '2': // Nous sommes Noir (Second joueur)
                        initializeGame(input, output, Mark.BLACK);
                        break;
                        
                    case '3': // Notre tour
                        playTurn(input, output);
                        break;
                        
                    case '4': // Coup invalide, rejouer
                        retryMove(output);
                        break;
                        
                    case '5': // Fin de partie
                        endGame(input, output);
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    private static void initializeGame(BufferedInputStream input, BufferedOutputStream output, Mark color) throws IOException {
        gameBoard = new Board();
        playerColor = color;
        ai = new CPUPlayer(color);
        invalidMoves.clear();
        occupiedPositions.clear();
        safeMovesIndex = 0;

        byte[] buffer = new byte[1024];
        int size = input.available();
        input.read(buffer, 0, size);
        
        debugPrint("Nouvelle partie - Nous jouons: " + (color == Mark.RED ? "ROUGE" : "NOIR"));
        printBoard();

        if (color == Mark.RED) {
            String firstMove = "H8";
            sendMove(output, firstMove);
            gameBoard.play(new Move('H', 8), Mark.RED);
            debugPrint("Premier coup (Rouge) joué: H8");
            printBoard();
        }
    }

    private static void playTurn(BufferedInputStream input, BufferedOutputStream output) throws IOException {
        try {
            // Lire et traiter le coup adverse
            byte[] buffer = new byte[16];
            int size = input.available();
            input.read(buffer, 0, size);
            String lastMove = new String(buffer).trim();
            
            debugPrint("Dernier coup adverse reçu: " + lastMove);

            // Mettre à jour notre représentation du plateau avec le coup adverse
            if (isValidMove(lastMove)) {
                try {
                    Move opponentMove = new Move(lastMove.charAt(0), 
                                               Integer.parseInt(lastMove.substring(1)));
                    gameBoard.play(opponentMove, playerColor.enemy());
                    occupiedPositions.add(lastMove);
                    printBoard();
                } catch (Exception e) {
                    debugPrint("Erreur lors de la mise à jour du plateau avec le coup adverse: " + e.getMessage());
                }
            }

            // Obtenir et jouer notre prochain coup
            String ourMove = getNextValidMove();
            sendMove(output, ourMove);
            
            try {
                Move move = new Move(ourMove.charAt(0), 
                                   Integer.parseInt(ourMove.substring(1)));
                gameBoard.play(move, playerColor);
                occupiedPositions.add(ourMove);
                debugPrint("Notre coup joué: " + ourMove);
                printBoard();
            } catch (Exception e) {
                debugPrint("Erreur lors de notre coup: " + e.getMessage());
                invalidMoves.add(ourMove);
                retryMove(output);
            }
        } catch (Exception e) {
            debugPrint("Erreur dans playTurn: " + e.getMessage());
            retryMove(output);
        }
    }

    private static String getNextValidMove() {
        // Premier coup rouge
        if (gameBoard.getMoveCount() == 0 && playerColor == Mark.RED) {
            return "H8";
        }

        // Deuxième coup rouge - doit être à distance 3+ du centre
        if (gameBoard.getMoveCount() == 2 && playerColor == Mark.RED) {
            for (String move : SAFE_MOVES) {
                if (!invalidMoves.contains(move) && !occupiedPositions.contains(move)) {
                    return move;
                }
            }
        }

        // Essayer d'obtenir un coup de l'IA
        try {
            ArrayList<Move> moves = ai.getNextMoveAB(gameBoard);
            for (Move move : moves) {
                String moveStr = move.toString();
                if (!invalidMoves.contains(moveStr) && !occupiedPositions.contains(moveStr)) {
                    return moveStr;
                }
            }
        } catch (Exception e) {
            debugPrint("Erreur lors de la génération du coup par l'IA: " + e.getMessage());
        }

        // Si rien ne marche, utiliser un coup de secours
        return findSafeMove();
    }

    private static String determineNextMove() {
        // Cas spéciaux
        if (gameBoard.getMoveCount() == 0 && playerColor == Mark.RED) {
            return "H8";
        }
        if (gameBoard.getMoveCount() == 1 && playerColor == Mark.BLACK) {
            return "I8";
        }
        if (playerColor == Mark.RED && gameBoard.getMoveCount() == 2) {
            ArrayList<Move> validMoves = ai.generateSecondRedMove(gameBoard);
            String move = validMoves.get(0).toString();
            if (!invalidMoves.contains(move)) {
                return move;
            }
            // Si le coup est invalide, utiliser un coup de secours
            return findSafeDistantMove();
        }

        // Coup normal avec l'IA
        ArrayList<Move> moves = ai.getNextMoveAB(gameBoard);
        for (Move move : moves) {
            String moveStr = move.toString();
            if (!invalidMoves.contains(moveStr) && isValidMove(moveStr)) {
                return moveStr;
            }
        }

        // Si aucun coup valide n'est trouvé, utiliser un coup de secours
        return findSafeMove();
    }

    private static String findSafeDistantMove() {
        // Pour le second coup rouge (distance >= 3 du centre)
        String[] distantMoves = {"E8", "K8", "H4", "H12", "D8", "L8"};
        for (String move : distantMoves) {
            if (!invalidMoves.contains(move)) {
                return move;
            }
        }
        return "E8"; // Coup par défaut si tous les autres sont invalides
    }

    // Méthode de validation des coups
    private static boolean isValidMove(String moveStr) {
        if (moveStr.equals("A0")) return false;
        if (moveStr.length() < 2) return false;
        
        char col = moveStr.charAt(0);
        if (col < 'A' || col > 'O') return false;
        
        try {
            int row = Integer.parseInt(moveStr.substring(1));
            return row >= 1 && row <= 15;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static void retryMove(BufferedOutputStream output) throws IOException {
        debugPrint("Coup invalide détecté, nouvel essai...");
        
        String newMove = findSafeMove();
        while (invalidMoves.contains(newMove)) {
            newMove = findSafeMove();
        }
        
        sendMove(output, newMove);
        debugPrint("Nouveau coup tenté: " + newMove);
        
        try {
            gameBoard.play(new Move(newMove.charAt(0), 
                                  Integer.parseInt(newMove.substring(1))), 
                          playerColor);
        } catch (Exception e) {
            invalidMoves.add(newMove);
            // Si même le coup de secours échoue, essayer le suivant
            retryMove(output);
        }
    }

    private static String findSafeMove() {
        // Essayer les coups prédéfinis d'abord
        for (int i = 0; i < SAFE_MOVES.length; i++) {
            String move = SAFE_MOVES[(safeMovesIndex + i) % SAFE_MOVES.length];
            if (!invalidMoves.contains(move) && !occupiedPositions.contains(move)) {
                safeMovesIndex = (safeMovesIndex + i + 1) % SAFE_MOVES.length;
                return move;
            }
        }

        // Si tous les coups sûrs sont épuisés, chercher systématiquement
        for (char col = 'A'; col <= 'O'; col++) {
            for (int row = 1; row <= 15; row++) {
                String move = "" + col + row;
                if (!invalidMoves.contains(move) && !occupiedPositions.contains(move)) {
                    try {
                        if (isValidPosition(col - 'A', row - 1)) {
                            return move;
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
            }
        }

        // En dernier recours
        return "H6";
    }

    private static boolean isValidPosition(int col, int row) {
        return row >= 0 && row < 15 && col >= 0 && col < 15 && 
               gameBoard.getMark(row, col) == Mark.EMPTY;
    }

    private static void endGame(BufferedInputStream input, BufferedOutputStream output) throws IOException {
        byte[] buffer = new byte[16];
        int size = input.available();
        input.read(buffer, 0, size);
        String lastMove = new String(buffer).trim();
        
        debugPrint("Partie terminée - Dernier coup: " + lastMove);
        printBoard();
        
        // Envoyer l'acquittement et fermer proprement
        sendMove(output, "A0");
        try {
            // Optionnel : attendre un court instant pour s'assurer que le message est envoyé
            Thread.sleep(100);
            System.exit(0);  // Termine proprement le programme
        } catch (InterruptedException e) {
            System.exit(0);
        }
    }

    private static void sendMove(BufferedOutputStream output, String move) throws IOException {
        output.write(move.getBytes(), 0, move.length());
        output.flush();
    }

    private static void debugPrint(String message) {
        if (DEBUG_MODE) {
            System.out.println("\n" + message);
        }
    }

    private static void printBoard() {
        if (!DEBUG_MODE) return;
        
        System.out.println("\nÉtat du plateau:");
        System.out.println("  A B C D E F G H I J K L M N O");
        
        for (int i = 0; i < 15; i++) {
            System.out.printf("%2d", (15 - i));
            for (int j = 0; j < 15; j++) {
                Mark mark = gameBoard.getMark(i, j);
                char symbol = switch (mark) {
                    case RED -> 'R';
                    case BLACK -> 'B';
                    case EMPTY -> '.';
                };
                System.out.print(" " + symbol);
            }
            System.out.printf(" %2d%n", (15 - i));
        }
        
        System.out.println("  A B C D E F G H I J K L M N O");
        System.out.println("Coups joués: " + gameBoard.getMoveCount() + "\n");
    }
}