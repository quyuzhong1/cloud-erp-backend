package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.SoB2cReturnDetailEntity;
import com.erp.model.oms.entity.SoB2cReturnEntity;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.SoReturnReceiveDTO;
import com.erp.model.wms.dto.SoReturnReceiveDetailDTO;
import com.erp.model.wms.entity.SoReturnNoticeDetailEntity;
import com.erp.model.wms.entity.SoReturnReceiveDetailEntity;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.oms.feign.SoReturnFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.SoReturnReceiveDetailMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 销售退货签收单明细表 服务实现类
 * @author Luo_WG
 * @since 2023-05-10
 */
@Service
public class SoReturnReceiveDetailServiceImpl extends SuperServiceImpl<SoReturnReceiveDetailMapper, SoReturnReceiveDetailEntity> implements SoReturnReceiveDetailService {
    @Resource
    private SoInfoFeign soInfoFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SoReturnFeign soReturnFeign;

    @Resource
    private SoDeliveryNoticeDetailService soDeliveryNoticeDetailService;

    @Resource
    private SoOutstockDetailService soOutstockDetailService;

    @Resource
    private SoReturnNoticeDetailService soReturnNoticeDetailService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private SkuMappingFeign skuMappingFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean add(SoReturnReceiveDTO.Add dto, String id) {
        List<SoReturnReceiveDetailEntity> list = new ArrayList<>();
        //如果有退货订单号
        if (CharSequenceUtil.isBlank(dto.getSourceId())) {
            return notReturnOrderAdd(dto, id);
        } else {
            List<String> returnDetailIds = dto.getDetailList().stream().map(SoReturnReceiveDetailDTO.Add::getSourceDetailId).collect(Collectors.toList());
            List<SoReturnDetailEntity> soReturnDetailEntities = soReturnFeign.listDetailByIds(returnDetailIds);
            SoB2cReturnEntity soB2cReturnEntity = FeignQuery.getById(SoB2cReturnEntity.class,dto.getSourceId());
            List<SoB2cReturnDetailEntity> soB2cReturnDetailEntityList = FeignQuery.getByIds(SoB2cReturnDetailEntity.class,returnDetailIds);
            List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = this.listDetailBySourceIds(Collections.singletonList(dto.getSourceId()));

            List<String> noticeDetailIds = dto.getDetailList().stream().map(SoReturnReceiveDetailDTO.Add::getNoticeDetailId).collect(Collectors.toList());
            List<SoReturnNoticeDetailEntity> soReturnNoticeDetailEntities = soReturnNoticeDetailService.listByIds(noticeDetailIds);

            for (SoReturnReceiveDetailDTO.Add detailDto : dto.getDetailList()) {
                SoReturnReceiveDetailEntity detailEntity = new SoReturnReceiveDetailEntity();
                //退货通知单详情
                SoReturnNoticeDetailEntity soReturnNoticeDetailEntity = soReturnNoticeDetailEntities.stream().filter(v -> v.getId().equals(detailDto.getNoticeDetailId())).findFirst().orElse(new SoReturnNoticeDetailEntity());
                //获取平台sku
                detailEntity.setPlatformSkuNo(soReturnNoticeDetailEntity.getPlatformSkuNo());
                detailEntity.setIsChildSkuNo(detailDto.getIsChildSkuNo());
                detailEntity.setMainId(id);

                detailEntity.setSkuId(detailDto.getSkuId());
                detailEntity.setSkuNo(detailDto.getSkuNo());
                detailEntity.setReturnQty(detailDto.getReturnQty());
                detailEntity.setReceiveQty(detailDto.getReceiveQty());
                detailEntity.setRemark(detailDto.getRemark());
                detailEntity.setSourceDetailId(detailDto.getSourceDetailId());
                detailEntity.setNoticeDetailId(detailDto.getNoticeDetailId());
                Integer returnQty = soReturnDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
                if("B2C".equals(dto.getType())){
                    returnQty = soB2cReturnDetailEntityList.stream().filter(v->v.getId().equals(detailDto.getSourceDetailId())).map(SoB2cReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
                }
                //子产品无需校验
                if(StringUtils.isNotBlank(detailDto.getSourceDetailId())){
                    //此单历史签收数量
                    Integer historyReceiveQty = soReturnReceiveDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                    if (detailDto.getReceiveQty() + historyReceiveQty > returnQty) {
                        throw new ServiceException(ApiError.ERROR_92020);
                    }
                }
                if("B2C".equals(dto.getType())){
                    if(Objects.nonNull(soB2cReturnEntity)){
                        detailEntity.setReturnTypeDict(soB2cReturnEntity.getType());
                        detailEntity.setReturnReasonDict(soB2cReturnEntity.getReason());
                    }
                }else{
                    if(StringUtils.isNotBlank(detailDto.getSourceDetailId())){
                        SoReturnDetailEntity soReturnDetailEntity = soReturnDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(null);
                        if (ObjectUtil.isEmpty(soReturnDetailEntity)) {
                            throw new ServiceException(ApiError.ERROR_92023, detailDto.getSkuNo());
                        }
                    }
                    detailEntity.setReturnTypeDict(soReturnNoticeDetailEntity.getReturnTypeDict());
                    detailEntity.setReturnReasonDict(soReturnNoticeDetailEntity.getReturnReasonDict());
                }
                list.add(detailEntity);
            }
            return this.saveBatch(list);
        }
    }

    /**
     * 无退货订单新增
     * @Author Luo_WG
     * @Date 2023/7/24 14:47
     * @param dto
     * @param id
     * @return java.util.List<com.erp.model.wms.entity.SoReturnReceiveDetailEntity>
     **/
    private Boolean notReturnOrderAdd(SoReturnReceiveDTO.Add dto, String id) {
        List<String> skuIds = dto.getDetailList().stream().map(SoReturnReceiveDetailDTO.Add::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuInfoByIds = plmTaskFeign.listSkuProductByIds(skuIds);
        //sku映射表
        SkuMappingDTO.SkuParamDTO skuParamDTO = new SkuMappingDTO.SkuParamDTO();
        skuParamDTO.setCutomerId(dto.getCustomerId());
        List<String> skuNos = skuInfoByIds.stream().map(SkuVO::getSkuNo).collect(Collectors.toList());
        skuParamDTO.setSkuNoList(skuNos);
        List<SkuMappingDTO.ProductSkuInfoDTO> productSkuInfoList = skuMappingFeign.listSkuBySkuNos(skuParamDTO);
        List<SoReturnReceiveDetailEntity> list = new ArrayList<>();
        for (SoReturnReceiveDetailDTO.Add detailDto : dto.getDetailList()) {
            SoReturnReceiveDetailEntity detailEntity = new SoReturnReceiveDetailEntity();
            SkuVO skuVO = skuInfoByIds.stream().filter(req -> req.getSkuId().equals(detailDto.getSkuId())).findFirst().orElse(new SkuVO());
            detailEntity.setMainId(id);
            detailEntity.setSkuId(detailDto.getSkuId());
            detailEntity.setSkuNo(skuVO.getSkuNo());
            detailEntity.setReturnQty(detailDto.getReturnQty());
            detailEntity.setReceiveQty(detailDto.getReceiveQty());
            detailEntity.setRemark(detailDto.getRemark());
            detailEntity.setSourceDetailId(detailDto.getSourceDetailId());
            detailEntity.setReturnTypeDict(detailDto.getReturnTypeDict());
            detailEntity.setReturnReasonDict(detailDto.getReturnReasonDict());
            String platformSkuNo = productSkuInfoList.stream().filter(v -> v.getSkuNo().equals(skuVO.getSkuNo())).map(SkuMappingDTO.ProductSkuInfoDTO::getPlatformSkuNo).findFirst().orElse("");
            detailEntity.setPlatformSkuNo(platformSkuNo);
            list.add(detailEntity);
        }
        this.saveBatch(list);
        //标记SKU
        plmTaskFeign.updateOccupyStatus(skuIds);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean update(SoReturnReceiveDTO.Update dto) {
        List<String> addList = dto.getDetailList().stream().filter(c -> CharSequenceUtil.isBlank(c.getId())).map(SoReturnReceiveDetailDTO.Update::getId).collect(Collectors.toList());
        //如果有退货订单号
        if (CharSequenceUtil.isNotBlank(dto.getSourceId())) {
            //获取退货单详情表id
            List<String> returnDetailIds = dto.getDetailList().stream().map(SoReturnReceiveDetailDTO.Update::getSourceDetailId).collect(Collectors.toList());
            List<SoReturnDetailEntity> soReturnDetailEntities = soReturnFeign.listDetailByIds(returnDetailIds);
            List<SoB2cReturnDetailEntity> soB2cReturnDetailEntityList = FeignQuery.getByIds(SoB2cReturnDetailEntity.class,returnDetailIds);
            List<SoReturnReceiveDetailEntity> list = new ArrayList<>();
            List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = this.listDetailBySourceIds(Collections.singletonList(dto.getSourceId()));
            //原明细数据
            List<SoReturnReceiveDetailEntity> oldList = this.listDetailByMainId(dto.getId());
            List<String> deleteIds = getDeleteIds(dto.getDetailList(), oldList);
            if (CollectionUtils.isNotEmpty(deleteIds)) {
                List<SoReturnReceiveDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
                //操作日志
                List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
                operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(),pairList,"编辑操作");
                this.removeByIds(deleteIds);
            }
            for (SoReturnReceiveDetailDTO.Update detailDto : dto.getDetailList()) {
                SoReturnReceiveDetailEntity detailEntity = new SoReturnReceiveDetailEntity();

                //此单历史签收数量
                Integer historyReceiveQty = soReturnReceiveDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                if (CharSequenceUtil.isNotBlank(detailDto.getId())) {
                    detailEntity.setId(detailDto.getId());
                    historyReceiveQty = soReturnReceiveDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId()) && !req.getId().equals(detailDto.getId())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                }
                if("B2C".equals(dto.getType())){
                    SoB2cReturnDetailEntity soReturnDetailEntity = soB2cReturnDetailEntityList.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(null);
                    if (ObjectUtil.isEmpty(soReturnDetailEntity)) {
                        throw new ServiceException(ApiError.ERROR_92023, detailDto.getSkuNo());
                    }
                    Integer returnQty = soB2cReturnDetailEntityList.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).map(SoB2cReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
                    if (detailDto.getReceiveQty() + historyReceiveQty > returnQty) {
                        throw new ServiceException(ApiError.ERROR_92020);
                    }
                    detailEntity.setSkuId(soReturnDetailEntity.getSkuId());
                    detailEntity.setSkuNo(soReturnDetailEntity.getSkuNo());
                }else{
                    SoReturnDetailEntity soReturnDetailEntity = soReturnDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(null);
                    if (ObjectUtil.isEmpty(soReturnDetailEntity)) {
                        throw new ServiceException(ApiError.ERROR_92023, detailDto.getSkuNo());
                    }
                    Integer returnQty = soReturnDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
                    if (detailDto.getReceiveQty() + historyReceiveQty > returnQty) {
                        throw new ServiceException(ApiError.ERROR_92020);
                    }
                    detailEntity.setSkuId(soReturnDetailEntity.getSkuId());
                    detailEntity.setSkuNo(soReturnDetailEntity.getSkuNo());
                }

                detailEntity.setId(detailDto.getId());
                detailEntity.setMainId(dto.getId());
                detailEntity.setReturnQty(detailDto.getReturnQty());
                detailEntity.setReceiveQty(detailDto.getReceiveQty());
                detailEntity.setRemark(detailDto.getRemark());
                detailEntity.setSourceDetailId(detailDto.getSourceDetailId());
                detailEntity.setReturnTypeDict(detailDto.getReturnTypeDict());
                detailEntity.setReturnReasonDict(detailDto.getReturnReasonDict());
                //获取平台sku
                String platformSkuNo = soReturnDetailEntities.stream()
                        .filter(v -> v.getId().equals(detailDto.getSourceDetailId()))
                        .map(SoReturnDetailEntity::getPlatformSkuNo).findFirst().orElse("");
                detailEntity.setPlatformSkuNo(platformSkuNo);
                list.add(detailEntity);
                //修改操作日志
                if (CharSequenceUtil.isNotBlank(detailEntity.getId())) {
                    SoReturnReceiveDetailEntity old = this.getById(detailEntity.getId());
                    operateLogService.addModuleOperateLogByObj(old,detailEntity, ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(),dto.getId(),"",String.format("【%s】",old.getSkuNo()));
                }
            }
            //添加操作日志
            if (CollectionUtils.isNotEmpty(addList)) {
                List<SoReturnReceiveDetailEntity> returnNoticeDetailEntities = this.listByIds(addList);
                List<Pair<String, String>> addPairList = returnNoticeDetailEntities.stream().map(obj -> new Pair<>(dto.getId(), obj.getSkuNo())).collect(Collectors.toList());
                operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(), addPairList, "编辑操作");
            }
            return this.saveOrUpdateBatch(list);
        } else {
            return notReturnOrderUpdate(dto);
        }
    }

    /**
     * 无退货订单修改
     * @Author Luo_WG
     * @Date 2023/7/24 14:47
     * @param dto
     * @return java.util.List<com.erp.model.wms.entity.SoReturnReceiveDetailEntity>
     **/
    private Boolean notReturnOrderUpdate(SoReturnReceiveDTO.Update dto) {
        List<String> addList = dto.getDetailList().stream().filter(c -> CharSequenceUtil.isBlank(c.getId())).map(SoReturnReceiveDetailDTO.Update::getId).collect(Collectors.toList());

        List<SoReturnReceiveDetailEntity> list = new ArrayList<>();
        List<String> skuIds = dto.getDetailList().stream().map(SoReturnReceiveDetailDTO.Update::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuInfoByIds = plmTaskFeign.listSkuProductByIds(skuIds);

        //原明细数据
        List<SoReturnReceiveDetailEntity> oldList = this.listDetailByMainId(dto.getId());
        List<String> deleteIds = getDeleteIds(dto.getDetailList(), oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<SoReturnReceiveDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }
        //sku对照表
        SkuMappingDTO.SkuParamDTO skuParamDTO = new SkuMappingDTO.SkuParamDTO();
        skuParamDTO.setCutomerId(dto.getCustomerId());
        List<String> skuNos = skuInfoByIds.stream().map(SkuVO::getSkuNo).collect(Collectors.toList());
        skuParamDTO.setSkuNoList(skuNos);
        List<SkuMappingDTO.ProductSkuInfoDTO> productSkuInfoList = skuMappingFeign.listSkuBySkuNos(skuParamDTO);

        for (SoReturnReceiveDetailDTO.Update detailDto : dto.getDetailList()) {
            SoReturnReceiveDetailEntity detailEntity = new SoReturnReceiveDetailEntity();
            SkuVO skuVO = skuInfoByIds.stream().filter(req -> req.getSkuId().equals(detailDto.getSkuId())).findFirst().orElse(new SkuVO());
            detailEntity.setId(detailDto.getId());
            detailEntity.setMainId(dto.getId());
            detailEntity.setSkuId(detailDto.getSkuId());
            detailEntity.setSkuNo(skuVO.getSkuNo());
            detailEntity.setReturnQty(detailDto.getReturnQty());
            detailEntity.setReceiveQty(detailDto.getReceiveQty());
            detailEntity.setRemark(detailDto.getRemark());
            detailEntity.setSourceDetailId(detailDto.getSourceDetailId());
            detailEntity.setReturnTypeDict(detailDto.getReturnTypeDict());
            detailEntity.setReturnReasonDict(detailDto.getReturnReasonDict());
            String platformSkuNo = productSkuInfoList.stream().filter(v -> v.getSkuNo().equals(skuVO.getSkuNo())).map(SkuMappingDTO.ProductSkuInfoDTO::getPlatformSkuNo).findFirst().orElse("");
            detailEntity.setPlatformSkuNo(platformSkuNo);
            list.add(detailEntity);
            //修改操作日志
            if (CharSequenceUtil.isNotBlank(detailEntity.getId())) {
                SoReturnReceiveDetailEntity old = this.getById(detailEntity.getId());
                operateLogService.addModuleOperateLogByObj(old,detailEntity, ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(),dto.getId(),"",String.format("【%s】",old.getSkuNo()));
            }
        }
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<SoReturnReceiveDetailEntity> returnNoticeDetailEntities = this.listByIds(addList);
            List<Pair<String, String>> addPairList = returnNoticeDetailEntities.stream().map(obj -> new Pair<>(dto.getId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(), addPairList, "编辑操作");
        }
        this.saveOrUpdateBatch(list);
        //标记SKU
        plmTaskFeign.updateOccupyStatus(skuIds);
        return Boolean.TRUE;
    }

    private List<String> getDeleteIds(List<SoReturnReceiveDetailDTO.Update> newList, List<SoReturnReceiveDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> CharSequenceUtil.isNotBlank(g.getId())).
                map(SoReturnReceiveDetailDTO.Update::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(SoReturnReceiveDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> mainIds) {
        return lambdaUpdate().set(SoReturnReceiveDetailEntity::getIsDeleted, Boolean.TRUE)
                .in(SoReturnReceiveDetailEntity::getMainId, mainIds)
                .remove();
    }

    @Override
    public List<SoReturnReceiveDetailEntity> listDetailBySourceIds(List<String> sourceIds) {
        if (CollectionUtils.isEmpty(sourceIds)) {
            return new ArrayList<>();
        }
        return baseMapper.listDetailBySourceIds(sourceIds);
    }

    @Override
    public List<SoReturnReceiveDetailEntity> listDetailBySourceDetailIds(List<String> sourceDetailIds) {
        if (CollectionUtils.isEmpty(sourceDetailIds)) {
            return new ArrayList<>();
        }
        return baseMapper.listDetailBySourceDetailIds(sourceDetailIds);
    }

    @Override
    public List<SoReturnReceiveDetailEntity> listDetailByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        return baseMapper.listDetailByIds(ids);
    }

    @Override
    public List<SoReturnReceiveDetailEntity> listDetailByMainId(String id) {
        return lambdaQuery().eq(SoReturnReceiveDetailEntity::getMainId, id).list();
    }

    @Override
    public List<SoReturnReceiveDetailEntity> listDetailByMainIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        return baseMapper.listDetailByMainIds(ids);
    }

}
