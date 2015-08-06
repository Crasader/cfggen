local os = require 'cfg.cvsstream'
local insert = table.insert
local ipairs = ipairs
function os:get_NamesCfg()
local o = {}
o.names = self:get_map('int', 'string')
return o
end
function os:get_Task2()
local o = {}
o.id = self:get_int()
o.y = self:get_int()
o.name = self:get_string()
return o
end
function os:get_Task1()
local o = {}
o.id = self:get_int()
o.x = self:get_float()
return o
end
function os:get_Item()
local o = {}
o.id1 = self:get_int()
o.id2 = self:get_int()
o.x = self:get_int()
return o
end
function os:get_Task()
return self['get_' .. self:get_string()](self)
end
function os:get_ItemsCfg()
local o = {}
local _list = self:get_list('Item')
o.items = _list
o.items_id2 = {}
o.items_id1 = {}
for _, _V in ipairs(_list) do
o.items_id2[_V.id2] = _V
o.items_id1[_V.id1] = _V
end
return o
end
function os:get_TestCfg()
local o = {}
o.id = self:get_Task()
o.d2 = self:get_Task()
o.id3 = self:get_Task()
o.d24 = self:get_Task()
o.d25 = self:get_Task()
o.ENUM1 = self:get_float()
o.enum2 = self:get_Task()
local _list = self:get_list('int')
o.a1 = _list
local _list = self:get_list('string')
o.a2 = _list
o.s1 = self:get_set('int')
local _list = self:get_list('Task')
o.d225 = _list
o.d225_id = {}
for _, _V in ipairs(_list) do
o.d225_id[_V.id] = _V
end
o.T1 = _list[1]
o.T2 = _list[2]
o.T3 = _list[3]
o.m1 = self:get_map('int', 'int')
o.m2 = self:get_map('int', 'string')
o.m3 = self:get_map('int', 'Task')
return o
end
function os:get_IndexsCfg()
local o = {}
o.d1 = self:get_int()
o.d2 = self:get_int()
o.d3 = self:get_int()
o.d4 = self:get_int()
local _list = self:get_list('int')
o.e1 = _list
o.e2 = self:get_set('int')
o.e3 = self:get_map('int', 'string')
o.e4 = self:get_map('string', 'int')
o.e5 = self:get_map('int', 'int')
return o
end
return os