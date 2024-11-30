import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
  public static void main(String[] args) {
    InputStreamReader isr = new InputStreamReader(System.in);
    BufferedReader br = new BufferedReader(isr);
    try {
      System.out.print("Enter the name of the source code of java file: ");
      String fileName = br.readLine();

      new ExtractFromComments(fileName);
      System.out.println("Files created index.html and style.css, open the file index.html to view the output");
    } catch (IOException e) {
      System.out.println("Invalid Input");
    }
  }
}