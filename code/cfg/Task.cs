using System;
namespace cfg{
public class Task  {
public const int __TYPE_ID__ = 1000001;
public int GetTypeid() { return __TYPE_ID__; }
public readonly int id;
public Task(CSVStream fs) {
this.id = fs.GetInt();
}
}
}