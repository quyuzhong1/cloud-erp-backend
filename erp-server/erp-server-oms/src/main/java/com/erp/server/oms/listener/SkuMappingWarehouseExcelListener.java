package com.erp.server.oms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.OmsPlatformEnum;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.excel.SkuMappingWarehouseImportExcelDTO;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.server.oms.service.ListingInfoService;
import com.erp.server.oms.service.SkuMappingService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
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
     * 更新的信息
     */
    private List<SkuMappingEntity> updateSkuMappingList = new ArrayList<>(10);

    /**
     * 更新的listing
     */
    private List<ListingInfoEntity> updateListingInfoList = new ArrayList<>(10);

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

        //仓库名称
        String warehouseName = importExcelDTO.getWarehouseName();
        WarehouseDTO.UpdateDTO warehouse = warehouseList.stream().filter(w -> w.getName().equals(warehouseName)).
                findFirst().orElse(null);
        if (null == warehouse) {
            errorMsgList.add("仓库不存在");
        }
        // 服务商校验
        OmsPlatformEnum platformEnum;
        if (StringUtils.isNotBlank(importExcelDTO.getPlatformName())){
            platformEnum = Arrays.stream(OmsPlatformEnum.values())
                    .filter(e -> e.getCode().equalsIgnoreCase(importExcelDTO.getPlatformName()) || e.getName().equalsIgnoreCase(importExcelDTO.getPlatformName()))
                    .findFirst().orElse(null);
            if (null == platformEnum){
                errorMsgList.add("服务商不存在");
            }
        } else {
            platformEnum = null;
        }

        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            importExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(importExcelDTO);
            return;
        }

        // 查询该仓库所有平台sku
        ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
        String currentPlatform = null == platformEnum ? "" : platformEnum.getCode();
        paramDTO.setPlatform(currentPlatform);
//        paramDTO.setWarehouseIdList(Collections.singletonList(warehouse.getId()));
        paramDTO.setType(RuleTypeEnum.WAREHOUSE.getCode());
        paramDTO.setPlatformSkuNoList(Collections.singletonList(importExcelDTO.getWarehouseSkuNo()));
        List<ListingInfoWithSkuMappingDTO> listDto = skuMappingService.findListDto(paramDTO);

        ListingInfoWithSkuMappingDTO currentSkuMapping = listDto.stream()
                .filter(e-> e.getHasMappingAll() || e.getWarehouseId().equalsIgnoreCase(warehouse.getId()))
                .findFirst().orElse(null);

        if (null != platformEnum && null == currentSkuMapping){
            errorMsgList.add("服务商不允许新增");
            importExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(importExcelDTO);
            return;
        }


        // 已存在
        if ( null != currentSkuMapping ){
            if(currentSkuMapping.getMatchResult()){
                errorMsgList.add("该仓库服务商sku已存在匹配关系");
                importExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(importExcelDTO);
                return;
            }
        }

        String skuNo = importExcelDTO.getSkuNo();
        SkuVO sku = skuList.stream().filter(s -> s.getSkuNo().equals(skuNo)).findFirst().orElse(null);
        if (Objects.isNull(sku)) {
            errorMsgList.add("产品sku不存在");
            importExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(importExcelDTO);
            return;
        }

        //仓库id
        String warehouseId = null == warehouse ? "" : warehouse.getId();
        //库存sku
        String warehouseSkuNo = importExcelDTO.getWarehouseSkuNo();
        ListingInfoEntity listingInfoEntity = listingInfoEntityList.stream()
                .filter(l -> l.getPlatformSkuNo().equals(warehouseSkuNo)
                        && l.getPlatform().equalsIgnoreCase(null == platformEnum ? "" : platformEnum.getCode())
                ).findFirst().orElse(null);
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
                        && s.getDictPlatform().equalsIgnoreCase(null == platformEnum ? "" : platformEnum.getCode())
        ).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(existList)) {
//            errorMsgList.add("相同平台sku只能对应一个平台sku");
            // 修改对应关系
            SkuMappingEntity skuMappingEntity = existList.stream().findFirst().orElse(null);
            if (null != skuMappingEntity){
                skuMappingEntity.setProductSkuId(sku.getSkuId());
                skuMappingEntity.setProductSkuNo(sku.getSkuNo());
                updateSkuMappingList.add(skuMappingEntity);
                listingInfoEntity.setMatchResult(true);
                updateListingInfoList.add(listingInfoEntity);
            } else {
                errorMsgList.add("未找到存在的映射记录");
                importExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(importExcelDTO);
            }
            return;
        }

        long count = skuMappingList.stream().filter(
                        a -> (warehouseType.equals(a.getType()) &&
                                currentPlatform.equalsIgnoreCase(a.getDictPlatform()) &&
                                (warehouseId.equals(a.getWarehouseId())) &&
                                sku.getSkuId().equals(a.getProductSkuId()) &&
                                !a.getHasMappingAll()) ||
                                (warehouseType.equals(a.getType()) && a.getHasMappingAll() && sku.getSkuId().equals(a.getProductSkuId()) && currentPlatform.equalsIgnoreCase(a.getDictPlatform()))
                ).map(SkuMappingEntity::getListingId).
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
