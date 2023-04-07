package com.erp.server.plm.rocketmq.sync.kingdee.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.plm.entity.*;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.rocketmq.sync.kingdee.SyncKingdeeProductDetailService;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/8 18:13
 */
@Service
public class SyncKingdeeProductDetailServiceImpl implements SyncKingdeeProductDetailService {

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private ProductSaleService productSaleService;

    @Resource
    private ProductLogisticsService productLogisticsService;

    @Resource
    private ProductPackService productPackService;

    @Resource
    private ProductCostService productCostService;

    @Resource
    private ProductPurchaseService productPurchaseService;

    @Resource
    private BasicCategoryService basicCategoryService;

    @Resource
    private BasicDictService basicDictService;

    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private MQProducerService mQProducerService;

    /**
     * 组装数据发送到金蝶
     */
    @Override
    public void syncDataToKingdee(ProductDetailEntity entity) {
        //产品信息
        ProductInfoEntity productInfoEntity = productInfoService.getById(entity.getProductId());
        if (ObjectUtils.isEmpty(productInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        //销售信息
        ProductSaleEntity productSaleEntity = productSaleService.getBySkuId(entity.getId());
        //物流信息
        ProductLogisticsEntity productLogisticsEntity = productLogisticsService.getBySkuId(entity.getId());
        //包装信息
        ProductPackEntity productPackEntity = productPackService.getBySkuId(entity.getId());
        //成本信息
        ProductCostEntity productCostEntity = productCostService.getBySkuId(entity.getId());
        //采购信息
        ProductPurchaseEntity productPurchaseEntity = productPurchaseService.getBySkuId(entity.getId());

        Map<String, Object> resultMap = new HashMap<>();
        //金蝶id
        resultMap.put("syncKingdeeId",entity.getSyncKingdeeId());
        //sku
        resultMap.put("id", entity.getId());
        //sku
        resultMap.put("skuNo", entity.getSkuNo());
        //名称
        resultMap.put("name", entity.getName());
        //spu
        resultMap.put("spuNo", productInfoEntity.getSpuNo());
        //产品功能描述
        resultMap.put("functionDesc", productInfoEntity.getFunctionDesc());
        //属性
        resultMap.put("property", productInfoEntity.getProperty());
        //单位
        resultMap.put("unitName", entity.getUnitName());

        BasicCategoryEntity basicCategoryEntity = basicCategoryService.getById(productInfoEntity.getCategoryId());
        if (ObjectUtils.isNotEmpty(basicCategoryEntity)) {
            List<BasicCategoryEntity> basicCategoryList = basicCategoryService.listParentEntity(basicCategoryEntity.getId());
            if (CollectionUtils.isNotEmpty(basicCategoryList)) {
                //TODO代码结构优化，有异常立即抛出
                //一级分类
                BasicCategoryEntity basicCategoryEntity1 = basicCategoryList.stream().filter(obj -> "0".equals(obj.getPid())).findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(basicCategoryEntity1)) {
                    resultMap.put("oneLevelCategory", basicCategoryEntity1.getName());
                    //一级分类编码
                    resultMap.put("oneLevelCategoryCode", basicCategoryEntity1.getCode());
                    //二级分类
                    BasicCategoryEntity basicCategoryEntity2 = basicCategoryList.stream().filter(obj -> basicCategoryEntity1.getId().equals(obj.getPid())).findFirst().orElse(null);
                    if (ObjectUtils.isNotEmpty(basicCategoryEntity2)) {
                        resultMap.put("secondLevelCategory", basicCategoryEntity2.getName());
                        if (StringUtils.isNotBlank(basicCategoryEntity1.getCode()) && StringUtils.isNotBlank(basicCategoryEntity2.getCode())) {
                            //二级分类编码
                            resultMap.put("secondLevelCategoryCode", basicCategoryEntity1.getCode().concat(basicCategoryEntity2.getCode()));
                        }
                    }
                }
            }
        }
        //产品经理
        resultMap.put("chargeName", productInfoEntity.getChargeName());
        //销售信息
        if (ObjectUtils.isNotEmpty(productSaleEntity)) {
            //上市时间
            resultMap.put("listingTime", productSaleEntity.getListingTime());
        }
        //物流信息
        if (ObjectUtils.isNotEmpty(productLogisticsEntity)) {
            //报关中文名
            resultMap.put("declareChineseName", productLogisticsEntity.getDeclareChineseName());
            //报关英文名
            resultMap.put("declareEnglishName", productLogisticsEntity.getDeclareEnglishName());
            //报关申报价
            resultMap.put("declarePrice", productLogisticsEntity.getDeclarePrice());
            //产品属性
            if (StringUtils.isNotBlank(productLogisticsEntity.getProductPropertyId())) {
                List<String> list = Arrays.stream(productLogisticsEntity.getProductPropertyId().split(",")).collect(Collectors.toList());
                for (String id : list) {
                    BasicDictEntity declareProperty = basicDictService.getById(id);
                    if (ObjectUtils.isNotEmpty(declareProperty)) {
                        String value = declareProperty.getValue();
                        //产品属性（是否带电）
                        if ("内电".equals(value) || "可拆卸电池".equals(value) || "纯电池".equals(value)) {
                            resultMap.put("productProperty_electric", true);
                        }
                        //产品属性（是否带磁）
                        if ("带磁".equals(value)) {
                            resultMap.put("productProperty_magnetism", true);
                        }
                    }
                }
            }
            //海关编码
            resultMap.put("customsCode", productLogisticsEntity.getCustomsCode());
            //申报要素
            resultMap.put("declareElement", productLogisticsEntity.getDeclareElement());
        }
        //包装信息
        if (ObjectUtils.isNotEmpty(productPackEntity)) {
            //毛重
            resultMap.put("grossWeight", productPackEntity.getGrossWeight());
            //净重
            resultMap.put("netWeight", productPackEntity.getNetWeight());
            //产品尺寸
            String productSize = productPackEntity.getProductSize();
            if (StringUtils.isNotBlank(productSize)) {
                List<String> productSizeList = Arrays.stream(productSize.split("X")).collect(Collectors.toList());
                if (productSizeList.size() == 1) {
                    //产品尺寸-长(cm)
                    resultMap.put("productSize_length", productSizeList.get(0));
                } else if (productSizeList.size() == 2) {
                    //产品尺寸-长(cm)
                    resultMap.put("productSize_length", productSizeList.get(0));
                    //产品尺寸-宽(cm)
                    resultMap.put("productSize_width", productSizeList.get(1));
                } else {
                    //产品尺寸-长(cm)
                    resultMap.put("productSize_length", productSizeList.get(0));
                    //产品尺寸-宽(cm)
                    resultMap.put("productSize_width", productSizeList.get(1));
                    //产品尺寸-高(cm)
                    resultMap.put("productSize_height", productSizeList.get(2));
                }
            }
            //单箱数量
            resultMap.put("boxQty", productPackEntity.getBoxQty());
            //单箱重量
            resultMap.put("boxWeight", productPackEntity.getBoxWeight());
            //单箱尺寸
            String boxSize = productPackEntity.getBoxSize();
            if (StringUtils.isNotBlank(boxSize)) {
                List<String> boxSizeList = Arrays.stream(boxSize.split("X")).collect(Collectors.toList());
                if (boxSizeList.size() == 1) {
                    //产品尺寸-长(cm)
                    resultMap.put("boxSize_length", boxSizeList.get(0));
                } else if (boxSizeList.size() == 2) {
                    //产品尺寸-长(cm)
                    resultMap.put("boxSize_length", boxSizeList.get(0));
                    //产品尺寸-宽(cm)
                    resultMap.put("boxSize_width", boxSizeList.get(1));
                } else {
                    //产品尺寸-长(cm)
                    resultMap.put("boxSize_length", boxSizeList.get(0));
                    //产品尺寸-宽(cm)
                    resultMap.put("boxSize_width", boxSizeList.get(1));
                    //产品尺寸-高(cm)
                    resultMap.put("boxSize_height", boxSizeList.get(2));
                }
            }
        }
        //成本信息
        if (ObjectUtils.isNotEmpty(productCostEntity)) {
            //实际不含税成本
            resultMap.put("actualNoTaxCost", productCostEntity.getActualNoTaxCost());
            //实际含税成本
            resultMap.put("actualTaxCost", productCostEntity.getActualTaxCost());
        }
        //采购信息
        if (ObjectUtils.isNotEmpty(productPurchaseEntity)) {
            //MOQ(最小起订量)
            resultMap.put("moq", productPurchaseEntity.getMoq());
            //EAN码
            resultMap.put("ean", productPurchaseEntity.getEan());
            if (StringUtils.isNotBlank(productPurchaseEntity.getPurchaseUserId())) {
                FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(productPurchaseEntity.getPurchaseUserId());
                if (ObjectUtils.isNotEmpty(findUserDTO)) {
                    //采购员
                    resultMap.put("purchaseUser", findUserDTO.getUserName());
                }
            }
            //一级供应商
            resultMap.put("mainSupplier", productPurchaseEntity.getMainSupplier());
        }
        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_PRODUCT_DETAIL_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //mq发送成更新业务表状态及时间
                return productDetailService.updateSyncKingdeeStatus(entity.getId(),SyncKingdeeStatusEnum.IN_SYNC.getCode(),"");
            }
           return Boolean.TRUE;
        });
    }

}
