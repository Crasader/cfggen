using System;
namespace cfg{
public class Item  {
public readonly int id1;
public readonly int id2;
public readonly int x;
public Item(CSVStream fs) {
this.id1 = fs.GetInt();
this.id2 = fs.GetInt();
this.x = fs.GetInt();
}
}
}