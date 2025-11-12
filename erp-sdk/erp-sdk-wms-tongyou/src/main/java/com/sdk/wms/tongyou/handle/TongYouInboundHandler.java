package com.sdk.wms.tongyou.handle;

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
import com.sdk.wms.tongyou.convert.TongYouConverter;
import com.sdk.wms.tongyou.dto.request.TongYouGetReceiptReq;
import com.sdk.wms.tongyou.dto.response.TongYouReceiptResp;
import com.sdk.wms.tongyou.dto.response.TongYouResponse;
import com.sdk.wms.tongyou.service.TongYouService;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 通邮入库处理服务实现类
 * @author will
 * @date 2025/11/11 15:21
 */
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.TONG_YOU)
@BusinessType(BusinessTypeEnum.INBOUND)
public class TongYouInboundHandler extends AbstractPullThirdWarehouseHandler<TongYouReceiptResp, PlatformInboundDTO> {

    @Resource
    private TongYouService tongYouService;

    @Resource
    private MQProducerService<T> mqProducerService;

    @Resource
    private WmsOverseasWarehouseFeign overseasWarehouseFeign;

    private final String failureMsgHead = "调用通邮获取入库数据接口异常";

    @Override
    public List<TongYouReceiptResp> download(JobTaskDTO data) {
        //查询待签收、部分签收状态的入库单
        List<String> receiveCodeList = overseasWarehouseFeign.getReceiptNumbersForStatus(Arrays.asList(OverseasInstockStatusEnum.TO_BE_SIGNED.getCode()
                ,OverseasInstockStatusEnum.PARTIAL_SIGNED.getCode()
                ,OverseasInstockStatusEnum.MANUAL_COMPLETION.getCode()), OmsPlatformEnum.TONG_YOU.getCode());
        if(CollectionUtils.isEmpty(receiveCodeList)){
            return new ArrayList<>();
        }
        //查询数据
        TongYouGetReceiptReq imlGetReceiptReq = TongYouGetReceiptReq.builder()
                .page(1)
                .pageSize(receiveCodeList.size())
                .receivingCodeArr(receiveCodeList)
                .build();
        TongYouResponse<List<TongYouReceiptResp>> response = tongYouService.getReceiptBatch(imlGetReceiptReq);
        checkResponse(response);
        List<TongYouReceiptResp> respList =response.getData();
        respList.forEach(v->{
            v.setUniqueId(MD5Util.toMD5(getPlatformDictEnum().getCode()+BusinessTypeEnum.INBOUND.getCode()+v.getReceivingCode()));
            v.setAuthId(data.getShopId());
        });

        return respList;
    }

    private void checkResponse(TongYouResponse<?> response) {
        if (!isSuccess(response.getAsk())) {
            log.error(failureMsgHead + response.getMessage());
            WarnMsgInfoDTO msgInfoDTO = this.buildWarnMsgInfoDTO(response.getMessage());
            mqProducerService.sendWarnMsg(msgInfoDTO);
            throw new ServiceException(failureMsgHead + response.getMessage());
        }
    }

    private WarnMsgInfoDTO buildWarnMsgInfoDTO(String msg) {
        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName("调用通邮获取入库数据接口");
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_THIRD_SDK);
        warnMsgInfo.setTitle(failureMsgHead);
        warnMsgInfo.setTableName(this.getClass().getName());
        warnMsgInfo.setTableId("");
        warnMsgInfo.setKeyInfo(msg);
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        return warnMsgInfo;
    }

    @Override
    public List<PlatformInboundDTO> convert(List<TongYouReceiptResp> sourceDataList) {
        return TongYouConverter.INSTANCE.inboundConversion(sourceDataList);
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP_WMS.getDesc();
    }
    private PlatformDictEnum getPlatformDictEnum(){
        return PlatformDictEnum.TONG_YOU;
    }
    public boolean isSuccess(String ask){
        return "Success".equals(ask);
    }
}
