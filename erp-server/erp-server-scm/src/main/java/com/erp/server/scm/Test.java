package com.erp.server.scm;

import com.common.core.utils.EnumCacheUtils;

import java.util.List;
import java.util.Map;

/**
 * @Classname: Test
 * @Description: TODO
 * @CreateTime: 2023-04-13  19:26
 * @Author: zhangchunlin
 */
public class Test {

    public static void main(String[] args) {
        Map<String, List<Map<String,Object>>> enumMaps = EnumCacheUtils.getInstance().getData();
        List<Map<String,Object>> enumList = enumMaps.get("ApprovalStatus");
        System.out.println(enumList);
    }

}