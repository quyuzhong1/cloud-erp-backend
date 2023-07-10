package com.common.business.constant;

/**
 * @Classname: LuaScript
 * @Description: LUA脚本常量
 * @CreateTime: 2023-07-10  10:19
 * @Author: zhangchunlin
 */
public interface LuaScript {

    /**
     * 生成递增单号，并设置过期时间
     */
    String GEN_DOC_NO_SCRIPT = "local key = KEYS[1] local increment = ARGV[1]  local cnt =  redis.call('INCRBY',key,increment) if cnt == 1 then redis.call('expire',key,ARGV[2]) end return cnt";

}
