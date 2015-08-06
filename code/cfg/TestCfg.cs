using System;
namespace cfg{
public class TestCfg  {
public static readonly int x1 = 7;
public static readonly float x2 = 8.5f;
public static readonly string x3 = "sd";
public readonly Task id;
public readonly Task d2;
public readonly Task id3;
public readonly Task d24;
public readonly Task d25;
public readonly float ENUM1;
public readonly Task enum2;
public readonly System.Collections.Generic.List<int> a1 = new System.Collections.Generic.List<int>();
public readonly System.Collections.Generic.List<string> a2 = new System.Collections.Generic.List<string>();
public readonly System.Collections.Generic.HashSet<int> s1 = new System.Collections.Generic.HashSet<int>();
public readonly System.Collections.Generic.List<Task> d225 = new System.Collections.Generic.List<Task>();
public readonly System.Collections.Generic.Dictionary<int, Task> d225_id = new System.Collections.Generic.Dictionary<int, Task>();
public readonly Task T1;
public readonly Task T2;
public readonly Task T3;
public readonly System.Collections.Generic.Dictionary<int, int> m1 = new System.Collections.Generic.Dictionary<int, int>();
public readonly System.Collections.Generic.Dictionary<int, string> m2 = new System.Collections.Generic.Dictionary<int, string>();
public readonly System.Collections.Generic.Dictionary<int, Task> m3 = new System.Collections.Generic.Dictionary<int, Task>();
public TestCfg(CSVStream fs) {
this.id = (Task)CfgMgr.Create(fs.GetString(), fs);
this.d2 = (Task)CfgMgr.Create(fs.GetString(), fs);
this.id3 = (Task)CfgMgr.Create(fs.GetString(), fs);
this.d24 = (Task)CfgMgr.Create(fs.GetString(), fs);
this.d25 = (Task)CfgMgr.Create(fs.GetString(), fs);
this.ENUM1 = fs.GetFloat();
this.enum2 = (Task)CfgMgr.Create(fs.GetString(), fs);
while(!fs.IsSectionEnd()) {
this.a1.Add(fs.GetInt());
}
while(!fs.IsSectionEnd()) {
this.a2.Add(fs.GetString());
}
while(!fs.IsSectionEnd()) {
this.s1.Add(fs.GetInt());
}
while(!fs.IsSectionEnd()) {
this.d225.Add((Task)CfgMgr.Create(fs.GetString(), fs));
}
foreach(var _V in this.d225) {
this.d225_id.Add(_V.id, _V);
}
this.T1 = this.d225[0];
this.T2 = this.d225[1];
this.T3 = this.d225[2];
while(!fs.IsSectionEnd()) {
this.m1[fs.GetInt()] = fs.GetInt();
}
while(!fs.IsSectionEnd()) {
this.m2[fs.GetInt()] = fs.GetString();
}
while(!fs.IsSectionEnd()) {
this.m3[fs.GetInt()] = (Task)CfgMgr.Create(fs.GetString(), fs);
}
}
}
}