using System;
namespace cfg{
public class NamesCfg  {
public readonly System.Collections.Generic.Dictionary<int, string> names = new System.Collections.Generic.Dictionary<int, string>();
public NamesCfg(CSVStream fs) {
while(!fs.IsSectionEnd()) {
this.names[fs.GetInt()] = fs.GetString();
}
}
}
}