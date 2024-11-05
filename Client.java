import java.io.*;
import java.net.*;
import java.util.ArrayList;

class Client {
    private static Board gameBoard;
    private static CPUPlayer ai;
    private static Mark playerColor;
    private static final boolean DEBUG_MODE = true;

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

        // Lire l'état initial (série de 0)
        byte[] buffer = new byte[1024];
        int size = input.available();
        input.read(buffer, 0, size);
        
        debugPrint("Nouvelle partie - Nous jouons: " + (color == Mark.RED ? "ROUGE" : "NOIR"));
        printBoard();

        // Si nous sommes Rouge, jouer le premier coup (H8)
        if (color == Mark.RED) {
            String firstMove = "H8";
            sendMove(output, firstMove);
            gameBoard.play(new Move('H', 8), Mark.RED);
            debugPrint("Premier coup (Rouge) joué: H8");
            printBoard();
        }
    }

    private static void playTurn(BufferedInputStream input, BufferedOutputStream output) throws IOException {
        // Lire le coup adverse
        byte[] buffer = new byte[16];
        int size = input.available();
        input.read(buffer, 0, size);
        String lastMove = new String(buffer).trim();
        
        debugPrint("Dernier coup adverse reçu: " + lastMove);

        // Valider et jouer le coup adverse
        if (isValidMove(lastMove)) {
            Move opponentMove = new Move(lastMove.charAt(0), 
                                       Integer.parseInt(lastMove.substring(1)));
            gameBoard.play(opponentMove, playerColor.enemy());
            printBoard();
        }

        // Déterminer notre coup
        String ourMove;
        if (gameBoard.getMoveCount() == 0 && playerColor == Mark.RED) {
            // Premier coup Rouge : H8 obligatoire
            ourMove = "H8";
        } else if (gameBoard.getMoveCount() == 1 && playerColor == Mark.BLACK) {
            // Premier coup Noir : après H8
            ourMove = "I8";
        } else if (playerColor == Mark.RED && gameBoard.getMoveCount() == 1) {
            // Deuxième coup Rouge : distance 3+ du centre
            ArrayList<Move> validMoves = ai.generateSecondRedMove(gameBoard);
            ourMove = validMoves.get(0).toString();
        } else {
            // Coup normal
            ArrayList<Move> moves = ai.getNextMoveAB(gameBoard);
            ourMove = moves.get(0).toString();
        }

        // Vérifier que notre coup est valide avant de l'envoyer
        if (isValidMove(ourMove)) {
            sendMove(output, ourMove);
            gameBoard.play(new Move(ourMove.charAt(0), 
                                  Integer.parseInt(ourMove.substring(1))), 
                          playerColor);
            debugPrint("Notre coup joué: " + ourMove);
            printBoard();
        } else {
            debugPrint("ERREUR: Tentative de coup invalide: " + ourMove);
            // Jouer un coup de secours
            retryMove(output);
        }
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
        
        String newMove;
        if (gameBoard.getMoveCount() == 1 && playerColor == Mark.BLACK) {
            // Si notre premier coup en tant que Noir était invalide, essayer un autre coup
            newMove = "G8";
        } else {
            ArrayList<Move> moves = ai.getNextMoveAB(gameBoard);
            newMove = moves.get(0).toString();
        }
        
        sendMove(output, newMove);
        debugPrint("Nouveau coup tenté: " + newMove);
    }

    private static void endGame(BufferedInputStream input, BufferedOutputStream output) throws IOException {
        byte[] buffer = new byte[16];
        int size = input.available();
        input.read(buffer, 0, size);
        String lastMove = new String(buffer).trim();
        
        debugPrint("Partie terminée - Dernier coup: " + lastMove);
        printBoard();
        sendMove(output, "A0");
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