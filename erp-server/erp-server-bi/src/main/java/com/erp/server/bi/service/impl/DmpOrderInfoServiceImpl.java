package com.erp.server.bi.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
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
import com.common.business.vo.ChartVO;
import com.common.business.vo.PagingVO;
import com.common.business.vo.SeriesVO;
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
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.dto.SysDepartmentTreeDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.constant.ChartType;
import com.erp.server.bi.enums.DateTypeEnum;
import com.erp.server.bi.enums.OrderStateEnum;
import com.erp.server.bi.enums.SettleMethodEnum;
import com.erp.server.bi.enums.TimeTypeEnum;
import com.erp.server.bi.listener.DmpOrderInfoExcelListener;
import com.erp.server.bi.mapper.DmpOrderInfoMapper;
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
import java.math.RoundingMode;
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
public class DmpOrderInfoServiceImpl extends ServiceImpl<DmpOrderInfoMapper, DmpOrderInfoEntity>
        implements DmpOrderInfoService {
    @Resource
    private DmpOrderItemService dmpOrderItemService;
    @Resource
    private DmpReturnOrderInfoService dmpReturnOrderInfoService;
    @Resource
    private DmpShopInfoService dmpShopInfoService;
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

    @Override
    public PagingVO<DmpOrderInfoDTO> paging(PagingDTO<DmpOrderInfoSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        DmpOrderInfoSearchDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        IPage<DmpOrderInfoDTO> pageData = baseMapper.paging(query, params);
        List<DmpOrderInfoDTO> records = pageData.getRecords();
        dmpOrderInfoHand(records);
        return new PagingVO(pageData);
    }

    @Override
    public Boolean updateState(DmpOrderStateDTO dto) {
        DmpOrderInfoEntity dmpOrderInfoEntity = this.getById(dto.getId());
        if (ObjectUtils.isEmpty(dmpOrderInfoEntity)) {
            throw new ServiceException(ApiError.Default);
        }
        dmpOrderInfoEntity.setCorrectionStatus(dto.getState());
        return this.updateById(dmpOrderInfoEntity);
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
        return;
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:sumSales",keyGenerator = "myKeyGenerator")
    public TargetSaleSumVO sumSales(BiFilterDTO dto) {
        // 没有sku情况
//        BigDecimal amount = BigDecimal.ZERO;
//        QueryWrapper<DmpOrderInfoEntity> query = getDmpOrderInfoEntityQueryWrapper(dto);
        // 条件存在sku的情况
        // 先查询订单号
//        query.select("id");
//        List<DmpOrderInfoEntity> list = baseMapper.selectList(query);
//        if (CollectionUtils.isEmpty(list)) {
//            return new TargetSaleSumVO(amount);
//        }
//        List<String> orderIds = list.stream().map(DmpOrderInfoEntity::getId).collect(Collectors.toList());
//        // 根据订单号获取订单详情，筛选sku
//        amount = dmpOrderItemService.sumSales(orderIds, dto);
//        Integer flag = null;
        if (null != dto.getHasNewSign() && dto.getHasNewSign()) {
            dto.setNewSign(1);
        }
        dto.setDateType(DateTypeEnum.DAY.getType());
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        BigDecimal amount = baseMapper.sumSales(dto, settleRate);
        return new TargetSaleSumVO(amount.setScale(4, RoundingMode.DOWN));
    }

    @Override
    public StatisticalDataVO salePriceDistribution(BiFilterDTO dto) {

        //获取区间列表
        List<SalesPriceRangeVO> rangeVOS = getRangeList(dto.getRangeType());
        //判断区间是否存在部门，存在则覆盖请求参数，不存在则查询为空
        SalesPriceRangeVO salesPriceRangeVO1 = rangeVOS.stream().filter(rangeVO -> StringUtils.isNotEmpty(rangeVO.getDeptId())).findFirst().orElse(null);
        StatisticalDataVO statistical = new StatisticalDataVO();
        statistical.setChartType(ChartType.BAR);
        statistical.setName("销售单价分布");
        Integer dataType;
        if (Objects.isNull(dto.getDataType())) {
            dataType = 1;
        } else {
            dataType = dto.getDataType();
        }
        if (Objects.nonNull(salesPriceRangeVO1) && StringUtils.isNotEmpty(salesPriceRangeVO1.getDeptId())) {
            //汇总组织下全部组织列表（包括本级和中间级）
            List<SysDepartmentTreeDTO> depts = sysUserFeign.getDeptByParentId(salesPriceRangeVO1.getDeptId());
            List<String> deptIds;
            if (CollectionUtils.isNotEmpty(depts)) {
                deptIds = depts.stream().map(SysDepartmentTreeDTO::getId).collect(Collectors.toList());
                deptIds.add(salesPriceRangeVO1.getDeptId());
            } else {
                deptIds = Collections.singletonList(salesPriceRangeVO1.getDeptId());
            }
            dto.setDepartment(deptIds);
            //获取到结算汇率
        }
        List<String> xAxisList = rangeVOS.stream().map(e -> {
            if (e.getEndValue() == -1) {
                return e.getStartValue() + "及以上";
            } else {
                return e.getStartValue() + "-" + e.getEndValue();
            }
        }).collect(Collectors.toList());
        ChartVO chart = new ChartVO();
        List<SeriesVO<Object>> seriesList = new ArrayList<>(10);
        SeriesVO series = new SeriesVO();
        if (2 == dataType) {
            series.setName("销量");
        } else {
            series.setName("销售额");
        }
        List<String> dataList = new ArrayList<>(rangeVOS.size());
        //根据区间进行汇总
        rangeVOS.forEach(salesPriceRangeVO -> {
            //防止最后范围统计不到最大单价
            String settleRate = getSettleRate(dto.getSettleMethod());
            if (Objects.nonNull(salesPriceRangeVO.getStartValue()) && Objects.nonNull(salesPriceRangeVO.getEndValue())) {
                SalePriceDistributionVO vo = baseMapper.countSalePriceDistribution(dto, settleRate, salesPriceRangeVO.getStartValue(), salesPriceRangeVO.getEndValue());
                if (2 == dataType) {
                    if (Objects.isNull(vo) || Objects.isNull(vo.getSalesQuantity())) {
                        dataList.add("0");
                    } else {
                        dataList.add(String.valueOf(vo.getSalesQuantity()));
                    }
                } else {
                    if (Objects.isNull(vo) || Objects.isNull(vo.getSaleAmount())) {
                        dataList.add("0");
                    } else {
                        dataList.add(vo.getSaleAmount().stripTrailingZeros().toPlainString());
                    }

                }
            }
        });
        series.setData(dataList);
        seriesList.add(series);
        chart.setXAxis(xAxisList);
        chart.setSeries(seriesList);
        statistical.setData(chart);
        return statistical;
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

    private static QueryWrapper<DmpOrderInfoEntity> getDmpOrderInfoEntityQueryWrapper(BiFilterDTO dto) {
        QueryWrapper<DmpOrderInfoEntity> query = new QueryWrapper<>();
        query.ge(dto.getTimeType().equals(TimeTypeEnum.ORDER_TIME.getCode()), "platform_create_time", dto.getStartTime())
                .lt(dto.getTimeType().equals(TimeTypeEnum.ORDER_TIME.getCode()), "platform_create_time", dto.getEndTime())
                // 订单时间字段
                .ge(dto.getTimeType().equals(TimeTypeEnum.DELIVERY_TIME.getCode()), "delivery_time", dto.getStartTime())
                .lt(dto.getTimeType().equals(TimeTypeEnum.DELIVERY_TIME.getCode()), "delivery_time", dto.getEndTime())
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
//        Integer count = 0;
//        QueryWrapper<DmpOrderInfoEntity> query = getDmpOrderInfoEntityQueryWrapper(dto);
//        // 先查询订单号
//        query.select("id")
//                .last(StringUtils.isNotBlank(dto.getParam()), dto.getParam());
//        List<DmpOrderInfoEntity> list = baseMapper.selectList(query);
//        if (CollectionUtils.isEmpty(list)) {
//            return new TargetSaleCountVO(count);
//        }
//        List<String> orderIds = list.stream().map(DmpOrderInfoEntity::getId).collect(Collectors.toList());
//        // 根据订单号获取订单详情，筛选sku
//        count = dmpOrderItemService.countSalesVolume(orderIds, dto.getSku());
        Integer count = baseMapper.countSalesVolume(dto);
        return new TargetSaleCountVO(count);
    }

    @Override
    public TargetSaleCountVO countOrderQuantity(BiFilterDTO dto) {
        // 没有sku情况
        Integer count = 0;
        QueryWrapper<DmpOrderInfoEntity> query = getDmpOrderInfoEntityQueryWrapper(dto);
        // 无sku条件 只查询订单表
        if (CollectionUtils.isEmpty(dto.getSku())) {
            count = baseMapper.selectCount(query);
        } else {
            // 条件存在sku的情况 查询订单详情表
            // 先查询订单号
            query.select("id");
            List<DmpOrderInfoEntity> list = baseMapper.selectList(query);
            if (CollectionUtils.isEmpty(list)) {
                return new TargetSaleCountVO(count);
            }
            List<String> orderIds = list.stream().map(DmpOrderInfoEntity::getId).collect(Collectors.toList());
            // 根据订单号获取订单详情，筛选sku
            count = dmpOrderItemService.countOrderQuantityBySku(orderIds, dto.getSku());
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
        QueryWrapper<DmpOrderInfoEntity> query = getDmpOrderInfoEntityQueryWrapper(dto);
        // 无sku条件 只查询订单表
        query.eq("is_returned", 1);
        List<DmpOrderInfoEntity> list = baseMapper.selectList(query);
        if (CollectionUtils.isEmpty(list)) {
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }
        List<String> orderIds = list.stream().map(DmpOrderInfoEntity::getId).collect(Collectors.toList());
        BigDecimal amount = dmpReturnOrderInfoService.sumRefundAmount(orderIds, dto);
        return new TargetSaleSumVO(amount);
    }

    @Override
    public TargetSaleCountVO countRefundOrderNum(BiFilterDTO dto) {
        // 获取退款订单数量
        // 没有sku情况
        Integer count;
        QueryWrapper<DmpOrderInfoEntity> query = getDmpOrderInfoEntityQueryWrapper(dto);
        // 无sku条件 只查询订单表
        query.eq("is_refund", 1);
        if (CollectionUtils.isEmpty(dto.getSku())) {
            count = baseMapper.selectCount(query);
        } else {
            // 条件存在sku的情况 查询订单详情表
            // 先查询订单号
            query.select("id");
            List<DmpOrderInfoEntity> list = baseMapper.selectList(query);
            if (CollectionUtils.isEmpty(list)) {
                return new TargetSaleCountVO(0);
            }
            List<String> orderIds = list.stream().map(DmpOrderInfoEntity::getId).collect(Collectors.toList());
            // 根据订单号获取订单详情，筛选sku
            count = dmpOrderItemService.countOrderQuantityBySku(orderIds, dto.getSku());
        }
        return new TargetSaleCountVO(count);
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:statisticsCustomerPrice",keyGenerator = "myKeyGenerator")
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
    @Cacheable(cacheNames = "cache:bi:statisticsDomesticSalesRatio",keyGenerator = "myKeyGenerator")
    public TargetSaleSumVO statisticsDomesticSalesRatio(BiFilterDTO dto) {
        // 销售总额
        TargetSaleSumVO targetSaleSumVO = sumSales(dto);
        BigDecimal salesAmount = targetSaleSumVO.getValue();
        if (BigDecimal.ZERO.compareTo(salesAmount) >= 0) {
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }
        // 查询国内店铺no
        List<DmpShopInfoEntity> shopList = dmpShopInfoService.lambdaQuery()
                .eq(DmpShopInfoEntity::getStatus, 1)
                .eq(DmpShopInfoEntity::getStoreSign, "cn")
                .last(StringUtils.isNotBlank(dto.getPermissionSql()), dto.getPermissionSql())
                .list();
        if (CollectionUtils.isEmpty(shopList)) {
            return new TargetSaleSumVO(BigDecimal.ZERO);
        }
        // 国内销售额
        dto.setShopName(shopList.stream().map(DmpShopInfoEntity::getName).collect(Collectors.toList()));
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
        StringBuffer sb = new StringBuffer();
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
    public DmpOrderInfoEntity getByPlatformOrderId(String platformOrderId) {
        LambdaQueryWrapper<DmpOrderInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DmpOrderInfoEntity::getPlatformOrderId, platformOrderId);
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }


    @Override
    @Cacheable(cacheNames = "cache:bi:sumQuarterSales",keyGenerator = "myKeyGenerator")
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
        QueryWrapper<DmpOrderInfoEntity> qw = new QueryWrapper<>();
        boolean flag = TimeTypeEnum.ORDER_TIME.getCode() == dto.getTimeType();
        String groupByStr = flag ? "platform_create_time" : "delivery_time";
        qw.select("SUM(COALESCE(amount_after,0)) as order_fee", groupByStr);
        List<DmpOrderInfoEntity> entityList = getOrderInfoEntities(dto, qw, start, end, groupByStr);
        if (CollectionUtils.isEmpty(entityList)) {
            return getQuarterResultList(quarterTargetMap, new HashMap<>(4), start.getYear());
        }
        Map<Integer, BigDecimal> quarterMap = entityList.stream().collect(Collectors.groupingBy(x -> (
                        // 按照季度分组
                        (flag ? x.getPlatformCreateTime() : x.getDeliveryTime()).getMonthValue() - 1) / 3 + 1,
                // 对销售额进行求和
                Collectors.reducing(BigDecimal.ZERO, DmpOrderInfoEntity::getOrderFee, BigDecimal::add)
        ));

        // 计算完成率
        return getQuarterResultList(quarterTargetMap, quarterMap, dto.getStartTime().getYear());
    }

    private List<DmpOrderInfoEntity> getOrderInfoEntities(BiFilterDTO dto, QueryWrapper<DmpOrderInfoEntity> qw, LocalDateTime start, LocalDateTime end, String groupStr) {
        boolean flag1 = TimeTypeEnum.ORDER_TIME.getCode() == dto.getTimeType();
        boolean flag2 = TimeTypeEnum.DELIVERY_TIME.getCode() == dto.getTimeType();
        qw.ge(flag1, "platform_create_time", start)
                .le(flag1, "platform_create_time", end)
                .ge(flag2, "delivery_time", start)
                .le(flag2, "delivery_time", end)
                .groupBy(StringUtils.isNotBlank(groupStr), groupStr)
                .last(StringUtils.isNotBlank(dto.getPermissionSql()), dto.getPermissionSql());
        List<DmpOrderInfoEntity> entityList = baseMapper.selectList(qw);
        return entityList;
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:sumQuarterSalesVolume",keyGenerator = "myKeyGenerator")
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
        QueryWrapper<DmpOrderInfoEntity> qw = new QueryWrapper<>();
        boolean flag = TimeTypeEnum.ORDER_TIME.getCode() == dto.getTimeType();
        qw.select("id", "platform_create_time", "delivery_time");
        List<DmpOrderInfoEntity> entityList = getOrderInfoEntities(dto, qw, start, end, null);
        if (CollectionUtils.isEmpty(entityList)) {
            return getQuarterVolumeResultList(quarterTargetMap, new HashMap<>(4), start.getYear());
        }
        List<String> orderIds = entityList.stream().map(DmpOrderInfoEntity::getId).collect(Collectors.toList());
        List<DmpOrderItemEntity> entityItemList = dmpOrderItemService.listByOrderInfoIds(orderIds);
        if (CollectionUtils.isEmpty(entityItemList)) {
            return getQuarterVolumeResultList(quarterTargetMap, new HashMap<>(4), start.getYear());
        }
        // 根据订单号的分组计算销量
        Map<String, Integer> orderQuantityMap = entityItemList.stream().collect(Collectors.groupingBy(DmpOrderItemEntity::getOrderId,
                Collectors.summingInt(DmpOrderItemEntity::getQuantity)));
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
    @Cacheable(cacheNames = "cache:bi:sumMonthSales",keyGenerator = "myKeyGenerator")
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
        QueryWrapper<DmpOrderInfoEntity> qw = new QueryWrapper<>();
        boolean flag = TimeTypeEnum.ORDER_TIME.getCode() == dto.getTimeType();
        String groupByStr = flag ? "platform_create_time" : "delivery_time";
        qw.select("SUM(COALESCE(order_fee, 0)) as order_fee", groupByStr);
        List<DmpOrderInfoEntity> entityList = getOrderInfoEntities(dto, qw, start, end, groupByStr);
        if (CollectionUtils.isEmpty(entityList)) {
            return getMonthResultList(monthTargetMap, new HashMap<>(4), start.getYear());
        }
        Map<Integer, BigDecimal> monthMap = entityList.stream().collect(Collectors.groupingBy(x ->
                        // 按照季度分组
                        (flag ? x.getPlatformCreateTime() : x.getDeliveryTime()).getMonthValue(),
                // 对销售额进行求和
                Collectors.reducing(BigDecimal.ZERO, DmpOrderInfoEntity::getOrderFee, BigDecimal::add)
        ));

        // 计算完成率
        return getMonthResultList(monthTargetMap, monthMap, start.getYear());
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:sumMonthSalesVolume",keyGenerator = "myKeyGenerator")
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
        QueryWrapper<DmpOrderInfoEntity> qw = new QueryWrapper<>();
        boolean flag = TimeTypeEnum.ORDER_TIME.getCode() == dto.getTimeType();
        qw.select("id", "platform_create_time", "delivery_time");
        List<DmpOrderInfoEntity> entityList = getOrderInfoEntities(dto, qw, start, end, null);
        if (CollectionUtils.isEmpty(entityList)) {
            return getMonthVolumeResultList(quarterTargetMap, new HashMap<>(4), start.getYear());
        }
        List<String> orderIds = entityList.stream().map(DmpOrderInfoEntity::getId).collect(Collectors.toList());
        List<DmpOrderItemEntity> entityItemList = dmpOrderItemService.listByOrderInfoIds(orderIds);
        if (CollectionUtils.isEmpty(entityItemList)) {
            return getMonthVolumeResultList(quarterTargetMap, new HashMap<>(4), start.getYear());
        }
        // 根据订单号的分组计算销量
        Map<String, Integer> orderQuantityMap = entityItemList.stream().collect(Collectors.groupingBy(DmpOrderItemEntity::getOrderId,
                Collectors.summingInt(DmpOrderItemEntity::getQuantity)));
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
        if (CollectionUtil.isEmpty(targetList)) {
            return new ArrayList<>();
        }
        Map<Integer, List<BiTargetManagementEntity>> salesTypeMap = targetList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getTargetType));
        // 查询目标销售额
        List<BiTargetManagementEntity> salesList = salesTypeMap.get(1);
        Map<String, BigDecimal> targetSalesMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(salesList)) {
            targetSalesMap = salesList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getPlatformName,
                    MathUtil.summingBigDecimal(BiTargetManagementEntity::getJanuary)));
        }

        // 查询目标销量
        List<BiTargetManagementEntity> salesVolumeList = salesTypeMap.get(0);
        Map<String, Integer> targetSalesVolumeMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(salesVolumeList)) {
            targetSalesVolumeMap = salesVolumeList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getPlatformName,
                    Collectors.summingInt(x -> x.getJanuary().intValue())));
        }
        // 查询实际销售额
        QueryWrapper<DmpOrderInfoEntity> qw = new QueryWrapper<>();
        List<DmpOrderInfoEntity> orderInfoEntities = getOrderInfoEntities(dto, qw, start, end, null);
        if (CollectionUtils.isEmpty(orderInfoEntities)) {
            return new ArrayList<>();
        }
        Map<String, BigDecimal> saleAmountMap = orderInfoEntities.stream()
                .collect(Collectors.groupingBy(DmpOrderInfoEntity::getSourcePlatform,
                        Collectors.reducing(BigDecimal.ZERO, x -> x.getOrderFee().multiply(x.getCurrencyRate()), BigDecimal::add)));
        // 查询实际销量
        List<String> orderIds = orderInfoEntities.stream().map(DmpOrderInfoEntity::getId).collect(Collectors.toList());
        List<DmpOrderItemEntity> entityItemList = dmpOrderItemService.listByOrderInfoIds(orderIds);
        if (CollectionUtils.isEmpty(entityItemList)) {
            return new ArrayList<>();
        }
        // 根据订单号的分组计算销量
        Map<String, Integer> orderQuantityMap = entityItemList.stream().collect(Collectors.groupingBy(DmpOrderItemEntity::getOrderId,
                Collectors.summingInt(DmpOrderItemEntity::getQuantity)));
        // 对订单号进行平台分组
        Map<String, Integer> salesVolumeMap = orderInfoEntities.stream().collect(Collectors.groupingBy(x -> x.getSourcePlatform(),
                Collectors.summingInt(x -> orderQuantityMap.getOrDefault(x.getId(), 0))));

        List<SalesCompletionInfoVO> rankResult = assemblyResult(dto, targetSalesMap, targetSalesVolumeMap, null, salesVolumeMap, saleAmountMap);
        return rankResult;
    }

    @Override
    public List<SalesCompletionInfoVO> sumCategorySalesCompletion(BiFilterDTO dto) {
        // 获取月度开始时间和结束时间
        LocalDateTime start = LocalDateTime.of(LocalDate.from(dto.getStartTime().with(TemporalAdjusters.firstDayOfMonth())), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.from(dto.getEndTime().with(TemporalAdjusters.lastDayOfMonth())), LocalTime.MAX);
        // 查询目标数据
        List<BiTargetManagementEntity> targetList = biTargetManagementService.getSales(start, end, dto.getPermissionSql());
        if (CollectionUtil.isEmpty(targetList)) {
            return new ArrayList<>();
        }
        Map<Integer, List<BiTargetManagementEntity>> salesTypeMap = targetList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getTargetType));
        // 查询目标销售额
        List<BiTargetManagementEntity> salesList = salesTypeMap.get(1);
        Map<String, BigDecimal> targetSalesMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(salesList)) {
            targetSalesMap = salesList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getCategory,
                    MathUtil.summingBigDecimal(BiTargetManagementEntity::getJanuary)));
        }

        // 查询目标销量
        List<BiTargetManagementEntity> salesVolumeList = salesTypeMap.get(0);
        Map<String, Integer> targetSalesVolumeMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(salesVolumeList)) {
            targetSalesVolumeMap = salesVolumeList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getCategory,
                    Collectors.summingInt(x -> x.getJanuary().intValue())));
        }
        // 查询实际销售额
        QueryWrapper<DmpOrderInfoEntity> qw = new QueryWrapper<>();
        List<DmpOrderInfoEntity> orderInfoEntities = getOrderInfoEntities(dto, qw, start, end, null);
        if (CollectionUtils.isEmpty(orderInfoEntities)) {
            return new ArrayList<>();
        }

        // 查询实际销量
        List<String> orderIds = orderInfoEntities.stream().map(DmpOrderInfoEntity::getId).collect(Collectors.toList());
        List<DmpOrderItemEntity> entityItemList = dmpOrderItemService.listByOrderInfoIds(orderIds);
        if (CollectionUtils.isEmpty(entityItemList)) {
            return new ArrayList<>();
        }
        // 根据订单号的分组计算销量
        Map<String, Integer> salesVolumeMap = entityItemList.stream().collect(Collectors.groupingBy(DmpOrderItemEntity::getCategoryName,
                Collectors.summingInt(DmpOrderItemEntity::getQuantity)));
        // 对订单号进行平台分组
        Map<String, BigDecimal> rateMap = orderInfoEntities.stream().collect(Collectors.toMap(DmpOrderInfoEntity::getId, DmpOrderInfoEntity::getCurrencyRate));
        Map<String, BigDecimal> saleAmountMap = entityItemList.stream().collect(Collectors.groupingBy(DmpOrderItemEntity::getCategoryName,
                Collectors.reducing(BigDecimal.ZERO,
                        x -> new BigDecimal(x.getQuantity()).multiply(null == x.getSellPrice() ? BigDecimal.ZERO : x.getSellPrice()).multiply(rateMap.getOrDefault(x.getOrderId(), BigDecimal.ZERO)),
                        BigDecimal::add)
        ));


        List<SalesCompletionInfoVO> rankResult = assemblyResult(dto, targetSalesMap, targetSalesVolumeMap, null, salesVolumeMap, saleAmountMap);
        return rankResult;
    }

    @Override
    public List<SalesCompletionInfoVO> sumNewProductSalesCompletion(BiFilterDTO dto, Integer newSign) {
        // 获取月度开始时间和结束时间
        LocalDateTime start = LocalDateTime.of(LocalDate.from(dto.getStartTime().with(TemporalAdjusters.firstDayOfMonth())), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.from(dto.getEndTime().with(TemporalAdjusters.lastDayOfMonth())), LocalTime.MAX);
        // 查询目标数据
        List<BiTargetManagementEntity> targetList = biTargetManagementService.getSales(start, end, dto.getPermissionSql());
        if (CollectionUtil.isEmpty(targetList)) {
            return new ArrayList<>();
        }
        Map<Integer, List<BiTargetManagementEntity>> salesTypeMap = targetList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getTargetType));
        // 查询目标销售额
        List<BiTargetManagementEntity> salesList = salesTypeMap.get(1);
        Map<String, BigDecimal> targetSalesMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(salesList)) {
            targetSalesMap = salesList.stream().filter(x -> null == newSign || newSign.equals(x.getProductType())).collect(Collectors.groupingBy(x -> x.getSkuNo(),
                    MathUtil.summingBigDecimal(BiTargetManagementEntity::getJanuary)));
        }

        // 查询目标销量
        List<BiTargetManagementEntity> salesVolumeList = salesTypeMap.get(0);
        Map<String, Integer> targetSalesVolumeMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(salesVolumeList)) {
            targetSalesVolumeMap = salesVolumeList.stream().filter(x -> null == newSign || newSign.equals(x.getProductType())).collect(Collectors.groupingBy(x -> x.getSkuNo(),
                    Collectors.summingInt(x -> x.getJanuary().intValue())));
        }

        // 查询实际销售额
        QueryWrapper<DmpOrderInfoEntity> qw = new QueryWrapper<>();
        List<DmpOrderInfoEntity> orderInfoEntities = getOrderInfoEntities(dto, qw, start, end, null);
        if (CollectionUtils.isEmpty(orderInfoEntities)) {
            return new ArrayList<>();
        }

        // 查询实际销量
        List<String> orderIds = orderInfoEntities.stream().map(DmpOrderInfoEntity::getId).collect(Collectors.toList());
        List<DmpOrderItemEntity> entityItemList = dmpOrderItemService.listByConditions(orderIds, newSign, new ArrayList<>());
        if (CollectionUtils.isEmpty(entityItemList)) {
            return new ArrayList<>();
        }
        // 根据sku的分组计算销量
        Map<String, Integer> salesVolumeMap = entityItemList.stream().filter(x -> StringUtils.isNotBlank(x.getSkuNo()))
                .collect(Collectors.groupingBy(DmpOrderItemEntity::getSkuNo,
                        Collectors.summingInt(DmpOrderItemEntity::getQuantity)));
        // 汇率map
        Map<String, BigDecimal> rateMap = orderInfoEntities.stream().collect(Collectors.toMap(DmpOrderInfoEntity::getId, DmpOrderInfoEntity::getCurrencyRate));
        // 根据sku的分组计算销售额
        Map<String, BigDecimal> saleAmountMap = entityItemList.stream()
                .filter(x -> StringUtils.isNotBlank(x.getSkuNo()))
                .collect(Collectors.groupingBy(DmpOrderItemEntity::getSkuNo,
                        Collectors.reducing(BigDecimal.ZERO,
                                x -> new BigDecimal(x.getQuantity()).multiply(null == x.getSellPrice() ? BigDecimal.ZERO : x.getSellPrice()).multiply(rateMap.getOrDefault(x.getOrderId(), BigDecimal.ZERO)),
                                BigDecimal::add)
                ));

        //  sku 品名Map
        Map<String, String> skuMap = entityItemList.stream().filter(x -> StringUtils.isNotBlank(x.getSkuNo())).collect(Collectors.groupingBy(DmpOrderItemEntity::getSkuNo,
                Collectors.collectingAndThen(Collectors.toList(), v -> v.get(0).getItemName())));

        List<SalesCompletionInfoVO> rankResult = assemblyResult(dto, targetSalesMap, targetSalesVolumeMap, skuMap, salesVolumeMap, saleAmountMap);
        return rankResult;
    }

    @Override
    public List<SalesCompletionInfoVO> sumProductPositionSalesCompletion(BiFilterDTO dto) {
        // 获取月度开始时间和结束时间
        LocalDateTime start = LocalDateTime.of(LocalDate.from(dto.getStartTime().with(TemporalAdjusters.firstDayOfMonth())), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.from(dto.getEndTime().with(TemporalAdjusters.lastDayOfMonth())), LocalTime.MAX);
        List<BiTargetManagementEntity> targetList = biTargetManagementService.getSales(start, end, dto.getPermissionSql());
        if (CollectionUtil.isEmpty(targetList)) {
            return new ArrayList<>();
        }
        Map<Integer, List<BiTargetManagementEntity>> salesTypeMap = targetList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getTargetType));
        // 查询目标销售额
        List<BiTargetManagementEntity> salesList = salesTypeMap.get(1);
        Map<String, BigDecimal> targetSalesMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(salesList)) {
            targetSalesMap = salesList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getProductPosition,
                    MathUtil.summingBigDecimal(BiTargetManagementEntity::getJanuary)));
        }

        // 查询目标销量
        List<BiTargetManagementEntity> salesVolumeList = salesTypeMap.get(0);
        Map<String, Integer> targetSalesVolumeMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(salesVolumeList)) {
            targetSalesVolumeMap = salesVolumeList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getProductPosition,
                    Collectors.summingInt(x -> x.getJanuary().intValue())));
        }
        // 查询实际销售额
        QueryWrapper<DmpOrderInfoEntity> qw = new QueryWrapper<>();
        List<DmpOrderInfoEntity> orderInfoEntities = getOrderInfoEntities(dto, qw, start, end, null);
        if (CollectionUtils.isEmpty(orderInfoEntities)) {
            return new ArrayList<>();
        }

        // 查询实际销量
        List<String> orderIds = orderInfoEntities.stream().map(DmpOrderInfoEntity::getId).collect(Collectors.toList());
        List<String> sku = targetList.stream().map(BiTargetManagementEntity::getSkuNo).collect(Collectors.toList());
        List<DmpOrderItemEntity> entityItemList = dmpOrderItemService.listByConditions(orderIds, null, sku);
        if (CollectionUtils.isEmpty(entityItemList)) {
            return new ArrayList<>();
        }
        // 根据sku的分组计算销量
        Map<String, Integer> skuSalesVolumeMap = entityItemList.stream()
                .collect(Collectors.groupingBy(DmpOrderItemEntity::getSkuNo,
                        Collectors.summingInt(DmpOrderItemEntity::getQuantity)));
        // 产品定位销量map
        Map<String, Integer> salesVolumeMap = targetList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getProductPosition,
                Collectors.summingInt(x -> skuSalesVolumeMap.getOrDefault(x.getSkuNo(), 0))));

        // 汇率map
        Map<String, BigDecimal> rateMap = orderInfoEntities.stream()
                .collect(Collectors.toMap(DmpOrderInfoEntity::getId, DmpOrderInfoEntity::getCurrencyRate));

        // 根据sku的分组计算销售额
        Map<String, BigDecimal> skuSaleAmountMap = entityItemList.stream()
                .collect(Collectors.groupingBy(DmpOrderItemEntity::getSkuNo,
                        Collectors.reducing(BigDecimal.ZERO,
                                x -> new BigDecimal(x.getQuantity())
                                        .multiply(null == x.getSellPrice() ? BigDecimal.ZERO : x.getSellPrice())
                                        .multiply(rateMap.getOrDefault(x.getOrderId(), BigDecimal.ZERO)),
                                BigDecimal::add)
                ));
        // 产品定位销售额map
        Map<String, BigDecimal> saleAmountMap = targetList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getProductPosition,
                MathUtil.summingBigDecimal(x -> skuSaleAmountMap.getOrDefault(x.getSkuNo(), BigDecimal.ZERO))));

        List<SalesCompletionInfoVO> rankResult = assemblyResult(dto, targetSalesMap, targetSalesVolumeMap, null, salesVolumeMap, saleAmountMap);
        return rankResult;
    }

    @Override
    public List<SalesCompletionInfoVO> sumProductTypeCompletion(BiFilterDTO dto) {
        // 获取月度开始时间和结束时间
        LocalDateTime start = LocalDateTime.of(LocalDate.from(dto.getStartTime().with(TemporalAdjusters.firstDayOfMonth())), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.from(dto.getEndTime().with(TemporalAdjusters.lastDayOfMonth())), LocalTime.MAX);
        List<BiTargetManagementEntity> targetList = biTargetManagementService.getSales(start, end, dto.getPermissionSql());
        if (CollectionUtil.isEmpty(targetList)) {
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
        QueryWrapper<DmpOrderInfoEntity> qw = new QueryWrapper<>();
        List<DmpOrderInfoEntity> orderInfoEntities = getOrderInfoEntities(dto, qw, start, end, null);
        if (CollectionUtils.isEmpty(orderInfoEntities)) {
            return new ArrayList<>();
        }

        // 查询实际销量
        List<String> orderIds = orderInfoEntities.stream().map(DmpOrderInfoEntity::getId).collect(Collectors.toList());
        List<String> sku = targetList.stream().map(BiTargetManagementEntity::getSkuNo).collect(Collectors.toList());
        List<DmpOrderItemEntity> entityItemList = dmpOrderItemService.listByConditions(orderIds, null, sku);
        if (CollectionUtils.isEmpty(entityItemList)) {
            return new ArrayList<>();
        }

        // 根据sku的分组计算销量
        Map<String, Integer> skuSalesVolumeMap = entityItemList.stream()
                .collect(Collectors.groupingBy(DmpOrderItemEntity::getSkuNo,
                        Collectors.summingInt(DmpOrderItemEntity::getQuantity)));
        // 产品定位销量map
        Map<String, Integer> salesVolumeMap = targetList.stream().collect(Collectors.groupingBy(x -> x.getProductType().toString(),
                Collectors.summingInt(x -> skuSalesVolumeMap.getOrDefault(x.getSkuNo(), 0))));

        // 汇率map
        Map<String, BigDecimal> rateMap = orderInfoEntities.stream()
                .collect(Collectors.toMap(DmpOrderInfoEntity::getId, DmpOrderInfoEntity::getCurrencyRate));

        // 根据sku的分组计算销售额
        Map<String, BigDecimal> skuSaleAmountMap = entityItemList.stream()
                .collect(Collectors.groupingBy(DmpOrderItemEntity::getSkuNo,
                        Collectors.reducing(BigDecimal.ZERO,
                                x -> new BigDecimal(x.getQuantity())
                                        .multiply(null == x.getSellPrice() ? BigDecimal.ZERO : x.getSellPrice())
                                        .multiply(rateMap.getOrDefault(x.getOrderId(), BigDecimal.ZERO)),
                                BigDecimal::add)
                ));
        // 产品类型销售额map
        Map<String, BigDecimal> saleAmountMap = targetList.stream().collect(Collectors.groupingBy(x -> x.getProductType().toString(),
                MathUtil.summingBigDecimal(x -> skuSaleAmountMap.getOrDefault(x.getSkuNo(), BigDecimal.ZERO))));

        List<SalesCompletionInfoVO> rankResult = assemblyResult(dto, targetSalesMap, targetSalesVolumeMap, null, salesVolumeMap, saleAmountMap);
        return rankResult;
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:getSalesAndYoy",keyGenerator = "myKeyGenerator")
    public TargetSaleAndYoySumVO getSalesAndYoy(BiFilterDTO dto) {
        // 查询当期销售额
//        dto.setEndTime(dto.getEndTime());
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
    @Cacheable(cacheNames = "cache:bi:countSalesVolumeAndYoy",keyGenerator = "myKeyGenerator")
    public TargetSaleAndYoyCountVO countSalesVolumeAndYoy(BiFilterDTO dto) {
        // 查询当期销售额
//        dto.setEndTime(dto.getEndTime());
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
    @Cacheable(cacheNames = "cache:bi:countOrderQuantityAndYoy",keyGenerator = "myKeyGenerator")
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
    @Cacheable(cacheNames = "cache:bi:countRefundRateAndYoy",keyGenerator = "myKeyGenerator")
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
    @Cacheable(cacheNames = "cache:bi:countRefundAmountAndYoy",keyGenerator = "myKeyGenerator")
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
    @Cacheable(cacheNames = "cache:bi:countRefundOrderNumAndYoy",keyGenerator = "myKeyGenerator")
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
        DmpOrderInfoExcelListener excelListenerUtil = new DmpOrderInfoExcelListener(importType, deptList, plmTaskFeign, dmpShopInfoService, sysUserFeign);
        try {
            EasyExcel.read(excelFile.getInputStream(), DmpOrderInfoImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();

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

            if (errorList.size() > 0) {
                StringBuffer sb = new StringBuffer();
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

        List<DmpOrderInfoEntity> list = this.lambdaQuery().in(DmpOrderInfoEntity::getPlatformOrderId, platformOrderIds).list();

        for (DmpOrderInfoImportExcelDTO addDTO : successList) {

            DmpOrderInfoEntity dmpOrderInfoEntity = list.stream().filter(obj -> obj.getPlatformOrderId().equals(addDTO.getPlatformOrderId())).findFirst().orElse(null);

            //存在错误信息则
            if (ObjectUtils.isNotEmpty(dmpOrderInfoEntity)) {
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
            List<DmpOrderInfoEntity> infoList = new ArrayList<>();
            //明细信息
            List<DmpOrderItemEntity> itemList = new ArrayList<>();

            for (Map.Entry<String, List<DmpOrderInfoImportExcelDTO>> entry : map.entrySet()) {
                DmpOrderInfoEntity info = new DmpOrderInfoEntity();
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
                    DmpOrderItemEntity item = new DmpOrderItemEntity();
                    item.setOrderId(info.getId());
                    item.setSkuNo(excelDTO.getSkuNo());
                    item.setItemName(excelDTO.getItemName());
                    item.setSellPriceOrigin(excelDTO.getSellPriceOrigin());
                    item.setQuantity(excelDTO.getQuantity());
                    itemList.add(item);
                }
            }
            //新增主表信息
            if (CollectionUtils.isNotEmpty(infoList)) {
                this.saveBatch(infoList);
            }
            //新增明细信息
            if (CollectionUtils.isNotEmpty(itemList)) {
                dmpOrderItemService.saveBatch(itemList);
            }
        }
    }

    @Override
    public Map<Integer, BigDecimal> statisticsSalesByDate(BiFilterDTO dto, Integer type) {
        // 查询销售额
        List<DmpOrderInfoEntity> list = lambdaQuery()
                .in(CollectionUtil.isNotEmpty(dto.getDepartment()), DmpOrderInfoEntity::getDeptId, dto.getDepartment())
                .in(CollectionUtil.isNotEmpty(dto.getPlatform()), DmpOrderInfoEntity::getSourcePlatform, dto.getPlatform())
                .in(CollectionUtil.isNotEmpty(dto.getSite()), DmpOrderInfoEntity::getSite, dto.getSite())
                .in(CollectionUtil.isNotEmpty(dto.getShopName()), DmpOrderInfoEntity::getShopName, dto.getShopName())
                .in(CollectionUtil.isNotEmpty(dto.getUserId()), DmpOrderInfoEntity::getChargeId, dto.getUserId())
                .ge(2 != type && ObjectUtil.isNotEmpty(dto.getStartTime()), DmpOrderInfoEntity::getPlatformCreateTime, dto.getStartTime())
                .le(2 != type && ObjectUtil.isNotEmpty(dto.getEndTime()), DmpOrderInfoEntity::getPlatformCreateTime, dto.getEndTime())
                .list();
        Map<Integer, BigDecimal> resultMap = new HashMap<>();
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
        if (CollectionUtil.isEmpty(list)) {
            return new HashMap<>();
        }
        Map<String, BigDecimal> collect = list.stream()
                .collect(Collectors.toMap(DimensionSalesVO::getDimension,
                        DimensionSalesVO::getSalesAmount));
        return collect;
    }

    @Override
    public List<DimensionSalesVO> sumSalesByCondition(BiFilterDTO dto, String groupName) {
        List<DimensionSalesVO> list = baseMapper.sumByDeptAndCostType(dto, groupName);
        return list;
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
        List<SalesCompletionInfoVO> rankResult = resultList.stream()
                .sorted(Comparator.comparing(SalesCompletionInfoVO::getSalesAmountCompletionRate).reversed()
                        .thenComparing(SalesCompletionInfoVO::getSalesVolumeCompletionRate).reversed())
                .peek(x -> x.setRanking(rankIndex.getAndIncrement()))
                .filter(x -> x.getRanking() <= dto.getRankNum())
                .collect(Collectors.toList());
        return rankResult;
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
        yearMap.put("目标销售额", new BigDecimal(yearSales.getTargetNum()).setScale(0));
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
        yearMap.put("目标销售额", yearSales.getTargetAmount());
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
        quarterMap.entrySet().stream().forEach(x -> {
            resultList.add(new QuarterMonthSalesVolumeVO(quarterTargetMap.get(x.getKey()), quarterMap.get(x.getKey()), null, x.getKey()));
        });
        TargetAnalysisVO<QuarterMonthSalesVolumeVO> vo = new TargetAnalysisVO<>();
        vo.setList(resultList);
        // 年度销售额
        QuarterMonthSalesVolumeVO yearSales = new QuarterMonthSalesVolumeVO(quarterTargetMap, quarterMap, year);
        HashMap<String, BigDecimal> yearMap = new LinkedHashMap<>();
        yearMap.put(yearSales.getDimension(), new BigDecimal(yearSales.getRealNum()).setScale(0));
        yearMap.put("目标销售额", new BigDecimal(yearSales.getTargetNum()).setScale(0));
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
        quarterMap.entrySet().stream().forEach(x -> {
            resultList.add(new QuarterMonthSalesVO(quarterTargetMap.get(x.getKey()), quarterMap.get(x.getKey()), null, x.getKey()));
        });
        TargetAnalysisVO<QuarterMonthSalesVO> vo = new TargetAnalysisVO<>();
        vo.setList(resultList);
        // 年度销售额
        QuarterMonthSalesVO yearSales = new QuarterMonthSalesVO(quarterTargetMap, quarterMap, year);
        HashMap<String, BigDecimal> yearMap = new LinkedHashMap<>();
        yearMap.put(yearSales.getDimension(), yearSales.getRealAmount());
        yearMap.put("目标销售额", yearSales.getTargetAmount());
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
        List<DmpOrderItemEntity> dmpOrderItemList = dmpOrderItemService.listByOrderInfoIds(ids);
        records.forEach(obj -> {
            List<DmpOrderItemEntity> itemList = dmpOrderItemList.stream().filter(e -> obj.getId().equals(e.getOrderId())).collect(Collectors.toList());
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
    public DmpOrderInfoEntity firstOrderBySkuNo(String skuNo) {
        String subSql = StrUtil.format("select order_id from dmp_order_item where sku_no = '{}'", skuNo);

        return lambdaQuery()
                .inSql(DmpOrderInfoEntity::getId, subSql)
                .orderByAsc(DmpOrderInfoEntity::getPlatformOrderStatus)
                .last(" LIMIT 1")
                .one();
    }

    /**
     * 通过SKU NO查询各平台首单
     */
    @Override
    public Map<String, DmpOrderInfoEntity> mapFirstOrderBySkuNo(String skuNo) {
        String subSql = StrUtil.format(" select order_id from dmp_order_item where sku_no = '{}' ", skuNo);

        List<DmpOrderInfoEntity> list = query()
                .select("MIN(platform_create_time) as platform_create_time",
                        "source_platform as source_platform")
                .inSql(BaseEntity.ID, subSql)
                .groupBy(DmpOrderInfoEntity.SOURCE_PLATFORM)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.stream()
                .collect(Collectors.toMap(DmpOrderInfoEntity::getSourcePlatform, Function.identity()));
    }


}




