local split = '&&';
local delimiter = '@@';
local type = ARGV[1];
local transaction = ARGV[2];
local key = ARGV[3];
local current = ARGV[4];
local result = {};
local value = redis.call('SMEMBERS', key);
local beforetransactions = key .. '==';
local beforeinventorys = '';
local afterinventorys = '';
local valueindex = 0;
for _, v in ipairs(value) do
    local currkey = (current .. v);
    local currvalue = redis.call('get', currkey);
    if currvalue == 0 or currvalue == false then
        result['success'] = false;
        result['errormsg'] = '即时库存key不存在' .. currkey;
        return cjson.encode(result);
    else
        if valueindex == 0 then
            beforetransactions = beforetransactions .. v;
            beforeinventorys = beforeinventorys .. currkey .. '==' .. currvalue;
        else
            beforetransactions = beforetransactions .. ',,' .. v;
            beforeinventorys = beforeinventorys .. ',,' .. currkey .. '==' .. currvalue;
        end
        local uqty = 0;
        local uvalue = '';
        local i = 0;
        for sku in string.gmatch(currvalue, '([^' .. split .. ']+)') do
            if i == 0 then
                uqty = sku;
            else
                local a = 0;
                local flag = 0;
                for s in string.gmatch(sku, '([^' .. delimiter .. ']+)') do
                    if a == 0 and s == transaction then
                        flag = 1;
                    else
                        if type == 'commit' and flag == 1 then
                            uqty = uqty + s;
                        end
                    end
                    a = a + 1;
                end
                if flag == 0 then
                    uvalue = uvalue .. split .. sku;
                end
            end
            i = i + 1;
        end
        local newvalue = uqty .. uvalue;
        redis.call('set', currkey, newvalue);
        redis.call('SREM', key, v);
        if valueindex == 0 then
            afterinventorys = afterinventorys .. currkey .. '==' .. newvalue;
        else
            afterinventorys = afterinventorys .. ',,' .. currkey .. '==' .. newvalue;
        end
        valueindex = valueindex + 1;
    end
end
result['success'] = true;
result['beforetransactions'] = beforetransactions;
result['beforeinventorys'] = beforeinventorys;
result['afterinventorys'] = afterinventorys;
local opallcount = redis.call('INCRBY' , 'inventory:op:all' , 1);
result['opallcount'] = opallcount;
if type == 'commit' then
    local commitcount = redis.call('INCRBY' , 'inventory:op:' .. type , 1);
    result['commitcount'] = commitcount;
else
    local rollbackcount = redis.call('INCRBY' , 'inventory:op:' .. type , 1);
    result['rollbackcount'] = rollbackcount;
end
return cjson.encode(result);
