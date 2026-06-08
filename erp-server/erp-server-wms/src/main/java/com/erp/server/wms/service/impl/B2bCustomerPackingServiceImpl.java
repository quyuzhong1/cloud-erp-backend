package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.AttachDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    @DistributeLocker(keyName = "mainId")
    @Transactional(rollbackFor = Exception.class)
    public List<B2bCustomerPackingEntity> batchSave(String mainId, List<B2bCustomerPackingDTO.AddDTO> packingList) {
        if (CharSequenceUtil.isNotBlank(mainId)) {
            deleteExistingByMainIds(Collections.singletonList(mainId));
        }
        if (CollUtil.isEmpty(packingList)) {
            return Collections.emptyList();
        }
        List<B2bCustomerPackingEntity> entityList = new ArrayList<>();
        int sort = 0;
        for (B2bCustomerPackingDTO.AddDTO box : packingList) {
            for (B2bCustomerPackingDTO.LineAddDTO line : box.getPackingLineList()) {
                entityList.add(toEntity(mainId, box, line, line.getSort() != null ? line.getSort() : sort++));
            }
        }
        boolean saved = super.saveBatch(entityList, 500);
        if (!saved) {
            throw new ServiceException("B2B客户装箱明细批量保存失败");
        }
        saveBoxAttachments(packingList, entityList);
        return entityList;
    }

    private void saveBoxAttachments(List<B2bCustomerPackingDTO.AddDTO> packingList,
                                    List<B2bCustomerPackingEntity> entityList) {
        Map<Integer, B2bCustomerPackingEntity> boxHeadMap = buildBoxHeadMap(entityList);
        for (B2bCustomerPackingDTO.AddDTO box : packingList) {
            List<AttachDTO> attachList = box.getAttachList();
            if (CollUtil.isNotEmpty(attachList)) {
                B2bCustomerPackingEntity entity = boxHeadMap.get(box.getBoxSeq());
                if (entity != null) {
                    wmsAttachmentService.batchSave(attachList, ModuleTypeEnum.B2B_CUSTOMER_PACKING_LABEL.getCode(), entity.getId());
                }
            }
        }
    }

    private Map<Integer, B2bCustomerPackingEntity> buildBoxHeadMap(List<B2bCustomerPackingEntity> entityList) {
        Map<Integer, B2bCustomerPackingEntity> boxHeadMap = new HashMap<>();
        for (B2bCustomerPackingEntity entity : entityList) {
            B2bCustomerPackingEntity head = boxHeadMap.get(entity.getBoxSeq());
            if (head == null || compareSort(entity, head) < 0) {
                boxHeadMap.put(entity.getBoxSeq(), entity);
            }
        }
        return boxHeadMap;
    }

    /**
     * 比较装箱行的 sort 字段，用于确定箱内首行（箱头）；null 排在最后（视为最大值）。
     */
    private int compareSort(B2bCustomerPackingEntity left, B2bCustomerPackingEntity right) {
        if (left.getSort() == null && right.getSort() == null) {
            return 0;
        }
        if (left.getSort() == null) {
            return 1;
        }
        if (right.getSort() == null) {
            return -1;
        }
        return left.getSort().compareTo(right.getSort());
    }

    private B2bCustomerPackingEntity toEntity(String mainId, B2bCustomerPackingDTO.AddDTO box,
                                              B2bCustomerPackingDTO.LineAddDTO line, int sort) {
        B2bCustomerPackingEntity entity = new B2bCustomerPackingEntity();
        entity.setMainId(mainId);
        entity.setBoxSeq(box.getBoxSeq());
        entity.setBoxMarkNo(CharSequenceUtil.blankToDefault(box.getBoxMarkNo(), ""));
        entity.setBoxMarkRefNo(CharSequenceUtil.blankToDefault(box.getBoxMarkRefNo(), ""));
        entity.setLabelSize(CharSequenceUtil.blankToDefault(box.getLabelSize(), ""));
        entity.setLabelingRequirement(CharSequenceUtil.blankToDefault(box.getLabelingRequirement(), ""));
        entity.setSkuId(CharSequenceUtil.blankToDefault(line.getSkuId(), ""));
        entity.setSkuNo(line.getSkuNo());
        entity.setProductName(CharSequenceUtil.blankToDefault(line.getProductName(), ""));
        entity.setSaleQty(line.getSaleQty() != null ? line.getSaleQty() : 0);
        entity.setPackingQty(line.getPackingQty());
        entity.setWarehousePlatformSku(CharSequenceUtil.blankToDefault(line.getWarehousePlatformSku(), ""));
        entity.setSort(sort);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByMainIds(List<String> mainIds) {
        deleteExistingByMainIds(mainIds);
    }

    private void deleteExistingByMainIds(List<String> mainIds) {
        if (CollUtil.isEmpty(mainIds)) {
            return;
        }
        List<B2bCustomerPackingEntity> packingList = listByMainIds(mainIds);
        if (CollUtil.isNotEmpty(packingList)) {
            List<String> packingIds = packingList.stream().map(B2bCustomerPackingEntity::getId).collect(Collectors.toList());
            wmsAttachmentService.batchRemoveAttachment(packingIds);
        }
        lambdaUpdate()
                .in(B2bCustomerPackingEntity::getMainId, mainIds)
                .set(B2bCustomerPackingEntity::getIsDeleted, Boolean.TRUE)
                .update();
    }
}
