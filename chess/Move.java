/**
 * Move.java
 * COSC 3P71 - Fluffy Bunny Chess
 * Taras Mychaskiw
 * Lachlan Plant
 * Simple class that holds information about a move on the board.
 * (Move.fromX,Move.fromY) is where we are moving the piece from.
 * (Move.toX,Move.toY) is where the piece is moving to.
 * Move.piece is the piece we are capturing, 0 if no capture.
 * Move.promote is the piece this piece will be promoted to, 0 if not promoted.
**/

public class Move {
  public int fromX, fromY, toX, toY;
  public int piece;   //piece that was at (toX,toY)
  public int promote; //what this piece is being promoted to, 0 if not promoted

  public Move(int fromX, int fromY, int toX, int toY, int piece){
    this.fromX = fromX;
    this.fromY = fromY;
    this.toX   = toX;
    this.toY   = toY;
    this.piece = piece;
    promote = 0;
  } //Move(int,int,int,int,int)

  public Move(int fromX, int fromY, int toX, int toY, int piece, int promote){
    this.fromX   = fromX;
    this.fromY   = fromY;
    this.toX     = toX;
    this.toY     = toY;
    this.piece   = piece;
    this.promote = promote;
  } //Move(int,int,int,int,int,int)
}
