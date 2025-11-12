package com.sdk.wms.tongyou.handle;

import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformWarehouseDTO;
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
import com.sdk.wms.tongyou.dto.request.TongYouBaseRequest;
import com.sdk.wms.tongyou.dto.response.TongYouWarehouseResp;
import com.sdk.wms.tongyou.dto.response.TongYouResponse;
import com.sdk.wms.tongyou.service.TongYouService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 通邮拉取仓库处理服务实现类
 * @author will
 * @date 2025/11/11 15:21
 */
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.TONG_YOU)
@BusinessType(BusinessTypeEnum.WAREHOUSE)
public class TongYouWarehouseHandler extends AbstractPullThirdWarehouseHandler<TongYouWarehouseResp, PlatformWarehouseDTO> {

    @Resource
    private TongYouService tongYouService;

    @Resource
    private MQProducerService<T> mqProducerService;

    private final String failureMsgHead = "调用通邮获取仓库数据接口异常";

    @Override
    public List<TongYouWarehouseResp> download(JobTaskDTO data) {
        TongYouResponse<List<TongYouWarehouseResp>> response = tongYouService.getWarehouse(TongYouBaseRequest.builder().build());
        checkResponse(response);
        List<TongYouWarehouseResp> imlWarehouseData = response.getData();
        imlWarehouseData.forEach(v->{
            v.setUniqueId(MD5Util.toMD5(getPlatformDictEnum().getCode()+BusinessTypeEnum.WAREHOUSE.getCode()+v.getWarehouseCode()));
            v.setAuthId(data.getShopId());
        });
        return imlWarehouseData;
    }

    private void checkResponse(TongYouResponse<?> response) {
        if (!isSuccess(response.getAsk())) {
            log.error(failureMsgHead + response.getMessage());
            WarnMsgInfoDTO msgInfoDTO = this.buildWarnMsgInfoDTO(response.getMessage());
            mqProducerService.sendWarnMsg(msgInfoDTO);
            throw new ServiceException(failureMsgHead + response.getMessage());
        }
    }

    public WarnMsgInfoDTO buildWarnMsgInfoDTO(String msg) {
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
    public List<PlatformWarehouseDTO> convert(List<TongYouWarehouseResp> sourceDataList) {
        return TongYouConverter.INSTANCE.warehouseConversion(sourceDataList);
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
