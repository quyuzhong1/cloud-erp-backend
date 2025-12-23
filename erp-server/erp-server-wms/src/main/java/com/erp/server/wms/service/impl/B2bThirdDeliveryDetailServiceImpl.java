package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.wms.dto.B2bThirdDeliveryDetailDTO;
import com.erp.model.wms.entity.B2bThirdDeliveryDetailEntity;
import com.erp.model.wms.enums.ThirdDeliveryStatusEnum;
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
        this.deleteByMainId(id);//直接移除明细记录
        List<B2bThirdDeliveryDetailEntity> detailEntityList = B2bThirdDeliveryConverter.INSTANCE.toB2bThirdDeliveryDetail(detailList);
        //校验发货数量
        checkDeliveryQty(detailEntityList);
        detailEntityList.forEach(e -> {
            e.setMainId(id);
            e.setBoxSpecNo(getBoxSpecNo(e.getSort()));
        });
        this.saveBatch(detailEntityList);
        return detailEntityList;
    }

    private void checkDeliveryQty(List<B2bThirdDeliveryDetailEntity> detailEntityList) {
        List<String> soDetailIds = detailEntityList.stream().map(B2bThirdDeliveryDetailEntity::getSoDetailId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SoDetailEntity> soDetailEntityList = soInfoFeign.listSoDetailByIds(soDetailIds);
        //已生成发货单的发货数量
        List<B2bThirdDeliveryDetailEntity> oldDeliveryDetailList = this.listBySoDetailIds(soDetailIds);
        soDetailEntityList.forEach(soDetailEntity -> {
            //销售数量
            Integer qty = soDetailEntity.getQty();
            //已发货数量
            Integer deliveryQty = oldDeliveryDetailList.stream().filter(e -> !ThirdDeliveryStatusEnum.CANCEL_DELIVERY.getCode().equals(e.getStatus()) && CharSequenceUtil.equals(e.getSoDetailId(),soDetailEntity.getId())).map(B2bThirdDeliveryDetailEntity::getDeliveryQty).reduce(Integer::sum).orElse(0);
            //可发货数量
            int unDeliveryQty = qty - deliveryQty;
            //需要发货数量
            int needDeliveryQty = detailEntityList.stream().filter(e -> e.getSoDetailId().equals(soDetailEntity.getId())).mapToInt(B2bThirdDeliveryDetailEntity::getDeliveryQty).sum();
            if (unDeliveryQty < needDeliveryQty){
                throw new ServiceException("SKU【{}】销售数量【{}】已发数量【{}】超过了可发数量【{}】",soDetailEntity.getDeliverySkuNo(),qty,deliveryQty,unDeliveryQty);
            }
        });
    }

    @Override
    public List<B2bThirdDeliveryDetailEntity> listBySoDetailIds(List<String> soDetailIds) {
        if (CollUtil.isEmpty(soDetailIds)){
            return Collections.emptyList();
        }
        return baseMapper.listBySoDetailIds(soDetailIds);
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
        number = number + 1;
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
