package cfg;
public class Item  {
public final static int __TYPE_ID__ = 1000000;
public final int getTypeid() { return __TYPE_ID__; }
public final int id1;
public final int id2;
public final int x;
public Item(CSVStream fs) {
this.id1 = fs.getInt();
this.id2 = fs.getInt();
this.x = fs.getInt();
}
}