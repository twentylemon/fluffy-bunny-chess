/**
 * Customization.java
 * COSC 3P71 - Fluffy Bunny Chess
 * Taras Mychaskiw
 * Lachlan Plant
 * This class handles the 'create custom board' event. Pops open a new
 * window where you can click a piece, click where to place it on the board,
 * then click "Set" to place it on the board. One can then click "Play" to start
 * the game from there. Side note: board legality is not checked.
**/

import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.awt.geom.*;
import java.awt.event.*;

public class Customization extends JPanel implements ActionListener {
  boolean debug = false;

  private int[][] used; //what each square will contain
  private int sx;       //(x,y) of selected square in the custom window
  private int sy;
  private int speice;   //selected piece

  private int cell;     //cell size for board squares
  private int sizex;    //total x size of new window
  private int sizey;    //y size of new window

  private GUI gui;      //take this to get info like the cell size etc, and for writing to
  private JButton b1;   //"Set" button, writes a piece to the board
  private JButton b2;   //"Play" button

  public Customization(GUI g){
   gui = g;
   cell = gui.cell;
   sizex = cell*2;
   sizey = cell*6+cell;
   int[][] ims = {
      { Board.PAWN,  -Board.PAWN,  0},
      { Board.QUEEN, -Board.QUEEN, 0},
      { Board.KING,  -Board.KING,  0},
      { Board.BISHOP,-Board.BISHOP,0},
      { Board.ROOK,  -Board.ROOK,  0},
      { Board.KNIGHT,-Board.KNIGHT,0}
    };
    used = ims; //for some reason just used = {blah} wouldn't work
    setPreferredSize(new Dimension(sizex+cell,sizey));
    setBackground(Color.WHITE);
    this.setFocusable(true);
    this.requestFocus();
    addMouseListener(new MyMouseListener());

    b1 = new JButton("Set");
    b1.setVerticalTextPosition(AbstractButton.CENTER);
    b1.setHorizontalTextPosition(AbstractButton.CENTER);
    b1.setMnemonic(KeyEvent.VK_M);
    b1.addActionListener(this);
    add(b1);

    b2 = new JButton("Play");
    b2.setVerticalTextPosition(AbstractButton.CENTER);
    b2.setHorizontalTextPosition(AbstractButton.CENTER);
    b2.setMnemonic(KeyEvent.VK_E);
    b2.addActionListener(this);
    add(b2);
    repaint();
  } //Customization(GUI)

  /**
   * void actionPerformed(ActionEvent)
   * Handles the button presses, "Set" and "Play"
  **/
  public void actionPerformed(ActionEvent e){
    if (debug)
      System.out.println(e.getActionCommand());
    if ("Play".equals(e.getActionCommand()))
      gui.setting = false;
    else if (gui.setting)
      gui.set(gui.settingx,gui.settingy,used[sy][sx]);
    repaint();
    gui.repaint();  //to get rid of the green border square
  } //void actionPerformed(ActionEvent)

  /**
   * class MyMouseListener
   * Mouse event handler. Finds out where we clicked in the custom box.
  **/
  private class MyMouseListener implements MouseListener {
    public void mouseClicked(MouseEvent e){
      int x = e.getX()/cell;
      int y = (e.getY()-40)/cell;
      sx = x;
      sy = y;
      repaint();
    } //void MyMouseListener.mouseClicked(MouseEvent)
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
  } //class MyMouseListener

  /**
   * void paintComponent(Graphics)
   * This gets called by JPanel.repaint(). Defines how to repaint the board.
  **/
  public void paintComponent(Graphics g){
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
    boolean b = false;
    int x,y;
    for (x = 0; x < 3; x++)
      for (y = 0; y < 7; y++){
        if (b)
          g2.drawImage(gui.whitesquare,x*cell,y*cell,this);
        else
          g2.drawImage(gui.blacksquare,x*cell,y*cell,this);
        b = !b;
      }

    for (x = 0; x < 2; x++)
      for (y = 0; y < 6; y++)
      switch (used[y][x]){
      case Board.PAWN:    g2.drawImage(gui.wpawn,x*cell,y*cell+cell,this);   break;
      case -Board.PAWN:   g2.drawImage(gui.bpawn,x*cell,y*cell+cell,this);   break;
      case Board.QUEEN:   g2.drawImage(gui.wqueen,x*cell,y*cell+cell,this);  break;
      case -Board.QUEEN:  g2.drawImage(gui.bqueen,x*cell,y*cell+cell,this);  break;
      case Board.KING:    g2.drawImage(gui.wking,x*cell,y*cell+cell,this);   break;
      case -Board.KING:   g2.drawImage(gui.bking,x*cell,y*cell+cell,this);   break;
      case Board.ROOK:    g2.drawImage(gui.wrook,x*cell,y*cell+cell,this);   break;
      case -Board.ROOK:   g2.drawImage(gui.brook,x*cell,y*cell+cell,this);   break;
      case Board.KNIGHT:  g2.drawImage(gui.wknight,x*cell,y*cell+cell,this); break;
      case -Board.KNIGHT: g2.drawImage(gui.bknight,x*cell,y*cell+cell,this); break;
      case Board.BISHOP:  g2.drawImage(gui.wbishop,x*cell,y*cell+cell,this); break;
      case -Board.BISHOP: g2.drawImage(gui.bbishop,x*cell,y*cell+cell,this); break;
      }
    if (gui.setting)
      g2.drawImage(gui.greenb,sx*cell,sy*cell+cell,this);
    if (debug)
      System.out.println("("+sy+","+sx+")");
  } //void paintComponent(Graphics)
}
