package com.erp.server.dmp.push.consumer;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.ApiModuleTypeEnum;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.server.dmp.push.service.business.KingdeeSoReturnConsumerService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.push.service.kingdee.impl.KingdeeCommonServiceImpl;
import com.erp.server.dmp.service.DmpPushTaskService;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeePushModuleEnum;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 对接金蝶销售出库
 *
 * @Author Luo_WG
 * @Date 2023/6/1 14:45
 **/
@Service
@Slf4j
//@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC,
//        selectorExpression = "kingdee_so_return_tag",
//        consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_SO_RETURN,
//        consumeMode = ConsumeMode.ORDERLY)
public class KingdeeSoReturnConsumer<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private KingdeeSoReturnConsumerService kingdeeSoReturnConsumerService;

    @Resource
    private DmpPushTaskService dmpPushTaskService;


    public static void main(String[] args) {
        //模块类型
        Integer type = ApiModuleTypeEnum.SO_RETURN.getCode();
        KingdeeCommonService kingdeeCommonService = new KingdeeCommonServiceImpl();
        Map<String, Object> map = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.SAL_RETURNSTOCK.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
//        queryFilters.add(String.format("FDocumentStatus = '%s'", "C"));
        queryFilters.add(String.format("FBillNo = '%s'", "XSTHD13659667"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FID," +
                "FBillTypeID," +
                "FBillTypeID.FName," +
                "FBillTypeID.FNumber," +
                "FBillNo," +
                "FDate," +
                "FDocumentStatus," +
                "FSaleOrgId," +
                "FSaleOrgId.FName," +
                "FRetcustId.FName," +
                "FRetcustId.FNumber," +
                "FSalesManId," +
                "FSalesManId.FName," +
                "FCreateDate," +
                "FModifyDate," +
                "FCancelStatus," +
                "FReceiverCountry," +
                "FLinkMan," +
                "FExchangeRate," +
                "FApproveDate," +
                "FBussinessType," +
                "FOwnerTypeIdHead," +
                "FSettleCurrId.FCode," +
                "FDelTime," +
                "FHeadNote," +
                "FReturnReason.FDataValue," +
                "FSaledeptid.FNumber," +
                "FSaledeptid.FName," +
                "FOrderNo," +
                "FAmount," +
                "FMustqty," +
                "FUnitID.FName," +
                "FEntity_FEntryId," +
                "FMaterialId," +
                "FMaterialId.FNumber," +
                "FMaterialName," +
                "FAuxpropId," +
                "FMaterialType," +
                "FPrice," +
                "FStockId," +
                "FStockId.FNumber," +
                "FStockId.FName," +
                "FStockLocId.FF100014.FNumber," +
                "FStockstatusId," +
                "FNote," +
                "FSrcBillNo," +
                "FSrcBillTypeID," +
                "FIsFree," +
                "FMaterialModel," +
                "FRealQty," +
                "FSOBILLTYPEID," +
                "FSalUnitQty," +
                "FProjectNo," +
                "F_ulz_KHSKU," +
                "FAllAmount," +
                "FReturnType," +
                "FSOEntryId," +
                "F_ULZ_data_sources,FISGENFORIOS," +
                "FETHIRDBILLNO";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1, 2);
        System.out.println(queryList);
    }


    @Override
    public void updateSyncTaskStatus(DmpSyncMqDTO.ParamDTO paramDTO) {
        dmpPushTaskService.updateStatus(paramDTO);
    }

    @Override
    public void updateMongodbData(String platform, String uniqueId, Integer isClean) {

    }
    @Override
    public void sendWarnMsg(String syncTaskId, String msg) {
        dmpPushTaskService.sendWarnMsg(syncTaskId);
    }

    @Override
    public ApiResult<?> handle(Object ext) {
        Map<String, Object> map = JSONUtil.parseObj(ext);
        kingdeeSoReturnConsumerService.executeConsumer(map);
        return ApiResult.success();
    }


}
