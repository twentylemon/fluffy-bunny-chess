/**
 * GUI.java
 * COSC 3P71 - Fluffy Bunny Chess
 * Taras Mychaskiw
 * Lachlan Plant
 * This class handles saving to and loading from files.
**/

import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;

public class Saves {

  private String base;    //directory where we save the file(s) to
  private File name;      //name of the save file to create/write
  private String[] names; //names of all the files within the base directory, used for loading

  private FileWriter nw;              //for names.txt - the list of save files
  private BufferedWriter nameWriter;

  public Saves(){
    base = "SavedGames/";
    names = new String[0];
    nameArrLoad();
    try {
      File dir = new File(base);
      if (!dir.exists())
        dir.mkdir();
      name = new File(base+"names.txt");
      nw = new FileWriter(name.getAbsoluteFile());
      nameWriter = new BufferedWriter(nw);
    } catch (Exception e){ System.out.println("Error creating save file."); }
  } //Saves()

  /********************************************************
   *                  Utility Functions                  *
   * void nameArrLoad()                                  *
   * String[] getNames()                                 *
  ********************************************************/
  /**
   * void nameArrLoad()
   * Fills the names array with the names of the files in the base directory.
  **/
  private void nameArrLoad(){
    ArrayList<String> arr = new ArrayList<String>();
    try {
      Scanner s = new Scanner(new File("SavedGames/names.txt"));
      while (s.hasNext()){
        String str = s.next();
        arr.add(str);
      }
    } catch (Exception e){}

    names = new String[arr.size()];
    for (int i = 0; i < arr.size(); i++)
      names[i] = arr.get(i);
  } //void nameArrLoad()

  /**
   * String[] getNames()
   * Accessor. Returns all the file names in the base directory.
  **/
  public String[] getNames(){
    return(names);
  } //String[] getNames()



  /********************************************************
   *                    I/O Functions                    *
   * void save(String,String)                            *
   * String load(String)                                 *
  ********************************************************/
  /**
   * void save(String,String)
   * Saves the board position, given by a FEN string, to a file named 'name'.txt.
  **/
  public void save(String name, String board){
    try {
      String dest = base+name+".txt";
      File out = new File(dest);
      out.createNewFile();
      FileWriter ow = new FileWriter(out.getAbsoluteFile());
      BufferedWriter obw = new BufferedWriter(ow);
      obw.write(board);

      nameWriter.write(name);
      nameWriter.newLine();
      for (String str : names){
        nameWriter.write(str);
        nameWriter.newLine();
      }
      obw.close();
      nameWriter.close();
    } catch (Exception e){ System.out.println("Error writing to save file."); }
  } //void save(String,String)

  /**
   * String load(String)
   * Returns the contents in the file 'saveName'
   * The FEN board is returned.
  **/
  public String load(String saveName){
    String fen = null;
    try {
      Scanner s = new Scanner(new File(base+saveName+".txt"));
      fen = s.nextLine();

      for (String str : names){
        nameWriter.write(str);
        nameWriter.newLine();
      }
      nameWriter.close();
    } catch(Exception e){}
    return(fen);
  } //String load(String)
}
