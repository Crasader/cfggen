using System;
namespace cfg{
public class NamesCfg  {
public const int __TYPE_ID__ = 1000003;
public int GetTypeid() { return __TYPE_ID__; }
public readonly System.Collections.Generic.Dictionary<int, string> names = new System.Collections.Generic.Dictionary<int, string>();
public NamesCfg(CSVStream fs) {
while(!fs.IsSectionEnd()) {
this.names[fs.GetInt()] = fs.GetString();
}
}
}
}