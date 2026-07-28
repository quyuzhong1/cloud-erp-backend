local split = '&&';
local delimiter = '@@';
local type = ARGV[1];
local transaction = ARGV[2];
local key = ARGV[3];
local current = ARGV[4];
local reservePrefix = ARGV[5];
local entityPrefix = ARGV[6];
local virtualPrefix = ARGV[7];
local result = {};
local unallocMarkerPrefix = 'unalloc@@';

local function safe_tonumber(val, default)
    if val == nil or val == '' or val == false or val == 0 then
        return default or 0;
    end
    local n = tonumber(val);
    if n == nil then
        return default or 0;
    end
    return n;
end

local function remove_transaction_from_reserve(reservevalue, txn)
    if reservevalue == 0 or reservevalue == false then
        return reservevalue, 0;
    end
    local removedQty = 0;
    local base = '0';
    local newSegments = '';
    local idx = 0;
    for segment in string.gmatch(reservevalue, '([^' .. split .. ']+)') do
        if idx == 0 then
            base = segment;
        else
            local partIdx = 0;
            local segTxn = '';
            local segQty = 0;
            for s in string.gmatch(segment, '([^' .. delimiter .. ']+)') do
                if partIdx == 0 then
                    segTxn = s;
                else
                    segQty = safe_tonumber(s, 0);
                end
                partIdx = partIdx + 1;
            end
            if segTxn == txn then
                removedQty = removedQty + segQty;
            else
                if newSegments == '' then
                    newSegments = segment;
                else
                    newSegments = newSegments .. split .. segment;
                end
            end
        end
        idx = idx + 1;
    end
    if newSegments == '' then
        return base, removedQty;
    end
    return base .. split .. newSegments, removedQty;
end

local value = redis.call('SMEMBERS', key);
local beforetransactions = key .. '==';
local beforeinventorys = '';
local afterinventorys = '';
local valueindex = 0;
for _, v in ipairs(value) do
    if string.sub(v, 1, string.len(unallocMarkerPrefix)) == unallocMarkerPrefix then
        local markerBody = string.sub(v, string.len(unallocMarkerPrefix) + 1);
        local markerParts = {};
        local markerIdx = 0;
        for mp in string.gmatch(markerBody, '([^' .. delimiter .. ']+)') do
            table.insert(markerParts, mp);
            markerIdx = markerIdx + 1;
        end
        if markerIdx >= 3 then
            local warehouseId = markerParts[1];
            local skuId = markerParts[2];
            local reserveKey = reservePrefix .. warehouseId .. ':' .. skuId;
            local entityKey = entityPrefix .. warehouseId .. ':' .. skuId;
            local virtualKey = virtualPrefix .. warehouseId .. ':' .. skuId;
            local reserveValue = redis.call('get', reserveKey);
            local newReserveValue, removedQty = remove_transaction_from_reserve(reserveValue, transaction);
            redis.call('set', reserveKey, newReserveValue);
            redis.call('del', entityKey);
            redis.call('del', virtualKey);
            if valueindex == 0 then
                beforeinventorys = beforeinventorys .. reserveKey .. '==' .. tostring(reserveValue);
            else
                beforeinventorys = beforeinventorys .. ',,' .. reserveKey .. '==' .. tostring(reserveValue);
            end
            if valueindex == 0 then
                afterinventorys = afterinventorys .. reserveKey .. '==' .. tostring(newReserveValue);
            else
                afterinventorys = afterinventorys .. ',,' .. reserveKey .. '==' .. tostring(newReserveValue);
            end
        end
        redis.call('SREM', key, v);
    else
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
                    uqty = safe_tonumber(sku, 0);
                else
                    local a = 0;
                    local flag = 0;
                    for s in string.gmatch(sku, '([^' .. delimiter .. ']+)') do
                        if a == 0 and s == transaction then
                            flag = 1;
                        else
                            if type == 'commit' and flag == 1 then
                                uqty = uqty + safe_tonumber(s, 0);
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
        end
    end
    valueindex = valueindex + 1;
end
result['success'] = true;
result['beforetransactions'] = beforetransactions;
result['beforeinventorys'] = beforeinventorys;
result['afterinventorys'] = afterinventorys;
local opallcount = redis.call('INCRBY', 'inventory:op:all', 1);
result['opallcount'] = opallcount;
if type == 'commit' then
    local commitcount = redis.call('INCRBY', 'inventory:op:commit', 1);
    result['commitcount'] = commitcount;
else
    local rollbackcount = redis.call('INCRBY', 'inventory:op:rollback', 1);
    result['rollbackcount'] = rollbackcount;
end
return cjson.encode(result);
