package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.kingdee.KingdeeReturnOrderEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.wms.rocketmq.sync.SyncSoReturnService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.DMP_SYNC_TASK_TOPIC, selectorExpression = "sync_kingdee_return_order_to_wms_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_RETURN_ORDER_TO_WMS)
public class SyncKingdeeReturnOrderCustomer implements RocketMQListener<Object> {

    @Resource
    private SyncSoReturnService syncSoReturnService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Override
    public void onMessage(Object ext) {
        //json字符串
        String jsonStr = JSONUtil.toJsonStr(ext);
        //json数据
        JSONObject jsonObject = JSONUtil.parseObj(jsonStr);
        String dmpSyncTaskId = jsonObject.get("dmpSyncTaskId").toString();
        DmpSyncMqDTO.ParamDTO paramDTO = new DmpSyncMqDTO.ParamDTO();
        paramDTO.setDmpSyncTaskId(dmpSyncTaskId);
        try {
            log.info("监听到金蝶销售退货单要同步：entity>>>>>{}", jsonObject);
            KingdeeReturnOrderEntity entity= JSONUtil.toBean(jsonStr,KingdeeReturnOrderEntity.class);
            syncSoReturnService.syncKingdeeReturnOrderToSoReturn(entity);
        } catch (Exception e){
            log.error("金蝶销售退货单同步失败，msg = {}",e.getMessage());
            //同步失败
            paramDTO.setSyncStatus(SyncStatusEnum.FAILED_SYNC.getCode());
            paramDTO.setResponseMsg(e.getMessage());
            dmpTaskFeign.updateSyncInfo(paramDTO);
            //错误预警
            dmpTaskFeign.sendWarnMsg(dmpSyncTaskId);
            return;
        }
        //同步成功
        paramDTO.setSyncStatus(SyncStatusEnum.SUCCESS_SYNC.getCode());
        paramDTO.setResponseMsg("同步成功");
        dmpTaskFeign.updateSyncInfo(paramDTO);
    }
}
//{"","fCreateDate":"2023-06-13T17:42:22.127","fDate":"2023-07-03T00:00:00","fDelTime":"null","fDocumentStatus":"D","fExchangeRate":1.0,"fHeadNote":" ","fId":"2282480","fLinkMan":" ","fModifyDate":"2023-07-03T14:36:26.697","fOwnerTypeIdHead":"BD_OwnerOrg","fReceiverCountry":" ","fRetcustName":"回归客户","fRetcustNumber":"CUST23061300001","fReturnReason":"005056c0000886ec11e40c093804bbdc","fSaleOrgId":"1","fSaleOrgName":"深圳市唯迹科技有限公司","fSaledeptName":"null","fSaledeptNumber":"null","fSalesManId":"0","fSalesManName":"null","fSettleCurrCode":"CNY","isClean":0,"itemEntityList":[{"fAllAmount":52.5,"fAmount":"52.5","fAuxPropId":"0","fBillNo":"XSTHD22485413","fIsFree":"false","fMaterialId":"201773","fMaterialModel":" R005","fMaterialName":"江卓涛客户定制 1365 R005通用相机手提手柄","fMaterialNumber":"DZ321","fMaterialType":"原材料","fMustQty":"10.0","fNote":" ","fOrderNo":" ","fPrice":"5.25","fProjectNo":" ","fRealQty":"10.0","fReturnType":"4151a33171a04ba6af24524c656b5f79","fSOEntryId":"0","fSalUnitQty":"10.0","fSoBillTypeId":" ","fSrcBillNo":" ","fSrcBillTypeID":" ","fStockId":"113280","fStockLocId":"0","fStockStatusId":"10000","fUnitName":"Pcs","f_ulz_KHSKU":""},{"fAllAmount":31.2,"fAmount":"31.2","fAuxPropId":"0","fBillNo":"XSTHD22485413","fIsFree":"false","fMaterialId":"380189","fMaterialModel":"MA01","fMaterialName":"金蝶","fMaterialNumber":"A118CNR1DZ","fMaterialType":"自制半成品","fMustQty":"10.0","fNote":" ","fOrderNo":" ","fPrice":"3.12","fProjectNo":" ","fRealQty":"10.0","fReturnType":"b9349cf911cd4fdbbf18bec028ee7fe1","fSOEntryId":"0","fSalUnitQty":"10.0","fSoBillTypeId":" ","fSrcBillNo":" ","fSrcBillTypeID":" ","fStockId":"113280","fStockLocId":"0","fStockStatusId":"10000","fUnitName":"Pcs","":""}]}