/**
 * Hash.java
 * COSC 3P71 - Fluffy Bunny Chess
 * Taras Mychaskiw
 * Lachlan Plant
 * Very simple hash table. Zobrist keys are used. The hash table only prevents Evaluate.evalScore()
 * evaluations. Right now, a HASH_SIZE of 300k seems to speed up the evaluation by ~30%.
 * We will probably run out of time to actually improve the hash table, but 30% is a great start.
 * No collision handling is used either. If a key collides with a previous entry, the
 * older entry is just overwritten. With linear probing, the difference in speed was not noticeable,
 * so it was just removed. Below are some findings, using the opening board position as the start point.
 *
 *  HASH_SIZE  ply   time (ms)  ply   time (ms)  ply   time (ms)
 *         1    4      1607      5      6489      6     88015
 *       500    4      1825      5      6287      6     87080
 *      1000    4      1702      5      6302      6     87360
 *     10000    4      1560      5      5725      6     78094
 *     50000    4      1435      5      5382      6     70154
 *    100000    4      1435      5      5054      6     63523
 *    200000    4      1419      5      4836      6     59842
 *    300000    4      1404      5      4400      6     58157
 *    500000    4      1419      5      4478      6     55131
 *    750000    4      1404      5      4274      6     52681
 *   1000000    4      1420      5      4493      6     51012
 *   2500000    4      1404      5      4290      6     45989
 *   5000000    4      1372      5      4259      6     43089
 *  10000000    4      1466      5      4306      6     41527
 *  50000000    4      1622      5      4711      6     41870
 * 100000000    4      2075      5      5148      6     45039
 *
 * sizeof(HashEntry) in C++ gives 16bytes (HashEntry.cpp). I assume Java and C++ should use the
 * same amount of RAM. Including the pointer to the class in table[], that's 24bytes. So at 500k,
 * the maximum size of the hash table is ~12MB. sizeof(Hash) would be slightly larger, but not by much.
 * 5kk seems to be best performing at 6 ply (over a full game) - table size is ~115MB.
 * Odd how it climbs back up after. Must be to do with the garbage collector running more often.
 * Or maybe the OS starts using the page file instead of RAM.
**/

import java.util.Random;

public class Hash {

  private long[/*2*/][/*6*/][][] pieces;  //random values for pieces
  private long[/*2*/][/*4*/] castle;      //random of castling rights - [0] is both, [1] is king_side [2] queen_side [3] neither
  private long[][] ep;                    //en passant - needs only 16 really, but it's just easier like this

  private final int HASH_SIZE = 5000000;
  private HashEntry[] table;

  //same as in Evaluate, allocate the space for variables now instead of every function call
  private long key;     //the Zobrist key of a board state
  private int index;    //hash table index
  private int i,j,k,n;  //just counters
  private int piece;    //piece on the board when generating the key
  private int csl;      //castling availability index

  private final int black = 0, white = 1; //array locations for all below values
  private final int PAWN   = Board.PAWN;
  private final int KNIGHT = Board.KNIGHT;
  private final int BISHOP = Board.BISHOP;
  private final int ROOK   = Board.ROOK;
  private final int QUEEN  = Board.QUEEN;
  private final int KING   = Board.KING;
  private final int queen_side = Board.queen_side, king_side = Board.king_side;

  public Hash(){
    table = new HashEntry[HASH_SIZE];

    Random rand = new Random(); //Math.random() just uses Random.nextDouble() - skip the middle man
    pieces = new long[2][6][8][8];
    castle = new long[2][4];
    ep = new long[8][8];

    //first, initialize everything to a random long
    for (k = 0; k < 2; k++){
      for (n = 0; n < 6; n++)
        for (i = 0; i < 8; i++)
          for (j = 0; j < 8; j++)
            pieces[k][n][i][j] = rand.nextLong();
      for (n = 0; n < 4; n++)
        castle[k][n] = rand.nextLong();
    }
    for (i = 0; i < 8; i++)
      for (j = 0; j < 8; j++)
        ep[i][j] = rand.nextLong();
  } //Hash()

  /**
   * int idx(int)
   * Converts 'piece' into an index value for the 'pieces' array.
  **/
  private int idx(int piece){
    switch (piece){
    case KING:   case -KING:   return(0);
    case QUEEN:  case -QUEEN:  return(1);
    case ROOK:   case -ROOK:   return(2);
    case BISHOP: case -BISHOP: return(3);
    case KNIGHT: case -KNIGHT: return(4);
    case PAWN:   case -PAWN:   return(5);
    }
    return(-1); //can throw ArrayIndexOutOfBoundsException, but shouldn't ever
  } //int idx(int)

  /**
   * long getKey(Board)
   * Gets the Zobrist key representation of the board.
   * In Zobrist hashing, we first initialize a bunch of different board elements to random
   * 64bit numbers. - see constructor. We go through each of the elements in the actual
   * board. If the board is in some particular state, the key is XOR'd with the random number.
   * For example, if (x,y) contains a white rook, we XOR the key with the random value given to
   * piece[white][rook][x][y]. Doing this for all game aspects gives a unique enough key for any position.
   * 64bits isn't quite enough to create a unique key for every position, but collisions are
   * so rare that you don't even need to worry about them. It is very unlikely that two keys
   * will collide when going only 6 ply.
  **/
  public long getKey(Board board){
    key = 0;

    for (i = 0; i < 8; i++)
      for (j = 0; j < 8; j++){
        piece = board.at(i,j);
        if (piece > 0)
          key = key ^ pieces[white][idx(piece)][i][j];
        else if (piece < 0)
          key = key ^ pieces[black][idx(piece)][i][j];
      }

    csl = 3;  //start at castle neither
    if (board.castle(true,king_side)){ csl -= 2; }  //then subtract to get the correct index
    if (board.castle(true,queen_side)){ csl -= 1; }
    key = key ^ castle[white][csl];

    csl = 3;  //start at castle neither
    if (board.castle(false,king_side)){ csl -= 2; }  //then subtract to get the correct index
    if (board.castle(false,queen_side)){ csl -= 1; }
    key = key ^ castle[black][csl];

    if (board.isEp)
      key = key ^ ep[board.epx][board.epy];
    return(key);  //return key for debugging purposes
  } //long getKey(Board,boolean)

  /**
   * void putEntry(Board,int)
   * Puts a new board position and best move into the hashtable.
  **/
  public void putEntry(Board board, int score){
    getKey(board);  //updates the class wide 'key'
    index = (int)(key % (long)HASH_SIZE);
    if (index < 0){ index += HASH_SIZE; } //java has no primitive unsigned types... dumb
    table[index] = new HashEntry(key,score);
  } //void putEntry(Board,int)

  /**
   * HashEntry getEntry(Board)
   * Attempts to pull the entry from the hash table. Returns null on failure.
  **/
  public HashEntry getEntry(Board board){
    getKey(board);  //updates the class wide 'key'
    index = (int)(key % (long)HASH_SIZE);
    if (index < 0){ index += HASH_SIZE; }
    if (table[index] != null && table[index].key == key)
      return(table[index]);
    return(null);
  } //Move getEntry(Board)
}
