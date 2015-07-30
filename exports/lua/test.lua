local os = require "cfg.structs"

local insert = table.insert
local concat = table.concat
local tostring = tostring

local function dump_atom (x)
    return tostring(x)
end

local function dump_table(t)
  local code = {"{"}
  
  for k, v in pairs(t) do
    if type(v) ~= "table" then
      insert(code, tostring(k) .. "=" .. dump_atom(v) .. ",")
    else
      insert(code, tostring(k) .. "=" .. dump_table(v) .. ",")
    end
  end
 insert(code, "}")
 return concat(code)
end

local r = os.new("f:/workspace/luxian/luxian/cfggen/data/test.csv")

local v = r:get_CTest()

print(dump_table(v))



