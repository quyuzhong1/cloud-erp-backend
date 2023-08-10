package com.erp.server.wms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.SoReturnInstockDTO;
import com.erp.model.wms.dto.SoReturnInstockDetailDTO;
import com.erp.model.wms.dto.SoReturnReceiveDTO;
import com.erp.model.wms.dto.SoReturnReceiveDetailDTO;
import com.erp.model.wms.entity.SoReturnInstockDetailEntity;
import com.erp.model.wms.entity.SoReturnReceiveDetailEntity;
import com.erp.rpc.oms.feign.SoReturnFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.SoReturnInstockDetailMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.QcInfoService;
import com.erp.server.wms.service.SoReturnInstockDetailService;
import com.erp.server.wms.service.SoReturnReceiveDetailService;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 销售退货入库单明细表 服务实现类
 *
 * @author LUO_WG
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
    private QcInfoService qcInfoService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(SoReturnInstockDTO.Add dto, String id) {
        if (StringUtils.isNotBlank(dto.getSoReturnId())) {
            //获取退货单详情表id
            List<String> returnDetailIds = dto.getDetailList().stream().map(SoReturnInstockDetailDTO.Add::getSourceDetailId).collect(Collectors.toList());
            List<SoReturnDetailEntity> soReturnDetailEntities = soReturnFeign.listDetailByIds(returnDetailIds);
            List<String> returnIds = soReturnDetailEntities.stream().map(req -> req.getMainId()).distinct().collect(Collectors.toList());
            List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = soReturnReceiveDetailService.listDetailBySourceIds(returnIds);
            List<SoReturnInstockDetailEntity> soReturnInstockDetailEntities = this.listDetailBySourceDetailIds(returnDetailIds);
            List<SoReturnInstockDetailEntity> list = new ArrayList<>();
            for (SoReturnInstockDetailDTO.Add detailDto : dto.getDetailList()) {
                SoReturnInstockDetailEntity detailEntity = new SoReturnInstockDetailEntity();
                SoReturnDetailEntity soReturnDetailEntity = soReturnDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(soReturnDetailEntity)) {
                    throw new ServiceException(ApiError.ERROR_92023);
                }
                //实退数量
                Integer realQty = soReturnInstockDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnInstockDetailEntity::getRealQty).reduce(MathUtil.ZERO, Integer::sum);
                //签收单数量
                Integer receiveQty = soReturnReceiveDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                if (receiveQty < detailDto.getRealQty() + realQty) {
                    throw new ServiceException(ApiError.ERROR_92045);
                }
                detailEntity.setMainId(id);
                detailEntity.setSkuId(soReturnDetailEntity.getSkuId());
                detailEntity.setSkuNo(soReturnDetailEntity.getSkuNo());
                detailEntity.setRealQty(detailDto.getRealQty());
                detailEntity.setReceiveQty(detailDto.getReceiveQty());
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
                detailEntity.setWarehouseLocation(detailDto.getWarehouseLocation());
                detailEntity.setRemark(detailDto.getRemark());
                detailEntity.setSourceDetailId(detailDto.getSourceDetailId());
                detailEntity.setSoReturnDetailId(detailDto.getSoReturnDetailId());
                list.add(detailEntity);
            }
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
        List<String> skuIds = dto.getDetailList().stream().map(SoReturnInstockDetailDTO.Add::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuInfoByIds = plmTaskFeign.getSkuInfoByIds(skuIds);
        //获取退货签收单详情表id
        List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = soReturnReceiveDetailService.listDetailByMainIds(Arrays.asList(dto.getSourceId()));
        List<SoReturnInstockDetailEntity> soReturnInstockDetailEntities = this.listDetailBySourceIds(Arrays.asList(dto.getSourceId()));
        List<SoReturnInstockDetailEntity> list = new ArrayList<>();
        for (SoReturnInstockDetailDTO.Add detailDto : dto.getDetailList()) {
            SoReturnInstockDetailEntity detailEntity = new SoReturnInstockDetailEntity();
            //实退数量
            Integer realQty = soReturnInstockDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnInstockDetailEntity::getRealQty).reduce(MathUtil.ZERO, Integer::sum);
            //签收单数量
            Integer receiveQty = soReturnReceiveDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            if (receiveQty < detailDto.getRealQty() + realQty) {
                throw new ServiceException(ApiError.ERROR_92045);
            }
            detailEntity.setMainId(id);
            detailEntity.setSkuId(detailDto.getSkuId());
            SkuVO skuVO = skuInfoByIds.stream().filter(req -> req.getSkuId().equals(detailDto.getSkuId())).findFirst().orElse(new SkuVO());
            detailEntity.setSkuNo(skuVO.getSkuNo());
            detailEntity.setRealQty(detailDto.getRealQty());
            detailEntity.setReceiveQty(detailDto.getReceiveQty());
            detailEntity.setReturnTypeDict(detailDto.getReturnTypeDict());
            detailEntity.setReturnReasonDict(detailDto.getReturnReasonDict());
            detailEntity.setWarehouseLocation(detailDto.getWarehouseLocation());
            detailEntity.setRemark(detailDto.getRemark());
            detailEntity.setSourceDetailId(detailDto.getSourceDetailId());
            list.add(detailEntity);
        }
        return this.saveBatch(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SoReturnInstockDTO.Update dto) {
        List<String> addList = dto.getDetailList().stream().filter(c -> StringUtils.isBlank(c.getId())).map(SoReturnInstockDetailDTO.Update::getId).collect(Collectors.toList());
        if (StringUtils.isNotBlank(dto.getSoReturnId())) {
            //获取退货单详情表id
            List<String> returnDetailIds = dto.getDetailList().stream().map(SoReturnInstockDetailDTO.Update::getSourceDetailId).collect(Collectors.toList());
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
            for (SoReturnInstockDetailDTO.Update detailDto : dto.getDetailList()) {
                SoReturnInstockDetailEntity detailEntity = new SoReturnInstockDetailEntity();
                SoReturnDetailEntity soReturnDetailEntity = soReturnDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(soReturnDetailEntity)) {
                    throw new ServiceException(ApiError.ERROR_92045);
                }
                //实退 入库数量
                Integer realQty = soReturnInstockDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnInstockDetailEntity::getRealQty).reduce(MathUtil.ZERO, Integer::sum);
                //签收单数量
                Integer receiveQty = soReturnReceiveDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                if (StringUtils.isNotBlank(detailDto.getId())) {
                    detailEntity.setId(detailDto.getId());
                    realQty = soReturnInstockDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId()) && !req.getId().equals(detailDto.getId())).map(SoReturnInstockDetailEntity::getRealQty).reduce(MathUtil.ZERO, Integer::sum);
                }
                if (receiveQty < detailDto.getRealQty() + realQty) {
                    throw new ServiceException(ApiError.ERROR_92026);
                }
                detailEntity.setMainId(dto.getId());
                detailEntity.setSkuId(soReturnDetailEntity.getSkuId());
                detailEntity.setSkuNo(soReturnDetailEntity.getSkuNo());
                detailEntity.setRealQty(detailDto.getRealQty());
                detailEntity.setReceiveQty(detailDto.getReceiveQty());
                detailEntity.setWarehouseLocation(detailDto.getWarehouseLocation());
                detailEntity.setRemark(detailDto.getRemark());
                detailEntity.setSourceDetailId(detailDto.getSourceDetailId());
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
        List<String> addList = dto.getDetailList().stream().filter(c -> StringUtils.isBlank(c.getId())).map(SoReturnInstockDetailDTO.Update::getId).collect(Collectors.toList());
        List<String> skuIds = dto.getDetailList().stream().map(SoReturnInstockDetailDTO.Update::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuInfoByIds = plmTaskFeign.getSkuInfoByIds(skuIds);
        //获取退货签收单详情表id
        List<String> sourceDetailIds = dto.getDetailList().stream().map(SoReturnInstockDetailDTO.Update::getSourceDetailId).collect(Collectors.toList());
        List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = soReturnReceiveDetailService.listDetailByIds(sourceDetailIds);
        List<SoReturnInstockDetailEntity> soReturnInstockDetailEntities = this.listDetailBySourceDetailIds(sourceDetailIds);
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
                throw new ServiceException(ApiError.ERROR_92026);
            }
            detailEntity.setMainId(dto.getId());
            detailEntity.setSkuId(detailEntity.getSkuId());
            SkuVO skuVO = skuInfoByIds.stream().filter(req -> req.getSkuId().equals(detailDto.getSkuId())).findFirst().orElse(new SkuVO());
            detailEntity.setSkuNo(skuVO.getSkuNo());
            detailEntity.setRealQty(detailDto.getRealQty());
            detailEntity.setReceiveQty(detailDto.getReceiveQty());
            detailEntity.setWarehouseLocation(detailDto.getWarehouseLocation());
            detailEntity.setRemark(detailDto.getRemark());
            detailEntity.setSourceDetailId(detailDto.getSourceDetailId());
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
    public List<SoReturnInstockDetailEntity> listDetailBySourceDetailIds(List<String> sourceDetailIds) {
        return baseMapper.listDetailBySourceDetailIds(sourceDetailIds);
    }
}
