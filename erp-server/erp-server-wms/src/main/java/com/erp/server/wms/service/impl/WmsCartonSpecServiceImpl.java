package com.erp.server.wms.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.enums.UnitEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.entity.BasicDictEntity;
import com.erp.model.plm.enums.BasicDictTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.CfgRuleOutDTO;
import com.erp.model.wms.dto.WmsCartonSpecDTO;
import com.erp.model.wms.dto.WmsCartonDetailDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.MeasureSourceEnum;
import com.erp.model.wms.enums.PickingSourceTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.WmsCartonSpecMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 发货单箱规信息 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@Service
public class WmsCartonSpecServiceImpl extends SuperServiceImpl<WmsCartonSpecMapper, WmsCartonSpecEntity> implements WmsCartonSpecService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private WmsCartonDetailService wmsCartonDetailService;
    @Autowired
    private WmsCartonService wmsCartonService;
    @Autowired
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private PackingTaskDetailService packingTaskDetailService;
    @Resource
    private CfgRuleOutService cfgRuleOutService;
    @Resource
    private FbaShipmentPackingService fbaShipmentPackingService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(WmsCartonSpecDTO.AddDTO addDTO) {
        WmsCartonSpecEntity wmsCartonSpecEntity = new WmsCartonSpecEntity();
        BeanMapperUtils.copy(addDTO, wmsCartonSpecEntity);
        //检查数据是否存在
        if (StrUtil.isNotBlank(addDTO.getSpecId())){
            WmsCartonSpecEntity old = this.getById(addDTO.getSpecId());
            wmsCartonSpecEntity.setId(Objects.isNull(old)? null: old.getId());
        }else if (StrUtil.isNotBlank(addDTO.getCartonId())){
            WmsCartonEntity wmsCarton = wmsCartonService.getById(addDTO.getCartonId());
            wmsCartonSpecEntity.setId(Objects.nonNull(wmsCarton) ? wmsCarton.getSpecId() : null);
        }
        // 数据处理
        handleData(wmsCartonSpecEntity, addDTO.getTaskId());

        log.info("开始新增发货单箱规信息");
        boolean save = super.saveOrUpdate(wmsCartonSpecEntity);
        if (!save) {
            throw new ServiceException("发货单箱规信息保存失败");
        }
        String msg = StrUtil.format("【{}】新增【{}】箱规号为【{}】", addDTO.getContent(),"装箱箱规", wmsCartonSpecEntity.getBoxSpecNo());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CARTON_SPC.getCode(), addDTO.getTaskId(), addDTO.getOperation());
        //新增箱子信息
        wmsCartonService.add(addDTO, wmsCartonSpecEntity);
        return wmsCartonSpecEntity.getId();
    }

    @Override
    public List<WmsCartonSpecEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(WmsCartonSpecEntity::getMainId, mainIds)
                .orderByAsc(WmsCartonSpecEntity::getCreateTime, WmsCartonSpecEntity::getId)
                .list();
    }

    @Override
    public List<WmsCartonSpecDTO.PackingQtyDTO> listPackingQtyByMainId(String mainId) {
        return baseMapper.listPackingQtyByMainId(mainId);
    }

    @Override
    public List<WmsCartonSpecDTO.PackingQtyDTO> listPackingQtyByMainIds(List<String> mainIds) {
        return baseMapper.listPackingQtyByMainIds(mainIds);
    }
    @Override
    public List<WmsCartonSpecDTO.PackDateDTO> listPackDateByPackingTaskId(String packingTaskId) {
        if (StringUtils.isBlank(packingTaskId)) {
            return Collections.emptyList();
        }
        return baseMapper.listPackDateByMainId(packingTaskId);
    }

    @Override
    public WmsCartonSpecDTO.WmsCartonSpecView getCartonViewByPackingTaskId(PackingTaskEntity packingTaskEntity) {
        WmsCartonSpecDTO.WmsCartonSpecView view = new WmsCartonSpecDTO.WmsCartonSpecView();
        if (ObjectUtil.isEmpty(packingTaskEntity)){
            return view;
        }
        //装箱任务
        view.setTaskId(packingTaskEntity.getId());
        //查询装箱详情
        view.setSourceId(packingTaskEntity.getSourceId());
        view.setSourceType(packingTaskEntity.getSourceType());
        view.setSourceCode(packingTaskEntity.getSourceCode());
        String taskEntityId = packingTaskEntity.getId();
        //查询箱规信息
        List<WmsCartonSpecEntity> wmsCartonSpecEntities = this.listByMainIds(Collections.singletonList(taskEntityId));
        List<WmsCartonEntity> cartonEntityList = wmsCartonService.listByTaskIds(Collections.singletonList(taskEntityId));
        List<WmsCartonSpecDTO.ViewDTO> wmsCartonSpecList = BeanMapper.copyList(wmsCartonSpecEntities, WmsCartonSpecDTO.ViewDTO.class);
        view.setWmsCartonList(wmsCartonSpecList);
        List<PackingTaskDetailEntity> taskDetailEntityList = packingTaskDetailService.listByMainIds(Collections.singletonList(taskEntityId));
        //根据主表id分组sku查询发货及待装箱数
        List<WmsCartonSpecDTO.PackDateDTO> packDateDTOS = this.listPackDateByPackingTaskId(taskEntityId);
        //查询产品信息
        List<String> skuIdList = packDateDTOS.stream().map(WmsCartonSpecDTO.PackDateDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuPackByIds(skuIdList);
        //查询箱规包含的产品信息
        for (WmsCartonSpecDTO.ViewDTO viewDTO : wmsCartonSpecList) {
            WmsCartonEntity cartonEntity = cartonEntityList.stream().filter(e -> e.getSpecId().equals(viewDTO.getId())).findFirst().orElse(new WmsCartonEntity());
            List<WmsCartonDetailEntity> detailEntityList = wmsCartonDetailService.listByMainIds(Collections.singletonList(cartonEntity.getId()));
//            List<WmsCartonSpecDTO.PackDateDTO> packDateDTOList = packDateDTOS.stream().filter(req -> req.getBoxSpecNo().equals(viewDTO.getBoxSpecNo())).collect(Collectors.toList());
            List<WmsCartonDetailDTO.ViewDTO> detailList = BeanMapper.copyList(detailEntityList, WmsCartonDetailDTO.ViewDTO.class);
            for (WmsCartonDetailDTO.ViewDTO dto : detailList) {
                int deliveryQty = taskDetailEntityList.stream().filter(req -> dto.getSkuId().equals(req.getSkuId()) && dto.getFnSku().equals(req.getFnSku())).mapToInt(PackingTaskDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
                dto.setDeliveryQty(deliveryQty);
                //待装箱数量=发货数量-所有已装箱数量
                int packQtySum = packDateDTOS.stream().filter(req -> req.getSkuId().equals(dto.getSkuId()) && dto.getFnSku().equals(req.getFnSku())).mapToInt(WmsCartonSpecDTO.PackDateDTO::getPackQty).sum();
                dto.setWaitPackQty(deliveryQty - packQtySum);

                //匹配产品信息，设置中文名
                SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(dto.getSkuId())).findFirst().orElse(new SkuVO());
                dto.setProductName(skuVO.getSkuName());
                dto.setWeightUnit(UnitEnum.WeightUnitEnum.KG.code);
            }
            viewDTO.setDetailList(detailList);
            viewDTO.setWeightUnit(UnitEnum.WeightUnitEnum.KG.code);
            BigDecimal grossWeight= detailList.stream().map(WmsCartonDetailDTO.ViewDTO::getGrossWeight).reduce(BigDecimal.ZERO,BigDecimal::add);
            viewDTO.setGrossWeight(grossWeight);
            viewDTO.setBoxNo(cartonEntity.getBoxNo() != 0 ? cartonEntity.getBoxNo() : null);
            viewDTO.setPackingUserId(cartonEntity.getPackingUserId());
            viewDTO.setPackingUserName(cartonEntity.getPackingUserName());
            viewDTO.setCartonId(cartonEntity.getId());
            //预警提示：超重值：10KG，本次装箱预计已超重1KG！
            WmsCartonSpecDTO.WeightRuleDTO warnMsg = getWarnMsg(packingTaskEntity.getSourceType(), grossWeight);
            viewDTO.setWarnMsg(warnMsg.getWarnMsg());
            viewDTO.setMaxWeight(warnMsg.getMaxWeight());
            viewDTO.setMinWeight(warnMsg.getMinWeight());
        }
        //装箱进度
        view.setDeliveryQty(taskDetailEntityList.stream().map(PackingTaskDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum));
        view.setPackTotalQty(packDateDTOS.stream().map(WmsCartonSpecDTO.PackDateDTO::getPackQty).reduce(MathUtil.ZERO,Integer::sum));
        view.setPackGrossWeight(packDateDTOS.stream().map(WmsCartonSpecDTO.PackDateDTO::getGrossWeight).reduce(BigDecimal.ZERO,BigDecimal::add));
        view.setPackWeightUnit(UnitEnum.WeightUnitEnum.KG.code);

        return view;
    }

    @Override
    public WmsCartonSpecDTO.WeightRuleDTO getWarnMsg(String sourceType,BigDecimal grossWeight){
        if (Objects.isNull(grossWeight)){
            grossWeight = BigDecimal.ZERO;
        }
        WmsCartonSpecDTO.WeightRuleDTO weightRuleDTO = new WmsCartonSpecDTO.WeightRuleDTO();
        CfgRuleOutDTO.CfgOverweightDetailDTO dto = cfgRuleOutService.getCfgOverweightDetailDTOByType(sourceType);
        if (Objects.isNull(dto)){
            return weightRuleDTO;
        }
        weightRuleDTO.setMaxWeight(dto.getMaxWeight());
        weightRuleDTO.setMinWeight(dto.getMinWeight());
        String warnMsg = "";
        //小于最小限制
        if (Objects.nonNull(dto.getMinWeight()) && grossWeight.compareTo(dto.getMinWeight()) < 0){
            BigDecimal subtract = dto.getMinWeight().subtract(grossWeight);
            warnMsg = StrUtil.format("预警提示：重量低于最低重量：{}KG，本次装箱预计已低{}KG！", grossWeight, subtract);

        }
        //大于最大限制
        if (Objects.nonNull(dto.getMaxWeight()) && grossWeight.compareTo(dto.getMaxWeight()) > 0){
            BigDecimal subtract = grossWeight.subtract(dto.getMaxWeight());
            warnMsg = StrUtil.format("预警提示：超重值：{}KG，本次装箱预计已超重{}KG！", grossWeight, subtract);
        }
        weightRuleDTO.setWarnMsg(warnMsg);
        return weightRuleDTO;
    }

    @Override
    public void checkProductPropertyIds(String sourceType, List<String> skuIds) {
        if (CollectionUtils.isEmpty(skuIds) || StrUtil.isBlank(sourceType)){
            return;
        }
        List<CfgRuleOutDTO.CfgProductPackingDetail> productRuleList = cfgRuleOutService.getCfgProductPackingDetailByType(sourceType);
        if (CollectionUtils.isEmpty(productRuleList)){
            return;
        }
        //获取sku属性值
        List<SkuVO> skuVOList = plmTaskFeign.listSkuLogisticsByIds(skuIds);
        Set<String> propertyIds = new HashSet<>();
        skuVOList.forEach(skuVO -> {
            String productPropertyId = skuVO.getProductPropertyId();
            if (StrUtil.isNotBlank(productPropertyId)){
                String[] split = productPropertyId.split(",");
                propertyIds.addAll(Arrays.asList(split));
            }
        });
        if (CollectionUtils.isEmpty(propertyIds)){
            return;
        }
        for (CfgRuleOutDTO.CfgProductPackingDetail productRule : productRuleList) {
            List<String> cannotPackingPropertyIds = productRule.getCannotPackingPropertyIds();
            List<String> canPackingPropertyIds = productRule.getCanPackingPropertyIds();
            if (CollectionUtils.isEmpty(canPackingPropertyIds) || CollectionUtils.isEmpty(canPackingPropertyIds)){
                continue;
            }
            List<String> containIds1 = cannotPackingPropertyIds.stream().filter(propertyIds::contains).collect(Collectors.toList());
            List<String> containIds2 = canPackingPropertyIds.stream().filter(propertyIds::contains).collect(Collectors.toList());
            List<BasicDictEntity> declarePropertyList = plmTaskFeign.listDictByType(BasicDictTypeEnum.DECLARE_PROPERTY.getCode());
            Map<String, String> dictMap = declarePropertyList.stream().collect(Collectors.toMap(BasicDictEntity::getId, BasicDictEntity::getName));
            if (CollectionUtils.isNotEmpty(containIds1) && CollectionUtils.isNotEmpty(containIds2)){
                List<String> canPackList = new ArrayList<>();
                containIds2.forEach(propertyId -> {
                    String name = dictMap.get(propertyId);
                    if (StrUtil.isNotBlank(name)){
                        canPackList.add(name);
                    }
                });
                List<String> cannotPackList = new ArrayList<>();
                containIds1.forEach(propertyId -> {
                    String name = dictMap.get(propertyId);
                    if (StrUtil.isNotBlank(name)){
                        cannotPackList.add(name);
                    }
                });
                String msg = StrUtil.format("分类【{}】装入【{}】不可装入【{}】", PickingSourceTypeEnum.getName(sourceType), String.join("," ,canPackList), String.join(",",cannotPackList));
                throw new ServiceException(msg);
            }
        }


    }


    /**
     * 删除原装箱信息
     * @param taskId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCarton(String taskId) {
        List<WmsCartonSpecEntity> specEntityList = this.listByMainIds(Collections.singletonList(taskId));
        if (CollectionUtils.isNotEmpty(specEntityList)){
            List<String> specIds = specEntityList.stream().map(WmsCartonSpecEntity::getId).distinct().collect(Collectors.toList());
            this.removeByIds(specIds);
        }
        List<WmsCartonEntity> cartonEntityList = wmsCartonService.listByTaskIds(Collections.singletonList(taskId));
        if (CollectionUtils.isNotEmpty(cartonEntityList)){
            List<String> cartonIds = cartonEntityList.stream().map(WmsCartonEntity::getId).distinct().collect(Collectors.toList());
            wmsCartonService.removeByIds(cartonIds);
            wmsCartonDetailService.deleteByCartonIds(cartonIds);
        }
    }

    @Override
    public void updateSpec(WmsCartonSpecEntity specEntity) {
        this.lambdaUpdate()
                .eq(WmsCartonSpecEntity::getId, specEntity.getId())
                .set(WmsCartonSpecEntity::getPackageWeight, specEntity.getPackageWeight())
                .set(WmsCartonSpecEntity::getWeightUnit, specEntity.getWeightUnit())
                .set(WmsCartonSpecEntity::getBoxLength, specEntity.getBoxLength())
                .set(WmsCartonSpecEntity::getBoxWidth, specEntity.getBoxWidth())
                .set(WmsCartonSpecEntity::getBoxHeight, specEntity.getBoxHeight())
                .set(WmsCartonSpecEntity::getSizeUnit, specEntity.getSizeUnit())
                .set(WmsCartonSpecEntity::getMeasureSource, specEntity.getMeasureSource())
                .update();
    }

    @Override
    public WmsCartonSpecEntity getByTaskIdAndBoxNo(String packingTaskId, String boxNo) {
        if(com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(packingTaskId) || com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(boxNo)){
            return null;
        }
        return lambdaQuery().eq(WmsCartonSpecEntity::getMainId,packingTaskId).eq(WmsCartonSpecEntity::getBoxSpecNo,boxNo).last("limit 1").one();
    }

    @Override
    public void updateSizeDataEmpty(WmsCartonSpecEntity cartonSpecEntity) {
        if (Objects.isNull(cartonSpecEntity) || StrUtil.isBlank(cartonSpecEntity.getId())){
            return;
        }
        this.lambdaUpdate().eq(WmsCartonSpecEntity::getId, cartonSpecEntity.getId())
                .set(WmsCartonSpecEntity::getBoxLength, BigDecimal.ZERO)
                .set(WmsCartonSpecEntity::getBoxHeight, BigDecimal.ZERO)
                .set(WmsCartonSpecEntity::getBoxWidth, BigDecimal.ZERO)
                .set(WmsCartonSpecEntity::getPackageWeight, BigDecimal.ZERO)
                .update();
    }

    /**
     * 删除web端移除数据 并校验fba数据来源
     * @param dto
     * @param isAddCarton 增量标识
     */
    @Override
    public void checkAndRemoveCartonInfo(WmsCartonSpecDTO.WmsCartonAdd dto,  Boolean isAddCarton) {
        String taskId = dto.getTaskId();
        if (StrUtil.isBlank(taskId)){
            return;
        }
        //已绑定货件不能操作
        List<FbaShipmentPackingEntity> fbaShipmentPackingEntityList = fbaShipmentPackingService.listByPackingTaskId(taskId);
        //已装箱信息
        List<WmsCartonEntity> oldCartonEntityList = wmsCartonService.listByTaskIds(Collections.singletonList(taskId));
        if (CollectionUtils.isEmpty(oldCartonEntityList)){
            return;
        }
        //已装箱明细
        List<WmsCartonDetailEntity> oldDetailEntityList = wmsCartonDetailService.listByTaskIds(Collections.singletonList(taskId));

        List<String> newCartonIds = dto.getWmsCartonList().stream().map(WmsCartonSpecDTO.AddDTO::getCartonId).filter(StrUtil::isNotBlank).distinct().collect(Collectors.toList());
        //需要删除的装箱
        List<String> needDeleteCartonIds = oldCartonEntityList.stream().map(WmsCartonEntity::getId).filter(id -> CollectionUtils.isNotEmpty(newCartonIds) && !newCartonIds.contains(id)).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(fbaShipmentPackingEntityList)){
            //存在关联货号，新接口数据为空时
            if (CollectionUtils.isEmpty(newCartonIds)){
                throw new ServiceException("所有已下推的箱号不允许删除");
            }
            //存在已关联箱号的货件
            List<String> cartonIds = fbaShipmentPackingEntityList.stream().map(FbaShipmentPackingEntity::getCartonId).distinct().collect(Collectors.toList());
            List<String> deleteFbaCartonIds = cartonIds.stream().filter(needDeleteCartonIds::contains).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(deleteFbaCartonIds)){
                List<String> boxNoList = oldCartonEntityList.stream().filter(e -> deleteFbaCartonIds.contains(e.getId())).map(e -> String.valueOf(e.getBoxNo())).distinct().collect(Collectors.toList());
                throw new ServiceException(StrUtil.format("已下推的箱号【{}】不允许再修改", String.join(",",boxNoList)));
            }
            List<WmsCartonEntity> cartonEntityList = oldCartonEntityList.stream().filter(e -> cartonIds.contains(e.getId())).collect(Collectors.toList());
            if (cartonIds.size() != cartonEntityList.size()){
                throw new ServiceException("已关联货件与存在的装箱数据不一致");
            }
            //获取最大装箱箱号信息
            WmsCartonEntity maxWmsCartonEntity = cartonEntityList.stream().max(Comparator.comparing(WmsCartonEntity::getBoxNo)).get();
            //最大装箱信息之前的所有装箱信息 不能删除
            List<WmsCartonEntity> lastCartionList = oldCartonEntityList.stream().filter(e -> e.getBoxNo() <= maxWmsCartonEntity.getBoxNo()).collect(Collectors.toList());
            //检查是否存在已删除的箱子
            List<WmsCartonEntity> notExistList = lastCartionList.stream().filter(e -> !newCartonIds.contains(e.getId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(notExistList)){
                List<String> boxNoList = notExistList.stream().filter(e -> Objects.nonNull(e.getBoxNo())).map(e -> String.valueOf(e.getBoxNo())).distinct().collect(Collectors.toList());
                throw new ServiceException(StrUtil.format("已下推的箱号【{}】前箱号【{}】不允许再修改", maxWmsCartonEntity.getBoxNo(),String.join(",",boxNoList)));
            }
            List<WmsCartonSpecEntity> cartonSpecEntityList = this.listByMainIds(Collections.singletonList(taskId));
            //比较明细
            for (String cartonId : cartonIds) {
                WmsCartonSpecDTO.AddDTO addDTO = dto.getWmsCartonList().stream().filter(e -> cartonId.equals(e.getCartonId())).findFirst().orElse(null);
                if (Objects.isNull(addDTO)){
                    throw new ServiceException("已下推的箱号不允许再修改");
                }
                WmsCartonEntity wmsCartonEntity = oldCartonEntityList.stream().filter(e -> Objects.nonNull(e) && e.getId().equals(cartonId)).findFirst().orElse(null);
                if (Objects.isNull(wmsCartonEntity)){
                    throw new ServiceException("已下推的装箱不允许再删除");
                }
                WmsCartonSpecEntity specEntity = cartonSpecEntityList.stream().filter(e -> Objects.nonNull(e) && StrUtil.isNotBlank(wmsCartonEntity.getSpecId()) && e.getId().equals(wmsCartonEntity.getSpecId())).findFirst().orElse(null);
                if (Objects.isNull(specEntity)){
                    throw new ServiceException("已下推的箱规不允许再删除");
                }
                List<WmsCartonDetailEntity> cartonDetailEntityList = oldDetailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getMainId().equals(cartonId)).collect(Collectors.toList());
                //对比数据
//                compareSpec(specEntity, addDTO);
                compareCarton(wmsCartonEntity,addDTO);
                compareCartonDetail(cartonDetailEntityList, addDTO.getDetailList());
            }
        }
        if (Objects.nonNull(isAddCarton) && isAddCarton){
            //页面新增编辑
            List<String> specIds = oldCartonEntityList.stream().filter(e -> needDeleteCartonIds.contains(e.getId())).map(WmsCartonEntity::getSpecId).distinct().collect(Collectors.toList());
            this.removeByIds(specIds);
            wmsCartonService.deleteByCartonIds(needDeleteCartonIds);
            wmsCartonDetailService.deleteByCartonIds(needDeleteCartonIds);
        }
    }

    private void compareCartonDetail(List<WmsCartonDetailEntity> cartonDetailEntityList, List<WmsCartonDetailDTO.AddDTO> detailList) {
        if (cartonDetailEntityList.size() != detailList.size()){
            throw new ServiceException("已下推的装箱明细不允许再修改");
        }
        for (WmsCartonDetailEntity wmsCartonDetailEntity : cartonDetailEntityList) {
            WmsCartonDetailDTO.AddDTO addDTO = detailList.stream().filter(e -> Objects.equals(wmsCartonDetailEntity.getId(), e.getId())).findFirst().orElse(null);
            if (Objects.isNull(addDTO)){
                throw new ServiceException(StrUtil.format("已下推的装箱明细SKU【{}】FNSKU【{}】不允许再修改", wmsCartonDetailEntity.getSkuNo(),wmsCartonDetailEntity.getFnSku()));
            }
            int hash1 = getCartonDetailHash(wmsCartonDetailEntity.getSkuId(),wmsCartonDetailEntity.getSkuNo(),wmsCartonDetailEntity.getFnSku(),wmsCartonDetailEntity.getPackQty());
            int hash2 = getCartonDetailHash(addDTO.getSkuId(),addDTO.getSkuNo(),addDTO.getFnSku(),addDTO.getPackQty());
            if (hash1 != hash2){
                throw new ServiceException(StrUtil.format("已下推的装箱明细不允许再修改，修改前【{}】修改后【{}】",
                        wmsCartonDetailEntity.getSkuNo() + "|" + wmsCartonDetailEntity.getFnSku() + "|" + wmsCartonDetailEntity.getPackQty()
                        ,addDTO.getSkuNo() + "|" + addDTO.getFnSku() + "|" + addDTO.getPackQty()));
            }
        }

    }

    private int getCartonDetailHash(String skuId, String skuNo, String fnSku, Integer packQty) {
        return Objects.hash(skuId,skuNo,fnSku,packQty);
    }

    private void compareCarton(WmsCartonEntity wmsCartonEntity, WmsCartonSpecDTO.AddDTO addDTO) {
        if (!Objects.equals(wmsCartonEntity.getBoxNo(), addDTO.getBoxNo())){
            throw new ServiceException(StrUtil.format("已下推的箱号不允许再修改,箱号修改前【{}】修改后【{}】",wmsCartonEntity.getBoxNo(),addDTO.getBoxNo()));
        }
    }

    private void compareSpec(WmsCartonSpecEntity specEntity, WmsCartonSpecDTO.AddDTO addDTO) {
        int hash1 = getSpecHash(specEntity.getBoxLength(), specEntity.getBoxWidth(), specEntity.getBoxHeight(),specEntity.getPackageWeight());
        int hash2 = getSpecHash(addDTO.getBoxLength(), addDTO.getBoxWidth(), addDTO.getBoxHeight(),addDTO.getPackageWeight());
        if (hash1 != hash2){
            throw new ServiceException(StrUtil.format("已下推的箱号不允许再修改，箱规修改前【{}】修改后【{}】",
                    String.valueOf(specEntity.getBoxLength()) + "-" + specEntity.getBoxWidth() + "-" + specEntity.getBoxHeight() + "-" + specEntity.getPackageWeight()
                    ,String.valueOf(addDTO.getBoxLength()) + "-" + addDTO.getBoxWidth() + "-" + addDTO.getBoxHeight() + "-" + addDTO.getPackageWeight()));
        }
    }

    private int getSpecHash(BigDecimal boxLength, BigDecimal boxWidth, BigDecimal boxHeight, BigDecimal packageWeight) {
        boxLength = Objects.nonNull(boxLength) ? boxLength : BigDecimal.ZERO;
        boxWidth = Objects.nonNull(boxWidth) ? boxWidth : BigDecimal.ZERO;
        boxHeight = Objects.nonNull(boxHeight) ? boxHeight : BigDecimal.ZERO;
        packageWeight = Objects.nonNull(packageWeight) ? packageWeight : BigDecimal.ZERO;
        return Objects.hash(boxLength.stripTrailingZeros().toPlainString(),boxWidth.stripTrailingZeros().toPlainString(),boxHeight.stripTrailingZeros().toPlainString(),packageWeight.stripTrailingZeros().toPlainString());
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(WmsCartonSpecEntity wmsCartonSpecEntity, String taskId) {
        wmsCartonSpecEntity.setMainId(taskId);
        if (StringUtils.isBlank(wmsCartonSpecEntity.getMeasureSource())){
            wmsCartonSpecEntity.setMeasureSource(MeasureSourceEnum.MANUAL.getCode());
        }
        if (Objects.isNull(wmsCartonSpecEntity.getBoxQty())){
            wmsCartonSpecEntity.setBoxQty(MathUtil.ONE);
        }
        if (Objects.isNull(wmsCartonSpecEntity.getId())){
            Integer boxSpecNo = baseMapper.selectBoxSpecNo(taskId);
            if (Objects.isNull(boxSpecNo)){
                wmsCartonSpecEntity.setBoxSpecNo(MathUtil.ONE);
            }else {
                wmsCartonSpecEntity.setBoxSpecNo(boxSpecNo + 1);
            }
        }
    }
}
