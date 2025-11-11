package com.sdk.wms.tongyou.handle;

import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformInventoryDTO;
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
import com.sdk.wms.tongyou.dto.request.TongYouGetInventoryReq;
import com.sdk.wms.tongyou.dto.response.TongYouInventoryResp;
import com.sdk.wms.tongyou.dto.response.TongYouResponse;
import com.sdk.wms.tongyou.service.TongYouService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 通邮拉取库存处理服务实现类
 * @author will
 * @date 2025/11/11 15:21
 */
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.TONG_YOU)
@BusinessType(BusinessTypeEnum.INVENTORY)
public class TongYouInventoryHandler extends AbstractPullThirdWarehouseHandler<TongYouInventoryResp, PlatformInventoryDTO> {

    @Resource
    private TongYouService tongYouService;

    @Resource
    private MQProducerService<T> mqProducerService;

    private final String failureMsgHead = "调用通邮获取库存数据接口异常";

    @Override
    public List<TongYouInventoryResp> download(JobTaskDTO data) {
        //查询数据
        TongYouGetInventoryReq req = new TongYouGetInventoryReq();
        //最大页码100，从第一页开始查询
        req.setPageSize(100);
        List<TongYouInventoryResp> respList = new ArrayList<>();
        int page = 1;
        while (true) {
            req.setPage(page);
            TongYouResponse<List<TongYouInventoryResp>> getProductInventory = tongYouService.getProductInventory(req);
            checkResponse(getProductInventory);
            respList.addAll(getProductInventory.getData());
            if (getProductInventory.getCount() <= page * 100) {
                break;
            }
            page++;
        }
        respList.forEach(v->{
            v.setUniqueId(MD5Util.toMD5(getPlatformDictEnum().getCode()+BusinessTypeEnum.INVENTORY.getCode()+v.getProductSku()+v.getWarehouseCode()));
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
        warnMsgInfo.setBizName("调用通邮获取库存数据接口");
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_THIRD_SDK);
        warnMsgInfo.setTitle(failureMsgHead);
        warnMsgInfo.setTableName(this.getClass().getName());
        warnMsgInfo.setTableId("");
        warnMsgInfo.setKeyInfo(msg);
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        return warnMsgInfo;
    }

    @Override
    public List<PlatformInventoryDTO> convert(List<TongYouInventoryResp> sourceDataList) {
        return TongYouConverter.INSTANCE.inventoryConversion(sourceDataList);
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
