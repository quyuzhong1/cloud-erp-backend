package com.erp.server.wms.controller.api;


import cn.hutool.core.text.CharSequenceUtil;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.vo.ProductVO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 质检单
 *
 * @author lambda
 * @since 2023-04-14
 */
@RestController
@RequestMapping("/qcProduct")
public class QcProductController extends BaseController {


    @Resource
    private PlmTaskFeign plmTaskFeign;


    /**
     * 质检列表 获取 产品信息
     *
     * @param skuId
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.plm.vo.ProductVO.ProductPackVO>
     * @author yl
     * @date 2023-04-20 18:34
     */
    @GetMapping("/skuView")
    public ApiResult<ProductVO.ProductPackVO> getSkuInfo(@RequestParam("skuId") String skuId) {
        if (CharSequenceUtil.isBlank(skuId)) {
            throw new ServiceException(ApiError.ERROR_95107);
        }
        List<String> skuIdList = new ArrayList<>();
        skuIdList.add(skuId);
        List<ProductVO.ProductPackVO> packVOList = plmTaskFeign.getProductPackBySkuIds(skuIdList);
        if (CollectionUtils.isEmpty(packVOList)) {
            throw new ServiceException(ApiError.ERROR_95107);
        }
        return success(packVOList.get(0));
    }

}
