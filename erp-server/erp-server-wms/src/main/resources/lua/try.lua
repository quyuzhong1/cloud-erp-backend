local split = '&&';
local delimiter = '@@';
local transaction = ARGV[1];
local override = ARGV[2];
local current = ARGV[3];
local transactionkey = ARGV[4];
local params = ARGV[5];
local inventorys = {};
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
        return inventory .. '正在做库存重算&&1000';
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
    local newcurrentvalue = '';
    if currentvalue == 0 or currentvalue == false then
        redis.call('set' , currentkey , '0');
        currentvalue = '0';
    end
    newcurrentvalue = (newcurrentvalue .. currentvalue);
    local d = 0;
    local currentqty = 0;
    for cv in string.gmatch(currentvalue, '([^' .. split .. ']+)') do
        if d == 0 then
            currentqty = cv;
        else
            newcurrentvalue = (newcurrentvalue .. cv);
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
    newcurrentvaluearr[b] = {paramlist[1] , currentkey , (newcurrentvalue .. split .. transaction .. delimiter .. oqty)};
end
if errorflag == 1 then
    return errormsg .. '&&1000';
end

for index ,newc in ipairs(newcurrentvaluearr) do
    redis.call('SADD' , (transactionkey .. transaction) , newc[1]);
    redis.call('set' , newc[2] , newc[3]);
end
return '0';
