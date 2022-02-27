package chess;

import java.util.Scanner;
import java.util.ArrayList;
import chess.piece.*;

/**
 * Chess is a class that takes care of the game logic of chess. It contains 
 * a 2D array of Piece, which is a class for the chess pieces. 
 * 
 * @author Seok Yim, Mae Khaled
 */
public class Chess {//HAVE TO IMPLEMENT CASTLING!!! ALSO NEED TO TEST THE THINGS I NEWLY IMPLEMENTED!// CHECK AND CHECK MATE IMPLEMENATION!// ALSO, ANY MOVE THAT MAKES THEIR OWN KING CHECKED/CHECKMATED IS NOT ALLOWED!
    //ALSO, HAVE TO WRITE JAVA DOC FOR THE CLASSES AS WELL!!
    /**
     * This is a chessboard of grid size 8 * 8 (regular chessboard). Is a 
     * 2D matrix for Piece instances.
     */
    public Piece[][] chessboard;
    /**
     * Specifies who's turn it is (either is 'w' or 'b', each standing for white and black,
     * respectively). Initial value is 'w', since white starts first in chess.
     */
    public char turn = 'w';
    /**
     * Specifies whether the chess game is over or not. Starts out as false.
     */
    public boolean isOver = false;
    /**
     * Field indicating the winner. The 'd' stands for default, and the actual values it can/should
     * hold are 'w' or 'b'.
     */
    public char winner = 'd';

    /**
     * Tells how many turns have passed since the start of the game. Initial value is 0.
     */
    public int turns_passed = 0;

    /**
     * The main method.
     * 
     * @param args  the command line arguments
     */
    public static void main(String[] args){
        Chess game = new Chess();
        game.initiateGame();
    }

    /**
     * Starts chess game by doing all the setups required. Takes care of
     * game logic and applying rules.
     */
    public void initiateGame(){//where the game logic lives; ASSUMES ALL INPUT GIVEN BY USER IS VALID!
        this.generateBoard();
        this.showBoard();
        Scanner scanner = new Scanner(System.in);
        while(!this.isOver){
            String response = this.promptInput(scanner);
            response = response.trim();
            String[] elements = response.split(" ");

            if(elements.length == 1){
                if(elements[0].equals("resign")){
                    if(this.turn == 'w'){
                        this.winner = 'b';
                    }
                    else{
                        this.winner = 'w';
                    }
                    this.endGame();
                    break;
                }
                else{
                    //should never reach here;
                        System.out.print("Illegal move, try again");
                        continue;//since the game should not change whose turn it is
                }
            }
            else if(elements.length >= 2 && elements.length <= 4){//regular move
                //fetching row index and column index for the move
            	
            	//locations are supposed to be only two characters long!
            	if(elements[0].length() != 2 || elements[1].length() != 2) {
            		System.out.print("Illegal move, try again");
                    continue;//since the game should not change whose turn it is
            	}
            	
                int sr, sc, er, ec;
                sr = 8 - (int)(elements[0].charAt(1) - '0');
                sc = (int)(elements[0].charAt(0)-'a');
                er = 8 - (int)(elements[1].charAt(1) - '0');
                ec = (int)(elements[1].charAt(0)-'a');
                if(!this.canMove(sr, sc, er, ec)){//illegal move
                    System.out.print("Illegal move, try again");
                    continue;//since the game should not change whose turn it is
                }
                else{//legal move here
                    if(elements.length > 2){//might contain draw proposal or promotion or even both
                        if(elements.length == 3){
                            if(elements[2].equals("draw?")){
                                movePiece(sr, sc, er, ec);
                                System.out.println();
                                this.showBoard();
                                if(this.checkCheckStatus() == 1) {//check detected
                                    System.out.println("\nCheck");
                                }
                                this.changeTurn();
                                response = this.promptInput(scanner);
                                response = response.trim();
                                while(!response.equals("draw")){
                                    System.out.print("Illegal move, try again");
                                    response = this.promptInput(scanner);
                                    response = response.trim();
                                }
                                break;//ending game wihtout announcing winner; simply break out of the while loop
                            }
                            else{//promotion
                                if(isValidPromotion(sr, sc, er, ec)){//do i need this condition checking part?
                                    movePieceWithPromotion(sr, sc, er, ec, elements[2]); 
                                }
                                else{
                                    //should never reach here; assumes all typed out promotions are valid promotions

                                System.out.print("Illegal move, try again");
                                continue;//since the game should not change whose turn it is
                                }
                            }
                        }
                        else if(elements.length == 4){//draw proposal with promotion
                            movePieceWithPromotion(sr, sc, er, ec, elements[2]);
                            System.out.println();
                            this.showBoard();
                            if(this.checkCheckStatus() == 1) {//check detected
                                System.out.println("\nCheck");
                            }
                            this.changeTurn();
                            response = this.promptInput(scanner);
                            response = response.trim();
                            while(!response.equals("draw")){
                                System.out.print("Illegal move, try again");
                                response = this.promptInput(scanner);
                                response = response.trim();
                            }
                            break;//ending game without announcing winner; simply break out of the while loop
                        }
                        else{
                            //should never reach here; user input never exceeds 4 space-separated terms
                        }
                    }
                    else{//a very regular move without any extra options
                        movePiece(sr, sc, er, ec);
                    }
                }   
            }
            else{
                //should never reach here
            }

            //check for check or checkmate after each valid move
            int check_status = this.checkCheckStatus();
            if(check_status == 0){
                changeTurn();
                System.out.println();
                this.showBoard();
            }
            else if(check_status == 1){//check
                changeTurn();
                System.out.println();
                this.showBoard();
                System.out.println("\nCheck");
            }
            else{//check_status == 2, checkmate, gameover
                if(this.turn == 'w'){
                    this.winner = 'w';
                }
                else{
                    this.winner = 'b';
                }
                System.out.println();
                this.showBoard();
                System.out.println("\nCheckmate");
                this.endGame();
            }
        }
        scanner.close();
        System.out.println();
    }
    
    /**
     * Changes turn in chess after one player ends their move.
     */
    public void changeTurn(){
        if(this.turn == 'w'){
            this.turn = 'b';
        }
        else{
            this.turn = 'w';
        }
        this.turns_passed++;
    }
    /**
     * Prompts input from user for the chess game.
     * 
     * @param scanner	Scanner instance used by initiateGame()
     * @return  		String typed in by user
     */
    public String promptInput(Scanner scanner){
        String which_player;
        if(this.turn == 'w'){
            which_player = "White";
        }
        else{
            which_player = "Black";
        }

        System.out.print("\n" + which_player + "'s move: ");
        String line = scanner.nextLine();
        return line;
    }
    /**
     * Ends the ongoing chess game. Also prints out who won the match.
     */
    public void endGame(){
        this.isOver = true;
        String winner = "White";
        if(this.winner == 'b'){
            winner = "Black";
        }

        System.out.println("\n" + winner + " wins");
    }

    /**
     * Generates the Piece[][] chessboard and populates it with the appropriate Piece instances. 
     * Row and column length are both 8(regular chess board).
     */
    public void generateBoard(){
        this.chessboard = new Piece[8][8];
        
        //inserting black pawns
        for(int i = 0; i < this.chessboard[1].length; i++){
            this.chessboard[1][i] = new Pawn(1, i, 'b');
        }
        //inserting white pawns
        for(int i = 0; i < this.chessboard[6].length; i++){
            this.chessboard[6][i] = new Pawn(6,i,'w');
        }

        //inserting other Pieces
        this.chessboard[0][0] = new Rook(0,0,'b'); this.chessboard[0][7] = new Rook(0,7,'b');
        this.chessboard[0][1] = new Knight(0,1,'b'); this.chessboard[0][6] = new Knight(0, 6, 'b');
        this.chessboard[0][2] = new Bishop(0,2,'b'); this.chessboard[0][5] = new Bishop(0, 5, 'b');
        this.chessboard[0][3] = new Queen(0, 3, 'b'); this.chessboard[0][4] = new King(0, 4, 'b');

        this.chessboard[7][0] = new Rook(7,0,'w'); this.chessboard[7][7] = new Rook(7,7,'w');
        this.chessboard[7][1] = new Knight(7,1,'w'); this.chessboard[7][6] = new Knight(7, 6, 'w');
        this.chessboard[7][2] = new Bishop(7,2,'w'); this.chessboard[7][5] = new Bishop(7, 5, 'w');
        this.chessboard[7][3] = new Queen(7, 3, 'w'); this.chessboard[7][4] = new King(7, 4, 'w');
    }
    /**
     * Prints the Piece[][] chessboard.
     */
    public void showBoard(){
        for(int i = 0; i < this.chessboard.length; i++){
            for(int j  = 0; j < this.chessboard[i].length; j++){
                if(this.chessboard[i][j] == null){
                    if((i + j) % 2 == 0){
                        System.out.print("   ");
                    }
                    else{
                        System.out.print("## ");
                    }
                }
                else{
                    System.out.print(this.chessboard[i][j] + " ");
                }
            }
            System.out.println((8-i));
        }
        for(int i = 0 ; i < this.chessboard[0].length; i++){
        	if(i == this.chessboard[0].length-1) {
                System.out.print(" " + (char)('a'+i));
        	}
        	else {
                System.out.print(" " + (char)('a'+i) + " ");

        	}
        }
        System.out.println();
    }
    /**
     * Checks whether the specified piece can move to the specified location.
     * 
     * @param sr    starting row index (row for Piece)
     * @param sc    starting column index (column for Piece)
     * @param er    ending/final row index
     * @param ec    ending/final column index
     * @return      true if move specified is valid, false otherwise
     */
    public boolean canMove(int sr, int sc, int er, int ec){
        //1. check whether the move is within the board (cannot go outside the board, also I’m pretty sure you HAVE TO MAKE A MOVE, so going to the same position as the current location does not work)
        if(sr < 0 || sr > 7 || sc < 0 || sc > 7 
        || er < 0 || er > 7 || ec < 0 || ec > 7){
            return false;
        }
        if(sr == er && sc == ec){
            return false;
        }
        //2. checks whether a piece exists at the specified location
        Piece s_piece = this.getPiece(sr, sc);
        if(s_piece == null){
            return false;
        }
        //.3. checks whether the piece is owned by the person moving the piece
        if(s_piece.color != this.turn){
            return false;
        }
		//4. checks whether the piece type can perform the action (call Piece.isValidMove(er,ec) —> induces polymorphism)
        if(!s_piece.isValidMove(er, ec, this.chessboard,this.turns_passed)){//this part also checks whether there is a piece of the same color 
            //at the final location
            return false;
        }
        return true;
    }
    /**
     * This method should be called only when the CanMove() method returned true for the specified pair of
     * sr and sc. 
     * <p>
     * Moves the piece at the specified location from chessboard[sr][sc] to chessboard[er][ec].
     * If an enemey piece is killed, remove that piece from the board. This method also takes into account Pawns
     * being promoted after reaching the edge of the board.
     * 
     * 
     * @param sr    staerting row index
     * @param sc    starting column index
     * @param er    ending row index
     * @param ec    ending column index
     */
    public void movePiece(int sr, int sc, int er, int ec){
        //take CASTLING into consideration
        if(this.getPiece(sr, sc) instanceof King && Math.abs(sc - ec) == 2){
            int which_colored_king_row = 0;
            if(this.getPiece(sr, sc).color == 'w'){
                which_colored_king_row = 7;
            }
            if(sc > ec){
                movePiece(which_colored_king_row,0,which_colored_king_row,3);    
            }
            else{
                movePiece(which_colored_king_row,7,which_colored_king_row,5);    
            }
        }
        else if(this.getPiece(sr, sc) instanceof Pawn){//part for handling En Passant
            Pawn temp_pawn = (Pawn)this.getPiece(sr, sc);
            if(Math.abs(er - sr) == 2){
                temp_pawn.twoStepTurnNumber = this.turns_passed;
            }
            else if(Math.abs(temp_pawn.currentLocation[0] - er) == 1 && Math.abs(temp_pawn.currentLocation[1] - ec) == 1
            && this.chessboard[er][ec] == null){
                //En Passant DETECTED BABY
                Piece EnPassant_enemey = this.chessboard[temp_pawn.currentLocation[0]][ec];
                EnPassant_enemey.kill();
                this.chessboard[temp_pawn.currentLocation[0]][ec] = null;
            }
        }
        
        //check if there exists an enemy piece. If there is, kill it.
        Piece enemey = this.chessboard[er][ec];
        if(enemey != null){
            enemey.kill();
            this.chessboard[er][ec] = null;
        }
		
        Piece piece = this.getPiece(sr, sc);
        //check if promotion takes place(only for pawns)
            //if yes, call movePieceWithPromotion(sr,sc,er,ec,"Queen");
        if(this.isValidPromotion(sr, sc, er, ec)){
            movePieceWithPromotion(sr, sc, er, ec, "Q");
        }
        else{
            //else...
                //move 
            piece.move(er,ec);
            this.chessboard[sr][sc] = null;
            this.chessboard[er][ec] = piece;
        }
    }
    /**
     * This method should be called only when the CanMove() method returned true for the specified pair of 
     * sr and sc. Also, this method assumes that isValidPromotion() already checked the validity of the promotion.
     * <p>
     * Moves the piece at the specified location from chessboard[sr][sc] to chessboard[er][ec].
     * If an enemy piece is killed, remove that piece from the board. This method promotes Pawns to
     * the specified promoType (Rook,Knight,Bishop, or Queen).
     * 
     * @param sr        starting row index
     * @param sc        starting colum index
     * @param er        ending row index
     * @param ec        ending colum index
     * @param promoType String that specifies which type of piece to promote to
     */
    public void movePieceWithPromotion(int sr, int sc, int er, int ec, String promoType){
        //check if there exists an enemy piece. If there is, kill it.
        Piece enemey = this.chessboard[er][ec];
        if(enemey != null){
            enemey.kill();
            chessboard[er][ec] = null;
        }

        //get hold of piece and move it
        Piece piece = this.getPiece(sr, sc);
        piece.move(er,ec);
        this.chessboard[sr][sc] = null;
        this.chessboard[er][ec] = piece;
        
        //promote the pawn to promoType
        this.chessboard[er][ec] =  piece.promote(promoType);
    }
    
    /**
     * Returns Piece at specified location of the chessboard. Can return null if
     * Piece does not exist at the specified location.
     * 
     * @param row   row index
     * @param col   column index
     * @return      Piece or null, depending on whether Piece exists at the specific location
     */
    public Piece getPiece(int row, int col){
        return this.chessboard[row][col];
    }

    /**
     * Checks whether the move specified for a piece causes a promotion. 
     * 
     * @param sr    starting row index
     * @param sc    starting column index
     * @param er    ending row index
     * @param ec    ending column index
     * @return      true if piece is a pawn and is allowed to promote, false otherwise
     */
    public boolean isValidPromotion(int sr, int sc, int er, int ec){
        Piece piece = this.getPiece(sr, sc);
        if((er == 0 || er == 7) && piece instanceof Pawn){
            return true;
        }
        return false;
    }
    /**
     * Determines whether there is a check, a checkmate, or none.
     * 
     * @return  0 if none, 1 if check, and 2 if checkmate
     */
    public int checkCheckStatus(){
        char temp_color = 'w';
        if(this.turn == 'w'){
            temp_color = 'b';
        }

        if(Piece.leavesKingChecked(this.chessboard,temp_color)){
            //NEEDS TO FIND KING TO SET its isInCheck VALUE to TRUE
            Piece.findKing(this.chessboard, temp_color).isInChcek = true;
            if(this.isCheckMate()){
                return 2;
            }
            else{
                return 1;
            }
        }
        else{
            Piece.findKing(this.chessboard, temp_color).isInChcek = false;
        }
        return 0;
    }

    /**
     * Checks whether there is a checkmate.
     * This mehtod should be called only when a check is detected.
     * 
     * @return  true is there is a checkmate, false otherwise
     */
    public boolean isCheckMate(){
        char temp_color = 'w';
        if(this.turn == 'w'){
            temp_color = 'b';
        }

        for(int i = 0; i < this.chessboard.length; i++){
            for(int j = 0; j < this.chessboard[i].length; j++){
                Piece piece = this.chessboard[i][j];
                if(piece != null && piece.color == temp_color){
                    ArrayList<Integer[]> possible_moves = piece.getAllPossibleMoves(this.chessboard, this.turns_passed);
                    for(int k = 0; k < possible_moves.size(); k++){
                        int er = possible_moves.get(k)[0];
                        int ec = possible_moves.get(k)[1];
                        if(piece.isValidMove(er, ec, this.chessboard, this.turns_passed)){
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }
}
