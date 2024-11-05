package com.erp.server.wms.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.constant.CfgApiAuthContant;
import com.erp.model.dmp.dto.CfgApiAuthDTO;
import com.erp.model.dmp.entity.CfgApiAuthEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.TransferInfoDetailDTO;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.WarehouseLocationTypeEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.TransferInfoDetailMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private WarehouseService warehouseService;

    @Resource
    private WarehouseLocationService warehouseLocationService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private TransferApplicationDetailService transferApplicationDetailService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void add(List<TransferInfoDetailDTO.AddDTO> detailList, String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_1041,"直接调波单明细");
        }
        List<TransferInfoDetailEntity> list = BeanMapperUtils.copyList(TransferInfoDetailEntity.class, detailList);

        //处理明细数据
        doOpHandleDetails(list,mainId,Boolean.FALSE);

        this.saveBatch(list);
        //标记SKU
        List<String> skuIds = list.stream().map(TransferInfoDetailEntity::getSkuId).collect(Collectors.toList());
        plmTaskFeign.updateOccupyStatus(skuIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void update(List<TransferInfoDetailDTO.UpdateDTO> detailList, String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_1041,"直接调波单明细");
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
        //标记SKU
        List<String> skuIds = newList.stream().map(TransferInfoDetailEntity::getSkuId).collect(Collectors.toList());
        plmTaskFeign.updateOccupyStatus(skuIds);
    }

    @Override
    public void removeByMainIds(List<String> mainIds) {
        lambdaUpdate().in(TransferInfoDetailEntity::getMainId,mainIds).remove();
    }

    @Override
    public List<TransferInfoDetailEntity> listByMainId(String mainId) {
        return lambdaQuery()
                .eq(TransferInfoDetailEntity::getMainId,mainId)
                .orderByAsc(TransferInfoDetailEntity::getId)
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

        if (!SourceTypeEnum.TRANSFER_APPLICATION.getCode().equals(transferInfoEntity.getSourceType())) {
            return;
        }

        List<String> sourceDetailIds = newList.stream().map(TransferInfoDetailEntity::getSourceDetailId).collect(Collectors.toList());

        //已下推明细
        List<TransferInfoDetailEntity> transferInfoDetailList = this.listSourceDetailIds(sourceDetailIds);

        List<TransferApplicationDetailEntity> entities = transferApplicationDetailService.listByIds(sourceDetailIds);
        for (TransferInfoDetailEntity detailEntity : newList) {
            //调拨申请
            Integer applicationQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(transferInfoDetailList)) {
                applicationQty = entities.stream().filter(obj -> obj.getId().equals(detailEntity.getSourceDetailId()))
                        .map(TransferApplicationDetailEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            //已下推数量（不包括本明细数量）
            Integer hasPickingQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(transferInfoDetailList)) {
                hasPickingQty = transferInfoDetailList.stream().filter(obj -> obj.getSourceDetailId().equals(detailEntity.getSourceDetailId()) && !obj.getId().equals(detailEntity.getId()))
                        .map(TransferInfoDetailEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            //数量检验
            if (detailEntity.getQty() >applicationQty - hasPickingQty) {
                throw new ServiceException(ApiError.ERROR_99051.code, String.format(ApiError.ERROR_99051.msg,transferInfoEntity.getSourceCode(), detailEntity.getSkuNo()));
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
    private void doOpHandleDetails (List<TransferInfoDetailEntity> detailList, String mainId, Boolean isUpdate) {
        //去除服务、费用SKU
        List<TransferInfoDetailEntity> newList = removeNoInventorySku(detailList);
        if (CollectionUtils.isEmpty(newList)) {
            throw new ServiceException(ApiError.ERROR_NO_INVENTORY_SKU_NOT_EXIST);
        }

        //需要新增的数据
        List<TransferInfoDetailEntity> addList = newList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());

        //需要修改的数据
        List<String> ids = newList.stream().filter(obj -> StringUtils.isNotBlank(obj.getId())).map(TransferInfoDetailEntity::getId).collect(Collectors.toList());
        List<TransferInfoDetailEntity> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            list = this.listByIds(ids);
        }

        //仓库信息
        List<String> warehouseIdList = newList.stream().flatMap(obj -> Stream.of(obj.getInWarehouseId(), obj.getOutWarehouseId())).collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIdList);
        if (CollectionUtils.isEmpty(warehouseList)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        //仓位必填验证
        checkWarehouseLocation(warehouseList,newList);

        //仓位信息
        List<WarehouseLocationDTO.WarehouseLocationSearchParamDTO> listParam = newList
                .stream()
                .filter(obj -> StringUtils.isNotBlank(obj.getInWarehouseLocation()) || StringUtils.isNotBlank(obj.getOutWarehouseLocation()))
                .flatMap(obj -> Stream.of(new WarehouseLocationDTO.WarehouseLocationSearchParamDTO(obj.getInWarehouseId(), obj.getInWarehouseLocation())
                        , new WarehouseLocationDTO.WarehouseLocationSearchParamDTO(obj.getOutWarehouseId(), obj.getOutWarehouseLocation())))
                .distinct()
                .collect(Collectors.toList());

        List<WarehouseLocationEntity> warehouseLocationList = warehouseLocationService.listByWarehouseIdAndCode(listParam);

        //SKU信息
        List<String> skuIds = newList.stream().map(TransferInfoDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }

        for (TransferInfoDetailEntity detail:newList) {

            //调入仓库
            WarehouseEntity inWarehouse = warehouseList.stream().filter(obj -> obj.getId().equals(detail.getInWarehouseId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(inWarehouse)) {
                throw new ServiceException(ApiError.ERROR_99002);
            }
            detail.setInWarehouseName(inWarehouse.getName());
            //调出仓库
            WarehouseEntity outWarehouse = warehouseList.stream().filter(obj -> obj.getId().equals(detail.getOutWarehouseId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(outWarehouse)) {
                throw new ServiceException(ApiError.ERROR_99002);
            }
            detail.setOutWarehouseName(outWarehouse.getName());
            //验证调入仓位
            if (StringUtils.isNotBlank(detail.getInWarehouseLocation())) {
                long count = warehouseLocationList
                        .stream()
                        .filter(obj -> detail.getInWarehouseLocation().equals(obj.getCode())
                                && detail.getInWarehouseId().equals(obj.getWarehouseId())
                                && WarehouseLocationTypeEnum.LOCATION.getCode().equals(obj.getType())
                                && (ObjectUtils.isNotEmpty(obj.getDisabled()) && !obj.getDisabled()))
                        .count();
                if (count == 0) {
                    throw new ServiceException(ApiError.ERROR_WAREHOUSE_REF_LOCATION,detail.getInWarehouseName(),detail.getInWarehouseLocation());
                }
            }
            //验证调出仓位
            if (StringUtils.isNotBlank(detail.getOutWarehouseLocation())) {
                long count = warehouseLocationList
                        .stream()
                        .filter(obj -> detail.getOutWarehouseLocation().equals(obj.getCode())
                                && detail.getOutWarehouseId().equals(obj.getWarehouseId())
                                && WarehouseLocationTypeEnum.LOCATION.getCode().equals(obj.getType())
                                && (ObjectUtils.isNotEmpty(obj.getDisabled()) && !obj.getDisabled()))
                        .count();
                if (count == 0) {
                    throw new ServiceException(ApiError.ERROR_WAREHOUSE_REF_LOCATION,detail.getOutWarehouseName(),detail.getOutWarehouseLocation());
                }
            }

            //单位
            SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(detail.getSkuId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(skuVO)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            detail.setUnit(skuVO.getUnitName());
            detail.setSkuNo(skuVO.getSkuNo());
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

    /**
     * @description: 仓位必填验证
     * @author Will
     * @date: 2023/12/19 15:19
     * @param warehouseList
     * @param list
     */
    private void checkWarehouseLocation (List<WarehouseEntity> warehouseList,List<TransferInfoDetailEntity> list) {
        //仓库配置
        CfgApiAuthEntity cfgApiAuthEntity = dmpTaskFeign.getByKey(new CfgApiAuthDTO.FeignDTO(CfgApiAuthContant.WAREHOUSE_LOCATION_VALIDATE));
        List<String> warehouseIdList = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(cfgApiAuthEntity)) {
            CfgApiAuthDTO.WarehouseLocationValidateDTO warehouseLocationValidateDTO = JSONUtil.toBean(cfgApiAuthEntity.getValue(), CfgApiAuthDTO.WarehouseLocationValidateDTO.class);
            warehouseIdList = Arrays.stream(warehouseLocationValidateDTO.getWarehouseIds().split(",")).collect(Collectors.toList());
        }
        for (TransferInfoDetailEntity entity : list) {
            //调入仓库
            WarehouseEntity inWarehouse = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getInWarehouseId())).findFirst().orElse(new WarehouseEntity());
            if (warehouseIdList.contains(inWarehouse.getId()) && StrUtil.isBlank(entity.getInWarehouseLocation())) {
                throw new ServiceException(ApiError.ERROR_WAREHOUSE_LOCATION_NOT_NULL,inWarehouse.getName());
            }
            //调出仓库
            WarehouseEntity outWarehouse = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getOutWarehouseId())).findFirst().orElse(new WarehouseEntity());
            if (warehouseIdList.contains(outWarehouse.getId()) && StrUtil.isBlank(entity.getInWarehouseLocation())) {
                throw new ServiceException(ApiError.ERROR_WAREHOUSE_LOCATION_NOT_NULL,outWarehouse.getName());
            }
        }
    }

    /**
     * 移除包含服务和费用的sku明细
     * @author will
     * @date 2024/7/26 22:52
     * @param newList
     * @return List<TransferInfoDetailEntity>
     */
    private List<TransferInfoDetailEntity> removeNoInventorySku (List<TransferInfoDetailEntity> newList) {
        List<SkuVO> noInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> skuIdList = CollectionUtils.isEmpty(noInventorySkuList)
                ? new ArrayList<>() : noInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList());
        return newList.stream().filter(obj -> !skuIdList.contains(obj.getSkuId())).collect(Collectors.toList());
    }

}
