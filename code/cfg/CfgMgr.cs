using System;
namespace cfg{
public class CfgMgr {
public class DataDir {
public static string Dir { set; get;} 
public static string Encoding { set; get; }
}
public static void Load() {  }
public static readonly NamesCfg names;
public static readonly TestCfg test;
public static readonly IndexsCfg indexs;
public static readonly ItemsCfg items;
static CfgMgr() {
names = new NamesCfg(CSVStream.Create(DataDir.Dir + "/names.csv", DataDir.Encoding));
test = new TestCfg(CSVStream.Create(DataDir.Dir + "/test.csv", DataDir.Encoding));
indexs = new IndexsCfg(CSVStream.Create(DataDir.Dir + "/indexs.csv", DataDir.Encoding));
items = new ItemsCfg(CSVStream.Create(DataDir.Dir + "/items.csv", DataDir.Encoding));
}
public static Object Create(string name, CSVStream fs) {
try {
return Type.GetType("cfg." + name).GetConstructor(new []{typeof (CSVStream)}).Invoke(new object[]{fs});
} catch (Exception e) {
System.Console.WriteLine(e);
return null;
}
}
}
}