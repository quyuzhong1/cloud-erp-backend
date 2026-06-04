package com.erp.server.plm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.model.plm.dto.AttachmentDTO;
import com.erp.model.plm.entity.PlmAttachmentEntity;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.server.plm.mapper.PlmAttachmentMapper;
import com.erp.server.plm.service.PlmAttachmentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 附件表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-06-09
 */
@Slf4j
@Service
public class PlmAttachmentServiceImpl extends SuperServiceImpl<PlmAttachmentMapper, PlmAttachmentEntity> implements PlmAttachmentService {

    @Resource
    private FileFeign fileFeign;

    /**
     * 批量保存附件信息
     *
     * @param attachUrlList
     * @param attachNameList
     * @param type
     * @param businessId
     * @return void
     * @author yl
     * @date 2023-06-20 15:45
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<String> attachUrlList, List<String> attachNameList, String type, String businessId) {
        int nameSize = CollectionUtils.isNotEmpty(attachNameList) ? attachNameList.size() : 0;
        if (CollectionUtils.isNotEmpty(attachUrlList)) {
            List<PlmAttachmentEntity> addList = new ArrayList<>(attachUrlList.size());
            for (int i = 0; i < attachUrlList.size(); i++) {
                PlmAttachmentEntity entity = new PlmAttachmentEntity();
                entity.setAttachUrl(attachUrlList.get(i));
                if (CollectionUtils.isNotEmpty(attachNameList) && nameSize > i) {
                   entity.setAttachName(attachNameList.get(i));
                }
                entity.setType(type);
                entity.setBusinessId(businessId);
                addList.add(entity);
            }
            this.saveBatch(addList);
        }
    }

    /**
     * 根据业务表ids 获取附件信息
     *
     * @param businessIdList
     * @return void
     * @author yl
     * @date 2023-06-21 12:01
     */
    @Override
    public List<PlmAttachmentEntity> listByBusinessIds(List<String> businessIdList) {
        if (CollectionUtils.isEmpty(businessIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(PlmAttachmentEntity::getBusinessId, businessIdList).orderByDesc(PlmAttachmentEntity::getId).list();

    }

    @Override
    public List<PlmAttachmentEntity> listByBusinessIdAndType(String businessId,String type) {
        return this.lambdaQuery().eq(PlmAttachmentEntity::getBusinessId, businessId)
                .eq(StringUtils.isNotBlank(type), PlmAttachmentEntity::getType, type)
                .orderByDesc(PlmAttachmentEntity::getId).list();

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void edit(AttachmentDTO.EditDTO editDTO) {
        String businessId = editDTO.getBusinessId();
        String type = editDTO.getType();
        List<AttachmentDTO.AttachDTO> attachDTOList = editDTO.getAttachDTOS();

        // 查询现有
        List<PlmAttachmentEntity> existingList = listByBusinessIdAndType(businessId, type);

        // 如果新列表为空，清空所有
        // PlmAttachmentEntity.isDeleted 已配置 @TableLogic（BaseEntity 继承），removeByIds 实为逻辑删除
        if (CollUtil.isEmpty(attachDTOList)) {
            if (CollUtil.isNotEmpty(existingList)) {
                removeByIds(existingList.stream().map(PlmAttachmentEntity::getId).collect(Collectors.toList()));
            }
            return;
        }

        // 过滤有效的新数据（URL 非空）
        List<AttachmentDTO.AttachDTO> validNewList = attachDTOList.stream()
                .filter(dto -> StrUtil.isNotBlank(dto.getAttachUrl()))
                .collect(Collectors.toList());

        // 如果过滤后为空，同样清空（@TableLogic 软删）
        if (CollUtil.isEmpty(validNewList)) {
            if (CollUtil.isNotEmpty(existingList)) {
                removeByIds(existingList.stream().map(PlmAttachmentEntity::getId).collect(Collectors.toList()));
            }
            return;
        }

        Set<String> newUrls = validNewList.stream().map(AttachmentDTO.AttachDTO::getAttachUrl).collect(Collectors.toSet());
        Map<String, PlmAttachmentEntity> existingMap = existingList.stream()
                .collect(Collectors.toMap(PlmAttachmentEntity::getAttachUrl, e -> e, (e1, e2) -> e1));

        // 删除：现有中不在新列表的（@TableLogic 软删）
        List<String> deleteIds = existingList.stream()
                .filter(e -> !newUrls.contains(e.getAttachUrl()))
                .map(PlmAttachmentEntity::getId)
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(deleteIds)) {
            removeByIds(deleteIds);
        }

        // 新增或更新
        List<PlmAttachmentEntity> saveList = new ArrayList<>();
        for (AttachmentDTO.AttachDTO dto : validNewList) {
            PlmAttachmentEntity entity = existingMap.get(dto.getAttachUrl());
            boolean isNew = (entity == null);

            if (isNew) {
                entity = new PlmAttachmentEntity();
                entity.setBusinessId(businessId);
                entity.setType(type);
                entity.setAttachUrl(dto.getAttachUrl());
                entity.setAttachVersion(1);
            }

            // 更新名称（如果有变化）
            if (!StrUtil.equals(entity.getAttachName(), dto.getAttachName())) {
                entity.setAttachName(dto.getAttachName());
            }

            saveList.add(entity);
        }

        saveOrUpdateBatch(saveList);
    }

    @Override
    public PlmAttachmentEntity upload(MultipartFile multipartFile,String type) {
        double size = multipartFile.getSize();
        double fileSize = size / (1024 * 1024);
        fileSize = (double) Math.round(fileSize * 100) / 100;
        if (fileSize > 300) {
            throw new ServiceException(ApiError.FILE_SIZE_EXCEEDS_LIMIT, 300);
        }
        String fileName = multipartFile.getOriginalFilename();
        if (org.springframework.util.StringUtils.isEmpty(fileName)) {
            throw new ServiceException(ApiError.COMMON_PARAM_NAME_TOO_LONG);
        }
        if (fileName.length() > 200) {
            throw new ServiceException(ApiError.COMMON_PARAM_NAME_TOO_LONG);
        }
        String fileUrl = fileFeign.uploadFile(multipartFile);
        if (StringUtils.isBlank(fileUrl)) {
            throw new ServiceException(ApiError.FILE_UPLOAD_FAILED);
        }
        PlmAttachmentEntity attachmentEntity = new PlmAttachmentEntity();
        attachmentEntity.setAttachUrl(fileUrl);
        attachmentEntity.setAttachName(fileName.toLowerCase());
        attachmentEntity.setAttachSize(BigDecimal.valueOf(fileSize));
        attachmentEntity.setType(type);
        this.save(attachmentEntity);
        return attachmentEntity;
    }

    @Override
    public void removeAttachment(BaseIdDTO dto) {
        PlmAttachmentEntity plmAttachmentEntity = this.getById(dto.getId());
        if (ObjectUtil.isEmpty(plmAttachmentEntity)) {
            return;
        }
        //删除附件id
        this.removeById(plmAttachmentEntity.getId());
        //删除fastdfs
        fileFeign.deleteFile(plmAttachmentEntity.getAttachUrl());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PlmAttachmentEntity> batchUpload(List<MultipartFile> multipartFileList, String type) {
        if (CollectionUtils.isEmpty(multipartFileList)) {
            throw new ServiceException(ApiError.FILE_UPLOAD_FAILED);
        }
        List<PlmAttachmentEntity> resultList = new ArrayList<>();
        for(MultipartFile multipartFile :multipartFileList) {
            PlmAttachmentEntity entity = upload(multipartFile, type);
            resultList.add(entity);
        }
        return resultList;
    }

    @Override
    public void removeAttachmentByUrl(AttachmentDTO.DeleteDTO dto) {
        this.lambdaUpdate()
                .eq(PlmAttachmentEntity::getAttachUrl, dto.getAttachUrl())
                .eq(StringUtils.isNotBlank(dto.getBusinessId()), PlmAttachmentEntity::getBusinessId, dto.getBusinessId())
                .remove();
    }

    @Override
    public List<AttachmentDTO.CommonDTO> getUrlById(String id) {
        List<PlmAttachmentEntity> entities = this.lambdaQuery().eq(PlmAttachmentEntity::getBusinessId, id).list();
        if(CollectionUtils.isEmpty(entities)){
            return new ArrayList<>();
        }
        return BeanUtil.copyToList(entities,AttachmentDTO.CommonDTO.class);
    }

    @Override
    public List<AttachmentDTO.CommonDTO> getSkuUrlByPid(String id, String businessId, LocalDateTime createTime) {
        if(StringUtils.isBlank(id) || StringUtils.isBlank(businessId)){
            return Collections.emptyList();
        }

        // 构建查询条件
        LambdaQueryWrapper<PlmAttachmentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PlmAttachmentEntity::getBusinessId, businessId)
                .eq(PlmAttachmentEntity::getType, SourceTypeEnum.PRODUCT_DETAIL.getTableName());

        // 如果提供了 createTime，则匹配时间范围（前后1秒）
        if(createTime != null) {
            LocalDateTime startTime = createTime.minusSeconds(1);
            LocalDateTime endTime = createTime.plusSeconds(1);
            queryWrapper.ge(PlmAttachmentEntity::getCreateTime, startTime)
                    .le(PlmAttachmentEntity::getCreateTime, endTime);
        }

        queryWrapper.orderByDesc(PlmAttachmentEntity::getCreateTime);

        List<PlmAttachmentEntity> attachments = this.list(queryWrapper);

        if(CollectionUtils.isEmpty(attachments)){
            return Collections.emptyList();
        }

        return BeanUtil.copyToList(attachments, AttachmentDTO.CommonDTO.class);
    }
}
