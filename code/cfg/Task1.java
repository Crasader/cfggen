package cfg;
public class Task1 extends Task {
public final static int __TYPE_ID__ = 1;
public final int getTypeid() { return __TYPE_ID__; }
public final float x;
public Task1(CSVStream fs) {
super(fs);
this.x = fs.getFloat();
}
}