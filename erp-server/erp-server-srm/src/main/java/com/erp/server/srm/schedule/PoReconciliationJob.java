package com.erp.server.srm.schedule;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.wms.dto.CfgSettingValueDTO;
import com.erp.model.wms.entity.CfgSettingEntity;
import com.erp.model.wms.enums.CfgSettingEnum;
import com.erp.model.wms.enums.ReconciliationTypeEnum;
import com.erp.rpc.wms.feign.CfgSettingFeign;
import com.erp.server.srm.service.PoReconciliationDetailScmService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/**
 * @description: 对账单定时任务
 * @author Will
 * @date: 2024/2/2 14:42
 */
@Component
@Slf4j
@EnableScheduling
public class PoReconciliationJob {

    @Resource
    private CfgSettingFeign cfgSettingFeign;

    @Resource
    private PoReconciliationDetailScmService poReconciliationDetailScmService;

    /**
     * 注册物流单号
     *
     * @return
     */
    @XxlJob("autoGeneratePoReconciliation")
    public ReturnT autoGeneratePoReconciliation() {
        String jobParam = XxlJobHelper.getJobParam();
        LocalDate nowDate = LocalDate.now();
        int dayOfMonth = nowDate.getDayOfMonth();
        //有传参就取传参值
        if (CharSequenceUtil.isNotBlank(jobParam)) {
            nowDate = LocalDate.parse(jobParam);
            dayOfMonth = nowDate.getDayOfMonth();
        }
        XxlJobHelper.log("====开始生成对账单,dayOfMonth = {} =====", dayOfMonth);
        //查询系统配置
        CfgSettingEntity cfgSettingEntity = cfgSettingFeign.getByKey(CfgSettingEnum.PO_RECONCILIATION.getCode());
        if (ObjectUtil.isEmpty(cfgSettingEntity) || ObjectUtil.isEmpty(cfgSettingEntity.getDataJson())) {
            XxlJobHelper.log("无生成对账单数据");
            return ReturnT.SUCCESS;
        }
        CfgSettingValueDTO.PoReconciliationSettingDTO dto = BeanUtil.toBean(cfgSettingEntity.getDataJson(), CfgSettingValueDTO.PoReconciliationSettingDTO.class);
        //自然月生成
        if (ReconciliationTypeEnum.CREAT_BY_MONTH.getCode().equals(dto.getReconciliationType())) {
            if (dayOfMonth != MathUtil.ONE.intValue()) {
                return ReturnT.SUCCESS;
            }
            LocalDate startDate = nowDate.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
            LocalDate endDate = nowDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
            poReconciliationDetailScmService.autoGeneratePoReconciliation(startDate,endDate);
        } else {
            if (dayOfMonth != Integer.valueOf(dto.getEndDate()).intValue() ) {
                return ReturnT.SUCCESS;
            }
            LocalDate endDate = nowDate.minusDays(1);
            LocalDate startDate = nowDate.minusMonths(1);
            poReconciliationDetailScmService.autoGeneratePoReconciliation(startDate,endDate);
        }
        XxlJobHelper.log("====结束生成对账单====");
        return ReturnT.SUCCESS;
    }
}
