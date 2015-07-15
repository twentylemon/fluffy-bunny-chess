/**
 * Board.java
 * COSC 3P71 - Fluffy Bunny Chess
 * Taras Mychaskiw
 * Lachlan Plant
 * The actual board is a 2d int array. This class wraps other data,
 * such as ability to castle and the en-passant square.
**/

public class Board {
  private final int black = 0, white = 1;
  public static final int queen_side = 0, king_side = 1;

  private int[][] board;        //the actual board, +ive values are white, -ive are black
  public boolean[][] canCastle; //if we can castle king/queen side, for each colour
  public int epx, epy;          //(epx,epy) is the en-passant capture square
  public boolean isEp;          //true if (epx,epy) is active, ie is a legal move
  public int numPieces;         //the number of pieces on the board
  private int fullMove;         //the number of full moves (inc'd after black's turn)
  private int halfMove;         //number of half moves since the last capture or pawn movement

  private int from; //storage for makeMove() and undoMove()
  private int to;   //declare them globally to avoid redeclaring them all the time

  public static final int PAWN   = 100;
  public static final int KNIGHT = 300;
  public static final int BISHOP = 325;
  public static final int ROOK   = 500;
  public static final int QUEEN  = 900;
  public static final int KING   = 32767;

  /********************************************************
   *                    Constructors                     *
   * Board()                                             *
   * Board(Board)                                        *
   * void copy(Board)                                    *
  ********************************************************/
  /**
   * Board()
   * Default constructor - initializes the board to a new game state.
  **/
  public Board(){
    int i,j;
    board = new int[8][8];
    for(i = 0; i < 8; i++){
      board[1][i] = PAWN;
      board[6][i] = -PAWN;
    }
    board[7][0] = board[7][7] = -ROOK;
    board[0][0] = board[0][7] = ROOK;
    board[7][1] = board[7][6] = -KNIGHT;
    board[0][1] = board[0][6] = KNIGHT;
    board[7][2] = board[7][5] = -BISHOP;
    board[0][2] = board[0][5] = BISHOP;
    board[0][4] = QUEEN;
    board[7][4] = -QUEEN;
    board[7][3] = -KING;
    board[0][3] = KING;
    canCastle = new boolean[2][2];
    for (i = 0; i < 2; i++)
      for (j = 0; j < 2; j++)
        canCastle[i][j] = true;
    isEp = false;
    epx = epy = 0;
    numPieces = 32;
    fullMove = 1;
    halfMove = 0;
  } //Board()

  /**
   * Board(Board)
   * Initializes this board to be a copy of the board sent.
  **/
  public Board(Board b){
    board = new int[8][8];
    canCastle = new boolean[2][2];
    if (b != null)
      copy(b);
    else
      fullMove = 1;
  } //Board(Board)

  /**
   * void copy(Board)
   * Copies the contents of the board sent into this board.
  **/
  public void copy(Board b){
    int i,j;
    for (i = 0; i < 8; i++)
      for (j = 0; j < 8; j++)
        board[i][j] = b.at(i,j);
    for (i = 0; i < 2; i++)
      for (j = 0; j < 2; j++)
        canCastle[i][j] = b.canCastle[i][j];
    isEp = b.isEp;
    epx = b.epx;
    epy = b.epy;
    numPieces = b.numPieces;
    fullMove = b.fullMove;
    halfMove = b.halfMove;
  } //void copy(Board)



  /********************************************************
   *                  Moving Functions                   *
   * void makeMove(Move)                                 *
   * void undoMove(Move)                                 *
  ********************************************************/
  /**
   * void makeMove(Move)
   * Makes the move sent. Updates canCastle and (epx,epy) accordingly.
   * If Move.promote is non-zero, the piece moving becomes Move.promote.
  **/
  public void makeMove(Move move){
    to = board[move.toX][move.toY];
    from = board[move.fromX][move.fromY];
    isEp = false;

    halfMove++; //assume that the half move clock will increment
    if (move.piece != 0){
      numPieces--;  //a piece was captured
      halfMove = 0; //restart 50 turn rule
    }
    if (from < 0)
      fullMove++;   //black's move, a full turn has gone by

    if (from == PAWN){
      halfMove = 0; //restart 50 turn rule, pawn move
      if (move.fromX == 1 && move.toX == 3){  //a white pawn jumped 2 forward
        isEp = true;
        epx = 2;
        epy = move.fromY;
      }
      else if (isEp && move.toX == epx && move.toY == epy){ //we captured en-passant
        board[epx-1][epy] = 0;
        numPieces--;
      }
    }
    else if (from == -PAWN){
      halfMove = 0; //restart 50 turn rule, pawn move
      if (move.fromX == 6 && move.toX == 4){  //a black pawn jumped 2 forward
        isEp = true;
        epx = 5;
        epy = move.fromY;
      }
      else if (isEp && move.toX == epx && move.toY == epy){ //we captured en-passant
        board[epx+1][epy] = 0;
        numPieces--;
      }
    }
    else if (from == KING){
      if (move.fromX == 0 && move.fromY == 3){
        if (canCastle[white][king_side] && move.toX == 0 && move.toY == 1){
          board[0][0] = 0;      //white castle king side
          board[0][2] = ROOK;
        }
        if (canCastle[white][queen_side] && move.toX == 0 && move.toY == 5){
          board[0][7] = 0;      //white castle queen side
          board[0][4] = ROOK;
        }
      }
      canCastle[white][king_side] = canCastle[white][queen_side] = false;
    }
    else if (from == -KING){
      if (move.fromX == 7 && move.fromY == 3){
        if (canCastle[black][king_side] && move.toX == 7 && move.toY == 1){
          board[7][0] = 0;      //black castle king side
          board[7][2] = -ROOK;
        }
        if (canCastle[black][queen_side] && move.toX == 7 && move.toY == 5){
          board[7][7] = 0;      //black castle queen side
          board[7][4] = -ROOK;
        }
      }
      canCastle[black][king_side] = canCastle[black][queen_side] = false;
    }
    else if (from == ROOK && move.fromX == 0 && move.fromY == 0)
      canCastle[white][king_side] = false;  //king side rook moved, we can't castle
    else if (from == ROOK && move.fromX == 0 && move.fromY == 7)
      canCastle[white][queen_side] = false;
    else if (from == -ROOK && move.fromX == 7 && move.fromY == 0)
      canCastle[black][king_side] = false;
    else if (from == -ROOK && move.fromX == 7 && move.fromY == 7)
      canCastle[black][queen_side] = false;
    if (move.piece == ROOK){
      if (move.toX == 0 && move.toY == 0)
        canCastle[white][king_side] = false;
      else if (move.toX == 0 && move.toY == 7)
        canCastle[white][queen_side] = false;
    }
    else if (move.piece == -ROOK){
      if (move.toX == 7 && move.toY == 0)
        canCastle[black][king_side] = false;
      else if (move.toX == 7 && move.toY == 7)
        canCastle[black][queen_side] = false;
    }

    board[move.toX][move.toY] = from;
    if (move.promote != 0)
      board[move.toX][move.toY] = move.promote;
    board[move.fromX][move.fromY] = 0;
  } //void makeMove(Move)

  /**
   * void undoMove(Move)
   * Undos the move. Basically for debug use.
  **/
  public void undoMove(Move move){
    board[move.fromX][move.fromY] = board[move.toX][move.toY];
    board[move.toX][move.toY] = move.piece;
  } //void undoMove(Move)



  /********************************************************
   *           Accessor and Utility Functions            *
   * boolean offBoard(int,int)                           *
   * boolean isThisA(int,int,int)                        *
   * boolean isPiece(int,int)                            *
   * int at(int,int)                                     *
   * boolean isEnemy(int,int,boolean)                    *
   * boolean isFriend(int,int,boolean)                   *
   * boolean canMoveTo(int,int,boolean)                  *
   * boolean canCapture(int,int,boolean)                 *
   * boolean castle(boolean,int)                         *
   * int set(int,int,int)                                *
  ********************************************************/
  /**
   * boolean offBoard(int,int)
   * Returns true if (x,y) is outside of the board.
  **/
  public boolean offBoard(int x, int y){
    return(x < 0 || x >= 8 || y < 0 || y >= 8);
  } //boolean offBoard(int,int)

  /**
   * boolean isThisA(int,int,int)
   * Returns true if (x,y) contains 'piece' of either colour.
  **/
  public boolean isThisA(int x, int y, int piece){
    return(board[x][y] == piece || board[x][y] == -piece);
  } //boolean isThisA(int,int,int)

  /**
   * boolean isPiece(int,int)
   * Returns true if (x,y) contains any piece.
  **/
  public boolean isPiece(int x, int y){
    return(board[x][y] != 0);
  } //boolean isPiece(int,int)

  /**
   * int at(int,int)
   * Returns whatever is at (x,y).
  **/
  public int at(int x, int y){
    return(board[x][y]);
  } //int at(int,int)

  /**
   * boolean isEnemy(int,int,boolean)
   * Returns true if (x,y) holds an enemy piece.
  **/
  public boolean isEnemy(int x, int y, boolean wtm){
    return((wtm && board[x][y] < 0) || (!wtm && board[x][y] > 0));
  } //boolean isEnemy(int,int,boolean)

  /**
   * boolean isFriend(int,int,boolean)
   * Returns true if (x,y) holds a friendly piece.
  **/
  public boolean isFriend(int x, int y, boolean wtm){
    return((wtm && board[x][y] > 0) || (!wtm && board[x][y] < 0));
  } //boolean isFriend(int,int,boolean)

  /**
   * boolean canMoveTo(int,int,boolean)
   * Returns true if 'wtm' can place a piece at (x,y).
   * So true if board[x][y] is 0, or enemy piece.
  **/
  public boolean canMoveTo(int x, int y, boolean wtm){
    return(!offBoard(x,y) && !isFriend(x,y,wtm));
  } //boolean canMoveTo(int,int,boolean)

  /**
   * boolean canCapture(int,int,boolean)
   * Returns true if 'wtm' can move to (x,y) through capture only.
  **/
  public boolean canCapture(int x, int y, boolean wtm){
    return(!offBoard(x,y) && isEnemy(x,y,wtm));
  } //boolean canCapture(int,int,boolean)

  /**
   * boolean castle(boolean,int)
   * Returns true if 'wtm' can castle to 'side'
  **/
  public boolean castle(boolean wtm, int side){
    return(canCastle[(wtm) ? white : black][side]);
  } //boolean castle(boolean,int)

  /**
   * int getFullMove()
   * Returns the number of full turns that have passed in the game.
  **/
  public int getFullMove(){
    return(fullMove);
  } //int getFullMove()

  /**
   * int getHalfMove()
   * Returns the number of half turns since a capture or a pawn move.
  **/
  public int getHalfMove(){
    return(halfMove);
  } //int getHalfMove()

  /**
   * int set(int,int,int)
   * Changes the value of board at (x,y). Returns what was there before.
  **/
  public int set(int x, int y, int piece){
    int ret = board[x][y];
    board[x][y] = piece;
    return(ret);
  } //int set(int,int,int)



  /********************************************************
   *                    I/O Functions                    *
   * void print()                                        *
   * String toFEN(boolean)                               *
   * boolean fromFEN(String)                             *
  ********************************************************/
  /**
   * void print()
   * Prints the board as the int array, and the FEN representation assuming white to move.
  **/
  public void print(){
    int i,j;
    for (i = 0; i < 8; i++){
      for (j = 0; j < 8; j++)
        System.out.print(board[i][j]+"  ");
      System.out.println();
    }
    System.out.println("\n"+toFEN(true)+"\n");
  } //void print()

  /**
   * String toFEN(boolean)
   * Converts the board information into a FEN representation.
   * Since this isn't a huge part of the project, I won't go into details.
   * Go to the wiki page if you want to look up the details.
   * http://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation
  **/
  public String toFEN(boolean wtm){
    int rank, file;
    int empty;
    boolean write = false;
    String fen = "";

    //piece placement from white's perspective, starting from rank 8
    for (rank = 7; rank >= 0; rank--){
      //contents of each rank from files a to h
      empty = 0;  //empty squares are denoted a number - the number of blank in a row
      for (file = 7; file >= 0; file--){
        write = false;
        if (board[rank][file] != 0)
          write = true;
        if ((empty != 0 && write) || empty == 8){
          fen += empty;
          write = false;
          empty = 0;
        }
        switch (board[rank][file]){
        case KING:    fen += "K"; write = true; break;
        case -KING:   fen += "k"; write = true; break;
        case QUEEN:   fen += "Q"; write = true; break;
        case -QUEEN:  fen += "q"; write = true; break;
        case ROOK:    fen += "R"; write = true; break;
        case -ROOK:   fen += "r"; write = true; break;
        case BISHOP:  fen += "B"; write = true; break;
        case -BISHOP: fen += "b"; write = true; break;
        case KNIGHT:  fen += "N"; write = true; break;
        case -KNIGHT: fen += "n"; write = true; break;
        case PAWN:    fen += "P"; write = true; break;
        case -PAWN:   fen += "p"; write = true; break;
        default:      empty++;  write = false;  break;
        }
      }
      if (empty != 0){
        fen += empty;
        write = false;
        empty = 0;
      }
      fen += "/"; // '/' denotes a new rank
    }
    fen = fen.substring(0,fen.length()-1); //remove the final '/'

    fen += (wtm) ? " w " : " b ";   //add whose turn it is

    //castling availability
    boolean csl = false;
    if (canCastle[white][king_side]){  fen += "K"; csl = true; }
    if (canCastle[white][queen_side]){ fen += "Q"; csl = true; }
    if (canCastle[black][king_side]){  fen += "k"; csl = true; }
    if (canCastle[black][queen_side]){ fen += "q"; csl = true; }
    if (!csl)
      fen += "-";
    fen += " ";

    //en-passant target square
    if (isEp){
      char c = "abcdefgh".charAt(7-epy);
      fen += c;
      fen += (epx+1);
    }
    else
      fen += "-";

    //half move clock, then full move clock
    fen += " "+halfMove+" "+fullMove;
    return(fen);
  } //String toFEN()

  /**
   * boolean fromFEN(String)
   * Initializes this board from a FEN string. Returns 'true' if it's white's turn to move.
   * This would make sense as a constructor, but there is no way to retain who's turn it is.
   * Yes, there could be Board.wtm, but the rest of the project is already coded using wtm in
   * every other class, so I'm not gonna rewrite all them.
  **/
  public boolean fromFEN(String fen){
    board = new int[8][8];  //clear the board
    char c;
    int pos = 0, end = fen.indexOf(' ');  //space indicated the end of the board
    int rank = 7, file = 7;
    while (pos < end){
      c = fen.charAt(pos);  //get the next character
      pos++;
      switch (c){
      case 'K': board[rank][file] = KING;    file--;  break;
      case 'k': board[rank][file] = -KING;   file--;  break;
      case 'Q': board[rank][file] = QUEEN;   file--;  break;
      case 'q': board[rank][file] = -QUEEN;  file--;  break;
      case 'R': board[rank][file] = ROOK;    file--;  break;
      case 'r': board[rank][file] = -ROOK;   file--;  break;
      case 'B': board[rank][file] = BISHOP;  file--;  break;
      case 'b': board[rank][file] = -BISHOP; file--;  break;
      case 'N': board[rank][file] = KNIGHT;  file--;  break;
      case 'n': board[rank][file] = -KNIGHT; file--;  break;
      case 'P': board[rank][file] = PAWN;    file--;  break;
      case 'p': board[rank][file] = -PAWN;   file--;  break;
      case '/': rank--; file = 7; break;
      default:  file -= Character.getNumericValue(c);  break;
      }
    }

    //here, pos is 'end', which is the space
    pos++;
    c = fen.charAt(pos);  //'w' if white to move, 'b' if black
    boolean wtm = (c == 'w');

    pos += 2;
    end = fen.indexOf(' ',pos); //end of the castling portion
    while (pos < end){
      c = fen.charAt(pos);
      pos++;
      switch (c){
      case 'K': canCastle[white][king_side] = true;   break;
      case 'k': canCastle[black][king_side] = true;   break;
      case 'Q': canCastle[white][queen_side] = true;  break;
      case 'q': canCastle[black][queen_side] = true;  break;
      }
    }

    //en-passant square
    pos++;
    if (fen.charAt(pos) == '-')
      isEp = false;
    else {
      epy = 7 - "abcdefgh".indexOf(fen.charAt(pos));
      pos++;
      epx = Character.getNumericValue(fen.charAt(pos))-1;
    }
    pos += 2; //halfMove number
    String s;
    s = fen.substring(pos,fen.indexOf(" ",pos));
    halfMove = Integer.parseInt(s);
    pos = fen.indexOf(" ",pos)+1;
    s = fen.substring(pos);
    fullMove = Integer.parseInt(s);
    return(wtm);
  } //boolean fromFEN(String)
}
