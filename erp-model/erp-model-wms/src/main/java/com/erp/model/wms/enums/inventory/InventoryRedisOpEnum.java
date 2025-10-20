package com.erp.model.wms.enums.inventory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.EnumMap;
import java.util.stream.Stream;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.business.constant.BusinessCommonConstants;
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
	OVERRIDE("override","重算流水" , "override.lua"),
	TRY("try","准备流水" , "try.lua"),
	COMMIT("commit","提交流水" , "commitAndRollback.lua"),
	ROLLBACK("rollback","回滚流水" , "commitAndRollback.lua"),
    ;
	
    InventoryRedisOpEnum(String code, String name , String luaScript) {
        this.code = code;
        this.name = name;
        this.luaScript = luaScript;
    }
    
    private static volatile EnumMap<InventoryRedisOpEnum, DefaultRedisScript<String>> opRedisScript = new EnumMap<>(InventoryRedisOpEnum.class);
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
//    	if(BusinessCommonConstants.hasProfile("dev")) {
//    		String luaBasePath = ClassLoader.getSystemResource("").getPath().split("/target/classes")[0] + "/src/main/resources/lua/";
//    		DefaultRedisScript<String> defaultRedisScript = new DefaultRedisScript<>();
//        	defaultRedisScript.setResultType(String.class);
//        	defaultRedisScript.setScriptText(FileUtil.readUtf8String(luaBasePath + inventoryRedisOpEnum.getLuaScript()));
//        	return defaultRedisScript;
//    	}
    	if(opRedisScript.get(inventoryRedisOpEnum) == null) {
    		synchronized (InventoryRedisOpEnum.class) {
    			if(opRedisScript.get(inventoryRedisOpEnum) == null) {
        	    	InventoryRedisOpEnum[] values = InventoryRedisOpEnum.values();
        	    	for(InventoryRedisOpEnum v : values) {
        	    		DefaultRedisScript<String> defaultRedisScript = new DefaultRedisScript<>();
        	    		defaultRedisScript.setResultType(String.class);
        	        	StringBuilder sb = new StringBuilder();
						try {
							InputStream inputStream = new ClassPathResource("lua/" + v.luaScript).getInputStream();
							BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
							String line;
				            while ((line = reader.readLine()) != null) {
				            	sb.append(line);
				            	sb.append("\n");
				            }
						} catch (Exception e) {
							throw new RuntimeException("获取lua脚本文件失败" + v.luaScript, e);
						}
						defaultRedisScript.setScriptText(sb.toString());
        	        	opRedisScript.put(v, defaultRedisScript);
        	    	}
    			}
			}
    	}
    	return opRedisScript.get(inventoryRedisOpEnum);
    }
    
    public static void main(String[] args) {
    	String luaBasePath = ClassLoader.getSystemResource("").getPath().split("erp-model/erp-model-wms")[0] + "\\erp-server\\erp-server-wms\\src\\main\\resources\\lua\\try.lua";
    	String scriptAsString = FileUtil.readUtf8String(luaBasePath);
		System.out.println("eval \"" + scriptAsString.replace("\r\n", " ") + "\"0 1995 override: current: transaction: sk@@5@@库存不足：sku=[{}],仓库=[{}],仓位=[{}],库存状态=[{}],库存:{ss1ss},交易数:{5},缺少数：{ss2ss}");
    	String cluaBasePath = ClassLoader.getSystemResource("").getPath().split("erp-model/erp-model-wms")[0] + "\\erp-server\\erp-server-wms\\src\\main\\resources\\lua\\commitAndRollback.lua";
    	String cscriptAsString = FileUtil.readUtf8String(cluaBasePath);
		System.out.print("eval \"" + cscriptAsString.replace("\r\n", " ") + "\"0 commit 1995 transaction:1995 current:");
	}
}
