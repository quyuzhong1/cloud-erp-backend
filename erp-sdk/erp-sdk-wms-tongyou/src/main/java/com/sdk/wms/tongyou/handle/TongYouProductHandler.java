package com.sdk.wms.tongyou.handle;

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
import com.sdk.wms.tongyou.convert.TongYouConverter;
import com.sdk.wms.tongyou.dto.request.TongYouGetProductReq;
import com.sdk.wms.tongyou.dto.response.TongYouProductResp;
import com.sdk.wms.tongyou.dto.response.TongYouResponse;
import com.sdk.wms.tongyou.enums.TongYouEnums;
import com.sdk.wms.tongyou.service.TongYouService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通邮拉取产品处理服务实现类
 * @author will
 * @date 2025/11/11 15:21
 */
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.TONG_YOU)
@BusinessType(BusinessTypeEnum.PRODUCT)
public class TongYouProductHandler extends AbstractPullThirdWarehouseHandler<TongYouProductResp, PlatformProductDTO> {

    @Resource
    private TongYouService tongYouService;

    @Resource
    private MQProducerService<T> mqProducerService;

    private final String failureMsgHead = "调用通邮获取产品数据接口异常";

    @Override
    public List<TongYouProductResp> download(JobTaskDTO data) {
        //查询数据
        TongYouGetProductReq imlGetProductReq = new TongYouGetProductReq();
        //最大页码100，从第一页开始查询
        imlGetProductReq.setPageSize(100);
        List<TongYouProductResp> respList = new ArrayList<>();
        int page = 1;
        while (true) {
            imlGetProductReq.setPage(page);
            TongYouResponse<List<TongYouProductResp>> goodCangResponse = tongYouService.getSkuList(imlGetProductReq);
            checkResponse(goodCangResponse);
            respList.addAll(goodCangResponse.getData());
            if (goodCangResponse.getCount() <= page * 100) {
                break;
            }
            page++;
        }
        respList = respList.stream().filter(v->v.getProductStatus().equals(TongYouEnums.ProductStatusEnum.AVAILABLE.getCode())).collect(Collectors.toList());
        respList.forEach(v->v.setUniqueId(MD5Util.toMD5(getPlatformDictEnum().getCode()+BusinessTypeEnum.PRODUCT.getCode()+v.getProductSku())));
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
        warnMsgInfo.setBizName("调用通邮获取产品数据接口");
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_THIRD_SDK);
        warnMsgInfo.setTitle(failureMsgHead);
        warnMsgInfo.setTableName(this.getClass().getName());
        warnMsgInfo.setTableId("");
        warnMsgInfo.setKeyInfo(msg);
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        return warnMsgInfo;
    }

    @Override
    public List<PlatformProductDTO> convert(List<TongYouProductResp> sourceDataList) {
        return TongYouConverter.INSTANCE.productConversion(sourceDataList);
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP_OMS.getDesc();
    }
    private PlatformDictEnum getPlatformDictEnum(){
        return PlatformDictEnum.TONG_YOU;
    }
    public boolean isSuccess(String ask){
        return "Success".equals(ask);
    }
}
