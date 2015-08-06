using System;
namespace cfg{
public class Task  {
public readonly int id;
public Task(CSVStream fs) {
this.id = fs.GetInt();
}
}
}