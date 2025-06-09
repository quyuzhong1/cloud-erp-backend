package com.sdk.wms.goodcang.handle;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformTransferWarehouseDTO;
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
import com.sdk.wms.goodcang.dto.response.GoodCangLogisticsAndWarehouseResp;
import com.sdk.wms.goodcang.dto.response.GoodCangResponse;
import com.sdk.wms.goodcang.dto.response.GoodCangTransferWarehouseResp;
import com.sdk.wms.goodcang.dto.response.TwcToWarehouse;
import com.sdk.wms.goodcang.service.GoodCangService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 谷仓拉取中转仓数据
 **/
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.GOOD_CANG)
@BusinessType(BusinessTypeEnum.TRANSFER)
public class GoodCangTransferHandler extends AbstractPullThirdWarehouseHandler<GoodCangTransferWarehouseResp, PlatformTransferWarehouseDTO> {

    @Resource
    private GoodCangService goodCangService;

    @Resource
    private MQProducerService<T> mqProducerService;

    private final String failureMsgHead = "调用谷仓获取中转仓数据接口异常";

    @Override
    public List<GoodCangTransferWarehouseResp> download(JobTaskDTO data) {
        GoodCangResponse<GoodCangLogisticsAndWarehouseResp> response = goodCangService.getSmCodeTwcToWarehouse();
        checkResponse(response);
        List<GoodCangTransferWarehouseResp> respList = new ArrayList<>();

        //封装参数
        processList(response.getData().getAirList(), respList);
        processList(response.getData().getExpressList(), respList);
        processList(response.getData().getLclList(), respList);

        respList.forEach(v->{
            v.setUniqueId(MD5Util.toMD5(getPlatformDictEnum().getCode()+BusinessTypeEnum.TRANSFER.getCode()+v.getLogisticsChannelCode()+v.getTransferWarehouseCode()+v.getDestinationWarehouseCode()));
            v.setAuthId(data.getShopId());
        });
        return respList;
    }

    private <T extends GoodCangLogisticsAndWarehouseResp.Base> void processList(List<T> list, List<GoodCangTransferWarehouseResp> respList) {
        if(CollUtil.isNotEmpty(list)){
            for(T item : list){
                String logisticsChannelCode = item.getSmCode();
                String logisticsChannelName = item.getSmCodeName();
                List<TwcToWarehouse> twcToWarehouseList = item.getTwcToWarehouseList();
                for(TwcToWarehouse twcToWarehouse : twcToWarehouseList){
                    GoodCangTransferWarehouseResp resp = getGoodCangTransferWarehouseResp(twcToWarehouse, logisticsChannelCode, logisticsChannelName);
                    respList.add(resp);
                }
            }
        }
    }

    private GoodCangTransferWarehouseResp getGoodCangTransferWarehouseResp(TwcToWarehouse twcToWarehouse, String logisticsChannelCode, String logisticsChannelName) {
        GoodCangTransferWarehouseResp resp = new GoodCangTransferWarehouseResp();
        resp.setLogisticsChannelCode(logisticsChannelCode);
        resp.setLogisticsChannelName(logisticsChannelName);
        resp.setTransferWarehouseCode(twcToWarehouse.getTransitWarehouseCode());
        resp.setTransferWarehouseName(twcToWarehouse.getTransitWarehouseName());
        resp.setDestinationWarehouseCode(twcToWarehouse.getWarehouseCode());
        resp.setDestinationWarehouseName(twcToWarehouse.getWarehouseName());
        return resp;
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
        warnMsgInfo.setBizName("调用谷仓获取中转仓数据接口");
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_THIRD_SDK);
        warnMsgInfo.setTitle(failureMsgHead);
        warnMsgInfo.setTableName(this.getClass().getName());
        warnMsgInfo.setTableId("");
        warnMsgInfo.setKeyInfo(msg);
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        return warnMsgInfo;
    }

    @Override
    public List<PlatformTransferWarehouseDTO> convert(List<GoodCangTransferWarehouseResp> sourceDataList) {
        return GoodCangConverter.INSTANCE.transferWarehouseConversion(sourceDataList);
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP_WMS.getDesc();
    }

    private PlatformDictEnum getPlatformDictEnum(){
        return PlatformDictEnum.GOOD_CANG;
    }

    public boolean isSuccess(String ask){
        return "Success".equals(ask);
    }
}
