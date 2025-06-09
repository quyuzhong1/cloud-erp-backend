package com.sdk.wms.iml.handle;

import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformInboundDTO;
import com.common.business.enums.*;
import com.common.business.handler.AbstractPullThirdWarehouseHandler;
import com.common.business.utils.MD5Util;
import com.common.core.exception.ServiceException;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
import com.sdk.wms.iml.convert.ImlConverter;
import com.sdk.wms.iml.dto.request.ImlGetReceiptReq;
import com.sdk.wms.iml.dto.response.ImlReceiptResp;
import com.sdk.wms.iml.dto.response.ImlResponse;
import com.sdk.wms.iml.service.ImlService;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 艾姆勒拉取入库单数据
 **/
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.IML)
@BusinessType(BusinessTypeEnum.INBOUND)
public class ImlInboundHandler extends AbstractPullThirdWarehouseHandler<ImlReceiptResp, PlatformInboundDTO> {

    @Resource
    private ImlService imlService;

    @Resource
    private MQProducerService<T> mqProducerService;

    @Resource
    private WmsOverseasWarehouseFeign overseasWarehouseFeign;

    private final String failureMsgHead = "调用艾姆勒获取入库数据接口异常";

    @Override
    public List<ImlReceiptResp> download(JobTaskDTO data) {
        //查询待签收、部分签收状态的入库单
        List<String> receiveCodeList = overseasWarehouseFeign.getReceiptNumbersForStatus(Arrays.asList(OverseasInstockStatusEnum.TO_BE_SIGNED.getCode()
                ,OverseasInstockStatusEnum.PARTIAL_SIGNED.getCode()
                ,OverseasInstockStatusEnum.MANUAL_COMPLETION.getCode()), OmsPlatformEnum.OMS_IML.getCode());
        if(CollectionUtils.isEmpty(receiveCodeList)){
            return new ArrayList<>();
        }
        //查询数据
        ImlGetReceiptReq imlGetReceiptReq = ImlGetReceiptReq.builder()
                .page(1)
                .pageSize(receiveCodeList.size())
                .receivingCodeArr(receiveCodeList)
                .build();
        ImlResponse<List<ImlReceiptResp>> response = imlService.getReceiptBatch(imlGetReceiptReq);
        checkResponse(response);
        List<ImlReceiptResp> respList =response.getData();
        respList.forEach(v->{
            v.setUniqueId(MD5Util.toMD5(getPlatformDictEnum().getCode()+BusinessTypeEnum.INBOUND.getCode()+v.getReceivingCode()));
            v.setAuthId(data.getShopId());
        });

        return respList;
    }

    private void checkResponse(ImlResponse<?> response) {
        if (!isSuccess(response.getAsk())) {
            log.error(failureMsgHead + response.getMessage());
            WarnMsgInfoDTO msgInfoDTO = this.buildWarnMsgInfoDTO(response.getMessage());
            mqProducerService.sendWarnMsg(msgInfoDTO);
            throw new ServiceException(failureMsgHead + response.getMessage());
        }
    }

    private WarnMsgInfoDTO buildWarnMsgInfoDTO(String msg) {
        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName("调用艾姆勒获取入库数据接口");
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_THIRD_SDK);
        warnMsgInfo.setTitle(failureMsgHead);
        warnMsgInfo.setTableName(this.getClass().getName());
        warnMsgInfo.setTableId("");
        warnMsgInfo.setKeyInfo(msg);
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        return warnMsgInfo;
    }

    @Override
    public List<PlatformInboundDTO> convert(List<ImlReceiptResp> sourceDataList) {
        return ImlConverter.INSTANCE.inboundConversion(sourceDataList);
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP_WMS.getDesc();
    }
    private PlatformDictEnum getPlatformDictEnum(){
        return PlatformDictEnum.IML;
    }
    public boolean isSuccess(String ask){
        return "Success".equals(ask);
    }
}
