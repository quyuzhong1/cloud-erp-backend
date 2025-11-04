local delimiter = '&&';
local i = 0;
local key = ARGV[1];
local qty = ARGV[2];
local result = {};
local value = redis.call('get', key);
local beforeinventorys = key .. '==';
if value == 0 or value == false then
    redis.call('set', key, qty);
else
    beforeinventorys = beforeinventorys .. value;
    for sku in string.gmatch(value, '([^' .. delimiter .. ']+)') do
        i = i + 1;
    end
    if i > 1 then
        result['success'] = false;
        result['errormsg'] = '存在未提交流水';
        return cjson.encode(result);
    else
        redis.call('set', key, qty);
    end
end
local afterinventorys = (key .. '==' .. qty);
result['success'] = true;
result['beforeinventorys'] = beforeinventorys;
result['afterinventorys'] = afterinventorys;
local opallcount = redis.call('INCRBY' , 'inventory:op:all' , 1);
local overridecount = redis.call('INCRBY' , 'inventory:op:override' , 1);
result['opallcount'] = opallcount;
result['overridecount'] = overridecount;
return cjson.encode(result);
