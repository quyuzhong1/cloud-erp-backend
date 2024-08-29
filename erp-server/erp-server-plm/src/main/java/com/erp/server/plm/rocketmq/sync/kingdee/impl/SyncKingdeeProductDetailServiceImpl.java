package com.erp.server.plm.rocketmq.sync.kingdee.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.LengthConverterUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.plm.entity.*;
import com.erp.model.sys.dto.PlmCfgSettingDTO;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.constant.ProductConstant;
import com.erp.server.plm.rocketmq.sync.kingdee.SyncKingdeeProductDetailService;
import com.erp.server.plm.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0

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
    private SysUserFeign sysUserFeign;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private CfgSettingService cfgSettingService;
    
    @Resource
    private PlmPushMsgService plmPushMsgService;

    /**
     * 组装数据发送到金蝶
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public DmpPushTaskEntity syncDataToKingdee(ProductDetailEntity entity, String operate) {

        Map<String, Object> resultMap = new HashMap<>();
        //sku
        resultMap.put("id", entity.getId());
        //sku
        resultMap.put("skuNo", entity.getSkuNo());
        //名称
        resultMap.put("name", entity.getName());
        //金蝶id
        resultMap.put("syncKingdeeId",entity.getSyncKingdeeId());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);

        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            return saveTask(entity,operate,resultMap);
        }

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
            resultMap.put("listingTime", LocalDateTimeUtil.format(productSaleEntity.getListingTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd")) );
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
                        String remark = declareProperty.getRemark();
                        //产品属性（是否带电）
                        if (StringUtils.isNotEmpty(remark) && "isElectric".equals(remark)) {
                            resultMap.put("productProperty_electric", true);
                        }
                        //产品属性（是否带磁）
                        if (StringUtils.isNotEmpty(remark) && "isMagnetism".equals(remark)) {
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
            //产品尺寸-长(cm)
            resultMap.put("productSize_length", LengthConverterUtil.mmToCm(productPackEntity.getProductLength()));
            //产品尺寸-宽(cm)
            resultMap.put("productSize_width", LengthConverterUtil.mmToCm(productPackEntity.getProductWidth()));
            //产品尺寸-高(cm)
            resultMap.put("productSize_height", LengthConverterUtil.mmToCm(productPackEntity.getProductHeight()));
            //单箱数量
            resultMap.put("boxQty", productPackEntity.getBoxQty());
            //单箱重量
            resultMap.put("boxWeight", productPackEntity.getBoxWeight());
            //单箱尺寸
            //产品尺寸-长(cm)
            resultMap.put("boxSize_length", LengthConverterUtil.mmToCm(productPackEntity.getBoxLength()));
            //产品尺寸-宽(cm)
            resultMap.put("boxSize_width", LengthConverterUtil.mmToCm(productPackEntity.getBoxWidth()));
            //产品尺寸-高(cm)
            resultMap.put("boxSize_height", LengthConverterUtil.mmToCm(productPackEntity.getBoxHeight()));
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
        //获取系统配置的物料属性控制
        List<PlmCfgSettingDTO.MaterialAttributeControlDetail> materialAttributeControlDetailList = cfgSettingService.view().getMaterialAttributeControl().getDetailList();
        if(CollectionUtils.isNotEmpty(materialAttributeControlDetailList)){
            PlmCfgSettingDTO.MaterialAttributeControlDetail materialAttributeControlDetail = materialAttributeControlDetailList.stream().filter(v->v.getMaterialAttributeList().contains(productInfoEntity.getPropertyId())).findFirst().orElse(null);
            if(Objects.nonNull(materialAttributeControlDetail)) {
                resultMap.put("allowProduction", materialAttributeControlDetail.isAllowProduction());
                resultMap.put("allowInventory", materialAttributeControlDetail.isAllowInventory());
                resultMap.put("allowPurchase", materialAttributeControlDetail.isAllowPurchase());
                resultMap.put("allowSubContract", materialAttributeControlDetail.isAllowSubContract());
                resultMap.put("allowTransferAssets", materialAttributeControlDetail.isAllowTransferAssets());
                resultMap.put("allowSale", materialAttributeControlDetail.isAllowSale());
            }
        }
        //服务和费用类型不允许库存
        if (StrUtil.equals(productInfoEntity.getProperty(), ProductConstant.PRODUCT_PROPERTY_COST)
                || StrUtil.equals(productInfoEntity.getProperty(), ProductConstant.PRODUCT_PROPERTY_SERVICE))  {
            //不允许库存
            resultMap.put("allowInventory", Boolean.FALSE);
        }

        //生成任务
       return saveTask(entity,operate,resultMap);
    }


    /**
     * @description: 生成任务
     * @author Will
     * @date: 2023/10/16 9:17
     * @param entity
     * @param operate
     * @param resultMap
     */
    private DmpPushTaskEntity saveTask (ProductDetailEntity entity, String operate, Map<String, Object> resultMap) {
    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.PRODUCT_DETAIL.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
        	//添加推送任务
            DmpPushTaskFeignDTO taskFeignDTO = new DmpPushTaskFeignDTO();
            taskFeignDTO.setSourceId(entity.getId());
            taskFeignDTO.setSourceCode(entity.getSkuNo());
            taskFeignDTO.setSourceType(SourceTypeEnum.PRODUCT_DETAIL.getCode());
            taskFeignDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
            taskFeignDTO.setMqTag(RocketMqTagEnum.KINGDEE_PRODUCT_DETAIL_TAG.getName());
            taskFeignDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            taskFeignDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            taskFeignDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
            taskFeignDTO.setSyncOperate(operate);
            return dmpMqFeign.saveTask(taskFeignDTO);
        }
        
        PlmPushMsgEntity plmPushMsgEntity = new PlmPushMsgEntity();
        plmPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
        plmPushMsgEntity.setSourceType(SourceTypeEnum.PRODUCT_DETAIL.getCode());
        plmPushMsgEntity.setSourceId(entity.getId());
        plmPushMsgEntity.setSourceCode(entity.getSkuNo());
        plmPushMsgEntity.setSyncOperate(operate);
        plmPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        
        plmPushMsgService.save(plmPushMsgEntity);
        
        return null;
    }
}
