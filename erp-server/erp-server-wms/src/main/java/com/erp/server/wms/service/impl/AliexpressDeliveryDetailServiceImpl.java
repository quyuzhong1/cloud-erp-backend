package com.erp.server.wms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.AliexpressDeliveryDetailDTO;
import com.erp.model.wms.entity.AliexpressDeliveryDetailEntity;
import com.erp.server.wms.mapper.AliexpressDeliveryDetailMapper;
import com.erp.server.wms.service.AliexpressDeliveryDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * <p>
 * 速卖通发货单详情 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-05-06
 */
@Slf4j
@Service
public class AliexpressDeliveryDetailServiceImpl extends SuperServiceImpl<AliexpressDeliveryDetailMapper, AliexpressDeliveryDetailEntity> implements AliexpressDeliveryDetailService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(List<AliexpressDeliveryDetailDTO.AddDTO> addDTO) {
        if(CollectionUtils.isEmpty(addDTO)){
            return true;
        }
        //先删除后新增
        this.removeByMainId(addDTO.get(0).getMainId());
        List<AliexpressDeliveryDetailEntity> aliexpressDeliveryDetailEntityList = BeanUtil.copyToList(addDTO,AliexpressDeliveryDetailEntity.class);
        log.info("开始新增速卖通发货单详情");
        boolean save = super.saveBatch(aliexpressDeliveryDetailEntityList);
        if(!save) {
            throw new ServiceException("速卖通发货单详情保存失败");
        }
        return true;
    }


    public boolean removeByMainId(String mainId){
        return this.lambdaUpdate().eq(AliexpressDeliveryDetailEntity::getMainId, mainId).remove();
    }
}
