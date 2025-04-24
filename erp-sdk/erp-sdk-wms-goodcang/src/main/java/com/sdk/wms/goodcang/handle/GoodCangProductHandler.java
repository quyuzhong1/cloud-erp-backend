package com.sdk.wms.goodcang.handle;

import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformProductDTO;
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
import com.sdk.wms.goodcang.dto.request.GoodCangGetSkuReq;
import com.sdk.wms.goodcang.dto.response.GoodCangResponse;
import com.sdk.wms.goodcang.dto.response.GoodCangSkuResp;
import com.sdk.wms.goodcang.enums.GoodCangEnums;
import com.sdk.wms.goodcang.service.GoodCangService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 谷仓拉取产品数据
 **/
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.GOOD_CANG)
@BusinessType(BusinessTypeEnum.PRODUCT)
public class GoodCangProductHandler extends AbstractPullThirdWarehouseHandler<GoodCangSkuResp, PlatformProductDTO> {

    @Resource
    private GoodCangService goodCangService;

    @Resource
    private MQProducerService<T> mqProducerService;

    private final String failureMsgHead = "调用谷仓获取产品数据接口异常";

    @Override
    public List<GoodCangSkuResp> download(JobTaskDTO data) {
        //查询数据
        GoodCangGetSkuReq goodCangGetSkuReq = new GoodCangGetSkuReq();
        //最大页码100，从第一页开始查询
        goodCangGetSkuReq.setPageSize(100);

        List<GoodCangSkuResp> respList = new ArrayList<>();
        int page = 1;
        while (true) {
            goodCangGetSkuReq.setPage(page);
            GoodCangResponse<List<GoodCangSkuResp>> goodCangResponse = goodCangService.getSkuList(goodCangGetSkuReq);
            checkResponse(goodCangResponse);
            respList.addAll(goodCangResponse.getData());
            if (goodCangResponse.getCount() <= page * 100) {
                break;
            }
            page++;
        }
        respList = respList.stream().filter(v->v.getProductStatus().equals(GoodCangEnums.OpenApiProductStatusEnum.AVAILABLE.getCode())).collect(Collectors.toList());
        respList.forEach(v->v.setUniqueId(MD5Util.toMD5(getPlatformDictEnum().getCode()+BusinessTypeEnum.PRODUCT.getCode()+v.getProductSku())));
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
        warnMsgInfo.setBizName("调用谷仓获取产品数据接口");
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_THIRD_SDK);
        warnMsgInfo.setTitle(failureMsgHead);
        warnMsgInfo.setTableName(this.getClass().getName());
        warnMsgInfo.setTableId("");
        warnMsgInfo.setKeyInfo(msg);
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        return warnMsgInfo;
    }

    @Override
    public List<PlatformProductDTO> convert(List<GoodCangSkuResp> sourceDataList) {
        return GoodCangConverter.INSTANCE.productConversion(sourceDataList);
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP_OMS.getDesc();
    }

    private PlatformDictEnum getPlatformDictEnum(){
        return PlatformDictEnum.GOOD_CANG;
    }

    public boolean isSuccess(String ask){
        return "Success".equals(ask);
    }
}
