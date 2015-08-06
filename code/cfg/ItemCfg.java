package cfg;
public class ItemCfg  {
public final java.util.List<Item> items = new java.util.ArrayList<Item>();
public final java.util.Map<Integer, Item> items_id2 = new java.util.HashMap<Integer, Item>();
public final java.util.Map<Integer, Item> items_id1 = new java.util.HashMap<Integer, Item>();
public ItemCfg(CSVStream fs) {
while(!fs.isSectionEnd()) {
this.items.add((Item)CfgMgr.create("Item", fs));
}
for(Item _V : this.items) {
this.items_id2.put(_V.id2, _V);
this.items_id1.put(_V.id1, _V);
}
}
}