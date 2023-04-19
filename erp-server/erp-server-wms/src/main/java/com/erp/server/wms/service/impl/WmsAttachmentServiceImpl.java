package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.model.wms.dto.WmsAttachmentDTO;
import com.erp.model.wms.entity.WmsAttachmentEntity;
import com.erp.server.wms.mapper.WmsAttachmentMapper;
import com.erp.server.wms.service.WmsAttachmentService;
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
public class WmsAttachmentServiceImpl extends SuperServiceImpl<WmsAttachmentMapper, WmsAttachmentEntity> implements WmsAttachmentService {

    @Override
    public void batchSave(List<String> attachmentUrlList, List<String> attachmentNameList, String type, String businessId) {

        if (CollectionUtils.isNotEmpty(attachmentUrlList) && attachmentUrlList.size() == attachmentNameList.size()) {
            List<WmsAttachmentEntity> addList = new ArrayList<>(attachmentUrlList.size());
            for (int i = 0; i < attachmentUrlList.size(); i++) {
                WmsAttachmentEntity entity = new WmsAttachmentEntity();
                entity.setAttachUrl(attachmentUrlList.get(i));
                entity.setAttachName(attachmentNameList.get(i));
                entity.setType(type);
                entity.setBusinessId(businessId);
                addList.add(entity);
            }
            this.saveBatch(addList);
        }

    }

    @Override
    public List<WmsAttachmentDTO.UpdateDTO> getByBusinessIds(List<String> businessIds) {
        LambdaQueryWrapper<WmsAttachmentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(WmsAttachmentEntity::getBusinessId, businessIds);
        List<WmsAttachmentEntity> list = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        return BeanMapper.copyList(list, WmsAttachmentDTO.UpdateDTO.class);

    }


    /**
     * 删除附件信息
     * @author yl
     * @date 2023-04-19 11:11
     * @param dto
     * @return void
     */
    @Override
    public void removeAttachment(AttachmentDTO.DeleteDTO dto) {
        LambdaQueryWrapper<WmsAttachmentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WmsAttachmentEntity::getAttachUrl, dto.getAttachUrl());
        if (StringUtils.isNotBlank(dto.getBusinessId())) {
            queryWrapper.eq(WmsAttachmentEntity::getBusinessId, dto.getBusinessId());

        }
        this.remove(queryWrapper);
        FastDFSClientUtil.deleteFile(dto.getAttachUrl());
    }
}
