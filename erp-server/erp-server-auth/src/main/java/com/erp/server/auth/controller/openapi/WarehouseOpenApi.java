package com.erp.server.auth.controller.openapi;

import javax.annotation.Resource;
import javax.validation.Valid;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.openapi.CollectorPacksDTO;
import com.erp.model.sys.openapi.DimensionalWeightDTO;
import com.erp.model.sys.openapi.ReturnTrackingDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.PackingTaskFeign;
import com.erp.rpc.wms.feign.SoB2cDeliveryFeign;
import com.erp.server.auth.config.OpenApi;

/**
 * <p>
 * 对接汇创智能设备
 * @see <a href="https://ulanzichina.feishu.cn/docx/Ur9XdRkgdoQojYx2mIhcjR7knDf?from=from_copylink"> 对接文档</br>
 * </p>
 * 设备调用接口,返回尺寸重量视频地址图片地址相关文件
 */
@OpenApi
public class WarehouseOpenApi {

    @Resource
    private SoB2cDeliveryFeign soB2cDeliveryFeign;

    @Resource
    private PackingTaskFeign packingTaskFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    /**
     * 流水线称重 设备调用该接口 返回渠道口分货口1-9，9为异常口）成功根据渠道返回1-8的分货口，失败返回9的异常口
     * @param dto 参数
     */
    @OpenApi("dimensionalWeightPipeline")
    public ApiResult<String> dimensionalWeightPipeline(@Valid DimensionalWeightDTO dto) {
        //设备回传的称重量方信息
        return soB2cDeliveryFeign.dimensionalWeightPipeline(dto);
    }

    /**
     * 称重
     * @param dto 参数
     */
    @OpenApi("dimensionalWeight")
    public ApiResult<String> dimensionalWeight(@Valid DimensionalWeightDTO dto) {
        //设备回传的称重量方信息
        return packingTaskFeign.dimensionalWeight(dto);
    }

    /**
     * 品质测量
     * @param dto 参数
     */
    @OpenApi("dimensionalWeightMeasure")
    public ApiResult<String> dimensionalWeightMeasure(@Valid DimensionalWeightDTO dto) {
        String result = plmTaskFeign.dimensionalWeightMeasure(dto);
        return ApiResult.success(result);
    }

    /**
     * 速卖通集包
     * @param dto 参数
     */
    @OpenApi("dimensionalWeightPackage")
    public ApiResult<String> dimensionalWeightPackage(@Valid CollectorPacksDTO dto) {

        return ApiResult.success("");
    }

    /**
     * 退货跟踪
     * @param dto 参数
     */
    @OpenApi("dimensionalWeightReturn")
    public ApiResult<String> dimensionalWeightReturn(@Valid ReturnTrackingDTO dto) {

        return ApiResult.success("");
    }
}
