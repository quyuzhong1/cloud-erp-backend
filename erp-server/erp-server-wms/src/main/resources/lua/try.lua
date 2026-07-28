local split = '&&';
local delimiter = '@@';
local transaction = ARGV[1];
local override = ARGV[2];
local current = ARGV[3];
local transactionkey = ARGV[4];
local params = ARGV[5];
local unallocParams = ARGV[6];
local operationId = ARGV[7];
local unallocMarkerPrefix = 'unalloc@@';
local transactionrediskey = (transactionkey .. transaction);

-- 须与 Java VirtualInventoryUnallocCheckHelper.UNALLOC_LUA_ERROR_PREFIX 完全一致
local unalloc_lua_error_prefix = 'VM_CHECK_OUT_VIRTUAL_INVENTORY@@';
-- 须与 Java InventoryRedisUtil.INVENTORY_LUA_BIZ_ERROR_PREFIX 完全一致
local inventory_lua_biz_prefix = 'INVENTORY_LUA_BIZ@@';

local function biz_error(msg)
    if msg == nil or msg == '' then
        msg = '库存Lua业务失败';
    end
    if string.sub(msg, 1, string.len(unalloc_lua_error_prefix)) ~= unalloc_lua_error_prefix
            and string.sub(msg, 1, string.len(inventory_lua_biz_prefix)) ~= inventory_lua_biz_prefix then
        msg = inventory_lua_biz_prefix .. msg;
    end
    return redis.error_reply(msg);
end

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

-- 仓位 TRY 段数量为有符号整数（出库为负）；与 reserve 预占（非负）区分
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

local function sum_reserve_pending(reservevalue, exclude_txn, exclude_opId)
    local pending = 0;
    if reservevalue == 0 or reservevalue == false then
        return pending, nil;
    end
    local idx = 0;
    for segment in string.gmatch(reservevalue, '([^' .. split .. ']+)') do
        if idx > 0 then
            local segTxn, segOpId, qty = parse_reserve_segment(segment);
            if qty == nil then
                return nil, '预占片段数量非法';
            end
            local isExcluded = false;
            if exclude_txn ~= nil and exclude_txn ~= '' and exclude_opId ~= nil and exclude_opId ~= ''
                    and segTxn == exclude_txn and segOpId == exclude_opId then
                isExcluded = true;
            end
            if not isExcluded then
                pending = pending + qty;
            end
        end
        idx = idx + 1;
    end
    return pending, nil;
end

local function find_reserve_for_operation(reservevalue, txn, opId)
    if reservevalue == 0 or reservevalue == false then
        return nil;
    end
    local idx = 0;
    for segment in string.gmatch(reservevalue, '([^' .. split .. ']+)') do
        if idx > 0 then
            local segTxn, segOpId, qty = parse_reserve_segment(segment);
            if segTxn == txn and segOpId == opId then
                return qty;
            end
        end
        idx = idx + 1;
    end
    return nil;
end

local function find_try_qty_for_operation(currentvalue, txn, opId)
    if currentvalue == 0 or currentvalue == false then
        return nil;
    end
    local d = 0;
    for cv in string.gmatch(currentvalue, '([^' .. split .. ']+)') do
        if d > 0 then
            local segTxn, segOpId, segQty = parse_try_segment(cv);
            if segTxn == txn and segOpId == opId then
                return segQty;
            end
        end
        d = d + 1;
    end
    return nil;
end

local function append_reserve(reservekey, reservevalue, txn, opId, qty)
    local segment = txn .. delimiter .. opId .. delimiter .. qty;
    if reservevalue == 0 or reservevalue == false then
        return '0' .. split .. segment;
    end
    return reservevalue .. split .. segment;
end

local function format_unalloc_error(errorTemplate, available, virtualQty)
    local allowed = available;
    if allowed < 0 then
        allowed = 0;
    end
    local msg = string.gsub(errorTemplate, 'ssvss', tostring(virtualQty));
    return string.gsub(msg, 'ss1ss', tostring(allowed));
end

local function unalloc_biz_error(errorTemplate, available, virtualQty)
    return biz_error(unalloc_lua_error_prefix .. format_unalloc_error(errorTemplate, available, virtualQty));
end

local function base_current_qty(currentvalue)
    if currentvalue == 0 or currentvalue == false then
        return 0;
    end
    local d = 0;
    for cv in string.gmatch(currentvalue, '([^' .. split .. ']+)') do
        if d == 0 then
            local baseQty = strict_nonneg_int(cv);
            if baseQty == nil then
                return nil;
            end
            return baseQty;
        end
        d = d + 1;
    end
    return 0;
end

local function sum_entity_from_inventory_ids(inventoryIds, currentPrefix)
    if inventoryIds == nil or inventoryIds == '' then
        return 0, nil;
    end
    local total = 0;
    for inventoryId in string.gmatch(inventoryIds, '([^,]+)') do
        if inventoryId ~= nil and inventoryId ~= '' then
            local currentkey = (currentPrefix .. inventoryId);
            local currentvalue = redis.call('get', currentkey);
            local baseQty = base_current_qty(currentvalue);
            if baseQty == nil then
                return nil, currentkey;
            end
            total = total + baseQty;
        end
    end
    return total, nil;
end

if operationId == nil or operationId == '' then
    return biz_error('TRY operationId不能为空');
end

local unallocWritePlans = {};
-- Phase 1: 只读校验未分配，不写镜像/reserve
if unallocParams ~= nil and unallocParams ~= '' then
    for unallocParam in string.gmatch(unallocParams, '([^' .. split .. ']+)') do
        local up = split_delimited_fields(unallocParam, delimiter);
        if #up ~= 9 then
            return biz_error('未分配参数字段不完整 expected=9 actual=' .. tostring(#up));
        end
        local warehouseId = up[1];
        local skuId = up[2];
        local outboundQty = strict_nonneg_int(up[3]);
        local reserveKey = up[4];
        local entityKey = up[5];
        local virtualKey = up[6];
        local inventoryIds = up[7];
        local virtualQty = strict_nonneg_int(up[8]);
        local errorTemplate = up[9];
        if outboundQty == nil or virtualQty == nil then
            return biz_error('未分配数量参数非法');
        end
        if virtualQty > 0 and (inventoryIds == nil or inventoryIds == '') then
            return biz_error('未分配校验缺少实体inventoryId列表');
        end
        local entityQty, illegalEntityKey = sum_entity_from_inventory_ids(inventoryIds, current);
        if illegalEntityKey ~= nil then
            return biz_error('实体库存基量非法 key=' .. illegalEntityKey);
        end
        local reserveValue = redis.call('get', reserveKey);
        local existingQty = find_reserve_for_operation(reserveValue, transaction, operationId);
        if existingQty ~= nil then
            if existingQty ~= outboundQty then
                return unalloc_biz_error(errorTemplate, 0, virtualQty);
            end
        else
            local pending, pendingErr = sum_reserve_pending(reserveValue, nil, nil);
            if pendingErr ~= nil then
                return biz_error(pendingErr);
            end
            local available = entityQty - virtualQty - pending;
            if outboundQty > available then
                return unalloc_biz_error(errorTemplate, available, virtualQty);
            end
            table.insert(unallocWritePlans, {
                reserveKey,
                append_reserve(reserveKey, reserveValue, transaction, operationId, outboundQty),
                unallocMarkerPrefix .. warehouseId .. delimiter .. skuId,
                entityKey,
                entityQty,
                virtualKey,
                virtualQty
            });
        end
    end
end

local inventorys = {};
if params ~= nil and params ~= '' then
    for param in string.gmatch(params, '([^' .. split .. ']+)') do
        for p in string.gmatch(param, '([^' .. delimiter .. ']+)') do
            table.insert(inventorys, p);
            break;
        end
    end
end

for _, inventory in ipairs(inventorys) do
    local overridekey = (override .. inventory);
    local overridevalue = redis.call('get', overridekey);
    if overridevalue ~= 0 and overridevalue ~= false then
        local result = {};
        result['success'] = false;
        result['sleep'] = 1000;
        result['errormsg'] = inventory .. '正在做库存重算';
        return cjson.encode(result);
    end
end

local newcurrentvaluearr = {};
if params ~= nil and params ~= '' then
    local errormsg = '';
    local errorflag = 0;
    for param in string.gmatch(params, '([^' .. split .. ']+)') do
        local paramlist = {};
        local c = 0;
        for p in string.gmatch(param, '([^' .. delimiter .. ']+)') do
            table.insert(paramlist, p);
            c = c + 1;
        end
        local currentkey = (current .. paramlist[1]);
        local currentvalue = redis.call('get', currentkey);
        if currentvalue == 0 or currentvalue == false then
            currentvalue = '0';
        end
        local oqty = strict_signed_int(paramlist[2]);
        if oqty == nil then
            return biz_error('仓位TRY数量非法 inventoryId=' .. tostring(paramlist[1]));
        end
        local existingTryQty = find_try_qty_for_operation(currentvalue, transaction, operationId);
        if existingTryQty ~= nil then
            if existingTryQty ~= oqty then
                return biz_error('TRY事务数量不一致 transaction=' .. transaction
                        .. ' operationId=' .. operationId
                        .. ' reserved=' .. tostring(existingTryQty)
                        .. ' request=' .. tostring(oqty));
            end
        else
            local d = 0;
            local currentqty = 0;
            for cv in string.gmatch(currentvalue, '([^' .. split .. ']+)') do
                if d == 0 then
                    local baseQty = strict_nonneg_int(cv);
                    if baseQty == nil then
                        return biz_error('即时库存基量非法 key=' .. currentkey);
                    end
                    currentqty = baseQty;
                else
                    local segTxn, segOpId, segQty = parse_try_segment(cv);
                    if segTxn == nil or segQty == nil then
                        return biz_error('即时库存TRY片段非法 key=' .. currentkey);
                    end
                    -- 负向在途段计入全部事务；正向在途段仅计入本事务（与 hotfix 前 try.lua 一致）
                    if segQty < 0 or segTxn == transaction then
                        currentqty = currentqty + segQty;
                    end
                end
                d = d + 1;
            end
            local updateqty = (currentqty + oqty);
            if updateqty < 0 and c > 2 then
                errorflag = 1;
                local kcbu = string.gsub(paramlist[3], 'ss1ss', currentqty);
                errormsg = (errormsg .. string.gsub(kcbu, 'sslss', -updateqty));
            end
            local trySegment = transaction .. delimiter .. operationId .. delimiter .. oqty;
            table.insert(newcurrentvaluearr, {paramlist[1], currentkey, (currentvalue .. split .. trySegment)});
        end
    end
    if errorflag == 1 then
        local result = {};
        result['success'] = false;
        result['sleep'] = 1000;
        result['errormsg'] = errormsg;
        return cjson.encode(result);
    end
end

if #unallocWritePlans > 0 and (params == nil or params == '') then
    return biz_error('未分配预占缺少仓位库存参数');
end

for _, plan in ipairs(unallocWritePlans) do
    redis.call('set', plan[4], tostring(plan[5]));
    redis.call('set', plan[6], tostring(plan[7]));
    redis.call('set', plan[1], plan[2]);
    redis.call('SADD', transactionrediskey, plan[3]);
end

local beforetransactions = transactionrediskey .. '==';
local beforetransactionsvalue = redis.call('SMEMBERS', transactionrediskey);
local indexbeforetransactions = 0;
for _, beforetransactionsv in ipairs(beforetransactionsvalue) do
    if indexbeforetransactions == 0 then
        beforetransactions = beforetransactions .. beforetransactionsv;
    else
        beforetransactions = beforetransactions .. ',,' .. beforetransactionsv;
    end
    indexbeforetransactions = indexbeforetransactions + 1;
end

local beforeinventorys = '';
local afterinventorys = '';
local newcurrentvaluearrindex = 0;
for _, newc in ipairs(newcurrentvaluearr) do
    local beforeinv = redis.call('get', newc[2]);
    if newcurrentvaluearrindex == 0 then
        beforeinventorys = beforeinventorys .. newc[2] .. '==' .. beforeinv;
    else
        beforeinventorys = beforeinventorys .. ',,' .. newc[2] .. '==' .. beforeinv;
    end
    redis.call('SADD', transactionrediskey, newc[1]);
    redis.call('set', newc[2], newc[3]);
    if newcurrentvaluearrindex == 0 then
        afterinventorys = afterinventorys .. newc[2] .. '==' .. newc[3];
    else
        afterinventorys = afterinventorys .. ',,' .. newc[2] .. '==' .. newc[3];
    end
    newcurrentvaluearrindex = newcurrentvaluearrindex + 1;
end

local aftertransactions = transactionrediskey .. '==';
local aftertransactionsvalue = redis.call('SMEMBERS', transactionrediskey);
local indexaftertransactions = 0;
for _, aftertransactionsv in ipairs(aftertransactionsvalue) do
    if indexaftertransactions == 0 then
        aftertransactions = aftertransactions .. aftertransactionsv;
    else
        aftertransactions = aftertransactions .. ',,' .. aftertransactionsv;
    end
    indexaftertransactions = indexaftertransactions + 1;
end

local result = {};
result['success'] = true;
result['beforetransactions'] = beforetransactions;
result['beforeinventorys'] = beforeinventorys;
result['aftertransactions'] = aftertransactions;
result['afterinventorys'] = afterinventorys;
local opallcount = redis.call('INCRBY', 'inventory:op:all', 1);
local trycount = redis.call('INCRBY', 'inventory:op:try', 1);
result['opallcount'] = opallcount;
result['trycount'] = trycount;
return cjson.encode(result);
