using System;
namespace cfg{
public class Item  {
public const int __TYPE_ID__ = 1000000;
public int GetTypeid() { return __TYPE_ID__; }
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