# cfggen

cfggen是一个游戏专用配置生成工具.
cfggen读取csv文件,导出配置数据,并且生成读取配置数据所需的代码.
cfggen目前支持java,csharp,lua三种语言.

cfggen有几个突出优点:
1. 强大而又灵活.
	支持数据类型有bool,int,long,float,string,list,set,map,enum及struct. 
	可以在单一csv文件里配置出任何复杂的数据,与json和lua的表达能力等价.
	不限定数据出现的位置,可以把一行拆分为多行,可以把多行合并为一行,甚至可以把单个csv文件拆分成多个,
	都不影响处理.
	支持注释,可以在csv任何位置 添加注释,以便数据清晰.
2. 简单.
	对于策划而言学习成本几乎为零.
3. 支持多态类型. 
	比如任务基类为Task, 有多种子类型Task1, Task2,Task3等等,每种子类型的字段不同.
	通过定义field type="Task" 可以识别读取这种数据.
4. 支持多索引. 
	如果list的成员类型为复杂结构,可以对该结构的多个字段进行索引,便于使用.
5. 支持索引校验. 
	如果某个字段是itemid,那么通过指定ref的方式,检查itemid是否合法.
	比如 <field name="itemid" type="int" ref="item.items"/>
6. 支持数据分组. 
	有些字段只是服务器使用,有些字段只是客户端使用,有些则是都使用.通过指定groups,在导出时只导出指定数据.
	例如 <field name="desc" type="string" groups="client"/>
