package com.erp.server.wms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.AliexpressDeliveryDetailDTO;
import com.erp.model.wms.dto.AliexpressDeliveryProratedInfoDTO;
import com.erp.model.wms.entity.AliexpressDeliveryDetailEntity;
import com.erp.model.wms.entity.AliexpressDeliveryEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.AliexpressDeliveryDetailMapper;
import com.erp.server.wms.service.AliexpressDeliveryDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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


    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean addOrUpdate(List<AliexpressDeliveryDetailDTO.AddDTO> addDTO, AliexpressDeliveryEntity mainEntity, List<AliexpressDeliveryProratedInfoDTO> proratedInfoList) {
        if(CollectionUtils.isEmpty(addDTO)){
            return true;
        }
        if (addDTO.stream().anyMatch(e-> StringUtils.isBlank(e.getUniqueId()))){
            ServiceException.runError("速卖通发货明细唯一ID为空,平台单号【{}】", mainEntity.getPlatformCode());
        }
        if (addDTO.stream().anyMatch(e-> StringUtils.isBlank(e.getPlatformSpuNo()))){
            ServiceException.runError("速卖通发货明细产品ID为空,平台单号【{}】", mainEntity.getPlatformCode());
        }
        //先删除后新增
//        this.removeByMainId(addDTO.get(0).getMainId());
        // 补充明细ID
        List<AliexpressDeliveryDetailEntity> aliexpressDeliveryDetailEntityList = fillData(addDTO, mainEntity, proratedInfoList);

        log.info("开始新增/更新速卖通发货单详情");
        boolean save = super.saveOrUpdateBatch(aliexpressDeliveryDetailEntityList);
        if(!save) {
            throw new ServiceException("速卖通发货单详情新增/更新失败");
        }
        return true;
    }

    /**
     * 补充信息
     */
    private List<AliexpressDeliveryDetailEntity> fillData(List<AliexpressDeliveryDetailDTO.AddDTO> addDTO,
                                                          AliexpressDeliveryEntity mainEntity,
                                                          List<AliexpressDeliveryProratedInfoDTO> proratedInfoList) {
        List<AliexpressDeliveryDetailEntity> aliexpressDeliveryDetailEntityList = BeanUtil.copyToList(addDTO,AliexpressDeliveryDetailEntity.class);
        String mainId = mainEntity.getId();
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
        // 记录分摊信息
        // proratedInfoList按UniqueId分组
        Map<String, AliexpressDeliveryProratedInfoDTO> proratedInfoMap = proratedInfoList
                .stream()
                .collect(Collectors.toMap(AliexpressDeliveryProratedInfoDTO::getUniqueId, Function.identity()));

        for (AliexpressDeliveryDetailEntity aliexpressDeliveryDetailEntity : aliexpressDeliveryDetailEntityList) {
            AliexpressDeliveryProratedInfoDTO proratedInfoDTO = proratedInfoMap.get(aliexpressDeliveryDetailEntity.getUniqueId());
            if (null == proratedInfoDTO) {
                ServiceException.runError("速卖通发货单明细分摊信息不存在,明细唯一ID【{}】", aliexpressDeliveryDetailEntity.getUniqueId());
            }
            aliexpressDeliveryDetailEntity.setPlatformDetailId(proratedInfoDTO.getPlatformOrderDetailId());
            // 20260324调整为税后
            aliexpressDeliveryDetailEntity.setProratedAmount(proratedInfoDTO.getProratedAfterTaxAmount());
            aliexpressDeliveryDetailEntity.setProratedUnitPrice(proratedInfoDTO.getProratedAfterTaxUnitPrice());
        }
        return aliexpressDeliveryDetailEntityList;
    }


    public boolean removeByMainId(String mainId){
        return this.lambdaUpdate().eq(AliexpressDeliveryDetailEntity::getMainId, mainId).remove();
    }




}
