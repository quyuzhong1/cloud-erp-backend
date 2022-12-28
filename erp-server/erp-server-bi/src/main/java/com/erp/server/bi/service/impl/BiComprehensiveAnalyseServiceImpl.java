package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.date.DateUtil;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.BiFilterDTO;
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
    public PagingVO<MatrixVO> skuMatrix(PagingDTO<BiFilterDTO> biFilterDTO) {
        biFilterDTO.getParams().setParam(biFilterDTO.getParam());
        Page query = new Page(biFilterDTO.getCurrPage(), biFilterDTO.getPageSize());
        IPage<MatrixVO> skuMatrixVOIPage = baseMapper.skuMatrix(query, biFilterDTO.getParams());
        return new PagingVO(skuMatrixVOIPage);
    }

    /**
     * 店铺矩阵
     * @Author Luo_WG
     * @Date 2022/12/26 10:42
     * @param biFilterDTO biFilterDTO
     * @return com.erp.common.vo.PagingVO<com.erp.model.bi.vo.SkuMatrixVO>
     **/
    @Override
    public PagingVO<MatrixVO> shopMatrix(PagingDTO<BiFilterDTO> biFilterDTO) {
        biFilterDTO.getParams().setParam(biFilterDTO.getParam());
        Page query = new Page(biFilterDTO.getCurrPage(), biFilterDTO.getPageSize());
        IPage<MatrixVO> skuMatrixVOIPage = baseMapper.shopMatrix(query, biFilterDTO.getParams());
        return new PagingVO(skuMatrixVOIPage);
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
    public PagingVO<MatrixVO> categoryMatrix(PagingDTO<BiFilterDTO> biFilterDTO) {
        biFilterDTO.getParams().setParam(biFilterDTO.getParam());
        Page query = new Page(biFilterDTO.getCurrPage(), biFilterDTO.getPageSize());
        IPage<MatrixVO> skuMatrixVOIPage = baseMapper.categoryMatrix(query, biFilterDTO.getParams());
        return new PagingVO(skuMatrixVOIPage);
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
                saleDetailVO.setSaleProportion(saleDetailVO.getSaleAmount().divide(targetSaleSumVO.getValue()).multiply(BigDecimal.valueOf(100)));
            }
            //计算去年sku销售额
            SkuYearSaleAmountVO skuYearSakeAmountVO = skuYearSakeAmountVOS.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVO != null) {
                saleDetailVO.setLastYearSaleAmount(skuYearSakeAmountVO.getAmount());
                saleDetailVO.setLastYearSaleProportion(skuYearSakeAmountVO.getAmount().divide(yearSakeAmount).multiply(BigDecimal.valueOf(100)));
            }
            //计算前年sku销售额
            SkuYearSaleAmountVO skuYearSakeAmountVOT = skuYearSakeAmountVOST.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVOT != null) {
                saleDetailVO.setYearBeforeLastSaleAmount(skuYearSakeAmountVOT.getAmount());
                saleDetailVO.setYearBeforeLastSaleProportion(skuYearSakeAmountVOT.getAmount().divide(yearSakeAmountT).multiply(BigDecimal.valueOf(100)));
            }

            //退货环比
            SkuYearSaleAmountVO skuYearSakeAmountVO1 = skuYearSakeAmountVOS1.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVO1 != null) {
                saleDetailVO.setReturnOrderRingRatio(saleDetailVO.getReturnOrderAmount().subtract(skuYearSakeAmountVO1.getAmount()).divide(skuYearSakeAmountVO1.getAmount()).multiply(BigDecimal.valueOf(100)));
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
                saleDetailVO.setSaleProportion(saleDetailVO.getSaleAmount().divide(targetSaleSumVO.getValue()).multiply(BigDecimal.valueOf(100)));
            }
            //计算去年sku销售额
            SkuYearSaleAmountVO skuYearSakeAmountVO = skuYearSakeAmountVOS.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVO != null) {
                saleDetailVO.setLastYearSaleAmount(skuYearSakeAmountVO.getAmount());
                saleDetailVO.setLastYearSaleProportion(skuYearSakeAmountVO.getAmount().divide(yearSakeAmount).multiply(BigDecimal.valueOf(100)));
            }
            //计算前年sku销售额
            SkuYearSaleAmountVO skuYearSakeAmountVOT = skuYearSakeAmountVOST.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVOT != null) {
                saleDetailVO.setYearBeforeLastSaleAmount(skuYearSakeAmountVOT.getAmount());
                saleDetailVO.setYearBeforeLastSaleProportion(skuYearSakeAmountVOT.getAmount().divide(yearSakeAmountT).multiply(BigDecimal.valueOf(100)));
            }

            //退货环比
            SkuYearSaleAmountVO skuYearSakeAmountVO1 = skuYearSakeAmountVOS1.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVO1 != null) {
                saleDetailVO.setReturnOrderRingRatio(saleDetailVO.getReturnOrderAmount().subtract(skuYearSakeAmountVO1.getAmount()).divide(skuYearSakeAmountVO1.getAmount()).multiply(BigDecimal.valueOf(100)));
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
                saleDetailVO.setSaleProportion(saleDetailVO.getSaleAmount().divide(targetSaleSumVO.getValue()).multiply(BigDecimal.valueOf(100)));
            }
            //计算去年sku销售额
            SkuYearSaleAmountVO skuYearSakeAmountVO = skuYearSakeAmountVOS.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVO != null) {
                saleDetailVO.setLastYearSaleAmount(skuYearSakeAmountVO.getAmount());
                saleDetailVO.setLastYearSaleProportion(skuYearSakeAmountVO.getAmount().divide(yearSakeAmount).multiply(BigDecimal.valueOf(100)));
            }
            //计算前年sku销售额
            SkuYearSaleAmountVO skuYearSakeAmountVOT = skuYearSakeAmountVOST.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVOT != null) {
                saleDetailVO.setYearBeforeLastSaleAmount(skuYearSakeAmountVOT.getAmount());
                saleDetailVO.setYearBeforeLastSaleProportion(skuYearSakeAmountVOT.getAmount().divide(yearSakeAmountT).multiply(BigDecimal.valueOf(100)));
            }

            //退货环比
            SkuYearSaleAmountVO skuYearSakeAmountVO1 = skuYearSakeAmountVOS1.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVO1 != null) {
                saleDetailVO.setReturnOrderRingRatio(saleDetailVO.getReturnOrderAmount().subtract(skuYearSakeAmountVO1.getAmount()).divide(skuYearSakeAmountVO1.getAmount()).multiply(BigDecimal.valueOf(100)));
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
    public List<SaleDetailVO> saleDetailDate(BiFilterDTO biFilterDTO) {
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
        List<SaleDetailVO> saleDetailList = baseMapper.saleDetailDate(biFilterDTO);
        for (SaleDetailVO saleDetailVO : saleDetailList) {
            if (saleDetailVO.getSaleAmount().compareTo(BigDecimal.ZERO) <= 0) {
                saleDetailVO.setSaleProportion(BigDecimal.ZERO);
            } else {
                saleDetailVO.setSaleProportion(saleDetailVO.getSaleAmount().divide(targetSaleSumVO.getValue()).multiply(BigDecimal.valueOf(100)));
            }
            //计算去年sku销售额
            SkuYearSaleAmountVO skuYearSakeAmountVO = skuYearSakeAmountVOS.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVO != null) {
                saleDetailVO.setLastYearSaleAmount(skuYearSakeAmountVO.getAmount());
                saleDetailVO.setLastYearSaleProportion(skuYearSakeAmountVO.getAmount().divide(yearSakeAmount).multiply(BigDecimal.valueOf(100)));
            }
            //计算前年sku销售额
            SkuYearSaleAmountVO skuYearSakeAmountVOT = skuYearSakeAmountVOST.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVOT != null) {
                saleDetailVO.setYearBeforeLastSaleAmount(skuYearSakeAmountVOT.getAmount());
                saleDetailVO.setYearBeforeLastSaleProportion(skuYearSakeAmountVOT.getAmount().divide(yearSakeAmountT).multiply(BigDecimal.valueOf(100)));
            }

            //退货环比
            SkuYearSaleAmountVO skuYearSakeAmountVO1 = skuYearSakeAmountVOS1.stream().filter(p -> p.getName().equals(saleDetailVO.getName())).findFirst().orElse(null);
            if (skuYearSakeAmountVO1 != null) {
                saleDetailVO.setReturnOrderRingRatio(saleDetailVO.getReturnOrderAmount().subtract(skuYearSakeAmountVO1.getAmount()).divide(skuYearSakeAmountVO1.getAmount()).multiply(BigDecimal.valueOf(100)));
            }

        }
        return saleDetailList;
    }
}
