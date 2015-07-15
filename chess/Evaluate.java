/**
 * Evaluate.java
 * COSC 3P71 - Fluffy Bunny Chess
 * Taras Mychaskiw
 * Lachlan Plant
 * This class handles all heuristic evaluation of the board state.
**/

import java.awt.TextField;

public class Evaluate {
  //public boolean debug = false;

  /*
    First, define all the constants that the evaluation function will use.
    More information can be found in 'evaluation.txt'
    All these constants are the basically same for white and black, except the board
    is flipped around. Black starts at the bottom ranks, white at the top.
  */
  public int MAX_PLY = 4; //default 4 is pretty fast and decently smart
  private final int INVALID_SCORE = Integer.MAX_VALUE;
  private final int MAX_SCORE = 10000000; //score given to checkmate

  private final int black = 0, white = 1; //array locations for all below values
  private final int end_game_threshold = 17;

  private final int PAWN   = Board.PAWN;
  private final int KNIGHT = Board.KNIGHT;
  private final int BISHOP = Board.BISHOP;
  private final int ROOK   = Board.ROOK;
  private final int QUEEN  = Board.QUEEN;
  private final int KING   = Board.KING;

  /*
    For all the 'boards' below, black starts the BOTTOM, and white at the TOP.
    R N B K Q B N R
    P P P P P P P P
    ...............
    p p p p p p p p
    r n b k q b n r
  */
  /** KING **/
  private int[/*2*/][/*8*/] king_value =
  { {-10, 30,-30,-20,-40, 20,-20,-10},    //[black][file]
    {-10, 30,-30, 0 ,-40, 20,-20,-10} };  //[white][file]

  private final int queen_side = 0, king_side = 1, both = 2;
  private int[/*3*/][/*2*/][/*8*/][/*8*/] king_endgame_value = {
    { { {-50,-40,-20,-20,-20,-20,-20,-20},
        {-50,-40,-20, 0 , 0 , 0 , 0 , 0 },
        {-50,-40,-20, 20, 20, 20, 20, 20},  //[queen_side][white]
        {-50,-40,-20, 20, 30, 30, 30, 30},
        {-50,-40,-20, 20, 40, 40, 40, 30},
        {-50,-40,-20, 20, 40, 40, 40, 30},
        {-50,-40,-20, 20, 30, 30, 30, 30},
        {-50,-40,-20,-20,-20,-20,-20,-20} },

      { {-50,-40,-20,-20,-20,-20,-20,-20},
        {-50,-40,-20, 20, 30, 30, 30, 30},
        {-50,-40,-20, 20, 40, 40, 40, 30},  //[queen_side][black]
        {-50,-40,-20, 20, 40, 40, 40, 30},
        {-50,-40,-20, 20, 30, 30, 30, 30},
        {-50,-40,-20, 20, 20, 20, 20, 20},
        {-50,-40,-20, 0 , 0 , 0 , 0 , 0 },
        {-50,-40,-20,-20,-20,-20,-20,-20} } },   //queen_side

    { { {-20,-20,-20,-20,-20,-20,-40,-50},
        { 0 , 0 , 0 , 0 , 0 ,-20,-40,-50},
        { 20, 20, 20, 20, 20,-20,-40,-50},  //[king_side][white]
        { 30, 30, 30, 30, 20,-20,-40,-50},
        { 30, 40, 40, 40, 20,-20,-40,-50},
        { 30, 40, 40, 40, 20,-20,-40,-50},
        { 30, 30, 30, 30, 20,-20,-40,-50},
        {-20,-20,-20,-20,-20,-20,-40,-50} },

      { {-20,-20,-20,-20,-20,-20,-40,-50},
        { 30, 30, 30, 30, 20,-20,-40,-50},
        { 30, 40, 40, 40, 20,-20,-40,-50},  //[king_side][black]
        { 30, 40, 40, 40, 20,-20,-40,-50},
        { 30, 30, 30, 30, 20,-20,-40,-50},
        { 20, 20, 20, 20, 20,-20,-40,-50},
        { 0 , 0 , 0 , 0 , 20,-20,-40,-50},
        {-20,-20,-20,-20,-20,-20,-40,-50} } },  //king_side

    { { {-30,-30,-30,-30,-30,-30,-30,-30},
        {-30,-10,-10,-10,-10,-10,-10,-30},
        {-20,-10, 20, 20, 20, 20,-10,-30},  //[both][white]
        {-30,-10, 30, 30, 30, 30,-10,-30},
        {-30,-10, 40, 40, 40, 40,-10,-30},
        {-30,-10, 35, 35, 35, 35,-10,-30},
        {-30,-10,-10,-10,-10,-10,-10,-30},
        {-30,-30,-30,-30,-30,-30,-30,-30} },

      { {-30,-30,-30,-30,-30,-30,-30,-30},
        {-30,-10,-10,-10,-10,-10,-10,-30},
        {-30,-10, 35, 35, 35, 35,-10,-30},  //[both][black]
        {-30,-10, 40, 40, 40, 40,-10,-30},
        {-30,-10, 30, 30, 30, 30,-10,-30},
        {-20,-10, 20, 20, 20, 20,-10,-30},
        {-30,-10,-10,-10,-10,-10,-10,-30},
        {-30,-30,-30,-30,-30,-30,-30,-30} } }  //both
  };

  /** QUEEN **/
  private int[/*2*/][/*8*/][/*8*/] queen_value = {
    { { 0 , 0 , 0 , 0 , 10, 0 , 0 , 0 },
      { 0 , 0 , 3 , 3 , 3 , 3 , 0 , 0 },
      { 0 , 3 , 3 , 6 , 6 , 3 , 3 , 0 },  //white
      { 0 , 3 , 6 , 10, 10, 6 , 3 , 0 },
      { 0 , 3 , 6 , 10, 10, 6 , 3 , 0 },
      { 0 , 3 , 3 , 6 , 6 , 3 , 3 , 0 },
      { 0 , 0 , 3 , 3 , 3 , 3 , 0 , 0 },
      { 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 } },

    { { 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 },
      { 0 , 0 , 3 , 3 , 3 , 3 , 0 , 0 },
      { 0 , 3 , 3 , 6 , 6 , 3 , 3 , 0 },  //black
      { 0 , 3 , 6 , 10, 10, 6 , 3 , 0 },
      { 0 , 3 , 6 , 10, 10, 6 , 3 , 0 },
      { 0 , 3 , 3 , 6 , 6 , 3 , 3 , 0 },
      { 0 , 0 , 3 , 3 , 3 , 3 , 0 , 0 },
      { 0 , 0 , 0 , 0 , 10, 0 , 0 , 0 } }
  };

  /** ROOK **/
  private final int open_file = 0, half_file = 1, closed_file = 2;
  private int[/*3*/] rook_value = { 25, 10, 0 };
  private int rook_pig = 30;

  /** BISHOP **/
  private int bishop_wing_pawns = 25;

  private int[/*2*/][/*8*/][/*8*/] bishop_value = {
    { { 0 , 0 , 2 , 2 , 2 , 2 , 0 , 0 },
      { 0 , 8 , 6 , 8 , 8 , 6 , 8 , 0 },
      { 2 , 6 , 12, 10, 10, 12, 6 , 2 },  //black
      { 2 , 8 , 10, 16, 16, 10, 8 , 2 },
      { 2 , 8 , 10, 16, 16, 10, 8 , 2 },
      { 2 , 6 , 12, 10, 10, 12, 6 , 2 },
      { 0 , 8 , 6 , 8 , 8 , 6 , 8 , 0 },
      {-10,-10, -8, -6, -6, -8,-10,-10} },

    { {-10,-10, -8, -6, -6, -8,-10,-10},
      { 0 , 8 , 6 , 8 , 8 , 6 , 8 , 0 },
      { 2 , 6 , 12, 10, 10, 12, 6 , 2 },  //white
      { 2 , 8 , 10, 16, 16, 10, 8 , 2 },
      { 2 , 8 , 10, 16, 16, 10, 8 , 2 },
      { 2 , 6 , 12, 10, 10, 12, 6 , 2 },
      { 0 , 8 , 6 , 8 , 8 , 6 , 8 , 0 },
      { 0 , 0 , 2 , 2 , 2 , 2 , 0 , 0 } }
  };

  private int[/*2*/][/*8*/][/*8*/] bishop_outpost = {
    { { 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 },
      { 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 },
      { 0 , 1 , 2 , 2 , 2 , 2 , 1 , 0 },  //black
      { 0 , 3 , 5 , 5 , 5 , 5 , 3 , 0 },
      { 0 , 1 , 3 , 3 , 3 , 3 , 1 , 0 },
      { 0 , 0 , 1 , 1 , 1 , 1 , 0 , 0 },
      { 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 },
      { 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 } },

    { { 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 },
      { 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 },
      { 0 , 0 , 1 , 1 , 1 , 1 , 0 , 0 },  //white
      { 0 , 1 , 3 , 3 , 3 , 3 , 1 , 0 },
      { 0 , 3 , 5 , 5 , 5 , 5 , 3 , 0 },
      { 0 , 1 , 2 , 2 , 2 , 2 , 1 , 0 },
      { 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 },
      { 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 } }
  };

  /** KNIGHT **/
  private int[/*2*/][/*8*/][/*8*/] knight_value = {
    { {-30,-20,-20,-20,-20,-20,-20,-30},
      {-10, 3 , 3 , 3 , 3 , 3 , 3 ,-10},
      { 3 , 10, 15, 20, 20, 15, 10, 3 },  //black
      { 3 , 10, 20, 20, 20, 20, 10, 3 },
      { 3 , 10, 20, 25, 25, 20, 10, 3 },
      { 3 , 10, 25, 25, 25, 25, 10, 3 },
      {-10, 10, 15, 17, 17, 15, 10,-10},
      {-30,-40,-20,-10,-10,-20,-75,-30} },

    { {-30,-40,-20,-10,-10,-20,-60,-30},
      {-10, 10, 15, 17, 17, 15, 10,-10},
      { 3 , 10, 19, 25, 25, 19, 10, 3 },  //white
      { 3 , 10, 20, 25, 25, 20, 10, 3 },
      { 3 , 10, 20, 20, 20, 20, 10, 3 },
      { 3 , 10, 15, 20, 20, 15, 10, 3 },
      {-10, 3 , 3 , 3 , 3 , 3 , 3 ,-10},
      {-30,-20,-20,-20,-20,-20,-20,-30} }
  };

  private int[/*2*/][/*8*/][/*8*/] knight_outpost = {
    { { 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 },
      { 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 },
      { 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 },  //black
      { 0 , 1 , 3 , 3 , 3 , 3 , 1 , 0 },
      { 0 , 2 , 5 , 8 , 8 , 5 , 2 , 0 },
      { 0 , 1 , 3 , 3 , 3 , 3 , 1 , 0 },
      { 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 },
      { 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 } },

    { { 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 },
      { 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 },
      { 0 , 1 , 3 , 3 , 3 , 3 , 1 , 0 },  //white
      { 0 , 2 , 5 , 8 , 8 , 5 , 2 , 0 },
      { 0 , 1 , 3 , 3 , 3 , 3 , 1 , 0 },
      { 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 },
      { 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 },
      { 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 } }
  };

  /** PAWN **/
  private int doubled_pawns = 10;
  private int isolated_pawn = 20;

  private int[/*2*/][/*8*/][/*8*/] pawn_value = {
    { { 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 },
      { 50, 50, 50, 50, 50, 50, 50, 50},
      { 10, 10, 10, 30, 30, 10, 10, 10},  //black
      { 5 , 5 , 5 , 20, 20, 5 , 5 , 5 },
      { 3 , 3 , 3 , 10, 10, 3 , 3 , 3 },
      { 0 , 0 , 0 , 7 , 6 ,-15, 0 , 0 },
      { 0 , 0 , 0 , -5, -5, 0 , 0 , 0 },
      { 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 } },

    { { 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 },
      { 0 , 0 , 0 , -5, -5, 0 , 0 , 0 },
      { 0 , 0 , 0 , 7 , 6 , 0 , 0 , 0 },  //white
      { 3 , 3 , 3 , 10, 10, 3 , 3 , 3 },
      { 5 , 5 , 5 , 20, 20, 5 , 5 , 5 },
      { 10, 10, 10, 30, 30, 10, 10, 10},
      { 50, 50, 50, 50, 50, 50, 50, 50},
      { 0 , 0 , 0 , 0 , 0 , 0 , 0 , 0 } }
  };

  private Hash hash;

  private Board gBoard; //for use by evalScore "sub-functions" I guess you can call them
  /*
    order defines which in what order we will try to look for our pieces in an attempt to help
    out alpha-beta prune off more. Generally, moving a piece in the center of the board might be
    a better move than moving a piece in the corner. However, activating a piece in the corner
    may be better than moving an already semi-active piece nearish the center of the board.
  */
  private int[] order = { 3,4,5,2,6,1,7,0 };
  //private int[] order = { 3,4,7,0,6,1,5,2 };

  //declare evalScore() variables now to avoid reallocating them constantly
  private HashEntry entry;
  private int eval_score;
  private int white_score;
  private int black_score;
  private int idx;
  private boolean e_wtm;  //wtm for each spot on the board, for function calls like isOutpost()
  private int pawn_side;  //which side the pawns are on - for bishops and kings

  //in the same spirit as above, declare all the utility function variables as well
  private int off;        //pawn travel direction and colour multiplier
  private int pawnRank;   //starting rank for the pawns
  private int friendPAWN; //friendly PAWN value, ie -PAWN if black
  private int kingSide;   //number of king side pawns in pawnSide()
  private int queenSide;  //number of queen side pawns in pawnSide()
  private int numPawns;   //number of pawns in a file

  //public int evaluations; //number of eval() calls
  //public int scores;      //number of evalScore() evaluations
  //public int hashreads;   //number of reads from the hash table

  public Evaluate(){
    hash = new Hash();
  } //Evaluate()

  public Evaluate(int maxPly){
    this();
    MAX_PLY = maxPly;
  } //Evaluate()



  /********************************************************
   *                  Utility Functions                  *
   * boolean endGame()                                   *
   * boolean isOutpost(int,int,boolean)                  *
   * int fileType(int,boolean)                           *
   * boolean isPig(int,boolean)                          *
   * int pawnSide()                                      *
   * boolean isDoublePawn(int,boolean)                   *
   * boolean isIsolatedPawn(int,boolean)                 *
  ********************************************************/
  /**
   * boolean endGame()
   * Returns true if it is the end game.
   * When the end game starts is not clearly defined in all cases - let's just say half the pieces are gone.
  **/
  private boolean endGame(){
    return(gBoard.numPieces < end_game_threshold);
  } //boolean endGame()

  /**
   * boolean isOutpost(int,int,boolean)
   * Returns true if the piece at (x,y) is on an outpost.
   * An outpost is a square protected by a pawn.
  **/
  private boolean isOutpost(int x, int y, boolean wtm){
    off = (wtm) ? 1 : -1; //can use this for pawn direction and piece colour
    if ((!gBoard.offBoard(x+off,y+1) && gBoard.at(x+off,y+1) == off*PAWN) ||
        (!gBoard.offBoard(x+off,y-1) && gBoard.at(x+off,y-1) == off*PAWN))
      return(true);
    return(false);
  } //boolean isOutpost(int,int,boolean)

  /**
   * int fileType(int,boolean)
   * Returns whether file 'x' is 'open_file' or 'half_file' or 'closed_file'
   * open_file - no pawns of either colour
   * half_file - only pawns of opposing colour
   * closed_file - pawns of same colour
  **/
  private int fileType(int x, boolean wtm){
    friendPAWN = ((wtm) ? 1 : -1)*PAWN;
    for (int y = 1; y < 7; y++){  //don't scan the whole board, we can trim it slightly
      if (gBoard.at(x,y) == friendPAWN)
        return(closed_file);
      else if (gBoard.at(x,y) == -friendPAWN)  //enemy pawn
        return(half_file);
    }
    return(open_file);
  } //int fileType(int,boolean)

  /**
   * boolean isPig(int,boolean)
   * Returns true if this a rook on rank 'y' is a pig (7th rank).
  **/
  private boolean isPig(int y, boolean wtm){
    return((wtm && y == 6) || (!wtm && y == 1));
  } //boolean isPig(int,boolean)

  /**
   * int pawnSide()
   * Returns 'queen_side' or 'both' or 'king_side' - where the pawns are.
   * 'both' is a substitute for neither as well, since the center is always a good spot to be.
  **/
  private int pawnSide(){
    int x,y;
    queenSide = kingSide = 0;
    for (x = 0; x < 4; x++)
      for (y = 1; y < 7; y++)
        if (gBoard.at(x,y) == PAWN || gBoard.at(x,y) == -PAWN)
          queenSide++;
    for (x = 4; x < 8; x++)
      for (y = 1; y < 7; y++)
        if (gBoard.at(x,y) == PAWN || gBoard.at(x,y) == -PAWN)
          kingSide++;
    if (kingSide > 2 && queenSide < 3){ return(king_side); }
    if (queenSide > 2 && kingSide < 3){ return(queen_side); }
    return(both);
  } //int pawnSide()

  /**
   * boolean isDoublePawn(int,boolean)
   * Returns true if there is more than pawn of 'wtm' in the file 'x'
  **/
  private boolean isDoublePawn(int x, boolean wtm){
    friendPAWN = ((wtm) ? 1 : -1)*PAWN;
    numPawns = 0;
    for (int y = 1; y < 7; y++)
      if (gBoard.at(x,y) == friendPAWN)
        numPawns++;
    return(numPawns > 1);
  } //boolean isDoublePawn(int,boolean)

  /**
   * boolean isIsolatedPawn(int,boolean)
   * Returns true if the pawn in file 'x' is isolated - no friendly pawns in neighbouring files.
  **/
  private boolean isIsolatedPawn(int x, boolean wtm){
    int y;
    friendPAWN = ((wtm) ? 1 : -1)*PAWN;
    if (x-1 > 0)
      for (y = 1; y < 7; y++)
        if (gBoard.at(x-1,y) == friendPAWN){ return(false); }
    if (x+1 < 8)
      for (y = 1; y < 7; y++)
        if (gBoard.at(x+1,y) == friendPAWN){ return(false); }
    return(true);
  } //boolean isIsolatedPawn(int,boolean)



  /********************************************************
   *                Evaluation Functions                 *
   * int evalScore(Board  )                              *
   * int eval(boolean[,int,int,int])                     *
   * Move getMove(Board,boolean)                         *
  ********************************************************/
  /**
   * int evalScore(Board)
   * Score given to nodes at the end of the alpha-beta.
  **/
  private int evalScore(Board board){
    int i,j;
    gBoard = board;
    entry = hash.getEntry(gBoard);  //attempt to read from hash table
    if (entry != null){
      //hashreads++;
      return(entry.score);
    }

    eval_score = 0;
    black_score = white_score = 0;
    pawn_side = pawnSide();
    MoveSet ms = new MoveSet(); //for calculating mobility score

    for (i = 0; i < 8; i++)
      for (j = 0; j < 8; j++){
        idx = (gBoard.at(i,j) > 0) ? white : black;
        e_wtm = (idx == white); //who owns the piece at (i,j)

        //add the piece value for material scoring
        eval_score += gBoard.at(i,j);

        switch (gBoard.at(i,j)){
        case PAWN:
          white_score += pawn_value[idx][i][j];
          if (isDoublePawn(j,e_wtm))
            white_score -= doubled_pawns;
          if (isIsolatedPawn(j,e_wtm))
            white_score -= isolated_pawn;
          break;
        case -PAWN:
          black_score += pawn_value[idx][i][j];
          if (isDoublePawn(j,e_wtm))
            black_score -= doubled_pawns;
          if (isIsolatedPawn(j,e_wtm))
            black_score -= isolated_pawn;
          break;

        case KNIGHT:
          white_score += knight_value[idx][i][j];
          if (isOutpost(i,j,e_wtm))
            white_score += knight_outpost[idx][i][j];
          white_score += ms.moveSet(i,j,e_wtm,gBoard).size();
          break;
        case -KNIGHT:
          black_score += knight_value[idx][i][j];
          if (isOutpost(i,j,e_wtm))
            black_score += knight_outpost[idx][i][j];
          black_score += ms.moveSet(i,j,e_wtm,gBoard).size();
          break;

        case BISHOP:
          white_score += bishop_value[idx][i][j];
          if (isOutpost(i,j,e_wtm))
            white_score += bishop_outpost[idx][i][j];
          if (pawn_side == both)
            white_score += bishop_wing_pawns;
          white_score += ms.moveSet(i,j,e_wtm,gBoard).size();
          break;
        case -BISHOP:
          black_score += bishop_value[idx][i][j];
          if (isOutpost(i,j,e_wtm))
            black_score += bishop_outpost[idx][i][j];
          if (pawn_side == both)
            black_score += bishop_wing_pawns;
          black_score += ms.moveSet(i,j,e_wtm,gBoard).size();
          break;

        case ROOK:
          white_score += rook_value[fileType(j,e_wtm)];
          if (isPig(i,e_wtm))
            white_score += rook_pig;
          white_score += ms.moveSet(i,j,e_wtm,gBoard).size();
          break;
        case -ROOK:
          black_score += rook_value[fileType(j,e_wtm)];
          if (isPig(i,e_wtm))
            black_score += rook_pig;
          black_score += ms.moveSet(i,j,e_wtm,gBoard).size();
          break;

        case QUEEN:
          white_score += queen_value[idx][i][j];
          break;
        case -QUEEN:
          black_score += queen_value[idx][i][j];
          break;

        case KING:
          if (endGame())
            white_score += king_endgame_value[pawn_side][idx][i][j];
          else
            white_score += king_value[idx][j];
          break;
        case -KING:
          if (endGame())
            black_score += king_endgame_value[pawn_side][idx][i][j];
          else
            black_score += king_value[idx][j];
          break;
        }
      }
    //scores++;
    //eval_score has the material score, which is already negative for black
    eval_score = eval_score + white_score - black_score;
    hash.putEntry(gBoard,eval_score); //write this position to the hash table
    return(eval_score);
  } //int evalScore(Board)

  /**
   * int eval(Board,boolean[,int,int,int])
   * Alpha-beta pruning function. White maximizes alpha, while black minimizes beta.
   * For drawn games, a score of 0 is returned, meaning the board is perfectly even.
   * For checkmates, a score of MAX_SCORE-ply is returned. Why "-ply"? In completely
   * dominant positions, the AI will see very many checkmates. So the checkmate that is
   * closest (or furthest, if losing) is the best.
  **/
  private int eval(Board board, boolean wtm){
    return(eval(board,wtm,-INVALID_SCORE,INVALID_SCORE,1));
  } //eval(Board,boolean)
  private int eval(Board board, boolean wtm, int alpha, int beta, int ply){
    //evaluations++;
    if (board.getHalfMove() >= 50)
      return(0);  //drawn game, return perfectly even score
    if (ply >= MAX_PLY)
      return(evalScore(board));

    int i,j;
    int score = 0;
    boolean checkmate = true;
    Board brd = new Board(null);
    MoveSet ms = new MoveSet();
    for (i = 0; i < 8; i++)
      for (j = 0; j < 8; j++)
        if (board.isFriend(order[i],order[j],wtm))
          for (Move m : ms.moveSet(order[i],order[j],wtm,board)){
            checkmate = false;
            brd.copy(board);
            brd.makeMove(m);
            score = eval(brd,!wtm,alpha,beta,ply+1);
            if (wtm && score > alpha){ alpha = score; }
            if (!wtm && score < beta){ beta = score; }
            if (alpha >= beta){ return((wtm) ? alpha : beta); }
          }

    if (checkmate){
      if (new MoveSet().isInCheck(wtm,board))
        return((wtm) ? -MAX_SCORE+ply : MAX_SCORE-ply);
      return(0);  //return 0 for stalemate
    }
    return((wtm) ? alpha : beta);
  } //int eval(Board,boolean,int,int,int)

  /**
   * Move getMove(Board,boolean,TextField)
   * Returns the best move for a given position. Outputs progress to the TextField given.
  **/
  public Move getMove(Board board, boolean wtm, final TextField out){
    int i,j;
    int score;
    int min_score = INVALID_SCORE, max_score = -INVALID_SCORE;
    Move best = null;
    Board brd = new Board(null);
    MoveSet ms = new MoveSet();
    long t = System.currentTimeMillis();
    //if (debug){
    //  System.out.println("Evaluate.getMove() debug information... "+MAX_PLY+" ply");
    //  evaluations = scores = hashreads = 0;
    //}

    for (i = 0; i < 8; i++)
      for (j = 0; j < 8; j++)
        if (board.isFriend(order[i],order[j],wtm))
          for (Move m : ms.moveSet(order[i],order[j],wtm,board)){
            out.setText("testing move ("+m.fromY+","+m.fromX+") -> ("+m.toY+","+m.toX+")");
            brd.copy(board);
            brd.makeMove(m);
            score = eval(brd,!wtm);
            if (wtm && score > max_score){
              best = m;
              max_score = score;
            }
            if (!wtm && score < min_score){
              best = m;
              min_score = score;
            }
            //if (debug){
            //  System.out.print("move: ("+m.fromY+","+m.fromX+") -> ("+m.toY+","+m.toX+")");
            //  System.out.print("  score = "+score);
            //  System.out.println("  best = "+((wtm)?max_score:min_score));
            //}
          }
    //if (debug){
    //  System.out.println("move: ("+best.fromY+","+best.fromX+") -> ("+best.toY+","+best.toX+") "+best.piece+" captured");
    //  System.out.println(evaluations+" evaluations took "+(System.currentTimeMillis()-t)+"ms");
    //  System.out.println(scores+" evalScore() evaluations");
    //  System.out.println(hashreads+" hash table reads\n");
    //}
    out.setText("I took "+(System.currentTimeMillis()-t)+"ms.");
    return(best);
  } //Move getMove(Board,boolean,TextField)
}
