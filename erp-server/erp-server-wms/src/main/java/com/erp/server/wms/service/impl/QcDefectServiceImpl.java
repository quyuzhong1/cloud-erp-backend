package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.AttachDTO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.QcDefectDTO;
import com.erp.model.wms.entity.QcDefectEntity;
import com.erp.model.wms.entity.QcProductEntity;
import com.erp.model.wms.entity.WmsAttachmentEntity;
import com.erp.model.wms.enums.WmsDefectLevelEnum;
import com.erp.server.wms.constant.WmsConstant;
import com.erp.server.wms.mapper.QcDefectMapper;
import com.erp.server.wms.service.QcDefectService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.WmsAttachmentService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wtr
 * @since 2026-03-23
 */
@Slf4j
@Service
public class QcDefectServiceImpl extends SuperServiceImpl<QcDefectMapper, QcDefectEntity> implements QcDefectService {

    @Resource
    private WmsAttachmentService attachmentService;

    @Override
    @Transactional
    public void add(String billId, List<QcDefectDTO.AddDTO> qcDefectList) {
        // 查询已存在的缺陷记录
        List<QcDefectEntity> exitList = this.lambdaQuery()
                .eq(QcDefectEntity::getMainId, billId)
                .list();

        if (Objects.isNull(qcDefectList) || qcDefectList.isEmpty()) {
            if (CollectionUtils.isNotEmpty(exitList)) {
                List<String> defectIds = exitList.stream().map(QcDefectEntity::getId).collect(Collectors.toList());
                attachmentService.batchRemoveAttachment(defectIds);
                removeByIds(defectIds);
            }
            return;
        }

        // 校验 defectLevel 是否重复
        Set<String> defectLevelSet = new HashSet<>();
        for (QcDefectDTO.AddDTO addDTO : qcDefectList) {
            String defectLevel = addDTO.getDefectLevel();
            if (StringUtils.isNotBlank(defectLevel)) {
                if (!defectLevelSet.add(defectLevel)) {
                    throw new ServiceException(ApiError.PO_QC_DEFECT_LEVEL_DUPLICATE);
                }
            }
        }

        // 创建已存在缺陷的ID集合和缺陷等级映射
        Set<String> existingIds = exitList.stream()
                .map(QcDefectEntity::getId)
                .collect(Collectors.toSet());

        List<QcDefectEntity> toUpdate = new ArrayList<>();
        List<String> toDeleteIds = new ArrayList<>();
        // 处理新增和更新
        for (QcDefectDTO.AddDTO addDTO : qcDefectList) {
            // 校验数据完整性
            boolean hasDefectLevel = StringUtils.isNotBlank(addDTO.getDefectLevel());
            boolean hasDefectQty = addDTO.getDefectQty() != null && addDTO.getDefectQty() > 0;
            boolean hasProblemAttribute = StringUtils.isNotBlank(addDTO.getIssueProperty());
            boolean hasDefectDesc = StringUtils.isNotBlank(addDTO.getDefectDesc());
            boolean hasDefectImage = Objects.nonNull(addDTO.getBadImageViewList()) && !addDTO.getBadImageViewList().isEmpty();

            if ((hasDefectLevel || hasDefectQty || hasProblemAttribute || hasDefectDesc || hasDefectImage)
                    && !(hasDefectLevel && hasDefectQty && hasProblemAttribute && hasDefectDesc && hasDefectImage)) {
                throw new ServiceException(ApiError.PO_QC_DEFECT_INFO_INCOMPLETE);
            }

            // 根据是否有ID决定是新增还是更新
            if (StringUtils.isBlank(addDTO.getId())) {
                // 新增记录
                QcDefectEntity newEntity = new QcDefectEntity();
                BeanUtils.copyProperties(addDTO, newEntity);
                newEntity.setBadQty(addDTO.getDefectQty());
                newEntity.setMainId(billId); // 确保设置主ID
                //新增缺陷记录
                save(newEntity);

                // 处理新增的附件
                if (hasDefectImage) {
                    List<AttachDTO> addAttachDTOS = buildAttachDTOList(addDTO.getBadImageViewList());
                    attachmentService.batchSave(addAttachDTOS, WmsConstant.QC_DEFECT, newEntity.getId());
                }
            } else {
                // 更新记录 - 检查是否存在
                if (existingIds.contains(addDTO.getId())) {
                    QcDefectEntity existingEntity = exitList.stream()
                            .filter(e -> e.getId().equals(addDTO.getId()))
                            .findFirst()
                            .orElseThrow(() -> new ServiceException("记录不存在"));

                    BeanUtils.copyProperties(addDTO, existingEntity);
                    existingEntity.setBadQty(addDTO.getDefectQty());
                    toUpdate.add(existingEntity);

                    // 处理更新的附件
                    if (hasDefectImage) {
                        List<AttachDTO> updateAttachDTOS = buildAttachDTOList(addDTO.getBadImageViewList());
                        attachmentService.batchSave(updateAttachDTOS, WmsConstant.QC_DEFECT, addDTO.getId());
                    }
                } else {
                    continue;
                }
            }
        }

        // 处理需要删除的记录 - 在exitList中但不在qcDefectList中的记录
        Set<String> submittedDefectIds = qcDefectList.stream()
                .map(QcDefectDTO.AddDTO::getId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());

        // 找出需要删除的记录
        for (QcDefectEntity entity : exitList) {
            if (!submittedDefectIds.contains(entity.getId())) {
                toDeleteIds.add(entity.getId());
            }
        }

        // 执行数据库操作
        if (!toUpdate.isEmpty()) {
            updateBatchById(toUpdate); // 更新缺陷记录
        }
        if (!toDeleteIds.isEmpty()) {
            attachmentService.batchRemoveAttachment(toDeleteIds);
            removeByIds(toDeleteIds); // 删除缺陷记录
        }
    }

    /**
     * 组装并去重附件，避免重复保存同一附件
     */
    private List<AttachDTO> buildAttachDTOList(List<QcDefectDTO.BadImageView> badImageViewList) {
        if (CollectionUtils.isEmpty(badImageViewList)) {
            return Collections.emptyList();
        }
        Map<String, AttachDTO> attachmentMap = new LinkedHashMap<>();
        for (QcDefectDTO.BadImageView badImageView : badImageViewList) {
            if (Objects.isNull(badImageView) || StringUtils.isBlank(badImageView.getAttachUrl())) {
                continue;
            }
            String dedupKey = badImageView.getAttachUrl() + "#" + StringUtils.defaultString(badImageView.getAttachName());
            if (attachmentMap.containsKey(dedupKey)) {
                continue;
            }
            AttachDTO attachDTO = new AttachDTO();
            attachDTO.setAttachUrl(badImageView.getAttachUrl());
            attachDTO.setAttachName(badImageView.getAttachName());
            attachmentMap.put(dedupKey, attachDTO);
        }
        return new ArrayList<>(attachmentMap.values());
    }


    @Override
    public List<QcDefectDTO.ViewDTO> getByMainId(String id) {
        List<QcDefectDTO.ViewDTO> addDTOS = new ArrayList<>();

        List<QcDefectEntity> list = this.lambdaQuery()
                .eq(QcDefectEntity::getMainId, id)
                .list();

        if (!list.isEmpty()) {
            for (QcDefectEntity qcDefectEntity : list) {
                QcDefectDTO.ViewDTO viewDTO = new QcDefectDTO.ViewDTO();
                List<QcDefectDTO.BadImageView> badImageViews = new ArrayList<>();
                BeanUtils.copyProperties(qcDefectEntity,viewDTO);
                viewDTO.setDefectQty(qcDefectEntity.getBadQty());
                viewDTO.setDefectLevelName(WmsDefectLevelEnum.getName(viewDTO.getDefectLevel()));
                List<WmsAttachmentEntity> attachments = attachmentService.getByBusinessId(qcDefectEntity.getId(), WmsConstant.QC_DEFECT);
                for (WmsAttachmentEntity attachment : attachments) {
                    QcDefectDTO.BadImageView badImageView = new QcDefectDTO.BadImageView();
                    badImageView.setAttachName(attachment.getAttachName());
                    badImageView.setAttachUrl(attachment.getAttachUrl());
                    badImageViews.add(badImageView);
                }
                viewDTO.setBadImageViewList(badImageViews);
                addDTOS.add(viewDTO);
            }
        }
        return addDTOS;
    }

    @Override
    public void removeByMainIds(List<String> mainIdList) {
        if (CollectionUtils.isNotEmpty(mainIdList)) {
            LambdaQueryWrapper<QcDefectEntity> defectQueryWrapper = new LambdaQueryWrapper<>();
            defectQueryWrapper.in(QcDefectEntity::getMainId, mainIdList);
            this.remove(defectQueryWrapper);

            LambdaQueryWrapper<QcDefectEntity> idQueryWrapper = new LambdaQueryWrapper<>();
            idQueryWrapper.select(QcDefectEntity::getId)
                    .in(QcDefectEntity::getMainId, mainIdList);
            List<QcDefectEntity> defectEntities = this.list(idQueryWrapper);

            List<String> defectIds = defectEntities.stream()
                    .map(QcDefectEntity::getId)
                    .collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(defectIds)) {
                // 删除图片
                LambdaQueryWrapper<WmsAttachmentEntity> attachmentQueryWrapper = new LambdaQueryWrapper<>();
                attachmentQueryWrapper.in(WmsAttachmentEntity::getBusinessId, defectIds);
                attachmentService.remove(attachmentQueryWrapper);
            }
        }
    }


}
