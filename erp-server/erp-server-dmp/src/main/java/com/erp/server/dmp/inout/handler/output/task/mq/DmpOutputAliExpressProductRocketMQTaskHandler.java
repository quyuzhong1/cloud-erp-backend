package com.erp.server.dmp.inout.handler.output.task.mq;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.dto.WdtReturnOrderDTO;
import com.common.business.dto.WdtReturnOrderDetailDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpSoReturnDetailEntity;
import com.erp.model.dmp.entity.DmpSoReturnInfoEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

@Service
@Scope("prototype")
public class DmpOutputAliExpressProductRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler{

	@Override
	public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
		Map<DmpCfgInputConvertEntity, List<Map<String, Object>>> changeConvertInputMongoEntityListMaps = dmpRequest.getChangeConvertInputMongoEntityListMaps();
		List<Map<String, Object>> changeProductDetailList = null;
		for(Map.Entry<DmpCfgInputConvertEntity, List<Map<String, Object>>> changeConvertInputMongoEntityListMap : changeConvertInputMongoEntityListMaps.entrySet()) {
			changeProductDetailList = changeConvertInputMongoEntityListMap.getValue();
		}
		Map<String, String> map = new HashMap<>();
		if(CollUtil.isNotEmpty(changeProductDetailList)) {
			String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
			for(Map<String, Object> changeProductDetail : changeProductDetailList) {
				PlatformProductDTO product = this.convert(changeProductDetail, cfgOutputId);
				if(product != null) {
					map.put(changeProductDetail.get("product_id").toString(), JSON.toJSONString(product));
				}
			}
		}
		return map;
	}
	
	/**
     * 解析订单数据
     **/
    public PlatformProductDTO convert(Map<String, Object> changeProductDetail , String cfgOutputId) {
    	if(this.validateDataBlack(changeProductDetail, cfgOutputId)) {
    		return null;
    	}
    	PlatformProductDTO product = new PlatformProductDTO();
    	
//    	product.setPlatform(dto.getPlatform());
//        // 平台sku no
//        product.setPlatformProductNo(changeProductDetail.get("product_id").toString());
//        // 平台sku 名
//        product.setPlatformProductName(sourceProduct.getSubject());
//        product.setPlatformSkuNo(StringUtils.isBlank(item.getSkuCode())? "" : item.getSkuCode());
//
//        product.setPlatformSkuName(sourceProduct.getSubject());
//        // 类型 platform 平台  warehouse 仓库
//        product.setPlatformType("platform");
//        String imageUrls = sourceProduct.getImageUrls();
//        if (StringUtils.isBlank(imageUrls)) {
//            product.setProductImageUrl("");
//        } else {
//            String imageUrl = imageUrls.split(";")[0];
//            product.setProductImageUrl(imageUrl);
//        }
//        product.setShopId(dto.getShopId());
//        // 包装信息
//        String packing = StrUtil.format("长度:{}cm;宽度:{}cm;高度:{}cm;重量:{}kg;", sourceProduct.getPackageLength(), sourceProduct.getPackageWidth(), sourceProduct.getPackageHeight(), sourceProduct.getGrossWeight());
//        product.setProductPacking(packing);
//        String gmtModified = sourceProduct.getGmtModified();
//        product.setPlatformUpdateTime(LocalDateUtil.parseStrToLocalTime(gmtModified));
//        product.setPlatformSkuId(item.getSkuId());
//
//        // 平台唯一标识=平台skuId + 店铺ID
//        String uniqueId = StrUtil.format("{}_{}", item.getSkuId(), dto.getShopId());
//        product.setUniqueId(uniqueId);
    	
        return product;
    }

}
