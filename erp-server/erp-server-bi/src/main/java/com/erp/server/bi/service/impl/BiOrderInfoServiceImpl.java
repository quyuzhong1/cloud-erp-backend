package com.erp.server.bi.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import static com.alibaba.excel.EasyExcel.read;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.RedisService;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.entity.BiDictEntity;
import com.erp.model.bi.entity.BiTargetManagementEntity;
import com.erp.model.bi.enums.SaleContryTypeEnum;
import com.erp.model.bi.enums.SalePriceRangeEnum;
import com.erp.model.bi.vo.*;
import com.erp.model.dmp.dto.*;
import com.erp.model.dmp.entity.BiOrderInfoEntity;
import com.erp.model.dmp.entity.BiOrderItemSplitEntity;
import com.erp.model.dmp.entity.BiShopInfoEntity;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.constant.BiConstant;
import com.erp.server.bi.enums.OrderStateEnum;
import com.erp.server.bi.enums.SettleMethodEnum;
import com.erp.server.bi.enums.TimeTypeEnum;
import com.erp.server.bi.listener.DmpOrderInfoExcelListener;
import com.erp.server.bi.mapper.BiOrderInfoMapper;
import com.erp.server.bi.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 订单服务类
 *
 * @author Cloud
 */
@Service
public class BiOrderInfoServiceImpl extends ServiceImpl<BiOrderInfoMapper, BiOrderInfoEntity>
        implements BiOrderInfoService {
    @Resource
    private BiOrderItemSplitService biOrderItemSplitService;
    @Resource
    private BiReturnOrderInfoService biReturnOrderInfoService;
    @Resource
    private BiShopInfoService biShopInfoService;
    @Resource
    private RedisService redisService;
    @Resource
    private BiTargetManagementService biTargetManagementService;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private BiDictService biDictService;
    
    public static final String PLATFORM_CREATE_TIME = "platform_create_time";
    public static final String DELIVERY_TIME = "delivery_time";

    @Override
    public PagingVO<DmpOrderInfoDTO> paging(PagingDTO<DmpOrderInfoSearchDTO> dto) {
        Page<Object> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        DmpOrderInfoSearchDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        IPage<DmpOrderInfoDTO> pageData = baseMapper.paging(query, params);
        List<DmpOrderInfoDTO> records = pageData.getRecords();
        dmpOrderInfoHand(records);
        return new PagingVO<>(pageData);
    }

    @Override
    public Boolean updateState(DmpOrderStateDTO dto) {
        BiOrderInfoEntity biOrderInfoEntity = this.getById(dto.getId());
        if (ObjectUtils.isEmpty(biOrderInfoEntity)) {
            throw new ServiceException(ApiError.Default);
        }
        biOrderInfoEntity.setCorrectionStatus(dto.getState());
        return this.updateById(biOrderInfoEntity);
    }

    @Override
    public void exportExcel(DmpOrderInfoSearchDTO dto, HttpServletResponse response) {
        //查询所有数据
        List<DmpOrderInfoExcelDTO> excelList = baseMapper.getAllDmpOrderInfo(dto);
        if (CollectionUtils.isEmpty(excelList)) {
            return;
        }
        String fileName = getFileName("销售数据导出");
        ExcelUtil.export(fileName, "销售数据导出", excelList, DmpOrderInfoExcelDTO.class, response);
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:sumSales", keyGenerator = "myKeyGenerator")
    public TargetSaleSumVO sumSales(BiFilterDTO dto) {
        // 没有sku情况
        if (null != dto.getHasNewSign() && dto.getHasNewSign()) {
            dto.setNewSign(1);
        }
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        BigDecimal amount = baseMapper.sumSales(dto, settleRate);
        if (Objects.nonNull(amount)) {
            return new TargetSaleSumVO(amount);
        } else {
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }
    }

    @Override
    public List<SalePriceDistributionVO> salePriceDistribution(BiFilterDTO dto) {
        //获取区间列表
        List<SalesPriceRangeVO> rangeVOS = getRangeList(dto.getRangeType());
        //获取店铺列表
        List<BiShopInfoEntity> shopInfoList = biShopInfoService.listByStoreSign();
        String cn = BiConstant.CN;
        if (1 == dto.getRangeType()) {
            dto.setShopNo(shopInfoList.stream().filter(s -> cn.equals(s.getStoreSign())).
                    map(BiShopInfoEntity::getPlatformShopNo).collect(Collectors.toList()));
        } else {
            dto.setShopNo(shopInfoList.stream().filter(s -> !cn.equals(s.getStoreSign())).
                    map(BiShopInfoEntity::getPlatformShopNo).collect(Collectors.toList()));
        }
        String settleRate = getSettleRate(dto.getSettleMethod());
        List<SalePriceDistributionVO> salePriceDistributionVOS = new ArrayList<>(rangeVOS.size());
        rangeVOS.forEach(salesPriceRangeVO -> salePriceDistributionVOS.add(baseMapper.countSalePriceDistribution(dto, settleRate, salesPriceRangeVO)));
        BigDecimal salesTotal = salePriceDistributionVOS.stream().map(SalePriceDistributionVO::getSaleAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO,BigDecimal::add);
        Integer qtyTotal = salePriceDistributionVOS.stream().filter(s -> Objects.nonNull(s.getSalesQuantity())).mapToInt(SalePriceDistributionVO::getSalesQuantity).sum();
            //占比计算
        salePriceDistributionVOS.forEach(salePriceDistributionVO -> {
            BigDecimal saleRate = BigDecimal.ZERO;
            if (Objects.nonNull(salePriceDistributionVO.getSaleAmount()) && salesTotal.compareTo(BigDecimal.ZERO) > 0){
                saleRate = MathUtil.divide(salePriceDistributionVO.getSaleAmount(), salesTotal).multiply(MathUtil.BigDecimal_100);
            }
            salePriceDistributionVO.setSaleAmountRate(saleRate.stripTrailingZeros().toPlainString());
            BigDecimal qtyRate = BigDecimal.ZERO;
            if (Objects.nonNull(salePriceDistributionVO.getSalesQuantity()) && qtyTotal > 0){
                qtyRate = MathUtil.divide(BigDecimal.valueOf(salePriceDistributionVO.getSalesQuantity()), BigDecimal.valueOf(qtyTotal)).multiply(MathUtil.BigDecimal_100);
            }
            salePriceDistributionVO.setSalesQuantityRate(qtyRate.stripTrailingZeros().toPlainString());
            });
        salePriceDistributionVOS.sort(Comparator.comparingInt(SalePriceDistributionVO::getStartValue));
        return salePriceDistributionVOS;
    }

    /**
     * 获取到结算汇率
     *
     * @param code
     * @return
     */
    private String getSettleRate(Integer code) {
        SettleMethodEnum settleMethod = SettleMethodEnum.getByCode(code);
        if (settleMethod != null) {
            return settleMethod.getField();
        }
        return "";
    }


    private List<SalesPriceRangeVO> getRangeList(Integer rangeType) {
        List<BiDictEntity> biDictEntities = 1 == rangeType ? biDictService.listEntityByType(SaleContryTypeEnum.DOMESTIC.code)
                : biDictService.listEntityByType(SaleContryTypeEnum.ABROAD.code);
        List<SalesPriceRangeVO> rangeVOS = new ArrayList<>();
        if (CollectionUtils.isEmpty(biDictEntities)) {
            return getDefaultRangeList(rangeType);
        } else {
            biDictEntities.forEach(biDictEntity -> {
                List<String> strings = Arrays.asList(biDictEntity.getName().split(","));
                rangeVOS.add(new SalesPriceRangeVO(biDictEntity.getId(), biDictEntity.getValue(), rangeType, Integer.parseInt(strings.get(0)), Integer.parseInt(strings.get(1))));
            });
        }
        return rangeVOS;
    }

    private List<SalesPriceRangeVO> getDefaultRangeList(Integer rangeType) {
        List<SalesPriceRangeVO> rangeVOS = new ArrayList<>();
        rangeVOS.add(new SalesPriceRangeVO(null, null, rangeType, SalePriceRangeEnum.ZERO.getStartValue(), SalePriceRangeEnum.ZERO.getEndValue()));
        rangeVOS.add(new SalesPriceRangeVO(null, null, rangeType, SalePriceRangeEnum.ONE_HUNDRED.getStartValue(), SalePriceRangeEnum.ONE_HUNDRED.getEndValue()));
        rangeVOS.add(new SalesPriceRangeVO(null, null, rangeType, SalePriceRangeEnum.TWO_HUNDRED.getStartValue(), SalePriceRangeEnum.TWO_HUNDRED.getEndValue()));
        rangeVOS.add(new SalesPriceRangeVO(null, null, rangeType, SalePriceRangeEnum.THREE_HUNDRED.getStartValue(), SalePriceRangeEnum.THREE_HUNDRED.getEndValue()));
        rangeVOS.add(new SalesPriceRangeVO(null, null, rangeType, SalePriceRangeEnum.FOUR_HUNDRED.getStartValue(), SalePriceRangeEnum.FOUR_HUNDRED.getEndValue()));
        rangeVOS.add(new SalesPriceRangeVO(null, null, rangeType, SalePriceRangeEnum.FIVE_HUNDRED.getStartValue(), SalePriceRangeEnum.FIVE_HUNDRED.getEndValue()));
        rangeVOS.add(new SalesPriceRangeVO(null, null, rangeType, SalePriceRangeEnum.SIX_HUNDRED.getStartValue(), SalePriceRangeEnum.SIX_HUNDRED.getEndValue()));
        rangeVOS.add(new SalesPriceRangeVO(null, null, rangeType, SalePriceRangeEnum.SEVEN_HUNDRED.getStartValue(), SalePriceRangeEnum.SEVEN_HUNDRED.getEndValue()));
        return rangeVOS;
    }

    private static QueryWrapper<BiOrderInfoEntity> getDmpOrderInfoEntityQueryWrapper(BiFilterDTO dto) {
        QueryWrapper<BiOrderInfoEntity> query = new QueryWrapper<>();
        query.ge(dto.getTimeType().equals(TimeTypeEnum.ORDER_TIME.getCode()), BiOrderInfoEntity.PLATFORM_CREATE_TIME, dto.getStartTime())
                .lt(dto.getTimeType().equals(TimeTypeEnum.ORDER_TIME.getCode()), BiOrderInfoEntity.PLATFORM_CREATE_TIME, dto.getEndTime())
                // 订单时间字段
                .ge(dto.getTimeType().equals(TimeTypeEnum.DELIVERY_TIME.getCode()), BiOrderInfoEntity.DELIVERY_TIME, dto.getStartTime())
                .lt(dto.getTimeType().equals(TimeTypeEnum.DELIVERY_TIME.getCode()), BiOrderInfoEntity.DELIVERY_TIME, dto.getEndTime())
                // 高级筛选字段待完善 事业部 站点 品类 品牌 人员
                //事业部
                .in(CollectionUtils.isNotEmpty(dto.getDepartment()), "dept_id", dto.getDepartment())
                //站点
                .in(CollectionUtils.isNotEmpty(dto.getSite()), "site", dto.getSite())
                //品类
                .in(CollectionUtils.isNotEmpty(dto.getCategory()), "category", dto.getCategory())
                // 品牌
                .in(CollectionUtils.isNotEmpty(dto.getBrand()), "brand", dto.getBrand())
                // 人员
                .in(CollectionUtils.isNotEmpty(dto.getUserId()), "charge_id", dto.getUserId())
                // 平台
                .in(CollectionUtils.isNotEmpty(dto.getPlatform()), "source_platform", dto.getPlatform())
                // 店铺
                .in(CollectionUtils.isNotEmpty(dto.getShopName()), "shop_name", dto.getShopName())
                // 权限
                .last(StringUtils.isNotBlank(dto.getPermissionSql()), dto.getPermissionSql());
        return query;
    }

    @Override
    public TargetSaleCountVO countSalesVolume(BiFilterDTO dto) {
        Integer count = baseMapper.countSalesVolume(dto);
        return new TargetSaleCountVO(count);
    }

    @Override
    public TargetSaleCountVO countOrderQuantity(BiFilterDTO dto) {
        // 没有sku情况
        Integer count = 0;
        QueryWrapper<BiOrderInfoEntity> query = getDmpOrderInfoEntityQueryWrapper(dto);
        // 无sku条件 只查询订单表
        if (CollectionUtils.isEmpty(dto.getSku())) {
            count = baseMapper.selectCount(query);
        } else {
            // 条件存在sku的情况 查询订单详情表
            // 先查询订单号
            query.select("id");
            List<BiOrderInfoEntity> list = baseMapper.selectList(query);
            if (CollectionUtils.isEmpty(list)) {
                return new TargetSaleCountVO(count);
            }
            List<String> orderIds = list.stream().map(BiOrderInfoEntity::getId).collect(Collectors.toList());
            // 根据订单号获取订单详情，筛选sku
            count = biOrderItemSplitService.countOrderQuantityBySku(orderIds, dto.getSku());
        }
        return new TargetSaleCountVO(count);
    }

    @Override
    public TargetSaleSumVO countRefundRate(BiFilterDTO dto) {
        // 获取总订单数量
        TargetSaleCountVO totalOrderQuantity = countOrderQuantity(dto);
        if (null == totalOrderQuantity || totalOrderQuantity.getValue() <= 0) {
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }
        TargetSaleCountVO refundOrderCount = countRefundOrderNum(dto);
        BigDecimal refundRate = new BigDecimal(refundOrderCount.getValue())
                .divide(new BigDecimal(totalOrderQuantity.getValue()), 2, BigDecimal.ROUND_DOWN);
        return new TargetSaleSumVO(refundRate);
    }

    @Override
    public TargetSaleSumVO countRefundAmount(BiFilterDTO dto) {
        // 获取退款金额
        QueryWrapper<BiOrderInfoEntity> query = getDmpOrderInfoEntityQueryWrapper(dto);
        // 无sku条件 只查询订单表
        query.eq("is_returned", 1);
        List<BiOrderInfoEntity> list = baseMapper.selectList(query);
        if (CollectionUtils.isEmpty(list)) {
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }
        List<String> orderIds = list.stream().map(BiOrderInfoEntity::getId).collect(Collectors.toList());
        BigDecimal amount = biReturnOrderInfoService.sumRefundAmount(orderIds, dto);
        return new TargetSaleSumVO(amount);
    }

    @Override
    public TargetSaleCountVO countRefundOrderNum(BiFilterDTO dto) {
        // 获取退款订单数量
        // 没有sku情况
        Integer count;
        QueryWrapper<BiOrderInfoEntity> query = getDmpOrderInfoEntityQueryWrapper(dto);
        // 无sku条件 只查询订单表
        query.eq("is_refund", 1);
        if (CollectionUtils.isEmpty(dto.getSku())) {
            count = baseMapper.selectCount(query);
        } else {
            // 条件存在sku的情况 查询订单详情表
            // 先查询订单号
            query.select("id");
            List<BiOrderInfoEntity> list = baseMapper.selectList(query);
            if (CollectionUtils.isEmpty(list)) {
                return new TargetSaleCountVO(0);
            }
            List<String> orderIds = list.stream().map(BiOrderInfoEntity::getId).collect(Collectors.toList());
            // 根据订单号获取订单详情，筛选sku
            count = biOrderItemSplitService.countOrderQuantityBySku(orderIds, dto.getSku());
        }
        return new TargetSaleCountVO(count);
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:statisticsCustomerPrice", keyGenerator = "myKeyGenerator")
    public TargetSaleSumVO statisticsCustomerPrice(BiFilterDTO dto) {
        // 销售额
        TargetSaleSumVO targetSaleSumVO = sumSales(dto);
        BigDecimal salesAmount = targetSaleSumVO.getValue();
        if (BigDecimal.ZERO.compareTo(salesAmount) >= 0) {
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }
        // 订单数量
        TargetSaleCountVO targetSaleCountVO = countOrderQuantity(dto);
        Integer orderNum = targetSaleCountVO.getValue();
        if (0 >= orderNum) {
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }
        // 客单价 = 销售额 / 订单量
        BigDecimal customerPrice = salesAmount.divide(new BigDecimal(orderNum), 4, BigDecimal.ROUND_DOWN);
        return new TargetSaleSumVO(customerPrice);
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:statisticsDomesticSalesRatio", keyGenerator = "myKeyGenerator")
    public TargetSaleSumVO statisticsDomesticSalesRatio(BiFilterDTO dto) {
        // 销售总额
        TargetSaleSumVO targetSaleSumVO = sumSales(dto);
        BigDecimal salesAmount = targetSaleSumVO.getValue();
        if (BigDecimal.ZERO.compareTo(salesAmount) >= 0) {
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }
        // 查询国内店铺no
        List<BiShopInfoEntity> shopList = biShopInfoService.lambdaQuery()
                .eq(BiShopInfoEntity::getStatus, 1)
                .eq(BiShopInfoEntity::getStoreSign, "cn")
                .last(StringUtils.isNotBlank(dto.getPermissionSql()), dto.getPermissionSql())
                .list();
        if (CollectionUtils.isEmpty(shopList)) {
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }
        // 国内销售额
        dto.setShopName(shopList.stream().map(BiShopInfoEntity::getName).collect(Collectors.toList()));
        TargetSaleSumVO saleSumVO = sumSales(dto);
        BigDecimal domesticAmount = saleSumVO.getValue();
        if (BigDecimal.ZERO.compareTo(salesAmount) >= 0) {
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }

        // 国内销售占比
        BigDecimal ratio = domesticAmount.divide(salesAmount, 4, BigDecimal.ROUND_DOWN);
        return new TargetSaleSumVO(ratio);
    }

    /**
     * @param fileName
     * @return String
     * @description: 导出文件名称
     * @author Will
     * @date: 2022/12/15 10:35
     */
    @Override
    public String getFileName(String fileName) {
        StringBuilder sb = new StringBuilder();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(fileName);
        sb.append(date);
        String redisKey = "file:name:" + date;
        Integer last = redisService.getCacheObject(redisKey);
        Integer lastNo = 1;
        if (last != null) {
            lastNo = last + 1;
        }
        redisService.setCacheObject(redisKey, lastNo, (long) 1, TimeUnit.DAYS);
        return sb.append(lastNo).toString();
    }


    @Override
    public BiOrderInfoEntity getByPlatformOrderId(String platformOrderId) {
        LambdaQueryWrapper<BiOrderInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiOrderInfoEntity::getPlatformOrderId, platformOrderId);
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }


    @Override
    @Cacheable(cacheNames = "cache:bi:sumQuarterSales", keyGenerator = "myKeyGenerator")
    public TargetAnalysisVO<QuarterMonthSalesVO> sumQuarterSales(BiFilterDTO dto) {
        // 获取年度开始时间和结束时间
        LocalDateTime start = LocalDateTime.of(LocalDate.from(dto.getStartTime().with(TemporalAdjusters.firstDayOfYear())), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.from(dto.getStartTime().with(TemporalAdjusters.lastDayOfYear())), LocalTime.MAX);
        // 查询目标销售额
        Map<Integer, BigDecimal> quarterTargetMap = new HashMap<>(4);
        // 统计目标销售额
        BiTargetManagementEntity target = biTargetManagementService.getMonthSales(start, end, dto.getPermissionSql(), 1);
        if (ObjectUtil.isNull(target)) {
            return getQuarterResultList(quarterTargetMap, new HashMap<>(4), start.getYear());
        }
        // 四个季度
        quarterTargetMap.put(1, target.getJanuary().add(target.getFebruary().add(target.getMarch())));
        quarterTargetMap.put(2, target.getApril().add(target.getMay().add(target.getJune())));
        quarterTargetMap.put(3, target.getJuly().add(target.getAugust().add(target.getSeptember())));
        quarterTargetMap.put(4, target.getOctober().add(target.getNovember().add(target.getDecember())));

        // 查询销售额
        QueryWrapper<BiOrderInfoEntity> qw = new QueryWrapper<>();
        boolean flag = TimeTypeEnum.ORDER_TIME.getCode() == dto.getTimeType();
        String groupByStr = flag ? PLATFORM_CREATE_TIME : DELIVERY_TIME;
        qw.select("SUM(COALESCE(amount_after,0)) as order_fee", groupByStr);
        List<BiOrderInfoEntity> entityList = getOrderInfoEntities(dto, qw, start, end, groupByStr);
        if (CollectionUtils.isEmpty(entityList)) {
            return getQuarterResultList(quarterTargetMap, new HashMap<>(4), start.getYear());
        }
        Map<Integer, BigDecimal> quarterMap = entityList.stream().collect(Collectors.groupingBy(x -> (
                        // 按照季度分组
                        (flag ? x.getPlatformCreateTime() : x.getDeliveryTime()).getMonthValue() - 1) / 3 + 1,
                // 对销售额进行求和
                Collectors.reducing(BigDecimal.ZERO, BiOrderInfoEntity::getOrderFee, BigDecimal::add)
        ));

        // 计算完成率
        return getQuarterResultList(quarterTargetMap, quarterMap, dto.getStartTime().getYear());
    }

    private List<BiOrderInfoEntity> getOrderInfoEntities(BiFilterDTO dto, QueryWrapper<BiOrderInfoEntity> qw, LocalDateTime start, LocalDateTime end, String groupStr) {
        boolean flag1 = TimeTypeEnum.ORDER_TIME.getCode() == dto.getTimeType();
        boolean flag2 = TimeTypeEnum.DELIVERY_TIME.getCode() == dto.getTimeType();
        qw.ge(flag1, PLATFORM_CREATE_TIME, start)
                .le(flag1, PLATFORM_CREATE_TIME, end)
                .ge(flag2, DELIVERY_TIME, start)
                .le(flag2, DELIVERY_TIME, end)
                .groupBy(StringUtils.isNotBlank(groupStr), groupStr)
                .last(StringUtils.isNotBlank(dto.getPermissionSql()), dto.getPermissionSql());
        return baseMapper.selectList(qw);
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:sumQuarterSalesVolume", keyGenerator = "myKeyGenerator")
    public TargetAnalysisVO<QuarterMonthSalesVolumeVO> sumQuarterSalesVolume(BiFilterDTO dto) {
        // 获取年度开始时间和结束时间
        int year = dto.getStartTime().getYear();
        LocalDateTime start = LocalDateTime.of(LocalDate.from(dto.getStartTime().with(TemporalAdjusters.firstDayOfYear())), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.from(dto.getStartTime().with(TemporalAdjusters.lastDayOfYear())), LocalTime.MAX);
        // 查询目标销量
        Map<Integer, Integer> quarterTargetMap = new HashMap<>(4);

        // 统计目标销量
        BiTargetManagementEntity target = biTargetManagementService.getMonthSales(start, end, dto.getPermissionSql(), 0);
        if (ObjectUtil.isNull(target)) {
            return getQuarterVolumeResultList(quarterTargetMap, new HashMap<>(4), start.getYear());
        }
        // 四个季度
        quarterTargetMap.put(1, target.getJanuary().add(target.getFebruary().add(target.getMarch())).intValue());
        quarterTargetMap.put(2, target.getApril().add(target.getMay().add(target.getJune())).intValue());
        quarterTargetMap.put(3, target.getJuly().add(target.getAugust().add(target.getSeptember())).intValue());
        quarterTargetMap.put(4, target.getOctober().add(target.getNovember().add(target.getDecember())).intValue());

        // 查询销售额
        QueryWrapper<BiOrderInfoEntity> qw = new QueryWrapper<>();
        boolean flag = TimeTypeEnum.ORDER_TIME.getCode() == dto.getTimeType();
        qw.select("id", PLATFORM_CREATE_TIME, DELIVERY_TIME);
        List<BiOrderInfoEntity> entityList = getOrderInfoEntities(dto, qw, start, end, null);
        if (CollectionUtils.isEmpty(entityList)) {
            return getQuarterVolumeResultList(quarterTargetMap, new HashMap<>(4), start.getYear());
        }
        List<String> orderIds = entityList.stream().map(BiOrderInfoEntity::getId).collect(Collectors.toList());
        List<BiOrderItemSplitEntity> entityItemList = biOrderItemSplitService.listByOrderInfoIds(orderIds);
        if (CollectionUtils.isEmpty(entityItemList)) {
            return getQuarterVolumeResultList(quarterTargetMap, new HashMap<>(4), start.getYear());
        }
        // 根据订单号的分组计算销量
        Map<String, Integer> orderQuantityMap = entityItemList.stream().collect(Collectors.groupingBy(BiOrderItemSplitEntity::getOrderId,
                Collectors.summingInt(BiOrderItemSplitEntity::getQuantity)));
        // 对订单号进行季度分组
        Map<Integer, Integer> quarterMap = entityList.stream().collect(Collectors.groupingBy(x -> (
                        // 按照季度分组
                        (flag ? x.getPlatformCreateTime() : x.getDeliveryTime()).getMonthValue() - 1) / 3 + 1,
                Collectors.summingInt(x -> orderQuantityMap.getOrDefault(x.getId(), 0)))
        );
        // 计算完成率
        return getQuarterVolumeResultList(quarterTargetMap, quarterMap, year);
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:sumMonthSales", keyGenerator = "myKeyGenerator")
    public TargetAnalysisVO<QuarterMonthSalesVO> sumMonthSales(BiFilterDTO dto) {
        // 获取月度开始时间和结束时间
        LocalDateTime start = LocalDateTime.of(LocalDate.from(dto.getStartTime().with(TemporalAdjusters.firstDayOfMonth())), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.from(dto.getEndTime().with(TemporalAdjusters.lastDayOfMonth())), LocalTime.MAX);
        // 查询目标销售额
        Map<Integer, BigDecimal> monthTargetMap = new HashMap<>(4);
        // 统计目标销量
        BiTargetManagementEntity target = biTargetManagementService.getMonthSales(start, end, dto.getPermissionSql(), 1);
        if (ObjectUtil.isNull(target)) {
            return getMonthResultList(monthTargetMap, new HashMap<>(4), start.getYear());
        }
        // 12 个月记录
        monthTargetMap.put(1, target.getJanuary());
        monthTargetMap.put(2, target.getFebruary());
        monthTargetMap.put(3, target.getMarch());
        monthTargetMap.put(4, target.getApril());
        monthTargetMap.put(5, target.getMay());
        monthTargetMap.put(6, target.getJune());
        monthTargetMap.put(7, target.getJuly());
        monthTargetMap.put(8, target.getAugust());
        monthTargetMap.put(9, target.getSeptember());
        monthTargetMap.put(10, target.getOctober());
        monthTargetMap.put(11, target.getNovember());
        monthTargetMap.put(12, target.getDecember());
        // 查询销售额
        QueryWrapper<BiOrderInfoEntity> qw = new QueryWrapper<>();
        boolean flag = TimeTypeEnum.ORDER_TIME.getCode() == dto.getTimeType();
        String groupByStr = flag ? PLATFORM_CREATE_TIME : DELIVERY_TIME;
        qw.select("SUM(COALESCE(order_fee, 0)) as order_fee", groupByStr);
        List<BiOrderInfoEntity> entityList = getOrderInfoEntities(dto, qw, start, end, groupByStr);
        if (CollectionUtils.isEmpty(entityList)) {
            return getMonthResultList(monthTargetMap, new HashMap<>(4), start.getYear());
        }
        Map<Integer, BigDecimal> monthMap = entityList.stream().collect(Collectors.groupingBy(x ->
                        // 按照季度分组
                        (flag ? x.getPlatformCreateTime() : x.getDeliveryTime()).getMonthValue(),
                // 对销售额进行求和
                Collectors.reducing(BigDecimal.ZERO, BiOrderInfoEntity::getOrderFee, BigDecimal::add)
        ));

        // 计算完成率
        return getMonthResultList(monthTargetMap, monthMap, start.getYear());
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:sumMonthSalesVolume", keyGenerator = "myKeyGenerator")
    public TargetAnalysisVO<QuarterMonthSalesVolumeVO> sumMonthSalesVolume(BiFilterDTO dto) {
        // 获取月度开始时间和结束时间
        LocalDateTime start = LocalDateTime.of(LocalDate.from(dto.getStartTime().with(TemporalAdjusters.firstDayOfMonth())), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.from(dto.getEndTime().with(TemporalAdjusters.lastDayOfMonth())), LocalTime.MAX);
        // 查询目标销量
        Map<Integer, Integer> quarterTargetMap = new HashMap<>(4);
        // 统计目标销量
        BiTargetManagementEntity target = biTargetManagementService.getMonthSales(start, end, dto.getPermissionSql(), 1);
        if (ObjectUtil.isNull(target)) {
            return getMonthVolumeResultList(quarterTargetMap, new HashMap<>(4), start.getYear());
        }
        // 12 个月记录
        quarterTargetMap.put(1, target.getJanuary().intValue());
        quarterTargetMap.put(2, target.getFebruary().intValue());
        quarterTargetMap.put(3, target.getMarch().intValue());
        quarterTargetMap.put(4, target.getApril().intValue());
        quarterTargetMap.put(5, target.getMay().intValue());
        quarterTargetMap.put(6, target.getJune().intValue());
        quarterTargetMap.put(7, target.getJuly().intValue());
        quarterTargetMap.put(8, target.getAugust().intValue());
        quarterTargetMap.put(9, target.getSeptember().intValue());
        quarterTargetMap.put(10, target.getOctober().intValue());
        quarterTargetMap.put(11, target.getNovember().intValue());
        quarterTargetMap.put(12, target.getDecember().intValue());
        // 统计目标销量
        // 查询销售额
        QueryWrapper<BiOrderInfoEntity> qw = new QueryWrapper<>();
        boolean flag = TimeTypeEnum.ORDER_TIME.getCode() == dto.getTimeType();
        qw.select("id", PLATFORM_CREATE_TIME, DELIVERY_TIME);
        List<BiOrderInfoEntity> entityList = getOrderInfoEntities(dto, qw, start, end, null);
        if (CollectionUtils.isEmpty(entityList)) {
            return getMonthVolumeResultList(quarterTargetMap, new HashMap<>(4), start.getYear());
        }
        List<String> orderIds = entityList.stream().map(BiOrderInfoEntity::getId).collect(Collectors.toList());
        List<BiOrderItemSplitEntity> entityItemList = biOrderItemSplitService.listByOrderInfoIds(orderIds);
        if (CollectionUtils.isEmpty(entityItemList)) {
            return getMonthVolumeResultList(quarterTargetMap, new HashMap<>(4), start.getYear());
        }
        // 根据订单号的分组计算销量
        Map<String, Integer> orderQuantityMap = entityItemList.stream().collect(Collectors.groupingBy(BiOrderItemSplitEntity::getOrderId,
                Collectors.summingInt(BiOrderItemSplitEntity::getQuantity)));
        // 对订单号进行月度分组
        Map<Integer, Integer> quarterMap = entityList.stream().collect(Collectors.groupingBy(x ->
                        // 按照季度分组
                        (flag ? x.getPlatformCreateTime() : x.getDeliveryTime()).getMonthValue(),
                Collectors.summingInt(x -> orderQuantityMap.getOrDefault(x.getId(), 0)))
        );
        // 计算完成率
        return getMonthVolumeResultList(quarterTargetMap, quarterMap, dto.getStartTime().getYear());
    }

    @Override
    public List<SalesCompletionInfoVO> sumPlatformSalesCompletion(BiFilterDTO dto) {
        // 获取月度开始时间和结束时间
        LocalDateTime start = LocalDateTime.of(LocalDate.from(dto.getStartTime().with(TemporalAdjusters.firstDayOfMonth())), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.from(dto.getEndTime().with(TemporalAdjusters.lastDayOfMonth())), LocalTime.MAX);
        // 查询目标数据
        List<BiTargetManagementEntity> targetList = biTargetManagementService.getSales(start, end, dto.getPermissionSql());
        if (CollUtil.isEmpty(targetList)) {
            return new ArrayList<>();
        }
        Map<Integer, List<BiTargetManagementEntity>> salesTypeMap = targetList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getTargetType));
        // 查询目标销售额
        List<BiTargetManagementEntity> salesList = salesTypeMap.get(1);
        Map<String, BigDecimal> targetSalesMap = new HashMap<>();
        if (CollUtil.isNotEmpty(salesList)) {
            targetSalesMap = salesList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getPlatformName,
                    MathUtil.summingBigDecimal(BiTargetManagementEntity::getJanuary)));
        }

        // 查询目标销量
        List<BiTargetManagementEntity> salesVolumeList = salesTypeMap.get(0);
        Map<String, Integer> targetSalesVolumeMap = new HashMap<>();
        if (CollUtil.isNotEmpty(salesVolumeList)) {
            targetSalesVolumeMap = salesVolumeList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getPlatformName,
                    Collectors.summingInt(x -> x.getJanuary().intValue())));
        }
        // 查询实际销售额
        QueryWrapper<BiOrderInfoEntity> qw = new QueryWrapper<>();
        List<BiOrderInfoEntity> orderInfoEntities = getOrderInfoEntities(dto, qw, start, end, null);
        if (CollectionUtils.isEmpty(orderInfoEntities)) {
            return new ArrayList<>();
        }
        Map<String, BigDecimal> saleAmountMap = orderInfoEntities.stream()
                .collect(Collectors.groupingBy(BiOrderInfoEntity::getSourcePlatform,
                        Collectors.reducing(BigDecimal.ZERO, x -> x.getOrderFee().multiply(x.getCurrencyRate()), BigDecimal::add)));
        // 查询实际销量
        List<String> orderIds = orderInfoEntities.stream().map(BiOrderInfoEntity::getId).collect(Collectors.toList());
        List<BiOrderItemSplitEntity> entityItemList = biOrderItemSplitService.listByOrderInfoIds(orderIds);
        if (CollectionUtils.isEmpty(entityItemList)) {
            return new ArrayList<>();
        }
        // 根据订单号的分组计算销量
        Map<String, Integer> orderQuantityMap = entityItemList.stream().collect(Collectors.groupingBy(BiOrderItemSplitEntity::getOrderId,
                Collectors.summingInt(BiOrderItemSplitEntity::getQuantity)));
        // 对订单号进行平台分组
        Map<String, Integer> salesVolumeMap = orderInfoEntities.stream().collect(Collectors.groupingBy(BiOrderInfoEntity::getSourcePlatform,
                Collectors.summingInt(x -> orderQuantityMap.getOrDefault(x.getId(), 0))));

        return assemblyResult(dto, targetSalesMap, targetSalesVolumeMap, null, salesVolumeMap, saleAmountMap);
    }

    @Override
    public List<SalesCompletionInfoVO> sumCategorySalesCompletion(BiFilterDTO dto) {
        // 获取月度开始时间和结束时间
        LocalDateTime start = LocalDateTime.of(LocalDate.from(dto.getStartTime().with(TemporalAdjusters.firstDayOfMonth())), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.from(dto.getEndTime().with(TemporalAdjusters.lastDayOfMonth())), LocalTime.MAX);
        // 查询目标数据
        List<BiTargetManagementEntity> targetList = biTargetManagementService.getSales(start, end, dto.getPermissionSql());
        if (CollUtil.isEmpty(targetList)) {
            return new ArrayList<>();
        }
        Map<Integer, List<BiTargetManagementEntity>> salesTypeMap = targetList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getTargetType));
        // 查询目标销售额
        List<BiTargetManagementEntity> salesList = salesTypeMap.get(1);
        Map<String, BigDecimal> targetSalesMap = new HashMap<>();
        if (CollUtil.isNotEmpty(salesList)) {
            targetSalesMap = salesList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getCategory,
                    MathUtil.summingBigDecimal(BiTargetManagementEntity::getJanuary)));
        }

        // 查询目标销量
        List<BiTargetManagementEntity> salesVolumeList = salesTypeMap.get(0);
        Map<String, Integer> targetSalesVolumeMap = new HashMap<>();
        if (CollUtil.isNotEmpty(salesVolumeList)) {
            targetSalesVolumeMap = salesVolumeList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getCategory,
                    Collectors.summingInt(x -> x.getJanuary().intValue())));
        }
        // 查询实际销售额
        QueryWrapper<BiOrderInfoEntity> qw = new QueryWrapper<>();
        List<BiOrderInfoEntity> orderInfoEntities = getOrderInfoEntities(dto, qw, start, end, null);
        if (CollectionUtils.isEmpty(orderInfoEntities)) {
            return new ArrayList<>();
        }

        // 查询实际销量
        List<String> orderIds = orderInfoEntities.stream().map(BiOrderInfoEntity::getId).collect(Collectors.toList());
        List<BiOrderItemSplitEntity> entityItemList = biOrderItemSplitService.listByOrderInfoIds(orderIds);
        if (CollectionUtils.isEmpty(entityItemList)) {
            return new ArrayList<>();
        }
        // 根据订单号的分组计算销量
        Map<String, Integer> salesVolumeMap = entityItemList.stream().collect(Collectors.groupingBy(BiOrderItemSplitEntity::getCategoryName,
                Collectors.summingInt(BiOrderItemSplitEntity::getQuantity)));
        // 对订单号进行平台分组
        Map<String, BigDecimal> rateMap = orderInfoEntities.stream().collect(Collectors.toMap(BiOrderInfoEntity::getId, BiOrderInfoEntity::getCurrencyRate));
        Map<String, BigDecimal> saleAmountMap = entityItemList.stream().collect(Collectors.groupingBy(BiOrderItemSplitEntity::getCategoryName,
                Collectors.reducing(BigDecimal.ZERO,
                        x -> new BigDecimal(x.getQuantity()).multiply(null == x.getSellPrice() ? BigDecimal.ZERO : x.getSellPrice()).multiply(rateMap.getOrDefault(x.getOrderId(), BigDecimal.ZERO)),
                        BigDecimal::add)
        ));

        return assemblyResult(dto, targetSalesMap, targetSalesVolumeMap, null, salesVolumeMap, saleAmountMap);
    }

    @Override
    public List<SalesCompletionInfoVO> sumNewProductSalesCompletion(BiFilterDTO dto, Integer newSign) {
        // 获取月度开始时间和结束时间
        LocalDateTime start = LocalDateTime.of(LocalDate.from(dto.getStartTime().with(TemporalAdjusters.firstDayOfMonth())), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.from(dto.getEndTime().with(TemporalAdjusters.lastDayOfMonth())), LocalTime.MAX);
        // 查询目标数据
        List<BiTargetManagementEntity> targetList = biTargetManagementService.getSales(start, end, dto.getPermissionSql());
        if (CollUtil.isEmpty(targetList)) {
            return new ArrayList<>();
        }
        Map<Integer, List<BiTargetManagementEntity>> salesTypeMap = targetList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getTargetType));
        // 查询目标销售额
        List<BiTargetManagementEntity> salesList = salesTypeMap.get(1);
        Map<String, BigDecimal> targetSalesMap = new HashMap<>();
        if (CollUtil.isNotEmpty(salesList)) {
            targetSalesMap = salesList.stream().filter(x -> null == newSign || newSign.equals(x.getProductType())).collect(Collectors.groupingBy(BiTargetManagementEntity::getSkuNo,
                    MathUtil.summingBigDecimal(BiTargetManagementEntity::getJanuary)));
        }

        // 查询目标销量
        List<BiTargetManagementEntity> salesVolumeList = salesTypeMap.get(0);
        Map<String, Integer> targetSalesVolumeMap = new HashMap<>();
        if (CollUtil.isNotEmpty(salesVolumeList)) {
            targetSalesVolumeMap = salesVolumeList.stream().filter(x -> null == newSign || newSign.equals(x.getProductType())).collect(Collectors.groupingBy(BiTargetManagementEntity::getSkuNo,
                    Collectors.summingInt(x -> x.getJanuary().intValue())));
        }

        // 查询实际销售额
        QueryWrapper<BiOrderInfoEntity> qw = new QueryWrapper<>();
        List<BiOrderInfoEntity> orderInfoEntities = getOrderInfoEntities(dto, qw, start, end, null);
        if (CollectionUtils.isEmpty(orderInfoEntities)) {
            return new ArrayList<>();
        }

        // 查询实际销量
        List<String> orderIds = orderInfoEntities.stream().map(BiOrderInfoEntity::getId).collect(Collectors.toList());
        List<BiOrderItemSplitEntity> entityItemList = biOrderItemSplitService.listByConditions(orderIds, newSign, new ArrayList<>());
        if (CollectionUtils.isEmpty(entityItemList)) {
            return new ArrayList<>();
        }
        // 根据sku的分组计算销量
        Map<String, Integer> salesVolumeMap = entityItemList.stream().filter(x -> StringUtils.isNotBlank(x.getSkuNo()))
                .collect(Collectors.groupingBy(BiOrderItemSplitEntity::getSkuNo,
                        Collectors.summingInt(BiOrderItemSplitEntity::getQuantity)));
        // 汇率map
        Map<String, BigDecimal> rateMap = orderInfoEntities.stream().collect(Collectors.toMap(BiOrderInfoEntity::getId, BiOrderInfoEntity::getCurrencyRate));
        // 根据sku的分组计算销售额
        Map<String, BigDecimal> saleAmountMap = entityItemList.stream()
                .filter(x -> StringUtils.isNotBlank(x.getSkuNo()))
                .collect(Collectors.groupingBy(BiOrderItemSplitEntity::getSkuNo,
                        Collectors.reducing(BigDecimal.ZERO,
                                x -> new BigDecimal(x.getQuantity()).multiply(null == x.getSellPrice() ? BigDecimal.ZERO : x.getSellPrice()).multiply(rateMap.getOrDefault(x.getOrderId(), BigDecimal.ZERO)),
                                BigDecimal::add)
                ));

        //  sku 品名Map
        Map<String, String> skuMap = entityItemList.stream().filter(x -> StringUtils.isNotBlank(x.getSkuNo())).collect(Collectors.groupingBy(BiOrderItemSplitEntity::getSkuNo,
                Collectors.collectingAndThen(Collectors.toList(), v -> v.get(0).getItemName())));

        return assemblyResult(dto, targetSalesMap, targetSalesVolumeMap, skuMap, salesVolumeMap, saleAmountMap);
    }

    @Override
    public List<SalesCompletionInfoVO> sumProductPositionSalesCompletion(BiFilterDTO dto) {
        // 获取月度开始时间和结束时间
        LocalDateTime start = LocalDateTime.of(LocalDate.from(dto.getStartTime().with(TemporalAdjusters.firstDayOfMonth())), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.from(dto.getEndTime().with(TemporalAdjusters.lastDayOfMonth())), LocalTime.MAX);
        List<BiTargetManagementEntity> targetList = biTargetManagementService.getSales(start, end, dto.getPermissionSql());
        if (CollUtil.isEmpty(targetList)) {
            return new ArrayList<>();
        }
        Map<Integer, List<BiTargetManagementEntity>> salesTypeMap = targetList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getTargetType));
        // 查询目标销售额
        List<BiTargetManagementEntity> salesList = salesTypeMap.get(1);
        Map<String, BigDecimal> targetSalesMap = new HashMap<>();
        if (CollUtil.isNotEmpty(salesList)) {
            targetSalesMap = salesList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getProductPosition,
                    MathUtil.summingBigDecimal(BiTargetManagementEntity::getJanuary)));
        }

        // 查询目标销量
        List<BiTargetManagementEntity> salesVolumeList = salesTypeMap.get(0);
        Map<String, Integer> targetSalesVolumeMap = new HashMap<>();
        if (CollUtil.isNotEmpty(salesVolumeList)) {
            targetSalesVolumeMap = salesVolumeList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getProductPosition,
                    Collectors.summingInt(x -> x.getJanuary().intValue())));
        }
        // 查询实际销售额
        QueryWrapper<BiOrderInfoEntity> qw = new QueryWrapper<>();
        List<BiOrderInfoEntity> orderInfoEntities = getOrderInfoEntities(dto, qw, start, end, null);
        if (CollectionUtils.isEmpty(orderInfoEntities)) {
            return new ArrayList<>();
        }

        // 查询实际销量
        List<String> orderIds = orderInfoEntities.stream().map(BiOrderInfoEntity::getId).collect(Collectors.toList());
        List<String> sku = targetList.stream().map(BiTargetManagementEntity::getSkuNo).collect(Collectors.toList());
        List<BiOrderItemSplitEntity> entityItemList = biOrderItemSplitService.listByConditions(orderIds, null, sku);
        if (CollectionUtils.isEmpty(entityItemList)) {
            return new ArrayList<>();
        }
        // 根据sku的分组计算销量
        Map<String, Integer> skuSalesVolumeMap = entityItemList.stream()
                .collect(Collectors.groupingBy(BiOrderItemSplitEntity::getSkuNo,
                        Collectors.summingInt(BiOrderItemSplitEntity::getQuantity)));
        // 产品定位销量map
        Map<String, Integer> salesVolumeMap = targetList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getProductPosition,
                Collectors.summingInt(x -> skuSalesVolumeMap.getOrDefault(x.getSkuNo(), 0))));

        // 汇率map
        Map<String, BigDecimal> rateMap = orderInfoEntities.stream()
                .collect(Collectors.toMap(BiOrderInfoEntity::getId, BiOrderInfoEntity::getCurrencyRate));

        // 根据sku的分组计算销售额
        Map<String, BigDecimal> skuSaleAmountMap = entityItemList.stream()
                .collect(Collectors.groupingBy(BiOrderItemSplitEntity::getSkuNo,
                        Collectors.reducing(BigDecimal.ZERO,
                                x -> new BigDecimal(x.getQuantity())
                                        .multiply(null == x.getSellPrice() ? BigDecimal.ZERO : x.getSellPrice())
                                        .multiply(rateMap.getOrDefault(x.getOrderId(), BigDecimal.ZERO)),
                                BigDecimal::add)
                ));
        // 产品定位销售额map
        Map<String, BigDecimal> saleAmountMap = targetList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getProductPosition,
                MathUtil.summingBigDecimal(x -> skuSaleAmountMap.getOrDefault(x.getSkuNo(), BigDecimal.ZERO))));

        return assemblyResult(dto, targetSalesMap, targetSalesVolumeMap, null, salesVolumeMap, saleAmountMap);
    }

    @Override
    public List<SalesCompletionInfoVO> sumProductTypeCompletion(BiFilterDTO dto) {
        // 获取月度开始时间和结束时间
        LocalDateTime start = LocalDateTime.of(LocalDate.from(dto.getStartTime().with(TemporalAdjusters.firstDayOfMonth())), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.from(dto.getEndTime().with(TemporalAdjusters.lastDayOfMonth())), LocalTime.MAX);
        List<BiTargetManagementEntity> targetList = biTargetManagementService.getSales(start, end, dto.getPermissionSql());
        if (CollUtil.isEmpty(targetList)) {
            return new ArrayList<>();
        }
        Map<Integer, List<BiTargetManagementEntity>> salesTypeMap = targetList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getTargetType));
        // 查询目标销售额
        List<BiTargetManagementEntity> salesList = salesTypeMap.get(1);
        Map<String, BigDecimal> targetSalesMap = salesList.stream().collect(Collectors.groupingBy(x -> x.getProductType().toString(),
                MathUtil.summingBigDecimal(BiTargetManagementEntity::getJanuary)));
        // 查询目标销量
        List<BiTargetManagementEntity> salesVolumeList = salesTypeMap.get(0);
        Map<String, Integer> targetSalesVolumeMap = salesVolumeList.stream().collect(Collectors.groupingBy(x -> x.getProductType().toString(),
                Collectors.summingInt(x -> x.getJanuary().intValue())));

        // 查询实际销售额
        QueryWrapper<BiOrderInfoEntity> qw = new QueryWrapper<>();
        List<BiOrderInfoEntity> orderInfoEntities = getOrderInfoEntities(dto, qw, start, end, null);
        if (CollectionUtils.isEmpty(orderInfoEntities)) {
            return new ArrayList<>();
        }

        // 查询实际销量
        List<String> orderIds = orderInfoEntities.stream().map(BiOrderInfoEntity::getId).collect(Collectors.toList());
        List<String> sku = targetList.stream().map(BiTargetManagementEntity::getSkuNo).collect(Collectors.toList());
        List<BiOrderItemSplitEntity> entityItemList = biOrderItemSplitService.listByConditions(orderIds, null, sku);
        if (CollectionUtils.isEmpty(entityItemList)) {
            return new ArrayList<>();
        }

        // 根据sku的分组计算销量
        Map<String, Integer> skuSalesVolumeMap = entityItemList.stream()
                .collect(Collectors.groupingBy(BiOrderItemSplitEntity::getSkuNo,
                        Collectors.summingInt(BiOrderItemSplitEntity::getQuantity)));
        // 产品定位销量map
        Map<String, Integer> salesVolumeMap = targetList.stream().collect(Collectors.groupingBy(x -> x.getProductType().toString(),
                Collectors.summingInt(x -> skuSalesVolumeMap.getOrDefault(x.getSkuNo(), 0))));

        // 汇率map
        Map<String, BigDecimal> rateMap = orderInfoEntities.stream()
                .collect(Collectors.toMap(BiOrderInfoEntity::getId, BiOrderInfoEntity::getCurrencyRate));

        // 根据sku的分组计算销售额
        Map<String, BigDecimal> skuSaleAmountMap = entityItemList.stream()
                .collect(Collectors.groupingBy(BiOrderItemSplitEntity::getSkuNo,
                        Collectors.reducing(BigDecimal.ZERO,
                                x -> new BigDecimal(x.getQuantity())
                                        .multiply(null == x.getSellPrice() ? BigDecimal.ZERO : x.getSellPrice())
                                        .multiply(rateMap.getOrDefault(x.getOrderId(), BigDecimal.ZERO)),
                                BigDecimal::add)
                ));
        // 产品类型销售额map
        Map<String, BigDecimal> saleAmountMap = targetList.stream().collect(Collectors.groupingBy(x -> x.getProductType().toString(),
                MathUtil.summingBigDecimal(x -> skuSaleAmountMap.getOrDefault(x.getSkuNo(), BigDecimal.ZERO))));

        return assemblyResult(dto, targetSalesMap, targetSalesVolumeMap, null, salesVolumeMap, saleAmountMap);
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:getSalesAndYoy", keyGenerator = "myKeyGenerator")
    public TargetSaleAndYoySumVO getSalesAndYoy(BiFilterDTO dto) {
        // 查询当期销售额
        TargetSaleSumVO currentVo = sumSales(dto);
        BigDecimal currentAmount = currentVo.getValue();
        if (BigDecimal.ZERO.compareTo(currentAmount) == 0) {
            return new TargetSaleAndYoySumVO();
        }
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        // 查询上一个周期销售额 环比
        Duration duration = Duration.between(startTime, endTime);
        LocalDateTime preStartTime = startTime.minusDays(duration.toDays());
        dto.setStartTime(preStartTime);
        dto.setEndTime(startTime.minusDays(1));
        TargetSaleSumVO ringVo = sumSales(dto);
        // 查询去年同周期 同比
        dto.setStartTime(startTime.minusYears(1));
        dto.setEndTime(endTime.minusYears(1).minusDays(1));
        TargetSaleSumVO yoyVo = sumSales(dto);

        return new TargetSaleAndYoySumVO(currentVo, ringVo, yoyVo);
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:countSalesVolumeAndYoy", keyGenerator = "myKeyGenerator")
    public TargetSaleAndYoyCountVO countSalesVolumeAndYoy(BiFilterDTO dto) {
        // 查询当期销售额
        TargetSaleCountVO currentVo = countSalesVolume(dto);
        Integer currentAmount = currentVo.getValue();
        if (0 == currentAmount) {
            return new TargetSaleAndYoyCountVO();
        }
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        // 查询上一个周期销售额 环比
        Duration duration = Duration.between(startTime, endTime);
        LocalDateTime preStartTime = startTime.minusDays(duration.toDays());
        dto.setStartTime(preStartTime);
        dto.setEndTime(startTime.minusDays(1));
        TargetSaleCountVO ringVo = countSalesVolume(dto);
        // 查询去年同周期 同比
        dto.setStartTime(startTime.minusYears(1));
        dto.setEndTime(endTime.minusYears(1).minusDays(1));
        TargetSaleCountVO yoyVo = countSalesVolume(dto);

        return new TargetSaleAndYoyCountVO(currentVo, ringVo, yoyVo);
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:countOrderQuantityAndYoy", keyGenerator = "myKeyGenerator")
    public TargetSaleAndYoyCountVO countOrderQuantityAndYoy(BiFilterDTO dto) {
        // 查询当期销售额
        TargetSaleCountVO currentVo = countOrderQuantity(dto);
        Integer currentAmount = currentVo.getValue();
        if (0 == currentAmount) {
            return new TargetSaleAndYoyCountVO();
        }
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        // 查询上一个周期销售额 环比
        Duration duration = Duration.between(startTime, endTime);
        LocalDateTime preStartTime = startTime.minusDays(duration.toDays());
        dto.setStartTime(preStartTime);
        dto.setEndTime(startTime.minusDays(1));
        TargetSaleCountVO ringVo = countOrderQuantity(dto);
        // 查询去年同周期 同比
        dto.setStartTime(startTime.minusYears(1));
        dto.setEndTime(endTime.minusYears(1).minusDays(1));
        TargetSaleCountVO yoyVo = countOrderQuantity(dto);

        return new TargetSaleAndYoyCountVO(currentVo, ringVo, yoyVo);
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:countRefundRateAndYoy", keyGenerator = "myKeyGenerator")
    public TargetSaleAndYoySumVO countRefundRateAndYoy(BiFilterDTO dto) {
        // 查询当期销售额
        dto.setEndTime(dto.getEndTime().plusMinutes(1));
        TargetSaleSumVO currentVo = countRefundRate(dto);
        BigDecimal currentAmount = currentVo.getValue();
        if (BigDecimal.ZERO.compareTo(currentAmount) == 0) {
            return new TargetSaleAndYoySumVO();
        }
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        // 查询上一个周期销售额 环比
        Duration duration = Duration.between(startTime, endTime);
        LocalDateTime preStartTime = startTime.minusDays(duration.toDays());
        dto.setStartTime(preStartTime);
        dto.setEndTime(startTime.minusDays(1));
        TargetSaleSumVO ringVo = countRefundRate(dto);
        // 查询去年同周期 同比
        dto.setStartTime(startTime.minusYears(1));
        dto.setEndTime(endTime.minusYears(1));
        TargetSaleSumVO yoyVo = countRefundRate(dto);

        return new TargetSaleAndYoySumVO(currentVo, ringVo, yoyVo);
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:countRefundAmountAndYoy", keyGenerator = "myKeyGenerator")
    public TargetSaleAndYoySumVO countRefundAmountAndYoy(BiFilterDTO dto) {
        // 查询当期销售额
        dto.setEndTime(dto.getEndTime().plusMinutes(1));
        TargetSaleSumVO currentVo = countRefundAmount(dto);
        BigDecimal currentAmount = currentVo.getValue();
        if (BigDecimal.ZERO.compareTo(currentAmount) == 0) {
            return new TargetSaleAndYoySumVO();
        }
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        // 查询上一个周期销售额 环比
        Duration duration = Duration.between(startTime, endTime);
        LocalDateTime preStartTime = startTime.minusDays(duration.toDays());
        dto.setStartTime(preStartTime);
        dto.setEndTime(startTime.minusDays(1));
        TargetSaleSumVO ringVo = countRefundAmount(dto);
        // 查询去年同周期 同比
        dto.setStartTime(startTime.minusYears(1));
        dto.setEndTime(endTime.minusYears(1));
        TargetSaleSumVO yoyVo = countRefundAmount(dto);

        return new TargetSaleAndYoySumVO(currentVo, ringVo, yoyVo);
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:countRefundOrderNumAndYoy", keyGenerator = "myKeyGenerator")
    public TargetSaleAndYoyCountVO countRefundOrderNumAndYoy(BiFilterDTO dto) {
        // 查询当期销售额
        dto.setEndTime(dto.getEndTime().plusMinutes(1));
        TargetSaleCountVO currentVo = countRefundOrderNum(dto);
        Integer currentAmount = currentVo.getValue();
        if (0 == currentAmount) {
            return new TargetSaleAndYoyCountVO();
        }
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        // 查询上一个周期销售额 环比
        Duration duration = Duration.between(startTime, endTime);
        LocalDateTime preStartTime = startTime.minusDays(duration.toDays());
        dto.setStartTime(preStartTime);
        dto.setEndTime(startTime.minusDays(1));
        TargetSaleCountVO ringVo = countRefundOrderNum(dto);
        // 查询去年同周期 同比
        dto.setStartTime(startTime.minusYears(1));
        dto.setEndTime(endTime.minusYears(1).minusDays(1));
        TargetSaleCountVO yoyVo = countRefundOrderNum(dto);

        return new TargetSaleAndYoyCountVO(currentVo, ringVo, yoyVo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean importOrderFile(MultipartFile excelFile, Integer importType, HttpServletResponse response) {
        List<SysDepartmentDTO> deptList = sysUserFeign.getDeptList();
        DmpOrderInfoExcelListener excelListenerUtil = new DmpOrderInfoExcelListener(deptList, plmTaskFeign, biShopInfoService, sysUserFeign);
        try {
            read(excelFile.getInputStream(), DmpOrderInfoImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();

            //验证导入数据是否为空
            List<DmpOrderInfoImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
            if (CollectionUtils.isEmpty(excelDateList)) {
                throw new ServiceException(ApiError.ERROR_95123);
            }

            //成功数据
            List<DmpOrderInfoImportExcelDTO> successList = excelListenerUtil.getSuccessList();

            //错误数据
            List<DmpOrderInfoImportExcelDTO> errorList = excelListenerUtil.getErrorList();

            //处理重复SKU
            doOpHandleOrderInfo(successList, errorList);

            if (!errorList.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                String excelPath = "excel/dmpOrderInfo.xlsx";
                String name = "dmpOrderInfo";
                String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
                sb.append(date);
                sb.append(name);
                new ExcelPrintUtils().patchExport(errorList, response, sb.toString(), excelPath);
                return false;
            }
        } catch (IOException e) {
            throw new ServiceException(ApiError.Default);
        }
        return true;
    }

    /**
     * @param successList
     * @param errorList
     * @description: 基础验证数据再次进行逻辑验证
     * @author Will
     * @date: 2023/7/5 17:31
     */
    private void doOpHandleOrderInfo(List<DmpOrderInfoImportExcelDTO> successList, List<DmpOrderInfoImportExcelDTO> errorList) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        List<DmpOrderInfoImportExcelDTO> removeList = new ArrayList<>();
        List<String> platformOrderIds = successList.stream().map(DmpOrderInfoImportExcelDTO::getPlatformOrderId).collect(Collectors.toList());

        List<BiOrderInfoEntity> list = this.lambdaQuery().in(BiOrderInfoEntity::getPlatformOrderId, platformOrderIds).list();

        for (DmpOrderInfoImportExcelDTO addDTO : successList) {

            BiOrderInfoEntity biOrderInfoEntity = list.stream().filter(obj -> obj.getPlatformOrderId().equals(addDTO.getPlatformOrderId())).findFirst().orElse(null);

            //存在错误信息则
            if (ObjectUtils.isNotEmpty(biOrderInfoEntity)) {
                addDTO.setErrorMsg("1、订单号已存在，不能重复添加");
                errorList.add(addDTO);
                removeList.add(addDTO);
                continue;
            }
        }
        if (CollectionUtils.isNotEmpty(removeList)) {
            successList.removeAll(removeList);
        }
        if (CollectionUtils.isNotEmpty(successList)) {
            Map<String, List<DmpOrderInfoImportExcelDTO>> map = successList.stream().collect(Collectors.groupingBy(DmpOrderInfoImportExcelDTO::getPlatformOrderId));
            //主表信息
            List<BiOrderInfoEntity> infoList = new ArrayList<>();
            //明细信息
            List<BiOrderItemSplitEntity> itemList = new ArrayList<>();

            orderItemSplitHandler(map, infoList, itemList);
            //新增主表信息
            if (CollectionUtils.isNotEmpty(infoList)) {
                this.saveBatch(infoList);
            }
            //新增明细信息
            if (CollectionUtils.isNotEmpty(itemList)) {
                biOrderItemSplitService.saveBatch(itemList);
            }
        }
    }

    private void orderItemSplitHandler(Map<String, List<DmpOrderInfoImportExcelDTO>> map, List<BiOrderInfoEntity> infoList, List<BiOrderItemSplitEntity> itemList) {
        for (Map.Entry<String, List<DmpOrderInfoImportExcelDTO>> entry : map.entrySet()) {
            BiOrderInfoEntity info = new BiOrderInfoEntity();
            List<DmpOrderInfoImportExcelDTO> value = entry.getValue();
            DmpOrderInfoImportExcelDTO mainEntity = value.get(0);
            BeanUtils.copyProperties(mainEntity, info);
            info.setOrderStatus(OrderStateEnum.getCodeByName(mainEntity.getOrderStateName()));
            BaseSearchDTO baseSearchDTO = new BaseSearchDTO();
            baseSearchDTO.setSearchKeyword(mainEntity.getChargeName());
            ApiResult<List<FindUserDTO>> listApiResult = sysUserFeign.userList(baseSearchDTO);
            List<FindUserDTO> chargeNameList = listApiResult.getData();
            info.setChargeId(chargeNameList.get(0).getUserId());

            String platformCreateTimeStr = mainEntity.getPlatformCreateTimeStr();
            if (StringUtils.isNotBlank(platformCreateTimeStr)) {
                info.setPlatformCreateTime(LocalDateUtil.stringToLocalDateTime(platformCreateTimeStr));
            }
            String deliveryTimeStr = mainEntity.getDeliveryTimeStr();
            if (StringUtils.isNotBlank(deliveryTimeStr)) {
                info.setDeliveryTime(LocalDateUtil.stringToLocalDateTime(deliveryTimeStr));
            }
            BigDecimal orderFee = mainEntity.getOrderFee();
            mainEntity.setOrderFee(MathUtil.multiply(orderFee, ObjectUtils.isEmpty(mainEntity.getCurrencyRate()) ? MathUtil.BigDecimal_1 : mainEntity.getCurrencyRate()));

            info.setId(IdWorker.getIdStr());
            infoList.add(info);
            for (DmpOrderInfoImportExcelDTO excelDTO : value) {
                BiOrderItemSplitEntity item = new BiOrderItemSplitEntity();
                item.setOrderId(info.getId());
                item.setSkuNo(excelDTO.getSkuNo());
                item.setItemName(excelDTO.getItemName());
                item.setSellPriceOrigin(excelDTO.getSellPriceOrigin());
                item.setQuantity(excelDTO.getQuantity());
                itemList.add(item);
            }
        }
    }

    @Override
    public Map<Integer, BigDecimal> statisticsSalesByDate(BiFilterDTO dto, Integer type) {
        // 查询销售额
        List<BiOrderInfoEntity> list = lambdaQuery()
                .in(CollUtil.isNotEmpty(dto.getDepartment()), BiOrderInfoEntity::getDeptId, dto.getDepartment())
                .in(CollUtil.isNotEmpty(dto.getPlatform()), BiOrderInfoEntity::getSourcePlatform, dto.getPlatform())
                .in(CollUtil.isNotEmpty(dto.getSite()), BiOrderInfoEntity::getSite, dto.getSite())
                .in(CollUtil.isNotEmpty(dto.getShopName()), BiOrderInfoEntity::getShopName, dto.getShopName())
                .in(CollUtil.isNotEmpty(dto.getUserId()), BiOrderInfoEntity::getChargeId, dto.getUserId())
                .ge(2 != type && ObjectUtil.isNotEmpty(dto.getStartTime()), BiOrderInfoEntity::getPlatformCreateTime, dto.getStartTime())
                .le(2 != type && ObjectUtil.isNotEmpty(dto.getEndTime()), BiOrderInfoEntity::getPlatformCreateTime, dto.getEndTime())
                .list();
        Map<Integer, BigDecimal> resultMap;
        if (0 == type) {
            // 月份
            resultMap = list.stream().filter(x -> ObjectUtil.isNotEmpty(x.getPlatformCreateTime())).collect(Collectors.groupingBy(x -> x.getPlatformCreateTime().getMonthValue(),
                    MathUtil.summingBigDecimal(x -> x.getOrderFee().multiply(x.getCurrencyRate()).setScale(4, BigDecimal.ROUND_DOWN))));
        } else if (1 == type) {
            // 季度
            resultMap = list.stream().filter(x -> ObjectUtil.isNotEmpty(x.getPlatformCreateTime())).collect(Collectors.groupingBy(x -> (x.getPlatformCreateTime().getMonthValue() - 1) / 3 + 1,
                    MathUtil.summingBigDecimal(x -> x.getOrderFee().multiply(x.getCurrencyRate()).setScale(4, BigDecimal.ROUND_DOWN))));
        } else {
            // 年度
            resultMap = list.stream().filter(x -> ObjectUtil.isNotEmpty(x.getPlatformCreateTime())).collect(Collectors.groupingBy(x -> x.getPlatformCreateTime().getYear(),
                    MathUtil.summingBigDecimal(x -> x.getOrderFee().multiply(x.getCurrencyRate()).setScale(4, BigDecimal.ROUND_DOWN))));
        }
        return resultMap;
    }

    @Override
    public Map<String, BigDecimal> statisticsSalesByCondition(BiFilterDTO dto, String groupName) {
        List<DimensionSalesVO> list = this.sumSalesByCondition(dto, groupName);
        if (CollUtil.isEmpty(list)) {
            return new HashMap<>();
        }
        return list.stream()
                .collect(Collectors.toMap(DimensionSalesVO::getDimension,
                        DimensionSalesVO::getSalesAmount));
    }

    @Override
    public List<DimensionSalesVO> sumSalesByCondition(BiFilterDTO dto, String groupName) {
        return baseMapper.sumByDeptAndCostType(dto, groupName);
    }


    private static List<SalesCompletionInfoVO> assemblyResult(BiFilterDTO dto, Map<String, BigDecimal> targetSalesMap, Map<String, Integer> targetSalesVolumeMap,
                                                              Map<String, String> skuMap, Map<String, Integer> salesVolumeMap, Map<String, BigDecimal> saleAmountMap) {
        // 组合数据计算目标达成率
        Set<String> keys = new HashSet<>();
        keys.addAll(targetSalesMap.keySet());
        keys.addAll(targetSalesVolumeMap.keySet());
        List<SalesCompletionInfoVO> resultList = keys
                .stream().map(x ->
                        new SalesCompletionInfoVO(x, targetSalesMap.getOrDefault(x, BigDecimal.ZERO), targetSalesVolumeMap.getOrDefault(x, 0),
                                saleAmountMap.getOrDefault(x, BigDecimal.ZERO), salesVolumeMap.getOrDefault(x, 0), null != skuMap ? skuMap.getOrDefault(x, "") : "")
                ).collect(Collectors.toList());

        AtomicInteger rankIndex = new AtomicInteger(1);
        return resultList.stream()
                .sorted(Comparator.comparing(SalesCompletionInfoVO::getSalesAmountCompletionRate).reversed()
                        .thenComparing(SalesCompletionInfoVO::getSalesVolumeCompletionRate).reversed())
                .peek(x -> x.setRanking(rankIndex.getAndIncrement()))
                .filter(x -> x.getRanking() <= dto.getRankNum())
                .collect(Collectors.toList());
    }

    /**
     * 季度销量结构构建
     *
     * @param quarterTargetMap
     * @param quarterMap
     * @param year
     * @return
     */
    private TargetAnalysisVO<QuarterMonthSalesVolumeVO> getQuarterVolumeResultList(Map<Integer, Integer> quarterTargetMap, Map<Integer, Integer> quarterMap, int year) {
        ArrayList<QuarterMonthSalesVolumeVO> resultList = new ArrayList<>();
        // 年度销售额
        QuarterMonthSalesVolumeVO yearSales = new QuarterMonthSalesVolumeVO(quarterTargetMap, quarterMap, year);
        resultList.add(yearSales);
        for (int i = 1; i < 5; i++) {
            resultList.add(new QuarterMonthSalesVolumeVO(quarterTargetMap.get(i), quarterMap.get(i), year, i));
        }
        TargetAnalysisVO<QuarterMonthSalesVolumeVO> vo = new TargetAnalysisVO<>();
        vo.setList(resultList);
        HashMap<String, BigDecimal> yearMap = new LinkedHashMap<>();
        yearMap.put(yearSales.getDimension(), new BigDecimal(yearSales.getRealNum()).setScale(0));
        yearMap.put(QuarterMonthSalesVolumeVO.TARGET_SALES_AMOUNT, new BigDecimal(yearSales.getTargetNum()).setScale(0));
        yearMap.put("完成率", yearSales.getCompletionRate());
        vo.setYearSalesTarget(yearMap);
        return vo;
    }

    /**
     * 季度销售额结构构建
     *
     * @param quarterTargetMap
     * @param quarterMap
     * @param year
     * @return
     */
    private TargetAnalysisVO<QuarterMonthSalesVO> getQuarterResultList(Map<Integer, BigDecimal> quarterTargetMap, Map<Integer, BigDecimal> quarterMap, Integer year) {
        ArrayList<QuarterMonthSalesVO> resultList = new ArrayList<>();
        // 年度销售额
        QuarterMonthSalesVO yearSales = new QuarterMonthSalesVO(quarterTargetMap, quarterMap, year);
        resultList.add(yearSales);
        for (int i = 1; i < 5; i++) {
            resultList.add(new QuarterMonthSalesVO(quarterTargetMap.get(i), quarterMap.get(i), year, i));
        }
        TargetAnalysisVO<QuarterMonthSalesVO> vo = new TargetAnalysisVO<>();
        vo.setList(resultList);
        HashMap<String, BigDecimal> yearMap = new LinkedHashMap<>();
        yearMap.put(yearSales.getDimension(), yearSales.getRealAmount());
        yearMap.put(QuarterMonthSalesVolumeVO.TARGET_SALES_AMOUNT, yearSales.getTargetAmount());
        yearMap.put("完成率", yearSales.getCompletionRate());
        vo.setYearSalesTarget(yearMap);
        return vo;
    }

    /**
     * 月度销量结果构建
     *
     * @param quarterTargetMap
     * @param quarterMap
     * @param year
     * @return
     */
    private TargetAnalysisVO<QuarterMonthSalesVolumeVO> getMonthVolumeResultList(Map<Integer, Integer> quarterTargetMap, Map<Integer, Integer> quarterMap, int year) {
        LinkedList<QuarterMonthSalesVolumeVO> resultList = new LinkedList<>();
        quarterMap.entrySet().stream().forEach(x -> resultList.add(new QuarterMonthSalesVolumeVO(quarterTargetMap.get(x.getKey()), quarterMap.get(x.getKey()), null, x.getKey())));
        TargetAnalysisVO<QuarterMonthSalesVolumeVO> vo = new TargetAnalysisVO<>();
        vo.setList(resultList);
        // 年度销售额
        QuarterMonthSalesVolumeVO yearSales = new QuarterMonthSalesVolumeVO(quarterTargetMap, quarterMap, year);
        HashMap<String, BigDecimal> yearMap = new LinkedHashMap<>();
        yearMap.put(yearSales.getDimension(), new BigDecimal(yearSales.getRealNum()).setScale(0));
        yearMap.put(QuarterMonthSalesVolumeVO.TARGET_SALES_AMOUNT, new BigDecimal(yearSales.getTargetNum()).setScale(0));
        yearMap.put("完成率", yearSales.getCompletionRate());
        vo.setYearSalesTarget(yearMap);
        return vo;
    }

    /**
     * 月度销售额结构构建
     *
     * @param quarterTargetMap
     * @param quarterMap
     * @param year
     * @return
     */
    private TargetAnalysisVO<QuarterMonthSalesVO> getMonthResultList(Map<Integer, BigDecimal> quarterTargetMap, Map<Integer, BigDecimal> quarterMap, Integer year) {
        LinkedList<QuarterMonthSalesVO> resultList = new LinkedList<>();
        quarterMap.entrySet().stream().forEach(x -> resultList.add(new QuarterMonthSalesVO(quarterTargetMap.get(x.getKey()), quarterMap.get(x.getKey()), null, x.getKey())));
        TargetAnalysisVO<QuarterMonthSalesVO> vo = new TargetAnalysisVO<>();
        vo.setList(resultList);
        // 年度销售额
        QuarterMonthSalesVO yearSales = new QuarterMonthSalesVO(quarterTargetMap, quarterMap, year);
        HashMap<String, BigDecimal> yearMap = new LinkedHashMap<>();
        yearMap.put(yearSales.getDimension(), yearSales.getRealAmount());
        yearMap.put(QuarterMonthSalesVolumeVO.TARGET_SALES_AMOUNT, yearSales.getTargetAmount());
        yearMap.put("完成率", yearSales.getCompletionRate());
        vo.setYearSalesTarget(yearMap);
        return vo;
    }

    /**
     * 列表界面数据处理
     */
    private void dmpOrderInfoHand(List<DmpOrderInfoDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        List<String> ids = records.stream().map(DmpOrderInfoDTO::getId).collect(Collectors.toList());
        List<BiOrderItemSplitEntity> dmpOrderItemList = biOrderItemSplitService.listByOrderInfoIds(ids);
        records.forEach(obj -> {
            List<BiOrderItemSplitEntity> itemList = dmpOrderItemList.stream().filter(e -> obj.getId().equals(e.getOrderId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(itemList)) {
                List<DmpOrderItemDTO> itemResultList = BeanMapperUtils.copyList(DmpOrderItemDTO.class, itemList);
                itemResultList.stream().forEach(e -> e.setSellAmountOrigin(MathUtil.multiply(e.getSellPriceOrigin(), e.getQuantity())));
                obj.setChildren(itemResultList);
            }
            obj.setOrderStateName(OrderStateEnum.getName(obj.getOrderState()));
            obj.setCorrectionStatusName(OrderStateEnum.getName(obj.getCorrectionStatus()));
        });
    }

    /**
     * 通过SKU NO查询首单
     */
    @Override
    public BiOrderInfoEntity firstOrderBySkuNo(String skuNo) {
        String subSql = CharSequenceUtil.format("select order_id from bi_order_item_split where sku_no = '{}'", skuNo);

        return lambdaQuery()
                .inSql(BiOrderInfoEntity::getId, subSql)
                .orderByAsc(BiOrderInfoEntity::getPlatformOrderStatus)
                .last(" LIMIT 1")
                .one();
    }

    /**
     * 通过SKU NO查询各平台首单
     */
    @Override
    public Map<String, BiOrderInfoEntity> mapFirstOrderBySkuNo(String skuNo) {
        String subSql = CharSequenceUtil.format(" select order_id from bi_order_item_split where sku_no = '{}' ", skuNo);

        List<BiOrderInfoEntity> list = query()
                .select("MIN(platform_create_time) as platform_create_time",
                        "source_platform as source_platform")
                .inSql(BaseEntity.ID, subSql)
                .groupBy(BiOrderInfoEntity.SOURCE_PLATFORM)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.stream()
                .collect(Collectors.toMap(BiOrderInfoEntity::getSourcePlatform, Function.identity()));
    }

    @Override
    public PagingVO<DmpOrderInfoExcelDTO> exportBiOrderInfo(PagingDTO<DmpOrderInfoSearchDTO> dto) {
        //查询所有数据
        Page<DmpOrderInfoExcelDTO> excelList = baseMapper.getAllDmpOrderInfo(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        return new PagingVO<>(excelList);
    }


}




