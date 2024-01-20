package com.erp.server.srm.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.dto.OmsAttachmentDTO;
import com.erp.model.oms.entity.OmsAttachmentEntity;
import com.erp.model.srm.entity.AttachmentEntity;
import com.erp.server.srm.mapper.AttachmentMapper;
import com.erp.server.srm.service.AttachmentService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.srm.service.OperateLogService;
import com.erp.server.srm.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.srm.dto.AttachmentDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
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
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @Override
    public void batchSave(List<String> attachmentUrlList, List<String> attachmentNameList, String type, String businessId) {
        int nameSize = CollectionUtils.isNotEmpty(attachmentNameList) ? attachmentNameList.size() : 0;
        if (CollectionUtils.isNotEmpty(attachmentUrlList)) {
            List<AttachmentEntity> addList = new ArrayList<>(attachmentUrlList.size());
            for (int i = 0; i < attachmentUrlList.size(); i++) {
                AttachmentEntity entity = new AttachmentEntity();
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
