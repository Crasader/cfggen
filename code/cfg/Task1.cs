using System;
namespace cfg{
public class Task1 : Task {
public const int __TYPE_ID__ = 1;
public int GetTypeid() { return __TYPE_ID__; }
public readonly float x;
public Task1(CSVStream fs) : base(fs) {
this.x = fs.GetFloat();
}
}
}