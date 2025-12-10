package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.wms.dto.B2bThirdDeliveryDetailDTO;
import com.erp.model.wms.entity.B2bThirdDeliveryDetailEntity;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.server.wms.convert.B2bThirdDeliveryConverter;
import com.erp.server.wms.mapper.B2bThirdDeliveryDetailMapper;
import com.erp.server.wms.service.B2bThirdDeliveryDetailService;
import com.erp.server.wms.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * B2B三方发货单明细 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-11-26
 */
@Slf4j
@Service
public class B2bThirdDeliveryDetailServiceImpl extends SuperServiceImpl<B2bThirdDeliveryDetailMapper, B2bThirdDeliveryDetailEntity> implements B2bThirdDeliveryDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Resource
    private SoInfoFeign soInfoFeign;

    @Override
    public List<B2bThirdDeliveryDetailEntity> listByMainIds(List<String> ids) {
        if (CollUtil.isEmpty(ids)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(B2bThirdDeliveryDetailEntity::getMainId,ids).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<B2bThirdDeliveryDetailEntity> batchAdd(String id, List<B2bThirdDeliveryDetailDTO.AddDTO> detailList) {
        this.deleteByMainId(id);//直接移除明细记录（发货数量在创建失败/取消发货时已经退到订单那边，不用重复回退）
        List<B2bThirdDeliveryDetailEntity> detailEntityList = B2bThirdDeliveryConverter.INSTANCE.toB2bThirdDeliveryDetail(detailList);
        //校验发货数量
        checkDeliveryQty(detailEntityList);
        List<SoDetailDTO.UpdateDeliveryStatusDTO> paramList = new ArrayList<>(detailEntityList.size());
        detailEntityList.forEach(e -> {
            e.setMainId(id);
            e.setBoxSpecNo(getBoxSpecNo(e.getSort()));
            SoDetailDTO.UpdateDeliveryStatusDTO statusDTO = new SoDetailDTO.UpdateDeliveryStatusDTO();
            statusDTO.setId(e.getSoDetailId());
            statusDTO.setDeliveryQty(e.getDeliveryQty());
            paramList.add(statusDTO);
        });
        //扣除已发货数量
        soInfoFeign.updateDeliveryStatus(paramList);
        this.saveBatch(detailEntityList);
        return detailEntityList;
    }

    private void checkDeliveryQty(List<B2bThirdDeliveryDetailEntity> detailEntityList) {
        List<String> soDetailIds = detailEntityList.stream().map(B2bThirdDeliveryDetailEntity::getSoDetailId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SoDetailEntity> soDetailEntityList = soInfoFeign.listSoDetailByIds(soDetailIds);
        detailEntityList.forEach(deliveryDetail -> {
            SoDetailEntity soDetailEntity = soDetailEntityList.stream().filter(e -> e.getId().equals(deliveryDetail.getSoDetailId())).findFirst().orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "销售订单明细不存在"));
            Integer qty = soDetailEntity.getQty();
            Integer deliveryQty = soDetailEntity.getDeliveryQty();
            Integer unDeliveryQty = deliveryDetail.getDeliveryQty();
            if (qty < (deliveryQty+unDeliveryQty)){
                throw new ServiceException("SKU【{}】销售数量【{}】已发数量【{}】下发数量【{}】超过了可发数量【{}】",soDetailEntity.getSkuNo(),qty,deliveryQty,unDeliveryQty,qty-deliveryQty);
            }
        });
    }

    @Override
    public void deleteByMainIds(List<String> mainIds) {
        if (CollUtil.isNotEmpty(mainIds)){
            this.lambdaUpdate().in(B2bThirdDeliveryDetailEntity::getMainId,mainIds).remove();
        }
    }

    private void deleteByMainId(String id) {
        if (CharSequenceUtil.isNotBlank(id)){
            this.lambdaUpdate().eq(B2bThirdDeliveryDetailEntity::getMainId,id).remove();
        }
    }

    private String getBoxSpecNo(Integer number) {
        //ZXGG0001
        String prefix = "ZXGG";
        String formatStr;

        if (number < 10) {
            formatStr = "000" + number;  // 个位数：000X
        } else if (number < 100) {
            formatStr = "00" + number;   // 十位数：00XX
        } else if (number < 1000) {
            formatStr = "0" + number;    // 百位数：0XXX
        } else {
            formatStr = String.valueOf(number); // 千位数：XXXX
        }

        return prefix + formatStr;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(B2bThirdDeliveryDetailEntity b2bThirdDeliveryDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
