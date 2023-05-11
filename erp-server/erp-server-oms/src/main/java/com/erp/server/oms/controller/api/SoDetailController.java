package com.erp.server.oms.controller.api;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.SoDetailDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 销售管理-销售订单
 *
 * @author lambda
 * @since 2023-05-10
 */
@RestController
@RequestMapping("/soDetail")
public class SoDetailController extends BaseController {


    /**
     *根据skuId 获取产品明细
     */
    @GetMapping("/listSkuInfoBySkuId")
    public ApiResult<List<SoDetailDTO.SkuDTO>> listSkuInfoBySkuId(@RequestParam("skuId")String skuId) {

        return success();
    }


    /**
     *导入产品
     */
    @GetMapping("/importFile")
    public ApiResult<List<SoDetailDTO.ImportDTO>> listSkuInfoBySkuId(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {

        return success();
    }

}
