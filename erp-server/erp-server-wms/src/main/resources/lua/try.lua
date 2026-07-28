local split = '&&';
local delimiter = '@@';
local transaction = ARGV[1];
local override = ARGV[2];
local current = ARGV[3];
local transactionkey = ARGV[4];
local params = ARGV[5];
local unallocParams = ARGV[6];
local unallocMarkerPrefix = 'unalloc@@';
local transactionrediskey = (transactionkey .. transaction);

local function biz_error(msg)
    -- 未分配等业务校验：走 error_reply -> Java RedisSystemException（不重试）
    redis.error_reply(msg);
end

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

local function sum_reserve_pending(reservevalue, exclude_transaction)
    local pending = 0;
    if reservevalue == 0 or reservevalue == false then
        return pending;
    end
    local idx = 0;
    for segment in string.gmatch(reservevalue, '([^' .. split .. ']+)') do
        if idx > 0 then
            local partIdx = 0;
            local txn = '';
            local qty = 0;
            for s in string.gmatch(segment, '([^' .. delimiter .. ']+)') do
                if partIdx == 0 then
                    txn = s;
                else
                    qty = safe_tonumber(s, 0);
                end
                partIdx = partIdx + 1;
            end
            if txn ~= exclude_transaction then
                pending = pending + qty;
            end
        end
        idx = idx + 1;
    end
    return pending;
end

local function find_reserve_for_transaction(reservevalue, txn)
    if reservevalue == 0 or reservevalue == false then
        return nil;
    end
    local idx = 0;
    for segment in string.gmatch(reservevalue, '([^' .. split .. ']+)') do
        if idx > 0 then
            local partIdx = 0;
            local segTxn = '';
            local qty = 0;
            for s in string.gmatch(segment, '([^' .. delimiter .. ']+)') do
                if partIdx == 0 then
                    segTxn = s;
                else
                    qty = safe_tonumber(s, 0);
                end
                partIdx = partIdx + 1;
            end
            if segTxn == txn then
                return qty;
            end
        end
        idx = idx + 1;
    end
    return nil;
end

local function append_reserve(reservekey, reservevalue, txn, qty)
    if reservevalue == 0 or reservevalue == false then
        return '0' .. split .. txn .. delimiter .. qty;
    end
    return reservevalue .. split .. txn .. delimiter .. qty;
end

local function format_unalloc_error(errorTemplate, available, virtualQty)
    local allowed = available;
    if allowed < 0 then
        allowed = 0;
    end
    local msg = string.gsub(errorTemplate, 'ssvss', tostring(virtualQty));
    return string.gsub(msg, 'ss1ss', tostring(allowed));
end

-- 须与 Java VirtualInventoryUnallocCheckHelper.UNALLOC_LUA_ERROR_PREFIX 完全一致（改 ApiError 枚举名时同步）
local unalloc_lua_error_prefix = 'VM_CHECK_OUT_VIRTUAL_INVENTORY@@';

local function unalloc_biz_error(errorTemplate, available, virtualQty)
    biz_error(unalloc_lua_error_prefix .. format_unalloc_error(errorTemplate, available, virtualQty));
end

-- 与 Java getRedisQtyByInventory 一致：基量 + 在途 TRY（负向或本事务段）
local function effective_current_qty(currentvalue, txn)
    if currentvalue == 0 or currentvalue == false then
        return 0;
    end
    local d = 0;
    local currentqty = 0;
    for cv in string.gmatch(currentvalue, '([^' .. split .. ']+)') do
        if d == 0 then
            currentqty = safe_tonumber(cv, 0);
        else
            local e = 0;
            local eflag = 0;
            for cs in string.gmatch(cv, '([^' .. delimiter .. ']+)') do
                if e == 0 and txn == cs then
                    eflag = 1;
                end
                if e > 0 then
                    local ncs = safe_tonumber(cs, 0);
                    if ncs < 0 or eflag == 1 then
                        currentqty = currentqty + ncs;
                    end
                end
                e = e + 1;
            end
        end
        d = d + 1;
    end
    return currentqty;
end

-- 未分配校验用基量（current 首段）；在途出库由 reserve pending 单独扣减，避免与 TRY 负向段双扣
local function base_current_qty(currentvalue)
    if currentvalue == 0 or currentvalue == false then
        return 0;
    end
    local d = 0;
    for cv in string.gmatch(currentvalue, '([^' .. split .. ']+)') do
        if d == 0 then
            return safe_tonumber(cv, 0);
        end
        d = d + 1;
    end
    return 0;
end

local function sum_entity_from_inventory_ids(inventoryIds, currentPrefix)
    if inventoryIds == nil or inventoryIds == '' then
        return 0;
    end
    local total = 0;
    for inventoryId in string.gmatch(inventoryIds, '([^,]+)') do
        local currentkey = (currentPrefix .. inventoryId);
        local currentvalue = redis.call('get', currentkey);
        total = total + base_current_qty(currentvalue);
    end
    return total;
end

local unallocWritePlans = {};
-- Phase 1: 只读校验未分配，不写镜像/reserve（避免后续仓位库存不足时镜像残留）
if unallocParams ~= nil and unallocParams ~= '' then
    for unallocParam in string.gmatch(unallocParams, '([^' .. split .. ']+)') do
        local up = {};
        local upIdx = 0;
        for p in string.gmatch(unallocParam, '([^' .. delimiter .. ']+)') do
            table.insert(up, p);
            upIdx = upIdx + 1;
        end
        if upIdx >= 9 then
            local warehouseId = up[1];
            local skuId = up[2];
            local outboundQty = safe_tonumber(up[3], 0);
            local reserveKey = up[4];
            local entityKey = up[5];
            local virtualKey = up[6];
            local inventoryIds = up[7];
            local virtualQty = safe_tonumber(up[8], 0);
            local errorTemplate = up[9];
            local entityQty = sum_entity_from_inventory_ids(inventoryIds, current);
            local reserveValue = redis.call('get', reserveKey);
            local existingQty = find_reserve_for_transaction(reserveValue, transaction);
            if existingQty ~= nil then
                if existingQty ~= outboundQty then
                    unalloc_biz_error(errorTemplate, 0, virtualQty);
                end
            else
                local pending = sum_reserve_pending(reserveValue, transaction);
                local available = entityQty - virtualQty - pending;
                if outboundQty > available then
                    unalloc_biz_error(errorTemplate, available, virtualQty);
                end
                table.insert(unallocWritePlans, {
                    reserveKey,
                    append_reserve(reserveKey, reserveValue, transaction, outboundQty),
                    unallocMarkerPrefix .. warehouseId .. delimiter .. skuId .. delimiter .. outboundQty,
                    entityKey,
                    entityQty,
                    virtualKey,
                    virtualQty
                });
            end
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
        local d = 0;
        local currentqty = 0;
        for cv in string.gmatch(currentvalue, '([^' .. split .. ']+)') do
            if d == 0 then
                currentqty = safe_tonumber(cv, 0);
            else
                local e = 0;
                local eflag = 0;
                for cs in string.gmatch(cv, '([^' .. delimiter .. ']+)') do
                    if e == 0 and transaction == cs then
                        eflag = 1;
                    end
                    if e > 0 then
                        local ncs = safe_tonumber(cs, 0);
                        if ncs < 0 or eflag == 1 then
                            currentqty = currentqty + ncs;
                        end
                    end
                    e = e + 1;
                end
            end
            d = d + 1;
        end
        local oqty = safe_tonumber(paramlist[2], 0);
        local updateqty = (currentqty + oqty);
        if updateqty < 0 and c > 2 then
            errorflag = 1;
            -- 仓位库存不足：返回 JSON {success:false,sleep} 供 Java 重试（与 biz_error 通道不同，见 InventoryRedisUtil）
            local kcbu = string.gsub(paramlist[3], 'ss1ss', currentqty);
            errormsg = (errormsg .. string.gsub(kcbu, 'sslss', -updateqty));
        end
        table.insert(newcurrentvaluearr, {paramlist[1], currentkey, (currentvalue .. split .. transaction .. delimiter .. oqty)});
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
    unalloc_biz_error('未分配预占缺少仓位库存参数', 0, 0);
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
