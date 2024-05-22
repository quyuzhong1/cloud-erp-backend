package com.erp.server.oms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.SkuInfoSimpleVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.oms.convert.B2cOrderConverter;
import com.erp.server.oms.mapper.SoB2cMapper;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * B2C销售订单表 拆分操作服务类
 * </p>
 *
 */
@Service
@Slf4j
public class SoB2cSplitServiceImpl extends SuperServiceImpl<SoB2cMapper, SoB2cEntity> implements SoB2cSplitService {

    @Resource
    private SoB2cService soB2cService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private SoB2cDetailService soB2cDetailService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    @Lazy
    private SoB2cSplitServiceImpl service;

    @Resource
    private SkuMappingService skuMappingService;

    @Override
    public List<BatchResultDTO> bomSplitAndSave(List<String> ids) {
        List<SoB2cEntity> soB2cEntityList = this.listByIds(ids);
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainIds(ids);
        List<String> skuIds = soB2cDetailEntityList.stream().map(v->v.getSkuId()).distinct().collect(Collectors.toList());
        //根据SKU查询BOM判断是否是组合SKU
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listBomChildBySkuIds(skuIds);
        bomChildrenList = bomChildrenList.stream().filter(b ->  BomTypeEnum.COMBINATION.getType().equals(b.getType())).collect(Collectors.toList());
        List<String> isCombinationSkuIds = bomChildrenList.stream().map(BomChildrenSkuDTO::getParentSkuId).collect(Collectors.toList());
        List<String> childSkuList = bomChildrenList.stream().map(BomChildrenSkuDTO::getSkuId).collect(Collectors.toList());
        List<SkuInfoSimpleVO> skuInfoSimpleVOList = plmTaskFeign.getSimpleSkuInfoByIds(childSkuList);
        soB2cDetailEntityList = soB2cDetailEntityList.stream().filter(v->isCombinationSkuIds.contains(v.getSkuId())).collect(Collectors.toList());
        List<BatchResultDTO> batchResultDTOList = new ArrayList<>();
        List<String> removeDetailIds = new ArrayList<>();
        List<SoB2cDetailEntity> addAllDetailList = new ArrayList<>();
        for(SoB2cEntity soB2cEntity : soB2cEntityList){
            List<SoB2cDetailEntity> detailList = soB2cDetailEntityList.stream().filter(v->v.getMainId().equals(soB2cEntity.getId())).collect(Collectors.toList());
            if(!SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode().equals(soB2cEntity.getBillStatus()) && !SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(soB2cEntity.getBillStatus())){
                batchResultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(),soB2cEntity.getCode(),"只有待配货和配货中可以拆分"));
                continue;
            }
            if(CollectionUtils.isEmpty(detailList)){
                batchResultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(),soB2cEntity.getCode(),"拆分失败，没有组合明细"));
                continue;
            }

            for(SoB2cDetailEntity detailEntity: detailList){
                removeDetailIds.add(detailEntity.getId());
                List<BomChildrenSkuDTO> bomChildrenSkuDTOList = bomChildrenList.stream().filter(b->b.getParentSkuId().equals(detailEntity.getSkuId())).collect(Collectors.toList());
                BigDecimal totalCostPrice = BigDecimal.ZERO;
                BigDecimal remainAmount = detailEntity.getAmount();
                BigDecimal remainAdvicePrice = detailEntity.getAdvicePrice();
                List<SoB2cDetailEntity> addDetailList = new ArrayList<>();
                for (BomChildrenSkuDTO bomChildrenSkuDTO : bomChildrenSkuDTOList) {
                    SoB2cDetailEntity addDetailEntity = B2cOrderConverter.INSTANCE.cloneSoB2cDetail(detailEntity);
                    addDetailEntity.setSplitDetailId(detailEntity.getId());
                    addDetailEntity.setImageUrl(bomChildrenSkuDTO.getImageUrl());
                    addDetailEntity.setSkuId(bomChildrenSkuDTO.getSkuId());
                    addDetailEntity.setSkuNo(bomChildrenSkuDTO.getSkuNo());
                    addDetailEntity.setQty(detailEntity.getQty() * bomChildrenSkuDTO.getQuantity());
                    addDetailEntity.setPlatformSkuNo("");
                    addDetailEntity.setPlatformSpuNo("");
                    addDetailEntity.setWarehouseSkuNo("");
                    SkuInfoSimpleVO skuInfoSimpleVO = skuInfoSimpleVOList.stream().filter(v->v.getSkuId().equals(bomChildrenSkuDTO.getSkuId())).findFirst().orElse(new SkuInfoSimpleVO());
                    //含税单价
                    BigDecimal costPrice = ObjectUtils.isEmpty(skuInfoSimpleVO.getActualTaxCost()) ? skuInfoSimpleVO.getTargetTaxCost() : skuInfoSimpleVO.getActualTaxCost();
                    if(Objects.isNull(costPrice)){
                        costPrice = BigDecimal.ZERO;
                    }
                    addDetailEntity.setTaxCost(costPrice);
                    totalCostPrice = totalCostPrice.add(costPrice);
                    addDetailList.add(addDetailEntity);
                }
                for (int i = 0; i < addDetailList.size(); i++) {
                    SoB2cDetailEntity soB2cDetailEntity = addDetailList.get(i);
                    //如果是最后一行 赋值剩余的金额
                    if(i == addDetailList.size() - 1){
                        soB2cDetailEntity.setAmount(remainAmount);
                        soB2cDetailEntity.setAdvicePrice(remainAdvicePrice);
                    }else if (soB2cDetailEntity.getTaxCost().compareTo(BigDecimal.ZERO) == 0){
                        soB2cDetailEntity.setAmount(BigDecimal.ZERO);
                        soB2cDetailEntity.setAdvicePrice(BigDecimal.ZERO);
                    }else if (totalCostPrice.compareTo(BigDecimal.ZERO) == 0){
                        soB2cDetailEntity.setAmount(BigDecimal.ZERO);
                        soB2cDetailEntity.setAdvicePrice(BigDecimal.ZERO);
                    }else{
                        //原捆绑商品真实售价金额*（单个SKU含税成本/总的SKU含税成本），最后一个订单明细行显示最后剩余的真实售价金额
                        BigDecimal amount = soB2cDetailEntity.getTaxCost().divide(totalCostPrice,4, RoundingMode.HALF_UP).multiply(detailEntity.getAmount());
                        soB2cDetailEntity.setAmount(amount);
                        remainAmount = remainAmount.subtract(amount);
                        //原捆绑商品建议售价金额*（单个SKU含税成本/总的SKU含税成本），最后一个订单明细行显示最后剩余的建议售价金额
                        BigDecimal advancePrice = soB2cDetailEntity.getTaxCost().divide(totalCostPrice,4, RoundingMode.HALF_UP).multiply(detailEntity.getAdvicePrice());
                        soB2cDetailEntity.setAdvicePrice(advancePrice);
                        remainAdvicePrice = remainAdvicePrice.subtract(advancePrice);
                    }
                    soB2cDetailEntity.setPrice(soB2cDetailEntity.getAmount().divide(new BigDecimal(soB2cDetailEntity.getQty()),4, RoundingMode.HALF_UP));
                }

                //封装平台sku信息
                //SKU对照表信息
                List<SkuMappingDTO.ListSkuParamDTO> listParamList = addDetailList.stream().map(obj -> new SkuMappingDTO.ListSkuParamDTO(obj.getSkuNo(), detailEntity.getWarehouseId(),soB2cEntity.getDictPlatform())).collect(Collectors.toList());
                ValidList<SkuMappingDTO.ListSkuParamDTO> listSkuParamList = new ValidList<>();
                listSkuParamList.setList(listParamList);
                List<SkuMappingDTO.ListSkuDTO> SkuMappingList = skuMappingService.listBySkuNoList(listSkuParamList);
                for (SoB2cDetailEntity soB2cDetailEntity : addDetailList) {
                    //库存SKU
                    SkuMappingDTO.ListSkuDTO warehouseListSkuDTO = SkuMappingList.stream().filter(obj -> obj.getProductSkuId().equals(soB2cDetailEntity.getSkuId()) && obj.getWarehouseId().equals(soB2cDetailEntity.getWarehouseId())).findFirst().orElse(null);
                    if (ObjectUtils.isNotEmpty(warehouseListSkuDTO)) {
                        soB2cDetailEntity.setWarehouseSkuNo(StrUtil.isBlank(warehouseListSkuDTO.getWarehouseSkuNo()) ? "" : warehouseListSkuDTO.getWarehouseSkuNo());
                    } else {
                        soB2cDetailEntity.setWarehouseSkuNo("");
                    }
                    //平台SKU
                    SkuMappingDTO.ListSkuDTO platformListSkuDTO = SkuMappingList.stream().filter(obj -> obj.getProductSkuId().equals(soB2cDetailEntity.getSkuId()) && obj.getDictPlatform().equals(soB2cEntity.getDictPlatform())).findFirst().orElse(null);
                    if (ObjectUtils.isNotEmpty(platformListSkuDTO)) {
                        // 非平台下载的订单
                        if (!SourceTypeEnum.SO_B2C.getCode().equalsIgnoreCase(soB2cEntity.getSourceType())) {
                            soB2cDetailEntity.setPlatformSkuNo(platformListSkuDTO.getPlatformSkuNo());
                            soB2cDetailEntity.setPlatformSpuNo(platformListSkuDTO.getPlatformSpuNo());
                        }
                    } else {
                        soB2cDetailEntity.setPlatformSkuNo("");
                        soB2cDetailEntity.setPlatformSpuNo("");
                    }
                }
                addAllDetailList.addAll(addDetailList);
            }
            // 操作日志
            String msg = StrUtil.format("用户【{}】操作捆绑拆分", UserContext.getDefaultLoginUser().getUserName());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), "捆绑拆分");
        }
        service.batchHandleTransaction(removeDetailIds,addAllDetailList,new ArrayList<>());
        return batchResultDTOList;
    }

    @Override
    public List<BatchResultDTO> bomRestoreAndSave(List<String> ids) {
        List<SoB2cEntity> soB2cEntityList = this.listByIds(ids);
        List<SoB2cDetailEntity> allSoB2cDetailEntityList = soB2cDetailService.listByMainIds(ids);
        allSoB2cDetailEntityList = allSoB2cDetailEntityList.stream().filter(v->StringUtils.isNotBlank(v.getSplitDetailId())).collect(Collectors.toList());
        List<String> splitDetailIds =  allSoB2cDetailEntityList.stream().map(SoB2cDetailEntity::getSplitDetailId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        //原来
        List<SoB2cDetailEntity> allOriginDetailList = soB2cDetailService.listContainDeleted(splitDetailIds);
        List<String> removeDetailIds = new ArrayList<>();
        List<String> revertDetailIds = new ArrayList<>();
        List<BatchResultDTO> batchResultDTOList = new ArrayList<>();
        for (SoB2cEntity soB2cEntity : soB2cEntityList) {
            if(!SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode().equals(soB2cEntity.getBillStatus()) && !SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(soB2cEntity.getBillStatus())){
                batchResultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(),soB2cEntity.getCode(),"只有待配货和配货中可以拆分"));
                continue;
            }
            List<SoB2cDetailEntity> detailList = allSoB2cDetailEntityList.stream().filter(v->v.getMainId().equals(soB2cEntity.getId())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(detailList)){
                batchResultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(),soB2cEntity.getCode(),"还原捆绑拆分失败，没有已拆分的明细"));
                continue;
            }
            List<String> originIds = detailList.stream().map(v->v.getSplitDetailId()).collect(Collectors.toList());
            List<SoB2cDetailEntity> originDetailList = allOriginDetailList.stream().filter(v->originIds.contains(v.getId())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(originDetailList)){
                batchResultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(),soB2cEntity.getCode(),"还原捆绑拆分失败，没有待还原的数据"));
                continue;
            }
            removeDetailIds.addAll(detailList.stream().map(v->v.getId()).collect(Collectors.toList()));
            revertDetailIds.addAll(originDetailList.stream().map(v->v.getId()).collect(Collectors.toList()));

            // 操作日志
            String msg = StrUtil.format("用户【{}】操作还原捆绑拆分", UserContext.getDefaultLoginUser().getUserName());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), "还原捆绑拆分");
        }
        service.batchHandleTransaction(removeDetailIds,new ArrayList<>(),revertDetailIds);
        return batchResultDTOList;
    }

    @Override
    public List<SoB2cEntity> listRefBomSplit(String detailId) {
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listBySplitId(detailId);
        List<String> mainIds = soB2cDetailEntityList.stream().map(SoB2cDetailEntity::getMainId).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(mainIds)){
            return new ArrayList<>();
        }
        return listByIds(mainIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchHandleTransaction(List<String> removeDetailIds, List<SoB2cDetailEntity> addDetailList, List<String> revertDetailIds){
        if(CollectionUtils.isNotEmpty(removeDetailIds)){
            soB2cDetailService.removeByIds(removeDetailIds);
        }
        if(CollectionUtils.isNotEmpty(addDetailList)){
            soB2cDetailService.saveBatch(addDetailList);
        }
        if(CollectionUtils.isNotEmpty(revertDetailIds)){
            soB2cDetailService.updateContainDeleted(revertDetailIds);
        }
    }
}
