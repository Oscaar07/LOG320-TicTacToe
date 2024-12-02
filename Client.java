import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

class Client {
    private static Game gameBoard;
    private static CPUPlayer ai;
    private static Mark playerColor;
    private static final boolean DEBUG_MODE = true;
    private static Set<String> invalidMoves = new HashSet<>();
    //private static Set<String> occupiedPositions = new HashSet<>();
    public static final String RESET = "\033[0m"; // Reset color
    public static final String RED = "\033[0;31m";
    public static final String BLACK = "\033[0;30m";

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
        gameBoard = new Game();
        playerColor = color;
        ai = new CPUPlayer(color);
        invalidMoves.clear();

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
                if (playerColor == Mark.RED && gameBoard.getMoveCount() == 3){
                    gameBoard.update2ndMoveRed();
                }
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


        // Deuxième coup rouge - doit être à distance 3+ du centre
        if (gameBoard.getMoveCount() == 2 && playerColor == Mark.RED) {

            Move move = gameBoard.getMoveList().get(1);
            if (move.getIngameRow() >= 8){
                if (move.getIngameCol() > 'H'){
                    return "K5";
                }else return "E5";
            }
            else{
                if (move.getIngameCol() > 'H'){
                    return "K11";
                }
                else return "E11";
            }
        }

        else{
            // Essayer d'obtenir un coup de l'IA
            try {
                ArrayList<Move> moves = ai.getNextMoveAB(gameBoard);        //pourrait retourner juste une valeur
                for (Move move : moves) {
                    String moveStr = move.toString();

                    if (!invalidMoves.contains(moveStr)) {
                        return moveStr;
                    }
                }
            } catch (Exception e) {
                debugPrint("Erreur lors de la génération du coup par l'IA: " + e.getMessage());
            }
        }

        return null;
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

        String newMove = "A1";


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
        System.out.println("     A B C D E F G H I J K L M N O");
        
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
        
        System.out.println("     A B C D E F G H I J K L M N O");
        System.out.println("Coups joués: " + gameBoard.getMoveCount() + "\n");



        System.out.println("\nÉtat des menaces rouges:");
        System.out.println("     A B C D E F G H I J K L M N O");

        for (int i = 0; i < 15; i++) {
            System.out.printf("%2d", (15 - i));
            for (int j = 0; j < 15; j++) {

                if (gameBoard.getBoard()[i][j].getMark() == Mark.RED){
                    System.out.print(RED + "  *" + RESET);
                } else if (gameBoard.getBoard()[i][j].getMark() == Mark.BLACK) {
                    System.out.print(BLACK + "  *" + RESET);
                }
                else{
                    int valeurMenacesRouges = gameBoard.getBoard()[i][j].getValue(Mark.RED);
                    System.out.printf("%3d", (valeurMenacesRouges));
                }
            }
            System.out.printf(" %2d%n", (15 - i));
        }

        System.out.println("\nÉtat des menaces noires:");
        System.out.println("     A B C D E F G H I J K L M N O");

        for (int i = 0; i < 15; i++) {
            System.out.printf("%2d", (15 - i));
            for (int j = 0; j < 15; j++) {
                int index = i * 15 + j;
                if (gameBoard.getBoard()[i][j].getMark() == Mark.RED){
                    System.out.print(RED + "  *" + RESET);
                } else if (gameBoard.getBoard()[i][j].getMark() == Mark.BLACK) {
                    System.out.print(BLACK + "  *" + RESET);
                }
                else{
                    int valeurMenacesNoires = gameBoard.getBoard()[i][j].getValue(Mark.BLACK);
                    System.out.printf("%3d", (valeurMenacesNoires));
                }
            }
            System.out.printf(" %2d%n", (15 - i));
        }

        System.out.println("     A B C D E F G H I J K L M N O");
    }
}