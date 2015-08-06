package cfg;
public class CfgMgr {
public static class DataDir { public static String dir; public static String encoding; }
public static void load() {  }
public static final NamesCfg names;
public static final TestCfg test;
public static final IndexsCfg indexs;
public static final ItemsCfg items;
static {
names = new NamesCfg(CSVStream.create(DataDir.dir + "/names.csv", DataDir.encoding));
test = new TestCfg(CSVStream.create(DataDir.dir + "/test.csv", DataDir.encoding));
indexs = new IndexsCfg(CSVStream.create(DataDir.dir + "/indexs.csv", DataDir.encoding));
items = new ItemsCfg(CSVStream.create(DataDir.dir + "/items.csv", DataDir.encoding));
}
public static Object create(String name, CSVStream fs) {
try {
return Class.forName("cfg." + name).getConstructor(CSVStream.class).newInstance(fs);
} catch (Exception e) {
e.printStackTrace();
return null;
}
}
}