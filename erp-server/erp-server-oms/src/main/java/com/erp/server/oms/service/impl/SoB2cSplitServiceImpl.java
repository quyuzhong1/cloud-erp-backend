package com.erp.server.oms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.annotation.DataIdempotent;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cInvalidTypeEnum;
import com.erp.model.oms.enums.SoB2cOptionTypeEnum;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.SkuInfoSimpleVO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.oms.convert.B2cOrderConverter;
import com.erp.server.oms.mapper.SoB2cMapper;
import com.erp.server.oms.service.*;
import com.sdk.oms.tiktok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tiktok.dto.tiktok.split.PackagesBean;
import com.sdk.oms.tiktok.dto.tiktok.split.PlatformSplitViewDTO;
import com.sdk.oms.tiktok.dto.tiktok.split.SplitAttributesBean;
import com.sdk.oms.tiktok.dto.tiktok.split.SplitAttributesDTO;
import com.sdk.oms.tiktok.service.TikTokSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @Resource
    private SoB2cRefService soB2cRefService;

    @Resource
    private TikTokSdkClientService tikTokSdkClientService;

    @Resource
    private SoB2cLogisticsService soB2cLogisticsService;

    @Resource
    private SoB2cReceiverService soB2cReceiverService;

    @Resource
    private SoB2cRefCategoryService soB2cRefCategoryService;

    @Override
    public List<SoB2cDetailDTO.ViewDTO> getBomSplitInfo(List<String> ids) {
        List<SoB2cDetailEntity> detailEntityList = soB2cDetailService.listContainDeleted(ids);
        if(CollectionUtils.isEmpty(detailEntityList)){
            throw new ServiceException("明细为空");
        }
        List<String> skuIds = detailEntityList.stream().map(v->v.getSkuId()).collect(Collectors.toList());
        List<SoB2cDetailDTO.ViewDTO> resultList = new ArrayList<>();
        //根据SKU查询BOM判断是否是组合SKU
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listBomChildBySkuIds(skuIds);
        for (SoB2cDetailEntity detailEntity : detailEntityList) {
            String combination = BomTypeEnum.COMBINATION.getType();
            bomChildrenList = bomChildrenList.stream().filter(b -> combination.equals(b.getType())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(bomChildrenList)){
                throw new ServiceException("不是销售套装组合品，无法拆分");
            }
            List<String> childSkuList = bomChildrenList.stream().map(v->v.getSkuId()).collect(Collectors.toList());
            List<SkuInfoSimpleVO> skuInfoSimpleVOList = plmTaskFeign.getSimpleSkuInfoByIds(childSkuList);
            BigDecimal totalCostPrice = BigDecimal.ZERO;
            BigDecimal remainAmount = detailEntity.getAmount();
            BigDecimal remainAdvicePrice = detailEntity.getAdvicePrice();
            for (BomChildrenSkuDTO bomChildrenSkuDTO : bomChildrenList) {
                SoB2cDetailDTO.ViewDTO viewDTO = new SoB2cDetailDTO.ViewDTO();
                viewDTO.setMainId(detailEntity.getMainId());
                viewDTO.setSplitDetailId(detailEntity.getId());
                viewDTO.setImageUrl(bomChildrenSkuDTO.getImageUrl());
                viewDTO.setSkuId(bomChildrenSkuDTO.getSkuId());
                viewDTO.setSkuNo(bomChildrenSkuDTO.getSkuNo());
                viewDTO.setProductName(bomChildrenSkuDTO.getSkuName());
                viewDTO.setQty(detailEntity.getQty() * bomChildrenSkuDTO.getQuantity());
                viewDTO.setSourcePlatform(detailEntity.getSourcePlatform());
                viewDTO.setWarehouseId(detailEntity.getWarehouseId());
                viewDTO.setWarehouseName(detailEntity.getWarehouseName());
                viewDTO.setSourceDetailId(detailEntity.getSourceDetailId());
                viewDTO.setExchangeRate(detailEntity.getExchangeRate());
                SkuInfoSimpleVO skuInfoSimpleVO = skuInfoSimpleVOList.stream().filter(v->v.getSkuId().equals(bomChildrenSkuDTO.getSkuId())).findFirst().orElse(new SkuInfoSimpleVO());
                //含税单价
                BigDecimal costPrice = ObjectUtils.isEmpty(skuInfoSimpleVO.getActualTaxCost()) ? skuInfoSimpleVO.getTargetTaxCost() : skuInfoSimpleVO.getActualTaxCost();
                if(Objects.isNull(costPrice)){
                    costPrice = BigDecimal.ZERO;
                }
                viewDTO.setTaxCost(costPrice);
                totalCostPrice = totalCostPrice.add(costPrice);
                //平台SKU
                viewDTO.setPlatformSkuNo(detailEntity.getPlatformSkuNo());
                viewDTO.setPlatformSpuNo(detailEntity.getPlatformSpuNo());
                resultList.add(viewDTO);
            }
            for (int i = 0; i < resultList.size(); i++) {
                SoB2cDetailDTO.ViewDTO viewDTO = resultList.get(i);
                //如果是最后一行 赋值剩余的金额
                if(i == resultList.size() - 1){
                    viewDTO.setAmount(remainAmount);
                    viewDTO.setAdvicePrice(remainAdvicePrice);
                }else if (viewDTO.getTaxCost().compareTo(BigDecimal.ZERO) == 0){
                    viewDTO.setAmount(BigDecimal.ZERO);
                    viewDTO.setAdvicePrice(BigDecimal.ZERO);
                }else if (totalCostPrice.compareTo(BigDecimal.ZERO) == 0){
                    viewDTO.setAmount(BigDecimal.ZERO);
                    viewDTO.setAdvicePrice(BigDecimal.ZERO);
                }else{
                    //原捆绑商品真实售价金额*（单个SKU含税成本/总的SKU含税成本），最后一个订单明细行显示最后剩余的真实售价金额
                    BigDecimal amount = viewDTO.getTaxCost().divide(totalCostPrice,4, RoundingMode.HALF_UP).multiply(detailEntity.getAmount());
                    viewDTO.setAmount(amount);
                    remainAmount = remainAmount.subtract(amount);
                    //原捆绑商品建议售价金额*（单个SKU含税成本/总的SKU含税成本），最后一个订单明细行显示最后剩余的建议售价金额
                    BigDecimal advancePrice = viewDTO.getTaxCost().divide(totalCostPrice,4, RoundingMode.HALF_UP).multiply(detailEntity.getAdvicePrice());
                    viewDTO.setAdvicePrice(advancePrice);
                    remainAdvicePrice = remainAdvicePrice.subtract(advancePrice);
                }
                viewDTO.setPrice(viewDTO.getAmount().divide(new BigDecimal(viewDTO.getQty()),4, RoundingMode.HALF_UP));
            }
        }
        return resultList;
    }

    @Override
    @DataIdempotent(keyIdName = "ids")
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
                List<SkuMappingDTO.ListSkuParamDTO> listParamList = addDetailList.stream().map(obj -> new SkuMappingDTO.ListSkuParamDTO(obj.getSkuNo(), detailEntity.getWarehouseId(),soB2cEntity.getDictPlatform(),soB2cEntity.getShopId())).collect(Collectors.toList());
                ValidList<SkuMappingDTO.ListSkuParamDTO> listSkuParamList = new ValidList<>();
                listSkuParamList.setList(listParamList);
                List<SkuMappingDTO.ListSkuDTO> SkuMappingList = skuMappingService.listBySkuNoList(listSkuParamList);
                for (SoB2cDetailEntity soB2cDetailEntity : addDetailList) {
                    //库存SKU
                    List<SkuMappingDTO.ListSkuDTO> warehouseListSkuDTO = SkuMappingList.stream().filter(obj -> obj.getProductSkuId().equals(soB2cDetailEntity.getSkuId()) && obj.getWarehouseId().equals(soB2cDetailEntity.getWarehouseId())).collect(Collectors.toList());
                    if (warehouseListSkuDTO.size() == 1) {
                        soB2cDetailEntity.setWarehouseSkuNo(StrUtil.isBlank(warehouseListSkuDTO.get(0).getWarehouseSkuNo()) ? "" : warehouseListSkuDTO.get(0).getWarehouseSkuNo());
                    } else {
                        soB2cDetailEntity.setWarehouseSkuNo("");
                    }

                    soB2cDetailEntity.setPlatformSkuNo(detailEntity.getPlatformSkuNo());
                    soB2cDetailEntity.setPlatformSpuNo(detailEntity.getPlatformSpuNo());
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
        List<String> originDetailSplitIds = allOriginDetailList.stream().map(SoB2cDetailEntity::getSplitDetailId).collect(Collectors.toList());
        allOriginDetailList = allOriginDetailList.stream().filter(v->!originDetailSplitIds.contains(v.getId())).collect(Collectors.toList());
        splitDetailIds = splitDetailIds.stream().filter(v->!originDetailSplitIds.contains(v)).collect(Collectors.toList());
        List<SoB2cDetailEntity> sameSplitDetailList = soB2cDetailService.listBySplitId(splitDetailIds);
        List<String> sameSplitMainIds = sameSplitDetailList.stream().map(SoB2cDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<SoB2cEntity> sameMainList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(sameSplitMainIds)){
            sameMainList = this.listByIds(sameSplitMainIds);
        }
        //过滤掉自动作废的
        sameMainList = sameMainList.stream().filter(v->!SoB2cInvalidTypeEnum.ENUM_AUTOMATIC.getCode().equals(v.getInvalidType())).collect(Collectors.toList());
        List<String> removeDetailIds = new ArrayList<>();
        List<String> revertDetailIds = new ArrayList<>();
        List<SoB2cDetailEntity> addDetailList = new ArrayList<>();
        List<BatchResultDTO> batchResultDTOList = new ArrayList<>();
        for (SoB2cEntity soB2cEntity : soB2cEntityList) {
            if(!SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode().equals(soB2cEntity.getBillStatus()) && !SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(soB2cEntity.getBillStatus())){
                batchResultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(),soB2cEntity.getCode(),"只有待配货和配货中可以拆分还原"));
                continue;
            }
            List<SoB2cDetailEntity> detailList = allSoB2cDetailEntityList.stream().filter(v->v.getMainId().equals(soB2cEntity.getId())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(detailList)){
                batchResultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(),soB2cEntity.getCode(),"还原捆绑拆分失败，没有已拆分的明细"));
                continue;
            }
            List<String> splitIds = detailList.stream().map(SoB2cDetailEntity::getSplitDetailId).distinct().collect(Collectors.toList());
            //判断有没有不同订单 otherDetailList: 不同订单相同明细
            List<SoB2cDetailEntity> otherDetailList = sameSplitDetailList.stream().filter(v->splitIds.contains(v.getSplitDetailId()) && !v.getMainId().equals(soB2cEntity.getId())).collect(Collectors.toList());
            List<String> otherMainIds = otherDetailList.stream().map(SoB2cDetailEntity::getMainId).distinct().collect(Collectors.toList());
            List<SoB2cEntity> otherMainList = sameMainList.stream().filter(v->otherMainIds.contains(v.getId())).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(otherMainList)){
                //一开始可以不同订单做还原捆绑，现在不允许，所以下面对不同订单的处理逻辑不会走
                batchResultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(),soB2cEntity.getCode(),"还原捆绑失败，因为需要的明细不在同个订单下"));
                continue;
            }
            //校验别的订单的状态
            List<SoB2cEntity> notPassMainList = otherMainList.stream().filter(v->v.getIsFrozen() || v.getInvalidStatus() || (!SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode().equals(v.getBillStatus()) && !SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(v.getBillStatus()))).collect(Collectors.toList());
            //不通过，封装错误信息返回
            if(CollectionUtils.isNotEmpty(notPassMainList)){
                StringBuilder sb = new StringBuilder();
                for (SoB2cEntity notPassMain : notPassMainList) {
                    List<SoB2cDetailEntity> notPassDetailList = otherDetailList.stream().filter(v->v.getMainId().equals(notPassMain.getId())).collect(Collectors.toList());
                    for(SoB2cDetailEntity soB2cDetailEntity : notPassDetailList){
                        sb.append(StrUtil.format("捆绑子件{}数量{}关联的销售订单{}状态为{}，不允许还原捆绑",soB2cDetailEntity.getSkuNo(),soB2cDetailEntity.getQty(),notPassMain.getCode(),notPassMain.getIsFrozen()?"已冻结":notPassMain.getInvalidStatus()?"已作废":SoB2cBillStatusEnum.getName(notPassMain.getBillStatus())));
                    }
                }
                batchResultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(),soB2cEntity.getCode(),sb.toString()));
                continue;
            }
            List<SoB2cDetailEntity> originDetailList = allOriginDetailList.stream().filter(v->splitIds.contains(v.getId())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(originDetailList)){
                batchResultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(),soB2cEntity.getCode(),"还原捆绑拆分失败，没有待还原的数据"));
                continue;
            }

            //删除的明细明细（可能有不同订单）不能删除原订单（比如订单A里面有捆绑拆分的sku1和sku2，订单拆分成订单B和订单C，订单B操作还原捆绑，不能删除订单A的明细）
            List<String> excludeMainIds = otherMainList.stream().map(BaseEntity::getId).collect(Collectors.toList());
            excludeMainIds.add(soB2cEntity.getId());
            List<String> removeDetails = sameSplitDetailList.stream().filter(v->excludeMainIds.contains(v.getMainId()) && splitIds.contains(v.getSplitDetailId())).map(BaseEntity::getId).collect(Collectors.toList());
            removeDetailIds.addAll(removeDetails);
            //还原的明细，分两种情况，如果要还原的明细的订单与当前订单相同，直接将明细还原，如果不同，将明细复制一条到当前订单
            for (SoB2cDetailEntity soB2cDetailEntity : originDetailList) {
                if(soB2cDetailEntity.getMainId().equals(soB2cEntity.getId())){
                    revertDetailIds.add(soB2cDetailEntity.getId());
                }else{
                    SoB2cDetailEntity addDetail = B2cOrderConverter.INSTANCE.cloneSoB2cDetail(soB2cDetailEntity);
                    addDetail.setMainId(soB2cEntity.getId());
                    addDetail.setIsDeleted(false);
                    addDetailList.add(addDetail);
                }
            }
            List<String> initSkuList = originDetailList.stream().map(SoB2cDetailEntity::getInitSkuId).filter(StrUtil::isNotBlank).collect(Collectors.toList());
            resetIsChangeSkuFlag(initSkuList,soB2cEntity);
            // 操作日志
            String msg = StrUtil.format("用户【{}】操作还原捆绑拆分", UserContext.getDefaultLoginUser().getUserName());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), "还原捆绑拆分");
        }
        service.batchHandleTransaction(removeDetailIds,addDetailList,revertDetailIds);
        return batchResultDTOList;
    }

    private void resetIsChangeSkuFlag(List<String> initSkuList, SoB2cEntity soB2cEntity) {
        if (Objects.nonNull(soB2cEntity.getIsChangeSku()) && soB2cEntity.getIsChangeSku() && CollectionUtils.isEmpty(initSkuList)){
            //订单存在更换sku标识，还原拆分订单时 还原订单状态
            soB2cService.updateIsChangeSku(Collections.singletonList(soB2cEntity.getId()), Boolean.FALSE);
        }
        if (Objects.nonNull(soB2cEntity.getIsChangeSku()) && !soB2cEntity.getIsChangeSku() && CollectionUtils.isNotEmpty(initSkuList)){
            //订单不存在更换sku标识，还原拆分订单时 子订单存在更换记录
            soB2cService.updateIsChangeSku(Collections.singletonList(soB2cEntity.getId()), Boolean.TRUE);
        }
    }

    @Override
    public SoB2cDTO.CombinationDTO listRefBomSplit(String detailId) {
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listBySplitId(Arrays.asList(detailId));
        List<String> mainIds = soB2cDetailEntityList.stream().map(SoB2cDetailEntity::getMainId).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(mainIds)){
            return SoB2cDTO.CombinationDTO.builder()
                    .soB2cEntityList(new ArrayList<>())
                    .soB2cDetailEntityList(new ArrayList<>())
                    .build();
        }
        List<SoB2cEntity> soB2cEntityList = listByIds(mainIds);
        return SoB2cDTO.CombinationDTO.builder()
                .soB2cEntityList(soB2cEntityList)
                .soB2cDetailEntityList(soB2cDetailEntityList)
                .build();
    }

    @Override
    public List<BatchResultDTO> splitOrderByWarehouse(List<String> ids) {
        List<SoB2cDTO.ViewSplitDTO> viewSplitDTOS = this.viewSplit(ids);
        //根据展示信息进行仓库组合
        List<SoB2cDTO.SplitSaveDTO> splitDTOS = buildSplitDtoByWarehouse(viewSplitDTOS);
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<String> allSoIdList = new ArrayList<>();
        for (SoB2cDTO.SplitSaveDTO dto : splitDTOS){
            BatchResultDTO result;
            try {
                SoB2cEntity entity = soB2cService.getById(dto.getId());
                //订单下的明细仓库一致，无法按仓库拆分
                if (dto.getGroupList().size() < 2){
                    result = BatchResultDTO.fail(dto.getId(),entity.getCode(),ApiError.ERROR_SO_B2C_ORDER_SPLIT_ON_WAREHOUSE.msg);
                }else {
                    SoB2cDTO.SplitSaveResultDTO resultDTO = service.splitSave(dto);
                    result = BatchResultDTO.success(dto.getId(),entity.getCode(),"订单拆分成功");
                    allSoIdList.addAll(resultDTO.getSoB2cIds());
                }
            } catch (Exception e) {
                log.error("B2C销售订单取消拆分失败", e);
                SoB2cEntity entity = soB2cService.getById(dto.getId());
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(dto.getId(), dto.getId(), "B2C销售订单不存在, 拆分保存失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        //原有逻辑
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(allSoIdList)) {
            for (String soId : allSoIdList) {
                try {
                    soB2cService.checkProductRegistrationAndUpdate(soId, "");
                } catch (Exception e) {
                    log.error("拆分保存后检查商品备案失败，soId:{}，异常信息{}", soId, e);
                }
            }
        }
        return resultDTOS;
    }

    @Override
    public List<SoB2cDetailDTO.ViewDTO> getBomRestoreInfo(List<String> ids) {
        if(CollectionUtils.isEmpty(ids)){
            return new ArrayList<>();
        }
        List<SoB2cDetailEntity> restoreEntity = soB2cDetailService.listContainDeleted(ids);
        if(Objects.isNull(restoreEntity)){
            throw new ServiceException("原明细为空");
        }
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listBySplitId(restoreEntity.stream().map(SoB2cDetailEntity::getId).collect(Collectors.toList()));
        SoB2cEntity soB2cEntity = this.getById(restoreEntity.get(0).getMainId());
        List<String> splitDetailIds = soB2cDetailEntityList.stream().map(SoB2cDetailEntity::getSplitDetailId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<SoB2cDetailEntity> sameSplitDetailList = soB2cDetailService.listBySplitId(splitDetailIds);
        List<String> allMainIds = sameSplitDetailList.stream().map(SoB2cDetailEntity::getMainId).collect(Collectors.toList());
        //相同明细对应的订单
        List<SoB2cEntity> allMainList = CollectionUtils.isEmpty(allMainIds)?new ArrayList<>():this.listByIds(sameSplitDetailList.stream().map(SoB2cDetailEntity::getMainId).collect(Collectors.toList()));
        //排除原单和自动作废
        List<SoB2cEntity> otherMainList = allMainList.stream().filter(v->!soB2cEntity.getId().equals(v.getId()) && !SoB2cInvalidTypeEnum.ENUM_AUTOMATIC.getCode().equals(v.getInvalidType())).collect(Collectors.toList());
        //大于1说明这个待还原明细在不同订单下
        if(otherMainList.size() > 1){
            //一开始可以不同订单做还原捆绑，现在不允许，所以下面对不同订单的处理逻辑不会走
            throw new ServiceException("还原捆绑失败，因为需要的明细不在同个订单下");
        }
        //校验别的订单的状态
        List<SoB2cEntity> notPassMainList = otherMainList.stream().filter(v->v.getIsFrozen() || v.getInvalidStatus() || (!SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode().equals(v.getBillStatus()) && !SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(v.getBillStatus()))).collect(Collectors.toList());
        //不通过，封装错误信息返回
        if(CollectionUtils.isNotEmpty(notPassMainList)){
            StringBuilder sb = new StringBuilder();
            for (SoB2cEntity notPassMain : notPassMainList) {
                List<SoB2cDetailEntity> notPassDetailList = sameSplitDetailList.stream().filter(v->v.getMainId().equals(notPassMain.getId())).collect(Collectors.toList());
                for(SoB2cDetailEntity soB2cDetailEntity : notPassDetailList){
                    sb.append(StrUtil.format("捆绑子件{}数量{}关联的销售订单{}状态为{}，不允许还原捆绑",soB2cDetailEntity.getSkuNo(),soB2cDetailEntity.getQty(),notPassMain.getCode(),notPassMain.getIsFrozen()?"已冻结":notPassMain.getInvalidStatus()?"已作废":SoB2cBillStatusEnum.getName(notPassMain.getBillStatus())));
                }
            }
            throw new ServiceException(sb.toString());
        }
        List<SoB2cDetailEntity> resultDetailList = new ArrayList<>();
        //还原的明细，分两种情况，如果要还原的明细的订单与当前订单相同，直接将明细还原，如果不同，将明细复制一条到当前订单
        for (SoB2cDetailEntity soB2cDetailEntity : restoreEntity) {
            resultDetailList.add(soB2cDetailEntity);
            soB2cDetailEntity.setRevertId(soB2cDetailEntity.getId());
        }

        List<SoB2cDetailDTO.ViewDTO> viewDTOList = BeanUtil.copyToList(resultDetailList,SoB2cDetailDTO.ViewDTO.class);
        List<String> skuIds = viewDTOList.stream().map(v->v.getSkuId()).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        viewDTOList.forEach(v->{
            v.setIsCombination(true);
            SkuVO skuVO = skuList.stream().filter(t->v.getSkuId().equals(t.getSkuId())).findFirst().orElse(null);
            if(Objects.nonNull(skuVO)){
                v.setProductName(skuVO.getSkuName());
            }
        });
        return viewDTOList;
    }

    /**
     * 根据仓库进行订单拆分
     * @param viewSplitDTOS
     * @return
     */
    private List<SoB2cDTO.SplitSaveDTO> buildSplitDtoByWarehouse(List<SoB2cDTO.ViewSplitDTO> viewSplitDTOS) {
        if (CollectionUtils.isEmpty(viewSplitDTOS)){
            return Collections.emptyList();
        }
        List<SoB2cDTO.SplitSaveDTO> splitSaveDTOS = new ArrayList<>(viewSplitDTOS.size());
        for (SoB2cDTO.ViewSplitDTO viewSplitDTO : viewSplitDTOS){
            SoB2cDTO.SplitSaveDTO splitSaveDTO = new SoB2cDTO.SplitSaveDTO();
            splitSaveDTO.setId(viewSplitDTO.getId());
            splitSaveDTO.setGroupList(groupSplitDtoByViewDto(viewSplitDTO.getDetailList()));
            splitSaveDTOS.add(splitSaveDTO);
        }
        return splitSaveDTOS;
    }

    /**
     * 根据仓库对订单进行分组
     * @param detailList
     * @return
     */
    private List<SoB2cDTO.GroupSplitSaveDTO> groupSplitDtoByViewDto(List<SoB2cDTO.ViewSplitDetailDTO> detailList) {
        List<SoB2cDTO.GroupSplitSaveDTO> groupSplitSaveDTOS = new ArrayList<>();
        //对于空仓库订单进行合并为同一组
        List<SoB2cDTO.ViewSplitDetailDTO> emptyWarehouseList = detailList.stream().filter(e -> StringUtils.isBlank(e.getWarehouseId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(emptyWarehouseList)){
            SoB2cDTO.GroupSplitSaveDTO groupSplitSaveDTO = new SoB2cDTO.GroupSplitSaveDTO();
            groupSplitSaveDTO.setDetailList(B2cOrderConverter.INSTANCE.convertViewToSplitDto(emptyWarehouseList));
            groupSplitSaveDTOS.add(groupSplitSaveDTO);
        }
        Map<String, List<SoB2cDTO.ViewSplitDetailDTO>> groupMap = detailList.stream().filter(e -> StringUtils.isNotBlank(e.getWarehouseId()))
                .collect(Collectors.groupingBy(SoB2cDTO.ViewSplitDetailDTO::getWarehouseId));
        if (!groupMap.isEmpty()){
            for (List<SoB2cDTO.ViewSplitDetailDTO> list :groupMap.values()){
                SoB2cDTO.GroupSplitSaveDTO groupSplitSaveDTO = new SoB2cDTO.GroupSplitSaveDTO();
                groupSplitSaveDTO.setDetailList(B2cOrderConverter.INSTANCE.convertViewToSplitDto(list));
                groupSplitSaveDTOS.add(groupSplitSaveDTO);
            }
        }
        return groupSplitSaveDTOS;
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


    @Override
    @DataIdempotent(keyIdName = "dto.id")
    public SoB2cDTO.SplitSaveResultDTO splitSave(SoB2cDTO.SplitSaveDTO dto) {
        //订单拆分字段处理
        SoB2cDTO.SplitSaveResultDTO resultDTO = service.splitSaveHandle(dto);
        List<SoB2cEntity> entityList = resultDTO.getNeedRuleIds();
        if(CollectionUtils.isNotEmpty(entityList)){
            for (SoB2cEntity entity : entityList) {
                //走仓库规则和物流规则的
                SoB2cDTO.RuleResultDTO warehouseRuleResult = new SoB2cDTO.RuleResultDTO();
                try {
                    //拉取订单正常处理
                    warehouseRuleResult = soB2cService.warehouseRule(entity.getId(), new ArrayList<>(), new HashMap<>());
                }catch (Exception e){
                    log.error("{}仓库规则异常",entity.getCode(),e);
                    warehouseRuleResult.setIsRuleMatch(false);
                }
                Boolean warehouseRuleMatch = warehouseRuleResult.getIsRuleMatch();
                if (warehouseRuleMatch) {
                    SoB2cDTO.RuleResultDTO logisticsRuleResult = new SoB2cDTO.RuleResultDTO();
                    try {
                        logisticsRuleResult = soB2cService.logisticsRule(entity.getId(), new HashMap<>(), true);
                    }catch (Exception e){
                        logisticsRuleResult.setAutoGetTrackNo(false);
                        log.error("{}物流规则异常",entity.getCode(),e);
                    }
                    Boolean autoGetTrackNo = logisticsRuleResult.getAutoGetTrackNo();
                    if (Objects.nonNull(autoGetTrackNo) && autoGetTrackNo) {
                        soB2cService.getLogisticsCode(entity.getId(), true);
                    }
                }
            }
        }
        //如果是TikTok平台拆分订单，需要同步到平台
        if (PlatformDictEnum.TIK_TOK.getCode().equals(resultDTO.getOldEntity().getDictPlatform())) {
            service.tikTokSplit(resultDTO);
        }
        return resultDTO;
    }

    @Transactional(rollbackFor = Exception.class)
    public SoB2cDTO.SplitSaveResultDTO splitSaveHandle(SoB2cDTO.SplitSaveDTO dto) {
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(dto.getId());
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //已拆分数据不能再次拆分
        List<SoB2cRefEntity> soB2cRefList = soB2cRefService.listBySourceIdOrTargetId(Arrays.asList(dto.getId()));
        //拆分后的销售订单集合
        List<String> soIdList = new ArrayList<>(5);
        List<String> soCodeList = new ArrayList<>(5);
        /**
         * 拆分后金额、费用根据金额比例进行分摊
         */
        //验证拆分数据
        checkSplitData(entity,soB2cRefList);
        //原单据明细
        List<SoB2cDetailEntity> oldDetailList = soB2cDetailService.listByMainId(dto.getId());
        if (CollectionUtils.isEmpty(oldDetailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        //物流信息
        SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsService.getByMainId(dto.getId());
        if (ObjectUtils.isEmpty(soB2cLogisticsEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        SoB2cLogisticsDTO.AddDTO logisticsAddDTO = new SoB2cLogisticsDTO.AddDTO();
        BeanMapperUtils.copy(soB2cLogisticsEntity, logisticsAddDTO);

        //买家信息
        SoB2cReceiverEntity soB2cReceiverEntity = soB2cReceiverService.getByMainId(dto.getId());
        if (ObjectUtils.isEmpty(soB2cReceiverEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_RECEIVER_NOT_EXIST);
        }
        SoB2cReceiverDTO.AddDTO receiverAddDTO = new SoB2cReceiverDTO.AddDTO();
        BeanMapperUtils.copy(soB2cReceiverEntity, receiverAddDTO);

        //订单分类
        List<SoB2cRefCategoryEntity> soB2cRefCategoryList = soB2cRefCategoryService.listByMainIds(Arrays.asList(dto.getId()));

        //原明细金额合计
        BigDecimal totalAmount = oldDetailList.stream().map(SoB2cDetailEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        //拆分后数据
        List<SoB2cDTO.GroupSplitSaveDTO> splitList = dto.getGroupList();
        if (MathUtil.ONE >= splitList.size()) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_SPLIT_SIZE);
        }
        Integer flag = MathUtil.ONE;
        //分组金额
        BigDecimal groupAmount = BigDecimal.ZERO;
        //分组预估费用
        BigDecimal groupEstimatedShippingCost = BigDecimal.ZERO;
        //分组实际费用
        BigDecimal groupActualShippingCost = BigDecimal.ZERO;
        //分组包装辅料费
        BigDecimal groupAccessoriesCost = BigDecimal.ZERO;
        //分组包装净重
        BigDecimal groupAccessoriesNw = BigDecimal.ZERO;
        //分组包装重量
        BigDecimal groupWeight = BigDecimal.ZERO;

        // 使用 Set 存储 platformSkuNo 值
        Set<String> platformSkuNoSet = new HashSet<>();

        //拆分同步tiktok入参
        OrderSplitPramDTO tikTokPramDTO = new OrderSplitPramDTO();
        List<SplittableGroupsBean> splittableGroups = new ArrayList<>();
        List<SoB2cEntity> needRuleList = new ArrayList<>();

        for (int i = 0; i < splitList.size(); i++) {
            SplittableGroupsBean groupsBean = new SplittableGroupsBean();

            SoB2cDTO.GroupSplitSaveDTO groupSplitSaveDTO = splitList.get(i);
            //新建拆分后数据
            SoB2cDTO.AddDTO addDTO = new SoB2cDTO.AddDTO();
            BeanMapperUtils.copy(entity, addDTO);
            addDTO.setSourceType(SourceTypeEnum.SELF_ADD.getCode());
            if (CollectionUtils.isNotEmpty(soB2cRefCategoryList)) {
                List<String> categoryIdList = soB2cRefCategoryList.stream().map(SoB2cRefCategoryEntity::getCategoryId).collect(Collectors.toList());
                addDTO.setCategoryIdList(categoryIdList);
            }
            addDTO.setReceiverDTO(receiverAddDTO);

            //拆分后金额合计
            BigDecimal splitTotalAmount = BigDecimal.ZERO;

            List<SoB2cDetailDTO.AddDTO> detailList = new ArrayList<>();
            for (SoB2cDTO.SplitDetailSaveDTO splitDetailSaveDTO : groupSplitSaveDTO.getDetailList()) {

                SoB2cDetailEntity detailEntity = oldDetailList.stream().filter(obj -> obj.getId().equals(splitDetailSaveDTO.getId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(detailEntity)) {
                    throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
                }
                if (MathUtil.compareTo(splitDetailSaveDTO.getQty(), detailEntity.getQty()) > MathUtil.ZERO) {
                    log.error("订单【{}】SKU【{}】拆分数量【{}】不能大于原数量【{}】", entity.getCode(), detailEntity.getSkuNo(), splitDetailSaveDTO.getQty(), detailEntity.getQty());
                    throw new ServiceException(ApiError.ERROR_SO_B2C_SPLIT_QTY, entity.getCode(), detailEntity.getSkuNo(), splitDetailSaveDTO.getQty(), detailEntity.getQty());
                }

                if (PlatformDictEnum.TIK_TOK.getCode().equals(entity.getDictPlatform())) {
                    String platformSkuNo = detailEntity.getPlatformSkuNo();
                    // 如果 platformSkuNo 已经存在于 Set 中，则表示重复
                    if (!platformSkuNoSet.add(platformSkuNo) && i != 0) {
                        throw new ServiceException(ApiError.ERROR_SO_B2C_TIKTOK_SPLIT_SKU, entity.getCode(), detailEntity.getSkuNo());
                    }
                }

                SoB2cDetailDTO.AddDTO addDetailDTO = new SoB2cDetailDTO.AddDTO();
                BeanMapperUtils.copy(detailEntity, addDetailDTO);
                addDetailDTO.setQty(splitDetailSaveDTO.getQty());
                addDetailDTO.setOperateDetailId(detailEntity.getId());
                detailList.add(addDetailDTO);
                //累加拆分金额
                splitTotalAmount = MathUtil.add(splitTotalAmount, MathUtil.multiply(detailEntity.getPrice(), splitDetailSaveDTO.getQty()));

            }
            addDTO.setDetailList(detailList);
            //拆分金额所占比例
            BigDecimal rate = MathUtil.divide(splitTotalAmount, totalAmount);
            BigDecimal amount = MathUtil.multiply(rate, entity.getAmount());
            BigDecimal estimatedShippingCost = MathUtil.multiply(rate, soB2cLogisticsEntity.getEstimatedShippingCost());
            BigDecimal actualShippingCost = MathUtil.multiply(rate, soB2cLogisticsEntity.getActualShippingCost());
            BigDecimal accessoriesCost = MathUtil.multiply(rate, soB2cLogisticsEntity.getAccessoriesCost());
            BigDecimal accessoriesNw = MathUtil.multiply(rate, soB2cLogisticsEntity.getAccessoriesNw());
            //最后一条根据减法计算金额
            if (i == splitList.size() - 1) {
                amount = MathUtil.subtract(entity.getAmount(), groupAmount);
                estimatedShippingCost = MathUtil.subtract(soB2cLogisticsEntity.getEstimatedShippingCost(), groupEstimatedShippingCost);
                actualShippingCost = MathUtil.subtract(soB2cLogisticsEntity.getActualShippingCost(), groupActualShippingCost);
                accessoriesCost = MathUtil.subtract(soB2cLogisticsEntity.getAccessoriesCost(), groupAccessoriesCost);
                accessoriesNw = MathUtil.subtract(soB2cLogisticsEntity.getAccessoriesNw(), groupAccessoriesNw);
            }
            //基本信息金额
            addDTO.setAmount(amount);
            //预估费用
            logisticsAddDTO.setEstimatedShippingCost(estimatedShippingCost);
            //实际费用
            logisticsAddDTO.setActualShippingCost(actualShippingCost);
            //包装辅料费
            logisticsAddDTO.setAccessoriesCost(accessoriesCost);
            //包装净重
            logisticsAddDTO.setAccessoriesNw(accessoriesNw);
            logisticsAddDTO.setLength(null);
            logisticsAddDTO.setWidth(null);
            logisticsAddDTO.setHeight(null);
            logisticsAddDTO.setWeight(null);
            logisticsAddDTO.setLogisticsChannelId("");
            addDTO.setLogisticsDTO(logisticsAddDTO);
            addDTO.setRemark(StrUtil.format("【{}】拆分订单", entity.getCode()));

            //操作信息
            addDTO.setOperateType(SoB2cOptionTypeEnum.ENUM_SPLIT);

            //新增拆分后订单
            String code = StrUtil.format("{}_{}", entity.getCode(), flag);
            SoB2cEntity add = soB2cService.add(addDTO, code);
            soIdList.add(add.getId());
            soCodeList.add(add.getCode());
            //迭代1.27.4 拆分的子订单的审核状态默认等于原订单审核状态 订单状态：如果子件不是审核通过，则默认待配货；如果子单是审核通过，则子件走仓库和物流规则，按实际规则执行结果确认订单状态
            add.setApproveStatus(entity.getApproveStatus());
            if(ApproveStatusEnum.APPROVE.equals(entity.getApproveStatus())){
                ApproveOneDTO approveOneDTO = new ApproveOneDTO();
                approveOneDTO.setType(ApproveTypeEnum.PASS.getStatus());
                soB2cService.approveEnd(approveOneDTO,add,true);
                needRuleList.add(add);
            }else{
                add.setApproveStatus(entity.getApproveStatus());
                this.updateById(add);
            }
            //用于同步到TikTok拆分数据的入参
            List<String> sourceDetailIds = detailList.stream().map(req -> req.getSourceDetailId()).collect(Collectors.toList());
            groupsBean.setOrderLineItemIds(sourceDetailIds);
            groupsBean.setId(add.getId());
            splittableGroups.add(groupsBean);
            //计算已生成金额
            groupAmount = MathUtil.add(addDTO.getAmount(), groupAmount);
            groupEstimatedShippingCost = MathUtil.add(logisticsAddDTO.getEstimatedShippingCost(), groupEstimatedShippingCost);
            groupActualShippingCost = MathUtil.add(logisticsAddDTO.getActualShippingCost(), groupActualShippingCost);
            groupAccessoriesCost = MathUtil.add(logisticsAddDTO.getAccessoriesCost(), groupAccessoriesCost);
            groupAccessoriesNw = MathUtil.add(logisticsAddDTO.getAccessoriesNw(), groupAccessoriesNw);
            groupWeight = MathUtil.add(logisticsAddDTO.getWeight(), groupWeight);
            flag++;
        }
        tikTokPramDTO.setSplittableGroups(splittableGroups);
        soB2cService.invalid(entity.getId(), StrUtil.format("【{}】被拆分作废", entity.getCode()), SoB2cInvalidTypeEnum.ENUM_AUTOMATIC);

        //操作日志
        String msg = "从【{}】拆分出新订单";
        operateLogService.addModuleOperateLog(StrUtil.format(msg, entity.getCode()), ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "拆分订单");
        SoB2cDTO.SplitSaveResultDTO splitSaveResultDTO = new SoB2cDTO.SplitSaveResultDTO();
        splitSaveResultDTO.setSoB2cIds(soIdList);
        splitSaveResultDTO.setSoB2cIds(soCodeList);
        splitSaveResultDTO.setTikTokPramDTO(tikTokPramDTO);
        splitSaveResultDTO.setOldEntity(entity);
        splitSaveResultDTO.setNeedRuleIds(needRuleList);
        return splitSaveResultDTO;
    }

    @Transactional(rollbackFor = Exception.class)
    public void tikTokSplit(SoB2cDTO.SplitSaveResultDTO resultDTO) {
        OrderSplitPramDTO tikTokPramDTO = resultDTO.getTikTokPramDTO();
        PlatformSplitViewDTO platformSplitViewDTO = tikTokSdkClientService.sendTikTokOrdersSplit(resultDTO.getOldEntity().getShopId(), resultDTO.getOldEntity().getPlatformCode(), tikTokPramDTO);
        if (0 != platformSplitViewDTO.getCode()) {
            throw new ServiceException(ApiError.ERROR_TIKTOK_SPLIT, resultDTO.getOldEntity().getCode());
        }
        List<PackagesBean> packages = platformSplitViewDTO.getData().getPackages();
        for (SplittableGroupsBean splittableGroup : tikTokPramDTO.getSplittableGroups()) {
            PackagesBean packagesBean = packages.stream().filter(req -> req.getSplittableGroupId().equals(splittableGroup.getId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(packagesBean)) {
                continue;
            }
            //回写平台包裹号
            soB2cDetailService.updatePlatformPackageIdByMainId(packagesBean.getId(), packagesBean.getSplittableGroupId());
        }
    }

    /**
     * @param entity
     * @param soB2cRefList
     * @description: 验证拆分数据
     * @author Will
     * @date: 2023/8/23 15:12
     */
    private void checkSplitData(SoB2cEntity entity,List<SoB2cRefEntity> soB2cRefList) {
        SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsService.getByMainId(entity.getId());
        if (SoB2cBillStatusEnum.ENUM_FROZEN.getCode().equals(entity.getBillStatus()) || entity.getInvalidStatus()
                || SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode().equals(entity.getBillStatus()) ||SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(entity.getBillStatus())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_SAVE_SPLIT_INVALID);
        }
        if(Objects.nonNull(soB2cLogisticsEntity) && StringUtils.isNotBlank(soB2cLogisticsEntity.getCode())){
            throw new ServiceException("已获取跟踪号，请取消物流单后再执行拆分");
        }
        if (PlatformDictEnum.SHOPEE.getCode().equals(entity.getDictPlatform())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_SHOPEE_NOT_SPLIT, entity.getCode());
        }
        if (PlatformDictEnum.MERCADOLIBRE.getCode().equals(entity.getDictPlatform())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_MERCADO_NOT_SPLIT, entity.getCode());
        }
        if (PlatformDictEnum.TIK_TOK.getCode().equals(entity.getDictPlatform())) {
            TikTokShopInfoDTO tikTokShopInfoDTO = tikTokSdkClientService.getShopInfoByShopId(entity.getShopId());
            SplitAttributesDTO splitAttributesDTO = tikTokSdkClientService.sendTikTokSplitAttributes(tikTokShopInfoDTO, entity.getPlatformCode());
            SplitAttributesBean splitAttributesBean = splitAttributesDTO.getData().getSplitAttributes().stream().filter(req -> entity.getPlatformCode().equals(req.getOrderId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(splitAttributesBean) || !splitAttributesBean.getCanSplit()) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_TIKTOK_NOT_SPLIT, entity.getCode(), ObjectUtil.isNotEmpty(splitAttributesBean) ? splitAttributesBean.getReason() : "");
            }
        }

        //未付款数据不能操作
        if (ObjectUtil.isEmpty(entity.getPayStatus()) || SoB2cPayStatusEnum.ENUM_PAYMENT.getCode().equals(entity.getPayStatus())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_PAYMENT_NOT_OPERATE, entity.getCode());
        }

        //查询订单是否是合并订单
        List<SoB2cRefEntity> thisRefList = soB2cRefList.stream().filter(obj -> StrUtil.equals(obj.getTargetId(), entity.getId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(thisRefList)) {
            return;
        }
        long mergeCount = thisRefList.stream().filter(obj -> SoB2cOptionTypeEnum.ENUM_MERGE.getCode().equals(obj.getType())).count();
        if (mergeCount > 0) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_MERGE_NOT_SPLIT, entity.getCode());
        }
    }

    @Override
    public List<SoB2cDTO.ViewSplitDTO> viewSplit(List<String> ids) {
        //查询销售订单信息
        List<SoB2cEntity> soB2cList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(soB2cList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //查询明细信息
        List<SoB2cDetailEntity> soB2cDetailList = soB2cDetailService.listByMainIds(ids);
        if (CollectionUtils.isEmpty(soB2cDetailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        //产品信息
        List<String> skuIdList = soB2cDetailList.stream().map(SoB2cDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuPackByIds(skuIdList);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }

        //查询订单是否是合并订单或拆分子订单
        List<SoB2cRefEntity> soB2cRefList = soB2cRefService.listByTargetIds(ids, null);

        List<SoB2cDTO.ViewSplitDTO> resultList = new ArrayList<>();
        for (SoB2cEntity entity : soB2cList) {
            //验证拆分数据
            checkSplitData(entity,soB2cRefList);
            SoB2cDTO.ViewSplitDTO viewSplitDTO = new SoB2cDTO.ViewSplitDTO();
            viewSplitDTO.setId(entity.getId());
            viewSplitDTO.setCode(entity.getCode());
            //销售订单下对应明细
            List<SoB2cDetailEntity> detailList = soB2cDetailList.stream().filter(obj -> StrUtil.equals(obj.getMainId(), entity.getId())).collect(Collectors.toList());
            List<SoB2cDTO.ViewSplitDetailDTO> viewSplitDetailList = new ArrayList<>();
            for (SoB2cDetailEntity detailEntity : detailList) {
                SoB2cDTO.ViewSplitDetailDTO viewSplitDetailDTO = new SoB2cDTO.ViewSplitDetailDTO();
                BeanMapperUtils.copy(detailEntity, viewSplitDetailDTO);
                SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(skuVO)) {
                    throw new ServiceException(ApiError.ERROR_95084);
                }
                viewSplitDetailDTO.setProductName(skuVO.getSkuName());
                viewSplitDetailDTO.setSourceAmount(detailEntity.getAmount());
                viewSplitDetailDTO.setSourceCurrency(detailEntity.getCurrency());
                viewSplitDetailDTO.setAmount(MathUtil.multiply(detailEntity.getAmount(), detailEntity.getExchangeRate()));
                viewSplitDetailDTO.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
                //产品包装重量 = SKU毛重 * 数量
                viewSplitDetailDTO.setWeight(MathUtil.multiply(skuVO.getGrossWeight(), detailEntity.getQty()));
                viewSplitDetailList.add(viewSplitDetailDTO);
            }
            viewSplitDTO.setDetailList(viewSplitDetailList);
            resultList.add(viewSplitDTO);
        }
        return resultList;
    }

    @Override
    public List<SoB2cDTO.CheckCancelSplitDTO> checkCancelSplit(List<String> ids) {
        //B2C销售订单主表信息
        List<SoB2cEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        List<String> codes = list.stream().map(SoB2cEntity::getCode).collect(Collectors.toList());
        List<String> sourceIdList = list.stream().map(SoB2cEntity::getSourceId).collect(Collectors.toList());
        List<SoB2cRefEntity> parentSoB2cRefList = soB2cRefService.listBySourceIds(sourceIdList, SoB2cOptionTypeEnum.ENUM_SPLIT);
        if (CollectionUtils.isEmpty(parentSoB2cRefList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_SPLIT, codes);
        }
        List<String> targetIdList = parentSoB2cRefList.stream().map(SoB2cRefEntity::getTargetId).collect(Collectors.toList());
        List<SoB2cEntity> targetList = this.listByIds(targetIdList);
        if (CollectionUtils.isEmpty(targetList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }

        List<SoB2cDTO.CheckCancelSplitDTO> resultList = new ArrayList<>();
        for (SoB2cEntity soB2cEntity : list) {
            SoB2cDTO.CheckCancelSplitDTO checkCancelSplitDTO = new SoB2cDTO.CheckCancelSplitDTO();
            checkCancelSplitDTO.setParentB2cSoCode(soB2cEntity.getSourceCode());
            //拆分后订单
            List<SoB2cEntity> splitList = targetList.stream().filter(obj -> obj.getSourceId().equals(soB2cEntity.getSourceId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(splitList)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_SPLIT, soB2cEntity.getCode());
            }
            List<SoB2cDTO.CheckCancelSplitDetailDTO> detailList = new ArrayList<>();
            for (SoB2cEntity splitEntity : splitList) {
                SoB2cDTO.CheckCancelSplitDetailDTO checkCancelSplitDetailDTO = new SoB2cDTO.CheckCancelSplitDetailDTO();
                checkCancelSplitDetailDTO.setChildB2cSoCode(splitEntity.getCode());
                checkCancelSplitDetailDTO.setInvalidStatus(splitEntity.getInvalidStatus());
                checkCancelSplitDetailDTO.setBillStatus(splitEntity.getBillStatus());
                checkCancelSplitDetailDTO.setApproveStatus(splitEntity.getApproveStatus());
                detailList.add(checkCancelSplitDetailDTO);
            }
            checkCancelSplitDTO.setDetailList(detailList);
            resultList.add(checkCancelSplitDTO);
        }
        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelSplit(String id) {
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //已作废，已冻结，待发货，已发货不允许取消拆分
//        if () {
//            throw new ServiceException("已作废，已冻结，待发货，已发货不允许还原拆分");
//        }
        //关联关系
        List<SoB2cRefEntity> soB2cRefList = soB2cRefService.listSourceByTargetIds(Arrays.asList(id), SoB2cOptionTypeEnum.ENUM_SPLIT.getCode());
        if (CollectionUtils.isEmpty(soB2cRefList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_PARENT_NOT_SPLIT, entity.getCode());
        }
        List<String> targetIdList = soB2cRefList.stream().map(SoB2cRefEntity::getTargetId).collect(Collectors.toList());
        List<SoB2cEntity> sameTargetList = this.listByIds(targetIdList);
        if (CollectionUtils.isEmpty(sameTargetList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_CHILD_NOT_EXIST, entity.getCode());
        }
        //关联的子单是否又拆分
        List<SoB2cRefEntity> otherB2cSplitRefList = soB2cRefService.listBySourceIds(targetIdList, SoB2cOptionTypeEnum.ENUM_SPLIT);
        if(CollectionUtils.isNotEmpty(otherB2cSplitRefList)){
            StringBuilder sb = new StringBuilder();
            List<String> otherB2cSplitIds = otherB2cSplitRefList.stream().map(SoB2cRefEntity::getTargetId).collect(Collectors.toList());
            List<SoB2cEntity> otherB2cSplitList = this.listByIds(otherB2cSplitIds);
            for(SoB2cEntity soB2cEntity : sameTargetList){
                List<String> otherSplitIds = otherB2cSplitRefList.stream().filter(v->v.getSourceId().equals(soB2cEntity.getId())).map(SoB2cRefEntity::getTargetId).collect(Collectors.toList());
                List<String> otherSplitCodes = otherB2cSplitList.stream().filter(v->otherSplitIds.contains(v.getId())).map(SoB2cEntity::getCode).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(otherSplitCodes)){
                    String msg = StrUtil.format("{}存在下游拆分子订单【{}】，请先还原子订单拆分后操作", soB2cEntity.getCode(), otherSplitCodes);
                    sb.append(msg);
                }
            }
            //判断sb不为空
            if(StringUtils.isNotBlank(sb.toString())){
                return BatchResultDTO.fail(entity.getId(), entity.getCode(), sb.toString());
            }
        }

        String invalidCodes = sameTargetList.stream().filter(obj -> obj.getInvalidStatus() ||  obj.getBillStatus().equals(SoB2cBillStatusEnum.ENUM_FROZEN.getCode()) || obj.getBillStatus().equals(SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode()) || obj.getBillStatus().equals(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode())).map(SoB2cEntity::getCode).collect(Collectors.joining(","));
        if (StringUtils.isNotBlank(invalidCodes)) {
            throw new ServiceException(StrUtil.format("{} 已作废，已冻结，待发货，已发货不允许还原拆分",invalidCodes));
        }

        log.info("删除B2C销售订单数据，ids = {}", targetIdList);
        //删除拆分后的数据
        soB2cService.deleteById(targetIdList);
        //反作废合并前的数据
        log.info("反作废原B2C销售订单数据，id = {}", entity.getId());
        soB2cService.unInvalid(soB2cRefList.get(0).getSourceId(), SoB2cInvalidTypeEnum.ENUM_AUTOMATIC);
        //操作日志
        String msg = "从【{}】取消拆分";
        operateLogService.addModuleOperateLog(StrUtil.format(msg, entity.getCode()), ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "取消拆分");
        operateLogService.addModuleOperateLog(StrUtil.format("订单取消拆分"), ModuleTypeEnum.SO_B2C.getCode(), soB2cRefList.get(0).getSourceId(), "取消拆分");
        //删除原单备注
        this.lambdaUpdate().eq(SoB2cEntity::getId,soB2cRefList.get(0).getSourceId()).set(SoB2cEntity::getRemark,"").update();
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "取消拆分");
    }

    @Override
    public SoB2cDTO.SplitSaveDTO buildSplitBySku(SoB2cEntity soB2cEntity, List<SoB2cDetailEntity> detailEntityList, List<SoB2cDTO.SplitSkuDetailDTO> splitSkuDetailDTOS, String skuNo) {
        SoB2cDTO.SplitSaveDTO splitSaveDTO = new SoB2cDTO.SplitSaveDTO();
        splitSaveDTO.setId(soB2cEntity.getId());
        splitSaveDTO.setGroupList(buildSplitGroupListBySku(skuNo,detailEntityList,splitSkuDetailDTOS));
        return splitSaveDTO;
    }

    /**
     * 构建拆分分组
     *
     * @param skuNo
     * @param detailEntityList
     * @param splitSkuDetailDTOS
     * @return
     */
    private List<SoB2cDTO.GroupSplitSaveDTO> buildSplitGroupListBySku(String skuNo, List<SoB2cDetailEntity> detailEntityList, List<SoB2cDTO.SplitSkuDetailDTO> splitSkuDetailDTOS) {
        List<SoB2cDTO.GroupSplitSaveDTO> groupSplitSaveDTOS = new ArrayList<>();
        List<String> detailIds = splitSkuDetailDTOS.stream().map(SoB2cDTO.SplitSkuDetailDTO::getDetailId).distinct().collect(Collectors.toList());
        List<SoB2cDetailEntity> detailEntityList1 = detailEntityList.stream().filter(e -> Objects.equals(e.getSkuNo(), skuNo) && CollectionUtils.isNotEmpty(detailIds) && detailIds.contains(e.getId())).collect(Collectors.toList());
        List<String> splitIds = detailEntityList1.stream().map(SoB2cDetailEntity::getId).distinct().collect(Collectors.toList());
        List<SoB2cDetailEntity> detailEntityList2 = detailEntityList.stream().filter(e -> !splitIds.contains(e.getId()) ).collect(Collectors.toList());
        groupSplitSaveDTOS.add(buildGroupSplitSaveDTO(detailEntityList1));
        groupSplitSaveDTOS.add(buildGroupSplitSaveDTO(detailEntityList2));
        return groupSplitSaveDTOS;
    }

    private SoB2cDTO.GroupSplitSaveDTO buildGroupSplitSaveDTO(List<SoB2cDetailEntity> detailEntityList1) {
        if (CollectionUtils.isEmpty(detailEntityList1)){
            throw new ServiceException("不能对单一SKU进行按照SKU拆单");
        }
        SoB2cDTO.GroupSplitSaveDTO groupSplitSaveDTO = new SoB2cDTO.GroupSplitSaveDTO();
        groupSplitSaveDTO.setDetailList(B2cOrderConverter.INSTANCE.convertDetailTOSplitDTO(detailEntityList1));
        return groupSplitSaveDTO;
    }
}
