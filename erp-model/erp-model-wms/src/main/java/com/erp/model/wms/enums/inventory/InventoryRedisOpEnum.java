package com.erp.model.wms.enums.inventory;

import java.util.EnumMap;
import java.util.stream.Stream;

import org.springframework.data.redis.core.script.DefaultRedisScript;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

import cn.hutool.core.io.FileUtil;
import lombok.Getter;


/**
 * 库存redis操作枚举
 * @author Administrator
 *
 */
@Getter
public enum InventoryRedisOpEnum implements EnumMessage {
	OVERRIDE("override","重算流水" , InventoryRedisOpEnum.OVERRIDE_LUA),
	COMMIT("commit","提交流水" , InventoryRedisOpEnum.COMMIT_OR_ROLLBACK_LUA),
	ROLLBACK("rollback","回滚流水" , InventoryRedisOpEnum.COMMIT_OR_ROLLBACK_LUA),
    ;
	
	private static final String OVERRIDE_LUA = "local delimiter = '&&'; local i = 0; local key = ARGV[1]; local qty = ARGV[2]; local value = redis.call('get', key); if value == 0 or value == false then     redis.call('set', key, qty); else     for sku in string.gmatch(value, '([^' .. delimiter .. ']+)') do         i = i + 1;     end     if i > 1 then         return '存在未提交流水&&1000'     else         redis.call('set', key, qty);     end end return '0'; ";
	private static final String COMMIT_OR_ROLLBACK_LUA = "local split = '&&'; local delimiter = '@@'; local type = ARGV[1]; local key = ARGV[2]; local value = redis.call('SMEMBERS', key); for _, v in ipairs(value) do     local currvalue = redis.call('get', v);     if currvalue == 0 or currvalue == false then         return '即时库存key不存在' .. v;     else         local uqty = 0;         local uvalue = split;         local i = 0;         for sku in string.gmatch(currvalue, '([^' .. split .. ']+)') do             if i == 0 then                 uqty = sku;             else                 local flag = 0;                 for s in string.gmatch(sku, '([^' .. delimiter .. ']+)') do                     if flag == 0 and s == key then                         flag = 1;                     else                         if type == 'commit' and flag == 1 then                             uqty = uqty + s;                         end                     end                 end                 if flag == 0 then                     uvalue = uvalue .. sku .. split;                 end             end             i = i + 1;         end         local newvalue = uqty .. uvalue;         redis.call('set', v, newvalue);         redis.call('SREM', key, v);     end end return '0'; ";
	
    InventoryRedisOpEnum(String code, String name , String luaScript) {
        this.code = code;
        this.name = name;
        this.luaScript = luaScript;
    }
    
    private static EnumMap<InventoryRedisOpEnum, DefaultRedisScript<String>> opRedisScript;
    static {
    	opRedisScript = new EnumMap<>(InventoryRedisOpEnum.class);
    	InventoryRedisOpEnum[] values = InventoryRedisOpEnum.values();
    	for(InventoryRedisOpEnum v : values) {
    		DefaultRedisScript<String> defaultRedisScript = new DefaultRedisScript<>();
        	defaultRedisScript.setResultType(String.class);
        	defaultRedisScript.setScriptText(v.luaScript);
        	opRedisScript.put(v, defaultRedisScript);
    	}
    }
    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;
    
    /**
     * lua脚本
     */
    private String luaScript;

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * 通过code查询
     * DeliverTypeEnum
     * 枚举
     */
    public static InventoryRedisOpEnum getByCode(String code) {
        return Stream.of(InventoryRedisOpEnum.values())
                .filter(e -> e.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }
    
    public static DefaultRedisScript<String> getDefaultRedisScript(InventoryRedisOpEnum inventoryRedisOpEnum){
    	return opRedisScript.get(inventoryRedisOpEnum);
    }
    
    public static void main(String[] args) {
		String s = FileUtil.readUtf8String("C:\\Users\\Administrator\\Desktop\\test.lua");
		System.out.print("eval \"" + s.replace("\r\n", " ") + "\"0 commit t1");
	}
}
