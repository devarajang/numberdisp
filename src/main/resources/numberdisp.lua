-- numberdisp.lua
local count = tonumber( KEYS[2])
local link_id = redis.call("SETBIT", KEYS[1], count, 1)
while (link_id == 1) 
do
    local rem = count % 8
    count = (count-rem) / 8
    count = redis.call("BITPOS", KEYS[1], 0, count)
    link_id = redis.call("SETBIT", KEYS[1], count, 1)
end
return count