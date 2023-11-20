package com.erp.server.oms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.dto.base.BaseIdDTO;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.excel.SkuMappingWarehouseImportExcelDTO;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.server.oms.service.ListingInfoService;
import com.erp.server.oms.service.SkuMappingService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname SkuMapingExcelListener
 * @Date 2023-06-28 18:07
 * @Created by yl
 */
public class SkuMappingWarehouseExcelListener extends AnalysisEventListener<SkuMappingWarehouseImportExcelDTO> {

    /**
     * 已审核消息
     */
    private List<SkuVO> skuList;


    /**
     * sku 映射信息
     */
    private List<SkuMappingEntity> skuMappingList;

    /**
     * 仓库信息
     */
    List<WarehouseDTO.UpdateDTO> warehouseList;

    /**
     * listing 信息
     */
    private List<ListingInfoEntity> listingInfoEntityList;

    /**
     * listing
     */
    private ListingInfoService listingInfoService;

    private SkuMappingService skuMappingService;
    /**
     * listing 信息
     */
    private List<ListingInfoEntity> addListingInfoEntityList = new ArrayList<>(10);

    private List<SkuMappingEntity> addSkuMappingList = new ArrayList<>(10);

    private List<BaseIdDTO> skuWarehouseList = new ArrayList<>(10);

    /**
     * 导入错误数据
     */
    private List<SkuMappingWarehouseImportExcelDTO> errorList = new ArrayList<>(10);

    public SkuMappingWarehouseExcelListener(SkuMappingService skuMappingService, List<SkuVO> skuList,
                                            List<SkuMappingEntity> skuMappingList,
                                            List<WarehouseDTO.UpdateDTO> warehouseList,
                                            List<ListingInfoEntity> listingInfoEntityList,
                                            ListingInfoService listingInfoService) {
        this.skuMappingService = skuMappingService;
        this.skuList = skuList;
        this.skuMappingList = skuMappingList;
        this.warehouseList = warehouseList;
        this.listingInfoEntityList = listingInfoEntityList;
        this.listingInfoService = listingInfoService;
    }

    /**
     * 每解析一行执行一次
     *
     * @param importExcelDTO
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-06-28 18:12
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(SkuMappingWarehouseImportExcelDTO importExcelDTO, AnalysisContext analysisContext) {
        List<String> msgList = FieldValidUtil.fieldValid(importExcelDTO);
        //注解验证信息
        List<String> errorMsgList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        String skuNo = importExcelDTO.getSkuNo();
        SkuVO sku = skuList.stream().filter(s -> s.getSkuNo().equals(skuNo)).findFirst().orElse(null);
        if (Objects.isNull(sku)) {
            errorMsgList.add("产品sku不存在");
        }
        //仓库名称
        String warehouseName = importExcelDTO.getWarehouseName();
        WarehouseDTO.UpdateDTO warehouse = warehouseList.stream().filter(w -> w.getName().equals(warehouseName)).
                findFirst().orElse(null);
        if (Objects.isNull(warehouse)) {
            errorMsgList.add("仓库不存在");
        }
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            importExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(importExcelDTO);
            return;
        }
        //仓库id
        String warehouseId = warehouse.getId();
        //库存sku
        String warehouseSkuNo = importExcelDTO.getWarehouseSkuNo();
        ListingInfoEntity listingInfoEntity = listingInfoEntityList.stream().filter(l -> l.getPlatformSkuNo().
                equals(warehouseSkuNo)).findFirst().orElse(null);
        String listingId = "";
        if (Objects.nonNull(listingInfoEntity)) {
            listingId = listingInfoEntity.getId();
        }

        RuleTypeEnum warehouseType = RuleTypeEnum.WAREHOUSE;
        //已对应的平台sku
        String finalListingId = listingId;
        List<SkuMappingEntity> existList = skuMappingList.stream().filter(
                s -> s.getListingId().equals(finalListingId)
                        && warehouseId.equals(s.getWarehouseId())
                        && (!s.getIsExpire())
                        && warehouseType.equals(s.getType())
        ).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(existList)) {
            errorMsgList.add("同仓库库存SKU只能对应一个产品SKU");
        }

        long count = skuMappingList.stream().filter(
                        a -> warehouseType.equals(a.getType()) &&
                                warehouseId.equals(a.getWarehouseId()) &&
                                sku.getSkuId().equals(a.getProductSkuId())).map(SkuMappingEntity::getListingId).
                distinct().count();
        if (count > 1) {
            errorMsgList.add("SKU在该仓库已关联其他库存SKU，请更换其他SKU");
        }
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            importExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(importExcelDTO);
            return;
        }
        BaseIdDTO idDTO=new BaseIdDTO();
        idDTO.setId(warehouseId);
        idDTO.setName(sku.getSkuId());
        skuWarehouseList.add(idDTO);
        long skuCount = skuWarehouseList.stream().filter(s -> s.getId().equals(warehouseId) &&
                s.getName().equals(sku.getSkuId())).count();
        if (skuCount > 1) {
            errorMsgList.add("SKU在该仓库已关联其他库存SKU，请更换其他SKU");
        }
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            importExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(importExcelDTO);
            return;
        }
        if (Objects.isNull(listingInfoEntity)) {
            listingId = IdWorker.getIdStr();
            ListingInfoEntity addListingInfoEntity = new ListingInfoEntity();
            addListingInfoEntity.setId(listingId);
            addListingInfoEntity.setType(RuleTypeEnum.WAREHOUSE.getCode());
            addListingInfoEntity.setPlatformSkuNo(warehouseSkuNo);
            addListingInfoEntity.setPlatformSkuName(importExcelDTO.getWarehouseProductName());
            addListingInfoEntity.setMatchResult(Boolean.TRUE);
            addListingInfoEntityList.add(addListingInfoEntity);
            listingInfoEntityList.add(addListingInfoEntity);
        }

        LocalDateTime now = LocalDateTime.now();
        SkuMappingEntity add = new SkuMappingEntity();
        add.setProductSkuId(sku.getSkuId());
        add.setProductSkuNo(sku.getSkuNo());
        add.setListingId(listingId);
        add.setType(warehouseType);
        add.setWarehouseId(warehouseId);
        add.setWarehouseName(warehouseName);
        add.setIsExpire(Boolean.FALSE);
        //生效时间
        add.setEffectiveTime(now);
        add.setExpireTime(now.plusYears(MathUtil.NUMBER_100));
        //校验用
        skuMappingList.add(add);
        addSkuMappingList.add(add);


    }


    //所有执行玩后 在执行
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (CollectionUtils.isNotEmpty(addSkuMappingList)) {
            skuMappingService.saveBatch(addSkuMappingList);
        }

        if (CollectionUtils.isNotEmpty(addListingInfoEntityList)) {
            listingInfoService.saveBatch(addListingInfoEntityList);
        }
    }


    public List<SkuMappingWarehouseImportExcelDTO> getErrorList() {
        return errorList;
    }
}
