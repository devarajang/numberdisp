--[[
Sets a bitmap range

Bitmaps are stored as Strings in Redis. A range spans one or more bytes,
so we can call `SETRANGE` when entire bytes need to be set instead of flipping
individual bits. Also, to avoid multiple internal memory allocations in
Redis, we traverse in reverse.

Expected input:
  KEYS[1] - bitfield key
  ARGV[1] - start offset (0-based, inclusive)
  ARGV[2] - end offset (same, should be bigger than start, no error checking)
  ARGV[3] - value (should be 0 or 1, no error checking)

]]--

-- A helper function to stringify a binary string to semi-binary format
local function tobits(str)
  local r = ''
  for i = 1, string.len(str) do
    local c = string.byte(str, i)
    local b = ' '
    for j = 0, 7 do
      b = tostring(bit.band(c, 1)) .. b
      c = bit.rshift(c, 1)
    end
    r = r  .. b
  end
  return r
end

-- Main
local k = KEYS[1]
local s, e, v = tonumber(ARGV[1]), tonumber(ARGV[2]), tonumber(ARGV[3])

-- First treat the dangling bits in the last byte
local ms, me = s % 8, (e + 1) % 8
if me > 0 then
  local t = math.max(e - me + 1, s)
  for i = e, t, -1 do
    redis.call('SETBIT', k, i, v)
  end
  e = t
end

-- Then the danglings in the first byte
if ms > 0 then
  local t = math.min(s - ms + 7, e)
  for i = s, t, 1 do
    redis.call('SETBIT', k, i, v)
  end
  s = t + 1
end

-- Set a range accordingly, if at all
local rs, re = s / 8, (e + 1) / 8
local rl = re - rs
if rl > 0 then
  local b = '\255'
  if 0 == v then
    b = '\0'
  end
  redis.call('SETRANGE', k, rs, string.rep(b, rl))
end

-- Debug only, uncomment if needed
-- local bitmap = redis.call('GET', k)
-- return tobits(bitmap)