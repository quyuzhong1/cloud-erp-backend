package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.MathUtil;
import com.erp.model.wms.dto.QcResultDTO;
import com.erp.model.wms.dto.WmsAttachmentDTO;
import com.erp.model.wms.entity.DictBasicEntity;
import com.erp.model.wms.entity.QcResultEntity;
import com.erp.model.wms.enums.QcResultEnum;
import com.erp.model.wms.enums.QcTypeEnum;
import com.erp.server.wms.constant.WmsConstant;
import com.erp.server.wms.mapper.QcResultMapper;
import com.erp.server.wms.service.DictBasicService;
import com.erp.server.wms.service.QcResultService;
import com.erp.server.wms.service.WmsAttachmentService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
@Service
public class QcResultServiceImpl extends SuperServiceImpl<QcResultMapper, QcResultEntity> implements QcResultService {

    @Resource
    private WmsAttachmentService wmsAttachmentService;

    @Resource
    private DictBasicService dictBasicService;

    /**
     * 质检信息 暂存
     *
     * @param billId
     * @param qcInfo
     * @return void
     * @author yl
     * @date 2023-04-19 10:11
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(String billId, QcResultDTO.AddDTO qcInfo) {
        QcResultEntity qcResultEntity = new QcResultEntity();
        String id = qcInfo.getId();
        if (StringUtils.isBlank(id)) {
            id = IdWorker.getIdStr();
        }

        String qcType = qcInfo.getQcType();
        //是否内检
        Boolean isInside = QcTypeEnum.getIsInsideByCode(qcType);
        BeanMapper.copy(qcInfo, qcResultEntity);

        //计算比率
        calculateRatio(qcResultEntity);
        qcResultEntity.setMainId(billId);
        qcResultEntity.setId(id);
        qcResultEntity.setIsInside(isInside);
        //
        List<String> imageNameList = qcInfo.getBadImageNameList();
        List<String> imageUrlList = qcInfo.getBadImageUrlList();
        wmsAttachmentService.batchSave(imageUrlList, imageNameList, WmsConstant.BAD, id);
        this.saveOrUpdate(qcResultEntity);

    }


    /**
     * 计算比率
     *
     * @param qcResultEntity
     * @return void
     * @author yl
     * @date 2023-04-19 14:39
     */
    private void calculateRatio(QcResultEntity qcResultEntity) {
        if (qcResultEntity != null) {
            //总数量
            Integer totalQty = qcResultEntity.getTotalQty() != null ? qcResultEntity.getTotalQty() : 0;
            //质检量
            Integer qcQty = qcResultEntity.getQcQty() != null ? qcResultEntity.getQcQty() : 0;
            if (totalQty != 0) {
                BigDecimal qcSampleRate = MathUtil.divide(new BigDecimal(qcQty), new BigDecimal(totalQty));
                qcResultEntity.setQcSampleRate(qcSampleRate);
            }
            //质检合格量
            Integer qcGoodQty = qcResultEntity.getQcGoodQty() != null ? qcResultEntity.getQcGoodQty() : 0;
            //质检不良量
            Integer qcBadQty = qcResultEntity.getQcBadQty() != null ? qcResultEntity.getQcBadQty() : 0;
            if (qcQty != 0) {
                BigDecimal qcGoodRate = MathUtil.divide(new BigDecimal(qcGoodQty), new BigDecimal(qcQty));
                qcResultEntity.setQcGoodRate(qcGoodRate);
                BigDecimal qcBadRate = MathUtil.divide(new BigDecimal(qcBadQty), new BigDecimal(qcQty));
                qcResultEntity.setQcBadRate(qcBadRate);
            }
        }
    }

    /**
     * 获取到质检信息
     *
     * @param billId
     * @return com.erp.model.wms.dto.QcInfoDTO.ViewDTO
     * @author yl
     * @date 2023-04-19 12:24
     */
    @Override
    public QcResultDTO.ViewDTO getByMainId(String billId) {
        QcResultDTO.ViewDTO qcInfoView = new QcResultDTO.ViewDTO();
        QcResultEntity qcInfo = this.getByBillId(billId);
        if (qcInfo != null) {
            BeanMapper.copy(qcInfo, qcInfoView);
            List<WmsAttachmentDTO.UpdateDTO> attachmentList = wmsAttachmentService.getByBusinessIds(Arrays.asList(qcInfo.getId()));
            List<String> imageUrlList = attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
            List<String> nameList = attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList());
            qcInfoView.setBadImageNameList(nameList);
            qcInfoView.setBadImageUrlList(imageUrlList);
            String qcType = qcInfoView.getQcType();
            //不良率
            BigDecimal badRate = qcInfoView.getQcBadRate();
            qcInfoView.setQcBadRate(MathUtil.BigDecimal_100.multiply(badRate));
            BigDecimal goodRate = qcInfoView.getQcGoodRate();
            qcInfoView.setQcGoodRate(MathUtil.BigDecimal_100.multiply(goodRate));

            BigDecimal sampleRate = qcInfoView.getQcSampleRate();
            qcInfoView.setQcSampleRate(MathUtil.BigDecimal_100.multiply(sampleRate));
            qcInfoView.setQcTypeName(QcTypeEnum.getByCode(qcType));
            List<DictBasicEntity> dictList = dictBasicService.getByKeyList(new ArrayList<>());
            //处理措施
            String handleModeDict = qcInfoView.getHandleModeDict();
            String handleModeName = dictList.stream().filter(d -> d.getValue().equals(handleModeDict)).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            qcInfoView.setHandleModeName(handleModeName);

            //处理措施
            String qcProblemDict = qcInfoView.getQcProblemDict();
            String qcProblemName = dictList.stream().filter(d -> d.getValue().equals(qcProblemDict)).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            qcInfoView.setQcProblemName(qcProblemName);
            String qcResult = qcInfoView.getQcResult();
            qcInfoView.setQcResultName(QcResultEnum.getByCode(qcResult));

        }
        return qcInfoView;
    }

    /**
     * 根据采购订单id集合 获取到已质检的数量
     *
     * @param purOrderIds
     * @return java.util.List<com.erp.model.wms.dto.QcInfoDTO.QcQtyDTO>
     * @author yl
     * @date 2023-04-20 12:59
     */
    @Override
    public List<QcResultDTO.QcQtyDTO> getPurOrderIds(List<String> purOrderIds) {
        if (CollectionUtils.isEmpty(purOrderIds)) {
            return Collections.emptyList();
        }
        return baseMapper.getByPurOrderIds(purOrderIds);
    }

    @Override
    public List<QcResultEntity> getByMainIdList(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            return this.lambdaQuery().in(QcResultEntity::getMainId, ids).list();
        }
        return Collections.emptyList();
    }


    /**
     * 批量免检后 批量去更新 数量
     *
     * @param ids
     * @return void
     * @author yl
     * @date 2023-04-20 17:07
     */
    @Override
    public void updateQcQty(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            LambdaUpdateWrapper<QcResultEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(QcResultEntity::getQcBadQty, 0);
            updateWrapper.set(QcResultEntity::getQcGoodQty, 0);
            updateWrapper.set(QcResultEntity::getQcQty, 0);
            updateWrapper.eq(QcResultEntity::getMainId, ids);
            this.update(updateWrapper);
        }

    }


    /**
     * 更新处理措施
     *
     * @param mainIds
     * @param handleModeDict
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-20 19:17
     */
    @Override
    public Boolean updateHandleMode(List<String> mainIds, String handleModeDict) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return false;
        }
        LambdaUpdateWrapper<QcResultEntity> updateWrapper = new LambdaUpdateWrapper();
        updateWrapper.in(QcResultEntity::getMainId, mainIds);
        updateWrapper.set(QcResultEntity::getHandleModeDict, handleModeDict);
        return this.update(updateWrapper);
    }


    /**
     * 根据质检单id集合 获取到一些需要入库的数据
     *
     * @param mainIdList
     * @return java.util.List<com.erp.model.wms.dto.QcInfoDTO.StockInDTO>
     * @author yl
     * @date 2023-04-24 15:47
     */
    @Override
    public List<QcResultDTO.StockInDTO> getStockIn(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        return baseMapper.getStockIn(mainIdList);
    }


    /**
     * 根据质检单id 获取到质检信息
     *
     * @param billId
     * @return com.erp.model.wms.entity.QcInfoEntity
     * @author yl
     * @date 2023-04-19 12:26
     */
    private QcResultEntity getByBillId(String billId) {
        LambdaQueryWrapper<QcResultEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QcResultEntity::getMainId, billId);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);

    }
}
