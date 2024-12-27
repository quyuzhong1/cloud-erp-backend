package com.erp.server.dmp.controller.api;


import com.common.business.dto.base.BaseResultDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.dmp.dto.DmpBasicSystemDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.dmp.service.DmpAmzReportInfoService;
import com.sdk.third.lingxing.dto.ProductInfo;
import com.sdk.third.lingxing.utils.LingxingApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 历史信息同记录
 *
 * @author Jim
 * @since 2024-01-18
 */
@Slf4j
@RestController
@RequestMapping("/history")
public class DmpHistorySyncThirdController extends BaseController {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    /**
     * 同步全量sku 到领星
     *
     * @param skuIdList skuIdList
     * @return ApiResult<String>
     */
    @PostMapping("/lingxing/product/sync")
    public ApiResult<?> add(@RequestBody List<String> skuIdList) {
        List<SkuVO> skuVOS;
        if (CollectionUtils.isEmpty(skuIdList)) {
            skuVOS = plmTaskFeign.listApproveSku();
        } else {
            skuVOS = plmTaskFeign.listBySkuNoList(skuIdList);
        }
        List<ProductInfo> collect = skuVOS.stream().map(e -> new ProductInfo(e.getSkuNo(), e.getSkuName(), e.getSkuId())).collect(Collectors.toList());
        for (ProductInfo productInfo : collect) {
            LingxingApiUtils.addOrUpdateProduct(productInfo);
        }
        return success();
    }
}
