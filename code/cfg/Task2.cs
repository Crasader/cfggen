using System;
namespace cfg{
public class Task2 : Task {
public const int __TYPE_ID__ = 2;
public int GetTypeid() { return __TYPE_ID__; }
public readonly int y;
public readonly string name;
public Task2(CSVStream fs) : base(fs) {
this.y = fs.GetInt();
this.name = fs.GetString();
}
}
}