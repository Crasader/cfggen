using System;
namespace cfg{
public class Task2 : Task {
public readonly int y;
public readonly string name;
public Task2(CSVStream fs) : base(fs) {
this.y = fs.GetInt();
this.name = fs.GetString();
}
}
}