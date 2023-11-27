package com.erp.server.wms.service.impl;


import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.OverseasDeliveryPlanDTO;
import com.erp.model.wms.entity.OverseasDeliveryPlanDetailEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.OverseasDeliveryPlanDetailMapper;
import com.erp.server.wms.service.OverseasDeliveryPlanDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.OverseasDeliveryPlanDetailDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 发货计划详情表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@Service
public class OverseasDeliveryPlanDetailServiceImpl extends SuperServiceImpl<OverseasDeliveryPlanDetailMapper, OverseasDeliveryPlanDetailEntity> implements OverseasDeliveryPlanDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(OverseasDeliveryPlanDTO.AddDTO addDTO, String mainId) {
        List<OverseasDeliveryPlanDetailEntity> list = BeanMapper.copyList(addDTO.getDetailList(), OverseasDeliveryPlanDetailEntity.class);

        // 数据处理
        handleData(list, mainId, Boolean.FALSE);

        log.info("开始新增发货计划详情单");
        boolean save = super.saveBatch(list);
        if(!save) {
            throw new ServiceException("发货计划详情单保存失败");
        }

    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(OverseasDeliveryPlanDTO.UpdateDTO updateDTO, String mainId) {
        List<OverseasDeliveryPlanDetailDTO.UpdateDTO> detailList = updateDTO.getDetailList();
        //原明细数据
        List<OverseasDeliveryPlanDetailEntity> oldList = this.listByMainIds(Arrays.asList(mainId));
        List<String> deleteIds = getDeleteIds(detailList, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<OverseasDeliveryPlanDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.OVERSEAS_DELIVERY_PLAN.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }

        //映射字段
        List<OverseasDeliveryPlanDetailEntity> list = BeanMapperUtils.copyList(OverseasDeliveryPlanDetailEntity.class, detailList);

        // 数据处理
        handleData(list, mainId, Boolean.TRUE);

        log.info("开始修改发货计划详情单");
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("修改发货计划详情失败");
        }
    }

    @Override
    public List<OverseasDeliveryPlanDetailEntity> listByMainIds(List<String> mainIds) {
        return lambdaQuery().in(OverseasDeliveryPlanDetailEntity::getMainId, mainIds).list();
    }

    @Override
    public Boolean removeByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Boolean.FALSE;
        }
        return lambdaUpdate().in(OverseasDeliveryPlanDetailEntity::getMainId,mainIds).remove();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<OverseasDeliveryPlanDetailEntity> list, String mainId, Boolean isUpdate) {
        //需要新增的数据
        List<OverseasDeliveryPlanDetailEntity> addList = list.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());

        //根据skuId查询拥有的子sku
        List<String> skuIds = list.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIds);

        //查询skuId产品信息
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIds);

        //设置详情字段
        for (OverseasDeliveryPlanDetailEntity detailEntity : list) {
            detailEntity.setMainId(mainId);
            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(detailEntity.getSkuId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                detailEntity.setIsCombination(Boolean.TRUE);
            } else {
                detailEntity.setIsCombination(Boolean.FALSE);
            }
            //设置产品编号
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(new SkuVO());
            detailEntity.setSkuNo(skuVO.getSkuNo());

            //校验是否是修改，如果是就新增修改日志
            if (StringUtils.isNotBlank(detailEntity.getId())) {
                OverseasDeliveryPlanDetailEntity old = list.stream().filter(obj -> obj.getId().equals(detailEntity.getId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_NOT_OVERSEAS_DELIVERY_PLAN);
                }
                operateLogService.addModuleOperateLogByObj(old, detailEntity, ModuleTypeEnum.OVERSEAS_DELIVERY_PLAN.getCode(), detailEntity.getId(),"", String.format("【%s】", old.getSkuNo()));
            }
        }

        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList) && isUpdate) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.OVERSEAS_DELIVERY_PLAN.getCode(), addPairList, "编辑操作");
        }
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<OverseasDeliveryPlanDetailDTO.UpdateDTO> newList, List<OverseasDeliveryPlanDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(OverseasDeliveryPlanDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(OverseasDeliveryPlanDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

}
