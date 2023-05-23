package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.SalesDemandDetailDTO;
import com.erp.model.scm.entity.SalesDemandDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.mapper.SalesDemandDetailMapper;
import com.erp.server.scm.service.ModuleOperateLogService;
import com.erp.server.scm.service.SalesDemandDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 销售需求明细表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Slf4j
@Service
public class SalesDemandDetailServiceImpl extends SuperServiceImpl<SalesDemandDetailMapper, SalesDemandDetailEntity> implements SalesDemandDetailService {

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SoInfoFeign soInfoFeign;

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(List<SalesDemandDetailDTO.AddDTO> details, String salesDemandId) {
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        List<SalesDemandDetailEntity> list = BeanMapperUtils.copyList(SalesDemandDetailEntity.class, details);


        //处理关联数据
        doOpHandleDataId(list,salesDemandId);
        this.saveBatch(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(List<SalesDemandDetailDTO.UpdateDTO> details,String salesDemandId) {
        if (details == null) {
            details = new ArrayList<>();
        }
        //原明细数据
        List<SalesDemandDetailEntity> oldList = this.listBySalesDemandId(salesDemandId);
        List<String> deleteIds = getDeleteIds(details, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<SalesDemandDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getSalesDemandId(), obj.getSkuNo())).collect(Collectors.toList());
            moduleOperateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.SALES_DEMAND.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }
        List<SalesDemandDetailEntity> newList = BeanMapperUtils.copyList(SalesDemandDetailEntity.class, details);
        //如果是下推单据则需要验证修改的数量
        checkPlanStockQty(newList);

        doOpHandleDataId(newList,salesDemandId);
        this.saveOrUpdateBatch(newList);
    }

    /**
     * @description: 验证下推数量
     * @author Will
     * @date: 2023/5/23 12:09
     * @param newList
     */
    private void checkPlanStockQty (List<SalesDemandDetailEntity> newList) {
        //来源明细ids
        List<String> sourceDetailIds = newList.stream().filter(obj -> StringUtils.isNotBlank(obj.getSourceDetailId())).map(SalesDemandDetailEntity::getSourceDetailId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(sourceDetailIds)) {
            return;
        }
        //来源单据明细id相同的其他备货申请单明细
        List<SalesDemandDetailEntity> salesDemandDetailList = this.listBySourceDetailIds(sourceDetailIds);
        //销售订单明细
        List<SoDetailEntity> soDetailList = soInfoFeign.listSoDetailByIds(sourceDetailIds);

        for (SalesDemandDetailEntity entity : newList) {
            //销售数量
            Integer qty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(soDetailList)) {
                qty = soDetailList.stream().filter(obj -> obj.getId().equals(entity.getSourceDetailId())).map(SoDetailEntity::getQty).findFirst().orElse(MathUtil.ZERO);

            }
            //已下推数量
            Integer refQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(salesDemandDetailList)) {
                refQty = salesDemandDetailList.stream().filter(obj -> !obj.getId().equals(entity.getId())).map(SalesDemandDetailEntity::getPlanStockQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            if (entity.getPlanStockQty().intValue() > qty.intValue() - refQty.intValue()) {
                throw new ServiceException(ApiError.ERROR_98064.code,String.format(ApiError.ERROR_98064.msg,entity.getSkuNo(),qty.intValue() - refQty.intValue()));
            }
        }
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<SalesDemandDetailDTO.UpdateDTO> newList, List<SalesDemandDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(SalesDemandDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(SalesDemandDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * 根据主表id查询
     */
    @Override
    public List<SalesDemandDetailEntity> listBySalesDemandId(String salesDemandId) {
      return  lambdaQuery().eq(SalesDemandDetailEntity::getSalesDemandId,salesDemandId).list();
    }


    @Override
    public void removeBySalesDemandIds(List<String> salesDemandIds) {
        lambdaUpdate().in(SalesDemandDetailEntity::getSalesDemandId,salesDemandIds).remove();
    }

    @Override
    public SalesDemandDetailEntity getBySalesDemandIdAndSkuId(String salesDemandId, String skuId) {
        return lambdaQuery().eq(SalesDemandDetailEntity::getSalesDemandId,salesDemandId).eq(SalesDemandDetailEntity::getSkuId,skuId).one();
    }

    @Override
    public List<SalesDemandDetailEntity> listBySourceDetailIds(List<String> sourceDetailIds) {
        return baseMapper.listBySourceDetailIds(sourceDetailIds);
    }

    /**
     * 处理明细中的数据id
     */
    private void doOpHandleDataId (List<SalesDemandDetailEntity> newList, String salesDemandId) {

        //仓库信息
        List<String> destWarehouseIdList = newList.stream().map(SalesDemandDetailEntity::getDestWarehouseId).collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(destWarehouseIdList);

        //产品信息
        List<String> skuIds = newList.stream().map(SalesDemandDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }

        //添加操作日志
        List<SalesDemandDetailEntity> addList = newList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(addList)) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(salesDemandId, obj.getSkuNo())).collect(Collectors.toList());
            moduleOperateLogService.batchAddModuleOperateLog("新增了一条SKU【%s】", ModuleTypeEnum.SALES_DEMAND.getCode(), addPairList, "编辑操作");
        }
        for (SalesDemandDetailEntity entity : newList) {
            entity.setSalesDemandId(salesDemandId);
            //仓库
            if (CollectionUtils.isNotEmpty(warehouseList)) {
                String warehouseName = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getDestWarehouseId())).map(WarehouseDTO.UpdateDTO::getName).findFirst().orElse(null);
                entity.setDestWarehouseName(warehouseName);
            }
            log.info("查询SKU【{}】信息",entity.getSkuNo());
            //产品信息
            SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(entity.getSkuId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(skuVO)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            entity.setProductName(skuVO.getSkuName());
            entity.setUnitQty(skuVO.getUnitQty());
            entity.setVariantProperty(skuVO.getVariantProperty());

            //修改操作日志
            if (StringUtils.isNotBlank(entity.getId())) {
                SalesDemandDetailEntity old = this.getById(entity.getId());
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_98002);
                }
                moduleOperateLogService.addModuleOperateLogByObj(old,entity, ModuleTypeEnum.SALES_DEMAND.getCode(),salesDemandId,"",String.format("【%s】",old.getSkuNo()));
            }
        }
    }

}
