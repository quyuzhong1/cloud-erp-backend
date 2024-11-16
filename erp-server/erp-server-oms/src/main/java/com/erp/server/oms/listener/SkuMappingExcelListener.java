package com.erp.server.oms.listener;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.excel.SkuMappingImportExcelDTO;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.model.oms.enums.ListingMatchResultEnum;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.service.ListingInfoService;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SkuMappingService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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

    /**
     * 更新的信息
     */
    private List<SkuMappingEntity> updateSkuMappingList = new ArrayList<>(10);

    /**
     * 更新的listing
     */
    private List<ListingInfoEntity> updateListingInfoList = new ArrayList<>(10);
    private final OperateLogService operateLogService;

    private final List<String> removeIds = new ArrayList<>();
    private final List<Pair<String, String>> addLogPairList = new ArrayList<>();

    private final List<Pair<String, String>> updateLogPairList = new ArrayList<>();
    public SkuMappingExcelListener(SkuMappingService skuMappingService, List<SkuVO> skuList,
                                   List<ShopInfoEntity> shopList, List<SkuMappingEntity> skuMappingList,
                                   List<DictBasicDTO.ViewDTO> dictBasicList,
                                   List<ListingInfoEntity> listingInfoEntityList,
                                   ListingInfoService listingInfoService,
                                   OperateLogService operateLogService) {
        this.skuMappingService = skuMappingService;
        this.skuList = skuList;
        this.shopList = shopList;
        this.skuMappingList = skuMappingList;
        this.dictBasicList = dictBasicList;
        this.listingInfoEntityList = listingInfoEntityList;
        this.listingInfoService = listingInfoService;
        this.operateLogService = operateLogService;
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

        //店铺名称
        String shopName = skuMappingImportExcelDTO.getShopName();
        ShopInfoEntity shop = shopList.stream()
                .filter(s -> s.getName().equals(shopName))
                .filter(s -> s.getDictPlatform().equalsIgnoreCase(platform.getValue()))
                .findFirst()
                .orElse(null);
        if (Objects.isNull(shop)) {
            errorMsgList.add("店铺在该平台不存在");
            skuMappingImportExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(skuMappingImportExcelDTO);
            return;
        }
        // 查询该店铺所有平台sku
        ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
        paramDTO.setPlatform(platform.getValue());
        paramDTO.setShopIdList(Collections.singletonList(shop.getId()));
        paramDTO.setType(RuleTypeEnum.PLATFORM.getCode());
        paramDTO.setPlatformSkuNoList(Collections.singletonList(skuMappingImportExcelDTO.getPlatformSkuNo()));
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

        // 已存在
        if (isApiPlatform){
            if( ListingMatchResultEnum.TRUE.getCode().equals(mappingDto.getMatchResult())){
                errorMsgList.add("该店铺平台sku已存在匹配关系");
                skuMappingImportExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(skuMappingImportExcelDTO);
                return;
            }
        }

        String skuNo = skuMappingImportExcelDTO.getProductSkuNo();

        // 校验不允许重复历史
        long historyCount = listDto.stream().filter(e -> e.getProductSkuNo().equals(skuNo)).count();
        if (0 < historyCount){
            errorMsgList.add(StrUtil.format("当前映射关系在【{}】已存在过，无法修改", skuNo));
            skuMappingImportExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(skuMappingImportExcelDTO);
            return;
        }

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
        RuleTypeEnum platformType = RuleTypeEnum.PLATFORM;

        //平台sku no
        String platformSkuNo = skuMappingImportExcelDTO.getPlatformSkuNo();

        String platformProductName = skuMappingImportExcelDTO.getPlatformProductName();
        String finalListingId1 = listingId;
        ListingInfoEntity listingInfoEntity = listingInfoEntityList.stream()
                .filter(l -> l.getId().equalsIgnoreCase(finalListingId1))
                .findFirst().orElse(null);

//        if (Objects.nonNull(listingInfoEntity)) {
//            listingId = listingInfoEntity.getId();
//            if (listingInfoEntity.getMatchResult()){
//                //存在错误数据则直接返回
//                errorMsgList.add("平台sku已存在匹配关系");
//                skuMappingImportExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
//                errorList.add(skuMappingImportExcelDTO);
//                return;
//            }
//        }


        //已对应的平台sku
        String finalListingId = listingId;
        List<SkuMappingEntity> excelList = skuMappingList.stream().filter(s -> s.getListingId().equals(finalListingId)
                && dictPlatform.equals(s.getDictPlatform())
                && platformType.equals(s.getType())
        ).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(excelList)) {
            // 修改对应关系
            SkuMappingEntity skuMappingEntity = excelList.stream().findFirst().orElse(null);
            if (null != skuMappingEntity){
                //删除原来的，再新增一条，与编辑逻辑保持一致
                skuMappingEntity.setExpireTime(LocalDateTime.now());
                skuMappingEntity.setIsExpire(Boolean.TRUE);
                skuMappingEntity.setIsDeleted(true);
                removeIds.add(skuMappingEntity.getId());

                SkuMappingEntity addSkuMapping = new SkuMappingEntity();
                addSkuMapping.setShopId(skuMappingEntity.getShopId());
                addSkuMapping.setType(RuleTypeEnum.PLATFORM);
                addSkuMapping.setProductSkuId(sku.getSkuId());
                addSkuMapping.setProductSkuNo(sku.getSkuNo());
                addSkuMapping.setListingId(listingId);
                addSkuMapping.setDictPlatform(platform.getValue());
                addSkuMapping.setPlatformName(platformName);
                //生效时间
                addSkuMapping.setEffectiveTime(LocalDateTime.now());
                addSkuMapping.setExpireTime(LocalDateTime.now().plusYears(MathUtil.NUMBER_100));

                updateSkuMappingList.add(skuMappingEntity);
                addSkuMappingList.add(addSkuMapping);
                List<String> contentList = operateLogService.getContentByObj(skuMappingEntity,addSkuMapping,"");
                for(String content:contentList){
                    Pair<String, String> pair = new Pair<>(addSkuMapping.getListingId(),content);
                    updateLogPairList.add(pair);
                }
                listingInfoEntity.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
                updateListingInfoList.add(listingInfoEntity);
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
            addListingInfoEntity.setType(RuleTypeEnum.PLATFORM.getCode());
            addListingInfoEntity.setPlatformSkuNo(platformSkuNo);
            addListingInfoEntity.setPlatformSkuName(platformProductName);
            addListingInfoEntity.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
            addListingInfoEntity.setPlatform(dictPlatform);
            addListingInfoEntityList.add(addListingInfoEntity);
        }
        LocalDateTime now = LocalDateTime.now();
        SkuMappingEntity add = new SkuMappingEntity();
        add.setDictPlatform(dictPlatform);
        add.setPlatformName(platform.getName());
        add.setProductSkuId(sku.getSkuId());
        add.setProductSkuNo(sku.getSkuNo());
        add.setShopId(shop.getId());
        add.setListingId(listingId);
        add.setType(platformType);
        //生效时间
        add.setEffectiveTime(now);
        add.setExpireTime(now.plusYears(MathUtil.NUMBER_100));
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

        if(CollectionUtils.isNotEmpty(removeIds)){
            skuMappingService.removeByIds(removeIds);
        }
        if (CollectionUtils.isNotEmpty(addLogPairList)) {
            operateLogService.batchAddModuleOperateLog(StrUtil.format("用户【{}】新增了sku映射表", UserContext.getDefaultLoginUser().getUserName())+"id为【%s】", ModuleTypeEnum.LISTING_INFO.getCode(), addLogPairList,"新增操作");
        }
        if (CollectionUtils.isNotEmpty(updateLogPairList)) {
            operateLogService.batchAddModuleOperateLog(StrUtil.format("用户【{}】编辑sku映射表",UserContext.getDefaultLoginUser().getUserName())+"，【%s】", ModuleTypeEnum.LISTING_INFO.getCode(), updateLogPairList,"编辑操作");
        }
    }


    public List<SkuMappingImportExcelDTO> getErrorList() {
        return errorList;
    }
}
