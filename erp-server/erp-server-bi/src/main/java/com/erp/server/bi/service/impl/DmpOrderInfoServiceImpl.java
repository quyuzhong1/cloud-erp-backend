package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.common.web.service.RedisService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.*;
import com.erp.model.bi.vo.TargetSaleCountVO;
import com.erp.model.bi.vo.TargetSaleSumVO;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.server.bi.enums.OrderStateEnum;
import com.erp.server.bi.enums.SettleMethodEnum;
import com.erp.server.bi.enums.TimeTypeEnum;
import com.erp.server.bi.mapper.DmpOrderInfoMapper;
import com.erp.server.bi.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
        List<DmpOrderInfoDTO> list = baseMapper.getAllDmpOrderInfo(dto);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(obj ->obj.setOrderStateName(OrderStateEnum.getName(obj.getOrderState())));
        //导出销售数据
        List<DmpOrderInfoExcelDTO> excelList = BeanMapperUtils.copyList(DmpOrderInfoExcelDTO.class, list);
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
            amount = dmpOrderInfoEntity.getItemTotal();
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
                .le(dto.getTimeType().equals(TimeTypeEnum.ORDER_TIME.getCode()), "platform_create_time", dto.getEndTime())
                // 订单时间字段
                .ge(dto.getTimeType().equals(TimeTypeEnum.DELIVERY_TIME.getCode()), "delivery_time", dto.getStartTime())
                .le(dto.getTimeType().equals(TimeTypeEnum.DELIVERY_TIME.getCode()), "delivery_time", dto.getEndTime())
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
                .in(CollectionUtils.isNotEmpty(dto.getUserId()), "charge_name_id", dto.getUserId())
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




