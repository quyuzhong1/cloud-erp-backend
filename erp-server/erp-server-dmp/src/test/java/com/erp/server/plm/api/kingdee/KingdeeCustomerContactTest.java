package com.erp.server.plm.api.kingdee;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.kingdee.bos.webapi.entity.OperatorResult;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * 直接调拨单 测试
 */
public class KingdeeCustomerContactTest {
    public static void main(String[] args) {
        KingdeeApiUtils customerApiService = new KingdeeApiUtils("BD_CommonContact");
        // 列表字段
        String fieldKeys = "FCONTACTID,FCustId,FDocumentStatus,FForbidStatus,FName,FNumber,FBizLocNumber,FBizLocation,FBizAddress";
        LinkedList<String> queryFilters = new LinkedList<>();
//        queryFilters.add(StrUtil.format("FNumber in ({})", "'CUST2694'"));
//        queryFilters.add(StrUtil.format("FCustId in ('{}','{}','{}','{}','{}','{}','{}','{}','{}','{}')", "222165",
//                "257593",
//                "192849",
//                "168940",
//                "138322",
//                "111889",
//                "176609",
//                "213498",
//                "179856",
//                "126840"));
//        queryFilters.add(StrUtil.format("FCustId = '{}'", "126840"));
        String filterStr = String.join(" and ",  queryFilters );
        // 过滤条件
        List<Map<String, Object>> maps = customerApiService.queryList(filterStr, fieldKeys, 10000, 1, 0);
        maps.parallelStream().forEach(map -> {
            // 先检查禁用状态
            if ("B".equalsIgnoreCase(String.valueOf(map.get("ForbidStatus")))) {
                System.out.println(StrUtil.format("编码为“{}”的联系人已禁用", map.get("FNumber")));
                return;
            }
            // 状态为C先反审核
            try {
                if("C".equalsIgnoreCase(String.valueOf(map.get("FDocumentStatus"))) || "B".equalsIgnoreCase(String.valueOf(map.get("FDocumentStatus"))) ){
                    OperatorResult unAuditResult = customerApiService.unAuditById(Arrays.asList(String.valueOf(map.get("FCONTACTID"))));
                }
            }catch (Exception e){
                System.out.println(e.getMessage());
                return;
            }
            // 审核后再删除
            try {
                OperatorResult deleteResult = customerApiService.deleteById(Arrays.asList(String.valueOf(map.get("FCONTACTID"))));
                System.out.println(StrUtil.format("删除 编码为“{}”的联系人FCustId={}  {}", map.get("FNumber"),  map.get("FCustId"), JSONUtil.toJsonStr(deleteResult)));
            }catch (Exception e){
                System.out.println(e.getMessage());
                // 删除失败后提交
                customerApiService.submit(Arrays.asList(String.valueOf(map.get("FCONTACTID"))));
                customerApiService.auditById(Arrays.asList(String.valueOf(map.get("FCONTACTID"))));
            }
            // 提交后再审核

        });

    }


}
