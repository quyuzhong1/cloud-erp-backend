local split = '&&';
local delimiter = '@@';
local transaction = ARGV[1];
local override = ARGV[2];
local current = ARGV[3];
local transactionkey = ARGV[4];
local params = ARGV[5];
local inventorys = {};
local result = {};
local a = 0;
for param in string.gmatch(params, '([^' .. split .. ']+)') do
    for p in string.gmatch(param, '([^' .. delimiter .. ']+)') do
        table.insert(inventorys, p);
        break;
    end
    a = a + 1;
end

for index , inventory in ipairs(inventorys) do
    local overridekey = (override .. inventory);
    local overridevalue = redis.call('get' , overridekey);
    if overridevalue == 0 or overridevalue == false then
        
    else
        result['success'] = false;
        result['sleep'] = 1000;
        result['errormsg'] = inventory .. '正在做库存重算';
        return cjson.encode(result);
    end
end

local b = 0;
local errorflag = 0;
local errormsg = '';
local newcurrentvaluearr = {};
for param in string.gmatch(params, '([^' .. split .. ']+)') do
    local paramlist = {};
    local c = 0;
    for p in string.gmatch(param, '([^' .. delimiter .. ']+)') do
        table.insert(paramlist, p);
        c = c + 1;
    end
    local currentkey = (current .. paramlist[1]);
    local currentvalue = redis.call('get' , currentkey);
    if currentvalue == 0 or currentvalue == false then
        redis.call('set' , currentkey , '0');
        currentvalue = '0';
    end
    local d = 0;
    local currentqty = 0;
    for cv in string.gmatch(currentvalue, '([^' .. split .. ']+)') do
        if d == 0 then
            currentqty = cv;
        else
            local e = 0;
            local eflag = 0;
            for cs in string.gmatch(cv, '([^' .. delimiter .. ']+)') do
                if e == 0 and transaction == cs then
                    eflag = 1;
                end
                if e > 0 then
                    local ncs = tonumber(cs);
                    if ncs < 0 or eflag == 1 then
                        currentqty = currentqty + ncs;
                    end 
                end
                e = e + 1;
            end
        end
        d = d + 1;
    end
    local oqty = paramlist[2];
    local updateqty = (currentqty + oqty);
    if updateqty < 0 and c > 2 then
        errorflag = 1;
        local kcbu = string.gsub(paramlist[3], 'ss1ss', currentqty);
        errormsg = (errormsg .. string.gsub(kcbu, 'ss2ss', -updateqty));
    end
    b = b + 1;
    newcurrentvaluearr[b] = {paramlist[1] , currentkey , (currentvalue .. split .. transaction .. delimiter .. oqty)};
end
if errorflag == 1 then
    result['success'] = false;
    result['sleep'] = 1000;
    result['errormsg'] = errormsg;
    return cjson.encode(result);
end

local transactionrediskey = (transactionkey .. transaction);
local beforetransactions = transactionrediskey .. '==';
local beforetransactionsvalue = redis.call('SMEMBERS', transactionrediskey);
for _, beforetransactionsv in ipairs(beforetransactionsvalue) do
    beforetransactions = beforetransactions .. beforetransactionsv .. ',';
end
local beforeinventorys = '';
local afterinventorys = '';
for index ,newc in ipairs(newcurrentvaluearr) do
    local beforeinv = redis.call('get' , newc[2]);
    beforeinventorys = beforeinventorys .. newc[2] .. '==' .. beforeinv .. ',';
    redis.call('SADD' , transactionrediskey , newc[1]);
    redis.call('set' , newc[2] , newc[3]);
    afterinventorys = afterinventorys .. newc[2] .. '==' .. newc[3] .. ',';
end
local aftertransactions = transactionrediskey;
local aftertransactionsvalue = redis.call('SMEMBERS', transactionrediskey);
for _, aftertransactionsv in ipairs(aftertransactionsvalue) do
    aftertransactions = aftertransactions .. ',' .. aftertransactionsv;
end
result['success'] = true;
result['beforetransactions'] = beforetransactions;
result['beforeinventorys'] = beforeinventorys;
result['aftertransactions'] = aftertransactions;
result['afterinventorys'] = afterinventorys;
local opallcount = redis.call('INCRBY' , 'inventory:op:all' , 1);
local trycount = redis.call('INCRBY' , 'inventory:op:try' , 1);
result['opallcount'] = opallcount;
result['trycount'] = trycount;
return cjson.encode(result);
