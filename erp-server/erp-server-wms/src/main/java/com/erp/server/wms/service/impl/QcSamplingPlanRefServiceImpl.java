package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.QcNoticeDTO;
import com.erp.model.wms.dto.QcResultDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.QcTypeEnum;
import com.erp.server.wms.mapper.QcSamplingPlanRefMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import io.seata.common.util.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wtr
 * @since 2026-03-25
 */
@Slf4j
@Service
public class QcSamplingPlanRefServiceImpl extends SuperServiceImpl<QcSamplingPlanRefMapper, QcSamplingPlanRefEntity> implements QcSamplingPlanRefService {

    @Resource
    private QcStandardRefService qcStandardRefService;

    @Resource
    private QcStandardImageRefService qcStandardImageRefService;

    @Resource
    private QcResultService qcResultService;

    @Resource
    private OperateLogService operateLogService;

    @Override
    @Transactional
    public void add(String billId, QcNoticeDTO.QcStandardAddDTO qcStandardAddDTO) {
        QcSamplingPlanRefEntity qcSamplingPlanRefEntity = new QcSamplingPlanRefEntity();
        BeanUtils.copyProperties(qcStandardAddDTO, qcSamplingPlanRefEntity);
        qcSamplingPlanRefEntity.setMainId(billId);
        QcSamplingPlanRefEntity oldEntity = null;
        if (StringUtils.isNotBlank(qcStandardAddDTO.getId())) {
            // 查询旧数据
            oldEntity = this.getById(qcStandardAddDTO.getId());
            if (oldEntity == null) {
                throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE,"质检单");
            }
        }

        if (StringUtils.isBlank(qcStandardAddDTO.getId())) {
            // 新增逻辑
            this.save(qcSamplingPlanRefEntity);
            // 新增质检项目
            if (Objects.nonNull(qcStandardAddDTO.getQcInspectItemAddDTOList())
                    && !qcStandardAddDTO.getQcInspectItemAddDTOList().isEmpty()) {
                List<QcStandardRefEntity> qcStandardRefs = qcStandardAddDTO.getQcInspectItemAddDTOList().stream()
                        .map(item -> {
                            QcStandardRefEntity ref = new QcStandardRefEntity();
                            BeanUtils.copyProperties(item, ref);
                            ref.setMainId(qcSamplingPlanRefEntity.getId());
                            return ref;
                        })
                        .collect(Collectors.toList());
                qcStandardRefService.saveBatch(qcStandardRefs);
            }

            // 新增参考图片
            if (Objects.nonNull(qcStandardAddDTO.getQcImageAddDTOList())
                    && !qcStandardAddDTO.getQcImageAddDTOList().isEmpty()) {
                List<QcStandardImageRefEntity> qcStandardImageRefs = qcStandardAddDTO.getQcImageAddDTOList().stream()
                        .map(image -> {
                            QcStandardImageRefEntity ref = new QcStandardImageRefEntity();
                            BeanUtils.copyProperties(image, ref);
                            ref.setMainId(qcSamplingPlanRefEntity.getId());
                            return ref;
                        })
                        .collect(Collectors.toList());
                qcStandardImageRefService.saveBatch(qcStandardImageRefs);
            }
        } else {
            // 更新逻辑
            this.updateById(qcSamplingPlanRefEntity);

            // 删除旧的质检项目
            if (Objects.nonNull(qcStandardAddDTO.getQcInspectItemAddDTOList())
                    && !qcStandardAddDTO.getQcInspectItemAddDTOList().isEmpty()) {
                // 提取所有新质检项目的 ID
                List<String> newInspectItemIds = qcStandardAddDTO.getQcInspectItemAddDTOList().stream()
                        .map(QcNoticeDTO.QcInspectItemAddDTO::getId)
                        .filter(id -> StringUtils.isNotBlank(id))
                        .collect(Collectors.toList());


                // 查询旧质检项目（排除新质检项目的 ID）
                LambdaQueryWrapper<QcStandardRefEntity> inspectItemQueryWrapper = new LambdaQueryWrapper<>();
                inspectItemQueryWrapper.eq(QcStandardRefEntity::getMainId, qcStandardAddDTO.getId());
                if (!newInspectItemIds.isEmpty()) {
                    inspectItemQueryWrapper.notIn(QcStandardRefEntity::getId, newInspectItemIds);
                }
                List<QcStandardRefEntity> oldInspectItems = qcStandardRefService.list(inspectItemQueryWrapper);

                // 删除旧质检项目
                if (!oldInspectItems.isEmpty()) {
                    qcStandardRefService.removeByIds(oldInspectItems.stream()
                            .map(QcStandardRefEntity::getId)
                            .collect(Collectors.toList()));
                }
            } else {
                // 如果前端没有传质检项目，则删除所有旧质检项目
                qcStandardRefService.remove(new LambdaQueryWrapper<QcStandardRefEntity>()
                        .eq(QcStandardRefEntity::getMainId, qcStandardAddDTO.getId()));
            }

            // 删除旧的参考图片
            if (Objects.nonNull(qcStandardAddDTO.getQcImageAddDTOList()) &&
                    !qcStandardAddDTO.getQcImageAddDTOList().isEmpty()) {
                List<String> newImageIds = qcStandardAddDTO.getQcImageAddDTOList().stream()
                        .map(QcNoticeDTO.QcImageAddDTO::getId)
                        .filter(id -> StringUtils.isNotBlank(id))
                        .collect(Collectors.toList());

                // 查询旧图片（排除新图片的 ID）
                LambdaQueryWrapper<QcStandardImageRefEntity> imageQueryWrapper = new LambdaQueryWrapper<>();
                imageQueryWrapper.eq(QcStandardImageRefEntity::getMainId, qcStandardAddDTO.getId());
                if (!newImageIds.isEmpty()) {
                    imageQueryWrapper.notIn(QcStandardImageRefEntity::getId, newImageIds);
                }
                List<QcStandardImageRefEntity> oldImages = qcStandardImageRefService.list(imageQueryWrapper);

                // 删除旧图片
                if (!oldImages.isEmpty()) {
                    qcStandardImageRefService.removeByIds(oldImages.stream()
                            .map(QcStandardImageRefEntity::getId)
                            .collect(Collectors.toList()));
                }
            } else {
                // 如果前端没有传图片，则删除所有旧图片
                qcStandardImageRefService.remove(new LambdaQueryWrapper<QcStandardImageRefEntity>()
                        .eq(QcStandardImageRefEntity::getMainId, qcStandardAddDTO.getId()));
            }
        }

        // 新增质检项目
        if (Objects.nonNull(qcStandardAddDTO.getQcInspectItemAddDTOList())
                && !qcStandardAddDTO.getQcInspectItemAddDTOList().isEmpty()) {
            List<QcStandardRefEntity> qcStandardRefs = new ArrayList<>();
            for (QcNoticeDTO.QcInspectItemAddDTO qcInspectItemAddDTO : qcStandardAddDTO.getQcInspectItemAddDTOList()) {
                QcStandardRefEntity qcStandardRef = new QcStandardRefEntity();
                BeanUtils.copyProperties(qcInspectItemAddDTO, qcStandardRef);
                qcStandardRefs.add(qcStandardRef);
            }
            qcStandardRefService.saveBatch(qcStandardRefs);
        }

        // 新增参考图片
        if (Objects.nonNull(qcStandardAddDTO.getQcImageAddDTOList()) &&
                !qcStandardAddDTO.getQcImageAddDTOList().isEmpty()) {
            List<QcStandardImageRefEntity> qcStandardImageRefs = new ArrayList<>();
            for (QcNoticeDTO.QcImageAddDTO qcImageAddDTO : qcStandardAddDTO.getQcImageAddDTOList()) {
                QcStandardImageRefEntity qcStandardImageRefEntity = new QcStandardImageRefEntity();
                BeanUtils.copyProperties(qcImageAddDTO, qcStandardImageRefEntity);
                qcStandardImageRefs.add(qcStandardImageRefEntity);
            }
            qcStandardImageRefService.saveBatch(qcStandardImageRefs);
        }

        //操作日志
        operateLogService.addModuleOperateLogByObj(oldEntity, qcSamplingPlanRefEntity,
                ModuleTypeEnum.QC_ORDER.getCode(), billId, "", "编辑了质检单的质检信息");
    }

    @Override
    @Transactional
    public void removeByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return;
        }

        LambdaQueryWrapper<QcSamplingPlanRefEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(QcSamplingPlanRefEntity::getMainId, mainIds);
        List<QcSamplingPlanRefEntity> entities = this.list(queryWrapper);

        if (entities.isEmpty()) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "质检单");
        }

        List<String> ids = entities.stream()
                .map(QcSamplingPlanRefEntity::getId)
                .collect(Collectors.toList());

        this.removeByIds(ids);

        // 删除质检项目
        LambdaQueryWrapper<QcStandardRefEntity> standardQueryWrapper = new LambdaQueryWrapper<>();
        standardQueryWrapper.in(QcStandardRefEntity::getMainId, ids);
        List<QcStandardRefEntity> standardEntities = qcStandardRefService.list(standardQueryWrapper);
        if (!standardEntities.isEmpty()) {
            List<String> standardIds = standardEntities.stream()
                    .map(QcStandardRefEntity::getId)
                    .collect(Collectors.toList());
            qcStandardRefService.removeByIds(standardIds);
        }

        // 删除参考图片
        LambdaQueryWrapper<QcStandardImageRefEntity> imageQueryWrapper = new LambdaQueryWrapper<>();
        imageQueryWrapper.in(QcStandardImageRefEntity::getMainId, ids);
        List<QcStandardImageRefEntity> imageEntities = qcStandardImageRefService.list(imageQueryWrapper);
        if (!imageEntities.isEmpty()) {
            List<String> imageIds = imageEntities.stream()
                    .map(QcStandardImageRefEntity::getId)
                    .collect(Collectors.toList());
            qcStandardImageRefService.removeByIds(imageIds);
        }
    }

    @Override
    public QcNoticeDTO.QcStandardView getByMainId(String id) {
        QcNoticeDTO.QcStandardView qcStandardView = new QcNoticeDTO.QcStandardView();
        QcSamplingPlanRefEntity qcSamplingPlanRef = this.lambdaQuery()
                .eq(QcSamplingPlanRefEntity::getMainId, id)
                .last(" limit 1 ")
                .one();
        if (Objects.nonNull(qcSamplingPlanRef)) {
            BeanUtils.copyProperties(qcSamplingPlanRef,qcStandardView);
            QcResultDTO.ViewDTO qcResult = qcResultService.getByMainId(id);
            qcStandardView.setSamplingPlanName(QcTypeEnum.getByCode(qcResult.getQcType()) + "抽样方案");
            if (Objects.nonNull(qcSamplingPlanRef)) {
                List<QcStandardRefEntity> standradList = qcStandardRefService.lambdaQuery()
                        .eq(QcStandardRefEntity::getMainId, qcSamplingPlanRef.getId())
                        .list();

                if (!standradList.isEmpty()) {
                    List<QcNoticeDTO.QcInspectItemView> qcInspectItemViews = BeanMapper.copyList(standradList, QcNoticeDTO.QcInspectItemView.class);
                    qcStandardView.setQcInspectItemViewDTOList(qcInspectItemViews);
                }
                List<QcStandardImageRefEntity> imageList = qcStandardImageRefService.lambdaQuery()
                        .eq(QcStandardImageRefEntity::getMainId, qcSamplingPlanRef.getId())
                        .list();
                if (!imageList.isEmpty()) {
                    List<QcNoticeDTO.QcImageView> qcImageViews = BeanMapper.copyList(imageList, QcNoticeDTO.QcImageView.class);
                    qcStandardView.setQcImageViewDTOList(qcImageViews);
                }
            }
            return qcStandardView;
        }
        return qcStandardView;
    }
}
