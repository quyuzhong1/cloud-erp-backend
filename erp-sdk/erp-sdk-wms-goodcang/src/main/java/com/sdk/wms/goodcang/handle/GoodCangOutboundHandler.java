package com.sdk.wms.goodcang.handle;

import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformOutboundDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.AbstractPullThirdWarehouseHandler;
import com.common.business.utils.MD5Util;
import com.common.core.exception.ServiceException;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.sdk.wms.goodcang.convert.GoodCangConverter;
import com.sdk.wms.goodcang.dto.request.GoodCangGetOutBoundReq;
import com.sdk.wms.goodcang.dto.response.GoodCangOutboundResp;
import com.sdk.wms.goodcang.dto.response.GoodCangResponse;
import com.sdk.wms.goodcang.enums.GoodCangEnums;
import com.sdk.wms.goodcang.service.GoodCangService;
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
 * 谷仓拉取出库数据
 **/
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.GOOD_CANG)
@BusinessType(BusinessTypeEnum.OUTBOUND)
public class GoodCangOutboundHandler extends AbstractPullThirdWarehouseHandler<GoodCangOutboundResp, PlatformOutboundDTO> {

    @Resource
    private GoodCangService goodCangService;

    @Resource
    private MQProducerService<T> mqProducerService;

    private final String failureMsgHead = "调用谷仓获取出库数据接口异常";

    @Override
    public List<GoodCangOutboundResp> download(JobTaskDTO data) {
        LocalDateTime lastTime = data.getLastTime();
        LocalDateTime nextTime = data.getNextTime();
        if (lastTime.isEqual(nextTime)){
            //nextTime +30分钟
            nextTime = lastTime.plusMinutes(30);
        }

        //查询数据
        GoodCangGetOutBoundReq goodCangGetOutBoundReq = new GoodCangGetOutBoundReq();
        goodCangGetOutBoundReq.setModifyDateFrom(lastTime);
        goodCangGetOutBoundReq.setModifyDateTo(nextTime);
        goodCangGetOutBoundReq.setPageSize(20);

        List<GoodCangOutboundResp> respList = new ArrayList<>();
        int page = 1;
        while (true) {
            goodCangGetOutBoundReq.setPage(page);
            GoodCangResponse<List<GoodCangOutboundResp>> goodCangResponse = goodCangService.getOutboundBatch(goodCangGetOutBoundReq);
            checkResponse(goodCangResponse);
            respList.addAll(goodCangResponse.getData());
            if (goodCangResponse.getCount() <= page * 20) {
                break;
            }
            page++;
        }
        //过滤待发货状态单据
        respList = respList.stream().filter(v->!v.getOrderStatus().equals(GoodCangEnums.OrderStatusEnum.TO_BE_SHIPPED.getCode())).collect(Collectors.toList());
        respList.forEach(v->v.setUniqueId(v.getOrderCode()));
        return respList;
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
        warnMsgInfo.setBizName("调用谷仓获取出库数据接口");
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_THIRD_SDK);
        warnMsgInfo.setTitle(failureMsgHead);
        warnMsgInfo.setTableName(this.getClass().getName());
        warnMsgInfo.setTableId("");
        warnMsgInfo.setKeyInfo(msg);
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        return warnMsgInfo;
    }

    @Override
    public List<PlatformOutboundDTO> convert(List<GoodCangOutboundResp> sourceDataList) {
        List<PlatformOutboundDTO> resultList = GoodCangConverter.INSTANCE.outboundConversion(sourceDataList);
        resultList = resultList.stream().filter(v-> StringUtils.isNotBlank(v.getOrderStatus())).collect(Collectors.toList());
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
