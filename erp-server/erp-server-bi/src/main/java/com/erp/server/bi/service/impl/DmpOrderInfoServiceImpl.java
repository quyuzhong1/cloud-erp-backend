package com.erp.server.bi.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.BigDecimalUtil;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.common.web.service.RedisService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.entity.BiTargetManagementEntity;
import com.erp.model.bi.vo.*;
import com.erp.model.dmp.dto.*;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.enums.OrderStateEnum;
import com.erp.server.bi.enums.SettleMethodEnum;
import com.erp.server.bi.enums.TimeTypeEnum;
import com.erp.server.bi.listener.DmpOrderInfoExcelListener;
import com.erp.server.bi.mapper.DmpOrderInfoMapper;
import com.erp.server.bi.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
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


    @Override
    public PagingVO<DmpOrderInfoDTO> paging(PagingDTO<DmpOrderInfoSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        DmpOrderInfoSearchDTO params = dto.getParams();
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
    public TargetSaleSumVO sumSales(BiFilterDTO dto) {
        // 没有sku情况
        BigDecimal amount = BigDecimal.ZERO;
        QueryWrapper<DmpOrderInfoEntity> query = getDmpOrderInfoEntityQueryWrapper(dto);
        if (CollectionUtils.isEmpty(dto.getSku()) && ObjectUtils.isEmpty(dto.getHasNewSign())) {
            if (SettleMethodEnum.ORIGINAL_CURRENCY.equals(dto.getSettleMethod())) {
                if (BiFilterDTO.validOriginalCurrency(dto)){
                    query.select("sum(item_total) as item_total");
                }else {
                    return new TargetSaleSumVO(amount);
                }
            } else if (SettleMethodEnum.CNY_SETTLE.equals(dto.getSettleMethod())) {
                query.select("sum(item_total*settle_rate) as item_total");
            } else  {
                query.select("sum(item_total*currency_rate) as item_total");
            }
            DmpOrderInfoEntity dmpOrderInfoEntity = baseMapper.selectOne(query);
            amount = null != dmpOrderInfoEntity?dmpOrderInfoEntity.getItemTotal(): BigDecimal.ZERO;
        } else {
            // 条件存在sku的情况
            // 先查询订单号
            query.select("id");
            List<DmpOrderInfoEntity> list = baseMapper.selectList(query);
            if (CollectionUtils.isEmpty(list)) {
                return new TargetSaleSumVO(amount);
            }
            List<String> orderIds = list.stream().map(DmpOrderInfoEntity::getId).collect(Collectors.toList());
            // 根据订单号获取订单详情，筛选sku
            amount = dmpOrderItemService.sumSales(orderIds, dto);
        }
        return new TargetSaleSumVO(amount.setScale(4, BigDecimal.ROUND_DOWN));
    }

    private static QueryWrapper<DmpOrderInfoEntity> getDmpOrderInfoEntityQueryWrapper(BiFilterDTO dto) {
        QueryWrapper<DmpOrderInfoEntity> query = new QueryWrapper<>();
        query.ge(dto.getTimeType().equals(TimeTypeEnum.ORDER_TIME.getCode()), "platform_create_time", dto.getStartTime())
                .lt(dto.getTimeType().equals(TimeTypeEnum.ORDER_TIME.getCode()), "platform_create_time", dto.getEndTime().plusDays(1))
                // 订单时间字段
                .ge(dto.getTimeType().equals(TimeTypeEnum.DELIVERY_TIME.getCode()), "delivery_time", dto.getStartTime())
                .lt(dto.getTimeType().equals(TimeTypeEnum.DELIVERY_TIME.getCode()), "delivery_time", dto.getEndTime().plusDays(1))
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
                .last(StringUtils.isNotBlank(dto.getParam()), dto.getParam());
        return query;
    }

    @Override
    public TargetSaleCountVO countSalesVolume(BiFilterDTO dto) {
        Integer count = 0;
        QueryWrapper<DmpOrderInfoEntity> query = getDmpOrderInfoEntityQueryWrapper(dto);
        // 先查询订单号
        query.select("id")
                .last(StringUtils.isNotBlank(dto.getParam()), dto.getParam());
        List<DmpOrderInfoEntity> list = baseMapper.selectList(query);
        if (CollectionUtils.isEmpty(list)) {
            return new TargetSaleCountVO(count);
        }
        List<String> orderIds = list.stream().map(DmpOrderInfoEntity::getId).collect(Collectors.toList());
        // 根据订单号获取订单详情，筛选sku
        count = dmpOrderItemService.countSalesVolume(orderIds, dto.getSku());
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
                .last(StringUtils.isNotBlank(dto.getParam()), dto.getParam())
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
        queryWrapper.eq(DmpOrderInfoEntity::getPlatformOrderId,platformOrderId);
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }


    @Override
    public TargetAnalysisVO<QuarterMonthSalesVO> sumQuarterSales(BiFilterDTO dto) {
        // 获取年度开始时间和结束时间
        LocalDateTime start = LocalDateTime.of(LocalDate.from(dto.getStartTime().with(TemporalAdjusters.firstDayOfYear())), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.from(dto.getStartTime().with(TemporalAdjusters.lastDayOfYear())), LocalTime.MAX);
        // 查询目标销售额
        Map<Integer, BigDecimal> quarterTargetMap = new HashMap<>(4);
        // 统计目标销售额
        BiTargetManagementEntity target = biTargetManagementService.getMonthSales(start, end, dto.getParam(), 1);
        if(ObjectUtil.isNull(target)){
            return getQuarterResultList(quarterTargetMap, new HashMap<>(4),start.getYear());
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
        qw.select("SUM(COALESCE(item_total*currency_rate,0)) as item_total", groupByStr);
        List<DmpOrderInfoEntity> entityList = getOrderInfoEntities(dto, qw, start, end, groupByStr);
        if (CollectionUtils.isEmpty(entityList)) {
            return getQuarterResultList(quarterTargetMap,new HashMap<>(4),start.getYear());
        }
        Map<Integer, BigDecimal> quarterMap = entityList.stream().collect(Collectors.groupingBy(x -> (
                    // 按照季度分组
                    LocalDateUtil.date2LocalDate(flag ? x.getPlatformCreateTime() : x.getDeliveryTime()).getMonthValue() - 1) / 3 + 1,
                    // 对销售额进行求和
                    Collectors.reducing(BigDecimal.ZERO, DmpOrderInfoEntity::getItemTotal, BigDecimal::add)
            ));

        // 计算完成率
        return getQuarterResultList(quarterTargetMap,quarterMap,dto.getStartTime().getYear());
    }

    private List<DmpOrderInfoEntity> getOrderInfoEntities(BiFilterDTO dto, QueryWrapper<DmpOrderInfoEntity> qw, LocalDateTime start, LocalDateTime end, String groupStr) {
        boolean flag1 = TimeTypeEnum.ORDER_TIME.getCode() == dto.getTimeType();
        boolean flag2 = TimeTypeEnum.DELIVERY_TIME.getCode() == dto.getTimeType();
        qw.ge(flag1, "platform_create_time", start)
            .le(flag1, "platform_create_time", end)
            .ge(flag2, "delivery_time", start)
            .le(flag2, "delivery_time", end)
            .groupBy(StringUtils.isNotBlank(groupStr), groupStr)
            .last(StringUtils.isNotBlank(dto.getParam()), dto.getParam());
        List<DmpOrderInfoEntity> entityList = baseMapper.selectList(qw);
        return entityList;
    }

    @Override
    public TargetAnalysisVO<QuarterMonthSalesVolumeVO> sumQuarterSalesVolume(BiFilterDTO dto) {
        // 获取年度开始时间和结束时间
        int year = dto.getStartTime().getYear();
        LocalDateTime start = LocalDateTime.of(LocalDate.from(dto.getStartTime().with(TemporalAdjusters.firstDayOfYear())), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.from(dto.getStartTime().with(TemporalAdjusters.lastDayOfYear())), LocalTime.MAX);
        // 查询目标销量
        Map<Integer, Integer> quarterTargetMap = new HashMap<>(4);

        // 统计目标销量
        BiTargetManagementEntity target = biTargetManagementService.getMonthSales(start, end, dto.getParam(), 0);
        if(ObjectUtil.isNull(target)){
            return getQuarterVolumeResultList(quarterTargetMap, new HashMap<>(4),start.getYear());
        }
        // 四个季度
        quarterTargetMap.put(1, target.getJanuary().add(target.getFebruary().add(target.getMarch())).intValue());
        quarterTargetMap.put(2, target.getApril().add(target.getMay().add(target.getJune())).intValue());
        quarterTargetMap.put(3, target.getJuly().add(target.getAugust().add(target.getSeptember())).intValue());
        quarterTargetMap.put(4, target.getOctober().add(target.getNovember().add(target.getDecember())).intValue());

        // 查询销售额
        QueryWrapper<DmpOrderInfoEntity> qw = new QueryWrapper<>();
        boolean flag = TimeTypeEnum.ORDER_TIME.getCode() == dto.getTimeType();
        qw.select("id", "platform_create_time","delivery_time");
        List<DmpOrderInfoEntity> entityList = getOrderInfoEntities(dto, qw, start, end, null);
        if (CollectionUtils.isEmpty(entityList)) {
            return getQuarterVolumeResultList(quarterTargetMap, new HashMap<>(4), start.getYear());
        }
        List<String> orderIds = entityList.stream().map(DmpOrderInfoEntity::getId).collect(Collectors.toList());
        List<DmpOrderItemEntity> entityItemList = dmpOrderItemService.listByOrderInfoIds(orderIds);
        if (CollectionUtils.isEmpty(entityItemList)) {
            return getQuarterVolumeResultList(quarterTargetMap,new HashMap<>(4), start.getYear());
        }
        // 根据订单号的分组计算销量
        Map<String, Integer> orderQuantityMap = entityItemList.stream().collect(Collectors.groupingBy(DmpOrderItemEntity::getOrderId,
                Collectors.summingInt(DmpOrderItemEntity::getQuantity)));
        // 对订单号进行季度分组
        Map<Integer, Integer> quarterMap = entityList.stream().collect(Collectors.groupingBy(x -> (
                        // 按照季度分组
                        LocalDateUtil.date2LocalDate(flag ? x.getPlatformCreateTime() : x.getDeliveryTime()).getMonthValue() - 1) / 3 + 1,
                Collectors.summingInt(x -> orderQuantityMap.getOrDefault(x.getId(), 0)))
        );
        // 计算完成率
        return getQuarterVolumeResultList(quarterTargetMap,quarterMap, year);
    }

    @Override
    public TargetAnalysisVO<QuarterMonthSalesVO> sumMonthSales(BiFilterDTO dto) {
        // 获取月度开始时间和结束时间
        LocalDateTime start = LocalDateTime.of(LocalDate.from(dto.getStartTime().with(TemporalAdjusters.firstDayOfMonth())), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.from(dto.getEndTime().with(TemporalAdjusters.lastDayOfMonth())), LocalTime.MAX);
        // 查询目标销售额
        Map<Integer, BigDecimal> monthTargetMap = new HashMap<>(4);
        // 统计目标销量
        BiTargetManagementEntity target = biTargetManagementService.getMonthSales(start, end, dto.getParam(), 1);
        if(ObjectUtil.isNull(target)){
            return getMonthResultList(monthTargetMap, new HashMap<>(4),start.getYear());
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
        qw.select("SUM(COALESCE(item_total*currency_rate, 0)) as item_total", groupByStr);
        List<DmpOrderInfoEntity> entityList = getOrderInfoEntities(dto, qw, start, end, groupByStr);
        if (CollectionUtils.isEmpty(entityList)) {
            return getMonthResultList(monthTargetMap, new HashMap<>(4),start.getYear());
        }
        Map<Integer, BigDecimal> monthMap = entityList.stream().collect(Collectors.groupingBy(x ->
                        // 按照季度分组
                        LocalDateUtil.date2LocalDate(flag ? x.getPlatformCreateTime() : x.getDeliveryTime()).getMonthValue(),
                // 对销售额进行求和
                Collectors.reducing(BigDecimal.ZERO, DmpOrderInfoEntity::getItemTotal, BigDecimal::add)
        ));

        // 计算完成率
        return getMonthResultList(monthTargetMap,monthMap,start.getYear());
    }

    @Override
    public TargetAnalysisVO<QuarterMonthSalesVolumeVO> sumMonthSalesVolume(BiFilterDTO dto) {
        // 获取月度开始时间和结束时间
        LocalDateTime start = LocalDateTime.of(LocalDate.from(dto.getStartTime().with(TemporalAdjusters.firstDayOfMonth())), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.from(dto.getEndTime().with(TemporalAdjusters.lastDayOfMonth())), LocalTime.MAX);
        // 查询目标销量
        Map<Integer, Integer> quarterTargetMap = new HashMap<>(4);
        // 统计目标销量
        BiTargetManagementEntity target = biTargetManagementService.getMonthSales(start, end, dto.getParam(), 1);
        if(ObjectUtil.isNull(target)){
            return getMonthVolumeResultList(quarterTargetMap, new HashMap<>(4),start.getYear());
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
        List<DmpOrderInfoEntity> entityList = getOrderInfoEntities(dto, qw, start, end,null);
        if (CollectionUtils.isEmpty(entityList)) {
            return getMonthVolumeResultList(quarterTargetMap,new HashMap<>(4), start.getYear());
        }
        List<String> orderIds = entityList.stream().map(DmpOrderInfoEntity::getId).collect(Collectors.toList());
        List<DmpOrderItemEntity> entityItemList = dmpOrderItemService.listByOrderInfoIds(orderIds);
        if (CollectionUtils.isEmpty(entityItemList)) {
            return getMonthVolumeResultList(quarterTargetMap,new HashMap<>(4), start.getYear());
        }
        // 根据订单号的分组计算销量
        Map<String, Integer> orderQuantityMap = entityItemList.stream().collect(Collectors.groupingBy(DmpOrderItemEntity::getOrderId,
                Collectors.summingInt(DmpOrderItemEntity::getQuantity)));
        // 对订单号进行月度分组
        Map<Integer, Integer> quarterMap = entityList.stream().collect(Collectors.groupingBy(x ->
                        // 按照季度分组
                        LocalDateUtil.date2LocalDate(flag ? x.getPlatformCreateTime() : x.getDeliveryTime()).getMonthValue(),
                Collectors.summingInt(x -> orderQuantityMap.getOrDefault(x.getId(), 0)))
        );
        // 计算完成率
        return getMonthVolumeResultList(quarterTargetMap,quarterMap,dto.getStartTime().getYear());
    }

    @Override
    public List<SalesCompletionInfoVO> sumPlatformSalesCompletion(BiFilterDTO dto) {
        // 获取月度开始时间和结束时间
        LocalDateTime start = LocalDateTime.of(LocalDate.from(dto.getStartTime().with(TemporalAdjusters.firstDayOfMonth())), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.from(dto.getEndTime().with(TemporalAdjusters.lastDayOfMonth())), LocalTime.MAX);
        // 查询目标数据
        List<BiTargetManagementEntity> targetList = biTargetManagementService.getSales(start, end, dto.getParam());
        if(CollectionUtil.isEmpty(targetList)){
            return new ArrayList<>();
        }
        Map<Integer, List<BiTargetManagementEntity>> salesTypeMap = targetList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getTargetType));
        // 查询目标销售额
        List<BiTargetManagementEntity> salesList = salesTypeMap.get(1);
        Map<String, BigDecimal> targetSalesMap = new HashMap<>();
        if(CollectionUtil.isNotEmpty(salesList)){
            targetSalesMap = salesList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getPlatformName,
                    BigDecimalUtil.summingBigDecimal(BiTargetManagementEntity::getJanuary)));
        }

        // 查询目标销量
        List<BiTargetManagementEntity> salesVolumeList = salesTypeMap.get(0);
        Map<String, Integer> targetSalesVolumeMap = new HashMap<>();
        if(CollectionUtil.isNotEmpty(salesVolumeList)){
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
                        Collectors.reducing(BigDecimal.ZERO, x -> x.getItemTotal().multiply(x.getCurrencyRate()), BigDecimal::add)));
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
        List<BiTargetManagementEntity> targetList = biTargetManagementService.getSales(start, end, dto.getParam());
        if(CollectionUtil.isEmpty(targetList)){
            return new ArrayList<>();
        }
        Map<Integer, List<BiTargetManagementEntity>> salesTypeMap = targetList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getTargetType));
        // 查询目标销售额
        List<BiTargetManagementEntity> salesList = salesTypeMap.get(1);
        Map<String, BigDecimal> targetSalesMap = new HashMap<>();
        if(CollectionUtil.isNotEmpty(salesList)){
            targetSalesMap = salesList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getCategory,
                    BigDecimalUtil.summingBigDecimal(BiTargetManagementEntity::getJanuary)));
        }

        // 查询目标销量
        List<BiTargetManagementEntity> salesVolumeList = salesTypeMap.get(0);
        Map<String, Integer> targetSalesVolumeMap = new HashMap<>();
        if(CollectionUtil.isNotEmpty(salesVolumeList)){
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
        List<BiTargetManagementEntity> targetList = biTargetManagementService.getSales(start, end, dto.getParam());
        if(CollectionUtil.isEmpty(targetList)){
            return new ArrayList<>();
        }
        Map<Integer, List<BiTargetManagementEntity>> salesTypeMap = targetList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getTargetType));
        // 查询目标销售额
        List<BiTargetManagementEntity> salesList = salesTypeMap.get(1);
        Map<String, BigDecimal> targetSalesMap = new HashMap<>();
        if(CollectionUtil.isNotEmpty(salesList)){
            targetSalesMap = salesList.stream().collect(Collectors.groupingBy(x -> x.getProductType().toString(),
                    BigDecimalUtil.summingBigDecimal(BiTargetManagementEntity::getJanuary)));
        }

        // 查询目标销量
        List<BiTargetManagementEntity> salesVolumeList = salesTypeMap.get(0);
        Map<String, Integer> targetSalesVolumeMap = new HashMap<>();
        if(CollectionUtil.isNotEmpty(salesVolumeList)){
            targetSalesVolumeMap = salesVolumeList.stream().collect(Collectors.groupingBy(x -> x.getProductType().toString(),
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
        Map<String, Integer> salesVolumeMap = entityItemList.stream().filter(x -> StringUtils.isNotBlank(x.getSkuNo())).collect(Collectors.groupingBy(DmpOrderItemEntity::getSkuNo,
                Collectors.summingInt(DmpOrderItemEntity::getQuantity)));
        // 汇率map
        Map<String, BigDecimal> rateMap = orderInfoEntities.stream().collect(Collectors.toMap(DmpOrderInfoEntity::getId, DmpOrderInfoEntity::getCurrencyRate));
        // 根据sku的分组计算销售额
        Map<String, BigDecimal> saleAmountMap = entityItemList.stream().filter(x -> StringUtils.isNotBlank(x.getSkuNo())).collect(Collectors.groupingBy(DmpOrderItemEntity::getSkuNo,
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
        List<BiTargetManagementEntity> targetList = biTargetManagementService.getSales(start,end, dto.getParam());
        if(CollectionUtil.isEmpty(targetList)){
            return new ArrayList<>();
        }
        Map<Integer, List<BiTargetManagementEntity>> salesTypeMap = targetList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getTargetType));
        // 查询目标销售额
        List<BiTargetManagementEntity> salesList = salesTypeMap.get(1);
        Map<String, BigDecimal> targetSalesMap = new HashMap<>();
        if(CollectionUtil.isNotEmpty(salesList)){
            targetSalesMap = salesList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getProductPosition,
                    BigDecimalUtil.summingBigDecimal(BiTargetManagementEntity::getJanuary)));
        }

        // 查询目标销量
        List<BiTargetManagementEntity> salesVolumeList = salesTypeMap.get(0);
        Map<String, Integer> targetSalesVolumeMap = new HashMap<>();
        if(CollectionUtil.isNotEmpty(salesVolumeList)){
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
                Collectors.summingInt(x -> skuSalesVolumeMap.get(x.getSkuNo()))));
        
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
                BigDecimalUtil.summingBigDecimal(x -> skuSaleAmountMap.get(x.getSkuNo()))));

        List<SalesCompletionInfoVO> rankResult = assemblyResult(dto, targetSalesMap, targetSalesVolumeMap, null, salesVolumeMap, saleAmountMap);
        return rankResult;
    }

    @Override
    public List<SalesCompletionInfoVO> sumProductTypeCompletion(BiFilterDTO dto) {
        // 获取月度开始时间和结束时间
        LocalDateTime start = LocalDateTime.of(LocalDate.from(dto.getStartTime().with(TemporalAdjusters.firstDayOfMonth())), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.from(dto.getEndTime().with(TemporalAdjusters.lastDayOfMonth())), LocalTime.MAX);
        List<BiTargetManagementEntity> targetList = biTargetManagementService.getSales(start,end, dto.getParam());
        if(CollectionUtil.isEmpty(targetList)){
            return new ArrayList<>();
        }
        Map<Integer, List<BiTargetManagementEntity>> salesTypeMap = targetList.stream().collect(Collectors.groupingBy(BiTargetManagementEntity::getTargetType));
        // 查询目标销售额
        List<BiTargetManagementEntity> salesList = salesTypeMap.get(1);
        Map<String, BigDecimal> targetSalesMap = salesList.stream().collect(Collectors.groupingBy(x -> x.getProductType().toString(),
                BigDecimalUtil.summingBigDecimal(BiTargetManagementEntity::getJanuary)));
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
                Collectors.summingInt(x -> skuSalesVolumeMap.get(x.getSkuNo()))));

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
                BigDecimalUtil.summingBigDecimal(x -> skuSaleAmountMap.get(x.getSkuNo()))));

        List<SalesCompletionInfoVO> rankResult = assemblyResult(dto, targetSalesMap, targetSalesVolumeMap, null, salesVolumeMap, saleAmountMap);
        return rankResult;
    }

    @Override
    public TargetSaleAndYoySumVO getSalesAndYoy(BiFilterDTO dto) {
        // 查询当期销售额
        dto.setEndTime(dto.getEndTime());
        TargetSaleSumVO currentVo = sumSales(dto);
        BigDecimal currentAmount = currentVo.getValue();
        if (BigDecimal.ZERO.compareTo(currentAmount)  == 0){
            return new TargetSaleAndYoySumVO();
        }
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime().plusDays(1);
        // 查询上一个周期销售额 环比
        Duration duration = Duration.between(startTime,endTime);
        LocalDateTime preStartTime = startTime.minusDays(duration.toDays());
        dto.setStartTime(preStartTime);
        dto.setEndTime(startTime.minusDays(1));
        TargetSaleSumVO ringVo = sumSales(dto);
        // 查询去年同周期 同比
        dto.setStartTime(startTime.minusYears(1));
        dto.setEndTime(endTime.minusYears(1));
        TargetSaleSumVO yoyVo = sumSales(dto);

        return new TargetSaleAndYoySumVO(currentVo, ringVo, yoyVo);
    }

    @Override
    public TargetSaleAndYoyCountVO countSalesVolumeAndYoy(BiFilterDTO dto) {
        // 查询当期销售额
        dto.setEndTime(dto.getEndTime());
        TargetSaleCountVO currentVo = countSalesVolume(dto);
        Integer currentAmount = currentVo.getValue();
        if (0 == currentAmount){
            return new TargetSaleAndYoyCountVO();
        }
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime().plusDays(1);
        // 查询上一个周期销售额 环比
        Duration duration = Duration.between(startTime,endTime);
        LocalDateTime preStartTime = startTime.minusDays(duration.toDays());
        dto.setStartTime(preStartTime);
        dto.setEndTime(startTime.minusDays(1));
        TargetSaleCountVO ringVo = countSalesVolume(dto);
        // 查询去年同周期 同比
        dto.setStartTime(startTime.minusYears(1));
        dto.setEndTime(endTime.minusYears(1));
        TargetSaleCountVO yoyVo = countSalesVolume(dto);

        return new TargetSaleAndYoyCountVO(currentVo, ringVo, yoyVo);
    }

    @Override
    public TargetSaleAndYoyCountVO countOrderQuantityAndYoy(BiFilterDTO dto) {
        // 查询当期销售额
        dto.setEndTime(dto.getEndTime());
        TargetSaleCountVO currentVo = countOrderQuantity(dto);
        Integer currentAmount = currentVo.getValue();
        if (0 == currentAmount){
            return new TargetSaleAndYoyCountVO();
        }
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime().plusDays(1);
        // 查询上一个周期销售额 环比
        Duration duration = Duration.between(startTime,endTime);
        LocalDateTime preStartTime = startTime.minusDays(duration.toDays());
        dto.setStartTime(preStartTime);
        dto.setEndTime(startTime.minusDays(1));
        TargetSaleCountVO ringVo = countOrderQuantity(dto);
        // 查询去年同周期 同比
        dto.setStartTime(startTime.minusYears(1));
        dto.setEndTime(endTime.minusYears(1));
        TargetSaleCountVO yoyVo = countOrderQuantity(dto);

        return new TargetSaleAndYoyCountVO(currentVo, ringVo, yoyVo);
    }

    @Override
    public TargetSaleAndYoySumVO countRefundRateAndYoy(BiFilterDTO dto) {
        // 查询当期销售额
        dto.setEndTime(dto.getEndTime().plusMinutes(1));
        TargetSaleSumVO currentVo = countRefundRate(dto);
        BigDecimal currentAmount = currentVo.getValue();
        if (BigDecimal.ZERO.compareTo(currentAmount)  == 0){
            return new TargetSaleAndYoySumVO();
        }
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime().plusDays(1);
        // 查询上一个周期销售额 环比
        Duration duration = Duration.between(startTime,endTime);
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
    public TargetSaleAndYoySumVO countRefundAmountAndYoy(BiFilterDTO dto) {
        // 查询当期销售额
        dto.setEndTime(dto.getEndTime().plusMinutes(1));
        TargetSaleSumVO currentVo = countRefundAmount(dto);
        BigDecimal currentAmount = currentVo.getValue();
        if (BigDecimal.ZERO.compareTo(currentAmount)  == 0){
            return new TargetSaleAndYoySumVO();
        }
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime().plusDays(1);
        // 查询上一个周期销售额 环比
        Duration duration = Duration.between(startTime,endTime);
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
    public TargetSaleAndYoyCountVO countRefundOrderNumAndYoy(BiFilterDTO dto) {
        // 查询当期销售额
        dto.setEndTime(dto.getEndTime().plusMinutes(1));
        TargetSaleCountVO currentVo = countRefundOrderNum(dto);
        Integer currentAmount = currentVo.getValue();
        if (0 == currentAmount){
            return new TargetSaleAndYoyCountVO();
        }
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime().plusDays(1);
        // 查询上一个周期销售额 环比
        Duration duration = Duration.between(startTime,endTime);
        LocalDateTime preStartTime = startTime.minusDays(duration.toDays());
        dto.setStartTime(preStartTime);
        dto.setEndTime(startTime.minusDays(1));
        TargetSaleCountVO ringVo = countRefundOrderNum(dto);
        // 查询去年同周期 同比
        dto.setStartTime(startTime.minusYears(1));
        dto.setEndTime(endTime.minusYears(1));
        TargetSaleCountVO yoyVo = countRefundOrderNum(dto);

        return new TargetSaleAndYoyCountVO(currentVo, ringVo, yoyVo);
    }

    @Override
    public Boolean importOrderFile(MultipartFile excelFile, Integer importType, HttpServletResponse response) {
        //系统中已存在的订单
        List<DmpOrderInfoEntity> orderList = this.list();
        DmpOrderInfoExcelListener excelListenerUtil = new DmpOrderInfoExcelListener(importType,orderList,plmTaskFeign,dmpOrderItemService, this, dmpShopInfoService, sysUserFeign);
        try {
            EasyExcel.read(excelFile.getInputStream(), DmpOrderInfoImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
            List<DmpOrderInfoImportExcelDTO> list = excelListenerUtil.getDateList();
            if (list.size() > 0) {
                StringBuffer sb = new StringBuffer();
                String excelPath = "excel/dmpOrderInfo.xlsx";
                String name = "dmpOrderInfo";
                String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
                sb.append(date);
                sb.append(name);
                new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
                return false;
            }
        } catch (IOException e) {
            throw new ServiceException(ApiError.Default);
        }
        return  true;
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
                .ge(2 == type && ObjectUtil.isNotEmpty(dto.getStartTime()), DmpOrderInfoEntity::getPlatformCreateTime, dto.getStartTime())
                .le(2 == type && ObjectUtil.isNotEmpty(dto.getEndTime()), DmpOrderInfoEntity::getPlatformCreateTime, dto.getEndTime())
                .list();
        Map<Integer, BigDecimal> resultMap = new HashMap<>();
        if (0 == type){
            // 月份
            resultMap = list.stream().collect(Collectors.groupingBy(x -> LocalDateUtil.date2LocalDate(x.getPlatformCreateTime()).getMonthValue(),
                    BigDecimalUtil.summingBigDecimal(x -> x.getItemTotal().multiply(x.getCurrencyRate()).setScale(4, BigDecimal.ROUND_DOWN))));
        }else if (1 == type){
            // 季度
            resultMap = list.stream().collect(Collectors.groupingBy(x -> (LocalDateUtil.date2LocalDate(x.getPlatformCreateTime()).getMonthValue()-1) / 3 + 1,
                    BigDecimalUtil.summingBigDecimal(x -> x.getItemTotal().multiply(x.getCurrencyRate()).setScale(4, BigDecimal.ROUND_DOWN))));
        }else {
            // 年度
            resultMap = list.stream().collect(Collectors.groupingBy(x -> LocalDateUtil.date2LocalDate(x.getPlatformCreateTime()).getYear(),
                    BigDecimalUtil.summingBigDecimal(x -> x.getItemTotal().multiply(x.getCurrencyRate()).setScale(4, BigDecimal.ROUND_DOWN))));
        }
        return resultMap;
    }

    private static List<SalesCompletionInfoVO> assemblyResult(BiFilterDTO dto, Map<String, BigDecimal> targetSalesMap, Map<String, Integer> targetSalesVolumeMap,
                                                              Map<String, String> skuMap, Map<String, Integer> salesVolumeMap, Map<String, BigDecimal> saleAmountMap) {
        // 组合数据计算目标达成率
        Set<String> keys = new HashSet<>();
        keys.addAll(targetSalesMap.keySet());
        keys.addAll(targetSalesVolumeMap.keySet());
        List<SalesCompletionInfoVO> resultList = keys
                .stream().map(x ->
                        new SalesCompletionInfoVO(x, targetSalesMap.get(x), targetSalesVolumeMap.get(x),
                                saleAmountMap.get(x), salesVolumeMap.get(x),  null != skuMap ? skuMap.getOrDefault(x, "") : "")
                ).collect(Collectors.toList());

        AtomicInteger rankIndex = new AtomicInteger(1);
        List<SalesCompletionInfoVO> rankResult = resultList.stream()
                .sorted(Comparator.comparing(SalesCompletionInfoVO::getSalesAmountCompletionRate)
                        .thenComparing(SalesCompletionInfoVO::getSalesVolumeCompletionRate))
                .peek(x -> x.setRanking(rankIndex.getAndIncrement()))
                .filter(x ->x.getRanking() <= dto.getRankNum())
                .collect(Collectors.toList());
        return rankResult;
    }

    /**
     * 季度销量结构构建
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
    private void dmpOrderInfoHand( List<DmpOrderInfoDTO> records) {
       if (CollectionUtils.isEmpty(records)) {
           return;
       }
        List<String> ids = records.stream().map(DmpOrderInfoDTO::getId).collect(Collectors.toList());
        List<DmpOrderItemEntity> dmpOrderItemList = dmpOrderItemService.listByOrderInfoIds(ids);
        records.forEach(obj -> {
            List<DmpOrderItemEntity> itemList = dmpOrderItemList.stream().filter(e -> obj.getId().equals(e.getOrderId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(itemList)) {
                List<DmpOrderItemDTO> itemResultList = BeanMapperUtils.copyList(DmpOrderItemDTO.class, itemList);
                itemResultList.stream().forEach(e -> e.setSellAmount(MathUtil.multiply(e.getSellPrice(),e.getQuantity())));
                obj.setChildren(itemResultList);
            }
            obj.setOrderStateName(OrderStateEnum.getName(obj.getOrderState()));
            obj.setCorrectionStatusName(OrderStateEnum.getName(obj.getCorrectionStatus()));
        });
    }
}




