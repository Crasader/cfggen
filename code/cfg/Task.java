package cfg;
public class Task  {
public final static int __TYPE_ID__ = 1000001;
public final int getTypeid() { return __TYPE_ID__; }
public final int id;
public Task(CSVStream fs) {
this.id = fs.getInt();
}
}