package com.erp.server.oms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.dto.excel.SkuMappingImportExcelDTO;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.plm.vo.SkuVO;
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
public class SkuMappingExcelListener extends AnalysisEventListener<SkuMappingImportExcelDTO> {

    /**
     * 已审核消息
     */
    private List<SkuVO> skuList;

    /**
     * 店铺信息
     */
    private List<ShopInfoEntity> shopList;

    /**
     * sku 映射信息
     */
    private List<SkuMappingEntity> skuMappingList;

    /**
     * 对应平台的信息
     */
    private List<DictBasicDTO.ViewDTO> dictBasicList;

    /**
     * listing 信息
     */
    private List<ListingInfoEntity> listingInfoEntityList;

    private SkuMappingService skuMappingService;

    private ListingInfoService listingInfoService;

    private List<SkuMappingEntity> addSkuMappingList = new ArrayList<>(10);

    /**
     * listing 信息
     */
    private List<ListingInfoEntity> addListingInfoEntityList = new ArrayList<>(10);

    /**
     * 导入错误数据
     */
    private List<SkuMappingImportExcelDTO> errorList = new ArrayList<>(10);

    public SkuMappingExcelListener(SkuMappingService skuMappingService, List<SkuVO> skuList,
                                   List<ShopInfoEntity> shopList, List<SkuMappingEntity> skuMappingList,
                                   List<DictBasicDTO.ViewDTO> dictBasicList,
                                   List<ListingInfoEntity> listingInfoEntityList,
                                   ListingInfoService listingInfoService) {
        this.skuMappingService = skuMappingService;
        this.skuList = skuList;
        this.shopList = shopList;
        this.skuMappingList = skuMappingList;
        this.dictBasicList = dictBasicList;
        this.listingInfoEntityList = listingInfoEntityList;
        this.listingInfoService = listingInfoService;

    }

    /**
     * 每解析一行执行一次
     *
     * @param skuMappingImportExcelDTO
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-06-28 18:12
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(SkuMappingImportExcelDTO skuMappingImportExcelDTO, AnalysisContext analysisContext) {
        List<String> msgList = FieldValidUtil.fieldValid(skuMappingImportExcelDTO);
        //注解验证信息
        List<String> errorMsgList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        String skuNo = skuMappingImportExcelDTO.getProductSkuNo();
        SkuVO sku = skuList.stream().filter(s -> s.getSkuNo().equals(skuNo)).findFirst().orElse(null);
        if (Objects.isNull(sku)) {
            errorMsgList.add("产品sku不存在");
        }
        //店铺名称
        String shopName = skuMappingImportExcelDTO.getShopName();
        ShopInfoEntity shop = shopList.stream().filter(s -> s.getName().equals(shopName)).findFirst().orElse(null);
        if (Objects.isNull(shop)) {
            errorMsgList.add("店铺不存在");
        }

        //平台名称
        String platformName = skuMappingImportExcelDTO.getPlatformName();
        DictBasicDTO.ViewDTO platform = dictBasicList.stream().filter(s -> s.getName().equals(platformName)).findFirst().orElse(null);
        if (Objects.isNull(platform)) {
            errorMsgList.add("平台不存在");
        }

        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            skuMappingImportExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(skuMappingImportExcelDTO);
            return;
        }
        //平台sku no
        String platformSkuNo = skuMappingImportExcelDTO.getPlatformSkuNo();

        String platformProductName = skuMappingImportExcelDTO.getPlatformProductName();
        ListingInfoEntity listingInfoEntity = listingInfoEntityList.stream().filter(l -> l.getPlatformSkuNo().
                equals(platformSkuNo)).findFirst().orElse(null);

        String listingId = "";
        if (Objects.nonNull(listingInfoEntity)) {
            listingId = listingInfoEntity.getId();
        }

        //平台标识
        String dictPlatform = platform.getValue();
        RuleTypeEnum platformType = RuleTypeEnum.PLATFORM;
        //已对应的平台sku
        String finalListingId = listingId;
        List<SkuMappingEntity> excelList = skuMappingList.stream().filter(s -> s.getListingId().equals(finalListingId)
                && dictPlatform.equals(s.getDictPlatform())
                && platformType.equals(s.getType())
        ).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(excelList)) {
            errorMsgList.add("相同平台sku只能对应一个平台sku");
        }
        long count = addSkuMappingList.stream().filter(a -> a.getListingId().equals(finalListingId) &&
                dictPlatform.equals(a.getDictPlatform())).count();
        if (count > 0) {
            errorMsgList.add("相同平台sku只能对应一个平台sku");
        }
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            skuMappingImportExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(skuMappingImportExcelDTO);
            return;
        }

        if (Objects.isNull(listingInfoEntity)) {
            listingId = IdWorker.getIdStr();
            ListingInfoEntity addListingInfoEntity = new ListingInfoEntity();
            addListingInfoEntity.setId(listingId);
            addListingInfoEntity.setType(RuleTypeEnum.PLATFORM.getCode());
            addListingInfoEntity.setPlatformSkuNo(platformSkuNo);
            addListingInfoEntity.setPlatformSkuName(platformProductName);
            addListingInfoEntity.setMatchResult(Boolean.TRUE);
            addListingInfoEntityList.add(addListingInfoEntity);
        }
        LocalDateTime now = LocalDateTime.now();
        SkuMappingEntity add = new SkuMappingEntity();
        add.setDictPlatform(dictPlatform);
        add.setPlatformName(platformName);
        add.setProductSkuId(sku.getSkuId());
        add.setProductSkuNo(sku.getSkuNo());
        add.setShopId(shop.getId());
        add.setListingId(listingId);
        add.setType(platformType);
        //生效时间
        add.setEffectiveTime(now);
        add.setExpireTime(now.plusYears(MathUtil.NUMBER_100));
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


    public List<SkuMappingImportExcelDTO> getErrorList() {
        return errorList;
    }
}
