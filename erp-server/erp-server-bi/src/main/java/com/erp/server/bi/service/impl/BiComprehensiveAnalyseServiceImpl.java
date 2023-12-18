package com.erp.server.bi.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.bi.dto.*;
import com.erp.model.bi.dto.excel.BiCountryRegionImportExcelDTO;
import com.erp.model.bi.entity.BiProductDetailEntity;
import com.erp.model.bi.entity.BiProductInfoEntity;
import com.erp.model.bi.vo.*;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.model.plm.dto.BasicCategoryDTO;
import com.erp.model.plm.dto.SkuDTO;
import com.erp.model.plm.vo.ProductRefLabelVO;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.mapper.BiComprehensiveAnalyseMapper;
import com.erp.server.bi.service.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BiComprehensiveAnalyseServiceImpl extends ServiceImpl<BiComprehensiveAnalyseMapper, DmpOrderInfoEntity> implements BiComprehensiveAnalyseService {
    @Resource
    private DmpShopInfoService dmpShopInfoService;

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;

    @Resource
    private DmpOrderItemService dmpOrderItemService;

    @Resource
    private BiProductDetailService biProductDetailService;

    @Resource
    private BiProductInfoService biProductInfoService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    /**
     * SKU矩阵
     *
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SkuMatrixVO>
     * @Author Luo_WG
     * @Date 2022/12/26 9:41
     **/
    @Override
    public List<List<Object>> skuMatrix(BiFilterDTO biFilterDTO) {
        List<SkuMatrixVO> skuMatrixVOIPage = baseMapper.skuMatrix(biFilterDTO);
        List<List<Object>> skuMatrixList = new ArrayList<>();
        skuMatrixVOIPage.stream().sorted(Comparator.comparing(SkuMatrixVO::getSales)).forEach(x -> {
            List<Object> tempList = new ArrayList<>();
            tempList.add(x.getSales());
            tempList.add(x.getAmount());
            tempList.add(x.getName());
            skuMatrixList.add(tempList);
        });
        return skuMatrixList;
    }

    /**
     * 店铺矩阵
     *
     * @param biFilterDTO biFilterDTO
     * @return com.erp.common.vo.PagingVO<com.erp.model.bi.vo.SkuMatrixVO>
     * @Author Luo_WG
     * @Date 2022/12/26 10:42
     **/
    @Override
    public List<List<Object>> shopMatrix(BiFilterDTO biFilterDTO) {
        List<MatrixVO> skuMatrixVOIPage = baseMapper.shopMatrix(biFilterDTO);
        List<List<Object>> skuMatrixList = new ArrayList<>();
        skuMatrixVOIPage.stream().sorted(Comparator.comparing(MatrixVO::getSales)).forEach(x -> {
            List<Object> tempList = new ArrayList<>();
            tempList.add(x.getSales());
            tempList.add(x.getNetProfit());
            tempList.add(x.getName());
            skuMatrixList.add(tempList);
        });
        return skuMatrixList;
    }

    /**
     * 平台店铺对比趋势
     *
     * @param biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.ShopContrastTrendVO>
     * @Author Luo_WG
     * @Date 2022/12/26 15:29
     **/
    @Override
    public List<List<Object>> shopContrastTrend(BiFilterDTO biFilterDTO) {
        List<ContrastTrendVO> contrastTrendVOList = baseMapper.shopContrastTrend(biFilterDTO);

        List<DmpShopInfoEntity> dmpShopInfoEntities = dmpShopInfoService.shopList();
        if (CollectionUtil.isEmpty(dmpShopInfoEntities)) {
            return Collections.emptyList();
        }
        List<List<Object>> result = new ArrayList<>();
        List<Object> shopName = new LinkedList<>();
        List<Object> lastYearSales = new LinkedList<>();
        List<Object> thisYearSales = new LinkedList<>();
        if (CollectionUtil.isEmpty(contrastTrendVOList)) {
            return Collections.emptyList();
        }
        Map<String, Map<String, BigDecimal>> shopNoMap = contrastTrendVOList.stream()
                .collect(Collectors.groupingBy(ContrastTrendVO::getShopNo, Collectors.toMap(ContrastTrendVO::getYear, ContrastTrendVO::getSales)));
        String lastYearKey = String.valueOf(LocalDate.now().minusYears(1L).getYear());
        String thisYearKey = String.valueOf(LocalDate.now().minusYears(1L).getYear());
        for (DmpShopInfoEntity dmpShopInfoEntity : dmpShopInfoEntities) {
            Map<String, BigDecimal> yearMap = shopNoMap.get(dmpShopInfoEntity.getPlatformShopNo());
            if (CollectionUtil.isEmpty(yearMap)) {
                continue;
            }
            lastYearSales.add(yearMap.getOrDefault(lastYearKey, BigDecimal.ZERO));
            thisYearSales.add(yearMap.getOrDefault(thisYearKey, BigDecimal.ZERO));
            shopName.add(dmpShopInfoEntity.getName());
        }
        result.add(shopName);
        result.add(lastYearSales);
        result.add(thisYearSales);
        return result;
    }

    /**
     * 品类矩阵
     *
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.ShopContrastTrendVO>
     * @Author Luo_WG
     * @Date 2022/12/26 10:35
     **/
    @Override
    public List<List<Object>> categoryMatrix(BiFilterDTO biFilterDTO) {
        List<SkuMatrixVO> skuMatrixVOIPage = baseMapper.categoryMatrix(biFilterDTO);
        List<List<Object>> skuMatrixList = new ArrayList<>();
        skuMatrixVOIPage.stream().sorted(Comparator.comparing(SkuMatrixVO::getSales)).forEach(x -> {
            List<Object> tempList = new ArrayList<>();
            tempList.add(x.getSales());
            tempList.add(x.getAmount());
            tempList.add(x.getName());
            skuMatrixList.add(tempList);
        });
        return skuMatrixList;
    }

    /**
     * 销售明细表-SKU
     *
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     **/
    @Override
    public List<SaleDetailVO> saleDetailSku(BiFilterDTO biFilterDTO) {
        //获取销售额
        TargetSaleSumVO targetSaleSumVO = dmpOrderInfoService.sumSales(biFilterDTO);

        //查询去年sku销售信息
        Date date = DateUtil.addDateYears(new Date(), -1);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Integer year = calendar.get(Calendar.YEAR);//获取年
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // 设置时间格式

        String startTime = sdf.format(DateUtil.getYearFirst(year));
        String endTime = sdf.format(DateUtil.getYearLast(year));
        List<SkuYearSaleAmountVO> skuYearSakeAmountVOS = baseMapper.skuYearSaleAmount(startTime, endTime);
        BigDecimal yearSakeAmount = baseMapper.yearSaleAmount(startTime, endTime);

        //查询前年sku销售信息
        date = DateUtil.addDateYears(new Date(), -2);
        calendar = Calendar.getInstance();
        calendar.setTime(date);
        year = calendar.get(Calendar.YEAR);//获取年
        startTime = sdf.format(DateUtil.getYearFirst(year));
        endTime = sdf.format(DateUtil.getYearLast(year));
        List<SkuYearSaleAmountVO> skuYearSakeAmountVOST = baseMapper.skuYearSaleAmount(startTime, endTime);
        BigDecimal yearSakeAmountT = baseMapper.yearSaleAmount(startTime, endTime);

        Date endDate = Date.from(biFilterDTO.getEndTime().atZone(ZoneId.systemDefault()).toInstant());
        Date startDate = Date.from(biFilterDTO.getStartTime().atZone(ZoneId.systemDefault()).toInstant());
        String start = DateUtil.getRingRatioDate(endDate, startDate);
        //设置时间格式
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<SkuYearSaleAmountVO> skuYearSakeAmountVOS1 = baseMapper.returnOrderAmountByDate(start, f.format(startDate));

        //组装近两年销售额信息
        List<SaleDetailVO> saleDetailList = baseMapper.saleDetailSku(biFilterDTO);
        for (SaleDetailVO saleDetailVO : saleDetailList) {
            if (saleDetailVO.getSaleAmount().compareTo(BigDecimal.ZERO) <= 0) {
                saleDetailVO.setSaleProportion(BigDecimal.ZERO);
            } else {
                if (targetSaleSumVO.getValue() != null) {
                    saleDetailVO.setSaleProportion(saleDetailVO.getSaleAmount().divide(targetSaleSumVO.getValue(), 4, BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)));
                } else {
                    saleDetailVO.setSaleProportion(BigDecimal.ZERO);
                }
            }
            //计算去年sku销售额
            SkuYearSaleAmountVO skuYearSakeAmountVO = skuYearSakeAmountVOS.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVO != null) {
                saleDetailVO.setLastYearSaleAmount(skuYearSakeAmountVO.getAmount());
                if (yearSakeAmount != null) {
                    saleDetailVO.setLastYearSaleProportion(skuYearSakeAmountVO.getAmount().divide(yearSakeAmount, 4, BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)));
                } else {
                    saleDetailVO.setLastYearSaleProportion(BigDecimal.ZERO);
                }

            } else {
                saleDetailVO.setLastYearSaleProportion(BigDecimal.ZERO);
                saleDetailVO.setLastYearSaleAmount(BigDecimal.ZERO);
            }
            //计算前年sku销售额
            SkuYearSaleAmountVO skuYearSakeAmountVOT = skuYearSakeAmountVOST.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVOT != null) {
                saleDetailVO.setYearBeforeLastSaleAmount(skuYearSakeAmountVOT.getAmount());
                if (yearSakeAmountT != null) {
                    saleDetailVO.setYearBeforeLastSaleProportion(skuYearSakeAmountVOT.getAmount().divide(yearSakeAmountT, 4, BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)));
                } else {
                    saleDetailVO.setYearBeforeLastSaleProportion(BigDecimal.ZERO);
                }
            } else {
                saleDetailVO.setYearBeforeLastSaleAmount(BigDecimal.ZERO);
                saleDetailVO.setYearBeforeLastSaleProportion(BigDecimal.ZERO);
            }

            //退货环比
            SkuYearSaleAmountVO skuYearSakeAmountVO1 = skuYearSakeAmountVOS1.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVO1 != null) {
                if (skuYearSakeAmountVO1.getAmount() != null) {
                    saleDetailVO.setReturnOrderRingRatio(saleDetailVO.getReturnOrderAmount().subtract(skuYearSakeAmountVO1.getAmount()).divide(skuYearSakeAmountVO1.getAmount(), 4, BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)));
                } else {
                    saleDetailVO.setReturnOrderRingRatio(BigDecimal.ZERO);
                }
            } else {
                saleDetailVO.setReturnOrderRingRatio(BigDecimal.ZERO);
            }
        }
        return saleDetailList;
    }

    @Override
    public StatisticalDataVO salePriceDistribution(BiFilterDTO biFilterDTO) {
        Optional.ofNullable(biFilterDTO.getRangeType()).orElseThrow(() -> new ServiceException(ApiError.ERROR_SALE_RANGE_EXIST));
        Optional.ofNullable(biFilterDTO.getSettleMethod()).orElseThrow(() -> new ServiceException(ApiError.ERROR_SETTLE_METHOD_EXIST));
        return dmpOrderInfoService.salePriceDistribution(biFilterDTO);
    }

    /**
     * 销售明细表-店铺
     *
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     **/
    @Override
    public List<SaleDetailVO> saleDetailShop(BiFilterDTO biFilterDTO) {
        //获取销售额
        TargetSaleSumVO targetSaleSumVO = dmpOrderInfoService.sumSales(biFilterDTO);

        //查询去年sku销售信息
        Date date = DateUtil.addDateYears(new Date(), -1);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Integer year = calendar.get(Calendar.YEAR);//获取年
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // 设置时间格式
        String startTime = sdf.format(DateUtil.getYearFirst(year));
        String endTime = sdf.format(DateUtil.getYearLast(year));
        List<SkuYearSaleAmountVO> skuYearSakeAmountVOS = baseMapper.shopYearSaleAmount(startTime, endTime);
        BigDecimal yearSakeAmount = baseMapper.yearSaleAmount(startTime, endTime);

        //查询前年sku销售信息
        date = DateUtil.addDateYears(new Date(), -2);
        calendar = Calendar.getInstance();
        calendar.setTime(date);
        year = calendar.get(Calendar.YEAR);//获取年
        startTime = sdf.format(DateUtil.getYearFirst(year));
        endTime = sdf.format(DateUtil.getYearLast(year));
        List<SkuYearSaleAmountVO> skuYearSakeAmountVOST = baseMapper.shopYearSaleAmount(startTime, endTime);
        BigDecimal yearSakeAmountT = baseMapper.yearSaleAmount(startTime, endTime);

        Date endDate = Date.from(biFilterDTO.getEndTime().atZone(ZoneId.systemDefault()).toInstant());
        Date startDate = Date.from(biFilterDTO.getStartTime().atZone(ZoneId.systemDefault()).toInstant());
        String start = DateUtil.getRingRatioDate(endDate, startDate);
        //设置时间格式
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<SkuYearSaleAmountVO> skuYearSakeAmountVOS1 = baseMapper.returnOrderAmountByDate(start, f.format(startDate));

        //组装近两年销售额信息
        List<SaleDetailVO> saleDetailList = baseMapper.saleDetailShop(biFilterDTO);
        for (SaleDetailVO saleDetailVO : saleDetailList) {
            if (saleDetailVO.getSaleAmount().compareTo(BigDecimal.ZERO) <= 0) {
                saleDetailVO.setSaleProportion(BigDecimal.ZERO);
            } else {
                saleDetailVO.setSaleProportion(saleDetailVO.getSaleAmount().divide(targetSaleSumVO.getValue(), 4, BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)));
            }
            //计算去年sku销售额
            SkuYearSaleAmountVO skuYearSakeAmountVO = skuYearSakeAmountVOS.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVO != null) {
                saleDetailVO.setLastYearSaleAmount(skuYearSakeAmountVO.getAmount());
                if (yearSakeAmount != null) {
                    saleDetailVO.setLastYearSaleProportion(skuYearSakeAmountVO.getAmount().divide(yearSakeAmount, 4, BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)));
                } else {
                    saleDetailVO.setLastYearSaleProportion(BigDecimal.ZERO);
                }
            }
            //计算前年sku销售额
            SkuYearSaleAmountVO skuYearSakeAmountVOT = skuYearSakeAmountVOST.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVOT != null) {
                saleDetailVO.setYearBeforeLastSaleAmount(skuYearSakeAmountVOT.getAmount());
                if (yearSakeAmountT != null) {
                    saleDetailVO.setYearBeforeLastSaleProportion(skuYearSakeAmountVOT.getAmount().divide(yearSakeAmountT, 4, BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)));
                } else {
                    saleDetailVO.setYearBeforeLastSaleProportion(BigDecimal.ZERO);
                }
            }

            //退货环比
            SkuYearSaleAmountVO skuYearSakeAmountVO1 = skuYearSakeAmountVOS1.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVO1 != null) {
                if (skuYearSakeAmountVO1.getAmount() != null) {
                    saleDetailVO.setReturnOrderRingRatio(saleDetailVO.getReturnOrderAmount().subtract(skuYearSakeAmountVO1.getAmount()).divide(skuYearSakeAmountVO1.getAmount(), 4, BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)));
                } else {
                    saleDetailVO.setReturnOrderRingRatio(BigDecimal.ZERO);
                }
            }

        }
        return saleDetailList;
    }

    /**
     * 销售明细表-用户
     *
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     **/
    @Override
    public List<SaleDetailVO> saleDetailUser(BiFilterDTO biFilterDTO) {
        //获取销售额
        TargetSaleSumVO targetSaleSumVO = dmpOrderInfoService.sumSales(biFilterDTO);

        //查询去年sku销售信息
        Date date = DateUtil.addDateYears(new Date(), -1);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Integer year = calendar.get(Calendar.YEAR);//获取年
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // 设置时间格式
        String startTime = sdf.format(DateUtil.getYearFirst(year));
        String endTime = sdf.format(DateUtil.getYearLast(year));
        List<SkuYearSaleAmountVO> skuYearSakeAmountVOS = baseMapper.userYearSaleAmount(startTime, endTime);
        BigDecimal yearSakeAmount = baseMapper.yearSaleAmount(startTime, endTime);

        //查询前年sku销售信息
        date = DateUtil.addDateYears(new Date(), -2);
        calendar = Calendar.getInstance();
        calendar.setTime(date);
        year = calendar.get(Calendar.YEAR);//获取年
        startTime = sdf.format(DateUtil.getYearFirst(year));
        endTime = sdf.format(DateUtil.getYearLast(year));
        List<SkuYearSaleAmountVO> skuYearSakeAmountVOST = baseMapper.userYearSaleAmount(startTime, endTime);
        BigDecimal yearSakeAmountT = baseMapper.yearSaleAmount(startTime, endTime);

        Date endDate = Date.from(biFilterDTO.getEndTime().atZone(ZoneId.systemDefault()).toInstant());
        Date startDate = Date.from(biFilterDTO.getStartTime().atZone(ZoneId.systemDefault()).toInstant());
        String start = DateUtil.getRingRatioDate(endDate, startDate);
        //设置时间格式
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<SkuYearSaleAmountVO> skuYearSakeAmountVOS1 = baseMapper.returnOrderAmountByDate(start, f.format(startDate));

        //组装近两年销售额信息
        List<SaleDetailVO> saleDetailList = baseMapper.saleDetailUser(biFilterDTO);
        for (SaleDetailVO saleDetailVO : saleDetailList) {
            if (saleDetailVO.getSaleAmount().compareTo(BigDecimal.ZERO) <= 0) {
                saleDetailVO.setSaleProportion(BigDecimal.ZERO);
            } else {
                saleDetailVO.setSaleProportion(saleDetailVO.getSaleAmount().divide(targetSaleSumVO.getValue(), 4, BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)));
            }
            //计算去年sku销售额
            SkuYearSaleAmountVO skuYearSakeAmountVO = skuYearSakeAmountVOS.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVO != null) {
                saleDetailVO.setLastYearSaleAmount(skuYearSakeAmountVO.getAmount());
                if (yearSakeAmount != null) {
                    saleDetailVO.setLastYearSaleProportion(skuYearSakeAmountVO.getAmount().divide(yearSakeAmount, 4, BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)));
                } else {
                    saleDetailVO.setLastYearSaleProportion(BigDecimal.ZERO);
                }
            } else {
                saleDetailVO.setLastYearSaleAmount(BigDecimal.ZERO);
                saleDetailVO.setLastYearSaleProportion(BigDecimal.ZERO);
            }
            //计算前年sku销售额
            SkuYearSaleAmountVO skuYearSakeAmountVOT = skuYearSakeAmountVOST.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVOT != null) {
                saleDetailVO.setYearBeforeLastSaleAmount(skuYearSakeAmountVOT.getAmount());
                if (yearSakeAmountT != null) {
                    saleDetailVO.setYearBeforeLastSaleProportion(skuYearSakeAmountVOT.getAmount().divide(yearSakeAmountT, 4, BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)));
                } else {
                    saleDetailVO.setYearBeforeLastSaleProportion(BigDecimal.ZERO);
                }
            } else {
                saleDetailVO.setYearBeforeLastSaleAmount(BigDecimal.ZERO);
                saleDetailVO.setYearBeforeLastSaleProportion(BigDecimal.ZERO);
            }

            //退货环比
            SkuYearSaleAmountVO skuYearSakeAmountVO1 = skuYearSakeAmountVOS1.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVO1 != null) {
                if (skuYearSakeAmountVO1.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    saleDetailVO.setReturnOrderRingRatio(BigDecimal.ZERO);
                } else {
                    saleDetailVO.setReturnOrderRingRatio(saleDetailVO.getReturnOrderAmount().subtract(skuYearSakeAmountVO1.getAmount()).divide(skuYearSakeAmountVO1.getAmount(), 4, BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)));
                }
            } else {
                saleDetailVO.setReturnOrderRingRatio(BigDecimal.ZERO);
            }
        }
        return saleDetailList;
    }

    /**
     * 销售明细表-日期
     *
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     **/
    @Override
    public List<SaleDetailVO> saleDetailDate(SkuDateFilterDTO biFilterDTO) {
        //获取销售额
        biFilterDTO.setSku(Arrays.asList(biFilterDTO.getSkuNo()));
        TargetSaleSumVO targetSaleSumVO = dmpOrderInfoService.sumSales(biFilterDTO);

        //查询去年sku销售信息
        LocalDateTime startTime = LocalDateTime.of(biFilterDTO.getStartTime().minusYears(1).toLocalDate(), LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(biFilterDTO.getEndTime().minusYears(1).toLocalDate(), LocalTime.MAX);
//        biFilterDTO.setDateType(StrUtil.isNotBlank(biFilterDTO.getDateType()) ? biFilterDTO.getDateType() : "DAY");
        // 固定是天
        biFilterDTO.setDateType("DAY");
        // 组装去年filter
        SkuDateFilterDTO lastYearBiFilterDTO = new SkuDateFilterDTO();
        BeanUtils.copyProperties(biFilterDTO, lastYearBiFilterDTO);
        lastYearBiFilterDTO.setStartTime(startTime);
        lastYearBiFilterDTO.setEndTime(endTime);
        List<SkuYearSaleAmountVO> skuYearSakeAmountVOS = baseMapper.dateYearSaleAmountBySku(lastYearBiFilterDTO);
        Map<String, BigDecimal> lastYearSaleMap = skuYearSakeAmountVOS.stream().collect(Collectors.toMap(SkuYearSaleAmountVO::getName, SkuYearSaleAmountVO::getAmount));
        // 去年销售总金额
        lastYearBiFilterDTO.setStartTime(LocalDateTime.of(startTime.getYear(), 1,1,0,0,0));
        lastYearBiFilterDTO.setEndTime(LocalDateTime.of(startTime.getYear() + 1, 1,1,0,0,0));

        BigDecimal yearSakeAmount = baseMapper.yearSaleAmountBySku(lastYearBiFilterDTO);

        //查询前年sku销售信息
        // 组装前年filter
        SkuDateFilterDTO twoYearAgeBiFilterDTO = new SkuDateFilterDTO();
        BeanUtils.copyProperties(biFilterDTO, twoYearAgeBiFilterDTO);
        twoYearAgeBiFilterDTO.setStartTime(startTime.minusYears(1));
        twoYearAgeBiFilterDTO.setEndTime(endTime.minusYears(1));
        List<SkuYearSaleAmountVO> skuYearSakeAmountVOST = baseMapper.dateYearSaleAmountBySku(twoYearAgeBiFilterDTO);
        Map<String, BigDecimal> twoYearAgeSaleMap = skuYearSakeAmountVOST.stream().collect(Collectors.toMap(SkuYearSaleAmountVO::getName, SkuYearSaleAmountVO::getAmount));
        // 前年销售总金额
        twoYearAgeBiFilterDTO.setStartTime(LocalDateTime.of(startTime.minusYears(1).getYear(), 1,1,0,0,0));
        lastYearBiFilterDTO.setEndTime(LocalDateTime.of(startTime.getYear(), 1,1,0,0,0));
        BigDecimal yearSakeAmountT = baseMapper.yearSaleAmountBySku(twoYearAgeBiFilterDTO);

//        Date endDate = Date.from(biFilterDTO.getEndTime().atZone(ZoneId.systemDefault()).toInstant());
//        Date startDate = Date.from(biFilterDTO.getStartTime().atZone(ZoneId.systemDefault()).toInstant());
//        String start = DateUtil.getRingRatioDate(endDate, startDate);
//        //设置时间格式
//        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        List<SkuYearSaleAmountVO> skuYearSakeAmountVOS1 = baseMapper.dateReturnOrderAmountByDate(start, f.format(startDate), biFilterDTO.getSkuNo(), biFilterDTO.getDateType(), biFilterDTO.getSettleMethod());
        // 退货信息
        List<SkuYearSaleAmountVO> returnOrderList = baseMapper.dateReturnOrder(biFilterDTO);
        Map<String, BigDecimal> returnOrderMap = returnOrderList.stream().collect(Collectors.toMap(SkuYearSaleAmountVO::getName, SkuYearSaleAmountVO::getAmount));
        // 退款信息
        List<SaleDetailVO> refundList = baseMapper.dateRefund(biFilterDTO);
        Map<String, SaleDetailVO> refundMap = refundList.stream().collect(Collectors.toMap(SaleDetailVO::getName, Function.identity()));

        //组装近两年销售额信息
        List<SaleDetailVO> saleDetailList = baseMapper.saleDetailDate(biFilterDTO);
        for (SaleDetailVO saleDetailVO : saleDetailList) {
            // 退货信息
            saleDetailVO.setReturnOrderAmount(returnOrderMap.getOrDefault(saleDetailVO.getName(), BigDecimal.ZERO));
            // 退款信息
            SaleDetailVO refundInfo = refundMap.get(saleDetailVO.getName());
            if (null != refundInfo){
                saleDetailVO.setRefundAmount(refundInfo.getRefundAmount());
                saleDetailVO.setRefundOrderQty(refundInfo.getRefundOrderQty());
            }

            if (saleDetailVO.getSaleAmount().compareTo(BigDecimal.ZERO) <= 0) {
                saleDetailVO.setSaleProportion(BigDecimal.ZERO);
            } else {
                saleDetailVO.setSaleProportion(saleDetailVO.getSaleAmount().divide(targetSaleSumVO.getValue(), 4, BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)));
            }
            //计算去年sku销售额
            String lastYearKey = LocalDate.parse(saleDetailVO.getName()).minusYears(1).toString();
            BigDecimal currentLastYearSaleAmount = lastYearSaleMap.getOrDefault(lastYearKey, BigDecimal.ZERO);
            saleDetailVO.setLastYearSaleAmount(currentLastYearSaleAmount);
            // 占比
            saleDetailVO.setLastYearSaleProportion(
                    yearSakeAmount.compareTo(BigDecimal.ZERO) <= 0 ? BigDecimal.ZERO :
                    currentLastYearSaleAmount.divide(yearSakeAmount, 4, RoundingMode.DOWN).multiply(BigDecimal.valueOf(100)));

            //计算前年sku销售额
            String TwoYearAgeKey = LocalDate.parse(saleDetailVO.getName()).minusYears(2).toString();
            BigDecimal currentTwoYearAgeAmount = twoYearAgeSaleMap.getOrDefault(TwoYearAgeKey, BigDecimal.ZERO);
            saleDetailVO.setYearBeforeLastSaleAmount(currentTwoYearAgeAmount);
            // 占比
            saleDetailVO.setYearBeforeLastSaleProportion(
                    yearSakeAmountT.compareTo(BigDecimal.ZERO) <= 0 ? BigDecimal.ZERO :
                    currentTwoYearAgeAmount.divide(yearSakeAmountT, 4, RoundingMode.DOWN).multiply(BigDecimal.valueOf(100)));


            //退货环比
//            SkuYearSaleAmountVO skuYearSakeAmountVO1 = skuYearSakeAmountVOS1.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
//            if (null != skuYearSakeAmountVO1 && null != skuYearSakeAmountVO1.getAmount()) {
//                if (skuYearSakeAmountVO1.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
//                    saleDetailVO.setReturnOrderRingRatio(BigDecimal.ZERO);
//                } else {
//                    saleDetailVO.setReturnOrderRingRatio(saleDetailVO.getReturnOrderAmount().subtract(skuYearSakeAmountVO1.getAmount()).divide(skuYearSakeAmountVO1.getAmount(), 4, BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)));
//                }
//            } else {
//                saleDetailVO.setReturnOrderRingRatio(BigDecimal.ZERO);
//            }
        }
        return saleDetailList;
    }

    /**
     * SKU日期销售额趋势图
     *
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     **/
    @Override
    public List<SkuDateSaleTrendVO> skuDateSaleTrend(SkuDateFilterDTO biFilterDTO) {
        List<SkuDateSaleTrendVO> skuDateSaleTrendVOS = null;
        switch (biFilterDTO.getDateType()) {
            case "DAY":
                skuDateSaleTrendVOS = baseMapper.skuDaySaleTrend(biFilterDTO);
                break;
            case "WEEK":
                skuDateSaleTrendVOS = baseMapper.skuWeekSaleTrend(biFilterDTO);
                break;
            case "MONTH":
                skuDateSaleTrendVOS = baseMapper.skuMonthSaleTrend(biFilterDTO);
                break;
            case "QUARTER":
                skuDateSaleTrendVOS = baseMapper.skuQuarterSaleTrend(biFilterDTO);
                break;
            case "YEAR":
                skuDateSaleTrendVOS = baseMapper.skuYearSaleTrend(biFilterDTO);
                break;
            default:
                skuDateSaleTrendVOS = baseMapper.skuDaySaleTrend(biFilterDTO);
                break;
        }
        return skuDateSaleTrendVOS;
    }

    @Override
    public BiSkuDetailTopDTO skuDetailTop(SkuDetailDTO dto) {
        // 商品详情
        BiProductDetailEntity detailEntity = biProductDetailService.getBySkuNo(dto.getSkuNo());
        if(null == detailEntity){
            throw new ServiceException(ApiError.ERROR_92051);
        }
        // 商品信息
        BiProductInfoEntity productEntity = biProductInfoService.getById(detailEntity.getProductId());
        // 商品销售信息
        List<SkuDTO.SalesDTO> salesDTOS = plmTaskFeign.listSkuSalesBySkuNos(Collections.singletonList(dto.getSkuNo()));
        SkuDTO.SalesDTO salesDTO = salesDTOS.stream().findFirst().orElse(null);

        //根据名称查询品类
        Map<String,String> categoryParams = new HashMap<>();
        categoryParams.put("id", productEntity.getCategoryId());
        BasicCategoryDTO category = plmTaskFeign.getParent(categoryParams);

        // 各平台首单时间
        // Map<平台, 订单>
        Map<String, DmpOrderInfoEntity> orderMap = dmpOrderInfoService.mapFirstOrderBySkuNo(dto.getSkuNo());

        // 销售状态
        Integer scalesStatus = null;
        if (null != salesDTO){
            scalesStatus = salesDTO.getSaleState();
        }

        //组合
        BiSkuDetailTopDTO resultDto = new BiSkuDetailTopDTO(detailEntity,
                productEntity,
                scalesStatus
        );
        // 设置父类名称
        if (null != category){
            resultDto.setParentCategoryName(category.getName());
        }

        // 最新订单
        DmpOrderItemEntity orderItemEntity = dmpOrderItemService.lambdaQuery()
                .eq(DmpOrderItemEntity::getSkuNo, dto.getSkuNo())
                .orderByDesc(DmpOrderItemEntity::getId)
                .last("LIMIT 1")
                .one();
        if(null != orderItemEntity){
            // 设置最新名称
           resultDto.setNameCn(orderItemEntity.getItemName());
        }

        // 平台首次下单时间
        if (null != salesDTO && null != salesDTO.getFirstOrderDate()){
            LocalDate platformCreateDate = salesDTO.getFirstOrderDate();
            resultDto.setFirstOrderDate(platformCreateDate.toString());
            // 超过一年认为非新品
            if (LocalDate.now(ZoneId.systemDefault()).getYear() >= (platformCreateDate.getYear() + 1)){
                resultDto.setHasNewSign(false);
            }
        }

        if (!orderMap.isEmpty()) {
            // 各平台首单时间
            List<String> platformFistOrderList = orderMap.entrySet()
                    .stream()
                    .filter(e-> StringUtils.isNotBlank(e.getKey()))
                    .map(e -> e.getKey().concat(":").concat(e.getValue().getPlatformCreateTime().toLocalDate().toString()))
                    .collect(Collectors.toList());
            resultDto.setPlatformFirstOrderDate(platformFistOrderList);
        }
        //增加标签列表
        List<ProductRefLabelVO> productRefLabelVOS = plmTaskFeign.getProductRelLabelBySkuId(detailEntity.getId());
        if (CollectionUtils.isNotEmpty(productRefLabelVOS)) {
            resultDto.setLabels(BeanMapperUtils.copyList(LabelVO.class, productRefLabelVOS));
        }
        return resultDto;
    }

    /**
     * 区域销售分析
     */
    @Override
    public List<BiRegionAnalyzeDTO> getSubRegionSales(BiCountryRegionFilterDTO dto) {
        // 国家列表
        List<DictCountryDTO.ListDTO> countryList = sysUserFeign.countryList()
                .stream()
                // 过滤其他
                .filter(e-> StringUtils.isNotBlank(e.getRegionCode()))
                .collect(Collectors.toList());;
        // 国家销售额
        List<BiCountryAnalyzeDTO> countrySalesList = baseMapper.getCountrySales(dto);

        // 区域Map<子区域Code, 国家List>
        Map<String, List<DictCountryDTO.ListDTO>> regionMap = countryList
                .stream()
                .filter(e-> StringUtils.isNotBlank(e.getSubregionCode()))
                .collect(Collectors.groupingBy(DictCountryDTO.ListDTO::getSubregionCode));

        // 区域所有国家名称Map<区域Code, 国家名称List>
        Map<String, List<String>> regionCountryNameMap = regionMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(DictCountryDTO.ListDTO::getNameCn)
                                .collect(Collectors.toList())));

        // 子区域名称map
        Map<String, DictCountryDTO.ListDTO> subregionNameMap = countryList.stream()
                .collect(Collectors.toMap(
                        // 指定去重的字段
                        DictCountryDTO.ListDTO::getSubregionCode,
                        // 保留第一个出现的对象
                        listDto -> listDto,
                        // 解决冲突时保留
                        (existing, replacement) -> existing));

        // 国家销量Map<国家名称, 国家销量>
        Map<String, BigDecimal> countrySalesMap = countrySalesList
                .stream()
                .filter(e-> StringUtils.isNotBlank(e.getCountryNameCn()))
                .collect(Collectors.toMap(BiCountryAnalyzeDTO::getCountryNameCn, BiCountryAnalyzeDTO::getSalesAmount));

        // 全球总销量
        BigDecimal globalTotal = BigDecimal.ZERO;
        // 结果列表
        List<BiRegionAnalyzeDTO> resultList = new LinkedList<>();

        // 组合信息
        for (Map.Entry<String, List<String>> entry : regionCountryNameMap.entrySet()) {
            // 初始化
            DictCountryDTO.ListDTO subregionDto = subregionNameMap.get(entry.getKey());
            BiRegionAnalyzeDTO resultDto = BiRegionAnalyzeDTO.init(subregionDto.getAreaName(),
                    subregionDto.getRegionCode(),
                    subregionDto.getSubregionName(),
                    subregionDto.getSubregionCode()
            );
            // 当前区域所有国家名称
            List<String> currentCountryNameList = entry.getValue();

            // 当前区域的销量
            BigDecimal currentRegionSales = currentCountryNameList.stream()
                    // 过滤出存在于map中的键
                    .filter(countrySalesMap::containsKey)
                    // 获取对应键的值
                    .map(countrySalesMap::get)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 设置到结果
            resultDto.setSalesAmount(currentRegionSales);
            // 累加到总数量
            globalTotal = globalTotal.add(currentRegionSales);
            // 添加到结果
            resultList.add(resultDto);
        }
        // 设置销售比例
        BigDecimal finalGlobalTotal = globalTotal;
        resultList.forEach(e -> e.calculateSalesRatio(finalGlobalTotal));

        return resultList;
    }

    /**
     * 国家销售分析
     */
    @Override
    public List<BiCountryAnalyzeDTO> getCountrySales(BiCountryRegionFilterDTO dto) {
        // 国家列表
        List<DictCountryDTO.ListDTO> countryList = sysUserFeign.countryList()
                .stream()
                // 过滤其他
                .filter(e-> StringUtils.isNotBlank(e.getRegionCode()))
                .collect(Collectors.toList());
                ;
        // 国家销售额
        List<BiCountryAnalyzeDTO> countrySalesList = baseMapper.getCountrySales(dto);

        // 国家销量Map<国家名称, 国家销量>
        Map<String, BigDecimal> countrySalesMap = countrySalesList
                .stream()
                .collect(Collectors.toMap(BiCountryAnalyzeDTO::getCountryNameCn, BiCountryAnalyzeDTO::getSalesAmount));

        // 全球总销量
        BigDecimal globalTotal = BigDecimal.ZERO;
        // 区域总销量Map<区域code, 当前区域总销量>
        Map<String, BigDecimal> regionTotalMap = new HashMap<>();
        // 子区域总销量Map<区域code, 当前区域总销量>
        Map<String, BigDecimal> subregionTotalMap = new HashMap<>();
        // 相应结果
        List<BiCountryAnalyzeDTO> resultList = new LinkedList<>();

        // 组合
        for (DictCountryDTO.ListDTO listDTO : countryList) {
            // 初始化
            BiCountryAnalyzeDTO resultDto = BiCountryAnalyzeDTO.init(
                    listDTO.getNameCn(),
                    listDTO.getNameEn(),
                    listDTO.getId(),
                    listDTO.getAreaName(),
                    listDTO.getRegionCode(),
                    listDTO.getSubregionName(),
                    listDTO.getSubregionCode(),
                    countrySalesMap.getOrDefault(listDTO.getNameCn(), BigDecimal.ZERO)
            );

            // 添加到结果
            resultList.add(resultDto);
            if (0 == resultDto.getSalesAmount().compareTo(BigDecimal.ZERO)){
                continue;
            }
            // 添加到全球总销量
            globalTotal = globalTotal.add(resultDto.getSalesAmount());
            // 添加到当前区域总销量
            regionTotalMap.merge(resultDto.getRegionCode(), resultDto.getSalesAmount(), BigDecimal::add);
            // 添加到当前子区域总销量
            subregionTotalMap.merge(resultDto.getSubregionCode(), resultDto.getSalesAmount(), BigDecimal::add);
        }
        // 设置所有占比
        BigDecimal finalGlobalTotal = globalTotal;
        resultList.forEach(e-> e.setAllRadio(finalGlobalTotal,
                regionTotalMap.getOrDefault(e.getRegionCode(), BigDecimal.ZERO),
                subregionTotalMap.getOrDefault(e.getSubregionCode(), BigDecimal.ZERO)
        ));

        return resultList.stream()
                // 过滤得到要求的区域
                .filter(dto::filterRegion)
                .collect(Collectors.toList());
    }

    /**
     * 导出区域/国家销售额
     */
    @Override
    public Boolean exportCountryExcel(BiCountryRegionFilterDTO dto, HttpServletResponse response) {
        List<BiCountryAnalyzeDTO> analyzeList = this.getCountrySales(dto);
        List<BiCountryRegionImportExcelDTO> resultList = analyzeList.stream().map(e -> {
            BiCountryRegionImportExcelDTO excelDto = new BiCountryRegionImportExcelDTO();
            BeanUtils.copyProperties(e, excelDto);
            return excelDto;
        }).collect(Collectors.toList());
        String fileName = "区域国家销售额数据" + dto.convertFileParams();
        try {
            ExcelUtil.export(fileName, "区域-国家销售额数据", resultList, BiCountryRegionImportExcelDTO.class, response);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
        return Boolean.TRUE;
    }


}
