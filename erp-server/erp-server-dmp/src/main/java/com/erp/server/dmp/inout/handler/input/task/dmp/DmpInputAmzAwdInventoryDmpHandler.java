package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.common.business.enums.PlatformDictEnum;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.*;

/**
 * @Author: wtr
 * @Date: 2025/12/25 8:40
 * @Param:
 * @Return:
 * @Description:
 **/
@Service
@Scope("prototype")
public class DmpInputAmzAwdInventoryDmpHandler extends DmpInputDbConvertDmpHandler {

    @Resource
    private SkuMappingFeign skuMappingFeign;

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        super.afterConvertData(dmpInputDataDmpRelationMaps);

        List<String> mskuIds = new ArrayList<>();
        String shopId = "";
        for (List<Map<String, Object>> keyList : dmpInputDataDmpRelationMaps.keySet()) {
            for (Map<String, Object> keyMap : keyList) {
                if (keyMap.containsKey("sku")) {
                    Object msku = keyMap.get("sku");
                    if (msku != null) {
                        mskuIds.add(msku.toString());
                    }
                }

                if (StringUtils.isBlank(shopId)){
                    shopId = keyMap.get("nextLevelId").toString();
                }
            }
        }
        ListingInfoParamDTO listingInfoParamDTO = new ListingInfoParamDTO();
        listingInfoParamDTO.setPlatformSkuNoList(mskuIds);
        listingInfoParamDTO.setPlatform(PlatformDictEnum.AMAZON.getCode());
        listingInfoParamDTO.setShopIdList(Collections.singletonList(shopId));
        List<SkuMappingDTO.MappingSkuViewDTO> mappingSkuViewDTOS = skuMappingFeign.listByPlatformSkuNoAndPlatform(listingInfoParamDTO);

        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                dmpDataMap.put("msku",Objects.nonNull(dmpDataMap.get("sku")) && StringUtils.isNotBlank(dmpDataMap.get("sku").toString()) ? dmpDataMap.get("sku") : "");
                dmpDataMap.put("wareHouseId",dmpDataMap.get("nextLevelId").toString());
                if (!mappingSkuViewDTOS.isEmpty()) {
                    SkuMappingDTO.MappingSkuViewDTO sku = mappingSkuViewDTOS.stream()
                            .filter(item -> item.getPlatformSkuNo().equals(dmpDataMap.get("sku")))
                            .findFirst()
                            .orElse(null);
                    dmpDataMap.put("skuId",Objects.nonNull(sku) && Objects.nonNull(sku.getId()) ? sku.getId() : "");
                    dmpDataMap.put("skuNo",Objects.nonNull(sku) && Objects.nonNull(sku.getProductSkuNo()) ? sku.getProductSkuNo() : "");
                    dmpDataMap.put("productName",Objects.nonNull(sku) && Objects.nonNull(sku.getProductName()) ? sku.getProductName() : "");
                }

            }
        }
    }

}
