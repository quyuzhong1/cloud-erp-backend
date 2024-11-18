package com.sdk.wms.iml.handle;

import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformOutboundDTO;
import com.common.business.enums.*;
import com.common.business.handler.AbstractPullThirdWarehouseHandler;
import com.common.business.utils.MD5Util;
import com.common.core.exception.ServiceException;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.sdk.wms.iml.convert.ImlConverter;
import com.sdk.wms.iml.dto.request.ImlGetOutboundReq;
import com.sdk.wms.iml.dto.response.ImlOutboundResp;
import com.sdk.wms.iml.dto.response.ImlResponse;
import com.sdk.wms.iml.enums.ImlEnums;
import com.sdk.wms.iml.service.ImlService;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 艾姆勒拉取入库单数据
 **/
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.IML)
@BusinessType(BusinessTypeEnum.OUTBOUND)
public class ImlOutboundHandler extends AbstractPullThirdWarehouseHandler<ImlOutboundResp, PlatformOutboundDTO> {

    @Resource
    private ImlService imlService;

    @Resource
    private MQProducerService<T> mqProducerService;

    private final String failureMsgHead = "调用艾姆勒获取出库数据接口异常";

    @Override
    public List<ImlOutboundResp> download(JobTaskDTO data) {
        LocalDateTime lastTime = data.getLastTime();
        LocalDateTime nextTime = data.getNextTime();
        if (lastTime.isEqual(nextTime)){
            //nextTime +30分钟
            nextTime = lastTime.plusMinutes(30);
        }
        //查询数据
        ImlGetOutboundReq imlGetOutboundReq = ImlGetOutboundReq.builder()
                .modifyDateFrom(lastTime)
                .modifyDateTo(nextTime)
                .pageSize(100)
                .orderStatus(ImlEnums.OrderStatusEnum.INITIAL_RECEIVING.getCode())
                .build();

        List<ImlOutboundResp> respList = new ArrayList<>();
        int page = 1;
        while (true) {
            imlGetOutboundReq.setPage(page);
            ImlResponse<List<ImlOutboundResp>> response = imlService.getOutboundBatch(imlGetOutboundReq);
            checkResponse(response);
            respList.addAll(response.getData());
            if (response.getCount() <= page * 100) {
                break;
            }
            page++;
        }
        //过滤掉代发货状态
        respList = respList.stream().filter(v->!(v.getOrderStatus().equals(ImlEnums.OrderStatusEnum.NEW.getCode()) || v.getOrderStatus().equals(ImlEnums.OrderStatusEnum.FIRST_JOURNEY_ON_THE_WAY.getCode()))).collect(Collectors.toList());
        respList.forEach(v->{
            v.setUniqueId(v.getOrderCode());
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
        warnMsgInfo.setBizName("调用艾姆勒获取出库数据接口");
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_THIRD_SDK);
        warnMsgInfo.setTitle(failureMsgHead);
        warnMsgInfo.setTableName(this.getClass().getName());
        warnMsgInfo.setTableId("");
        warnMsgInfo.setKeyInfo(msg);
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        return warnMsgInfo;
    }

    @Override
    public List<PlatformOutboundDTO> convert(List<ImlOutboundResp> sourceDataList) {
        List<PlatformOutboundDTO> resultList = ImlConverter.INSTANCE.outboundConversion(sourceDataList);
        return resultList;
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP_WMS.getDesc();
    }
    public boolean isSuccess(String ask){
        return "Success".equals(ask);
    }
}
