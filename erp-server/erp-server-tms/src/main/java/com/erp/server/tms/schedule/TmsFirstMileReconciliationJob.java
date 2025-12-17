package com.erp.server.tms.schedule;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.entity.CfgSettingEntity;
import com.erp.model.tms.enums.CfgSettingEnum;
import com.erp.model.wms.enums.ReconciliationTypeEnum;
import com.erp.server.tms.service.CfgSettingService;
import com.erp.server.tms.service.TmsFirstMileReconciliationDetailService;
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
 * 头程对账单相关任务
 */
@Slf4j
@Component
@EnableScheduling
public class TmsFirstMileReconciliationJob {

    @Resource
    private TmsFirstMileReconciliationDetailService tmsFirstMileReconciliationDetailService;

    @Resource
    private CfgSettingService cfgSettingService;


    /**
     * 生成头程对账单任务
     */
    @XxlJob("autoGenFirstMileReconciliation")
    public ReturnT<String> autoGenFirstMileReconciliation() {
        String jobParam = XxlJobHelper.getJobParam();
        XxlJobHelper.log("[生成头程对账单任务] autoGenFirstMileReconciliation 任务开始: 任务参数={}", JSONUtil.toJsonStr(jobParam));
        LocalDate startDate = null;
        LocalDate endDate = null;
        String transportNo = null;

        if (StringUtils.isNotBlank(jobParam)) {
            JSONObject jsonObject = new JSONObject(jobParam);
            String startDate1 = jsonObject.getStr("startDate");
            startDate = CharSequenceUtil.isNotBlank(startDate1) ? LocalDate.parse(startDate1) : null;
            String endDate1 = jsonObject.getStr("endDate");
            endDate = CharSequenceUtil.isNotBlank(endDate1) ? LocalDate.parse(endDate1) : null;
            transportNo = CharSequenceUtil.isNotBlank(jsonObject.getStr("transportNo")) ? jsonObject.getStr("transportNo") : null;
        }

        if (null == startDate && null == endDate) {
            //查询系统配置
            CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.RECONCILIATION_CYCLE.getCode());
            if (ObjectUtil.isEmpty(cfgSettingEntity) || ObjectUtil.isEmpty(cfgSettingEntity.getDataJson())) {
                XxlJobHelper.log("无生成对账单数据");
                return ReturnT.SUCCESS;
            }
            CfgSettingValueDTO.ReconciliationCycleDTO dto = BeanUtil.toBean(cfgSettingEntity.getDataJson(), CfgSettingValueDTO.ReconciliationCycleDTO.class);

            //自然月生成
            if (ReconciliationTypeEnum.CREAT_BY_MONTH.getCode().equals(dto.getFirstMileReconciliationType())) {
                int dayOfMonth = LocalDate.now().getDayOfMonth();
                if (dayOfMonth != MathUtil.ONE) {
                    XxlJobHelper.log("[生成头程对账单任务] autoGenFirstMileReconciliation 任务结束: 自然月生成：非1号不生成");
                    return ReturnT.SUCCESS;
                }
                startDate = LocalDate.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
                endDate = LocalDate.now().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
            } else {
                int dayOfMonth = LocalDate.now().getDayOfMonth();
                if (dayOfMonth != dto.getFirstMileReconciliationDate()) {
                    XxlJobHelper.log("[生成头程对账单任务] autoGenFirstMileReconciliation 任务结束: 按周期生成：非周期号【{}】不生成", dayOfMonth);
                    return ReturnT.SUCCESS;
                }
                endDate = LocalDate.now().minusDays(1);
                startDate = LocalDate.now().minusMonths(1);
            }
        }
        tmsFirstMileReconciliationDetailService.autoGenFirstMileReconciliation(startDate, endDate, transportNo);
        XxlJobHelper.log("[生成头程对账单任务] autoGenFirstMileReconciliation 任务结束: 任务参数={}", JSONUtil.toJsonStr(jobParam));
        return ReturnT.SUCCESS;
    }


}
