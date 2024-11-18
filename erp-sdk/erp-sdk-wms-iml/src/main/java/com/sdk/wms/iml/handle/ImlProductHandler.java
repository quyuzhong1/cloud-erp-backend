package com.sdk.wms.iml.handle;

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
import com.sdk.wms.iml.convert.ImlConverter;
import com.sdk.wms.iml.dto.request.ImlGetProductReq;
import com.sdk.wms.iml.dto.response.ImlProductResp;
import com.sdk.wms.iml.dto.response.ImlResponse;
import com.sdk.wms.iml.enums.ImlEnums;
import com.sdk.wms.iml.service.ImlService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 艾姆勒拉取产品数据
 **/
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.IML)
@BusinessType(BusinessTypeEnum.PRODUCT)
public class ImlProductHandler extends AbstractPullThirdWarehouseHandler<ImlProductResp, PlatformProductDTO> {

    @Resource
    private ImlService imlService;

    @Resource
    private MQProducerService<T> mqProducerService;

    private final String failureMsgHead = "调用艾姆勒获取产品数据接口异常";

    @Override
    public List<ImlProductResp> download(JobTaskDTO data) {
        //查询数据
        ImlGetProductReq imlGetProductReq = new ImlGetProductReq();
        //最大页码100，从第一页开始查询
        imlGetProductReq.setPageSize(100);
        List<ImlProductResp> respList = new ArrayList<>();
        int page = 1;
        while (true) {
            imlGetProductReq.setPage(page);
            ImlResponse<List<ImlProductResp>> goodCangResponse = imlService.getSkuList(imlGetProductReq);
            checkResponse(goodCangResponse);
            respList.addAll(goodCangResponse.getData());
            if (goodCangResponse.getCount() <= page * 100) {
                break;
            }
            page++;
        }
        respList = respList.stream().filter(v->v.getProductStatus().equals(ImlEnums.ProductStatusEnum.AVAILABLE.getCode())).collect(Collectors.toList());
        respList.forEach(v->v.setUniqueId(MD5Util.toMD5(getPlatformDictEnum().getCode()+BusinessTypeEnum.PRODUCT.getCode()+v.getProductSku())));
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
        warnMsgInfo.setBizName("调用艾姆勒获取产品数据接口");
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_THIRD_SDK);
        warnMsgInfo.setTitle(failureMsgHead);
        warnMsgInfo.setTableName(this.getClass().getName());
        warnMsgInfo.setTableId("");
        warnMsgInfo.setKeyInfo(msg);
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        return warnMsgInfo;
    }

    @Override
    public List<PlatformProductDTO> convert(List<ImlProductResp> sourceDataList) {
        return ImlConverter.INSTANCE.productConversion(sourceDataList);
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP_OMS.getDesc();
    }
    private PlatformDictEnum getPlatformDictEnum(){
        return PlatformDictEnum.IML;
    }
    public boolean isSuccess(String ask){
        return "Success".equals(ask);
    }
}
