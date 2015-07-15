/**
 * GUI.java
 * COSC 3P71 - Fluffy Bunny Chess
 * Taras Mychaskiw
 * Lachlan Plant
 * Defines the entry point into the program. Creates a nice GUI display for the board.
**/

import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.util.Scanner;
import java.util.LinkedList;

public class GUI extends JPanel {
  public boolean debug = false;

  private boolean selected;         //if a piece is selected
  private int selectedx;            //x of selected piece
  private int selectedy;            //y of selected piece
  private Move ai;                  //where the ai moves from/to
  private Move move;                //where the human moved - mainly for debug
  private MoveSet m;                //calculates move sets of selected piece
  private LinkedList<Move> moveset; //storage for move set of selected piece
  private boolean highlight;        //true -> display borders around selected piece

  private final int PAWN   = Board.PAWN;
  private final int KNIGHT = Board.KNIGHT;
  private final int BISHOP = Board.BISHOP;
  private final int ROOK   = Board.ROOK;
  private final int QUEEN  = Board.QUEEN;
  private final int KING   = Board.KING;

  private Board chess;        //the board state
  private Evaluate eve;       //ai main basically
  private boolean wtm;        //"white to move"
  private boolean humanMove;  //true if it's the human's turn. fancy.
  private final int black = 0, white = 1;
  private int humanColour;    //which colour the human is playing as
  private boolean playing;    //true if the game is not over yet
  private Thread aiThread;    //ai has it's own thread to prevent UI malfaunctions

  public int cell;    //size of a square on the board
  private int size;   //size of the whole board
  private int totalX; //total width of the window
  private int totalY; //total height of the window
  public Image bqueen,wqueen,bpawn,wpawn,bknight,wknight,bbishop,wbishop,brook,wrook,wking,bking;
  public Image blacksquare,whitesquare,redb,blueb,greenb;
  public Image playturn,aiturn,playcheck;

  public int settingx;      //(x,y) of selected square when creating a custom board state
  public int settingy;
  public boolean setting;   //true if we are creating a custom board state

  private FileWriter bfw;         //mainly for checking threefold repetition
  private BufferedWriter boardsW; //we write all positions seen so far into a file
  private File boardsOut;         //then see if we've seen any 3+ times, then stalemate

  private TextField aiOut;        //output for ai progress
  private JTextArea moves;        //output for the moves made so far

  public GUI(int max_ply){
    cell = 50;
    size = 8*cell;
    totalX = size+150;
    totalY = size;

    setPreferredSize(new Dimension(totalX,totalY));
    setBackground(Color.WHITE);
    humanColour = white;
    restart(max_ply);

    addMouseListener(new MyMouseListener());
    addKeyListener(new MyKeyListener());
    this.setFocusable(true);
    this.requestFocus();
    ImageUnpacker();
    highlight = true;
    repaint();
  } //GUI()



  /********************************************************
   *               Game Playing Functions                *
   * void forceEndAIThread()                             *
   * void restart([int])                                 *
   * void switchSides()                                  *
   * boolean inMoveSet(int,int)                          *
   * int promote()                                       *
   * void makeMove(Move)                                 *
   * void aiMove()                                       *
   * class MyMouseListener                               *
  ********************************************************/
  /**
   * void forceEndAIThread()
   * Forces the AI thread to end gracefully.
   * We achieve this by setting the max ply to 2, then just joining the thread.
  **/
  private void forceEndAIThread(){
    if (aiThread != null && aiThread.isAlive()){
      int ply = eve.MAX_PLY;
      eve.MAX_PLY = 2;
      try { aiThread.join(); } catch (Exception e){}
      eve.MAX_PLY = ply;
    }
  } //void forceEndAIThread()

  /**
   * void restart([int])
   * Starts a new game. If 'ply' is sent, sets a new MAX_PLY for Evaluate.
  **/
  private void restart(){
    playing = true;
    forceEndAIThread();
    moveset = null;
    ai = null;
    chess = new Board();
    m = new MoveSet();
    initWriters();
    wtm = true;
    if (moves != null)
      moves.setText("");
    if (aiOut != null)
      aiOut.setText("");
    humanMove = (humanColour == white);
    repaint();
    if (!humanMove && !debug)
      aiMove();
  } //void restart()
  private void restart(int ply){
    eve = new Evaluate(ply);
    aiThread = new Thread();
    restart();
  } //void restart(int)

  /**
   * void switchSides()
   * Switches the human player's colour and starts a new game.
  **/
  private void switchSides(){
    if (humanColour == white)
      humanColour = black;
    else
      humanColour = white;
    restart();
  } //void switchSides()

  /**
   * boolean inMoveSet(int,int)
   * Returns true if (x,y) is a possible move for the selected piece.
  **/
  private boolean inMoveSet(int x, int y){
    for (Move i : moveset)
      if (x == i.toX && y == i.toY)
        return(true);
    return(false);
  } //boolean inMoveSet(int,int)

  /**
   * int promote()
   * Creates a option pane for the user, prompting for which piece to promote their pawn to.
  **/
  private int promote(){
    String pieces[] = new String[4];
    pieces[0] = "Knight";
    pieces[1] = "Bishop";
    pieces[2] = "Rook";
    pieces[3] = "Queen";
    String s = (String)JOptionPane.showInputDialog(this,"Pick a piece to promote to","Promotion",JOptionPane.QUESTION_MESSAGE,null,pieces,pieces[3]);
    if (debug)
      System.out.println("Promoting this piece to a "+((s==null)?"Queen":s));
    if (s == null){ return(QUEEN); }
    if (s.equals(pieces[0])){ return(KNIGHT); }
    if (s.equals(pieces[1])){ return(BISHOP); }
    if (s.equals(pieces[2])){ return(ROOK); }
    return(QUEEN);
  } //int promote()

  /**
   * void makeMove(Move)
   * Makes the human player's move. Starts the AI's move.
  **/
  private void makeMove(Move move){
    if (!humanMove){ return; }
    if (chess.at(move.fromX,move.fromY) == PAWN && move.toX == 7){ move.promote = promote(); }
    if (chess.at(move.fromX,move.fromY) == -PAWN && move.toX == 0){ move.promote = -promote(); }
    chess.makeMove(move);
    humanMove = debug;
    wtm = !wtm;
    selected = false;
    ai = null;
    if (debug){
      System.out.println("en-passant square = ("+chess.epy+","+chess.epx+") "+((chess.isEp)?"on":"off"));
      System.out.println("white castle  king_side = "+chess.canCastle[1][1]+"  queen_side = "+chess.canCastle[1][0]);
      System.out.println("black castle  king_side = "+chess.canCastle[0][1]+"  queen_side = "+chess.canCastle[0][0]);
      System.out.println();
    }
    repaint();
    playing = stillPlaying();
    writeMove(move);
    if (!humanMove && playing)
      aiMove();
  } //void makeMove(Move)

  /**
   * void aiMove()
   * Makes the AI's move. See Evaluate for more details.
   * If the null pointer is caught, the AI has no moves, and it's checkmate or stalemate.
  **/
  private void aiMove(){
    if (!playing){ return; }
    humanMove = false;
    if (!aiThread.isAlive()){
      aiThread = new Thread(){
        public void run(){
          ai = eve.getMove(chess,wtm,aiOut);
          try {
            chess.makeMove(ai);
          } catch (NullPointerException e){}
          wtm = !wtm;
          humanMove = true;
          SwingUtilities.invokeLater(new Runnable(){
            public void run(){
              writeMove(ai);
              repaint();
            }
          });
          playing = stillPlaying();
        }
      };
      aiThread.start();
    }
  } //void aiMove()

  /**
   * class MyMouseListener
   * Handles clicky-clicks. Damn mice.
   * If no piece is selected, the clicked piece is selected.
   * If a piece is selected, the piece selected is moved to the clicked square if that move is legal.
   * If we are making a custom board, the square clicked is selected.
  **/
  private class MyMouseListener implements MouseListener {
    public void mouseClicked(MouseEvent e){
      int x = boardPos(e.getY());
      int y = boardPos(e.getX());
      if (!playing || chess.offBoard(x,y)){ return; }
      if (setting){ //in custom board set up
        settingx = x;
        settingy = y;
      }
      else {
        if (!selected){
          if (chess.isFriend(x,y,wtm)){
            selectedx = x;
            selectedy = y;
            selected = true;
            moveset = m.moveSet(x,y,wtm,chess);
          }
        }
        else if (x == selectedx && y == selectedy){
          selected = false;
        }
        else if (selected){
          if (inMoveSet(x,y)){
            move = new Move(selectedx,selectedy,x,y,chess.at(x,y));
            makeMove(move);
          }
          selected = false;
        }
      }
      repaint();
    } //void MyMouseListener.mouseClicked(MouseEvent)

    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
  } //class MyMouseListener



  /********************************************************
   *                  Game End Handlers                  *
   * boolean threeFold(String)                           *
   * boolean fiftyMoveRule()                             *
   * boolean lackOfMaterial()                            *
   * boolean stillPlaying()                              *
   * void resign()                                       *
  ********************************************************/
  /**
   * boolean threeFold(String)
   * Checks this board for threefold repetition.
  **/
  private boolean threeFold(String fen){
    int count = 0;
    try {
      Scanner s = new Scanner(boardsOut);
      String st;
      while (s.hasNext()){
        st = s.next();
        s.nextLine();
        if (fen.equals(st))
          count++;
      }
    } catch(Exception e){}
    return(count >= 3);
  } //boolean threeFold(String)

  /**
   * boolean fiftyMoveRule()
   * Returns true if there hasn't been a pawn move or capture in 50 turns.
  **/
  private boolean fiftyMoveRule(){
    return(chess.getHalfMove() >= 50);
  } //boolean fiftyMoveRule()

  /**
   * boolean lackOfMaterial()
   * Returns true if there is not enough material to win the game.
  **/
  private boolean lackOfMaterial(){
    int i,j;
    int piece;
    int white_material = 0, black_material = 0;
    boolean white_pawn = false, black_pawn = false;
    //scan through the board, looking for pawns and total material worth
    for (i = 0; i < 8; i++)
      for (j = 0; j < 8; j++){
        piece = chess.at(i,j);
        switch (piece){
          case PAWN: white_pawn = true;
          case KNIGHT: case BISHOP: case ROOK: case QUEEN:
            white_material += piece; break;
          case -PAWN: black_pawn = true;
          case -KNIGHT: case -BISHOP: case -ROOK: case -QUEEN:
            black_material -= piece; break;
        }
      }
    //if either side has a pawn or at least 2 pieces or a rook, game continues
    return(!(white_pawn || black_pawn || white_material >= 500 || black_material >= 500));
  } //boolean lackOfMaterial()

  /**
   * boolean stillPlaying()
   * Checks for and handles all wins/draws.
  **/
  private boolean stillPlaying(){
    //first, write the board state to file
    String str = chess.toFEN(wtm);
    str = str.substring(0,str.indexOf(" "));
    writeBoard(str);

    //check if 'wtm' has any moves
    int i,j;
    boolean noMoves = true;
    for (i = 0; i < 8; i++)
      for (j = 0; j < 8; j++)
        if (chess.isFriend(i,j,wtm))
          for (Move mm : m.moveSet(i,j,wtm,chess))
            noMoves = false;

    //if they had no moves, check if it's checkmate or stalemate
    if (noMoves){
      if (m.isInCheck(wtm,chess)){
        if (humanMove)
          JOptionPane.showMessageDialog(this,"Way to botch the job, ace.","USELESS!",JOptionPane.INFORMATION_MESSAGE);
        else
          JOptionPane.showMessageDialog(this,"Your foe is vanquished!","VICTORY!",JOptionPane.INFORMATION_MESSAGE);
      }
      else
        JOptionPane.showMessageDialog(this,"Draw game by forced stalemate.","STALEMATE!",JOptionPane.INFORMATION_MESSAGE);
      return(false);
    }

    //check for the other draw games
    if (fiftyMoveRule()){
      JOptionPane.showMessageDialog(this,"Stalemate by fifty move rule.","STALEMATE!",JOptionPane.INFORMATION_MESSAGE);
      return(false);
    }
    if (threeFold(str)){
      JOptionPane.showMessageDialog(this,"Stalemate by threefold repitition.","STALEMATE!",JOptionPane.INFORMATION_MESSAGE);
      return(false);
    }
    if (lackOfMaterial()){
      JOptionPane.showMessageDialog(this,"Stalemate by lack of material.","STALEMATE!",JOptionPane.INFORMATION_MESSAGE);
      return(false);
    }
    return(true);
  } //boolean stillPlaying()

  /**
   * void resign()
   * Handles the human player resigning.
  **/
  private void resign(){
    playing = false;
    forceEndAIThread();
    JOptionPane.showMessageDialog(this,"You could have done that ages ago.","WASTE OF TIME!",JOptionPane.INFORMATION_MESSAGE);
  } //void resign()



  /********************************************************
   *              Customization Functions                *
   * void customBoard()                                  *
   * void set(int,int,int)                               *
   * void setPly(int)                                    *
  ********************************************************/
  /**
   * void customBoard()
   * Create a new empty & custom board.
  **/
  private void customBoard(){
    restart();
    chess = new Board(null);
    m = new MoveSet();
    moveset = null;
    ai = null;
    playing = true;
    humanMove = true;
    wtm = true;
    setting = true;
    repaint();
  } //void customBoard(int)

  /**
   * void set(int,int,int)
   * Changes the board - (x,y) becomes 'piece'
  **/
  public void set(int x, int y, int piece){
    chess.set(x,y,piece);
    repaint();
  } //void set(int,int,int)

  /**
   * void setPly(int)
   * Changes ply of the negamax algorithm.
  **/
  private void setPly(int ply){
    eve.MAX_PLY = ply;
  } //void setPly(int)



  /********************************************************
   *                   Writer Handlers                   *
   * void initWriters()                                  *
   * void writeBoard(String)                             *
   * void writeMove(Move)                                *
   * void load()                                         *
   * void save()                                         *
  ********************************************************/
  /**
   * void initWriters()
   * Recreates all the file handing objects.
  **/
  private void initWriters(){
    try {
      boardsOut = File.createTempFile("history",".txt");
      boardsOut.deleteOnExit();
      bfw = new FileWriter(boardsOut.getAbsoluteFile());
      boardsW = new BufferedWriter(bfw);
    }
    catch (Exception e){ System.out.println("Issue with file writing. Deal with it"); }
  } //void initWriters()

  /**
   * void writeBoard(String)
   * Saves the reduced FEN representation of the board in the output file.
   * This is mainly for checking threefold repetition.
  **/
  private void writeBoard(String reducedFEN){
    try {
      boardsW.write(reducedFEN);
      boardsW.newLine();
      boardsW.flush();
    } catch(Exception e){}
  } //void writeBoard(String)

  /**
   * void writeMove(Move)
   * Writes the move made to the move output file. This keeps the history of the game played,
   * should the user want it "for study."
  **/
  private void writeMove(Move move){
    try {
      char c = "abcdefgh".charAt(7-move.fromY);
      char h = "abcdefgh".charAt(7-move.toY);
      String s = Character.toString(c)+(move.fromX+1)+" - "+Character.toString(h)+(move.toX+1)+"\t";
      if (!wtm){
        int f = chess.getFullMove();
        String q = f+".  ";
        if (f < 10)
          q += "  ";
        q += s+"  ";
        moves.setText(moves.getText()+q.replace("\t","  "));
      }
      else
        moves.setText(moves.getText()+s.replace("\t","  ")+"\n");
    } catch (Exception e){}
  } //void writeMove(Move)

  /**
   * void load()
   * Loads a board state from a file containing the FEN representation.
  **/
  private void load(){
    Saves save = new Saves();
    try {
      String[] ops = save.getNames();
      String s = (String)JOptionPane.showInputDialog(this,"Choose a file to load","Load",JOptionPane.QUESTION_MESSAGE,null,ops,ops[0]);
      String fen = save.load(s);
      wtm = chess.fromFEN(fen);
      humanMove = false;
      ai = null;
      if (humanColour == white && wtm)
        humanMove = true;
      if (!humanMove)
        aiMove();
    }
    catch(Exception e){ JOptionPane.showMessageDialog(this,"No saved games to load.","ERROR!",JOptionPane.INFORMATION_MESSAGE); }
    repaint();
  } //void load()

  /**
   * void save()
   * Saves this game to a file.
  **/
  private void save(){
    Saves save = new Saves();
    String ans = JOptionPane.showInputDialog(this,"Enter the save file name.");
    save.save(ans,chess.toFEN(wtm));
  } //void save()



  /********************************************************
   *                         GUI                         *
   * int cellPos(int)                                    *
   * int boardPos(int)                                   *
   * void paintComponent(Graphics)                       *
   * void ImageUnpacker()                                *
   * class MyKeyListener                                 *
  ********************************************************/
  /**
   * int cellPos(int)
   * Translates 'c' from board coordinates to cell positioning in pixels.
  **/
  private int cellPos(int c){
    if (humanColour == white)
      return(size-cell*(c+1));
    return(c*cell);
  } //int cellPos(int,int)

  /**
   * int boardPos(int)
   * Translates 'p' from cell positioning to board coordinates.
  **/
  private int boardPos(int p){
    if (humanColour == white)
      return(7-p/cell);
    return(p/cell);
  } //int boardPos(int)

  /**
   * void paintComponent(Graphics)
   * This gets called by JFrame.repaint(). Defines how to repaint the board.
  **/
  public void paintComponent(Graphics g){
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
    boolean b = true; //true means paint a white square
    int x,y;
    for(x = 0; x < 8; x++){
      for (y = 0; y < 8; y++){  //draw the squares
        if (b){ g2.drawImage(whitesquare,cellPos(x),cellPos(y),this); }
        else  { g2.drawImage(blacksquare,cellPos(x),cellPos(y),this); }
        b = !b;
      }
      b = !b;
    }

    if (!humanMove)
      g2.drawImage(aiturn,8*cell,0,this); //display 'computer thinking'
    else {
      if (m.isInCheck(wtm,chess))
        g2.drawImage(playcheck,8*cell,0,this);  //tell them they are in check
      else
        g2.drawImage(playturn,8*cell,0,this);   //otherwise, tell them it's their move
    }

    for (x = 0; x < 8; x++)
      for (y = 0; y < 8; y++)
        switch (chess.at(y,x)){ //draw all the pieces
        case PAWN:    g2.drawImage(wpawn,cellPos(x),cellPos(y),this);   break;
        case -PAWN:   g2.drawImage(bpawn,cellPos(x),cellPos(y),this);   break;
        case QUEEN:   g2.drawImage(wqueen,cellPos(x),cellPos(y),this);  break;
        case -QUEEN:  g2.drawImage(bqueen,cellPos(x),cellPos(y),this);  break;
        case KING:    g2.drawImage(wking,cellPos(x),cellPos(y),this);   break;
        case -KING:   g2.drawImage(bking,cellPos(x),cellPos(y),this);   break;
        case ROOK:    g2.drawImage(wrook,cellPos(x),cellPos(y),this);   break;
        case -ROOK:   g2.drawImage(brook,cellPos(x),cellPos(y),this);   break;
        case KNIGHT:  g2.drawImage(wknight,cellPos(x),cellPos(y),this); break;
        case -KNIGHT: g2.drawImage(bknight,cellPos(x),cellPos(y),this); break;
        case BISHOP:  g2.drawImage(wbishop,cellPos(x),cellPos(y),this); break;
        case -BISHOP: g2.drawImage(bbishop,cellPos(x),cellPos(y),this); break;
        }

    if (ai != null){  //draw the ai move if any
      g2.drawImage(greenb,cellPos(ai.fromY),cellPos(ai.fromX),this);
      g2.drawImage(greenb,cellPos(ai.toY),cellPos(ai.toX),this);
    }
    if (setting)  //if in customization, indicate which square is selected
      g2.drawImage(greenb,cellPos(settingy),cellPos(settingx),this);
    if (selected) //indicated selected piece if in game
      g2.drawImage(redb,cellPos(selectedy),cellPos(selectedx),this);
    if (highlight && selected)
      for (Move m : moveset){
        y = m.toX;
        x = m.toY;  //highlight the moveset of selected piece
        g2.drawImage(blueb,cellPos(x),cellPos(y),this);
      }
    moves.setText(moves.getText()); //text disappears on click for some reason
  } //void paintComponent(Graphics)

  /**
   * void ImageUnpacker()
   * Image loader. Fun stuff... fun stuff.
  **/
  private void ImageUnpacker(){
    bqueen  = Toolkit.getDefaultToolkit().getImage("images/pieces/black_queen.png");
    bpawn   = Toolkit.getDefaultToolkit().getImage("images/pieces/black_pawn.png");
    bknight = Toolkit.getDefaultToolkit().getImage("images/pieces/black_knight.png");
    brook   = Toolkit.getDefaultToolkit().getImage("images/pieces/black_rook.png");
    bking   = Toolkit.getDefaultToolkit().getImage("images/pieces/black_king.png");
    bbishop = Toolkit.getDefaultToolkit().getImage("images/pieces/black_bishop.png");

    wqueen  = Toolkit.getDefaultToolkit().getImage("images/pieces/white_queen.png");
    wpawn   = Toolkit.getDefaultToolkit().getImage("images/pieces/white_pawn.png");
    wknight = Toolkit.getDefaultToolkit().getImage("images/pieces/white_knight.png");
    wrook   = Toolkit.getDefaultToolkit().getImage("images/pieces/white_rook.png");
    wking   = Toolkit.getDefaultToolkit().getImage("images/pieces/white_king.png");
    wbishop = Toolkit.getDefaultToolkit().getImage("images/pieces/white_bishop.png");

    blacksquare = Toolkit.getDefaultToolkit().getImage("images/board/black_square.png");
    whitesquare = Toolkit.getDefaultToolkit().getImage("images/board/white_square.png");
    blueb       = Toolkit.getDefaultToolkit().getImage("images/blue_border.png");
    redb        = Toolkit.getDefaultToolkit().getImage("images/red_border.png");
    greenb      = Toolkit.getDefaultToolkit().getImage("images/green_border.png");

    playturn  = Toolkit.getDefaultToolkit().getImage("images/board/player_turn.png");
    aiturn    = Toolkit.getDefaultToolkit().getImage("images/board/ai_turn.png");
    playcheck = Toolkit.getDefaultToolkit().getImage("images/board/player_turn_check.png");

    bqueen  = bqueen.getScaledInstance(cell,cell,Image.SCALE_AREA_AVERAGING);
    bpawn   = bpawn.getScaledInstance(cell,cell,Image.SCALE_AREA_AVERAGING);
    brook   = brook.getScaledInstance(cell,cell,Image.SCALE_AREA_AVERAGING);
    bknight = bknight.getScaledInstance(cell,cell,Image.SCALE_AREA_AVERAGING);
    bbishop = bbishop.getScaledInstance(cell,cell,Image.SCALE_AREA_AVERAGING);
    bking   = bking.getScaledInstance(cell,cell,Image.SCALE_AREA_AVERAGING);

    wqueen  = wqueen.getScaledInstance(cell,cell,Image.SCALE_AREA_AVERAGING);
    wpawn   = wpawn.getScaledInstance(cell,cell,Image.SCALE_AREA_AVERAGING);
    wrook   = wrook.getScaledInstance(cell,cell,Image.SCALE_AREA_AVERAGING);
    wknight = wknight.getScaledInstance(cell,cell,Image.SCALE_AREA_AVERAGING);
    wbishop = wbishop.getScaledInstance(cell,cell,Image.SCALE_AREA_AVERAGING);
    wking   = wking.getScaledInstance(cell,cell,Image.SCALE_AREA_AVERAGING);

    blacksquare = blacksquare.getScaledInstance(cell,cell,Image.SCALE_AREA_AVERAGING);
    whitesquare = whitesquare.getScaledInstance(cell,cell,Image.SCALE_AREA_AVERAGING);
    blueb       = blueb.getScaledInstance(cell,cell,Image.SCALE_AREA_AVERAGING);
    redb        = redb.getScaledInstance(cell,cell,Image.SCALE_AREA_AVERAGING);
    greenb      = greenb.getScaledInstance(cell,cell,Image.SCALE_AREA_AVERAGING);

    playturn  = playturn.getScaledInstance(150,75,Image.SCALE_AREA_AVERAGING);
    aiturn    = aiturn.getScaledInstance(150,75,Image.SCALE_AREA_AVERAGING);
    playcheck = playcheck.getScaledInstance(150,75,Image.SCALE_AREA_AVERAGING);
  } //void ImageUnpacker()

  /**
   * class MyKeyListener
   * Keyboard handler for debugging (mainly).
   *  a - force AI move
   *  b - print board as integer values to System.out
   *  m - print moveset to System.out
   *  z - undo human move - is not a full undo, just moves the piece back
  **/
  private class MyKeyListener implements KeyListener {
    public void keyPressed(KeyEvent e){
      if (e.getKeyCode() == KeyEvent.VK_B)
        chess.print();
      if (e.getKeyCode() == KeyEvent.VK_M){
        System.out.println("MoveSet.moveSet debug information... ("+selectedy+","+selectedx+")");
        if (moveset != null)
          for (Move m : moveset)
            System.out.println("move: ("+m.fromX+","+m.fromY+") -> ("+m.toX+","+m.toY+")");
        System.out.println();
      }
      if (e.getKeyCode() == KeyEvent.VK_Z){
        chess.undoMove(move);
        wtm = !wtm;
        repaint();
      }
      if (e.getKeyCode() == KeyEvent.VK_A)
        aiMove();
    } //void MyKeyListener.keyPressed(KeyEvent)

    public void keyReleased(KeyEvent e){}
    public void keyTyped(KeyEvent e){}
  } //class MyKeyListener



  /********************************************************
   *                        main                         *
   * JMenuBar menuBar(GUI)                               *
   * void main(String[])                                 *
  ********************************************************/
  /**
   * JMenuBar menuBar(GUI)
   * Returns the full menu bar for the main application.
  **/
  private static JMenuBar menuBar(final GUI gui){
    JMenuBar menu = new JMenuBar();
    final JFrame fb = new JFrame("Customizer");

    JMenu game = new JMenu("Game");
    JMenuItem new_game = new JMenuItem("New Game");
    JMenuItem resign = new JMenuItem("Concede Defeat");
    JMenuItem switch_sides = new JMenuItem("Switch Sides");
    JMenuItem save_game = new JMenuItem("Save Game");
    JMenuItem load_game = new JMenuItem("Load Game");
    fb.addWindowListener(new WindowAdapter(){
      public void windowClosing(WindowEvent evt){
        gui.setting = false;
        gui.repaint();
      }
    });

    new_game.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent ae){ gui.restart(); }
    });
    game.add(new_game);

    resign.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent ae){ gui.resign(); }
    });
    game.add(resign);

    switch_sides.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent ae){ gui.switchSides(); }
    });
    game.add(switch_sides);

    save_game.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent ae){ gui.save(); }
    });
    game.add(save_game);

    load_game.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent ae){ gui.load(); }
    });
    game.add(load_game);

    JMenu ai = new JMenu("AI");
    JMenuItem change_ply = new JMenuItem("Change Max Ply");
    JMenuItem force_move = new JMenuItem("Force AI Move");
    JMenuItem turn_off = new JMenuItem("Turn On/Off");

    change_ply.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent ae){
        String ans = JOptionPane.showInputDialog(gui,"Enter new max ply.\nIf it's a negative number I will make it 4.");
        int ply;
        try { ply = Integer.parseInt(ans); } catch (NumberFormatException e){ ply = 4; }
        if (ply > 0){ gui.setPly(ply); }
        else { gui.setPly(4); }
        gui.aiOut.setText("Max ply set to "+gui.eve.MAX_PLY+".");
      }
    });
    ai.add(change_ply);

    force_move.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent ae){
        if (gui.aiThread.isAlive())
          gui.forceEndAIThread();
        else
          gui.aiMove();   //otherwise, start up the ai thread
      }
    });
    ai.add(force_move);

    turn_off.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent ae){
        gui.debug = !gui.debug;
        gui.aiOut.setText("AI is now "+((gui.debug)?"off":"on")+".");
      }
    });
    ai.add(turn_off);

    JMenu opt = new JMenu("Options");
    JMenuItem highlight = new JMenuItem("Toggle Highlight");
    JMenuItem custom_board = new JMenuItem("Create Custom Board");
    highlight.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent ae){
        gui.highlight = !gui.highlight;
        gui.aiOut.setText("Move Highlighting "+((gui.highlight)?"on":"off")+".");
      }
    });
    opt.add(highlight);

    custom_board.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent ae){
        gui.customBoard();
        Customization c = new Customization(gui);
        fb.add(c);
        fb.pack();
        fb.setVisible(true);
      }
    });
    opt.add(custom_board);

    menu.add(game);
    menu.add(opt);
    menu.add(ai);
    return(menu);
  } //JMenuBar menuBar(GUI)

  /**
   * void main(String[])
   * Defines the entry point into the application. Duh.
  **/
  public static void main(String[] args){
    final JFrame f = new JFrame("Fluffy Bunny Chess");
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    final GUI gui = new GUI(4);

    gui.aiOut = new TextField();
    gui.aiOut.setEnabled(false);
    gui.aiOut.setBounds(400,75,150,25);
    f.add(gui.aiOut);

    gui.moves = new JTextArea();
    gui.moves.setEnabled(false);
    gui.moves.setDisabledTextColor(Color.BLACK);
    gui.moves.setFont(new Font("Helvetica",Font.PLAIN,12));
    JScrollPane sp = new JScrollPane(gui.moves);
    sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    sp.setBounds(400,100,150,300);
    f.add(sp);

    f.setJMenuBar(menuBar(gui));
    f.setResizable(false);
    f.setIconImage(Toolkit.getDefaultToolkit().getImage("images/board/bunny.png"));
    f.add(gui);
    f.pack();
    f.setVisible(true);
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    f.setLocation((dim.width-f.getSize().width)/3,(dim.height-f.getSize().height)/3);
  } //void main(String[])
}
