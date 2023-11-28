package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.validator.ValidList;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.dto.SoB2cDetailDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.oms.convert.B2cOrderConsumerConverter;
import com.erp.server.oms.mapper.SoB2cDetailMapper;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SkuMappingService;
import com.erp.server.oms.service.SoB2cDetailService;
import com.erp.server.oms.service.SoB2cService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * B2C销售订单明细表 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
@Slf4j
@Service
public class SoB2cDetailServiceImpl extends SuperServiceImpl<SoB2cDetailMapper, SoB2cDetailEntity> implements SoB2cDetailService {

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private SkuMappingService skuMappingService;

    @Resource
    private SoB2cService soB2cService;

    @Override
    public Boolean add(List<SoB2cDetailDTO.AddDTO> detailList, String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
           throw new ServiceException(ApiError.ERROR_1040, SourceTypeEnum.SO_B2C.getName());
        }
        List<SoB2cDetailEntity> list = BeanMapperUtils.copyList(SoB2cDetailEntity.class, detailList);
        //处理明细中的数据id
        handleDetailList(list,mainId,Boolean.TRUE);
        //批量新增
        boolean flag = this.saveBatch(list);
        return flag;
    }

    @Override
    public Boolean update(List<SoB2cDetailDTO.UpdateDTO> detailList, String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_1040, SourceTypeEnum.SO_B2C.getName());
        }
        //原明细数据
        List<SoB2cDetailEntity> oldList = this.listByMainIds(Arrays.asList(mainId));
        List<String> deleteIds = getDeleteIds(detailList, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<SoB2cDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.SO_B2C.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }

        List<SoB2cDetailEntity> list = BeanMapperUtils.copyList(SoB2cDetailEntity.class, detailList);
        //处理明细中的数据id
        handleDetailList(list,mainId,Boolean.FALSE);
        return this.saveOrUpdateBatch(list);
    }

    @Override
    public List<SoB2cDetailEntity> listByMainId(String mainId) {
        return lambdaQuery().eq(SoB2cDetailEntity::getMainId,mainId).list();
    }

    @Override
    public List<SoB2cDetailEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(SoB2cDetailEntity::getMainId,mainIds).list();
    }

    @Override
    public Boolean updateWarehouseIdByMainId(String mainId, String warehouseId) {
        //仓库信息
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(warehouseId));
        if (CollectionUtils.isEmpty(warehouseList)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        WarehouseDTO.UpdateDTO updateDTO = warehouseList.get(0);
        //组织信息
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(updateDTO.getOrgId()));
        if (CollectionUtils.isEmpty(accountingCompanyList)) {
            throw new ServiceException(ApiError.ERROR_WAREHOUSE_NOT_EXIST_ORG,updateDTO.getName());
        }
        BaseIdDTO.CodeDTO codeDTO = accountingCompanyList.get(0);
        return lambdaUpdate()
                .eq(SoB2cDetailEntity::getMainId,mainId)
                .set(SoB2cDetailEntity::getWarehouseId,warehouseId)
                .set(SoB2cDetailEntity::getWarehouseName,updateDTO.getName())
                .set(SoB2cDetailEntity::getWarehouseOrgId,updateDTO.getOrgId())
                .set(SoB2cDetailEntity::getWarehouseOrgName,codeDTO.getName())
                .update(new SoB2cDetailEntity())
                ;
    }

    @Override
    public Boolean deleteByMainIds(List<String> mainIds) {
        return lambdaUpdate().in(SoB2cDetailEntity::getMainId,mainIds).remove();
    }

    @Override
    public Boolean updateWarehouse(List<SoB2cDetailEntity> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        List<String> warehouseIdList = detailList.stream().map(SoB2cDetailEntity::getWarehouseId).collect(Collectors.toList());
        //仓库信息
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(warehouseIdList);
        if (CollectionUtils.isEmpty(warehouseList)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        List<String> orgIdList = warehouseList.stream().map(WarehouseDTO.UpdateDTO::getOrgId).collect(Collectors.toList());
        //组织信息
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIdList);
        if (CollectionUtils.isEmpty(accountingCompanyList)) {
            throw new ServiceException(ApiError.ERROR_WAREHOUSE_NOT_EXIST_ORG,warehouseList.stream().map(WarehouseDTO.UpdateDTO::getName).collect(Collectors.joining(",")));
        }
        for (SoB2cDetailEntity entity : detailList) {
            WarehouseDTO.UpdateDTO warehouseDTO = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getWarehouseId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(warehouseDTO)) {
                throw new ServiceException(ApiError.ERROR_99002);
            }
            entity.setWarehouseName(warehouseDTO.getName());
            entity.setWarehouseOrgId(warehouseDTO.getOrgId());
            BaseIdDTO.CodeDTO codeDTO = accountingCompanyList.stream().filter(obj -> obj.getId().equals(warehouseDTO.getOrgId())).findFirst().orElse(null);
            entity.setWarehouseOrgName(codeDTO.getName());
        }
        return this.updateBatchById(detailList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public List<SoB2cDetailEntity> saveOrUpdateEntity(PlatformOrderDTO dto, SoB2cEntity mainEntity) {
        // 订单明细
        List<SoB2cDetailEntity> oldDetailEntityList = this.listByMainId(mainEntity.getId());
        // 来源为空
        if(CollectionUtils.isEmpty(dto.getDetails())){
            if (CollectionUtils.isEmpty(oldDetailEntityList)){
                // 新建空
                SoB2cDetailEntity detailEntity = B2cOrderConsumerConverter.INSTANCE.convertNewDetail(null, mainEntity.getId());
                if(!this.save(detailEntity)){
                    throw new ServiceException("[SoB2cDetailEntity] 保存失败");
                }
                return Collections.singletonList(detailEntity);
            }
            return oldDetailEntityList;
        }
        // 来源不为空
        // 历史map
        Map<String, SoB2cDetailEntity> oldDetailMap = oldDetailEntityList.stream().collect(Collectors.toMap(SoB2cDetailEntity::getSourceDetailId, Function.identity()));
        // 新增或更新列表
        List<SoB2cDetailEntity> saveOrUpdateList = dto.getDetails().stream().map(detailDTO -> {
            // 历史记录
            SoB2cDetailEntity oldEntity = oldDetailMap.get(detailDTO.getSourceDetailId());
            SoB2cDetailEntity saveOrUpdateEntity;
            if (null != oldEntity) {
                // 更新指定内容
                saveOrUpdateEntity = B2cOrderConsumerConverter.INSTANCE.convertUpdateDetail(oldEntity, detailDTO);
            } else {
                // 新记录
                saveOrUpdateEntity = B2cOrderConsumerConverter.INSTANCE.convertNewDetail(detailDTO, mainEntity.getId());
            }
            return saveOrUpdateEntity;
        }).collect(Collectors.toList());

        // 批量保存和更新
        if (!this.saveOrUpdateBatch(saveOrUpdateList)){
            throw new ServiceException(" [SoB2cDetailEntity] 订单明细批量更新或保存失败");
        }
        return saveOrUpdateList;
    }


    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<SoB2cDetailDTO.UpdateDTO> newList, List<SoB2cDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(SoB2cDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(SoB2cDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * @description: 明细处理
     * @author Will
     * @date: 2023/8/22 11:06
     * @param list
     * @param mainId
     * @param isAdd
     */
    private void handleDetailList (List<SoB2cDetailEntity> list,String mainId,Boolean isAdd) {

        //主表信息
        SoB2cEntity soB2cEntity = soB2cService.getById(mainId);
        if (ObjectUtils.isEmpty(soB2cEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }

        //产品信息
        List<String> skuIds = list.stream().map(SoB2cDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            log.error("未找到SKU，warehouseIds = {}",skuList);
            throw new ServiceException(ApiError.ERROR_95084);
        }

        //仓库信息
        List<String> warehouseIdList = list.stream().map(SoB2cDetailEntity::getWarehouseId).collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(warehouseIdList);
        if (CollectionUtils.isEmpty(warehouseList)) {
            log.error("未找到仓库，warehouseIdList = {}",warehouseIdList);
            throw new ServiceException(ApiError.ERROR_99002);
        }
        List<String> orgIdList = warehouseList.stream().map(WarehouseDTO.UpdateDTO::getOrgId).collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIdList);
        if (CollectionUtils.isEmpty(accountingCompanyList)) {
            log.error("未找到核算公司，orgIdList = {}",orgIdList);
            throw new ServiceException(ApiError.ERROR_9014);
        }


        //SKU对照表信息
        List<SkuMappingDTO.ListSkuParamDTO> listParamList = list.stream().map(obj -> new SkuMappingDTO.ListSkuParamDTO(skuList.stream().filter(e -> e.getSkuId().equals(obj.getSkuId())).findFirst().flatMap(e -> Optional.ofNullable(e.getSkuNo())).orElse(""), obj.getWarehouseId(),soB2cEntity.getDictPlatform())).collect(Collectors.toList());
        ValidList<SkuMappingDTO.ListSkuParamDTO> listSkuParamList = new ValidList<>();
        listSkuParamList.setList(listParamList);
        List<SkuMappingDTO.ListSkuDTO> SkuMappingList = skuMappingService.listBySkuNoList(listSkuParamList);

        for (SoB2cDetailEntity detailEntity :list) {

            //产品信息
            SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(detailEntity.getSkuId())).findFirst()
                    .orElse(null);
            if (ObjectUtils.isEmpty(skuVO)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            detailEntity.setMainId(mainId);
            detailEntity.setSkuNo(skuVO.getSkuNo());
            detailEntity.setCurrency(soB2cEntity.getCurrency());
            detailEntity.setExchangeRate(soB2cEntity.getExchangeRate());
            detailEntity.setImageUrl(skuVO.getSkuImagesUrl());
            //建议售价
            BigDecimal advicePrice = MathUtil.multiply(skuVO.getRetailPrice(), detailEntity.getQty());
            detailEntity.setAdvicePrice(advicePrice);
            //含税单价
            BigDecimal costPrice = ObjectUtils.isEmpty(skuVO.getActualTaxCost()) ? skuVO.getTargetTaxCost() : skuVO.getActualTaxCost();
            detailEntity.setTaxCost(MathUtil.multiply(costPrice,detailEntity.getQty()));

            //仓库名称
            WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream()
                    .filter(obj -> obj.getId().equals(detailEntity.getWarehouseId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(updateDTO)) {
                log.error("未找到仓库，warehouseId = {}",detailEntity.getWarehouseId());
                throw new ServiceException(ApiError.ERROR_99002);
            }
            detailEntity.setWarehouseName(updateDTO.getName());

            //库存组织
            BaseIdDTO.CodeDTO companyDTO = accountingCompanyList.stream()
                    .filter(obj -> obj.getId().equals(updateDTO.getOrgId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(companyDTO)) {
                log.error("未找到核算公司，orgId = {}",updateDTO.getOrgId());
                throw new ServiceException(ApiError.ERROR_9014);
            }
            detailEntity.setWarehouseOrgId(updateDTO.getOrgId());
            detailEntity.setWarehouseOrgName(companyDTO.getName());
            detailEntity.setAmount(MathUtil.multiply(detailEntity.getPrice(),detailEntity.getQty()));

            //库存SKU
            SkuMappingDTO.ListSkuDTO warehouseListSkuDTO = SkuMappingList.stream().filter(obj -> obj.getProductSkuId().equals(detailEntity.getSkuId()) && obj.getWarehouseId().equals(detailEntity.getWarehouseId())).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(warehouseListSkuDTO)) {
                detailEntity.setWarehouseSkuNo(warehouseListSkuDTO.getWarehouseSkuNo());
            }
            //平台SKU
            SkuMappingDTO.ListSkuDTO platformListSkuDTO = SkuMappingList.stream().filter(obj -> obj.getProductSkuId().equals(detailEntity.getSkuId()) && obj.getDictPlatform().equals(soB2cEntity.getDictPlatform())).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(platformListSkuDTO)) {
                detailEntity.setSellerSkuNo(platformListSkuDTO.getSellerSkuNo());
                detailEntity.setPlatformSkuNo(platformListSkuDTO.getPlatformSkuNo());
            }

            //操作日志
            if (StringUtils.isNotBlank(detailEntity.getId())) {
                SoB2cDetailEntity old = this.getById(detailEntity.getId());
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_98026);
                }
                operateLogService.addModuleOperateLogByObj(old,detailEntity, ModuleTypeEnum.SO_B2C.getCode(),old.getMainId(),"",String.format("【%s】",old.getSkuNo()));
            }
        }

        //添加操作日志
        List<SoB2cDetailEntity> addList = list.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        //新增不需要添加新增SKU的日志
        if (CollectionUtils.isNotEmpty(addList) && !isAdd) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("新增了一条SKU【%s】", ModuleTypeEnum.SO_B2C.getCode(), addPairList, "编辑操作");
        }
    }
}
