package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.FbaDeliveryDTO;
import com.erp.model.wms.dto.FbaShipmentDTO;
import com.erp.model.wms.dto.TransferApplicationDetailDTO;
import com.erp.model.wms.entity.FbaDeliveryDetailEntity;
import com.erp.model.wms.entity.FbaDeliveryLogisticsEntity;
import com.erp.model.wms.entity.TransferApplicationDetailEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.FbaDeliveryDetailMapper;
import com.erp.server.wms.service.FbaDeliveryDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import feign.Feign;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.FbaDeliveryDetailDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * FBA发货单明细表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
@Slf4j
@Service
public class FbaDeliveryDetailServiceImpl extends SuperServiceImpl<FbaDeliveryDetailMapper, FbaDeliveryDetailEntity> implements FbaDeliveryDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private ShopInfoFeign shopInfoFeign;
    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(FbaDeliveryDTO.AddDTO addDTO, String mainId) {
        List<FbaDeliveryDetailDTO.AddDTO> detailList = addDTO.getDetailList();
        //映射字段
        List<FbaDeliveryDetailEntity> list = BeanMapperUtils.copyList(FbaDeliveryDetailEntity.class, detailList);
        //处理明细数据
        handleData(list, mainId, Boolean.FALSE);
        //批量新增
        this.saveBatch(list);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(FbaDeliveryDTO.UpdateDTO updateDTO, String mainId) {
        FbaDeliveryDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "FBA发货单明细单"));
        List<FbaDeliveryDetailDTO.UpdateDTO> detailList = updateDTO.getDetailList();
        //原明细数据
        List<FbaDeliveryDetailEntity> oldList = this.listByMainId(mainId);
        List<String> deleteIds = getDeleteIds(detailList, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<FbaDeliveryDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.FBA_DELIVERY.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }
        //映射字段
        List<FbaDeliveryDetailEntity> list = BeanMapperUtils.copyList(FbaDeliveryDetailEntity.class, detailList);
        //处理明细数据
        handleData(list, mainId, Boolean.FALSE);

        //新增或修改明细
        this.saveOrUpdateBatch(list);
    }

    @Override
    public Boolean removeByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Boolean.FALSE;
        }
        return lambdaUpdate().in(FbaDeliveryDetailEntity::getMainId,mainIds).remove();
    }

    @Override
    public List<FbaDeliveryDetailEntity> listBySourceDetailIds(List<String> detailIds) {
        if (CollectionUtils.isEmpty(detailIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(FbaDeliveryDetailEntity::getSourceDetailId, detailIds).list();
    }

    @Override
    public List<FbaDeliveryDetailEntity> listByMainId(String mainId) {
        return lambdaQuery().eq(FbaDeliveryDetailEntity::getMainId, mainId).list();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<FbaDeliveryDetailEntity> list, String mainId, Boolean isUpdate) {
        //需要新增的数据
        List<FbaDeliveryDetailEntity> addList = list.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());

        //获取sku信息
        List<String> skuNoList = list.stream().map(FbaDeliveryDetailEntity::getSkuNo).collect(Collectors.toList());
        //产品名称
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNoList);
        if (CollectionUtils.isEmpty(skuVOList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        for (FbaDeliveryDetailEntity fbaDeliveryDetailEntity : list) {
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuNo().equals(fbaDeliveryDetailEntity.getSkuNo())).findFirst().orElse(new SkuVO());
            fbaDeliveryDetailEntity.setProductName(skuVO.getSkuName());
            fbaDeliveryDetailEntity.setWarehouseLocation(fbaDeliveryDetailEntity.getWarehouseLocation());
            //校验是否是修改，如果是就新增修改日志
            if (StringUtils.isNotBlank(fbaDeliveryDetailEntity.getId())) {
                FbaDeliveryDetailEntity old = list.stream().filter(obj -> obj.getId().equals(fbaDeliveryDetailEntity.getId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_NOT_FBA_DELIVERY_DETAIL);
                }
                operateLogService.addModuleOperateLogByObj(old,fbaDeliveryDetailEntity, ModuleTypeEnum.FBA_DELIVERY.getCode(),mainId,"",String.format("【%s】",old.getSkuNo()));
            }
        }
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList) && isUpdate) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.FBA_DELIVERY.getCode(), addPairList, "编辑操作");
        }
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<FbaDeliveryDetailDTO.UpdateDTO> newList, List<FbaDeliveryDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(FbaDeliveryDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(FbaDeliveryDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }
}
