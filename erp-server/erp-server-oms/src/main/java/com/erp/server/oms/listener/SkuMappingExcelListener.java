package com.erp.server.oms.listener;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.dto.excel.SkuMappingImportExcelDTO;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.model.oms.enums.ListingInfoPlatformStatusEnum;
import com.erp.model.oms.enums.ListingMatchResultEnum;
import com.erp.model.oms.enums.ListingSourceTypeEnum;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.plm.entity.ProductUnitEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
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
public class SkuMappingExcelListener extends AnalysisEventListener<SkuMappingImportExcelDTO> {

    /**
     * 已审核消息
     */
    private List<SkuVO> skuList;

    /**
     * 店铺信息
     */
    private ShopInfoService shopInfoService;

    /**
     * sku 映射信息
     */
    private List<SkuMappingEntity> skuMappingList;

    /**
     * 对应平台的信息
     */
    private List<DictBasicDTO.ViewDTO> dictBasicList;

    /**
     * 单位
     */
    private List<ProductUnitEntity> unitList;

    /**
     * 原产地
     */
    private  List<DictBasicDTO.ViewDTO> originList;

    /**
     * listing 信息
     */
    private List<ListingInfoEntity> listingInfoEntityList;

    private SkuMappingService skuMappingService;

    private ListingInfoService listingInfoService;

    private InvoiceTaxService invoiceTaxService;


    private List<SkuMappingEntity> addSkuMappingList = new ArrayList<>(10);

    /**
     * listing 信息
     */
    private List<ListingInfoEntity> addListingInfoEntityList = new ArrayList<>(10);


    /**
     * 导入错误数据
     */
    private List<SkuMappingImportExcelDTO> errorList = new ArrayList<>(10);

    /**
     * 更新的信息
     */
    private List<SkuMappingEntity> updateSkuMappingList = new ArrayList<>(10);

    /**
     * 更新的listing
     */
    private List<ListingInfoEntity> updateListingInfoList = new ArrayList<>(10);

    /**
     * 发票税务信息
     */
    private List<InvoiceTaxDTO.UpdateDTO> invoiceTaxList = new ArrayList<>();

    private final OperateLogService operateLogService;

    private final List<String> removeIds = new ArrayList<>();
    private final List<Pair<String, String>> addLogPairList = new ArrayList<>();

    private final List<Pair<String, String>> updateLogPairList = new ArrayList<>();
    public SkuMappingExcelListener(SkuMappingService skuMappingService,
                                   List<ProductUnitEntity> unitList,
                                   List<DictBasicDTO.ViewDTO> originList,
                                   List<SkuVO> skuList,
                                   ShopInfoService shopInfoService, List<SkuMappingEntity> skuMappingList,
                                   List<DictBasicDTO.ViewDTO> dictBasicList,
                                   List<ListingInfoEntity> listingInfoEntityList,
                                   ListingInfoService listingInfoService,
                                   OperateLogService operateLogService,
                                   InvoiceTaxService invoiceTaxService) {
        this.skuMappingService = skuMappingService;
        this.unitList = unitList;
        this.originList = originList;
        this.skuList = skuList;
        this.shopInfoService = shopInfoService;
        this.skuMappingList = skuMappingList;
        this.dictBasicList = dictBasicList;
        this.listingInfoEntityList = listingInfoEntityList;
        this.listingInfoService = listingInfoService;
        this.operateLogService = operateLogService;
        this.invoiceTaxService = invoiceTaxService;
    }

    private Set<String> existSet = new HashSet<>();

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
        LocalDateTime effectiveTime = LocalDateUtil.parseStrToLocalTime(skuMappingImportExcelDTO.getEnabledTime());
        if(Objects.isNull(effectiveTime)){
            errorMsgList.add("启用时间[时间]格式不正确");
        }
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        //平台名称
        String platformName = skuMappingImportExcelDTO.getPlatformName();
        DictBasicDTO.ViewDTO platform = dictBasicList.stream()
                .filter(s -> s.getName().equalsIgnoreCase(platformName) || s.getValue().equalsIgnoreCase(platformName))
                .findFirst().orElse(null);
        if (null == platform) {
            errorMsgList.add("平台不存在");
            skuMappingImportExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(skuMappingImportExcelDTO);
            return;
        }

        String key = skuMappingImportExcelDTO.getPlatformName() + skuMappingImportExcelDTO.getShopName() + skuMappingImportExcelDTO.getPlatformSkuNo() + skuMappingImportExcelDTO.getPlatformProductId();

        if(existSet.contains(key)){
            errorMsgList.add("平台名称、店铺名称、平台sku、平台产品ID重复");
            skuMappingImportExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(skuMappingImportExcelDTO);
            return;
        } else {
            existSet.add(key);
        }

        //单位
        if (CharSequenceUtil.isNotBlank(skuMappingImportExcelDTO.getUnit())) {
            ProductUnitEntity productUnitEntity = unitList.stream().filter(obj -> CharSequenceUtil.equals(obj.getName(), skuMappingImportExcelDTO.getUnit())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(productUnitEntity)) {
                errorMsgList.add("单位不存在");
            }
        }
        //原产地
        String dictOrigin = "";
        if (CharSequenceUtil.isNotBlank(skuMappingImportExcelDTO.getDictOriginNo())) {
            DictBasicDTO.ViewDTO origin = originList.stream().filter(obj -> CharSequenceUtil.equals(obj.getRemark(), skuMappingImportExcelDTO.getDictOriginNo())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(origin)) {
                errorMsgList.add("原产地不存在");
            } else {
                dictOrigin = origin.getValue();
            }
        }

        //店铺名称
        String shopName = skuMappingImportExcelDTO.getShopName();
        List<ShopInfoDTO.ListDTO> shopList = shopInfoService.listShopByName(Collections.singletonList(shopName));
        ShopInfoDTO.ListDTO shop = shopList.stream()
                .filter(s -> s.getName().equals(shopName))
                .filter(s -> s.getDictPlatform().equalsIgnoreCase(platform.getValue()))
                .findFirst()
                .orElse(null);
        if (Objects.isNull(shop)) {
            errorMsgList.add(ApiError.SHOP_NOT_EXIST_NO_PERMISSION.msg);
            skuMappingImportExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(skuMappingImportExcelDTO);
            return;
        }
        // 查询该店铺所有平台sku
        ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
        paramDTO.setPlatform(platform.getValue());
        paramDTO.setShopIdList(Collections.singletonList(shop.getId()));
        paramDTO.setType(RuleTypeEnum.B2C_PLATFORM.getCode());
        paramDTO.setPlatformSkuNoList(Collections.singletonList(skuMappingImportExcelDTO.getPlatformSkuNo()));
        paramDTO.setPlatformSpuNoList(CharSequenceUtil.isNotBlank(skuMappingImportExcelDTO.getPlatformProductId()) ? Collections.singletonList(skuMappingImportExcelDTO.getPlatformProductId()) : null);
//        paramDTO.setIsExpire(false);
        // 所有包含历史映射关系
        List<ListingInfoWithSkuMappingDTO> listDto = skuMappingService.findListDto(paramDTO);
        String listingId = "";

        // 已接入平台不允许新增
        boolean isApiPlatform = PlatformDictEnum.hasConnectionPlatform().contains(platform.getValue());
        if (CollectionUtils.isEmpty(listDto) && isApiPlatform){
            errorMsgList.add("亚马逊, 速卖通, Shopify, 虾皮, 沃尔玛不允许新增");
            skuMappingImportExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(skuMappingImportExcelDTO);
            return;
        }

        ListingInfoWithSkuMappingDTO mappingDto = listDto.stream().filter(e -> !e.getIsExpire()).findFirst().orElse(new ListingInfoWithSkuMappingDTO());
//        if (null == mappingDto){
//            errorMsgList.add("平台sku信息不存在");
//            skuMappingImportExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
//            errorList.add(skuMappingImportExcelDTO);
//            return;
//        }


        String skuNo = skuMappingImportExcelDTO.getProductSkuNo();

     /*   // 校验不允许重复历史
        long historyCount = listDto.stream().filter(e -> e.getProductSkuNo().equals(skuNo)).count();
        if (0 < historyCount){
            errorMsgList.add( CharSequenceUtil.format("当前映射关系在【{}】已存在过，无法修改", skuNo));
            skuMappingImportExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(skuMappingImportExcelDTO);
            return;
        }*/

        // 设置当前listingId
        listingId = mappingDto.getListingId();


        SkuVO sku = skuList.stream().filter(s -> s.getSkuNo().equals(skuNo)).findFirst().orElse(null);
        if (Objects.isNull(sku)) {
            errorMsgList.add("产品sku不存在");
        }

        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            skuMappingImportExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(skuMappingImportExcelDTO);
            return;
        }
        //平台标识
        String dictPlatform = platform.getValue();
        RuleTypeEnum platformType = RuleTypeEnum.B2C_PLATFORM;

        //平台sku no
        String platformSkuNo = skuMappingImportExcelDTO.getPlatformSkuNo();

        String platformProductName = skuMappingImportExcelDTO.getPlatformProductName();
        String platformProductId = skuMappingImportExcelDTO.getPlatformProductId();
        String finalListingId1 = listingId;
        ListingInfoEntity listingInfoEntity = listingInfoEntityList.stream()
                .filter(l -> l.getId().equalsIgnoreCase(finalListingId1))
                .findFirst().orElse(null);

        //已对应的平台sku
        String finalListingId = listingId;
        List<String> existListingIds = listDto.stream().filter(e -> !e.getIsExpire()).map(v->v.getListingId()).collect(Collectors.toList());
        List<SkuMappingEntity> excelList = skuMappingList.stream().filter(s -> existListingIds.contains(s.getListingId())
                && CharSequenceUtil.equals(dictPlatform,s.getDictPlatform())
                && platformType.equals(s.getType())
        ).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(excelList)) {
            // 修改对应关系
            SkuMappingEntity skuMappingEntity = excelList.stream().findFirst().orElse(null);
            excelList.forEach(v->{
                //删除原来的，再新增一条，与编辑逻辑保持一致
                v.setExpireTime(LocalDateTime.now());
                v.setIsExpire(Boolean.TRUE);
                v.setIsDeleted(true);
                removeIds.add(v.getId());
            });
            if (null != skuMappingEntity){

                SkuMappingEntity addSkuMapping = new SkuMappingEntity();
                addSkuMapping.setShopId(skuMappingEntity.getShopId());
                addSkuMapping.setType(RuleTypeEnum.B2C_PLATFORM);
                addSkuMapping.setProductSkuId(sku.getSkuId());
                addSkuMapping.setProductSkuNo(sku.getSkuNo());
                addSkuMapping.setListingId(listingId);
                addSkuMapping.setDictPlatform(platform.getValue());
                addSkuMapping.setPlatformName(platformName);
                //生效时间
                addSkuMapping.setEffectiveTime(LocalDateUtil.parseStrToLocalTime(skuMappingImportExcelDTO.getEnabledTime()));
                addSkuMapping.setExpireTime(addSkuMapping.getEffectiveTime().plusYears(MathUtil.NUMBER_100));

                updateSkuMappingList.add(skuMappingEntity);
                addSkuMappingList.add(addSkuMapping);
                List<String> contentList = operateLogService.getContentByObj(skuMappingEntity,addSkuMapping,"");
                for(String content:contentList){
                    Pair<String, String> pair = new Pair<>(addSkuMapping.getListingId(),content);
                    updateLogPairList.add(pair);
                }
                if (Objects.nonNull(listingInfoEntity)){
                    listingInfoEntity.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
                    if(StringUtils.isNotBlank(skuMappingImportExcelDTO.getPlatformStatusName())){
                        String platformStatus = ListingInfoPlatformStatusEnum.getCodeByName(skuMappingImportExcelDTO.getPlatformStatusName());
                        listingInfoEntity.setPlatformStatus(platformStatus);
                    }

                    updateListingInfoList.add(listingInfoEntity);
                    //税务信息
                    InvoiceTaxDTO.UpdateDTO invoiceTaxUpdateDTO = BeanUtil.toBean(skuMappingImportExcelDTO, InvoiceTaxDTO.UpdateDTO.class);
                    invoiceTaxUpdateDTO.setListingId(listingInfoEntity.getId());
                    invoiceTaxUpdateDTO.setDictOrigin(dictOrigin);
                    invoiceTaxList.add(invoiceTaxUpdateDTO);
                }
                return;
            }
            errorMsgList.add("平台sku已存在匹配关系");
        } else {
            // 已接入平台不允许新增
            if (PlatformDictEnum.hasConnectionPlatform().contains(dictPlatform)){
                errorMsgList.add("亚马逊, 速卖通, Shopify, 虾皮, 沃尔玛不允许新增");
            }
        }

        long count = addSkuMappingList.stream().filter(a -> a.getListingId().equals(finalListingId) &&
                dictPlatform.equals(a.getDictPlatform())).count();
        if (count > 0) {
            errorMsgList.add("相同平台sku只能对应一个平台sku");
        }
        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            skuMappingImportExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(skuMappingImportExcelDTO);
            return;
        }

        if (Objects.isNull(listingInfoEntity)) {
            listingId = IdWorker.getIdStr();
            ListingInfoEntity addListingInfoEntity = new ListingInfoEntity();
            addListingInfoEntity.setId(listingId);
            addListingInfoEntity.setType(RuleTypeEnum.B2C_PLATFORM.getCode());
            addListingInfoEntity.setPlatformSkuNo(platformSkuNo);
            addListingInfoEntity.setPlatformSpuNo(platformProductId);
            addListingInfoEntity.setPlatformSkuName(platformProductName);
            addListingInfoEntity.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
            addListingInfoEntity.setPlatform(dictPlatform);
            addListingInfoEntity.setSourceType(ListingSourceTypeEnum.SELF_ADD.getCode());
            addListingInfoEntityList.add(addListingInfoEntity);

            //税务信息
            InvoiceTaxDTO.UpdateDTO invoiceTaxUpdateDTO = BeanUtil.toBean(skuMappingImportExcelDTO, InvoiceTaxDTO.UpdateDTO.class);
            invoiceTaxUpdateDTO.setListingId(addListingInfoEntity.getId());
            invoiceTaxUpdateDTO.setDictOrigin(dictOrigin);
            invoiceTaxList.add(invoiceTaxUpdateDTO);
        }
        SkuMappingEntity add = new SkuMappingEntity();
        add.setDictPlatform(dictPlatform);
        add.setPlatformName(platform.getName());
        add.setProductSkuId(sku.getSkuId());
        add.setProductSkuNo(sku.getSkuNo());
        add.setShopId(shop.getId());
        add.setListingId(listingId);
        add.setType(platformType);
        //生效时间
        add.setEffectiveTime(LocalDateUtil.parseStrToLocalTime(skuMappingImportExcelDTO.getEnabledTime()));
        add.setExpireTime(add.getEffectiveTime().plusYears(MathUtil.NUMBER_100));
        addSkuMappingList.add(add);
        Pair<String, String> pair = new Pair<>(listingId,listingId);

        addLogPairList.add(pair);
    }


    //所有执行玩后 在执行
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (CollectionUtils.isNotEmpty(addListingInfoEntityList)) {
            listingInfoService.saveBatch(addListingInfoEntityList);
        }

        if (CollectionUtils.isNotEmpty(updateSkuMappingList)){
            if (!skuMappingService.updateBatchById(updateSkuMappingList)){
                throw new ServiceException("映射关系更新异常");
            }
        }
        if (CollectionUtils.isNotEmpty(updateListingInfoList)){
            if (!listingInfoService.updateBatchById(updateListingInfoList)){
                throw new ServiceException("Listing更新异常");
            }
        }
        if (CollectionUtils.isNotEmpty(addSkuMappingList)) {
            skuMappingService.saveBatch(addSkuMappingList);
        }

        if (CollectionUtils.isNotEmpty(invoiceTaxList)) {
            invoiceTaxService.importUpdate(invoiceTaxList);
        }

        if(CollectionUtils.isNotEmpty(removeIds)){
            skuMappingService.removeByIds(removeIds);
        }
        if (CollectionUtils.isNotEmpty(addLogPairList)) {
            operateLogService.batchAddModuleOperateLog( CharSequenceUtil.format("用户【{}】新增了sku映射表", UserContext.getDefaultLoginUser().getUserName())+"id为【%s】", ModuleTypeEnum.LISTING_INFO.getCode(), addLogPairList,"新增操作");
        }
        if (CollectionUtils.isNotEmpty(updateLogPairList)) {
            operateLogService.batchAddModuleOperateLog( CharSequenceUtil.format("用户【{}】编辑sku映射表",UserContext.getDefaultLoginUser().getUserName())+"，【%s】", ModuleTypeEnum.LISTING_INFO.getCode(), updateLogPairList,"编辑操作");
        }
    }


    public List<SkuMappingImportExcelDTO> getErrorList() {
        return errorList;
    }
}
