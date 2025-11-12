package com.sdk.wms.tongyou.handle;

import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformOutboundDTO;
import com.common.business.enums.*;
import com.common.business.handler.AbstractPullThirdWarehouseHandler;
import com.common.core.exception.ServiceException;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.sdk.wms.tongyou.convert.TongYouConverter;
import com.sdk.wms.tongyou.dto.request.TongYouGetOutboundReq;
import com.sdk.wms.tongyou.dto.response.TongYouOutboundResp;
import com.sdk.wms.tongyou.dto.response.TongYouResponse;
import com.sdk.wms.tongyou.enums.TongYouEnums;
import com.sdk.wms.tongyou.service.TongYouService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通邮出库处理服务实现类
 * @author will
 * @date 2025/11/11 15:21
 */
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.TONG_YOU)
@BusinessType(BusinessTypeEnum.OUTBOUND)
public class TongYouOutboundHandler extends AbstractPullThirdWarehouseHandler<TongYouOutboundResp, PlatformOutboundDTO> {

    @Resource
    private TongYouService tongYouService;

    @Resource
    private MQProducerService<T> mqProducerService;

    private final String failureMsgHead = "调用通邮获取出库数据接口异常";

    @Override
    public List<TongYouOutboundResp> download(JobTaskDTO data) {
        LocalDateTime lastTime = data.getLastTime();
        LocalDateTime nextTime = data.getNextTime();
        if (lastTime.isEqual(nextTime)){
            //nextTime +30分钟
            nextTime = lastTime.plusMinutes(30);
        }
        //查询数据
        TongYouGetOutboundReq imlGetOutboundReq = TongYouGetOutboundReq.builder()
                .modifyDateFrom(lastTime)
                .modifyDateTo(nextTime)
                .pageSize(100)
                .orderStatus(TongYouEnums.OrderStatusEnum.INITIAL_RECEIVING.getCode())
                .build();

        List<TongYouOutboundResp> respList = new ArrayList<>();
        int page = 1;
        while (true) {
            imlGetOutboundReq.setPage(page);
            TongYouResponse<List<TongYouOutboundResp>> response = tongYouService.getOutboundBatch(imlGetOutboundReq);
            checkResponse(response);
            respList.addAll(response.getData());
            if (response.getCount() <= page * 100) {
                break;
            }
            page++;
        }
        //过滤掉代发货状态
        respList = respList.stream().filter(v->!(v.getOrderStatus().equals(TongYouEnums.OrderStatusEnum.NEW.getCode()) || v.getOrderStatus().equals(TongYouEnums.OrderStatusEnum.FIRST_JOURNEY_ON_THE_WAY.getCode()))).collect(Collectors.toList());
        respList.forEach(v->{
            v.setUniqueId(v.getOrderCode());
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
        warnMsgInfo.setBizName("调用通邮获取出库数据接口");
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_THIRD_SDK);
        warnMsgInfo.setTitle(failureMsgHead);
        warnMsgInfo.setTableName(this.getClass().getName());
        warnMsgInfo.setTableId("");
        warnMsgInfo.setKeyInfo(msg);
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        return warnMsgInfo;
    }

    @Override
    public List<PlatformOutboundDTO> convert(List<TongYouOutboundResp> sourceDataList) {
        List<PlatformOutboundDTO> resultList = TongYouConverter.INSTANCE.outboundConversion(sourceDataList);
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
