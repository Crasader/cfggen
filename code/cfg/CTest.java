package cfg;
public class CTest  {
public static final int x1 = 7;
public static final float x2 = 8.5f;
public static final String x3 = "sd";
public final Task id;
public final Task d2;
public final Task id3;
public final Task d24;
public final Task d25;
public final float ENUM1;
public final Task enum2;
public final java.util.List<Integer> a1 = new java.util.ArrayList<Integer>();
public final java.util.List<String> a2 = new java.util.ArrayList<String>();
public final java.util.Set<Integer> s1 = new java.util.HashSet<Integer>();
public final java.util.List<Task> d225 = new java.util.ArrayList<Task>();
public final java.util.Map<Integer, Task> d225_id = new java.util.HashMap<Integer, Task>();
public final Task T1;
public final Task T2;
public final Task T3;
public final java.util.Map<Integer, Integer> m1 = new java.util.HashMap<Integer, Integer>();
public final java.util.Map<Integer, String> m2 = new java.util.HashMap<Integer, String>();
public final java.util.Map<Integer, Task> m3 = new java.util.HashMap<Integer, Task>();
public CTest(CSVStream fs) {
this.id = (Task)CfgMgr.create(fs.getString(), fs);
this.d2 = (Task)CfgMgr.create(fs.getString(), fs);
this.id3 = (Task)CfgMgr.create(fs.getString(), fs);
this.d24 = (Task)CfgMgr.create(fs.getString(), fs);
this.d25 = (Task)CfgMgr.create(fs.getString(), fs);
this.ENUM1 = fs.getFloat();
this.enum2 = (Task)CfgMgr.create(fs.getString(), fs);
while(!fs.isSectionEnd()) {
this.a1.add(fs.getInt());
}
while(!fs.isSectionEnd()) {
this.a2.add(fs.getString());
}
while(!fs.isSectionEnd()) {
this.s1.add(fs.getInt());
}
while(!fs.isSectionEnd()) {
this.d225.add((Task)CfgMgr.create(fs.getString(), fs));
}
for(Task _V : this.d225) {
this.d225_id.put(_V.id, _V);
}
this.T1 = this.d225.get(0);
this.T2 = this.d225.get(1);
this.T3 = this.d225.get(2);
while(!fs.isSectionEnd()) {
this.m1.put(fs.getInt(), fs.getInt());
}
while(!fs.isSectionEnd()) {
this.m2.put(fs.getInt(), fs.getString());
}
while(!fs.isSectionEnd()) {
this.m3.put(fs.getInt(), (Task)CfgMgr.create(fs.getString(), fs));
}
}
}