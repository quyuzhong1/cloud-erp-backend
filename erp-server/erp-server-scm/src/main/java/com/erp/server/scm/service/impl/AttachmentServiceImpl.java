package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.model.scm.entity.AttachmentEntity;
import com.erp.server.scm.mapper.AttachmentMapper;
import com.erp.server.scm.service.AttachmentService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 公共附件表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Service
public class AttachmentServiceImpl extends SuperServiceImpl<AttachmentMapper, AttachmentEntity> implements AttachmentService {


    /**
     * 根据业务表id获取附件信息
     *
     * @param businessIds
     * @return java.util.List<com.erp.model.scm.dto.AttachmentDTO.UpdateDTO>
     * @author yl
     * @date 2023-03-20 10:27
     */
    @Override
    public List<AttachmentDTO.UpdateDTO> getByBusinessIds(List<String> businessIds) {
        List<AttachmentEntity> list = this.list(businessIds);
        return BeanMapper.copyList(list, AttachmentDTO.UpdateDTO.class);
    }


    /**
     * 根据业务表id 集合删除
     *
     * @param businessIdList
     * @return void
     * @author yl
     * @date 2023-03-20 11:52
     */
    @Override
    public void deleteByBusinessIds(List<String> businessIdList) {
        if (CollectionUtils.isNotEmpty(businessIdList)) {
            LambdaQueryWrapper<AttachmentEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(AttachmentEntity::getBusinessId, businessIdList);
            List<AttachmentEntity> list = this.list(queryWrapper);
            List<String> urlList = list.stream().map(AttachmentEntity::getAttachUrl).collect(Collectors.toList());
            //批量删除fastdfs 数据
            FastDFSClientUtil.deleteBatchFile(urlList);
            this.removeByIds(list.stream().map(AttachmentEntity::getId).collect(Collectors.toList()));
        }
    }

    /**
     * 批量添加附件
     *
     * @param attachmentUrlList
     * @param type
     * @param businessId
     * @return void
     * @author yl
     * @date 2023-03-23 16:09
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<String> attachmentUrlList, List<String> attachmentNameList, String type, String businessId) {
        if (CollectionUtils.isNotEmpty(attachmentUrlList) && attachmentUrlList.size() == attachmentNameList.size()) {
            List<AttachmentEntity> addList = new ArrayList<>(attachmentUrlList.size());
            for (int i = 0; i < attachmentUrlList.size(); i++) {
                AttachmentEntity entity = new AttachmentEntity();
                entity.setAttachUrl(attachmentUrlList.get(i));
                entity.setAttachName(attachmentNameList.get(i));
                entity.setType(type);
                entity.setBusinessId(businessId);
                addList.add(entity);
            }
            this.saveBatch(addList);
        }

    }


    /**
     * 根据业务表id 获取附件信息
     *
     * @param businessId
     * @return com.erp.model.scm.dto.AttachmentDTO.UpdateDTO
     * @author yl
     * @date 2023-03-27 9:37
     */
    @Override
    public List<AttachmentDTO.UpdateDTO> getByBusinessId(String businessId) {
        LambdaQueryWrapper<AttachmentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttachmentEntity::getBusinessId, businessId);
        List<AttachmentEntity> list = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        return BeanMapper.copyList(list, AttachmentDTO.UpdateDTO.class);
    }


    private List<AttachmentEntity> list(List<String> businessIds) {
        if (CollectionUtils.isEmpty(businessIds)) {
            return new ArrayList<>(1);
        }
        LambdaQueryWrapper<AttachmentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(AttachmentEntity::getBusinessId, businessIds);
        return this.list(queryWrapper);
    }
}
