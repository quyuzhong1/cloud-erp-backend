package com.erp.server.wms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.SubcontractTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.entity.SoB2cReturnDetailEntity;
import com.erp.model.oms.entity.SoB2cReturnEntity;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.model.oms.entity.SoReturnEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.SoReturnInstockDTO;
import com.erp.model.wms.dto.SoReturnInstockDetailDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.SoReturnInstockDetailEntity;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import com.erp.model.wms.entity.SoReturnReceiveDetailEntity;
import com.erp.rpc.oms.feign.SoReturnFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.server.wms.mapper.SoReturnInstockDetailMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 销售退货入库单明细表 服务实现类
 *
 * @author Luo_WG
 * @since 2023-05-10
 */
@Service
public class SoReturnInstockDetailServiceImpl extends SuperServiceImpl<SoReturnInstockDetailMapper, SoReturnInstockDetailEntity> implements SoReturnInstockDetailService {
    @Resource
    private SoReturnFeign soReturnFeign;

    @Resource
    private SoReturnReceiveDetailService soReturnReceiveDetailService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private SoReturnInstockService soReturnInstockService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean add(SoReturnInstockDTO.Add dto, String id) {
        if (StringUtils.isNotBlank(dto.getSoReturnId())) {
            //获取退货单详情表id
            List<String> returnDetailIds = dto.getDetailList().stream().map(SoReturnInstockDetailDTO.Add::getSoReturnDetailId).collect(Collectors.toList());
            List<String> skuIds = dto.getDetailList().stream().map(SoReturnInstockDetailDTO.Add::getSkuId).collect(Collectors.toList());
            List<SkuVO> skuInfoByIds = plmTaskFeign.listSkuProductByIds(skuIds);
            List<SoReturnDetailEntity> soReturnDetailEntities = soReturnFeign.listDetailByIds(returnDetailIds);
            SoB2cReturnEntity soB2cReturnEntity = FeignQuery.getById(SoB2cReturnEntity.class,dto.getSoReturnId());

            List<String> returnIds = soReturnDetailEntities.stream().map(req -> req.getMainId()).distinct().collect(Collectors.toList());
            if(Objects.nonNull(soB2cReturnEntity)){
                returnIds.add(soB2cReturnEntity.getId());
            }
            List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = soReturnReceiveDetailService.listDetailBySourceIds(returnIds);
            List<SoReturnInstockDetailEntity> soReturnInstockDetailEntities = this.listDetailBySoReturnDetailIds(returnDetailIds);

            List<String> warehouseIds = dto.getDetailList().stream().map(SoReturnInstockDetailDTO.Add::getWarehouseId).collect(Collectors.toList());
            warehouseIds.add(dto.getWarehouseId());
            //获取仓库信息
            List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(warehouseIds);

            List<SoReturnInstockDetailEntity> list = new ArrayList<>();
            for (SoReturnInstockDetailDTO.Add detailDto : dto.getDetailList()) {
                SkuVO skuVO = skuInfoByIds.stream().filter(req -> req.getSkuId().equals(detailDto.getSkuId())).findFirst().orElse(new SkuVO());
                SoReturnInstockDetailEntity detailEntity = new SoReturnInstockDetailEntity();
                detailEntity.setMainId(id);
                detailEntity.setSkuId(detailDto.getSkuId());
                detailEntity.setSkuNo(detailDto.getSkuNo());
                detailEntity.setRealQty(detailDto.getRealQty());
                detailEntity.setReceiveQty(detailDto.getReceiveQty());
                detailEntity.setWarehouseLocation(detailDto.getWarehouseLocation());
                detailEntity.setRemark(detailDto.getRemark());
                detailEntity.setSourceDetailId(detailDto.getSourceDetailId());
                detailEntity.setSoReturnDetailId(detailDto.getSoReturnDetailId());
                //封装仓库，如果没有明细仓库，取主记录的仓库
                if(StringUtils.isBlank(detailDto.getWarehouseId())){
                    if(StringUtils.isNotBlank(dto.getWarehouseId())){
                        WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(v->v.getId().equals(dto.getWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
                        detailEntity.setWarehouseId(dto.getWarehouseId());
                        detailEntity.setWarehouseName(updateDTO.getName());
                    }
                }else{
                    WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(v->v.getId().equals(detailDto.getWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
                    detailEntity.setWarehouseId(detailDto.getWarehouseId());
                    detailEntity.setWarehouseName(updateDTO.getName());
                }
                //实退数量
                Integer realQty = soReturnInstockDetailEntities.stream().filter(req -> req.getSoReturnDetailId().equals(detailDto.getSoReturnDetailId())).map(SoReturnInstockDetailEntity::getRealQty).reduce(MathUtil.ZERO, Integer::sum);
                //签收单数量
                Integer receiveQty = soReturnReceiveDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSoReturnDetailId())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                if (receiveQty < detailDto.getRealQty() + realQty) {
                    throw new ServiceException(ApiError.ERROR_92045, skuVO.getSkuNo());
                }
                if(dto.getType().equals("B2C")){
                    if(Objects.nonNull(soB2cReturnEntity)){
                        detailEntity.setReturnTypeDict(soB2cReturnEntity.getType());
                        detailEntity.setReturnReasonDict(soB2cReturnEntity.getReason());
                    }
                }else{
                    SoReturnDetailEntity soReturnDetailEntity = soReturnDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSoReturnDetailId())).findFirst().orElse(null);
                    if (ObjectUtil.isEmpty(soReturnDetailEntity)) {
                        throw new ServiceException(ApiError.ERROR_92023, skuVO.getSkuNo());
                    }
                    //类型
                    String returnTypeDict = soReturnDetailEntity.getReturnTypeDict();
                    //原因
                    String returnReasonDict = soReturnDetailEntity.getReturnReasonDict();
                    if (StringUtils.isBlank(returnTypeDict)) {
                        returnTypeDict = detailDto.getReturnTypeDict();
                    }
                    if (StringUtils.isBlank(returnReasonDict)) {
                        returnReasonDict = detailDto.getReturnReasonDict();
                    }
                    detailEntity.setReturnTypeDict(returnTypeDict);
                    detailEntity.setReturnReasonDict(returnReasonDict);
                }
                list.add(detailEntity);
            }
            //更新委外标识
            updateSubContract(list);
            return this.saveBatch(list);
        } else {
            return notReturnOrderAdd(dto, id);
        }
    }


    /**
     * 无退货订单新增
     *
     * @param dto
     * @param id
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/7/24 14:47
     **/
    private Boolean notReturnOrderAdd(SoReturnInstockDTO.Add dto, String id) {
        List<String> sourceDetailIds = dto.getDetailList().stream().map(SoReturnInstockDetailDTO.Add::getSourceDetailId).collect(Collectors.toList());
        List<SoReturnReceiveDetailEntity> soReturnReceiveDetailList = soReturnReceiveDetailService.listDetailByIds(sourceDetailIds);

        List<String> skuIds = dto.getDetailList().stream().map(SoReturnInstockDetailDTO.Add::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuInfoByIds = plmTaskFeign.listSkuProductByIds(skuIds);
        //获取退货签收单详情表id
        List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = soReturnReceiveDetailService.listDetailByMainIds(Arrays.asList(dto.getSourceId()));
        List<SoReturnInstockDetailEntity> soReturnInstockDetailEntities = this.listDetailBySourceIds(Arrays.asList(dto.getSourceId()));
        List<SoReturnInstockDetailEntity> list = new ArrayList<>();
        List<String> warehouseIds = dto.getDetailList().stream().map(SoReturnInstockDetailDTO.Add::getWarehouseId).collect(Collectors.toList());
        warehouseIds.add(dto.getWarehouseId());
        //获取仓库信息
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(warehouseIds);
        for (SoReturnInstockDetailDTO.Add detailDto : dto.getDetailList()) {
            SkuVO skuVO = skuInfoByIds.stream().filter(req -> req.getSkuId().equals(detailDto.getSkuId())).findFirst().orElse(new SkuVO());
            SoReturnReceiveDetailEntity soReturnReceiveDetailEntity = soReturnReceiveDetailList.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soReturnReceiveDetailEntity)) {
                throw new ServiceException(ApiError.SO_RETURN_RECEIVE_SKU_NOT_EXIST, skuVO.getSkuNo());
            }
            SoReturnInstockDetailEntity detailEntity = new SoReturnInstockDetailEntity();
            //实退数量
            Integer realQty = soReturnInstockDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnInstockDetailEntity::getRealQty).reduce(MathUtil.ZERO, Integer::sum);
            //签收单数量
            Integer receiveQty = soReturnReceiveDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            if (receiveQty < detailDto.getRealQty() + realQty) {
                throw new ServiceException(ApiError.ERROR_92045, skuVO.getSkuNo());
            }
            detailEntity.setMainId(id);
            detailEntity.setSkuId(detailDto.getSkuId());

            detailEntity.setSkuNo(skuVO.getSkuNo());
            detailEntity.setRealQty(detailDto.getRealQty());
            detailEntity.setReceiveQty(detailDto.getReceiveQty());
            detailEntity.setReturnTypeDict(soReturnReceiveDetailEntity.getReturnTypeDict());
            detailEntity.setReturnReasonDict(soReturnReceiveDetailEntity.getReturnReasonDict());
            detailEntity.setWarehouseLocation(detailDto.getWarehouseLocation());
            detailEntity.setRemark(detailDto.getRemark());
            detailEntity.setSourceDetailId(detailDto.getSourceDetailId());
            detailEntity.setSoReturnDetailId(detailDto.getSoReturnDetailId());
            //封装仓库
            WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(v->v.getId().equals(detailDto.getWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
            detailEntity.setWarehouseId(detailDto.getWarehouseId());
            detailEntity.setWarehouseName(updateDTO.getName());
            list.add(detailEntity);
        }
        //更新委外标识
        updateSubContract(list);
        this.saveBatch(list);
        //标记SKU
        plmTaskFeign.updateOccupyStatus(skuIds);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean update(SoReturnInstockDTO.Update dto) {
        List<String> addList = dto.getDetailList().stream().filter(c -> StringUtils.isBlank(c.getId())).map(SoReturnInstockDetailDTO.Update::getId).collect(Collectors.toList());
        if (StringUtils.isNotBlank(dto.getSoReturnId())) {
            List<String> skuIds = dto.getDetailList().stream().map(SoReturnInstockDetailDTO.Update::getSkuId).collect(Collectors.toList());
            List<SkuVO> skuInfoByIds = plmTaskFeign.listSkuProductByIds(skuIds);
            //获取退货单详情表id
            List<String> returnDetailIds = dto.getDetailList().stream().map(SoReturnInstockDetailDTO.Update::getSoReturnDetailId).collect(Collectors.toList());
            List<SoReturnDetailEntity> soReturnDetailEntities = soReturnFeign.listDetailByIds(returnDetailIds);
            List<String> returnIds = soReturnDetailEntities.stream().map(req -> req.getMainId()).distinct().collect(Collectors.toList());
            List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = soReturnReceiveDetailService.listDetailBySourceIds(returnIds);
            List<SoReturnInstockDetailEntity> soReturnInstockDetailEntities = this.listDetailBySourceDetailIds(returnDetailIds);
            List<SoReturnInstockDetailEntity> list = new ArrayList<>();
            //原明细数据
            List<SoReturnInstockDetailEntity> oldList = this.listDetailByMainId(dto.getId());
            List<String> deleteIds = getDeleteIds(dto.getDetailList(), oldList);
            if (CollectionUtils.isNotEmpty(deleteIds)) {
                List<SoReturnInstockDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
                //操作日志
                List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
                operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.SO_RETURN_INSTOCK.getCode(), pairList, "编辑操作");
                this.removeByIds(deleteIds);
            }
            List<String> warehouseIds = dto.getDetailList().stream().map(SoReturnInstockDetailDTO.Update::getWarehouseId).collect(Collectors.toList());
            warehouseIds.add(dto.getWarehouseId());
            //获取仓库信息
            List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(warehouseIds);

            for (SoReturnInstockDetailDTO.Update detailDto : dto.getDetailList()) {
                SkuVO skuVO = skuInfoByIds.stream().filter(req -> req.getSkuId().equals(detailDto.getSkuId())).findFirst().orElse(new SkuVO());
                SoReturnInstockDetailEntity detailEntity = new SoReturnInstockDetailEntity();
//                SoReturnDetailEntity soReturnDetailEntity = soReturnDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSoReturnDetailId())).findFirst().orElse(null);
//                if (ObjectUtil.isEmpty(soReturnDetailEntity)) {
//                    throw new ServiceException(ApiError.ERROR_92023, skuVO.getSkuNo());
//                }
                //实退 入库数量
                Integer realQty = soReturnInstockDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSoReturnDetailId())).map(SoReturnInstockDetailEntity::getRealQty).reduce(MathUtil.ZERO, Integer::sum);
                //签收单数量
                Integer receiveQty = soReturnReceiveDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSoReturnDetailId())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                if (StringUtils.isNotBlank(detailDto.getId())) {
                    detailEntity.setId(detailDto.getId());
                    realQty = soReturnInstockDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSoReturnDetailId()) && !req.getId().equals(detailDto.getId())).map(SoReturnInstockDetailEntity::getRealQty).reduce(MathUtil.ZERO, Integer::sum);
                }
                if (receiveQty < detailDto.getRealQty() + realQty) {
                    throw new ServiceException(ApiError.ERROR_92026, skuVO.getSkuNo());
                }
                detailEntity.setMainId(dto.getId());
                detailEntity.setSkuId(skuVO.getSkuId());
                detailEntity.setSkuNo(skuVO.getSkuNo());
                detailEntity.setRealQty(detailDto.getRealQty());
                detailEntity.setReceiveQty(detailDto.getReceiveQty());
                detailEntity.setWarehouseLocation(detailDto.getWarehouseLocation());
                detailEntity.setRemark(detailDto.getRemark());
                detailEntity.setSourceDetailId(detailDto.getSourceDetailId());
                detailEntity.setSoReturnDetailId(detailDto.getSoReturnDetailId());

                //封装仓库，如果没有明细仓库，取主记录的仓库
                if(StringUtils.isBlank(detailDto.getWarehouseId())){
                    if(StringUtils.isNotBlank(dto.getWarehouseId())){
                        WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(v->v.getId().equals(dto.getWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
                        detailEntity.setWarehouseId(dto.getWarehouseId());
                        detailEntity.setWarehouseName(updateDTO.getName());
                    }
                }else{
                    WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(v->v.getId().equals(detailDto.getWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
                    detailEntity.setWarehouseId(detailDto.getWarehouseId());
                    detailEntity.setWarehouseName(updateDTO.getName());
                }

                list.add(detailEntity);
                //修改操作日志
                if (StringUtils.isNotBlank(detailEntity.getId())) {
                    SoReturnInstockDetailEntity old = this.getById(detailEntity.getId());
                    operateLogService.addModuleOperateLogByObj(old, detailEntity, ModuleTypeEnum.SO_RETURN_INSTOCK.getCode(), dto.getId(), "", String.format("【%s】", old.getSkuNo()));
                }
            }
            //添加操作日志
            if (CollectionUtils.isNotEmpty(addList)) {
                List<SoReturnInstockDetailEntity> returnInstockDetailEntities = this.listByIds(addList);
                List<Pair<String, String>> addPairList = returnInstockDetailEntities.stream().map(obj -> new Pair<>(dto.getId(), obj.getSkuNo())).collect(Collectors.toList());
                operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.SO_RETURN_INSTOCK.getCode(), addPairList, "编辑操作");
            }
            return this.saveOrUpdateBatch(list);
        } else {
            return notReturnOrderUpdate(dto);
        }

    }

    /**
     * 无退货订单修改
     *
     * @param dto
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/7/25 10:33
     **/
    private Boolean notReturnOrderUpdate(SoReturnInstockDTO.Update dto) {
        //获取退货签收单详情表id
        List<String> sourceDetailIds = dto.getDetailList().stream().map(SoReturnInstockDetailDTO.Update::getSourceDetailId).collect(Collectors.toList());

        List<String> addList = dto.getDetailList().stream().filter(c -> StringUtils.isBlank(c.getId())).map(SoReturnInstockDetailDTO.Update::getId).collect(Collectors.toList());
        List<String> skuIds = dto.getDetailList().stream().map(SoReturnInstockDetailDTO.Update::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuInfoByIds = plmTaskFeign.listSkuProductByIds(skuIds);

        List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = soReturnReceiveDetailService.listDetailByIds(sourceDetailIds);
        List<SoReturnInstockDetailEntity> soReturnInstockDetailEntities = this.listDetailBySourceDetailIds(sourceDetailIds);
        List<String> warehouseIds = dto.getDetailList().stream().map(SoReturnInstockDetailDTO.Update::getWarehouseId).collect(Collectors.toList());
        warehouseIds.add(dto.getWarehouseId());
        //获取仓库信息
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(warehouseIds);

        List<SoReturnInstockDetailEntity> list = new ArrayList<>();
        //原明细数据
        List<SoReturnInstockDetailEntity> oldList = this.listDetailByMainId(dto.getId());
        List<String> deleteIds = getDeleteIds(dto.getDetailList(), oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<SoReturnInstockDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.SO_RETURN_INSTOCK.getCode(), pairList, "编辑操作");
            this.removeByIds(deleteIds);
        }
        for (SoReturnInstockDetailDTO.Update detailDto : dto.getDetailList()) {
            SkuVO skuVO = skuInfoByIds.stream().filter(req -> req.getSkuId().equals(detailDto.getSkuId())).findFirst().orElse(new SkuVO());
            SoReturnReceiveDetailEntity soReturnReceiveDetailEntity = soReturnReceiveDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(new SoReturnReceiveDetailEntity());
            if (ObjectUtil.isEmpty(soReturnReceiveDetailEntity)) {
                throw new ServiceException(ApiError.SO_RETURN_RECEIVE_SKU_NOT_EXIST, skuVO.getSkuNo());
            }
            SoReturnInstockDetailEntity detailEntity = new SoReturnInstockDetailEntity();
            //实退 入库数量
            Integer realQty = soReturnInstockDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnInstockDetailEntity::getRealQty).reduce(MathUtil.ZERO, Integer::sum);
            //签收单数量
            Integer receiveQty = soReturnReceiveDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            if (StringUtils.isNotBlank(detailDto.getId())) {
                detailEntity.setId(detailDto.getId());
                realQty = soReturnInstockDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId()) && !req.getId().equals(detailDto.getId())).map(SoReturnInstockDetailEntity::getRealQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            if (receiveQty < detailDto.getRealQty() + realQty) {
                throw new ServiceException(ApiError.ERROR_92026, skuVO.getSkuNo());
            }
            detailEntity.setMainId(dto.getId());
            detailEntity.setSkuId(detailEntity.getSkuId());

            detailEntity.setSkuNo(skuVO.getSkuNo());
            detailEntity.setRealQty(detailDto.getRealQty());
            detailEntity.setReceiveQty(detailDto.getReceiveQty());
            detailEntity.setWarehouseLocation(detailDto.getWarehouseLocation());
            detailEntity.setRemark(detailDto.getRemark());
            detailEntity.setSourceDetailId(detailDto.getSourceDetailId());
            //封装仓库，如果没有明细仓库，取主记录的仓库
            if(StringUtils.isBlank(detailDto.getWarehouseId())){
                if(StringUtils.isNotBlank(dto.getWarehouseId())){
                    WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(v->v.getId().equals(dto.getWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
                    detailEntity.setWarehouseId(dto.getWarehouseId());
                    detailEntity.setWarehouseName(updateDTO.getName());
                }
            }else{
                WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(v->v.getId().equals(detailDto.getWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
                detailEntity.setWarehouseId(detailDto.getWarehouseId());
                detailEntity.setWarehouseName(updateDTO.getName());
            }
            list.add(detailEntity);
            //修改操作日志
            if (StringUtils.isNotBlank(detailEntity.getId())) {
                SoReturnInstockDetailEntity old = this.getById(detailEntity.getId());
                operateLogService.addModuleOperateLogByObj(old, detailEntity, ModuleTypeEnum.SO_RETURN_INSTOCK.getCode(), dto.getId(), "", String.format("【%s】", old.getSkuNo()));
            }
        }
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<SoReturnInstockDetailEntity> returnInstockDetailEntities = this.listByIds(addList);
            List<Pair<String, String>> addPairList = returnInstockDetailEntities.stream().map(obj -> new Pair<>(dto.getId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.SO_RETURN_INSTOCK.getCode(), addPairList, "编辑操作");
        }
        this.saveOrUpdateBatch(list);
        //标记SKU
        plmTaskFeign.updateOccupyStatus(skuIds);
        return Boolean.TRUE;
    }

    private List<String> getDeleteIds(List<SoReturnInstockDetailDTO.Update> newList, List<SoReturnInstockDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(SoReturnInstockDetailDTO.Update::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(SoReturnInstockDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> mainIds) {
        return lambdaUpdate().set(SoReturnInstockDetailEntity::getIsDeleted, Boolean.TRUE)
                .in(SoReturnInstockDetailEntity::getMainId, mainIds)
                .update();
    }

    @Override
    public List<SoReturnInstockDetailEntity> listDetailBySourceIds(List<String> sourceIds) {
        return baseMapper.listDetailBySourceIds(sourceIds);
    }

    @Override
    public List<SoReturnInstockDetailEntity> listDetailByMainId(String id) {
        return lambdaQuery().eq(SoReturnInstockDetailEntity::getMainId, id).list();
    }

    @Override
    public List<SoReturnInstockDetailEntity> listDetailByMainIds(List<String> ids) {
        return lambdaQuery().in(SoReturnInstockDetailEntity::getMainId, ids).list();
    }

    @Override
    public List<SoReturnInstockDetailEntity> listDetailBySourceDetailIds(List<String> sourceDetailIds) {
        return baseMapper.listDetailBySourceDetailIds(sourceDetailIds);
    }

    @Override
    public List<SoReturnInstockDetailEntity> listDetailBySoReturnDetailIds(List<String> soReturnDetailIds) {
        if(CollectionUtils.isEmpty(soReturnDetailIds)){
            return new ArrayList<>();
        }
        List<SoReturnInstockDetailEntity> detailEntityList = lambdaQuery().in(SoReturnInstockDetailEntity::getSoReturnDetailId, soReturnDetailIds).list();
        if(CollectionUtils.isEmpty(detailEntityList)){
            return new ArrayList<>();
        }
        List<String> mainIdList = detailEntityList.stream().map(SoReturnInstockDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<SoReturnInstockEntity> mainList = soReturnInstockService.listByIds(mainIdList);
        detailEntityList = detailEntityList.stream().filter(v->{
            SoReturnInstockEntity main = mainList.stream().filter(t->t.getId().equals(v.getMainId())).findFirst().orElse(new SoReturnInstockEntity());
            return Objects.isNull(main.getInvalidStatus()) || !main.getInvalidStatus();
        }).collect(Collectors.toList());

        return detailEntityList;
    }

    @Override
    public List<SoReturnInstockDetailEntity> getSoReturnInstockByReturnIds(List<String> returnIds) {
        if(CollectionUtils.isEmpty(returnIds)){
            return new ArrayList<>();
        }
        return baseMapper.getSoReturnInstockByReturnIds(returnIds);
    }

    /**
     * @description: 更新委外标识
     * @author Will
     * @date: 2023/8/29 11:26
     * @param list
     */
    private void updateSubContract (List<SoReturnInstockDetailEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> skuIdList = list.stream().map(SoReturnInstockDetailEntity::getSkuId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> detailList = scmTaskFeign.getLatestByCrtTime(skuIdList);
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        for (SoReturnInstockDetailEntity entity : list) {
            PurchaseOrderDetailEntity detailEntity = detailList.stream().filter(obj -> obj.getSkuId().equals(entity.getSkuId())).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(detailEntity) && SubcontractTypeEnum.ENUM_PARENT.getCode().equals(detailEntity.getSubcontractType()) ) {
                entity.setIsSubContract(Boolean.TRUE);
            }
        }
    }
}
