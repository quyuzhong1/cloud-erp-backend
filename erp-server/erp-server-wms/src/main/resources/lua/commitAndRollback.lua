local split = '&&';
local delimiter = '@@';
local type = ARGV[1];
local transaction = ARGV[2];
local key = ARGV[3];
local current = ARGV[4];
local value = redis.call('SMEMBERS', key);
for _, v in ipairs(value) do
    local currkey = (current .. v);
    local currvalue = redis.call('get', currkey);
    if currvalue == 0 or currvalue == false then
        return '即时库存key不存在' .. currkey;
    else
        local uqty = 0;
        local uvalue = '';
        local i = 0;
        for sku in string.gmatch(currvalue, '([^' .. split .. ']+)') do
            if i == 0 then
                uqty = sku;
            else
                local flag = 0;
                for s in string.gmatch(sku, '([^' .. delimiter .. ']+)') do
                    if flag == 0 and s == transaction then
                        flag = 1;
                    else
                        if type == 'commit' and flag == 1 then
                            uqty = uqty + s;
                        end
                    end
                end
                if flag == 0 then
                    uvalue = uvalue .. sku .. split;
                end
            end
            i = i + 1;
        end
        local newvalue = uqty .. uvalue;
        redis.call('set', currkey, newvalue);
        redis.call('SREM', key, v);
    end
end
return '0';
