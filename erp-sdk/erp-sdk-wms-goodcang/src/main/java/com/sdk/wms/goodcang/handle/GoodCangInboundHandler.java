package com.sdk.wms.goodcang.handle;

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
import com.sdk.wms.goodcang.convert.GoodCangConverter;
import com.sdk.wms.goodcang.dto.response.GoodCangReceiptBatchResp;
import com.sdk.wms.goodcang.dto.response.GoodCangResponse;
import com.sdk.wms.goodcang.service.GoodCangService;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 谷仓拉取入库数据
 **/
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.GOOD_CANG)
@BusinessType(BusinessTypeEnum.INBOUND)
public class GoodCangInboundHandler extends AbstractPullThirdWarehouseHandler<GoodCangReceiptBatchResp, PlatformInboundDTO> {

    @Resource
    private GoodCangService goodCangService;

    @Resource
    private WmsOverseasWarehouseFeign overseasWarehouseFeign;

    @Resource
    private MQProducerService<T> mqProducerService;

    private final String failureMsgHead = "调用谷仓获取入库数据接口异常";

    @Override
    public List<GoodCangReceiptBatchResp> download(JobTaskDTO data) {
        return new ArrayList<>();
    }

    private void checkResponse(GoodCangResponse<?> response) {
        if (!isSuccess(response.getAsk())) {
            log.error(failureMsgHead + response.getMessage());
            WarnMsgInfoDTO msgInfoDTO = this.buildWarnMsgInfoDTO(response.getMessage());
            mqProducerService.sendWarnMsg(msgInfoDTO);
            throw new ServiceException(failureMsgHead + response.getMessage());
        }
    }

    private WarnMsgInfoDTO buildWarnMsgInfoDTO(String msg) {
        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName("调用谷仓获取入库数据接口");
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_THIRD_SDK);
        warnMsgInfo.setTitle(failureMsgHead);
        warnMsgInfo.setTableName(this.getClass().getName());
        warnMsgInfo.setTableId("");
        warnMsgInfo.setKeyInfo(msg);
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        return warnMsgInfo;
    }

    @Override
    public List<PlatformInboundDTO> convert(List<GoodCangReceiptBatchResp> sourceDataList) {
        List<PlatformInboundDTO> list = GoodCangConverter.INSTANCE.inboundConversion(sourceDataList);
        //封装明细数据
        for(PlatformInboundDTO dto : list){
            this.groupBySku(dto);
        }
        return list;
    }


    private void groupBySku(PlatformInboundDTO dto) {
        Map<String, Integer> receivedQuantityMap = dto.getReceivingDataList().stream()
                .collect(Collectors.groupingBy(PlatformInboundDTO.Receiving::getProductSku, Collectors.summingInt(PlatformInboundDTO.Receiving::getReceiveQty)));

        List<PlatformInboundDTO.Item> items = receivedQuantityMap.entrySet().stream()
                .map(entry -> new PlatformInboundDTO.Item(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        dto.setItems(items);
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP_WMS.getDesc();
    }

    private PlatformDictEnum getPlatformDictEnum(){
        return PlatformDictEnum.GOOD_CANG;
    }

    public boolean isSuccess(String ask){
        return "Success".equals(ask);
    }
}
