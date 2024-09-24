import java.util.ArrayList;

// IMPORTANT: Il ne faut pas changer la signature des méthodes
// de cette classe, ni le nom de la classe.
// Vous pouvez par contre ajouter d'autres méthodes (ça devrait 
// être le cas)
class CPUPlayer
{

    private static final int NBROWS = 3;
    private static final int NBCOLS = 3;
    private Mark maxMark;

    // Contient le nombre de noeuds visités (le nombre
    // d'appel à la fonction MinMax ou Alpha Beta)
    // Normalement, la variable devrait être incrémentée
    // au début de votre MinMax ou Alpha Beta.
    private int numExploredNodes;

    // Le constructeur reçoit en paramètre le
    // joueur MAX (X ou O)
    public CPUPlayer(Mark cpu){
        maxMark = cpu;
    }

    // Ne pas changer cette méthode
    public int  getNumOfExploredNodes(){
        return numExploredNodes;
    }

    // Retourne la liste des coups possibles.  Cette liste contient
    // plusieurs coups possibles si et seuleument si plusieurs coups
    // ont le même score.
    public ArrayList<Move> getNextMoveMinMax(Board board)
    {
        ArrayList<Move> listMove = new ArrayList<>();

        for (int boardRows = 0; boardRows < CPUPlayer.NBROWS; boardRows++) {
            for (int boardCols = 0; boardCols < CPUPlayer.NBCOLS; boardCols++) {

                if (board.getBoard()[boardRows][boardCols] == Mark.EMPTY){

                    Move moveAEssayer = new Move(boardRows, boardCols);
                    numExploredNodes++;     //pour savoir si x ou o

                    if (getNumOfExploredNodes() % 2 == 0){
                        maxMark = Mark.X;
                    }
                    else{
                        maxMark = Mark.O;
                    }


                    board.play(moveAEssayer, maxMark);  //on joue le coup de facon temporaire
                    if (board.evaluate())

                    //TODO IMPLEMENTER BACKTRACKING
                }

            }
        }


        numExploredNodes = 0;
        return null;
    }

    // Retourne la liste des coups possibles.  Cette liste contient
    // plusieurs coups possibles si et seuleument si plusieurs coups
    // ont le même score.
    public ArrayList<Move> getNextMoveAB(Board board){
        numExploredNodes = 0;
        return null;
    }

    public int recursiveBoard(Board board, int valeurTableau, ArrayList<Move> listMove, Move move, Mark mark){

        board.play(move, mark);
        valeurTableau = board.evaluate(mark);
        boolean empty = false;


        for (int i = 0; i < 3; i++){            //check if grid is empty to continue or not
            for (int j = 0; j<3; j++){
                if (board.getBoard()[i][j] == Mark.EMPTY){
                    empty = true;
                }
            }
        }

        if (valeurTableau == 100 || valeurTableau == -100){
            return valeurTableau;
        }
        if (valeurTableau == 0 && empty == false){
            return valeurTableau;
        }


        else{

            for (int boardRows = 0; boardRows < CPUPlayer.NBROWS; boardRows++) {
                for (int boardCols = 0; boardCols < CPUPlayer.NBCOLS; boardCols++) {

                    if (board.getBoard()[boardRows][boardCols] == Mark.EMPTY){

                        Move moveAEssayer = new Move(boardRows, boardCols);
                        numExploredNodes++;     //pour savoir si x ou o

                        if (getNumOfExploredNodes() % 2 == 0){
                            maxMark = Mark.X;
                        }
                        else{
                            maxMark = Mark.O;
                        }

                        int resultat = recursiveBoard(board, valeurTableau, listMove, moveAEssayer, maxMark);
                        
                        //comparer le resultat avec les autres resultats presents dans la listMove

                    }

                }
            }


        }
    }

    //+100 EST DEFINI COMME UNE VICTOIRE DES X
    public void addMoveToList(Move move, ArrayList<Move> listMove, Mark mark){
        if (listMove.isEmpty()){
            listMove.add(move);
        }
        else{
            
        }
    }


}


