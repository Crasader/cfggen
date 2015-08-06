using System;
namespace cfg{
public class NameCfg  {
public readonly System.Collections.Generic.Dictionary<int, string> names = new System.Collections.Generic.Dictionary<int, string>();
public NameCfg(CSVStream fs) {
while(!fs.IsSectionEnd()) {
this.names[fs.GetInt()] = fs.GetString();
}
}
}
}