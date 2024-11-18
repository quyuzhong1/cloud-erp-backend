package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.constant.CfgApiAuthContant;
import com.erp.model.dmp.dto.CfgApiAuthDTO;
import com.erp.model.dmp.entity.CfgApiAuthEntity;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.PoInstockDetailDTO;
import com.erp.model.wms.entity.*;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.server.wms.mapper.PoInstockDetailMapper;
import com.erp.server.wms.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 采购入库明细表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-04-10
 */
@Service
public class PoInstockDetailServiceImpl extends SuperServiceImpl<PoInstockDetailMapper, PoInstockDetailEntity> implements PoInstockDetailService {

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private WarehouseReceiveDetailService warehouseReceiveDetailService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private PoInstockService poInstockService;

    @Resource
    private PoReturnDetailService poReturnDetailService;

    @Resource
    private WarehouseService warehouseService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(List<PoInstockDetailDTO.AddDTO> details, String mainId, String sourceType,Boolean isNotCheck) {
        if (CollectionUtils.isEmpty(details)) {
            return;
        }

        List<PoInstockDetailEntity> list = BeanMapperUtils.copyList(PoInstockDetailEntity.class, details);

        //处理明细数据
        doOpHandleDetails(list,mainId,Boolean.FALSE);

        //验证关联数量
        checkStockInQty(list,sourceType,mainId,isNotCheck);


        this.saveBatch(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(List<PoInstockDetailDTO.UpdateDTO> details, String mainId, String sourceType) {
        if (details == null) {
            details = new ArrayList<>();
        }
        //原明细数据
        List<PoInstockDetailEntity> oldList = this.listByMainId(mainId);
        List<String> deleteIds = getDeleteIds(details, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<PoInstockDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.PO_INSTOCK.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }
        List<PoInstockDetailEntity> newList = BeanMapperUtils.copyList(PoInstockDetailEntity.class, details);

        //处理明细id及操作日志
        doOpHandleDetails(newList,mainId,Boolean.TRUE);

        //验证关联数量
        checkStockInQty(newList,sourceType,mainId,Boolean.FALSE);

        //新增或修改明细
        this.saveOrUpdateBatch(newList);
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<PoInstockDetailDTO.UpdateDTO> newList, List<PoInstockDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> CharSequenceUtil.isNotBlank(g.getId())).
                map(PoInstockDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(PoInstockDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    @Override
    public void removeByMainIds(List<String> mainIds) {
        lambdaUpdate().in(PoInstockDetailEntity::getMainId,mainIds).remove();
    }

    @Override
    public List<PoInstockDetailEntity> listByMainId(String mainId) {
        return lambdaQuery().eq(PoInstockDetailEntity::getMainId,mainId).list();
    }

    @Override
    public List<PoInstockDetailEntity> listByMainIds(List<String> mainIds) {
        return lambdaQuery().in(PoInstockDetailEntity::getMainId,mainIds).list();
    }

    @Override
    public List<PoInstockDetailEntity> listDetailBySourceDetailIds(List<String> sourceDetailIds) {
        return baseMapper.listDetailBySourceDetailIds(sourceDetailIds);
    }


    @Override
    public  List<PoInstockDetailEntity> listDetailByPodIds(List<String> podIds) {
        if(CollectionUtils.isEmpty(podIds)){
            return new ArrayList<>();
        }
        return baseMapper.listDetailByPodIds(podIds);
    }

    /**
     * 处理明细中的数据id
     */
    private void doOpHandleDetails (List<PoInstockDetailEntity> newList, String mainId, Boolean isUpdate) {

        List<PoInstockDetailEntity> addList = newList.stream().filter(c -> CharSequenceUtil.isBlank(c.getId())).collect(Collectors.toList());
        //采购明细信息
        List<String> podIds = newList.stream().map(PoInstockDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = scmTaskFeign.listPurchaseOrderDetailById(podIds);
        if (CollectionUtils.isEmpty(purchaseOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        for (PoInstockDetailEntity entity : newList) {
            PurchaseOrderDetailEntity detailEntity = purchaseOrderDetailList.stream().filter(obj -> obj.getId().equals(entity.getPurchaseOrderDetailId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(detailEntity)) {
                throw new ServiceException(ApiError.ERROR_98026);
            }
            entity.setSkuId(detailEntity.getSkuId());
            entity.setSkuNo(detailEntity.getSkuNo());
            entity.setPurchaseQty(detailEntity.getPurchaseQty());
            entity.setVariantProperty(detailEntity.getVariantProperty());
            entity.setMainId(mainId);
            entity.setTaxPrice(detailEntity.getTaxPrice());
            entity.setTaxRate(detailEntity.getTaxRate());
            entity.setCurrency(detailEntity.getCurrency());
            entity.setCurrencySymbol(detailEntity.getCurrencySymbol());
            //修改操作日志
            if (CharSequenceUtil.isNotBlank(entity.getId())) {
                PoInstockDetailEntity old = this.getById(entity.getId());
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_98002);
                }
                operateLogService.addModuleOperateLogByObj(old,entity, ModuleTypeEnum.PO_INSTOCK.getCode(),mainId,"",String.format("【%s】",old.getSkuNo()));
            }
        }
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList) && isUpdate) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.PO_INSTOCK.getCode(), addPairList, "编辑操作");
        }
    }

    /**
     * @description: 验证数量(审核通过的时候校验)
     * @author Will
     * @date: 2023/4/17 16:04
     * @param list
     * @param sourceType
     */
    private void checkStockInQty (List<PoInstockDetailEntity> list , String sourceType, String mainId,Boolean isNotCheck) {
        if (CollectionUtils.isEmpty(list) || isNotCheck) {
            return;
        }
        //来源ids
        List<String> ids = list.stream().map(PoInstockDetailEntity::getSourceDetailId).collect(Collectors.toList());
        //采购订单明细ids
        List<String> podIds = list.stream().map(PoInstockDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());


        PoInstockEntity entity = poInstockService.getById(mainId);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98050);
        }
       
        //采购订单
        List<PurchaseOrderDetailEntity> details = scmTaskFeign.listPurchaseOrderDetailById(podIds);
        if (CollectionUtils.isEmpty(details)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }

        //下推单据明细id查询
        List<PoInstockDetailEntity> stockInDetails = this.listDetailByPodIds(podIds);

        //查收货单明细
        List<WarehouseReceiveDetailEntity> receiveDetails = warehouseReceiveDetailService.listByIds(ids);
        if (CollectionUtils.isEmpty(details)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        //查询退货明细
        List<PoReturnDetailEntity> returnOrderDetailList = poReturnDetailService.listReturnOrderDetailByPodIds(podIds);

        //查询仓库
        WarehouseEntity warehouseEntity = warehouseService.getById(entity.getDeliveryWarehouseId());
        if (ObjectUtils.isEmpty(warehouseEntity)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        //仓位必填验证
        checkWarehouseLocation(warehouseEntity,list);

        for (PoInstockDetailEntity detailEntity : list) {

            //采购订单数量
            Integer purchaseQty = details.stream().filter(obj -> obj.getId().equals(detailEntity.getPurchaseOrderDetailId())).map(obj -> obj.getPurchaseQty()).findFirst().orElse(MathUtil.ZERO);

            //本次入库数量
            Integer thisStockInQty = detailEntity.getStockInQty();

            //下推入库单数量
            Integer stockInQty = MathUtil.ZERO;

            if (CollectionUtils.isNotEmpty(stockInDetails)) {
                stockInQty = stockInDetails.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(detailEntity.getPurchaseOrderDetailId()) && !obj.getId().equals(detailEntity.getId())).map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
            }

            //退货单补货数量
            Integer returnQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(returnOrderDetailList)) {
                returnQty = returnOrderDetailList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(detailEntity.getPurchaseOrderDetailId()) && ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).map(PoReturnDetailEntity::getReplenishQty).reduce(MathUtil.ZERO, Integer::sum);
            }


            if (thisStockInQty > purchaseQty - stockInQty + returnQty) {
                throw new ServiceException(new ApiResult(MathUtil.ONE,String.format("SKU【%s】入库数量不能大于",detailEntity.getSkuNo()) + (purchaseQty - stockInQty + returnQty)));
            }
            //来源收货单
            if (SourceTypeEnum.PO_RECEIVE.getCode().equals(sourceType)) {
                //收货数量
                Integer receiveQty = receiveDetails.stream().filter(obj -> obj.getId().equals(detailEntity.getSourceDetailId())).map(WarehouseReceiveDetailEntity::getReceiveQty).findFirst().orElse(MathUtil.ZERO);

                //数量验证
                if (thisStockInQty > receiveQty) {
                    throw new ServiceException(new ApiResult(1,String.format("SKU【%s】入库数量不能大于",detailEntity.getSkuNo()) + receiveQty));
                }
            }
        }
    }

    /**
     * @description: 仓位必填验证
     * @author Will
     * @date: 2023/12/19 15:19
     * @param warehouseEntity
     * @param list
     */
    private void checkWarehouseLocation (WarehouseEntity warehouseEntity,List<PoInstockDetailEntity> list) {
        //仓库配置
        CfgApiAuthEntity cfgApiAuthEntity = dmpTaskFeign.getByKey(new CfgApiAuthDTO.FeignDTO(CfgApiAuthContant.WAREHOUSE_LOCATION_VALIDATE));
        List<String> warehouseIdList = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(cfgApiAuthEntity)) {
            CfgApiAuthDTO.WarehouseLocationValidateDTO warehouseLocationValidateDTO = JSONUtil.toBean(cfgApiAuthEntity.getValue(), CfgApiAuthDTO.WarehouseLocationValidateDTO.class);
            warehouseIdList = Arrays.stream(warehouseLocationValidateDTO.getWarehouseIds().split(",")).collect(Collectors.toList());
        }
        long count = list.stream().filter(obj -> CharSequenceUtil.isBlank(obj.getWarehouseLocation())).count();
        //判断仓位是否需要必填
        if (warehouseIdList.contains(warehouseEntity.getId()) && count > 0) {
            throw new ServiceException(ApiError.ERROR_WAREHOUSE_LOCATION_NOT_NULL,warehouseEntity.getName());
        }
    }

    @Override
    public void updateKingdeeDetailId(JSONArray list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (Object obj : list) {
            JSONObject jsonObject = JSONUtil.parseObj(obj);
            String detailId = (String) jsonObject.get("detailId");
            String kingdeeDetailId = (String) jsonObject.get("kingdeeDetailId");
            this.lambdaUpdate()
                    .set(PoInstockDetailEntity::getKingdeeDetailId, kingdeeDetailId)
                    .eq(PoInstockDetailEntity::getId, detailId)
                    .update();
        }
    }
}
