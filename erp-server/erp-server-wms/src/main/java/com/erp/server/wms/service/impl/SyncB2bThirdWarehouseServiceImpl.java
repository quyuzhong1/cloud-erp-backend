package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.WmsAttachmentDTO;
import com.erp.model.wms.dto.third.ThirdWarehouseCancelFbaOutboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseCreateFbaOutboundReq;
import com.erp.model.wms.entity.B2bThirdDeliveryDetailEntity;
import com.erp.model.wms.entity.B2bThirdDeliveryEntity;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.model.wms.entity.WmsPushMsgEntity;
import com.erp.server.wms.convert.B2bThirdDeliveryConverter;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class SyncB2bThirdWarehouseServiceImpl implements SyncB2bThirdWarehouseService {

    @Autowired
    private OperateLogService operateLogService;
    @Resource
    private WmsPushMsgService wmsPushMsgService;
    @Resource
    private OverseasProviderWarehouseService overseasProviderWarehouseService;
    @Resource
    private WmsAttachmentService wmsAttachmentService;
    @Override
    public void syncB2bThirdWarehouseCreate(B2bThirdDeliveryEntity entity, List<B2bThirdDeliveryDetailEntity> detailEntityList, String operate) {
        if (Objects.isNull(entity.getIsApiDelivery()) || !entity.getIsApiDelivery()){
            return;
        }
        OverseasProviderEntity overseasProviderEntity = overseasProviderWarehouseService.findPlatformByWarehouseId(entity.getDeliveryWarehouseId());
        if (Objects.isNull(overseasProviderEntity)){
            throw new ServiceException(ApiError.NOT_FOUND_OVERSEAS_PROVIDE);
        }
        List<WmsAttachmentDTO.UpdateDTO> attachmentList = wmsAttachmentService.getByBusinessIds(Collections.singletonList(entity.getId()), ModuleTypeEnum.B2B_THIRD_DELIVERY.getCode());
        ThirdWarehouseCreateFbaOutboundReq req = B2bThirdDeliveryConverter.INSTANCE.toCreateFbaOutboundReq(entity, detailEntityList);
        req.setAuthId(overseasProviderEntity.getId());
        req.setThirdWarehouseProvideCode(overseasProviderEntity.getCode());
        req.setFileUrl(CollUtil.isNotEmpty(attachmentList) ? FastDFSClientUtil.publicUrl + "/" + attachmentList.get(0).getAttachUrl() : null);
        WmsPushMsgEntity wmsPushMsgEntity = new WmsPushMsgEntity();
        wmsPushMsgEntity.setSourceId(entity.getId());
        wmsPushMsgEntity.setSourceCode(entity.getCode());
        wmsPushMsgEntity.setSourceType(SourceTypeEnum.B2B_THIRD_DELIVERY.getCode());
        wmsPushMsgEntity.setPushData(JSONObject.toJSONString(req));
        wmsPushMsgEntity.setTargetPlatform(overseasProviderEntity.getCode());
        wmsPushMsgEntity.setSyncOperate(operate);
        wmsPushMsgService.save(wmsPushMsgEntity);

        String msg = StrUtil.format("用户【{}】操作【{}】单据单号为【{}】异步创建三方仓出库订单", UserContext.getDefaultLoginUser().getUserName(), "B2B三方发货单" , entity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.B2B_THIRD_DELIVERY.getCode(), entity.getId(), "创建三方仓出库订单");
    }

    @Override
    public void syncB2bThirdWarehouseCancel(B2bThirdDeliveryEntity entity, String operate) {
        if (Objects.isNull(entity.getIsApiDelivery()) || !entity.getIsApiDelivery()){
            return;
        }
        OverseasProviderEntity overseasProviderEntity = overseasProviderWarehouseService.findPlatformByWarehouseId(entity.getDeliveryWarehouseId());
        if (Objects.isNull(overseasProviderEntity)){
            throw new ServiceException(ApiError.NOT_FOUND_OVERSEAS_PROVIDE);
        }

        ThirdWarehouseCancelFbaOutboundReq req = new ThirdWarehouseCancelFbaOutboundReq();
        req.setAuthId(overseasProviderEntity.getId());
        req.setThirdWarehouseProvideCode(overseasProviderEntity.getCode());
        req.setErpOrderCode(entity.getCode());
        req.setOrderCode(entity.getPlatformOrderCode());
        req.setSourceId(entity.getId());
        req.setSourceCode(entity.getCode());
        req.setSoCode(entity.getSoCode());

        WmsPushMsgEntity wmsPushMsgEntity = new WmsPushMsgEntity();
        wmsPushMsgEntity.setSourceId(entity.getId());
        wmsPushMsgEntity.setSourceCode(entity.getCode());
        wmsPushMsgEntity.setSourceType(SourceTypeEnum.B2B_THIRD_DELIVERY.getCode());
        wmsPushMsgEntity.setPushData(JSONObject.toJSONString(req));
        wmsPushMsgEntity.setTargetPlatform(overseasProviderEntity.getCode());
        wmsPushMsgEntity.setSyncOperate(operate);
        wmsPushMsgService.save(wmsPushMsgEntity);

        String msg = StrUtil.format("用户【{}】操作【{}】单据单号为【{}】异步拦截三方仓出库订单，等待三方仓处理/未同步三方仓", UserContext.getDefaultLoginUser().getUserName(), "B2B三方发货单" , entity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.B2B_THIRD_DELIVERY.getCode(), entity.getId(), "发货拦截");
    }
}
