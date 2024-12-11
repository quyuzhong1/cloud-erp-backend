package com.erp.server.tms.schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import com.common.business.enums.ErpServerModuleEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.entity.CfgSettingEntity;
import com.erp.model.tms.entity.LogisticsBillCostEntity;
import com.erp.model.tms.entity.TmsB2cDeclareReconciliationDetailEntity;
import com.erp.model.tms.enums.CfgSettingEnum;
import com.erp.model.wms.enums.ReconciliationTypeEnum;
import com.erp.server.tms.service.CfgSettingService;
import com.erp.server.tms.service.TmsB2cDeclareReconciliationDetailService;
import com.erp.server.tms.service.TransferDeclareService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zdy
 * @ClassName FirstMileCostAllocationJob
 * @description: 中转费用分摊
 * @date 2024年08月25日
 * @version: 1.0
 */
@Component
@Slf4j
@EnableScheduling
public class TransferDeclareCostAllocationJob {

    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private TransferDeclareService transferDeclareService;
    @Resource
    private TmsB2cDeclareReconciliationDetailService tmsB2cDeclareReconciliationDetailService;
    @Autowired
	@Qualifier("costAllocationPool")
	private ExecutorService costAllocationPool;
    @Resource
    private MQProducerService mqProducerService;
    
    /**
     * 自动生成中转费用分摊
     *
     * @return
     */
    @XxlJob("autoGenerateTransferDeclareCostAllocation")
    public ReturnT<String> autoGenerateTransferDeclareCostAllocation() {
        XxlJobHelper.log("====开始自动生成中转费用分摊====");
        String jobParam = XxlJobHelper.getJobParam();
        XxlJobHelper.log("任务参数={}", JSONUtil.toJsonStr(jobParam));
        
        LocalDateTime currentDateTime = LocalDateTime.now();
        if (StringUtils.isNotBlank(jobParam)) {
            String reportDate = jobParam;
            if(StringUtils.isNotBlank(reportDate)) {
            	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            	currentDateTime = LocalDate.parse(reportDate, formatter).atStartOfDay();
            }
        }

        //查询系统配置
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.RECONCILIATION_CYCLE.getCode());
        if (ObjectUtil.isEmpty(cfgSettingEntity) || ObjectUtil.isEmpty(cfgSettingEntity.getDataJson())) {
            XxlJobHelper.log("无生成系统配置数据");
            return ReturnT.SUCCESS;
        }
        CfgSettingValueDTO.ReconciliationCycleDTO dto = BeanUtil.toBean(cfgSettingEntity.getDataJson(), CfgSettingValueDTO.ReconciliationCycleDTO.class);
        Integer transferAllocationDate = dto.getTransferAllocationDate();
        int dayOfMonth = currentDateTime.getDayOfMonth();
        if(transferAllocationDate != null && dayOfMonth > transferAllocationDate) {
        	LocalDateTime startTime = null;
        	LocalDateTime endTime = null;
        	//自然月生成
            if (ReconciliationTypeEnum.CREAT_BY_MONTH.getCode().equals(dto.getTransferAllocationType())) {
            	startTime = currentDateTime.minus(1, ChronoUnit.MONTHS)
                        .withDayOfMonth(1)
                        .withHour(0)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);
            	endTime = currentDateTime.minus(0, ChronoUnit.MONTHS)
                        .withDayOfMonth(1)
                        .withHour(0)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);
            }else if(ReconciliationTypeEnum.CREAT_BY_PERIOD.getCode().equals(dto.getTransferAllocationType())){
                Integer transferBeginAllocationDate = dto.getTransferBeginAllocationDate();
                if(transferBeginAllocationDate != null) {
                	startTime = currentDateTime.minusMonths(2)
                             .withDayOfMonth(transferBeginAllocationDate)
                             .withHour(0)
                             .withMinute(0)
                             .withSecond(0)
                             .withNano(0);
                	endTime = currentDateTime.minusMonths(1)
                            .withDayOfMonth(transferBeginAllocationDate)
                            .withHour(0)
                            .withMinute(0)
                            .withSecond(0)
                            .withNano(0);
                }else {
                	XxlJobHelper.log("====自动生成中转费用分摊周期生成指定日期不存在====");
                	return ReturnT.SUCCESS;
                }
            }
            
            List<TmsB2cDeclareReconciliationDetailEntity> tmsB2cDeclareReconciliationDetailEntityList = tmsB2cDeclareReconciliationDetailService
            		.lambdaQuery()
            		.ge(TmsB2cDeclareReconciliationDetailEntity::getConfirmDate, startTime.toLocalDate())
                    .lt(TmsB2cDeclareReconciliationDetailEntity::getConfirmDate, endTime.toLocalDate())
                    .last(" and id not in (select declare_reconciliation_detail_id from transfer_declare_cost_allocation_main where is_deleted = 'f') ")
            		.list();
            if(CollUtil.isNotEmpty(tmsB2cDeclareReconciliationDetailEntityList)) {
            	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            	for(TmsB2cDeclareReconciliationDetailEntity l : tmsB2cDeclareReconciliationDetailEntityList) {
            		costAllocationPool.execute(() -> {
            			try {
            				transferDeclareService.singPushAllocation(l.getSourceId() , l.getConfirmDate().format(formatter) , Arrays.asList(l));
    					} catch (Exception e) {
    						WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
    				        warnMsgInfo.setBizName("自动生成中转分摊");
    				        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_TMS);
    				        warnMsgInfo.setTitle("自动生成中转分摊失败，soCode=" + l.getSoCode());
    				        warnMsgInfo.setTableName("tms_b2c_declare_reconciliation_detail");
    				        warnMsgInfo.setTableId(l.getId());
    				        warnMsgInfo.setKeyInfo(e.getMessage());
    				        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
    				        mqProducerService.sendWarnMsg(warnMsgInfo);
    				        log.error("自动生成中转分摊失败，soCode=" + l.getSoCode() , e);
    						throw e;
    					}
            		});
            	}
            }
        }
    
        XxlJobHelper.log("====结束自动生成中转费用分摊====");
        return ReturnT.SUCCESS;
    }
    
    public static void main(String[] args) {
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    	LocalDateTime currentDateTime = LocalDate.parse("2024-09-01", formatter).atStartOfDay();

        System.out.println(currentDateTime);
	}
}
