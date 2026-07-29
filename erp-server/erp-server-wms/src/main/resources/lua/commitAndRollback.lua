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

local function split_delimited_fields(str, delim)
    local fields = {};
    if str == nil or str == '' then
        return fields;
    end
    local startPos = 1;
    local delimLen = string.len(delim);
    while true do
        local pos = string.find(str, delim, startPos, true);
        if pos == nil then
            table.insert(fields, string.sub(str, startPos));
            break;
        end
        table.insert(fields, string.sub(str, startPos, pos - 1));
        startPos = pos + delimLen;
    end
    return fields;
end

local function strict_nonneg_int(val)
    if val == nil or val == '' or val == false then
        return nil;
    end
    local n = tonumber(val);
    if n == nil or n < 0 or n ~= math.floor(n) then
        return nil;
    end
    return n;
end

local function strict_signed_int(val)
    if val == nil or val == '' or val == false then
        return nil;
    end
    local n = tonumber(val);
    if n == nil or n ~= math.floor(n) then
        return nil;
    end
    return n;
end

local function parse_reserve_segment(segment)
    local parts = split_delimited_fields(segment, delimiter);
    if #parts == 2 then
        return parts[1], '', strict_nonneg_int(parts[2]);
    end
    if #parts >= 3 then
        return parts[1], parts[2], strict_nonneg_int(parts[3]);
    end
    return nil, nil, nil;
end

local function parse_try_segment(segment)
    local parts = split_delimited_fields(segment, delimiter);
    if #parts == 2 then
        return parts[1], '', strict_signed_int(parts[2]);
    end
    if #parts >= 3 then
        return parts[1], parts[2], strict_signed_int(parts[3]);
    end
    return nil, nil, nil;
end

local function normalize_redis_string(val)
    if val == false or val == 0 or val == nil then
        return '0';
    end
    return tostring(val);
end

local function reserve_has_transaction(reservevalue, txn)
    if reservevalue == 0 or reservevalue == false then
        return false;
    end
    local idx = 0;
    for segment in string.gmatch(reservevalue, '([^' .. split .. ']+)') do
        if idx > 0 then
            local segTxn, segOpId, qty = parse_reserve_segment(segment);
            if segTxn == txn and qty ~= nil then
                return true;
            end
        end
        idx = idx + 1;
    end
    return false;
end

local function remove_transaction_from_reserve(reservevalue, txn)
    if reservevalue == 0 or reservevalue == false then
        return '0', 0;
    end
    local removedQty = 0;
    local base = '0';
    local newSegments = '';
    local idx = 0;
    for segment in string.gmatch(reservevalue, '([^' .. split .. ']+)') do
        if idx == 0 then
            base = segment;
        else
            local segTxn, segOpId, segQty = parse_reserve_segment(segment);
            if segQty == nil then
                return nil, 0;
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
        return normalize_redis_string(base), removedQty;
    end
    return normalize_redis_string(base .. split .. newSegments), removedQty;
end

local function compute_new_current_value(currvalue, txn, opType)
    local uqty = 0;
    local uvalue = '';
    local i = 0;
    for sku in string.gmatch(currvalue, '([^' .. split .. ']+)') do
        if i == 0 then
            -- 基量允许负整数，与允许负库存仓 commit 结果及 override 重算一致
            local baseQty = strict_signed_int(sku);
            if baseQty == nil then
                return nil;
            end
            uqty = baseQty;
        else
            local segTxn, segOpId, segQty = parse_try_segment(sku);
            if segTxn == txn and segQty ~= nil then
                if opType == 'commit' then
                    uqty = uqty + segQty;
                end
            else
                uvalue = uvalue .. split .. sku;
            end
        end
        i = i + 1;
    end
    return uqty .. uvalue;
end

local members = redis.call('SMEMBERS', key);
local unallocPlans = {};
local inventoryPlans = {};
local orphanMarkers = {};
local processedReserveKeys = {};
local beforetransactions = key .. '==';
local beforeinventorys = '';
local afterinventorys = '';
local txnIndex = 0;
local invIndex = 0;

-- Phase 1: 解析并校验，不写 Redis
for _, v in ipairs(members) do
    if string.sub(v, 1, string.len(unallocMarkerPrefix)) == unallocMarkerPrefix then
        local markerBody = string.sub(v, string.len(unallocMarkerPrefix) + 1);
        local markerParts = split_delimited_fields(markerBody, delimiter);
        if #markerParts >= 2 then
            local warehouseId = markerParts[1];
            local skuId = markerParts[2];
            local reserveKey = reservePrefix .. warehouseId .. ':' .. skuId;
            if processedReserveKeys[reserveKey] then
                table.insert(orphanMarkers, v);
            else
            local entityKey = entityPrefix .. warehouseId .. ':' .. skuId;
            local virtualKey = virtualPrefix .. warehouseId .. ':' .. skuId;
            local reserveValue = redis.call('get', reserveKey);
            if not reserve_has_transaction(reserveValue, transaction) then
                result['success'] = false;
                result['errormsg'] = '未分配预占reserve不存在 reserveKey=' .. reserveKey;
                return cjson.encode(result);
            end
            local newReserveValue, removedQty = remove_transaction_from_reserve(reserveValue, transaction);
            if newReserveValue == nil then
                result['success'] = false;
                result['errormsg'] = '未分配预占reserve片段非法 reserveKey=' .. reserveKey;
                return cjson.encode(result);
            end
            if removedQty <= 0 then
                result['success'] = false;
                result['errormsg'] = '未分配预占reserve未找到当前事务 reserveKey=' .. reserveKey;
                return cjson.encode(result);
            end
            processedReserveKeys[reserveKey] = true;
            table.insert(unallocPlans, {
                marker = v,
                reserveKey = reserveKey,
                entityKey = entityKey,
                virtualKey = virtualKey,
                reserveValue = reserveValue,
                newReserveValue = newReserveValue
            });
            end
        else
            table.insert(orphanMarkers, v);
        end
    else
        local currkey = (current .. v);
        local currvalue = redis.call('get', currkey);
        if currvalue == 0 or currvalue == false then
            result['success'] = false;
            result['errormsg'] = '即时库存key不存在' .. currkey;
            return cjson.encode(result);
        end
        local newvalue = compute_new_current_value(currvalue, transaction, type);
        if newvalue == nil then
            result['success'] = false;
            result['errormsg'] = '即时库存TRY片段非法 key=' .. currkey;
            return cjson.encode(result);
        end
        table.insert(inventoryPlans, {
            inventoryId = v,
            currkey = currkey,
            currvalue = currvalue,
            newvalue = newvalue
        });
        if txnIndex == 0 then
            beforetransactions = beforetransactions .. v;
        else
            beforetransactions = beforetransactions .. ',,' .. v;
        end
        txnIndex = txnIndex + 1;
    end
end

-- Phase 2: 全部校验通过后统一写入
for _, plan in ipairs(unallocPlans) do
    if invIndex == 0 then
        beforeinventorys = beforeinventorys .. plan.reserveKey .. '==' .. tostring(plan.reserveValue);
        afterinventorys = afterinventorys .. plan.reserveKey .. '==' .. tostring(plan.newReserveValue);
    else
        beforeinventorys = beforeinventorys .. ',,' .. plan.reserveKey .. '==' .. tostring(plan.reserveValue);
        afterinventorys = afterinventorys .. ',,' .. plan.reserveKey .. '==' .. tostring(plan.newReserveValue);
    end
    redis.call('set', plan.reserveKey, plan.newReserveValue);
    redis.call('del', plan.entityKey);
    redis.call('del', plan.virtualKey);
    redis.call('SREM', key, plan.marker);
    invIndex = invIndex + 1;
end

for _, plan in ipairs(inventoryPlans) do
    if invIndex == 0 then
        beforeinventorys = beforeinventorys .. plan.currkey .. '==' .. plan.currvalue;
        afterinventorys = afterinventorys .. plan.currkey .. '==' .. plan.newvalue;
    else
        beforeinventorys = beforeinventorys .. ',,' .. plan.currkey .. '==' .. plan.currvalue;
        afterinventorys = afterinventorys .. ',,' .. plan.currkey .. '==' .. plan.newvalue;
    end
    redis.call('set', plan.currkey, plan.newvalue);
    redis.call('SREM', key, plan.inventoryId);
    invIndex = invIndex + 1;
end

for _, orphan in ipairs(orphanMarkers) do
    redis.call('SREM', key, orphan);
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
