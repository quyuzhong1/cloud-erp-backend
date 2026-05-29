package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.dto.AttachDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.B2bCustomerPackingDTO;
import com.erp.model.wms.entity.B2bCustomerPackingEntity;
import com.erp.server.wms.mapper.B2bCustomerPackingMapper;
import com.erp.server.wms.service.B2bCustomerPackingService;
import com.erp.server.wms.service.WmsAttachmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * B2B客户装箱 服务实现类
 */
@Slf4j
@Service
public class B2bCustomerPackingServiceImpl extends SuperServiceImpl<B2bCustomerPackingMapper, B2bCustomerPackingEntity>
        implements B2bCustomerPackingService {

    @Resource
    private WmsAttachmentService wmsAttachmentService;

    @Override
    public List<B2bCustomerPackingEntity> listByMainIds(List<String> mainIds) {
        if (CollUtil.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(B2bCustomerPackingEntity::getMainId, mainIds)
                .orderByAsc(B2bCustomerPackingEntity::getSort)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<B2bCustomerPackingEntity> batchSave(String mainId, List<B2bCustomerPackingDTO.AddDTO> packingList) {
        deleteByMainId(mainId);
        if (CollUtil.isEmpty(packingList)) {
            return Collections.emptyList();
        }
        List<B2bCustomerPackingEntity> entityList = new ArrayList<>();
        int sort = 0;
        for (B2bCustomerPackingDTO.AddDTO dto : packingList) {
            B2bCustomerPackingEntity entity = new B2bCustomerPackingEntity();
            entity.setMainId(mainId);
            entity.setBoxSeq(dto.getBoxSeq());
            entity.setBoxMarkNo(CharSequenceUtil.blankToDefault(dto.getBoxMarkNo(), ""));
            entity.setBoxMarkRefNo(CharSequenceUtil.blankToDefault(dto.getBoxMarkRefNo(), ""));
            entity.setLabelSize(CharSequenceUtil.blankToDefault(dto.getLabelSize(), ""));
            entity.setLabelingRequirement(CharSequenceUtil.blankToDefault(dto.getLabelingRequirement(), ""));
            entity.setSkuId(CharSequenceUtil.blankToDefault(dto.getSkuId(), ""));
            entity.setSkuNo(dto.getSkuNo());
            entity.setProductName(CharSequenceUtil.blankToDefault(dto.getProductName(), ""));
            entity.setSaleQty(dto.getSaleQty() != null ? dto.getSaleQty() : 0);
            entity.setPackingQty(dto.getPackingQty());
            entity.setWarehousePlatformSku(CharSequenceUtil.blankToDefault(dto.getWarehousePlatformSku(), ""));
            entity.setSort(dto.getSort() != null ? dto.getSort() : sort++);
            entityList.add(entity);
        }
        saveBatch(entityList);
        for (int i = 0; i < packingList.size(); i++) {
            List<AttachDTO> attachList = packingList.get(i).getAttachList();
            if (CollUtil.isNotEmpty(attachList)) {
                wmsAttachmentService.batchSave(attachList, ModuleTypeEnum.B2B_CUSTOMER_PACKING_LABEL.getCode(), entityList.get(i).getId());
            }
        }
        return entityList;
    }

    @Override
    public void deleteByMainIds(List<String> mainIds) {
        if (CollUtil.isEmpty(mainIds)) {
            return;
        }
        List<B2bCustomerPackingEntity> packingList = listByMainIds(mainIds);
        if (CollUtil.isNotEmpty(packingList)) {
            List<String> packingIds = packingList.stream().map(B2bCustomerPackingEntity::getId).collect(Collectors.toList());
            wmsAttachmentService.batchRemoveAttachment(packingIds);
        }
        lambdaUpdate().in(B2bCustomerPackingEntity::getMainId, mainIds).remove();
    }

    private void deleteByMainId(String mainId) {
        if (CharSequenceUtil.isNotBlank(mainId)) {
            deleteByMainIds(Collections.singletonList(mainId));
        }
    }
}
