package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.date.DateUtil;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.dto.SkuDateFilterDTO;
import com.erp.model.bi.dto.SkuFilterDTO;
import com.erp.model.bi.vo.*;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.server.bi.mapper.BiComprehensiveAnalyseMapper;
import com.erp.server.bi.service.BiComprehensiveAnalyseService;
import com.erp.server.bi.service.DmpOrderInfoService;
import com.erp.server.bi.service.DmpShopInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class BiComprehensiveAnalyseServiceImpl extends ServiceImpl<BiComprehensiveAnalyseMapper, DmpOrderInfoEntity> implements BiComprehensiveAnalyseService {
    @Resource
    private DmpShopInfoService dmpShopInfoService;

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;

    /**
     * SKU矩阵
     * @Author Luo_WG
     * @Date 2022/12/26 9:41
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SkuMatrixVO>
     **/
    @Override
    public List<SkuMatrixVO> skuMatrix(BiFilterDTO biFilterDTO) {
        List<SkuMatrixVO> skuMatrixVOIPage = baseMapper.skuMatrix(biFilterDTO);
        return skuMatrixVOIPage;
    }

    /**
     * 店铺矩阵
     * @Author Luo_WG
     * @Date 2022/12/26 10:42
     * @param biFilterDTO biFilterDTO
     * @return com.erp.common.vo.PagingVO<com.erp.model.bi.vo.SkuMatrixVO>
     **/
    @Override
    public List<MatrixVO> shopMatrix(BiFilterDTO biFilterDTO) {
        List<MatrixVO> skuMatrixVOIPage = baseMapper.shopMatrix(biFilterDTO);
        return skuMatrixVOIPage;
    }

    /**
     * 平台店铺对比趋势
     * @Author Luo_WG
     * @Date 2022/12/26 15:29
     * @param biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.ShopContrastTrendVO>
     **/
    @Override
    public List<ShopContrastTrendVO> shopContrastTrend(BiFilterDTO biFilterDTO) {
        List<ShopContrastTrendVO> list = new ArrayList<>();
        List<ContrastTrendVO> contrastTrendVOList = baseMapper.shopContrastTrend(biFilterDTO);

        List<DmpShopInfoEntity> dmpShopInfoEntities = dmpShopInfoService.shopList();
        for (DmpShopInfoEntity dmpShopInfoEntity : dmpShopInfoEntities) {
            ShopContrastTrendVO shopContrastTrendVO = new ShopContrastTrendVO();
            shopContrastTrendVO.setShopName(dmpShopInfoEntity.getName());

            for (ContrastTrendVO contrastTrendVO : contrastTrendVOList) {
                if (dmpShopInfoEntity.getPlarformShopNo().equals(contrastTrendVO.getShopNo())) {
                    shopContrastTrendVO.setContrastTrendVO(contrastTrendVO);
                }
            }
        }
        return list;
    }

    /**
     * 品类矩阵
     * @Author Luo_WG
     * @Date 2022/12/26 10:35
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.ShopContrastTrendVO>
     **/
    @Override
    public List<SkuMatrixVO> categoryMatrix(BiFilterDTO biFilterDTO) {
        List<SkuMatrixVO> skuMatrixVOIPage = baseMapper.categoryMatrix(biFilterDTO);
        return skuMatrixVOIPage;
    }

    /**
     * 销售明细表-SKU
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
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
                    saleDetailVO.setSaleProportion(saleDetailVO.getSaleAmount().divide(targetSaleSumVO.getValue(),4,BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)));
                } else {
                    saleDetailVO.setSaleProportion(BigDecimal.ZERO);
                }
            }
            //计算去年sku销售额
            SkuYearSaleAmountVO skuYearSakeAmountVO = skuYearSakeAmountVOS.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVO != null) {
                saleDetailVO.setLastYearSaleAmount(skuYearSakeAmountVO.getAmount());
                if (yearSakeAmount != null) {
                    saleDetailVO.setLastYearSaleProportion(skuYearSakeAmountVO.getAmount().divide(yearSakeAmount,4,BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)));
                } else {
                    saleDetailVO.setLastYearSaleProportion(BigDecimal.ZERO);
                }

            }
            //计算前年sku销售额
            SkuYearSaleAmountVO skuYearSakeAmountVOT = skuYearSakeAmountVOST.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVOT != null) {
                saleDetailVO.setYearBeforeLastSaleAmount(skuYearSakeAmountVOT.getAmount());
                if (yearSakeAmountT != null) {
                    saleDetailVO.setYearBeforeLastSaleProportion(skuYearSakeAmountVOT.getAmount().divide(yearSakeAmountT,4,BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)));
                } else {
                    saleDetailVO.setYearBeforeLastSaleProportion(BigDecimal.ZERO);
                }
            }

            //退货环比
            SkuYearSaleAmountVO skuYearSakeAmountVO1 = skuYearSakeAmountVOS1.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVOT != null) {
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
     * 销售明细表-店铺
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
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
                saleDetailVO.setSaleProportion(saleDetailVO.getSaleAmount().divide(targetSaleSumVO.getValue(),4,BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)));
            }
            //计算去年sku销售额
            SkuYearSaleAmountVO skuYearSakeAmountVO = skuYearSakeAmountVOS.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVO != null) {
                saleDetailVO.setLastYearSaleAmount(skuYearSakeAmountVO.getAmount());
                if (yearSakeAmount != null) {
                    saleDetailVO.setLastYearSaleProportion(skuYearSakeAmountVO.getAmount().divide(yearSakeAmount,4,BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)));
                } else {
                    saleDetailVO.setLastYearSaleProportion(BigDecimal.ZERO);
                }
            }
            //计算前年sku销售额
            SkuYearSaleAmountVO skuYearSakeAmountVOT = skuYearSakeAmountVOST.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVOT != null) {
                saleDetailVO.setYearBeforeLastSaleAmount(skuYearSakeAmountVOT.getAmount());
                if (yearSakeAmountT != null) {
                    saleDetailVO.setYearBeforeLastSaleProportion(skuYearSakeAmountVOT.getAmount().divide(yearSakeAmountT,4,BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)));
                } else {
                    saleDetailVO.setYearBeforeLastSaleProportion(BigDecimal.ZERO);
                }
            }

            //退货环比
            SkuYearSaleAmountVO skuYearSakeAmountVO1 = skuYearSakeAmountVOS1.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVO1 != null) {
                if (skuYearSakeAmountVO1.getAmount() != null) {
                    saleDetailVO.setReturnOrderRingRatio(saleDetailVO.getReturnOrderAmount().subtract(skuYearSakeAmountVO1.getAmount()).divide(skuYearSakeAmountVO1.getAmount(),4,BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)));
                } else {
                    saleDetailVO.setReturnOrderRingRatio(BigDecimal.ZERO);
                }
            }

        }
        return saleDetailList;
    }

    /**
     * 销售明细表-用户
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
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
                saleDetailVO.setSaleProportion(saleDetailVO.getSaleAmount().divide(targetSaleSumVO.getValue(),4,BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)));
            }
            //计算去年sku销售额
            SkuYearSaleAmountVO skuYearSakeAmountVO = skuYearSakeAmountVOS.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVO != null) {
                saleDetailVO.setLastYearSaleAmount(skuYearSakeAmountVO.getAmount());
                if (yearSakeAmount != null) {
                    saleDetailVO.setLastYearSaleProportion(skuYearSakeAmountVO.getAmount().divide(yearSakeAmount,4,BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)));
                } else {
                    saleDetailVO.setLastYearSaleProportion(BigDecimal.ZERO);
                }
            }
            //计算前年sku销售额
            SkuYearSaleAmountVO skuYearSakeAmountVOT = skuYearSakeAmountVOST.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVOT != null) {
                saleDetailVO.setYearBeforeLastSaleAmount(skuYearSakeAmountVOT.getAmount());
                if (yearSakeAmountT != null) {
                    saleDetailVO.setYearBeforeLastSaleProportion(skuYearSakeAmountVOT.getAmount().divide(yearSakeAmountT,4,BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)));
                } else {
                    saleDetailVO.setYearBeforeLastSaleProportion(BigDecimal.ZERO);
                }
            }

            //退货环比
            SkuYearSaleAmountVO skuYearSakeAmountVO1 = skuYearSakeAmountVOS1.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVO1 != null) {
                if (skuYearSakeAmountVO1.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    saleDetailVO.setReturnOrderRingRatio(BigDecimal.ZERO);
                } else {
                    saleDetailVO.setReturnOrderRingRatio(saleDetailVO.getReturnOrderAmount().subtract(skuYearSakeAmountVO1.getAmount()).divide(skuYearSakeAmountVO1.getAmount(),4,BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)));
                }
            }
        }
        return saleDetailList;
    }

    /**
     * 销售明细表-日期
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     **/
    @Override
    public List<SaleDetailVO> saleDetailDate(SkuFilterDTO biFilterDTO) {
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
        List<SkuYearSaleAmountVO> skuYearSakeAmountVOS = baseMapper.dateYearSaleAmountBySku(startTime, endTime, biFilterDTO.getSkuNo());
        BigDecimal yearSakeAmount = baseMapper.yearSaleAmountBySku(startTime, endTime, biFilterDTO.getSkuNo());

        //查询前年sku销售信息
        date = DateUtil.addDateYears(new Date(), -2);
        calendar = Calendar.getInstance();
        calendar.setTime(date);
        year = calendar.get(Calendar.YEAR);//获取年
        startTime = sdf.format(DateUtil.getYearFirst(year));
        endTime = sdf.format(DateUtil.getYearLast(year));
        List<SkuYearSaleAmountVO> skuYearSakeAmountVOST = baseMapper.dateYearSaleAmountBySku(startTime, endTime, biFilterDTO.getSkuNo());
        BigDecimal yearSakeAmountT = baseMapper.yearSaleAmountBySku(startTime, endTime, biFilterDTO.getSkuNo());

        Date endDate = Date.from(biFilterDTO.getEndTime().atZone(ZoneId.systemDefault()).toInstant());
        Date startDate = Date.from(biFilterDTO.getStartTime().atZone(ZoneId.systemDefault()).toInstant());
        String start = DateUtil.getRingRatioDate(endDate, startDate);
        //设置时间格式
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<SkuYearSaleAmountVO> skuYearSakeAmountVOS1 = baseMapper.dateReturnOrderAmountByDate(start, f.format(startDate), biFilterDTO.getSkuNo());

        //组装近两年销售额信息
        List<SaleDetailVO> saleDetailList = baseMapper.saleDetailDate(biFilterDTO);
        for (SaleDetailVO saleDetailVO : saleDetailList) {
            if (saleDetailVO.getSaleAmount().compareTo(BigDecimal.ZERO) <= 0) {
                saleDetailVO.setSaleProportion(BigDecimal.ZERO);
            } else {
                saleDetailVO.setSaleProportion(saleDetailVO.getSaleAmount().divide(targetSaleSumVO.getValue(),4,BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)));
            }
            //计算去年sku销售额
            SkuYearSaleAmountVO skuYearSakeAmountVO = skuYearSakeAmountVOS.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVO != null) {
                saleDetailVO.setLastYearSaleAmount(skuYearSakeAmountVO.getAmount());
                if (yearSakeAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    saleDetailVO.setLastYearSaleProportion(BigDecimal.ZERO);
                } else {
                    saleDetailVO.setLastYearSaleProportion(skuYearSakeAmountVO.getAmount().divide(yearSakeAmount,4,BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)));
                }
            }
            //计算前年sku销售额
            SkuYearSaleAmountVO skuYearSakeAmountVOT = skuYearSakeAmountVOST.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVOT != null) {
                saleDetailVO.setYearBeforeLastSaleAmount(skuYearSakeAmountVOT.getAmount());
                if (yearSakeAmountT.compareTo(BigDecimal.ZERO) <= 0) {
                    saleDetailVO.setYearBeforeLastSaleProportion(BigDecimal.ZERO);
                } else {
                    saleDetailVO.setYearBeforeLastSaleProportion(skuYearSakeAmountVOT.getAmount().divide(yearSakeAmountT,4,BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)));
                }
            }

            //退货环比
            SkuYearSaleAmountVO skuYearSakeAmountVO1 = skuYearSakeAmountVOS1.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVO1 != null) {
                if (skuYearSakeAmountVO1.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    saleDetailVO.setReturnOrderRingRatio(BigDecimal.ZERO);
                } else {
                    saleDetailVO.setReturnOrderRingRatio(saleDetailVO.getReturnOrderAmount().subtract(skuYearSakeAmountVO1.getAmount()).divide(skuYearSakeAmountVO1.getAmount(),4,BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)));
                }
            }
        }
        return saleDetailList;
    }

    /**
     * SKU日期销售额趋势图
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
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
}
