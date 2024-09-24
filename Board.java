import java.util.*;

// IMPORTANT: Il ne faut pas changer la signature des méthodes
// de cette classe, ni le nom de la classe.
// Vous pouvez par contre ajouter d'autres méthodes (ça devrait 
// être le cas)
class Board
{

    private Mark[][] board;

    // Ne pas changer la signature de cette méthode
    public Board() {
        board = new Mark[][]{
                {Mark.EMPTY, Mark.EMPTY, Mark.EMPTY},
                {Mark.EMPTY, Mark.EMPTY, Mark.EMPTY},
                {Mark.EMPTY, Mark.EMPTY, Mark.EMPTY},
        };

        Scanner clavier = new Scanner(System.in);
        System.out.print("Voulez-vous jouer en premier (1) ou en deuxième (2) ?: ");
        int choix = clavier.nextInt();

        if (choix == 1){
            System.out.println("Choix: " + choix + ". Vous jouez les X");
            CPUPlayer cpu = new CPUPlayer(Mark.O);
        }
        else if (choix == 2){
            System.out.println("Choix: " + choix + ". Vous jouez les O");
            CPUPlayer cpu = new CPUPlayer(Mark.X);
        }


    }

    // Place la pièce 'mark' sur le plateau, à la
    // position spécifiée dans Move
    //
    // Ne pas changer la signature de cette méthode
    public void play(Move m, Mark mark){
        board[m.getRow()][m.getCol()] = mark;
        //TODO ALGO DE RECHERCHE ?
    }


    // retourne  100 pour une victoire
    //          -100 pour une défaite
    //           0   pour un match nul
    // Ne pas changer la signature de cette méthode
    public int evaluate(Mark mark){

        for (int i = 0; i < 3; i++){        //checking rows for win

            for (int j = 0; j < 3; j++){
                if (this.board[i][j] == this.board[i][0]){
                    if (j == 2){
                        if (this.board[i][j] == Mark.X){        //victoire de x amene toujours +100
                            return 100;
                        }
                        else{
                            return -100;
                        }
                    }
                }
                else
                    break;
            }

        }

        for (int k = 0; k < 3; k++){        //checking columns for win

            for (int l = 0; l < 3; l++){
                if (this.board[l][k] == this.board[0][k]){
                    if (l == 2){
                        if (this.board[l][k] == Mark.X){
                            return 100;
                        }
                        else{
                            return -100;
                        }
                    }
                    continue;
                }
                else
                    break;
            }

        }

        for (int m = 0; m < 2; m++){        //checking diagonal from left up corner for win
            for (int n = m; n == m; n++){
                if (this.board[0][0] == this.board[m][n]){
                    if (m == 2){
                        if (this.board[0][0] == Mark.X){
                            return 100;
                        }
                        else{
                            return -100;
                        }
                    }
                    continue;
                }
                else
                    break;
            }
        }

        for (int m = 0; m < 2; m++){        //checking diagonal from left up corner for win
            for (int n = 2-m; n == 2-m; n++){
                if (this.board[0][2] == this.board[m][n]){
                    if (m == 2){
                        if (this.board[0][2] == Mark.X){
                            return 100;
                        }
                        else{
                            return -100;
                        }
                    }
                    continue;
                }
                else
                    break;
            }
        }

        return 0;
    }

    public Mark[][] getBoard() {
        return board;
    }
}
