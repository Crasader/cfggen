package cfg;
public class Task2 extends Task {
public final static int __TYPE_ID__ = 2;
public final int getTypeid() { return __TYPE_ID__; }
public final int y;
public final String name;
public Task2(CSVStream fs) {
super(fs);
this.y = fs.getInt();
this.name = fs.getString();
}
}