package com.erp.server.oms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.excel.SkuMapingImportExcelDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SkuMapingEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.DictBasicDTO;
import com.erp.server.oms.service.SkuMapingService;
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
 * @Description TODO
 * @Date 2023-06-28 18:07
 * @Created by yl
 */
public class SkuMapingExcelListener extends AnalysisEventListener<SkuMapingImportExcelDTO> {

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
    private List<SkuMapingEntity> skuMapingList;

    /**
     * 对应平台的信息
     */
    private List<DictBasicDTO.ViewDTO> dictBasicList;

    private SkuMapingService skuMapingService;

    private List<SkuMapingEntity> addSkuMapingList = new ArrayList<>(10);

    private List<SkuMapingEntity> updateSkuMapingList = new ArrayList<>(10);


    /**
     * 导入错误数据
     */
    private List<SkuMapingImportExcelDTO> errorList = new ArrayList<>(10);

    public SkuMapingExcelListener(SkuMapingService skuMapingService, List<SkuVO> skuList,
                                  List<ShopInfoEntity> shopList, List<SkuMapingEntity> skuMapingList,
                                  List<DictBasicDTO.ViewDTO> dictBasicList) {
        this.skuMapingService = skuMapingService;
        this.skuList = skuList;
        this.shopList = shopList;
        this.skuMapingList = skuMapingList;
        this.dictBasicList = dictBasicList;
    }

    /**
     * 每解析一行执行一次
     *
     * @param skuMapingImportExcelDTO
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-06-28 18:12
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(SkuMapingImportExcelDTO skuMapingImportExcelDTO, AnalysisContext analysisContext) {
        List<String> msgList = FieldValidUtil.fieldValid(skuMapingImportExcelDTO);
        //注解验证信息
        List<String> errorMsgList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        String skuNo = skuMapingImportExcelDTO.getProductSkuNo();
        SkuVO sku = skuList.stream().filter(s -> s.getSkuNo().equals(skuNo)).findFirst().orElse(null);
        if (Objects.isNull(sku)) {
            errorMsgList.add("产品sku 不存在");
        }
        //店铺名称
        String shopName = skuMapingImportExcelDTO.getShopName();
        ShopInfoEntity shop = shopList.stream().filter(s -> s.getName().equals(shopName)).findFirst().orElse(null);
        if (Objects.isNull(shop)) {
            errorMsgList.add("店铺 不存在");
        }

        //平台名称
        String platformName = skuMapingImportExcelDTO.getPlatformName();
        DictBasicDTO.ViewDTO platform = dictBasicList.stream().filter(s -> s.getName().equals(platformName)).findFirst().orElse(null);
        if (Objects.isNull(platform)) {
            errorMsgList.add("平台 不存在");
        }
        //平台标识
        String platformDict = platform.getValue();

        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            skuMapingImportExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(skuMapingImportExcelDTO);
            return;
        }
        //平台skuno
        String platformSkuNo = skuMapingImportExcelDTO.getPlatformSkuNo();

        //平台skuname
        String platformSkuName = skuMapingImportExcelDTO.getPlatformSkuName();
        LocalDateTime now = LocalDateTime.now();

        //已对应的平台sku
        List<SkuMapingEntity> alreadyList = skuMapingList.stream().filter(s -> s.getPlatformSkuNo().equals(platformSkuNo) && platformDict.equals(s.getPlatformDict())).
                collect(Collectors.toList());
        //设置失效时间为现在
        for (SkuMapingEntity already : alreadyList) {
            already.setExpireTime(now);
            already.setIsExpire(Boolean.TRUE);
        }
        updateSkuMapingList.addAll(alreadyList);
        SkuMapingEntity add = new SkuMapingEntity();
        add.setPlatformDict(platformDict);
        add.setPlatformName(platformName);
        add.setPlatformSkuNo(platformSkuNo);
        add.setPlatformSkuName(platformSkuName);
        add.setProductSkuId(sku.getSkuId());
        add.setProductSkuNo(sku.getSkuNo());
        add.setShopId(shop.getId());
        add.setMatchResult(Boolean.TRUE);
        //生效时间
        add.setEffectiveTime(now);
        add.setExpireTime(now.plusYears(MathUtil.NUMBER_100));
        addSkuMapingList.add(add);
    }


    //所有执行玩后 在执行
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (CollectionUtils.isNotEmpty(addSkuMapingList)) {
            skuMapingService.saveBatch(addSkuMapingList);
        }

        if (CollectionUtils.isNotEmpty(updateSkuMapingList)) {
            skuMapingService.updateBatchById(updateSkuMapingList);
        }
    }


    public List<SkuMapingImportExcelDTO> getErrorList() {
        return errorList;
    }
}
