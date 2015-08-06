using System;
namespace cfg{
public class IndexCfg  {
public readonly int d1;
public readonly int d2;
public readonly int d3;
public readonly int d4;
public readonly System.Collections.Generic.List<int> e1 = new System.Collections.Generic.List<int>();
public readonly System.Collections.Generic.HashSet<int> e2 = new System.Collections.Generic.HashSet<int>();
public readonly System.Collections.Generic.Dictionary<int, string> e3 = new System.Collections.Generic.Dictionary<int, string>();
public readonly System.Collections.Generic.Dictionary<string, int> e4 = new System.Collections.Generic.Dictionary<string, int>();
public readonly System.Collections.Generic.Dictionary<int, int> e5 = new System.Collections.Generic.Dictionary<int, int>();
public IndexCfg(CSVStream fs) {
this.d1 = fs.GetInt();
this.d2 = fs.GetInt();
this.d3 = fs.GetInt();
this.d4 = fs.GetInt();
while(!fs.IsSectionEnd()) {
this.e1.Add(fs.GetInt());
}
while(!fs.IsSectionEnd()) {
this.e2.Add(fs.GetInt());
}
while(!fs.IsSectionEnd()) {
this.e3[fs.GetInt()] = fs.GetString();
}
while(!fs.IsSectionEnd()) {
this.e4[fs.GetString()] = fs.GetInt();
}
while(!fs.IsSectionEnd()) {
this.e5[fs.GetInt()] = fs.GetInt();
}
}
}
}