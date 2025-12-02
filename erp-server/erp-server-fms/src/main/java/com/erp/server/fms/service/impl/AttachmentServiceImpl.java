package com.erp.server.fms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.AttachDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.fms.dto.AttachmentDTO;
import com.erp.model.fms.entity.AttachmentEntity;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.server.fms.mapper.AttachmentMapper;
import com.erp.server.fms.service.AttachmentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * FMS附件 服务实现类
 * </p>
 *
 * @author wuht
 * @since 2025-10-16
 */
@Slf4j
@Service
public class AttachmentServiceImpl extends SuperServiceImpl<AttachmentMapper, AttachmentEntity> implements AttachmentService {

    @Resource
    private FileFeign fileFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<String> attachmentUrlList, List<String> attachmentNameList, String type, String businessId) {
        //先删除
        this.delete(type, businessId);
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
            log.info("FMS附件批量保存成功，共保存{}个附件", addList.size());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<AttachDTO> attachmentList, String type, String businessId) {
        //先删除
        this.delete(type, businessId);
        if (CollectionUtils.isNotEmpty(attachmentList)) {
            List<AttachmentEntity> addList = new ArrayList<>(attachmentList.size());
            for (int i = 0; i < attachmentList.size(); i++) {
                AttachmentEntity entity = new AttachmentEntity();
                entity.setAttachUrl(attachmentList.get(i).getAttachUrl());
                entity.setAttachName(attachmentList.get(i).getAttachName());
                entity.setType(type);
                entity.setBusinessId(businessId);
                addList.add(entity);
            }
            this.saveBatch(addList);
            log.info("FMS附件批量保存成功，共保存{}个附件", addList.size());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveNotDel(List<String> attachmentUrlList, List<String> attachmentNameList, String type, String businessId) {
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
            log.info("FMS附件批量保存成功(不删除原有)，共保存{}个附件", addList.size());
        }
    }

    /**
     * 先删除
     *
     * @param type 类型
     * @param businessId 业务ID
     */
    private void delete(String type, String businessId) {
        LambdaQueryWrapper<AttachmentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttachmentEntity::getBusinessId, businessId);
        queryWrapper.eq(AttachmentEntity::getType, type);
        this.remove(queryWrapper);
        log.info("FMS附件删除成功，type={}, businessId={}", type, businessId);
    }

    @Override
    public List<AttachmentDTO.UpdateDTO> getByBusinessIds(List<String> businessIds) {
        if (CollectionUtils.isEmpty(businessIds)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<AttachmentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(AttachmentEntity::getBusinessId, businessIds);
        List<AttachmentEntity> list = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return BeanMapper.copyList(list, AttachmentDTO.UpdateDTO.class);
    }

    /**
     * 删除附件信息
     *
     * @param dto 删除DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeAttachment(AttachmentDTO.DeleteDTO dto) {
        LambdaQueryWrapper<AttachmentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttachmentEntity::getAttachUrl, dto.getAttachUrl());
        if (CharSequenceUtil.isNotBlank(dto.getBusinessId())) {
            queryWrapper.eq(AttachmentEntity::getBusinessId, dto.getBusinessId());
        }
        this.remove(queryWrapper);
        fileFeign.deleteFile(dto.getAttachUrl());
        log.info("FMS附件删除成功，url={}", dto.getAttachUrl());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchRemoveAttachment(List<String> businessIds) {
        if (CollectionUtils.isEmpty(businessIds)) {
            return;
        }
        LambdaQueryWrapper<AttachmentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(AttachmentEntity::getBusinessId, businessIds);
        this.remove(queryWrapper);
        log.info("FMS附件批量删除成功，共删除{}个业务的附件", businessIds.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAttachment(AttachmentDTO.AddDTO dto) {
        String fileName = dto.getFileName();
        String url = dto.getUrl();
        if (CharSequenceUtil.isBlank(fileName) || CharSequenceUtil.isBlank(url)) {
            return;
        }
        AttachmentEntity entity = new AttachmentEntity();
        entity.setAttachUrl(url);
        entity.setAttachName(fileName);
        this.save(entity);
        log.info("FMS附件保存成功，fileName={}", fileName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByUrlList(List<String> urlList) {
        if (CollectionUtils.isNotEmpty(urlList)) {
            LambdaQueryWrapper<AttachmentEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(AttachmentEntity::getAttachUrl, urlList);
            this.remove(queryWrapper);
            //批量删除fastdfs 数据
            FastDFSClientUtil.deleteBatchFile(urlList);
            log.info("FMS附件根据URL列表删除成功，共删除{}个附件", urlList.size());
        }
    }
}

