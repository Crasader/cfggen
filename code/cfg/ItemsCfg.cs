using System;
namespace cfg{
public class ItemsCfg  {
public const int __TYPE_ID__ = 1000002;
public int GetTypeid() { return __TYPE_ID__; }
public readonly System.Collections.Generic.List<Item> items = new System.Collections.Generic.List<Item>();
public readonly System.Collections.Generic.Dictionary<int, Item> items_id2 = new System.Collections.Generic.Dictionary<int, Item>();
public readonly System.Collections.Generic.Dictionary<int, Item> items_id1 = new System.Collections.Generic.Dictionary<int, Item>();
public ItemsCfg(CSVStream fs) {
while(!fs.IsSectionEnd()) {
this.items.Add((Item)CfgMgr.Create("Item", fs));
}
foreach(var _V in this.items) {
this.items_id2.Add(_V.id2, _V);
this.items_id1.Add(_V.id1, _V);
}
}
}
}