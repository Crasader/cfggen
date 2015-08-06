package cfg;
public class Task2 extends Task {
public final int y;
public final String name;
public Task2(CSVStream fs) {
super(fs);
this.y = fs.getInt();
this.name = fs.getString();
}
}