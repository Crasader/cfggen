package cfg;
public class Task  {
public final int id;
public Task(CSVStream fs) {
this.id = fs.getInt();
}
}