package com.sdk.oms.mercado.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.AbstractProductHandler;
import com.common.business.utils.RedisUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.HttpCommonUtil;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.sdk.oms.mercado.dto.MercadoListingDTO;
import com.sdk.oms.mercado.dto.MercadoShopInfoDTO;
import com.sdk.oms.mercado.dto.mercado.listing.ListingViewDTO;
import com.sdk.oms.mercado.service.MercadoSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 沃尔玛商品信息
 * @Author Luo_WG
 * @Date 2023/10/18 11:24
 **/
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.MERCADOLIBRE)
@BusinessType(BusinessTypeEnum.PRODUCT)
public class MercadoListingHandler extends AbstractProductHandler<MercadoListingDTO, PlatformProductDTO> {
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private MercadoSdkClientService mercadoSdkClientService;

    public static void main(String[] args) {
        String baseUrl = "https://api.mercadolibre.com/items";
//        String baseUrl = "https://api.mercadolibre.com/products/CBT1870347315";

        //入参
        HashMap<String, Object> params = new HashMap<>(1);
        params.put("ids", "CBT1870347315,CBT1908735880,CBT1908723878,CBT1885832065,CBT1908898756,CBT1908873808,CBT1908858870,CBT1908815982,CBT1908713864,CBT1886138265,CBT1886113867,CBT1886022863,CBT1885988119,CBT1910174848,CBT1910137756,CBT1910109186,CBT1910107728,CBT1910078390,CBT1910013762,CBT1909985010");

        //设置请求头
        Map<String, String> headerMap = new HashMap<>(1);
        headerMap.put("Authorization", "Bearer APP_USR-3457166802805723-031821-be13aaddbd3636599e087244fd12b3dd-1509269799");

        //拉取数据
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(baseUrl, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
        if (!Objects.equals(apiResult.getCode(), 200)) {
            log.error("调用url={},入参params={}, 美客多Listing数据失败，返回值 responseMap={}", baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 美客多Listing数据失败，返回值 responseMap={}",
                    baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult)));
        }
    }

    @Override
    public List<MercadoListingDTO> download(JobTaskDTO data) {
        //  根据店铺ID获取授权
        MercadoShopInfoDTO shopInfoDTO = mercadoSdkClientService.getShopInfoByShopId(data.getShopId());
        if (null == shopInfoDTO) {
//            log.error("[美客多商品下载]  获取 token 失败: shopId={}", data.getShopId());
            return Collections.emptyList();
        }

        //发送请求
        List<ListingViewDTO> resultsBeanList = mercadoSdkClientService.sendMercadoGetListing(shopInfoDTO);

        // 返回下载源数据
        return resultsBeanList.stream()
                .map(e -> new MercadoListingDTO(e.getBody(), data))
                .collect(Collectors.toList());
    }

    @Override
    public List<PlatformProductDTO> convert(List<MercadoListingDTO> sourceDataList) {
        // 包含数据过滤数据 数据转换 数据合并拆分等操作
        return sourceDataList.stream()
                // 组装
                .map(MercadoListingDTO::convertDTO).collect(Collectors.toList());
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP.getDesc();
    }






}
