package com.sdk.wms.iml.handle;

import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformCityDictDTO;
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
import com.sdk.wms.iml.dto.response.ImlRegionResp;
import com.sdk.wms.iml.dto.response.ImlResponse;
import com.sdk.wms.iml.service.ImlService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 艾姆勒拉取城市基础数据
 **/
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.IML)
@BusinessType(BusinessTypeEnum.CITY_DICT)
public class ImlCityDictHandler extends AbstractPullThirdWarehouseHandler<ImlRegionResp, PlatformCityDictDTO> {

    @Resource
    private ImlService imlService;

    @Resource
    private MQProducerService<T> mqProducerService;

    private final String FAIL_MSG_HEAD = "调用艾姆勒获取区域数据接口异常";

    @Override
    public List<ImlRegionResp> download(JobTaskDTO data) {
        ImlResponse<List<ImlRegionResp>> response = imlService.getReceivingRegion();
        checkResponse(response);
        List<ImlRegionResp> imlWarehouseData = response.getData();
        imlWarehouseData.forEach(v->{
            v.setUniqueId(MD5Util.toMD5(getPlatformDictEnum().getCode()+BusinessTypeEnum.CITY_DICT.getCode()+v.getRegionName()));
            v.setAuthId(data.getShopId());
        });
        return imlWarehouseData;
    }

    private void checkResponse(ImlResponse<?> response) {
        if (!isSuccess(response.getAsk())) {
            log.error(FAIL_MSG_HEAD + response.getMessage());
            WarnMsgInfoDTO msgInfoDTO = this.buildWarnMsgInfoDTO(response.getMessage());
            mqProducerService.sendWarnMsg(msgInfoDTO);
            throw new ServiceException(FAIL_MSG_HEAD + response.getMessage());
        }
    }

    private WarnMsgInfoDTO buildWarnMsgInfoDTO(String msg) {
        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName("调用艾姆勒获取区域数据接口");
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_THIRD_SDK);
        warnMsgInfo.setTitle(FAIL_MSG_HEAD);
        warnMsgInfo.setTableName(this.getClass().getName());
        warnMsgInfo.setTableId("");
        warnMsgInfo.setKeyInfo(msg);
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        return warnMsgInfo;
    }

    @Override
    public List<PlatformCityDictDTO> convert(List<ImlRegionResp> sourceDataList) {
        return ImlConverter.INSTANCE.regionConversion(sourceDataList);
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP_SYS.getDesc();
    }

    private PlatformDictEnum getPlatformDictEnum(){
        return PlatformDictEnum.IML;
    }
    public boolean isSuccess(String ask){
        return "Success".equals(ask);
    }
}
