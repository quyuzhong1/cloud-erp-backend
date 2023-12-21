package com.erp.server.wms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.dto.PurchaseReturnOrderDetailDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.PoInstockDetailEntity;
import com.erp.model.wms.entity.PurchaseReturnOrderDetailEntity;
import com.erp.model.wms.entity.PurchaseReturnOrderEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.model.wms.enums.ReturnModeEnum;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.ReturnModeEnum;
import com.erp.model.wms.enums.ReturnOrderSourceEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.server.wms.mapper.PurchaseReturnOrderDetailMapper;
import com.erp.server.wms.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 采购退货单明细 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-04-07
 */
@Service
public class PurchaseReturnOrderDetailServiceImpl extends SuperServiceImpl<PurchaseReturnOrderDetailMapper, PurchaseReturnOrderDetailEntity> implements PurchaseReturnOrderDetailService {


    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private PoInstockDetailService poInstockDetailService;

    @Resource
    private WarehouseReceiveDetailService warehouseReceiveDetailService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private InventoryService inventoryService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private PurchaseReturnOrderService purchaseReturnOrderService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private DmpTaskFeign dmpTaskFeign;


    /**
     * @param sourceDetailIds
     * @return List<PurchaseReturnOrderDetailEntity>
     * @description: 根据来源明细ids查询退货明细
     * @author Will
     * @date: 2023/4/14 11:54
     */
    @Override
    public List<PurchaseReturnOrderDetailEntity> listBySourceDetailIds(List<String> sourceDetailIds) {
        return baseMapper.listBySourceDetailIds(sourceDetailIds);
    }

    /**
     * 新增
     *
     * @param dto dto
     * @param id  id:主表id
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/4/13 14:43
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(PurchaseReturnOrderDTO.AddDTO dto, String id) {
        Integer returnQty = 0;
        //创建保存详情的集合
        List<PurchaseReturnOrderDetailEntity> listDetail = new ArrayList<>();
        //退货仓库
        String returnWarehouseId = dto.getReturnWarehouseId();
        WarehouseEntity warehouse = warehouseService.getById(returnWarehouseId);
        String warehouseOrgId = Objects.nonNull(warehouse) ? warehouse.getOrgId() : "";
        if (StringUtils.isNotBlank(dto.getPurchaseOrderId())) {
            //获取界面传过来的采购单详情表id集合
            List<String> orderDetailIds = dto.getPurchasePriceDetailList().stream().map(PurchaseReturnOrderDetailDTO.AddDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
            //根据ids查询采购单详情
            List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(orderDetailIds);
            if (CollectionUtils.isEmpty(purchaseOrderDetailEntities)) {
                throw new ServiceException(ApiError.ERROR_98026);
            }
            List<String> skuIdList = purchaseOrderDetailEntities.stream().map(PurchaseOrderDetailEntity::getSkuId).collect(Collectors.toList());
            List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);

            // 关闭SKU重复检查
            // checkAddDetailsRepeatSku(purchaseOrderDetailEntities);
            //遍历需要保存的采购收货单详情信息，并赋值采购单信息
            List<PurchaseReturnOrderDetailDTO.AddDTO> detailList = dto.getPurchasePriceDetailList();
            List<PoInstockDetailEntity> stockInDetailEntityList = poInstockDetailService.listDetailByPodIds(orderDetailIds);
            List<WarehouseReceiveDetailEntity> detailEntityList = warehouseReceiveDetailService.listWarehouseReceiveByPodIds(orderDetailIds);
            List<String> collect = dto.getPurchasePriceDetailList().stream().map(req -> req.getCurrency()).distinct().collect(Collectors.toList());
            List<CurrencyDTO.ViewDTO> currency = sysUserFeign.listByCurrency(collect);

            List<PurchaseReturnOrderDetailEntity> purchaseReturnOrderDetailEntities = listReturnOrderDetailByPodIds(orderDetailIds);
            for (PurchaseReturnOrderDetailDTO.AddDTO addDTO : detailList) {
                PurchaseReturnOrderDetailEntity purchaseReturnOrderDetailEntity = new PurchaseReturnOrderDetailEntity();
                purchaseReturnOrderDetailEntity.setMainId(id);
                PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(detail -> detail.getId().equals(addDTO.getPurchaseOrderDetailId())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(purchaseOrderDetailEntity)) {
                    purchaseReturnOrderDetailEntity.setSkuId(purchaseOrderDetailEntity.getSkuId());
                    purchaseReturnOrderDetailEntity.setSkuNo(purchaseOrderDetailEntity.getSkuNo());
                    returnQty = purchaseReturnOrderDetailEntities.stream().filter(req -> req.getPurchaseOrderDetailId().equals(addDTO.getPurchaseOrderDetailId())).map(PurchaseReturnOrderDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
                    if (dto.getSourceType().equals(SourceTypeEnum.PO_RECEIVE.getCode())) {
                        Integer receiveQty = detailEntityList.stream().filter(req -> req.getPurchaseOrderDetailId().equals(addDTO.getPurchaseOrderDetailId()) && req.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                        if (addDTO.getReturnQty() > receiveQty) {
                            throw new ServiceException(ApiError.ERROR_99030.code, String.format(ApiError.ERROR_99030.msg, purchaseOrderDetailEntity.getSkuNo()));
                        }
                    } else if (dto.getSourceType().equals(ReturnOrderSourceEnum.QC.getCode())) {
                        Integer inventoryTotal = inventoryService.getInventoryTotal(warehouseOrgId, dto.getReturnWarehouseId(), addDTO.getSkuId(), addDTO.getWarehouseLocation(), InventoryStatusEnum.WAIT_QC.getCode());
                        if (addDTO.getReturnQty() > inventoryTotal) {
                            throw new ServiceException(ApiError.ERROR_99079, JSONUtil.toJsonStr(purchaseOrderDetailEntity.getSkuNo()));
                        }
                    } else {
                        Integer stockInQty = stockInDetailEntityList.stream().filter(req -> req.getPurchaseOrderDetailId().equals(addDTO.getPurchaseOrderDetailId()) && req.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                        if (addDTO.getReturnQty() > stockInQty) {
                            throw new ServiceException(ApiError.ERROR_99026.code, String.format(ApiError.ERROR_99026.msg, purchaseOrderDetailEntity.getSkuNo()));
                        }

                        if (addDTO.getReturnQty() + returnQty > stockInQty) {
                            throw new ServiceException(ApiError.ERROR_99031.code, String.format(ApiError.ERROR_99031.msg, purchaseOrderDetailEntity.getSkuNo()));
                        }
                    }
                    SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(purchaseReturnOrderDetailEntity.getSkuId())).findFirst().orElse(new SkuVO());
                    purchaseReturnOrderDetailEntity.setMainSupplierId(skuVO.getSupplierId());

                    purchaseReturnOrderDetailEntity.setReturnQty(addDTO.getReturnQty());
                    purchaseReturnOrderDetailEntity.setReplenishQty(addDTO.getReplenishQty());
                    purchaseReturnOrderDetailEntity.setDeductAmountQty(addDTO.getDeductAmountQty());
                    purchaseReturnOrderDetailEntity.setReturnPrice(addDTO.getReturnPrice());
                    purchaseReturnOrderDetailEntity.setRemark(addDTO.getRemark());
                    purchaseReturnOrderDetailEntity.setPurchaseOrderDetailId(addDTO.getPurchaseOrderDetailId());
                    purchaseReturnOrderDetailEntity.setCurrency(addDTO.getCurrency());

                    if (CollectionUtils.isNotEmpty(currency)) {
                        CurrencyDTO.ViewDTO viewDTO = currency.stream().filter(req -> req.getId().equals(addDTO.getCurrency())).findFirst().orElse(new CurrencyDTO.ViewDTO());
                        purchaseReturnOrderDetailEntity.setCurrencySymbol(viewDTO.getSymbol());
                    }
                    purchaseReturnOrderDetailEntity.setSourceDetailId(addDTO.getSourceDetailId());
                    purchaseReturnOrderDetailEntity.setWarehouseLocation(addDTO.getWarehouseLocation());
                    purchaseReturnOrderDetailEntity.setWarehouseLocation(addDTO.getWarehouseLocation());
                } else {
                    throw new ServiceException(ApiError.ERROR_99006);
                }
                listDetail.add(purchaseReturnOrderDetailEntity);
            }
        } else {
            notProductOrderAdd(dto, id, listDetail);
        }
        //仓位必填验证
        checkWarehouseLocation(warehouse,listDetail);
        //保存详情信息
        return this.saveBatch(listDetail);
    }

    /**
     * @description: 仓位必填验证
     * @author Will
     * @date: 2023/12/19 15:20
     * @param warehouseEntity
     * @param list
     */
    private void checkWarehouseLocation (WarehouseEntity warehouseEntity,List<PurchaseReturnOrderDetailEntity> list) {
        //仓库配置
        CfgApiAuthEntity cfgApiAuthEntity = dmpTaskFeign.getByKey(new CfgApiAuthDTO.FeignDTO(CfgApiAuthContant.WAREHOUSE_LOCATION_VALIDATE));
        List<String> warehouseIdList = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(cfgApiAuthEntity)) {
            CfgApiAuthDTO.WarehouseLocationValidateDTO warehouseLocationValidateDTO = JSONUtil.toBean(cfgApiAuthEntity.getValue(), CfgApiAuthDTO.WarehouseLocationValidateDTO.class);
            warehouseIdList = Arrays.stream(warehouseLocationValidateDTO.getWarehouseIds().split(",")).collect(Collectors.toList());
        }
        long count = list.stream().filter(obj -> StrUtil.isBlank(obj.getWarehouseLocation())).count();
        //判断仓位是否需要必填
        if (warehouseIdList.contains(warehouseEntity.getId()) && count > 0) {
            throw new ServiceException(ApiError.ERROR_WAREHOUSE_LOCATION_NOT_NULL,warehouseEntity.getName());
        }
    }

    /**
     * 无采购单新增
     *
     * @param dto id
     * @return void
     * @Author Luo_WG
     * @Date 2023/4/25 14:39
     **/
    private List<PurchaseReturnOrderDetailEntity> notProductOrderAdd(PurchaseReturnOrderDTO.AddDTO dto, String id, List<PurchaseReturnOrderDetailEntity> listDetail) {
        //遍历需要保存的采购收货单详情信息，并赋值采购单信息
        List<PurchaseReturnOrderDetailDTO.AddDTO> detailList = dto.getPurchasePriceDetailList();
        // 关闭SKU重复检查
        // checkAddDetailsRepeat(detailList);

        List<String> skuIdList = dto.getPurchasePriceDetailList().stream().map(PurchaseReturnOrderDetailDTO.AddDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> detailEntityList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        List<String> collect = dto.getPurchasePriceDetailList().stream().map(req -> req.getCurrency()).distinct().collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currency = sysUserFeign.listByCurrency(collect);
        for (PurchaseReturnOrderDetailDTO.AddDTO addDTO : detailList) {
            PurchaseReturnOrderDetailEntity purchaseReturnOrderDetailEntity = new PurchaseReturnOrderDetailEntity();
            BeanMapperUtils.copy(addDTO, purchaseReturnOrderDetailEntity);
            if (CollectionUtils.isNotEmpty(currency)) {
                CurrencyDTO.ViewDTO viewDTO = currency.stream().filter(req -> req.getId().equals(addDTO.getCurrency())).findFirst().orElse(new CurrencyDTO.ViewDTO());
                purchaseReturnOrderDetailEntity.setCurrencySymbol(viewDTO.getSymbol());
            }
            purchaseReturnOrderDetailEntity.setMainId(id);
            purchaseReturnOrderDetailEntity.setReturnQty(addDTO.getReturnQty());
            //获取sku信息
            SkuVO skuVO = detailEntityList.stream().filter(entityClass -> entityClass.getSkuId().equals(addDTO.getSkuId())).findFirst().orElse(new SkuVO());
            purchaseReturnOrderDetailEntity.setSkuNo(skuVO.getSkuNo());
            purchaseReturnOrderDetailEntity.setMainSupplierId(skuVO.getSupplierId());
            listDetail.add(purchaseReturnOrderDetailEntity);
        }
        return listDetail;
    }

    /**
     * 修改
     *
     * @param dto dto
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/4/13 15:22
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(PurchaseReturnOrderDTO.UpdateDTO dto, String id) {
        PurchaseReturnOrderEntity entity = purchaseReturnOrderService.getById(id);

        List<String> addList = dto.getPurchasePriceDetailList().stream().filter(c -> StringUtils.isBlank(c.getId())).map(PurchaseReturnOrderDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        //原明细数据
        List<PurchaseReturnOrderDetailEntity> oldList = this.getDetailByMainId(dto.getId());
        List<String> deleteIds = getDeleteIds(dto.getPurchasePriceDetailList(), oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<PurchaseReturnOrderDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.PURCHASE_RETURN_ORDER.getCode(), pairList, "编辑操作");
            this.removeByIds(deleteIds);
        }
        //仓库
        WarehouseEntity warehouse = warehouseService.getById(dto.getReturnWarehouseId());
        if (ObjectUtils.isEmpty(warehouse)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        Integer returnQty = 0;
        //创建保存详情的集合
        List<PurchaseReturnOrderDetailEntity> listDetail = new ArrayList<>();
        if (StringUtils.isNotBlank(dto.getPurchaseOrderId())) {
            //获取界面传过来的采购单详情表id集合
            List<String> orderDetailIds = dto.getPurchasePriceDetailList().stream().map(PurchaseReturnOrderDetailDTO.UpdateDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
            //根据ids查询采购单详情
            List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(orderDetailIds);
            if (CollectionUtils.isEmpty(purchaseOrderDetailEntities)) {
                throw new ServiceException(ApiError.ERROR_98026);
            }
            List<String> skuIdList = purchaseOrderDetailEntities.stream().map(PurchaseOrderDetailEntity::getSkuId).collect(Collectors.toList());
            List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);

            //遍历需要保存的采购收货单详情信息，并赋值采购单信息
            List<PurchaseReturnOrderDetailDTO.UpdateDTO> detailList = dto.getPurchasePriceDetailList();
            List<PoInstockDetailEntity> stockInDetailEntityList = poInstockDetailService.listDetailByPodIds(orderDetailIds);
            List<WarehouseReceiveDetailEntity> detailEntityList = warehouseReceiveDetailService.listWarehouseReceiveByPodIds(orderDetailIds);
            List<PurchaseReturnOrderDetailEntity> purchaseReturnOrderDetailEntities = listReturnOrderDetailByPodIds(orderDetailIds);

            List<String> collect = dto.getPurchasePriceDetailList().stream().map(req -> req.getCurrency()).distinct().collect(Collectors.toList());
            List<CurrencyDTO.ViewDTO> currency = sysUserFeign.listByCurrency(collect);

            for (PurchaseReturnOrderDetailDTO.UpdateDTO updateDTO : detailList) {
                PurchaseReturnOrderDetailEntity purchaseReturnOrderDetailEntity = new PurchaseReturnOrderDetailEntity();
                BeanMapperUtils.copy(updateDTO, purchaseReturnOrderDetailEntity);
                //退货补货
                if (ReturnModeEnum.REPLENISHMENT.getCode().equals(dto.getReturnMode())) {
                    purchaseReturnOrderDetailEntity.setDeductAmountQty(0);
                } else {
                    //退货扣款
                    purchaseReturnOrderDetailEntity.setReplenishQty(0);
                }
                purchaseReturnOrderDetailEntity.setMainId(id);
                PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(detail -> detail.getId().equals(updateDTO.getPurchaseOrderDetailId())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(purchaseOrderDetailEntity)) {
                    purchaseReturnOrderDetailEntity.setSkuId(purchaseOrderDetailEntity.getSkuId());
                    purchaseReturnOrderDetailEntity.setSkuNo(purchaseOrderDetailEntity.getSkuNo());
                    //参考供应商赋值
                    SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(purchaseReturnOrderDetailEntity.getSkuId())).findFirst().orElse(new SkuVO());
                    purchaseReturnOrderDetailEntity.setMainSupplierId(skuVO.getSupplierId());

                    if (entity.getSourceType().equals(SourceTypeEnum.PO_RECEIVE.getCode())) {
                        Integer receiveQty = detailEntityList.stream().filter(req -> req.getPurchaseOrderDetailId().equals(updateDTO.getPurchaseOrderDetailId()) && req.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                        if (updateDTO.getReturnQty() > receiveQty) {
                            throw new ServiceException(ApiError.ERROR_99030.code, String.format(ApiError.ERROR_99030.msg, purchaseOrderDetailEntity.getSkuNo()));
                        }
                    } else if (entity.getSourceType().equals(ReturnOrderSourceEnum.QC.getCode())) {
                        Integer inventoryTotal = inventoryService.getInventoryTotal(warehouse.getOrgId(), warehouse.getId(), updateDTO.getSkuId(), updateDTO.getWarehouseLocation(), InventoryStatusEnum.WAIT_QC.getCode());
                        returnQty = purchaseReturnOrderDetailEntities.stream().filter(req -> req.getPurchaseOrderDetailId().equals(updateDTO.getPurchaseOrderDetailId()) && !req.getId().equals(updateDTO.getId())).map(PurchaseReturnOrderDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
                        if (updateDTO.getReturnQty() > inventoryTotal) {
                            throw new ServiceException(ApiError.ERROR_99079, JSONUtil.toJsonStr(purchaseOrderDetailEntity.getSkuNo()));
                        }
                    } else {
                        Integer stockInQty = stockInDetailEntityList.stream().filter(req -> req.getPurchaseOrderDetailId().equals(updateDTO.getPurchaseOrderDetailId()) && req.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                        if (updateDTO.getReturnQty() > stockInQty) {
                            throw new ServiceException(ApiError.ERROR_99026.code, String.format(ApiError.ERROR_99026.msg, purchaseOrderDetailEntity.getSkuNo()));
                        }
                        returnQty = purchaseReturnOrderDetailEntities.stream().filter(req -> req.getPurchaseOrderDetailId().equals(updateDTO.getPurchaseOrderDetailId()) && !req.getId().equals(updateDTO.getId())).map(PurchaseReturnOrderDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
                        if (updateDTO.getReturnQty() + returnQty > stockInQty) {
                            throw new ServiceException(ApiError.ERROR_99031.code, String.format(ApiError.ERROR_99031.msg, purchaseOrderDetailEntity.getSkuNo()));
                        }
                    }
                    if (CollectionUtils.isNotEmpty(currency)) {
                        CurrencyDTO.ViewDTO viewDTO = currency.stream().filter(req -> req.getId().equals(updateDTO.getCurrency())).findFirst().orElse(new CurrencyDTO.ViewDTO());
                        purchaseReturnOrderDetailEntity.setCurrencySymbol(viewDTO.getSymbol());
                    }
                } else {
                    throw new ServiceException(ApiError.ERROR_99006);
                }
                //修改操作日志
                if (StringUtils.isNotBlank(purchaseReturnOrderDetailEntity.getId())) {
                    PurchaseReturnOrderDetailEntity old = this.getById(purchaseReturnOrderDetailEntity.getId());
                    operateLogService.addModuleOperateLogByObj(old, purchaseReturnOrderDetailEntity, ModuleTypeEnum.PURCHASE_RETURN_ORDER.getCode(), dto.getId(), "", String.format("【%s】", old.getSkuNo()));
                }
                listDetail.add(purchaseReturnOrderDetailEntity);
            }
        } else {
            notProductOrderUpdate(dto, id, listDetail);
        }
        //仓位必填验证
        checkWarehouseLocation(warehouse,listDetail);

        boolean flag = this.saveOrUpdateBatch(listDetail);
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<PurchaseReturnOrderDetailEntity> returnOrderDetailEntities = this.listByIds(addList);
            List<Pair<String, String>> addPairList = returnOrderDetailEntities.stream().map(obj -> new Pair<>(dto.getId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.PURCHASE_RETURN_ORDER.getCode(), addPairList, "编辑操作");
        }
        return flag;
    }

    private List<String> getDeleteIds(List<PurchaseReturnOrderDetailDTO.UpdateDTO> newList, List<PurchaseReturnOrderDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(PurchaseReturnOrderDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(PurchaseReturnOrderDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * 无采购单新增
     *
     * @param dto id
     * @return void
     * @Author Luo_WG
     * @Date 2023/4/25 14:39
     **/
    private List<PurchaseReturnOrderDetailEntity> notProductOrderUpdate(PurchaseReturnOrderDTO.UpdateDTO dto, String id, List<PurchaseReturnOrderDetailEntity> listDetail) {
        //遍历需要保存的采购收货单详情信息，并赋值采购单信息
        List<PurchaseReturnOrderDetailDTO.UpdateDTO> detailList = dto.getPurchasePriceDetailList();
        List<String> skuIdList = dto.getPurchasePriceDetailList().stream().map(PurchaseReturnOrderDetailDTO.UpdateDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> detailEntityList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        //获取币别符号
        List<String> collect = dto.getPurchasePriceDetailList().stream().map(req -> req.getCurrency()).distinct().collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currency = sysUserFeign.listByCurrency(collect);

        for (PurchaseReturnOrderDetailDTO.UpdateDTO updateDTO : detailList) {

            PurchaseReturnOrderDetailEntity purchaseReturnOrderDetailEntity = new PurchaseReturnOrderDetailEntity();
            BeanMapperUtils.copy(updateDTO, purchaseReturnOrderDetailEntity);
            //退货补货
            if (ReturnModeEnum.REPLENISHMENT.getCode().equals(dto.getReturnMode())) {
                purchaseReturnOrderDetailEntity.setDeductAmountQty(0);
            } else {
                //退货扣款
                purchaseReturnOrderDetailEntity.setReplenishQty(0);
            }
            purchaseReturnOrderDetailEntity.setMainId(id);
            purchaseReturnOrderDetailEntity.setReturnQty(updateDTO.getReturnQty());

            if (CollectionUtils.isNotEmpty(currency)) {
                CurrencyDTO.ViewDTO viewDTO = currency.stream().filter(req -> req.getId().equals(updateDTO.getCurrency())).findFirst().orElse(new CurrencyDTO.ViewDTO());
                purchaseReturnOrderDetailEntity.setCurrencySymbol(viewDTO.getSymbol());
            }

            //获取sku信息
            SkuVO skuVO = detailEntityList.stream().filter(entityClass -> entityClass.getSkuId().equals(updateDTO.getSkuId())).findFirst().orElse(new SkuVO());
            purchaseReturnOrderDetailEntity.setSkuNo(skuVO.getSkuNo());
            purchaseReturnOrderDetailEntity.setMainSupplierId(skuVO.getSupplierId());
            listDetail.add(purchaseReturnOrderDetailEntity);
            //修改操作日志
            if (StringUtils.isNotBlank(purchaseReturnOrderDetailEntity.getId())) {
                PurchaseReturnOrderDetailEntity old = this.getById(purchaseReturnOrderDetailEntity.getId());
                operateLogService.addModuleOperateLogByObj(old, purchaseReturnOrderDetailEntity, ModuleTypeEnum.PURCHASE_RETURN_ORDER.getCode(), dto.getId(), "", String.format("【%s】", old.getSkuNo()));
            }
        }
        return listDetail;
    }

    /**
     * 根据主表id删除
     *
     * @param mainIds mainIds
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     **/
    @Override
    public Boolean delete(List<String> mainIds) {
        return lambdaUpdate().set(PurchaseReturnOrderDetailEntity::getIsDeleted, Boolean.TRUE)
                .in(PurchaseReturnOrderDetailEntity::getMainId, mainIds)
                .remove();
    }

    /**
     * 根据主表id查询详情表信息
     *
     * @param mainId mainId
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/4/13 17:44
     **/
    @Override
    public List<PurchaseReturnOrderDetailEntity> getDetailByMainId(String mainId) {
        LambdaQueryWrapper<PurchaseReturnOrderDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PurchaseReturnOrderDetailEntity::getMainId, mainId);
        return this.list(queryWrapper);
    }

    @Override
    public List<PurchaseReturnOrderDetailEntity> listReturnOrderDetailByPodIds(List<String> podIds) {
        return baseMapper.listReturnOrderDetailByPodIds(podIds);
    }

    @Override
    public List<PurchaseReturnOrderDetailEntity> listByMainIds(List<String> mainIds) {
        return lambdaQuery().in(PurchaseReturnOrderDetailEntity::getMainId, mainIds).list();
    }
}
