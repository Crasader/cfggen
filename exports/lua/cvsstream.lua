 local tonumber = tonumber
 local sub = string.sub
 local gsub = string.gsub
 local gmatch = string.gmatch
 local error = error
 local open = io.open
 local setmetatable = setmetatable
 local tostring = tostring
 local insert = table.insert
 
 local os = {}
 os.__index = os
 
 function os.new(csvfile)
    local o = {}
    setmetatable(o, os)
    
    local f, err = open(csvfile, "r")
    if not f then
        error(err)
    end
    
    local all = f:read("*a")
    local n = #all
    local pos = 1
    o.data_iter =  function ()  --  gmatch(all, '([^,]*),?')
        if pos > n then return end
        local start = pos
		local tail
        if sub(all, pos, pos) == '"' then
            pos = pos + 1
            start = pos
            while pos <= n do
                if sub(all, pos, pos) == '"' then
                    if pos == n then
						tail = pos - 1
                        pos = n + 1
                        return sub(all, start, tail)
                    elseif sub(all, pos + 1, pos + 1) ~= '"' then
						tail = pos - 1
						pos = pos + 1
						while pos <= n and sub(all, pos, pos) ~= "," do
							pos = pos + 1
						end
						pos = pos + 1
							
                        return sub(all, start, tail)
                    else
                        pos = pos + 2
                    end
                else
                    pos = pos + 1
                end
            end
        else
            while pos <= n do
                if sub(all, pos, pos) == "," then
                    pos = pos + 1
                    return sub(all, start, pos - 2)
                else
                    pos = pos + 1
                end
            end
            return sub(all, start)
        end
    end
    return o
 end
 
 function os:dump()
    while true do
        local w = self:get_next()
        if not w then break end
        print(w)
    end
 end
 
 function os:get_next()
    local last = self.last
    if last then
        self.last = nil
        return last
    else
        return gsub(self.data_iter(), '""', '"')
    end
 end
 
 function os:is_section_end()
    local next = self:get_next()
    if next == "]]" then
        return true
    else
        self.last = next
        return false
    end
 end
 
function os:get_bool()
    local next = self:get_next()
    if next == "true" then
        return true
    elseif next == "false" then
        return false
    else
        error(tostring(next) .. "isn't bool")
    end
 end
 
function os:get_int()
    local next = self:get_next()
    return tonumber(next)
 end
 
function os:get_long() 
    local next = self:get_next()
    return tonumber(next)
 end 
 
function os:get_float()
    local next = self:get_next()
    return tonumber(next)
 end
 
function os:get_string()
    local next = self:get_next()
    local s = next:gsub("\\#", "#"):gsub("\\%]", "%]"):gsub("\\s", ""):gsub("\\\\", "\\")
	return s
 end 
 
function os:get_list(key)
    local r = {}
    local oper = self["get_" .. key]
    while not self:is_section_end() do
          insert(r, oper(self))
    end
    return r
 end 
 
function os:get_set(key)
    local r = {}
    local oper = self["get_" .. key]
    while not self:is_section_end() do
        r[oper(self)] = true
    end
    return r
 end 
 
function os:get_map(key, value)
    local r = {}
    local oper_key = self["get_" .. key]
    local oper_value = self["get_" .. value]
    while not self:is_section_end() do
        r[oper_key(self)] = oper_value(self)
    end
    return r
 end
 
 return os