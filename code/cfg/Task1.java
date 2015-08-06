package cfg;
public class Task1 extends Task {
public final float x;
public Task1(CSVStream fs) {
super(fs);
this.x = fs.getFloat();
}
}