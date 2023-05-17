package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.dto.OmsAttachmentDTO;
import com.erp.model.oms.entity.OmsAttachmentEntity;
import com.erp.server.oms.mapper.OmsAttachmentMapper;
import com.erp.server.oms.service.OmsAttachmentService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-19
 */
@Service
public class OmsAttachmentServiceImpl extends SuperServiceImpl<OmsAttachmentMapper, OmsAttachmentEntity> implements OmsAttachmentService {

    @Override
    public void batchSave(List<String> attachmentUrlList, List<String> attachmentNameList, String type, String businessId) {
        int nameSize = CollectionUtils.isNotEmpty(attachmentNameList) ? attachmentNameList.size() : 0;
        if (CollectionUtils.isNotEmpty(attachmentUrlList)) {
            List<OmsAttachmentEntity> addList = new ArrayList<>(attachmentUrlList.size());
            for (int i = 0; i < attachmentUrlList.size(); i++) {
                OmsAttachmentEntity entity = new OmsAttachmentEntity();
                entity.setAttachUrl(attachmentUrlList.get(i));
                if (CollectionUtils.isNotEmpty(attachmentNameList)) {
                    if (nameSize > i) {
                        entity.setAttachName(attachmentNameList.get(i));
                    }
                }
                entity.setType(type);
                entity.setBusinessId(businessId);
                addList.add(entity);
            }
            this.saveBatch(addList);
        }

    }

    /**
     * 先删除
     *
     * @param type
     * @param businessId
     */
    private void delete(String type, String businessId) {
        LambdaQueryWrapper<OmsAttachmentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OmsAttachmentEntity::getBusinessId, businessId);
        queryWrapper.eq(OmsAttachmentEntity::getType, type);
        this.remove(queryWrapper);
    }

    @Override
    public List<OmsAttachmentDTO.UpdateDTO> getByBusinessIds(List<String> businessIds) {
        LambdaQueryWrapper<OmsAttachmentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(OmsAttachmentEntity::getBusinessId, businessIds);
        List<OmsAttachmentEntity> list = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        return BeanMapper.copyList(list, OmsAttachmentDTO.UpdateDTO.class);

    }


    /**
     * 删除附件信息
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2023-04-19 11:11
     */
    @Override
    public void removeAttachment(OmsAttachmentDTO.DeleteDTO dto) {
        LambdaQueryWrapper<OmsAttachmentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OmsAttachmentEntity::getAttachUrl, dto.getAttachUrl());
        if (StringUtils.isNotBlank(dto.getBusinessId())) {
            queryWrapper.eq(OmsAttachmentEntity::getBusinessId, dto.getBusinessId());
        }
        this.remove(queryWrapper);
    }
}
