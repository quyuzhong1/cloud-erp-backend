package com.erp.server.dmp.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.anno.ParamData;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.dto.AfterSaleDTO;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.dto.DmpSoInfoDTO;
import com.erp.model.dmp.entity.DmpSoDetailEntity;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.model.dmp.entity.DmpSoReceiverEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.DmpOrderReturnStatusEnum;
import com.erp.model.dmp.gyy.GyyOrderEntity;
import com.erp.model.dmp.gyy.bean.DeliverysBean;
import com.erp.model.dmp.gyy.bean.DetailsBean;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.erp.sdk.oms.amz.spapi.model.orders.Order;
import com.erp.server.dmp.mapper.DmpSoInfoMapper;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.DmpSoDetailService;
import com.erp.server.dmp.service.DmpSoInfoService;
import com.erp.server.dmp.service.DmpSoReceiverService;
import io.seata.spring.annotation.GlobalTransactional;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 中台销售订单表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-24
 */
@Slf4j
@Service
public class DmpSoInfoServiceImpl extends SuperServiceImpl<DmpSoInfoMapper, DmpSoInfoEntity> implements DmpSoInfoService {

    @Resource
    private DmpSoDetailService dmpSoDetailService;
    @Resource
    private DmpSoReceiverService dmpSoReceiverService;
    @Resource
    private MongoService mongoService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpSoInfoDTO.AddDTO addDTO) {
        DmpSoInfoEntity dmpSoInfoEntity = new DmpSoInfoEntity();
        BeanMapperUtils.copy(addDTO, dmpSoInfoEntity);

        // 数据处理
        handleData(dmpSoInfoEntity);

        log.info("开始新增中台销售订单表");
        boolean save = super.save(dmpSoInfoEntity);
        if (!save) {
            throw new ServiceException("中台销售订单表保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "中台销售订单表", dmpSoInfoEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpSoInfoEntity.getId(), dmpSoInfoEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpSoInfoDTO.UpdateDTO updateDTO) {
        DmpSoInfoEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "中台销售订单表"));
        DmpSoInfoEntity dmpSoInfoEntity = BeanMapperUtils.map(DmpSoInfoEntity.class, updateDTO);

        // 数据处理
        handleData(dmpSoInfoEntity);
        log.info("编辑 开始修改中台销售订单表数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpSoInfoEntity);
        if (!save) {
            throw new ServiceException("中台销售订单表保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录中台销售订单表日志数据，id：【{}】", dmpSoInfoEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpSoInfoEntity.getId(), "中台销售订单表");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(DmpSoInfoEntity dmpSoInfoEntity) {
        // TODO 验证数据 & 数据赋值
    }

    @Override
    public void addGyyOrder(List<GyyOrderEntity> mongoData) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (GyyOrderEntity gyyOrderEntity : mongoData) {
            DmpSoInfoEntity entity = new DmpSoInfoEntity();
            if (!"销售订单".equals(gyyOrderEntity.getOrderTypeName())) {
                continue;
            }
            String mainId = IdWorker.getIdStr();
            entity.setId(mainId);
            if (CharSequenceUtil.isNotBlank(gyyOrderEntity.getCreatetime())) {
                entity.setPlatformCreateTime(LocalDateTime.parse(gyyOrderEntity.getCreatetime(), formatter));
            }
            if (CharSequenceUtil.isNotBlank(gyyOrderEntity.getModifytime())) {
                entity.setPlatformUpdateTime(LocalDateTime.parse(gyyOrderEntity.getModifytime(), formatter));
            }
            entity.setSourceSystem("gyy");
            entity.setSourcePlatform("gyy");
            entity.setThirdCode(gyyOrderEntity.getCode());
            entity.setPlatformCode(gyyOrderEntity.getPlatformCode());
            entity.setInvalidStatus(gyyOrderEntity.getCancle());
            entity.setApproveStatus(ApproveStatusEnum.APPROVE.getStatus());
            if (gyyOrderEntity.getDeliveryState() == 1) {
                entity.setDeliveryStatus(SoB2cBillStatusEnum.ENUM_PARTIAL_SHIPPED.getCode());
            } else if (gyyOrderEntity.getDeliveryState() == 2) {
                entity.setDeliveryStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
            } else {
                entity.setDeliveryStatus(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
            }

            if (gyyOrderEntity.getRefundState() == 1) {
                entity.setReturnStatus(DmpOrderReturnStatusEnum.PARTIAL_RETURN.getCode());
            } else if (gyyOrderEntity.getDeliveryState() == 2) {
                entity.setReturnStatus(DmpOrderReturnStatusEnum.ORDER_RETURN.getCode());
            } else {
                entity.setReturnStatus(DmpOrderReturnStatusEnum.NOT_RETURN.getCode());
            }

            entity.setPlatformOriginalStatus(String.valueOf(gyyOrderEntity.getDeliveryState()));
            entity.setShopId(gyyOrderEntity.getShopCode());
            entity.setShopName(gyyOrderEntity.getShopName());
            entity.setSellRemark(gyyOrderEntity.getSellerMemo());
            entity.setBuyerRemark(gyyOrderEntity.getBuyerMemo());
            if (CharSequenceUtil.isNotBlank(gyyOrderEntity.getPaytime())) {
                entity.setPayTime(LocalDateTime.parse(gyyOrderEntity.getPaytime(), formatter));
            }
            entity.setPayStatus(Boolean.TRUE);
            entity.setPayMethod("");
            entity.setCurrencyCode(CurrencyEnum.CNY.getCurrencyCode());
            entity.setExchangeRate(BigDecimal.ZERO);
            entity.setPayAmount(gyyOrderEntity.getPayment());
            entity.setAllAmount(gyyOrderEntity.getPaymentAmount());
            entity.setShippingAmount(gyyOrderEntity.getPostFee());
            entity.setPlatformCost(gyyOrderEntity.getOtherServiceFee());
            entity.setDeliveryTime(null);
            List<DeliverysBean> deliverys = gyyOrderEntity.getDeliverys();
            if (CollUtil.isNotEmpty(deliverys)) {
                entity.setLogisticsCode(deliverys.get(0).getMailNo());
            }
            entity.setLogisticsName(gyyOrderEntity.getExpressName());
            if (gyyOrderEntity.getApprove()) {
                entity.setApproveStatus(ApproveStatusEnum.APPROVE.getStatus());
            } else {
                entity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
            }
            entity.setTotalDiscount(gyyOrderEntity.getDiscountFee());
            BigDecimal totalCancelGoodsAmount = gyyOrderEntity.getDetails().stream()
                    .filter(req -> req.getRefund() == 1 || req.getCancel())
                    .map(req -> req.getPrice().multiply(MathUtil.valueOf(req.getQty())))
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);
            entity.setTotalCancelGoodsAmount(totalCancelGoodsAmount);
            entity.setCancelGoodsCurrency("CNY");
            try {
                this.save(entity);
            } catch (Exception e) {
                log.error("数据：" + JSONUtil.toJsonStr(entity));
                e.printStackTrace();
                return;
            }
            try {
                dmpSoReceiverService.save(this.receiverHandler(gyyOrderEntity, mainId));
            } catch (Exception e) {
                log.error("数据：" + JSONUtil.toJsonStr(gyyOrderEntity));
                e.printStackTrace();
                return;
            }
            try {
                dmpSoDetailService.saveBatch(this.detailHandler(gyyOrderEntity.getDetails(), mainId));
            } catch (Exception e) {
                log.error("数据：" + JSONUtil.toJsonStr(entity));
                e.printStackTrace();
                return;
            }

        }
    }

    private List<DmpSoDetailEntity> detailHandler(List<DetailsBean> detailsBeans, String mainId) {
        List<DmpSoDetailEntity> list = new ArrayList<>();
        for (DetailsBean detailsBean : detailsBeans) {
            DmpSoDetailEntity dmpSoDetailEntity = new DmpSoDetailEntity();
            dmpSoDetailEntity.setMainId(mainId);
            dmpSoDetailEntity.setThirdDetailId(detailsBean.getItemCode());
            dmpSoDetailEntity.setSkuNo(detailsBean.getItemCode());
            dmpSoDetailEntity.setPlatformSku(detailsBean.getItemCode());
            dmpSoDetailEntity.setQty(detailsBean.getQty());
            dmpSoDetailEntity.setSkuName(detailsBean.getItemName());
            dmpSoDetailEntity.setIsGift(detailsBean.getIsGift());
            dmpSoDetailEntity.setItemRemark(detailsBean.getNote());
            dmpSoDetailEntity.setExchangeRate(detailsBean.getExchangeRate());
            dmpSoDetailEntity.setSellPriceOrigin(detailsBean.getPrice());
            dmpSoDetailEntity.setDiscountAmount(detailsBean.getDiscountFee());
            dmpSoDetailEntity.setSellPrice(detailsBean.getPrice().subtract((detailsBean.getDiscountFee().divide(MathUtil.valueOf(detailsBean.getQty()), 4, RoundingMode.DOWN))));
            dmpSoDetailEntity.setAfterAmount(detailsBean.getAmountAfter());
            dmpSoDetailEntity.setShippingCost(detailsBean.getPostFee());
            dmpSoDetailEntity.setCurrencyCode("CNY");
            list.add(dmpSoDetailEntity);
        }
        return list;
    }

    private DmpSoReceiverEntity receiverHandler(GyyOrderEntity gyyOrderEntity, String mainId) {
        DmpSoReceiverEntity soReceiverEntity = new DmpSoReceiverEntity();
        soReceiverEntity.setMainId(mainId);
        soReceiverEntity.setCountry("CN");
        soReceiverEntity.setBuyerId(gyyOrderEntity.getVipCode());
        soReceiverEntity.setBuyerName(gyyOrderEntity.getVipName());
        soReceiverEntity.setReceiverName(gyyOrderEntity.getReceiverName());
        soReceiverEntity.setReceiverTelNumber(gyyOrderEntity.getReceiverMobile());
        soReceiverEntity.setPostCode(gyyOrderEntity.getReceiverZip());
        if (CharSequenceUtil.isNotBlank(gyyOrderEntity.getReceiverArea())) {
            String[] split = gyyOrderEntity.getReceiverArea().split("-");
            if (split.length > 0) {
                soReceiverEntity.setProvince(split[0]);
            }
            if (split.length > 1) {
                soReceiverEntity.setCity(split[1]);
            }
            if (split.length > 2) {
                soReceiverEntity.setDistrict(split[2]);
            }
        }
        soReceiverEntity.setFullAddress(gyyOrderEntity.getReceiverAddress());
        soReceiverEntity.setMainStreet(gyyOrderEntity.getReceiverAddress());
        soReceiverEntity.setEmail(gyyOrderEntity.getVipEmail());
        return soReceiverEntity;
    }

    @Override
    public List<DmpSoInfoEntity> findSoMissingDetail(LocalDateTime startTime, LocalDateTime endTime, String sourceSystem, String nextLevelId) {
        return baseMapper.findSoMissingDetail(startTime, endTime, sourceSystem, nextLevelId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lxSoUpdate() {
        List<ParamData> paramDataList = new ArrayList<>();
        List<Map<String, Object>> findMongoData = mongoService.findMongoData(paramDataList, "lingxing_so_info");
        if (CollUtil.isEmpty(findMongoData)) {
            log.info("空内容");
            return;
        }
        // 领星店铺和ERP映射
        Map<String, AmazonShopInfoDTO> shopInfoMap = new HashMap<>();
        AmazonShopInfoDTO shopInfoDTOMX13 = new AmazonShopInfoDTO();
        shopInfoDTOMX13.setPlatformShopCode("AVPQ7ZCW9ENDY");
        shopInfoDTOMX13.setName("墨西哥13站");
        shopInfoDTOMX13.setId("1778673027341357058");
        shopInfoMap.put("美13-徐邦德-MX",shopInfoDTOMX13);
        AmazonShopInfoDTO shopInfoDTOCA13 = new AmazonShopInfoDTO();
        shopInfoDTOCA13.setPlatformShopCode("AVPQ7ZCW9ENDY");
        shopInfoDTOCA13.setName("加拿大13站");
        shopInfoDTOCA13.setId("1736934612648595458");
        shopInfoMap.put("美13-孙婉珊-CA", shopInfoDTOCA13);

        shopInfoDTOCA13.setPlatformShopCode("A250OY6BJRDCID");
        shopInfoDTOCA13.setName("阿联酋11");
        shopInfoDTOCA13.setId("1735593579214016513");
        shopInfoMap.put("阿联酋11-AE", shopInfoDTOCA13);

        // 按店铺分组
        Map<String, List<Map<String, Object>>> shopGroup = findMongoData.stream().collect(Collectors.groupingBy(e -> e.getOrDefault("店铺", "").toString()));
        // 按订单分组
        List<String> orderIds = findMongoData.stream().map(e -> e.getOrDefault("订单号", "").toString()).distinct().collect(Collectors.toList());
        List<DmpSoInfoEntity> list = this.lambdaQuery()
                .in(DmpSoInfoEntity::getThirdCode, orderIds)
                .list();
        List<String> existOrderId = list.stream().map(DmpSoInfoEntity::getThirdCode).distinct().collect(Collectors.toList());
        for (Map.Entry<String, List<Map<String, Object>>> entry : shopGroup.entrySet()) {
            // 按订单分组
            Map<String, List<Map<String, Object>>> orderGroup = entry.getValue().stream().collect(Collectors.groupingBy(e -> e.getOrDefault("订单号", "").toString()));
            for (Map.Entry<String, List<Map<String, Object>>> orderEntry : orderGroup.entrySet()) {
                if (existOrderId.contains(orderEntry.getKey())){
                    log.warn("订单已存在：跳过：{}", orderEntry.getKey());
                }
                // 校验店铺映射
                AmazonShopInfoDTO shopInfoDTO = shopInfoMap.get(entry.getKey());
                if (null == shopInfoDTO){
                    ServiceException.runError("店铺映射不存在");
                }
                List<Map<String, Object>> orderItemMongoList = orderEntry.getValue();
                // 组合主表
                Map<String, Object> itemMap = orderItemMongoList.get(0);
                DmpSoInfoEntity mainEntity = lxConvertDmpSoInfo(itemMap, shopInfoDTO);
                boolean save = this.save(mainEntity);
                if (!save){
                    ServiceException.runError("主订单保持失败");
                }
                List<DmpSoDetailEntity> detailList = orderItemMongoList.stream()
                        .map(e -> lxConvertDmpSoDetail(e, mainEntity)).collect(Collectors.toList());
                boolean detailSave = dmpSoDetailService.saveBatch(detailList);
                if (!detailSave){
                    ServiceException.runError("明细保存失败");
                }
                DmpSoReceiverEntity receiverEntity = lxConvertDmpSoReceive(itemMap, mainEntity);
                boolean saveReceiver = dmpSoReceiverService.save(receiverEntity);
                if (!saveReceiver){
                    ServiceException.runError("收货人保存失败");
                }
            }
        }

    }


    /**
     * 领星导出转换DMP主表
     */
    private DmpSoInfoEntity lxConvertDmpSoInfo(Map<String, Object> itemMap, AmazonShopInfoDTO shopInfoDTO) {
        DmpSoInfoEntity entity = new DmpSoInfoEntity();
        entity.setSourcePlatform(PlatformDictEnum.AMAZON.getCode());
        entity.setSourceSystem(DmpBasicSystemCodeEnum.LING_XING.getCode());

        String createTimeStr = itemMap.getOrDefault("订购日期", "").toString();
        String updateTimeStr = itemMap.getOrDefault("更新时间", "").toString();

        LocalDateTime purchaseLocalDateTime = LocalDateTimeUtil.parse(createTimeStr, "yyyy-MM-dd HH:mm:ss");
        entity.setPlatformCreateTime(purchaseLocalDateTime);

        LocalDateTime updateTime = LocalDateTimeUtil.parse(updateTimeStr, "yyyy-MM-dd HH:mm:ss");
        entity.setPlatformUpdateTime(updateTime);

        String thirdCode = itemMap.getOrDefault("订单号", "").toString();
        entity.setThirdCode(thirdCode);
        entity.setPlatformCode(thirdCode);

        String sourceOrderStatus = itemMap.getOrDefault("订单状态", "").toString();
        Order sourceOrder = new Order();
        sourceOrder.setOrderStatus(sourceOrderStatus);
        entity.setInvalidStatus(sourceOrder.convertCancel());
        // 是否取消（false未取消，true已取消）
        entity.setIsCancel(sourceOrder.convertCancel());
        // 订单状态 waitSubmit.待提交 approveIng.审核中 reject.审核不通过 approve.已审核
        entity.setOrderStatus(sourceOrder.convertBillStatus());
        // 平台原始状态
        entity.setPlatformOriginalStatus(sourceOrderStatus);
        // 店铺编号
        entity.setShopId(shopInfoDTO.getId());
        // 店铺名称
        entity.setShopName(shopInfoDTO.getName());
        // 付款状态 （false未付款，true已付款）
        entity.setPayStatus(SoB2cPayStatusEnum.ENUM_PAID.getCode().equalsIgnoreCase(sourceOrder.convertPayStatus()));
        // 支付时间
        entity.setPayTime("payment".equalsIgnoreCase(sourceOrder.convertPayStatus()) ? null : purchaseLocalDateTime);
        String currencyCode = itemMap.getOrDefault("订单币种", "").toString();
        // 币种编码
        entity.setCurrencyCode(currencyCode);
        // 订单总金额
        String allAmountStr = itemMap.getOrDefault("订单总金额", "").toString();
        BigDecimal allAmount = new BigDecimal(allAmountStr);
        entity.setAllAmount(allAmount);
        entity.setPayAmount(allAmount);
        // 平台费
        String platformCostStr = itemMap.getOrDefault("平台费", "").toString().replaceAll("-", "");
        if (StringUtils.isNotBlank(platformCostStr) && !"null".equalsIgnoreCase(platformCostStr)) {
            BigDecimal platformCost = new BigDecimal(platformCostStr);
            entity.setPlatformCost(platformCost);
        }

        Object deliveryTimeObj = itemMap.getOrDefault("发货时间", "");
        if (null != deliveryTimeObj){
            String deliveryTimeStr = deliveryTimeObj.toString();
            if (StringUtils.isNotBlank(deliveryTimeStr) && !"null".equalsIgnoreCase(deliveryTimeStr)){
                LocalDateTime deliveryTime = LocalDateTimeUtil.parse(deliveryTimeStr, "yyyy-MM-dd HH:mm:ss");
                entity.setDeliveryTime(deliveryTime);
            }
        }

        // 审核状态
        entity.setApproveStatus(sourceOrder.convertApproveStatusStr());
        // 拓展字段
        // 标签json
        Map<String, String> lableMap = new HashMap<>();
        String fulfillmentChannelStr = itemMap.getOrDefault("订单类型", "").toString();
        if (Order.FulfillmentChannelEnum.AFN.getValue().equalsIgnoreCase(fulfillmentChannelStr)) {
            lableMap.put("fulfillmentChannel", "AFN");
        }
        if (Order.OrderStatusEnum.UNFULFILLABLE.getValue().equalsIgnoreCase(sourceOrder.getOrderStatus())) {
            lableMap.put("amazonStatus", "Unfulfillable");
        }
        entity.setExtendData(JSON.toJSONString(lableMap));
        return entity;
    }

    /**
     * 领星导出转换DMP明细表
     */
    private DmpSoDetailEntity lxConvertDmpSoDetail(Map<String, Object> itemMap, DmpSoInfoEntity soInfoEntity) {
        DmpSoDetailEntity entity = new DmpSoDetailEntity();
        entity.setMainId(soInfoEntity.getId());
        // 订单明细ID
        String orderItemId = itemMap.getOrDefault("Order Item ID", "").toString();
        entity.setThirdDetailId(orderItemId);
        entity.setPlatformDetailId(orderItemId);
        // msku
        String MSKU = itemMap.getOrDefault("MSKU", "").toString();
        entity.setPlatformSku(MSKU);
        // ASIN
        String ASIN = itemMap.getOrDefault("ASIN", "").toString();
        entity.setPlatformSpuNo(ASIN);
        // 数量
        String qty = itemMap.getOrDefault("数量", "").toString();
        entity.setQty(Integer.parseInt(qty));
        // 标题
        String name = itemMap.getOrDefault("标题", "").toString();
        entity.setSkuName(name);
        //单价
        String priceStr = itemMap.getOrDefault("单价", "").toString();
        BigDecimal price = new BigDecimal(priceStr);
        entity.setSellPriceOrigin(price);
        entity.setSellPrice(price);
        // 明细总价
        entity.setAfterAmount(price.multiply(new BigDecimal(qty)));

        // 折扣金额
        String discountStr = itemMap.getOrDefault("促销费-商品折扣", "").toString().replaceAll("-", "");
        entity.setDiscount(new BigDecimal(discountStr));
        return entity;
    }

    /**
     * 领星导出转换DMP收货信息
     */
    private DmpSoReceiverEntity lxConvertDmpSoReceive(Map<String, Object> itemMap, DmpSoInfoEntity soInfoEntity) {
        DmpSoReceiverEntity entity = new DmpSoReceiverEntity();
        entity.setMainId(soInfoEntity.getId());
        Object countryObj = itemMap.getOrDefault("国家/地区", "");
        if (null != countryObj){
            // 国家信息
            String countryStr = countryObj.toString();
            entity.setCountry(countryStr);
        }

        // 买家姓名
        Object buyerNameObj = itemMap.getOrDefault("买家姓名", "");
        if (null != buyerNameObj){
            String buyerNameStr = buyerNameObj.toString();
            entity.setBuyerName(buyerNameStr);
            entity.setReceiverName(buyerNameStr);
        }

        Object phoneObj = itemMap.getOrDefault("电话", "");
        if (null != phoneObj){
            String phone = phoneObj.toString();
            if (StringUtils.isNotBlank(phone) && !"null".equalsIgnoreCase(phone)){
                entity.setReceiverTelNumber(phone);
            }
        }


        Object postCodeObj = itemMap.getOrDefault("邮编", "");
        if (null != postCodeObj){
            String postCode = postCodeObj.toString();
            if (StringUtils.isNotBlank(postCode) && !"null".equalsIgnoreCase(postCode)){
                entity.setPostCode(postCode);
            }
        }

        Object provinceObj = itemMap.getOrDefault("州/地区", "");
        if (null != provinceObj){
            String province = provinceObj.toString();
            if (StringUtils.isNotBlank(province) && !"null".equalsIgnoreCase(province)){
                entity.setProvince(province);
            }
        }

        Object cityObj = itemMap.getOrDefault("城市", "");
        if (null != cityObj){
            String city = cityObj.toString();
            if (StringUtils.isNotBlank(city) && !"null".equalsIgnoreCase(city)){
                entity.setCity(city);
            }
        }


        Object streetObj = itemMap.getOrDefault("地址", "");
        if (null != streetObj){
            String address = streetObj.toString();
            if (StringUtils.isNotBlank(address) && !"null".equalsIgnoreCase(address)){
                entity.setMainStreet(address);
            }
        }

        Object emailObj = itemMap.getOrDefault("买家邮箱", "");
        if (null != emailObj){
            String email = emailObj.toString();
            if (StringUtils.isNotBlank(email) && !"null".equalsIgnoreCase(email)){
                entity.setEmail(email);
            }
        }

        return entity;
    }

    @Override
    public List<AfterSaleDTO.DropDownDTO> listDetailByPlatformCode(String platformCode) {
        if(StringUtil.isEmpty(platformCode)){
            return Collections.emptyList();
        }
        return this.baseMapper.listDetailByPlatformCode(platformCode);
    }
}
