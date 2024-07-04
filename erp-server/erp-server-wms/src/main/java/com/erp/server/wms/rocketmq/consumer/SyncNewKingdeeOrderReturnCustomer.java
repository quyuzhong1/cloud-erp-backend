package com.erp.server.wms.rocketmq.consumer;

import javax.annotation.Resource;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.model.dmp.kingdee.KingdeeReturnOrderEntity;
import com.erp.rpc.dmp.feign.DmpInoutTaskFeign;
import com.erp.server.wms.rocketmq.sync.SyncSoReturnService;

import cn.hutool.core.exceptions.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;



@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_KINGDEE_ORDER_RETURN_TO_WMS_TOPIC, 
selectorExpression = RocketMqNewTag.DMP_KINGDEE_ORDER_RETURN_TO_WMS_TAG, 
consumerGroup = RocketMqNewConsumerGroup.DMP_KINGDEE_ORDER_RETURN_TO_WMS_GROUP)
public class SyncNewKingdeeOrderReturnCustomer implements RocketMQListener<Object> {

    @Resource
    private SyncSoReturnService syncSoReturnService;

    @Resource
    private DmpInoutTaskFeign dmpInoutTaskFeign;

    @Override
    public void onMessage(Object ext) {
        //json数据
    	JSONObject jsonObject = JSON.parseObject(ext.toString());
        String dmpOutputTaskRecordId = jsonObject.get("dmpOutputTaskRecordId").toString();

        DmpOutputTaskRecordDTO.UpdateDTO updateDTO = new DmpOutputTaskRecordDTO.UpdateDTO();
        updateDTO.setId(dmpOutputTaskRecordId);
        updateDTO.setStatus(DmpOutputTaskRecordStatusEnum.FINISH.getCode());
        log.info("监听到金蝶退货单需要同步：entity={}", jsonObject);
        KingdeeReturnOrderEntity entity = JSON.parseObject(ext.toString(),  KingdeeReturnOrderEntity.class);
        try {
        	syncSoReturnService.syncKingdeeReturnOrderToSoReturn(entity);
        } catch (Throwable e) {
            log.error("金蝶直接退货单同步失败，msg = {}",e.getMessage());
            updateDTO.setStatus(DmpOutputTaskRecordStatusEnum.COSUMERERROR.getCode());
            updateDTO.setResponseData("消费数据失败：" + ExceptionUtil.stacktraceToOneLineString(e));
        }
        dmpInoutTaskFeign.updateOutputTaskRecord(updateDTO);
    }
}
//{"","fCreateDate":"2023-06-13T17:42:22.127","fDate":"2023-07-03T00:00:00","fDelTime":"null","fDocumentStatus":"D","fExchangeRate":1.0,"fHeadNote":" ","fId":"2282480","fLinkMan":" ","fModifyDate":"2023-07-03T14:36:26.697","fOwnerTypeIdHead":"BD_OwnerOrg","fReceiverCountry":" ","fRetcustName":"回归客户","fRetcustNumber":"CUST23061300001","fReturnReason":"005056c0000886ec11e40c093804bbdc","fSaleOrgId":"1","fSaleOrgName":"深圳市唯迹科技有限公司","fSaledeptName":"null","fSaledeptNumber":"null","fSalesManId":"0","fSalesManName":"null","fSettleCurrCode":"CNY","isClean":0,"itemEntityList":[{"fAllAmount":52.5,"fAmount":"52.5","fAuxPropId":"0","fBillNo":"XSTHD22485413","fIsFree":"false","fMaterialId":"201773","fMaterialModel":" R005","fMaterialName":"江卓涛客户定制 1365 R005通用相机手提手柄","fMaterialNumber":"DZ321","fMaterialType":"原材料","fMustQty":"10.0","fNote":" ","fOrderNo":" ","fPrice":"5.25","fProjectNo":" ","fRealQty":"10.0","fReturnType":"4151a33171a04ba6af24524c656b5f79","fSOEntryId":"0","fSalUnitQty":"10.0","fSoBillTypeId":" ","fSrcBillNo":" ","fSrcBillTypeID":" ","fStockId":"113280","fStockLocId":"0","fStockStatusId":"10000","fUnitName":"Pcs","f_ulz_KHSKU":""},{"fAllAmount":31.2,"fAmount":"31.2","fAuxPropId":"0","fBillNo":"XSTHD22485413","fIsFree":"false","fMaterialId":"380189","fMaterialModel":"MA01","fMaterialName":"金蝶","fMaterialNumber":"A118CNR1DZ","fMaterialType":"自制半成品","fMustQty":"10.0","fNote":" ","fOrderNo":" ","fPrice":"3.12","fProjectNo":" ","fRealQty":"10.0","fReturnType":"b9349cf911cd4fdbbf18bec028ee7fe1","fSOEntryId":"0","fSalUnitQty":"10.0","fSoBillTypeId":" ","fSrcBillNo":" ","fSrcBillTypeID":" ","fStockId":"113280","fStockLocId":"0","fStockStatusId":"10000","fUnitName":"Pcs","":""}]}