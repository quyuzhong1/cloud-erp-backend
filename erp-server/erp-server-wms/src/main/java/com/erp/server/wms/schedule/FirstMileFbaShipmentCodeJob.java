package com.erp.server.wms.schedule;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.model.wms.entity.RequisitionApplicationEntity;
import com.erp.server.wms.service.FirstMileDeliveryDetailService;
import com.erp.server.wms.service.FirstMileDeliveryService;
import com.erp.server.wms.service.RequisitionApplicationService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 
 * @date 2024-08-09
 * @author tanmujin
 */
@Slf4j
@Component
public class FirstMileFbaShipmentCodeJob {

    @Resource
    private RequisitionApplicationService  requisitionApplicationService;
    @Resource
    private FirstMileDeliveryService firstMileDeliveryService;
    @Resource
    private FirstMileDeliveryDetailService firstMileDeliveryDetailService;

    @XxlJob("bindFbaShipmentCode")
    public ReturnT<String> bindFbaShipmentCode(){
        List<RequisitionApplicationEntity> entityList = requisitionApplicationService.lambdaQuery()
                .lt(RequisitionApplicationEntity::getCreateTime, "2024-08-09 00:00:00")
                .isNotNull(RequisitionApplicationEntity::getFbaShipmentCode)
                .ne(RequisitionApplicationEntity::getFbaShipmentCode, "")
                .list();
        for (RequisitionApplicationEntity entity : entityList) {
            FirstMileDeliveryEntity firstMileDelivery = firstMileDeliveryService.getOne(new LambdaQueryWrapper<FirstMileDeliveryEntity>()
                    .eq(FirstMileDeliveryEntity::getSourceCode, entity.getCode())
                    .eq(FirstMileDeliveryEntity::getSourceType, SourceTypeEnum.REQUISITION_APPLICATION.getCode()));
            if(firstMileDelivery == null){
                XxlJobHelper.log("未找到要货申请单对应的发货单，要货申请单号：{}", entity.getCode());
                continue;
            }
            firstMileDeliveryDetailService.lambdaUpdate()
                    .set(FirstMileDeliveryDetailEntity::getFbaShipmentCode, entity.getFbaShipmentCode())
                    .eq(FirstMileDeliveryDetailEntity::getMainId, firstMileDelivery.getId())
                    .nested(qw -> qw.isNull(FirstMileDeliveryDetailEntity::getFbaShipmentCode).or().eq(FirstMileDeliveryDetailEntity::getFbaShipmentCode, ""))
                    .update();
            XxlJobHelper.log("绑定FBA货件编码，要货申请单号：{}，发货单号：{}，货件编码：{}", entity.getCode(), firstMileDelivery.getCode(), entity.getFbaShipmentCode());
        }

        return ReturnT.SUCCESS;
    }
}
