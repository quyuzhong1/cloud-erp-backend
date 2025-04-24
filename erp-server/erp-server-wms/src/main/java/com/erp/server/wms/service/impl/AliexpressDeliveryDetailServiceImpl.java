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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public Boolean addOrUpdate(List<AliexpressDeliveryDetailDTO.AddDTO> addDTO, String platformCode) {
        if(CollectionUtils.isEmpty(addDTO)){
            return true;
        }
        if (addDTO.stream().anyMatch(e-> StringUtils.isBlank(e.getUniqueId()))){
            ServiceException.runError("速卖通发货明细唯一ID为空,平台单号【{}】", platformCode);
        }
        if (addDTO.stream().anyMatch(e-> StringUtils.isBlank(e.getPlatformSpuNo()))){
            ServiceException.runError("速卖通发货明细产品ID为空,平台单号【{}】", platformCode);
        }

        //先删除后新增
//        this.removeByMainId(addDTO.get(0).getMainId());
        List<AliexpressDeliveryDetailEntity> aliexpressDeliveryDetailEntityList = BeanUtil.copyToList(addDTO,AliexpressDeliveryDetailEntity.class);

        String mainId = addDTO.get(0).getMainId();
        // 查询历史已存在明细
        List<String> detailUniqueIds = addDTO.stream().map(AliexpressDeliveryDetailDTO.AddDTO::getUniqueId).distinct().collect(Collectors.toList());
        Map<String, AliexpressDeliveryDetailEntity> existDetailMap = lambdaQuery()
                .eq(AliexpressDeliveryDetailEntity::getMainId, mainId)
                .in(AliexpressDeliveryDetailEntity::getUniqueId, detailUniqueIds)
                .list()
                .stream()
                .collect(Collectors.toMap(AliexpressDeliveryDetailEntity::getUniqueId, e -> e));
        if (!existDetailMap.isEmpty()) {
            // 根据唯一ID设置已存在ID
            for (AliexpressDeliveryDetailEntity aliexpressDeliveryDetailEntity : aliexpressDeliveryDetailEntityList) {
                AliexpressDeliveryDetailEntity existDetailEntity = existDetailMap.get(aliexpressDeliveryDetailEntity.getUniqueId());
                if(null != existDetailEntity){
                    aliexpressDeliveryDetailEntity.setId(existDetailEntity.getId());
                    aliexpressDeliveryDetailEntity.setCreateTime(existDetailEntity.getCreateTime());
                }
            }
        }

        log.info("开始新增/更新速卖通发货单详情");
        boolean save = super.saveOrUpdateBatch(aliexpressDeliveryDetailEntityList);
        if(!save) {
            throw new ServiceException("速卖通发货单详情新增/更新失败");
        }
        return true;
    }


    public boolean removeByMainId(String mainId){
        return this.lambdaUpdate().eq(AliexpressDeliveryDetailEntity::getMainId, mainId).remove();
    }
}
