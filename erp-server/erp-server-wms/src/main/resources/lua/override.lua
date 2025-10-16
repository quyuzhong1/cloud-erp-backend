local delimiter = '&&';
local i = 0;
local key = ARGV[1];
local qty = ARGV[2];
local value = redis.call('get', key);
if value == 0 or value == false then
    redis.call('set', key, qty);
else
    for sku in string.gmatch(value, '([^' .. delimiter .. ']+)') do
        i = i + 1;
    end
    if i > 1 then
        return '存在未提交流水&&1000'
    else
        redis.call('set', key, qty);
    end
end
return '0';
