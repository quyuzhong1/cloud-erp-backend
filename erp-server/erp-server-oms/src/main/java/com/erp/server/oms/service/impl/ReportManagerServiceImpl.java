package com.erp.server.oms.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.ReportDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.oms.service.ReportManagerService;
import com.erp.server.oms.service.ShopInfoService;
import com.erp.server.oms.service.SkuMappingService;
import com.erp.server.oms.service.SoB2cService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_SO_B2C_PRODUCT_SALES;

/**
 * 报关管理
 * @author Jim
 * @date 2025/1/15 20:34
 */
@Service
public class ReportManagerServiceImpl implements ReportManagerService {


    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private SoB2cService soB2cService;
    @Resource
    private SkuMappingService skuMappingService;
    @Resource
    private ShopInfoService shopInfoService;


    /**
     * 报表管理 销售统计
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.ReportDTO.ProductSalesPagingViewDTO>
     * @author yl
     * @date 2023-09-01 11:19
     */
    @Override
    public PagingVO<ReportDTO.ProductSalesPagingViewDTO> productSalesPaging(PagingDTO<ReportDTO.ProductSalesPagingParamDTO> dto) {
        ReportDTO.ProductSalesPagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<T> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        List<LocalDateTime> timeList = parseOrderCreationTime(dto.getParams().getAdvanceQueryDTOList());
        LocalDateTime startTime = timeList.get(0);
        LocalDateTime endTime = timeList.get(1);

        //sku 创建时间
        List<LocalDateTime> skuCreateTimeList = params.getSkuCreateTimeList();
        List<String> skuIdList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(skuCreateTimeList)) {
            List<ProductDetailEntity> skuList = plmTaskFeign.listByCreateTimeList(skuCreateTimeList);
            skuIdList = skuList.stream().map(ProductDetailEntity::getId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(skuIdList)) {
                return new PagingVO<>(new Page<>());
            }
        }
        IPage pageData = soB2cService.productSalesPaging(query, params, skuIdList);
        List<ReportDTO.ProductSalesPagingViewDTO> list = pageData.getRecords();
        Duration between = LocalDateTimeUtil.between(startTime, endTime);
        long diffDays = between.toDays();
        if (diffDays == 0) {
            diffDays = 1;
        }
        fillProductSalesList(list, diffDays);
        return new PagingVO<>(pageData);

    }

    /**
     * 填充销售订单数据
     *
     * @param list
     * @param diffDays
     */
    private void fillProductSalesList(List<ReportDTO.ProductSalesPagingViewDTO> list, long diffDays) {
        List<String> shopIdList = list.stream().map(ReportDTO.ProductSalesPagingViewDTO::getShopId).collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoList = CollectionUtils.isNotEmpty(shopIdList) ? shopInfoService.listByIds(shopIdList) : Collections.emptyList();
        //平台skuno
        List<String> platformSkuNoList = list.stream().map(ReportDTO.ProductSalesPagingViewDTO::getPlatformSkuNo).collect(Collectors.toList());

        List<SkuMappingDTO.SkuDTO> skuInfoList = skuMappingService.listByPlatformSkuNoList(platformSkuNoList);
        for (ReportDTO.ProductSalesPagingViewDTO item : list) {
            String shopId = item.getShopId();
            //平台sku
            String platformSkuNo = item.getPlatformSkuNo();
            SkuMappingDTO.SkuDTO sku = skuInfoList.stream().filter(s -> s.getPlatformSkuNo().equals(platformSkuNo)).
                    findFirst().orElse(null);
            String productSkuNo = "";
            String sellerSkuNo = "";
            if (Objects.nonNull(sku)) {
                productSkuNo = sku.getProductSkuNo();
                sellerSkuNo = sku.getFlagSkuNo();
            }
            item.setProductSkuNo(productSkuNo);
            item.setSellerSkuNo(sellerSkuNo);
            String shopName = shopInfoList.stream().filter(s -> s.getId().equals(shopId)).
                    findFirst().map(ShopInfoEntity::getName).orElse("");
            item.setShopName(shopName);
            int qty = null == item.getQty() ? 0 : item.getQty();
            Integer avgQty = Math.toIntExact(qty / diffDays);
            item.setAvgQty(avgQty);
            BigDecimal amount = item.getAmount();
            BigDecimal avgAmount = amount.divide(new BigDecimal(diffDays), 4, RoundingMode.HALF_UP);
            item.setAvgAmount(avgAmount);

        }
    }

    /**
     * 导出 销售统计
     *
     * @param params
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-09-04 16:39
     */
    @Override
    public Boolean productSalesExport(ReportDTO.ProductSalesPagingParamDTO params) {
        downloadTaskFeign.saveDownloadTask("产品销售统计", EXPORT_OMS_SO_B2C_PRODUCT_SALES.getCode(), params);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<ReportDTO.ProductSalesPagingViewDTO> exportSoB2CProductSales(PagingDTO<ReportDTO.ProductSalesPagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        //sku 创建时间
        List<LocalDateTime> skuCreateTimeList = dto.getParams().getSkuCreateTimeList();
        List<String> skuIdList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(skuCreateTimeList)) {
            List<ProductDetailEntity> skuList = plmTaskFeign.listByCreateTimeList(skuCreateTimeList);
            skuIdList = skuList.stream().map(ProductDetailEntity::getId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(skuIdList)) {
                return new PagingVO<>();
            }
        }
        List<LocalDateTime> timeList = parseOrderCreationTime(dto.getParams().getAdvanceQueryDTOList());
        LocalDateTime startTime = timeList.get(0);
        LocalDateTime endTime = timeList.get(1);
        //获取到产品销售统计导出的数据
        Page<ReportDTO.ProductSalesPagingViewDTO> page = soB2cService.listProductSalesExport(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams(), skuIdList);
        Duration between = LocalDateTimeUtil.between(startTime, endTime);
        long diffDays = between.toDays();
        if (diffDays == 0) {
            diffDays = 1;
        }
        fillProductSalesList(page.getRecords(), diffDays);
        return new PagingVO<>(page);
    }



    /**
     * 解析时间
     */
    public List<LocalDateTime> parseOrderCreationTime(List<AdvanceQueryDTO> advanceQueryDTOList) {
        AdvanceQueryDTO advanceQueryDTO = advanceQueryDTOList.stream()
                .filter(e -> "sb.platform_order_create_time".equals(e.getField()))
                .findFirst()
                .orElse(null);
        if (null == advanceQueryDTO){
            advanceQueryDTO = advanceQueryDTOList.stream()
                    .filter(e -> "sb.create_time".equals(e.getField()))
                    .findFirst()
                    .orElse(null);
        }
        if (null == advanceQueryDTO){
            throw new ServiceException("订单创建时间必传");
        }
        if (null == advanceQueryDTO.getValue()){
            throw new ServiceException("订单创建时间不能为空");
        }

        // 销售订单开始时间
        LocalDateTime startTime = null;
        // 销售订单结束时间
        LocalDateTime endTime = null;
        if (QueryConditionEnum.GT.getCompareCode().equalsIgnoreCase(advanceQueryDTO.getCompare())
                || QueryConditionEnum.GE.getCompareCode().equalsIgnoreCase(advanceQueryDTO.getCompare())){
            startTime = LocalDateTime.of(LocalDate.parse(advanceQueryDTO.getValue().toString()), LocalTime.MIN);
            endTime = LocalDateTime.now(ZoneId.systemDefault());
        }

        if (QueryConditionEnum.LT.getCompareCode().equalsIgnoreCase(advanceQueryDTO.getCompare())
                || QueryConditionEnum.LE.getCompareCode().equalsIgnoreCase(advanceQueryDTO.getCompare())){
            startTime = LocalDateTime.of(1970, 1 , 1,  0, 0, 0);
            endTime = LocalDateTime.of(LocalDate.parse(advanceQueryDTO.getValue().toString()), LocalTime.MIN);;
        }
        if (QueryConditionEnum.BETWEEN.getCompareCode().equalsIgnoreCase(advanceQueryDTO.getCompare())) {
            JSONArray dateJsonArray = JSONArray.parseArray(JSON.toJSONString(advanceQueryDTO.getValue()));
            startTime = LocalDateTime.of(LocalDate.parse(dateJsonArray.get(0).toString()), LocalTime.MIN);
            endTime = LocalDateTime.of(LocalDate.parse(dateJsonArray.get(1).toString()), LocalTime.MIN);
        }
        if (null == startTime || null == endTime){
            ServiceException.runError("解析单创建时间失败");
        }

        return Arrays.asList(startTime, endTime);
    }

}
