public class Sequence {
    private Mark mark;
    private int indexDebut;
    private int indexFin;
    private int directionX;
    private int directionY;
    private int length;

    public Sequence(int debut, int fin, Mark mark, int x, int y, int length){
        this.indexDebut = debut;
        this.indexFin = fin;
        this.mark = mark;
        this.directionX = x;
        this.directionY = y;
        this.length = length;
    }

    public String getSequenceDirection(){
        if (directionX == 1 && directionY == 0){
            return "horizontale";
        } else if (directionY == 1 && directionX == 0) {
            return "verticale";
        } else if (directionX == 1 && directionY == 1) {
            return "diagonale sud-est";
        }
        else{
            return "diagonale sud-ouest";
        }
    }

    public int getIndexDebut() {
        return indexDebut;
    }

    public int getIndexFin() {
        return indexFin;
    }
    public int getLength(){
        return length;
    }

    public int getSequenceLength(){
        int counter = 0;
        int indexJump = 0;
        int i = indexDebut;
        String direction = getSequenceDirection();
        switch (direction){
            case "verticale":
                indexJump = 15;
                break;
            case "horizontale":
                indexJump = 1;
                break;
            case "diagonale sud-est":
                indexJump = 16;
                break;
            case "diagonale sud-ouest":
                indexJump = 14;
                break;
        }
        while (i < indexFin){
            i += indexJump;
            counter++;
        }
        return counter;

    }
    public Mark getMark(){
        return mark;
    }
}
