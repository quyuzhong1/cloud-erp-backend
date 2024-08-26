package com.erp.server.tms.schedule;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.entity.CfgSettingEntity;
import com.erp.model.tms.enums.CfgSettingEnum;
import com.erp.model.wms.enums.ReconciliationTypeEnum;
import com.erp.server.tms.service.CfgSettingService;
import com.erp.server.tms.service.FirstMileCostAllocationService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/**
 * @author zdy
 * @ClassName FirstMileCostAllocationJob
 * @description: 头程费用分摊
 * @date 2024年08月25日
 * @version: 1.0
 */
@Component
@Slf4j
@EnableScheduling
public class FirstMileCostAllocationJob {

    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private FirstMileCostAllocationService firstMileCostAllocationService;

    /**
     * 自动生成头程费用分摊
     *
     * @return
     */
    @XxlJob("autoGenerateFirstMileCostAllocation")
    public ReturnT<String> autoGenerateFirstMileCostAllocation() {
        XxlJobHelper.log("====开始自动生成头程费用分摊====");
        String jobParam = XxlJobHelper.getJobParam();
        XxlJobHelper.log("任务参数={}", JSONUtil.toJsonStr(jobParam));
        LocalDate reportPeriodMonth = null;
        if (StringUtils.isNotBlank(jobParam)) {
            JSONObject jsonObject = new JSONObject(jobParam);
            reportPeriodMonth = LocalDate.parse(jsonObject.getStr("reportPeriodMonth"));
        }
        if (null == reportPeriodMonth) {
            //查询系统配置
            CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.RECONCILIATION_CYCLE.getCode());
            if (ObjectUtil.isEmpty(cfgSettingEntity) || ObjectUtil.isEmpty(cfgSettingEntity.getDataJson())) {
                XxlJobHelper.log("无生成系统配置数据");
                return ReturnT.SUCCESS;
            }
            CfgSettingValueDTO.ReconciliationCycleDTO dto = BeanUtil.toBean(cfgSettingEntity.getDataJson(), CfgSettingValueDTO.ReconciliationCycleDTO.class);
            //自然月生成
            if (ReconciliationTypeEnum.CREAT_BY_MONTH.getCode().equals(dto.getFirstMileAllocationType())) {
                int dayOfMonth = LocalDate.now().getDayOfMonth();
                if (dayOfMonth != MathUtil.ONE) {
                    XxlJobHelper.log("[生成头程费用分摊] autoGenFirstMileReconciliation 任务结束: 自然月生成：非1号不生成");
                    return ReturnT.SUCCESS;
                }
                reportPeriodMonth = LocalDate.now().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
            } else {
                int dayOfMonth = LocalDate.now().getDayOfMonth();
                if (dayOfMonth != dto.getFirstMileAllocationDate()) {
                    XxlJobHelper.log("[生成头程费用分摊] autoGenFirstMileReconciliation 任务结束: 按周期生成：非周期号【{}】不生成", dayOfMonth);
                    return ReturnT.SUCCESS;
                }
                reportPeriodMonth = LocalDate.now().withDayOfMonth(1);
            }
        }
        firstMileCostAllocationService.autoGenerateFirstMileCostAllocation(reportPeriodMonth);
        XxlJobHelper.log("====结束自动生成头程费用分摊====");
        return ReturnT.SUCCESS;
    }
}
