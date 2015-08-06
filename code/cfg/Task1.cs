using System;
namespace cfg{
public class Task1 : Task {
public readonly float x;
public Task1(CSVStream fs) : base(fs) {
this.x = fs.GetFloat();
}
}
}