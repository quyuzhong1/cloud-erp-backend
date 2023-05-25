package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.TransferInfoDetailDTO;
import com.erp.model.wms.entity.PickingDetailEntity;
import com.erp.model.wms.entity.TransferInfoDetailEntity;
import com.erp.model.wms.entity.TransferInfoEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.TransferInfoDetailMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.PickingDetailService;
import com.erp.server.wms.service.TransferInfoDetailService;
import com.erp.server.wms.service.TransferInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 直接调拨单明细表
 *
 * @author will
 * @since 2023-05-10
 */
@Service
public class TransferInfoDetailServiceImpl extends SuperServiceImpl<TransferInfoDetailMapper, TransferInfoDetailEntity> implements TransferInfoDetailService {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private TransferInfoService transferInfoService;

    @Resource
    private PickingDetailService pickingDetailService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(List<TransferInfoDetailDTO.AddDTO> detailList, String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<TransferInfoDetailEntity> list = BeanMapperUtils.copyList(TransferInfoDetailEntity.class, detailList);

        //处理明细数据
        doOpHandleDetails(list,mainId,Boolean.FALSE);

        this.saveBatch(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(List<TransferInfoDetailDTO.UpdateDTO> detailList, String mainId) {
        if (detailList == null) {
            detailList = new ArrayList<>();
        }
        //原明细数据
        List<TransferInfoDetailEntity> oldList = this.listByMainId(mainId);
        List<String> deleteIds = getDeleteIds(detailList, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<TransferInfoDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.TRANSFER_INFO.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }
        List<TransferInfoDetailEntity> newList = BeanMapperUtils.copyList(TransferInfoDetailEntity.class, detailList);

        //验证上级单据数量
        checkTransferInfoQty(newList,mainId);

        //处理明细id及操作日志
        doOpHandleDetails(newList,mainId,Boolean.TRUE);

        //新增或修改明细
        this.saveOrUpdateBatch(newList);
    }

    @Override
    public void removeByMainIds(List<String> mainIds) {
        lambdaUpdate().in(TransferInfoDetailEntity::getMainId,mainIds).remove();
    }

    @Override
    public List<TransferInfoDetailEntity> listByMainId(String mainId) {
        return lambdaQuery()
                .eq(TransferInfoDetailEntity::getMainId,mainId)
                .orderByDesc(TransferInfoDetailEntity::getId)
                .list();
    }

    @Override
    public List<TransferInfoDetailEntity> listByMainIds(List<String> mainIds) {
        return lambdaQuery().in(TransferInfoDetailEntity::getMainId,mainIds).list();
    }

    @Override
    public List<TransferInfoDetailEntity> listSourceDetailIds(List<String> sourceDetailIds) {
        return baseMapper.listSourceDetailIds(sourceDetailIds);
    }

    /**
     * @description: 修改时数量验证
     * @author Will
     * @date: 2023/5/25 10:28
     * @param newList
     * @param mainId
     */
    private void checkTransferInfoQty (List<TransferInfoDetailEntity> newList ,String mainId) {
        TransferInfoEntity transferInfoEntity = transferInfoService.getById(mainId);
        if (ObjectUtils.isEmpty(transferInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_99047);
        }

        if (SourceTypeEnum.SELF_ADD.getCode().equals(transferInfoEntity.getSourceType())) {
            return;
        }

        List<String> sourceDetailIds = newList.stream().map(TransferInfoDetailEntity::getSourceDetailId).collect(Collectors.toList());

        //拣货明细
        List<PickingDetailEntity> pickingDetailList = pickingDetailService.listByIds(sourceDetailIds);

        //已下推明细
        List<TransferInfoDetailEntity> transferInfoDetailList = this.listSourceDetailIds(sourceDetailIds);

        for (TransferInfoDetailEntity detailEntity : newList) {
            //拣货数量
            Integer pickingQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(pickingDetailList)) {
                pickingQty = pickingDetailList.stream().filter(obj -> obj.getId().equals(detailEntity.getSourceDetailId()))
                        .map(PickingDetailEntity::getQty).findFirst().orElse(MathUtil.ZERO);
            }
            //已下推数量（不包括本明细数量）
            Integer hasPickingQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(transferInfoDetailList)) {
                hasPickingQty = transferInfoDetailList.stream().filter(obj -> obj.getSourceDetailId().equals(detailEntity.getSourceDetailId()) && !obj.getId().equals(detailEntity.getId()))
                        .map(TransferInfoDetailEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            //数量检验
            if (detailEntity.getQty().intValue() > pickingQty.intValue() - hasPickingQty.intValue()) {
                throw new ServiceException(ApiError.ERROR_99051.code, String.format(ApiError.ERROR_99051.msg, detailEntity.getSkuNo()));
            }
        }

    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<TransferInfoDetailDTO.UpdateDTO> newList, List<TransferInfoDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(TransferInfoDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(TransferInfoDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }
    /**
     * 处理明细中的数据id
     */
    private void doOpHandleDetails (List<TransferInfoDetailEntity> newList, String mainId, Boolean isUpdate) {

        //需要新增的数据
        List<TransferInfoDetailEntity> addList = newList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());

        //需要修改的数据
        List<String> ids = newList.stream().filter(obj -> StringUtils.isNotBlank(obj.getId())).map(TransferInfoDetailEntity::getId).collect(Collectors.toList());
        List<TransferInfoDetailEntity> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            list = this.listByIds(ids);
        }

        //SKU信息
        List<String> skuIds = newList.stream().map(TransferInfoDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        for (TransferInfoDetailEntity detail:newList) {
            //单位
            String unit = skuList.stream().filter(obj -> obj.getSkuId().equals(detail.getSkuId()) && StringUtils.isNotBlank(obj.getUnitName())).map(SkuVO::getUnitName).findFirst().orElse("");
            detail.setUnit(unit);
            detail.setMainId(mainId);
            //修改操作日志
            if (StringUtils.isNotBlank(detail.getId())) {
                if (CollectionUtils.isEmpty(list)) {
                    throw new ServiceException(ApiError.ERROR_99048);
                }
                TransferInfoDetailEntity old = list.stream().filter(obj -> obj.getId().equals(detail.getId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_99048);
                }
                operateLogService.addModuleOperateLogByObj(old,detail, ModuleTypeEnum.TRANSFER_INFO.getCode(),mainId,"",String.format("【%s】",old.getSkuNo()));
            }
        }

        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList) && isUpdate) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.TRANSFER_INFO.getCode(), addPairList, "编辑操作");
        }
    }
}
