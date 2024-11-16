package com.erp.server.srm.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.common.core.utils.BeanMapper;
import com.erp.model.srm.dto.AttachmentDTO;
import com.erp.model.srm.entity.AttachmentEntity;
import com.erp.server.srm.mapper.AttachmentMapper;
import com.erp.server.srm.service.AttachmentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * <p>
 * 公共附件表 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-01-20
 */
@Slf4j
@Service
public class AttachmentServiceImpl extends SuperServiceImpl<AttachmentMapper, AttachmentEntity> implements AttachmentService {

    @Override
    public void batchSave(List<String> attachmentUrlList, List<String> attachmentNameList, String type, String businessId) {
        int nameSize = CollectionUtils.isNotEmpty(attachmentNameList) ? attachmentNameList.size() : 0;
        if (CollectionUtils.isNotEmpty(attachmentUrlList)) {
            List<AttachmentEntity> addList = new ArrayList<>(attachmentUrlList.size());
            for (int i = 0; i < attachmentUrlList.size(); i++) {
                AttachmentEntity entity = new AttachmentEntity();
                entity.setAttachUrl(attachmentUrlList.get(i));
                if (CollectionUtils.isNotEmpty(attachmentNameList) && nameSize > i) {
                        entity.setAttachName(attachmentNameList.get(i));
                    }

                entity.setType(type);
                entity.setBusinessId(businessId);
                addList.add(entity);
            }
            AttachmentServiceImpl bean = ApplicationContextUtils.getBean(AttachmentServiceImpl.class);
            bean.saveBatch(addList);
        }

    }

    @Override
    public List<AttachmentDTO.UpdateDTO> listByBusinessIds(List<String> businessIds) {
        if (CollectionUtils.isEmpty(businessIds)) {
            return Collections.EMPTY_LIST;
        }
        LambdaQueryWrapper<AttachmentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(AttachmentEntity::getBusinessId, businessIds);
        List<AttachmentEntity> list = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        return BeanMapper.copyList(list, AttachmentDTO.UpdateDTO.class);
    }


    @Override
    public void removeAttachment(AttachmentDTO.DeleteDTO dto) {
        LambdaQueryWrapper<AttachmentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttachmentEntity::getAttachUrl, dto.getAttachUrl());
        if (StringUtils.isNotBlank(dto.getBusinessId())) {
            queryWrapper.eq(AttachmentEntity::getBusinessId, dto.getBusinessId());
        }
        this.remove(queryWrapper);
    }



}
