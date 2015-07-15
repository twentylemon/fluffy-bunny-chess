/**
 * HashEntry.java
 * COSC 3P71 - Fluffy Bunny Chess
 * Taras Mychaskiw
 * Lachlan Plant
 * Wrapper for a hash table entry. Contains the key and the score.
**/

public class HashEntry {
  public long key;  //see Hash.getKey() for details
  public int score; //result of Evaluate.evalScore()

  public HashEntry(long key, int score){
    this.key   = key;
    this.score = score;
  } //HashEntry(long,int)
}