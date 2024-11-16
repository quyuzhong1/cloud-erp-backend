package com.erp.server.wms.schedule;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONArray;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.InventoryClosedRecordEnum;
import com.erp.model.wms.dto.extension.TStkCloseProfileDTO;
import com.erp.model.wms.entity.InventoryClosedRecordEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.sdk.third.kingdee.utils.KingdeeExtensionUtils;
import com.erp.server.wms.service.InventoryClosedRecordService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 金蝶扩展项目任务
 *
 * @author Jim
 * @since 2023-10-12
 */
@Component
@Slf4j
@EnableScheduling
public class KingdeeExtensionJob {

    @Resource
    private InventoryClosedRecordService inventoryClosedRecordService;

    @Resource
    private SysUserFeign sysUserFeign;

    /**
     * 拉取【关账时间】(金蝶->WMS)
     */
    @XxlJob("kingdeeExtStkCloseDateJob")
    public ReturnT<String> kingdeeExtStkCloseDateJob() {
        XxlJobHelper.log("[拉取【关账时间】(金蝶->WMS)]：开始执行");
        // 获取当前组织
        List<BaseIdDTO.CodeDTO> companyList = sysUserFeign.getAccountingCompanyList(Collections.emptyList());
        if (CollectionUtils.isEmpty(companyList)){
            XxlJobHelper.log("[拉取【关账时间】(金蝶->WMS)]：获取数据库当前组织列表为空");
            log.error("[拉取金蝶库存组织关账时间列表任务]：获取数据库当前组织列表为空");
            return ReturnT.FAIL;
        }
        // 请求获取最新库存组织关账时间列表
        String jsonArray = KingdeeExtensionUtils.queryStkClosedList();
        if (CharSequenceUtil.isBlank(jsonArray)) {
            XxlJobHelper.log("[拉取【关账时间】(金蝶->WMS)]：列表响应为空");
            log.error("[拉取金蝶库存组织关账时间列表任务]：列表响应为空");
            return ReturnT.FAIL;
        }
        List<TStkCloseProfileDTO> list = JSONArray.parseArray(JSONUtil.toJsonStr(jsonArray), TStkCloseProfileDTO.class);
        if (CollectionUtils.isEmpty(list)) {
            XxlJobHelper.log("[拉取【关账时间】(金蝶->WMS)]：列表为空");
            log.error("[拉取金蝶库存组织关账时间列表任务]：列表为空");
            return ReturnT.FAIL;
        }
        Map<String, List<TStkCloseProfileDTO>> map = list.stream().collect(Collectors.groupingBy(obj -> obj.getFOrgId().toString()));

        // 转换新记录
        List<InventoryClosedRecordEntity> newEntityList = companyList.stream()
                // 检查当前金蝶组织ID是否存在最新库存组织关账时间列表
                .filter(e -> map.containsKey(e.getFlagId()))
                // 实体初始化
                .flatMap(e -> Stream.of(InventoryClosedRecordEntity.initList(map.get(e.getFlagId()), e)))
                .distinct()
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(newEntityList)) {
            XxlJobHelper.log("拉取【关账时间】(金蝶->WMS)]：执行结束-无需要金蝶最新库存组织关账时间保存记录");
            return ReturnT.SUCCESS;
        }
        //现只取STK信息
        newEntityList = newEntityList.stream().filter(obj -> StrUtil.equals(obj.getCategory(), InventoryClosedRecordEnum.STK.getCode())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(newEntityList)) {
            XxlJobHelper.log("拉取【STK关账时间】(金蝶->WMS)]：执行结束-无需要金蝶最新库存组织关账时间保存记录");
            return ReturnT.SUCCESS;
        }

        // 查询所有当前组织ID已有的记录
        List<InventoryClosedRecordEntity> oldEntityList = inventoryClosedRecordService.lambdaQuery()
                .list();

        // 批量操作
        inventoryClosedRecordService.actionBatch(newEntityList, oldEntityList);

        XxlJobHelper.log("[拉取【关账时间】(金蝶->WMS)]：执行结束-无需要金蝶最新库存组织关账时间保存记录");
        return ReturnT.SUCCESS;
    }

}
