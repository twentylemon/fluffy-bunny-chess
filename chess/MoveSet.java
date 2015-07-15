/**
 * MoveSet.java
 * COSC 3P71 - Fluffy Bunny Chess
 * Taras Mychaskiw
 * Lachlan Plant
 * Class that calculates possible moves for any piece.
 * The move sets are calculated in the following manner...
 * First, all possible moves are generated, ignoring if we are put into check.
 * Then, for each move in the set so far, we actually make the move in our local board.
 * If that move put us in check, we remove it from the list of moves in the move set.
 * Otherwise, it is a perfectly legal move, and is left in the set.
**/

import java.util.LinkedList;

public class MoveSet {

  private Board board;      //our own board to try moves on to check for legal moves
  private int[][] kingpos;  //the positions of the kings

  private final int PAWN   = Board.PAWN;
  private final int KNIGHT = Board.KNIGHT;
  private final int BISHOP = Board.BISHOP;
  private final int ROOK   = Board.ROOK;
  private final int QUEEN  = Board.QUEEN;
  private final int KING   = Board.KING;
  private final int queen_side = Board.queen_side, king_side = Board.king_side;

  public MoveSet(){
    board = new Board(null);
    kingpos = new int[2][2];  //[0][0] -> [black][x]... etc
  } //MoveSet()



 /*********************************************************
  *                  Utility Functions                   *
  * void init(Board)                                     *
  * int at(int,int)                                      *
  * boolean inMoveSet(int,int,LinkedList<Move>)          *
  * isInCheck(boolean[,Board])                           *
 *********************************************************/
  /**
   * void init(Board)
   * Reinitializes the board state. Also resets the 'kingpos' values.
  **/
  private void init(Board b){
    int i,j;
    board.copy(b);
    for (i = 0; i < 8; i++)
      for (j = 0; j < 8; j++)
        if (at(i,j) == KING){
          kingpos[1][0] = i;
          kingpos[1][1] = j;
        }
        else if (at(i,j) == -KING){
          kingpos[0][0] = i;
          kingpos[0][1] = j;
        }
  } //void init(Board)

  /**
   * int at(int,int)
   * Just Board.at() - some shorthand for functions below.
  **/
  private int at(int x, int y){
    return(board.at(x,y));
  } //int at(int,int)

  /**
   * boolean inMoveSet(int,int,LinkedList<Move>)
   * Returns true if (x,y) is in the move set sent. (x,y) is the 'move to' location.
  **/
  private boolean inMoveSet(int x, int y, LinkedList<Move> set){
    for (Move move : set)
      if (move.toX == x && move.toY == y)
        return(true);
    return(false);
  } //boolean inMoveSet(int,int,LinkedList<Move>)

  /**
   * boolean isInCheck(boolean wtm[,Board])
   * Returns true if 'wtm' is currently in check. Used to prune off illegal moves.
   * If a board is sent, checks that board for 'wtm' king being in check.
   * Basically, we just travel out from the king's position in every direction until we hit a piece.
   * If that piece is putting the king in check, we just return true right away.
   * If no threats are found, we return false.
  **/
  public boolean isInCheck(boolean wtm, Board brd){
    init(brd);
    return(isInCheck(wtm));
  } //boolean isInCheck(boolean,Board)
  private boolean isInCheck(boolean wtm){
    int i,j;
    int idx = (wtm) ? 1 : 0;
    int x = kingpos[idx][0]; int y = kingpos[idx][1];
    //check for rook/queen
    for (i = x+1; !board.offBoard(i,y); i++)
      if (board.isEnemy(i,y,wtm) && (board.isThisA(i,y,ROOK) || board.isThisA(i,y,QUEEN))){ return(true); }
      else if (board.isPiece(i,y)){ break; }
    for (i = x-1; !board.offBoard(i,y); i--)
      if (board.isEnemy(i,y,wtm) && (board.isThisA(i,y,ROOK) || board.isThisA(i,y,QUEEN))){ return(true); }
      else if (board.isPiece(i,y)){ break; }
    for (j = y+1; !board.offBoard(x,j); j++)
      if (board.isEnemy(x,j,wtm) && (board.isThisA(x,j,ROOK) || board.isThisA(x,j,QUEEN))){ return(true); }
      else if (board.isPiece(x,j)){ break; }
    for (j = y-1; !board.offBoard(x,j); j--)
      if (board.isEnemy(x,j,wtm) && (board.isThisA(x,j,ROOK) || board.isThisA(x,j,QUEEN))){ return(true); }
      else if (board.isPiece(x,j)){ break; }

    //check for bishop/queen
    for (i = x+1, j = y+1; !board.offBoard(i,j); i++, j++)
      if (board.isEnemy(i,j,wtm) && (board.isThisA(i,j,BISHOP) || board.isThisA(i,j,QUEEN))){ return(true); }
      else if (board.isPiece(i,j)){ break; }
    for (i = x-1, j = y+1; !board.offBoard(i,j); i--, j++)
      if (board.isEnemy(i,j,wtm) && (board.isThisA(i,j,BISHOP) || board.isThisA(i,j,QUEEN))){ return(true); }
      else if (board.isPiece(i,j)){ break; }
    for (i = x+1, j = y-1; !board.offBoard(i,j); i++, j--)
      if (board.isEnemy(i,j,wtm) && (board.isThisA(i,j,BISHOP) || board.isThisA(i,j,QUEEN))){ return(true); }
      else if (board.isPiece(i,j)){ break; }
    for (i = x-1, j = y-1; !board.offBoard(i,j); i--, j--)
      if (board.isEnemy(i,j,wtm) && (board.isThisA(i,j,BISHOP) || board.isThisA(i,j,QUEEN))){ return(true); }
      else if (board.isPiece(i,j)){ break; }

    //check for knight
    if (board.canMoveTo(x+2,y+1,wtm) && board.isThisA(x+2,y+1,KNIGHT)){ return(true); }
    if (board.canMoveTo(x+2,y-1,wtm) && board.isThisA(x+2,y-1,KNIGHT)){ return(true); }
    if (board.canMoveTo(x-2,y+1,wtm) && board.isThisA(x-2,y+1,KNIGHT)){ return(true); }
    if (board.canMoveTo(x-2,y-1,wtm) && board.isThisA(x-2,y-1,KNIGHT)){ return(true); }
    if (board.canMoveTo(x+1,y+2,wtm) && board.isThisA(x+1,y+2,KNIGHT)){ return(true); }
    if (board.canMoveTo(x+1,y-2,wtm) && board.isThisA(x+1,y-2,KNIGHT)){ return(true); }
    if (board.canMoveTo(x-1,y+2,wtm) && board.isThisA(x-1,y+2,KNIGHT)){ return(true); }
    if (board.canMoveTo(x-1,y-2,wtm) && board.isThisA(x-1,y-2,KNIGHT)){ return(true); }

    //check for the king... weird but needed
    idx = 1-idx;  //the other colour now
    if (x-kingpos[idx][0] == 0 && Math.abs(y-kingpos[idx][1]) < 2){ return(true); }
    if (y-kingpos[idx][1] == 0 && Math.abs(x-kingpos[idx][0]) < 2){ return(true); }
    if (Math.abs(x-kingpos[idx][0]) == 1 && Math.abs(y-kingpos[idx][1]) == 1){ return(true); }

    //check for a pawn
    x += (wtm) ? 1 : -1;  //xoffset, pawns attack in opposite directions
    if (board.canMoveTo(x,y+1,wtm) && board.isThisA(x,y+1,PAWN)){ return(true); }
    if (board.canMoveTo(x,y-1,wtm) && board.isThisA(x,y-1,PAWN)){ return(true); }
    return(false);
  } //boolean isInCheck(boolean wtm)



  /********************************************************
   *                 Move Set Generators                 *
   * LinkedList<Move> pawnMoves(int,int,boolean)         *
   * LinkedList<Move> knightMoves(int,int,boolean)       *
   * LinkedList<Move> queenMoves(int,int,boolean)        *
   * LinkedList<Move> rookMoves(int,int,boolean)         *
   * LinkedList<Move> bishopMoves(int,int,boolean)       *
   * LinkedList<Move> kingMoves(int,int,boolean)         *
   * LinkedList<Move> moveSet(int,int,boolean,Board)     *
  ********************************************************/
  /**
   * LinkedList<Move> pawnMoves(int,int,boolean)
   * Returns a list containing all possible moves for a pawn at (x,y).
  **/
  private LinkedList<Move> pawnMoves(int x, int y, boolean wtm){
    LinkedList<Move> list = new LinkedList<Move>();
    int off = (wtm) ? 1 : -1;
    int startRank = (wtm) ? 1 : 6;
    if (!board.isPiece(x+off,y)){
      if (x == 7-startRank){
        list.add(new Move(x,y,x+off,y,at(x+off,y),off*QUEEN));  //add queen first to help alpha-beta
        list.add(new Move(x,y,x+off,y,at(x+off,y),off*KNIGHT)); //then knight, check for forced mate in alpha-beta
        list.add(new Move(x,y,x+off,y,at(x+off,y),off*ROOK));
        list.add(new Move(x,y,x+off,y,at(x+off,y),off*BISHOP));
      }
      else {
        list.add(new Move(x,y,x+off,y,at(x+off,y)));      //move forward one
        if (x == startRank && !board.isPiece(x+2*off,y))  //jump forward two if first move
          list.add(new Move(x,y,x+2*off,y,at(x+2*off,y)));
      }
    }
    if (board.canCapture(x+off,y+1,wtm)){ //capture to the right
      if (x == 7-startRank){
        list.add(new Move(x,y,x+off,y+1,at(x+off,y+1),off*QUEEN));
        list.add(new Move(x,y,x+off,y+1,at(x+off,y+1),off*KNIGHT));
        list.add(new Move(x,y,x+off,y+1,at(x+off,y+1),off*ROOK));
        list.add(new Move(x,y,x+off,y+1,at(x+off,y+1),off*BISHOP));
      }
      else
        list.add(new Move(x,y,x+off,y+1,at(x+off,y+1)));
    }
    if (board.canCapture(x+off,y-1,wtm)){ //capture to the left
      if (x == 7-startRank){
        list.add(new Move(x,y,x+off,y-1,at(x+off,y-1),off*QUEEN));
        list.add(new Move(x,y,x+off,y-1,at(x+off,y-1),off*KNIGHT));
        list.add(new Move(x,y,x+off,y-1,at(x+off,y-1),off*ROOK));
        list.add(new Move(x,y,x+off,y-1,at(x+off,y-1),off*BISHOP));
      }
      else
        list.add(new Move(x,y,x+off,y-1,at(x+off,y-1)));
    }
    if (board.isEp){
      if (board.epx == x+off && board.epy == y+1)
        list.add(new Move(x,y,x+off,y+1,0));
      if (board.epx == x+off && board.epy == y-1)
        list.add(new Move(x,y,x+off,y-1,0));
    }
    return(list);
  } //LinkedList<Move> pawnMoves(int,int,boolean)

  /**
   * LinkedList<Move> knightMoves(int,int,boolean)
   * Returns a list containing all possible moves for a knight at (x,y).
  **/
  private LinkedList<Move> knightMoves(int x, int y, boolean wtm){
    LinkedList<Move> list = new LinkedList<Move>();
    if (board.canMoveTo(x+2,y+1,wtm)){ list.add(new Move(x,y,x+2,y+1,at(x+2,y+1))); }
    if (board.canMoveTo(x+2,y-1,wtm)){ list.add(new Move(x,y,x+2,y-1,at(x+2,y-1))); }
    if (board.canMoveTo(x-2,y+1,wtm)){ list.add(new Move(x,y,x-2,y+1,at(x-2,y+1))); }
    if (board.canMoveTo(x-2,y-1,wtm)){ list.add(new Move(x,y,x-2,y-1,at(x-2,y-1))); }
    if (board.canMoveTo(x+1,y+2,wtm)){ list.add(new Move(x,y,x+1,y+2,at(x+1,y+2))); }
    if (board.canMoveTo(x+1,y-2,wtm)){ list.add(new Move(x,y,x+1,y-2,at(x+1,y-2))); }
    if (board.canMoveTo(x-1,y+2,wtm)){ list.add(new Move(x,y,x-1,y+2,at(x-1,y+2))); }
    if (board.canMoveTo(x-1,y-2,wtm)){ list.add(new Move(x,y,x-1,y-2,at(x-1,y-2))); }
    return(list);
  } //LinkedList<Move> knightMoves(int,int,boolean)

  /**
   * LinkedList<Move> queenMoves(int,int,boolean)
   * Returns a list containing all possible moves for a queen at (x,y).
  **/
  private LinkedList<Move> queenMoves(int x, int y, boolean wtm){
    int i,j;
    LinkedList<Move> list = new LinkedList<Move>();
    for (i = 1; x+i < 8; i++){
      if (board.canMoveTo(x+i,y,wtm)){ list.add(new Move(x,y,x+i,y,at(x+i,y))); }
      if (board.isPiece(x+i,y)){ break; }
    } //move down
    for (i = 1; x-i >= 0; i++){
      if (board.canMoveTo(x-i,y,wtm)){ list.add(new Move(x,y,x-i,y,at(x-i,y))); }
      if (board.isPiece(x-i,y)){ break; }
    } //move up
    for (j = 1; y+j < 8; j++){
      if (board.canMoveTo(x,y+j,wtm)){ list.add(new Move(x,y,x,y+j,at(x,y+j))); }
      if (board.isPiece(x,y+j)){ break; }
    } //move right
    for (j = 1; y-j >= 0; j++){
      if (board.canMoveTo(x,y-j,wtm)){ list.add(new Move(x,y,x,y-j,at(x,y-j))); }
      if (board.isPiece(x,y-j)){ break; }
    } //move left
    for (i = 1, j = 1; x+i < 8 && y+j < 8; i++, j++){
      if (board.canMoveTo(x+i,y+j,wtm)){ list.add(new Move(x,y,x+i,y+j,at(x+i,y+j))); }
      if (board.isPiece(x+i,y+j)){ break; }
    } //move down+right
    for (i = 1, j = 1; x+i < 8 && y-j >= 0; i++, j++){
      if (board.canMoveTo(x+i,y-j,wtm)){ list.add(new Move(x,y,x+i,y-j,at(x+i,y-j))); }
      if (board.isPiece(x+i,y-j)){ break; }
    } //move down+left
    for (i = 1, j = 1; x-i >= 0 && y+j < 8; i++, j++){
      if (board.canMoveTo(x-i,y+j,wtm)){ list.add(new Move(x,y,x-i,y+j,at(x-i,y+j))); }
      if (board.isPiece(x-i,y+j)){ break; }
    } //move up+right
    for (i = 1, j = 1; x-i >= 0 && y-j >= 0; i++, j++){
      if (board.canMoveTo(x-i,y-j,wtm)){ list.add(new Move(x,y,x-i,y-j,at(x-i,y-j))); }
      if (board.isPiece(x-i,y-j)){ break; }
    } //move up+left
    return(list);
  } //LinkedList<Move> queenMoves(int,int,boolean)

  /**
   * LinkedList<Move> rookMoves(int,int,boolean)
   * Returns a list containing all possible moves for a rook at (x,y).
  **/
  private LinkedList<Move> rookMoves(int x, int y, boolean wtm){
    int i,j;
    LinkedList<Move> list = new LinkedList<Move>();
    for (i = 1; x+i < 8; i++){
      if (board.canMoveTo(x+i,y,wtm)){ list.add(new Move(x,y,x+i,y,at(x+i,y))); }
      if (board.isPiece(x+i,y)){ break; }
    } //move down
    for (i = 1; x-i >= 0; i++){
      if (board.canMoveTo(x-i,y,wtm)){ list.add(new Move(x,y,x-i,y,at(x-i,y))); }
      if (board.isPiece(x-i,y)){ break; }
    } //move up
    for (j = 1; y+j < 8; j++){
      if (board.canMoveTo(x,y+j,wtm)){ list.add(new Move(x,y,x,y+j,at(x,y+j))); }
      if (board.isPiece(x,y+j)){ break; }
    } //move right
    for (j = 1; y-j >= 0; j++){
      if (board.canMoveTo(x,y-j,wtm)){ list.add(new Move(x,y,x,y-j,at(x,y-j))); }
      if (board.isPiece(x,y-j)){ break; }
    } //move left
    return(list);
  } //LinkedList<Move> rookMoves(int,int,boolean)

  /**
   * LinkedList<Move> bishopMoves(int,int,boolean)
   * Returns a list containing all possible moves for a bishop at (x,y).
  **/
  private LinkedList<Move> bishopMoves(int x, int y, boolean wtm){
    int i,j;
    LinkedList<Move> list = new LinkedList<Move>();
    for (i = 1, j = 1; x+i < 8 && y+j < 8; i++, j++){
      if (board.canMoveTo(x+i,y+j,wtm)){ list.add(new Move(x,y,x+i,y+j,at(x+i,y+j))); }
      if (board.isPiece(x+i,y+j)){ break; }
    } //move down+right
    for (i = 1, j = 1; x+i < 8 && y-j >= 0; i++, j++){
      if (board.canMoveTo(x+i,y-j,wtm)){ list.add(new Move(x,y,x+i,y-j,at(x+i,y-j))); }
      if (board.isPiece(x+i,y-j)){ break; }
    } //move down+left
    for (i = 1, j = 1; x-i >= 0 && y+j < 8; i++, j++){
      if (board.canMoveTo(x-i,y+j,wtm)){ list.add(new Move(x,y,x-i,y+j,at(x-i,y+j))); }
      if (board.isPiece(x-i,y+j)){ break; }
    } //move up+right
    for (i = 1, j = 1; x-i >= 0 && y-j >= 0; i++, j++){
      if (board.canMoveTo(x-i,y-j,wtm)){ list.add(new Move(x,y,x-i,y-j,at(x-i,y-j))); }
      if (board.isPiece(x-i,y-j)){ break; }
    } //move up+left
    return(list);
  } //LinkedList<Move> bishopMoves(int,int,boolean)

  /**
   * LinkedList<Move> kingMoves(int,int,boolean)
   * Returns a list containing all possible moves for a king at (x,y).
   * The king is slightly more complex, so the 'in check' pruning is done in this function.
  **/
  private LinkedList<Move> kingMoves(int x, int y, boolean wtm){
    int i,j;
    LinkedList<Move> list = new LinkedList<Move>();
    for (i = -1; i <= 1; i++)
      for (j = -1; j <= 1; j++)
        if (board.canMoveTo(x+i,y+j,wtm))
          list.add(new Move(x,y,x+i,y+j,at(x+i,y+j)));

    //prune off moves that leave us in check - check for castling ability
    int idx = (wtm) ? 1 : 0;
    int p = kingpos[idx][0], q = kingpos[idx][1];
    int rank = (wtm) ? 0 : 7;
    int king = (wtm) ? KING : -KING;
    int piece;
    i = 0; Move m;
    while (i < list.size()){
      m = list.get(i);
      board.set(p,q,0);     //in order to avoid calling init(), just update the king's position
      piece = board.set(m.toX,m.toY,king);
      kingpos[idx][0] = m.toX; kingpos[idx][1] = m.toY;
      if (isInCheck(wtm))
        list.remove(i);
      else
        i++;
      board.set(p,q,king);  //restore the king's position
      board.set(m.toX,m.toY,piece);
    }
    kingpos[idx][0] = p;
    kingpos[idx][1] = q;

    if (!isInCheck(wtm)){
      if (board.castle(wtm,king_side) && !board.isPiece(rank,2) && !board.isPiece(rank,1) && inMoveSet(rank,2,list))
        list.add(new Move(x,y,rank,1,0)); //castle king side
      if (board.castle(wtm,queen_side) && !board.isPiece(rank,4) && !board.isPiece(rank,5) && !board.isPiece(rank,6) && inMoveSet(rank,4,list))
        list.add(new Move(x,y,rank,5,0)); //castle queen side
    }
    p = kingpos[idx][0]; q = kingpos[idx][1];
    i = 0;  //prune again - check for castling leaving us in check
    while (i < list.size()){
      m = list.get(i);
      board.set(p,q,0);
      piece = board.set(m.toX,m.toY,king);
      kingpos[idx][0] = m.toX; kingpos[idx][1] = m.toY;
      if (isInCheck(wtm))
        list.remove(i);
      else
        i++;
      board.set(p,q,king);
      board.set(m.toX,m.toY,piece);
    }
    kingpos[idx][0] = p;
    kingpos[idx][1] = q;
    return(list);
  } //LinkedList<Move> kingMoves(int,int,boolean)

  /**
   * LinkedList<Move> moveSet(int,int,boolean,Board)
   * Generates the move set for whatever piece is at (x,y).
  **/
  public LinkedList<Move> moveSet(int x, int y, boolean wtm, Board brd){
    init(brd);
    int piece = at(x,y);
    LinkedList<Move> moves = null;
    switch (piece){
      case PAWN:   case -PAWN:   moves = pawnMoves(x,y,wtm);   break;
      case KNIGHT: case -KNIGHT: moves = knightMoves(x,y,wtm); break;
      case BISHOP: case -BISHOP: moves = bishopMoves(x,y,wtm); break;
      case ROOK:   case -ROOK:   moves = rookMoves(x,y,wtm);   break;
      case QUEEN:  case -QUEEN:  moves = queenMoves(x,y,wtm);  break;
      case KING:   case -KING:   return(kingMoves(x,y,wtm));
      default:                   return(new LinkedList<Move>());
    }
    //prune moves off that lead us to being in check
    int i = 0; Move m;
    while (i < moves.size()){
      m = moves.get(i);
      init(brd);
      board.makeMove(m);
      if (isInCheck(wtm))
        moves.remove(i);
      else
        i++;
    }
    return(moves);
  } //LinkedList<Move> moveSet(int,int,boolean,Board)
}
