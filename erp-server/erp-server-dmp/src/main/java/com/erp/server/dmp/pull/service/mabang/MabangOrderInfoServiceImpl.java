package com.erp.server.dmp.pull.service.mabang;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.security.HmacSHA256Utils;
import com.common.core.utils.HttpCommonUtil;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.constant.UrlContant;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import com.erp.model.dmp.entity.MabangAppEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.mabang.OrderEntity;
import com.erp.model.dmp.mabang.OrderItemEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import com.erp.server.dmp.pull.service.dmp.DmpOrderInfoService;
import com.erp.server.dmp.pull.service.dmp.DmpOrderItemService;
import com.erp.server.dmp.pull.service.dmp.DmpShopInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 马帮订单
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.ORDER_GET_ORDER_LIST)
public class MabangOrderInfoServiceImpl implements IReportSaveService {

    private static final Integer NOT_SHIPPED_STATUS = 6;
    private static final Integer NOT_UNSHIPPED_STATUS = 7;
    @Resource
    private MongoService mongoService;

    @Resource
    private DmpErrorLogService dmpErrorLogService;

    @Resource
    private DmpOrderItemService dmpOrderItemService;

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public static void main(String[] args) {
        MabangOrderInfoServiceImpl getOrderInfoService = new MabangOrderInfoServiceImpl();
        PlatformApiEnum platformApiEnum = PlatformApiEnum.getEnumByType("MABANG_GET_ORDER_LIST_TASK");
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode("order-get-order-list-new");
        jobTaskDTO.setApiId(5);
        jobTaskDTO.setApiName("获取订单列表");
        jobTaskDTO.setId(30L);
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(null);
        jobTaskDTO.setNextTime(null);
        jobTaskDTO.setPlatformId(1);
        jobTaskDTO.setState(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(platformApiEnum);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        List<OrderEntity> orderEntities = getOrderInfoService.pullDate(requestDTO);
        orderEntities.stream().peek(x -> System.out.println(StrUtil.format("{},{},{}",x.getSalesRecordNumber(),getOrderString(x.getOrderStatus()),  x.getPlatformId()))).collect(Collectors.toList());
//        System.out.println(orderEntities);
    }

    private static String getOrderString(Integer orderStatus) {
        switch (orderStatus){
            case 2:
                return "配货中";
            case 3:
                return "已发货";
            case 4 :
                return "已完成";
            case 5 :
                return "已作废";
            default:
                return orderStatus.toString();
        }
    }

    /**
     * 拉取订单数据
     *
     * @param dto 任务信息
     * @return
     */
    @Override
    public void pullDataSave(RequestDTO dto) throws Exception {
        List<OrderEntity> orderEntities = pullDate(dto);

        if (orderEntities != null && orderEntities.size() > 0) {
            for (OrderEntity orderEntity : orderEntities) {
                OrderMongoDTO orderMongoDTO = new OrderMongoDTO();
                orderMongoDTO.setPlatformOrderId(orderEntity.getPlatformOrderId());
                orderMongoDTO.setSalesRecordNumber(orderEntity.getSalesRecordNumber());
                List<OrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_MABANG_ORDER, OrderEntity.class);
                if (mongoData != null && mongoData.size() > 0) {
                    for (OrderEntity mongoDatum : mongoData) {
                        // 比较数据是否相同
                        if (!mongoDatum.toString().equals(orderEntity.toString())) {
                            // 修改数据
                            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(orderEntity), MapUtil.class);
                            try {
                                mongoService.updateMongoData(orderMongoDTO, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_ORDER, OrderEntity.class);
                            } catch (Exception e) {
                                DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity();
                                dmpErrorLogEntity.setTaskId(dto.getJobTaskDTO().getId());
                                dmpErrorLogEntity.setParams("");
                                dmpErrorLogEntity.setErrorMsg("==== 马帮修改mongodb订单数据失败，[ 订单号 = " + orderEntity.getPlatformOrderId() + "], 错误信息 = " + e.getMessage());
                                dmpErrorLogEntity.setReturnMsg("");
                                dmpErrorLogEntity.setCreateTime(new Date());
                                dmpErrorLogService.add(dmpErrorLogEntity);
                                throw new RuntimeException("==== 马帮修改mongodb订单数据失败，[ 订单号 = " + orderEntity.getPlatformOrderId() + "], 错误信息 = " + e.getMessage());
                            }
                        }
                    }
                } else {
                    mongoService.saveMongoData(orderEntity, MongoTableNameContant.ORIGINAL_MABANG_ORDER);
                }
                //存储数据到中台
                analysisOrder(orderEntity);
            }
        }
    }

    /**
     * 请求马帮订单接口
     *
     * @param dto
     * @return
     */
    public List<OrderEntity> pullDate(RequestDTO dto) {
        List<OrderEntity> infoArrayList = new ArrayList<>();
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        String st = "";
        String sd = "";
        if (dto.getJobTaskDTO().getLastTime() != null && dto.getJobTaskDTO().getNextTime() != null) {
            LocalDateTime localDateTime = lastTime.minusMinutes(5);
            DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
            st = sdf.format(localDateTime);
            sd = sdf.format(nextTime);
            dto.getJobTaskDTO().setLastTime(nextTime);
        } else {
            LocalDateTime date = LocalDateTime.now();
            DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
            LocalDateTime localDateTime = date.minusDays(1);
            st = sdf.format(localDateTime);
            sd = sdf.format(date);
            dto.getJobTaskDTO().setLastTime(date);
        }
        MabangAppEntity mabangAppEntity = new MabangAppEntity();

        //每次最多获取100条
        Integer pageSize = 100;
        //当前页数
        Integer pageIndex = 1;
        //总页数
        Integer pageCount = 1;
        Integer status = NOT_SHIPPED_STATUS;
        HttpCommonUtil httpCommonUtil = new HttpCommonUtil();
        while (pageIndex <= pageCount || NOT_SHIPPED_STATUS.equals(status)) {
            Map<String, Object> paramsMap = new HashMap();
            paramsMap.put("updateTimeStart", st);
            paramsMap.put("updateTimeEnd", sd);
//            paramsMap.put("page", pageIndex);
//            paramsMap.put("pageSize", pageSize);
            paramsMap.put("status",status);

            // 封装传参数据
            Map<String, Object> datas = new HashMap();
            datas.put("api", dto.getJobTaskDTO().getApiCode());
            datas.put("appkey", mabangAppEntity.getAppKey());
            datas.put("version", 1);
            datas.put("timestamp", new Long(System.currentTimeMillis() / 1000).toString());
            datas.put("data", paramsMap);

            // 将传参转为Json格式
            String jsonData = JSONObject.toJSONString(datas);
            String authorization = HmacSHA256Utils.hmacSHA256(jsonData, mabangAppEntity.getSecretKey());

            //设置请求头
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("Content-Type", "application/json");
            headerMap.put("Authorization", authorization);

            Map<String, Object> stringObjectMap = null;
            try {
                stringObjectMap = httpCommonUtil.sendOkhttp(UrlContant.MABANG_HOST, jsonData, null, headerMap, RequestMethod.POST);
                if (stringObjectMap.get("code").equals(200)) {
                    JSONObject jsonObject = JSONObject.parseObject(String.valueOf(stringObjectMap.get("data")));
                    List<OrderEntity> dataList = JSONObject.parseArray(jsonObject.get("data").toString(), OrderEntity.class);
                    pageCount = Integer.valueOf(jsonObject.get("pageCount").toString());
                    infoArrayList.addAll(dataList);
                } else {
                    log.info(" ===== 马帮拉取订单失败，错误信息：+" + stringObjectMap + " ==== 时间戳：" + new Date().getTime() + "");
                    throw new RuntimeException(" ===== 马帮拉取订单失败，错误信息：+" + stringObjectMap + " ====");
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.info("请求接口地址异常 错误信息：" + e.getMessage());
                Integer errorCount = dto.getJobTaskDTO().getErrorCount();
                if (errorCount < 3) {
                    dto.getJobTaskDTO().setErrorCount(errorCount + 1);
                    redisTemplate.boundListOps(dto.getJobTaskDTO().getTaskName()).leftPush(JSONObject.toJSONString(dto.getJobTaskDTO()));
                } else {
                    DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity();
                    dmpErrorLogEntity.setTaskId(dto.getJobTaskDTO().getId());
                    dmpErrorLogEntity.setParams(jsonData);
                    dmpErrorLogEntity.setErrorMsg(e.getMessage());
                    dmpErrorLogEntity.setReturnMsg(JSONObject.toJSONString(stringObjectMap));
                    dmpErrorLogEntity.setCreateTime(new Date());
                    dmpErrorLogService.add(dmpErrorLogEntity);
                }
                throw new RuntimeException(" ===== 马帮拉取订单失败，错误信息：+" + stringObjectMap + " ====详情请看错误表");
            }
            pageIndex++;
            if (pageIndex > pageCount && NOT_SHIPPED_STATUS.equals(status)) {
                status = NOT_UNSHIPPED_STATUS;
                pageIndex = 1;
                pageCount = 1;
            }
        }

        return infoArrayList;
    }

    /**
     * 解析订单数据
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     **/
    public void analysisOrder(OrderEntity orderEntity) throws Exception {
        DmpOrderInfoEntity dmpOrderInfoEntity = new DmpOrderInfoEntity();
        SimpleDateFormat sdf = new SimpleDateFormat(EnumTimePattern.y_m_dhms.toTimePattern());

        //平台订单id
        dmpOrderInfoEntity.setPlatformOrderId(orderEntity.getPlatformOrderId());

        //订单状态 2.配货中 3.已发货 4.已完成 5.已作废 6.退货 7.退款
        Integer orderStatus = orderEntity.getOrderStatus();
        if (orderEntity.getIsReturned().equals("1")) {
            orderStatus = 6;
        }

        if (orderEntity.getIsRefund().equals("1")) {
            orderStatus = 7;
        }

        dmpOrderInfoEntity.setOrderState(orderStatus);

        //买家账号
        dmpOrderInfoEntity.setBuyerUserId(orderEntity.getBuyerUserId());

        //买家姓名
        dmpOrderInfoEntity.setBuyerName(orderEntity.getBuyerName());

        //店铺编号
        dmpOrderInfoEntity.setShopNo(orderEntity.getShopId());

        //店铺名称
        dmpOrderInfoEntity.setShopName(orderEntity.getShopName());

        //订单成本价
        dmpOrderInfoEntity.setOrderCost(orderEntity.getOrderCost());

        //待审核订单 1.否 2.是
        dmpOrderInfoEntity.setCanSend(Integer.valueOf(orderEntity.getCanSend()));

        //是否退货 1.退货 2.非退货
        dmpOrderInfoEntity.setIsReturned(Integer.valueOf(orderEntity.getIsReturned()));

        //是否退款 1.退款 2.非退款
        dmpOrderInfoEntity.setIsRefund(Integer.valueOf(orderEntity.getIsRefund()));

        //订单付款时间
        if (StringUtils.isNotBlank(orderEntity.getPaidTime())) {
            dmpOrderInfoEntity.setPaidTime(sdf.parse(orderEntity.getPaidTime()));
        }

        //平台订单时间
        if (StringUtils.isNotBlank(orderEntity.getCreateDate())) {
            dmpOrderInfoEntity.setPlatformCreateTime(sdf.parse(orderEntity.getCreateDate()));
        }

        //平台交易号
        dmpOrderInfoEntity.setSalesRecordNumber(orderEntity.getSalesRecordNumber());

        //平台的订单状态
        dmpOrderInfoEntity.setPlatformOrderStatus(orderEntity.getPlatformOrderStatus());

        //订单金额
        dmpOrderInfoEntity.setOrderFee(orderEntity.getOrderFee());

        //订单来源平台
        dmpOrderInfoEntity.setSourcePlatform(orderEntity.getPlatformId());

        //是否合并订单 1.合并订单 2.非合并订单
        dmpOrderInfoEntity.setIsUnion(Integer.valueOf(orderEntity.getIsUnion()));

        //是否拆分订单 1.拆分订单 2.非拆分订单
        dmpOrderInfoEntity.setIsSplit(Integer.valueOf(orderEntity.getIsSplit()));

        //是否重发订单 1.重发订单 2.非重发订单
        dmpOrderInfoEntity.setIsResend(Integer.valueOf(orderEntity.getIsRefund()));

        //缺货订单 0 正在计算是否缺货 1有货 2缺货 3 已补货
        dmpOrderInfoEntity.setHasGoods(Integer.valueOf(orderEntity.getHasGoods()));

        //所属区域
        dmpOrderInfoEntity.setDistrict(orderEntity.getDistrict());

        //买家城市
        dmpOrderInfoEntity.setCity(orderEntity.getCity());

        //买家省份
        dmpOrderInfoEntity.setProvince(orderEntity.getProvince());

        //买家地址1
        dmpOrderInfoEntity.setManStreet(orderEntity.getStreet1());

        //买家地址2
        dmpOrderInfoEntity.setSecondStreet(orderEntity.getStreet2());

        //交易关闭时间
        if (StringUtils.isNotBlank(orderEntity.getCloseDate())) {
            dmpOrderInfoEntity.setCloseDate(sdf.parse(orderEntity.getCloseDate()));
        }

        //买家电话1
        dmpOrderInfoEntity.setManPhone(orderEntity.getPhone1());

        //买家电话2
        dmpOrderInfoEntity.setSecondPhone(orderEntity.getPhone2());

        //是否平台发货订单 1.否 2.是
        dmpOrderInfoEntity.setFbaFlag(Integer.valueOf(orderEntity.getFbaFlag()));

        //平台备注
        dmpOrderInfoEntity.setSellerMessage(orderEntity.getSellerMessage());

        //币种
        dmpOrderInfoEntity.setCurrencyCode(orderEntity.getCurrencyId());

        //汇率
        if (orderEntity.getCurrencyRate() != null
                && orderEntity.getCurrencyRate().compareTo(BigDecimal.ZERO) <= 0
                && orderEntity.getCurrencyId().equalsIgnoreCase("CNY")) {
            dmpOrderInfoEntity.setCurrencyRate(BigDecimal.ONE);
        } else {
            dmpOrderInfoEntity.setCurrencyRate(orderEntity.getCurrencyRate());
        }


        //商品总售价
        dmpOrderInfoEntity.setItemTotal(orderEntity.getItemTotal());

        //运费收入
        dmpOrderInfoEntity.setShippingFee(orderEntity.getShippingFee());

        //平台费
        dmpOrderInfoEntity.setPlatformFee(orderEntity.getShippingFee());

        //原始运费收入
        dmpOrderInfoEntity.setShippingTotalOrigin(orderEntity.getShippingTotalOrigin());

        //商品原始总售价
        dmpOrderInfoEntity.setItemTotalOrigin(orderEntity.getItemTotalOrigin());

        //商品总成本
        dmpOrderInfoEntity.setItemTotalCost(orderEntity.getItemTotalCost());

        //补贴金额
        dmpOrderInfoEntity.setSubsidyAmount(orderEntity.getSubsidyAmount());

        //国家英文名称
        dmpOrderInfoEntity.setCountryNameEn(orderEntity.getCountryNameEN());

        //国家中文名称
        dmpOrderInfoEntity.setCountryNameCn(orderEntity.getCountryNameCN());

        //平台标识
        dmpOrderInfoEntity.setPlatformSign("马帮");

        //企业Id
        dmpOrderInfoEntity.setCompanyId("1");

        //企业名称
        dmpOrderInfoEntity.setCompanyName("唯迹集团");

        //发货时间
        if (StringUtils.isNotBlank(orderEntity.getTransportTime())) {
            dmpOrderInfoEntity.setDeliveryTime(sdf.parse(orderEntity.getTransportTime()));
        }

        //创建时间
        dmpOrderInfoEntity.setCreateTime(LocalDateTime.now());

        //新增订单信息
        String orderInfoId = dmpOrderInfoService.checkOrder(dmpOrderInfoEntity);
        if (StringUtils.isNotBlank(orderInfoId)) {
            //新增订单商品信息
            analysisOrderItem(orderEntity, orderInfoId, orderEntity.getPlatformOrderId());
        }
    }

    /**
     * 解析订单商品数据
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     **/
    public void analysisOrderItem(OrderEntity orderEntity, String orderId, String platformOrderId) {
        List<OrderItemEntity> orderItems = orderEntity.getOrderItem();
        List<DmpOrderItemEntity> orderItemList = new ArrayList<>();
        for (OrderItemEntity orderItemBean : orderItems) {
            DmpOrderItemEntity dmpOrderItemEntity = new DmpOrderItemEntity();

            //订单表id
            dmpOrderItemEntity.setOrderId(orderId);

            //商品id
            dmpOrderItemEntity.setItemId(orderItemBean.getItemId());

            //平台sku
            dmpOrderItemEntity.setPlatformSku(orderItemBean.getPlatformSku());

            //平台原始sku数量
            dmpOrderItemEntity.setPlatformQuantity(orderItemBean.getPlatformQuantity());

            //商品名称
            dmpOrderItemEntity.setItemName(orderItemBean.getTitle());

            //商品图片
            dmpOrderItemEntity.setPictureUrl(orderItemBean.getPictureUrl());

            //商品成本价
            dmpOrderItemEntity.setCostPrice(orderItemBean.getCostPrice());

            //商品原始售价
            dmpOrderItemEntity.setSellPriceOrigin(orderItemBean.getSellPriceOrigin());

            //商品售价
            dmpOrderItemEntity.setSellPrice(orderItemBean.getSellPrice());

            //商品数量
            dmpOrderItemEntity.setQuantity(orderItemBean.getQuantity());

            //商品单位
            dmpOrderItemEntity.setProductUnit(orderItemBean.getProductUnit());

            //是否是赠品 1. 是 2. 否
            dmpOrderItemEntity.setIsGift(orderItemBean.getIsGift());

            //缺货订单 0.正在计算是否缺货 1.有货 2.缺货 3.已补货
            dmpOrderItemEntity.setHasGoods(orderItemBean.getHasGoods());

            //是否是组合商品 1.组合 2非组合
            dmpOrderItemEntity.setIsCombo(orderItemBean.getIsCombo());

            //订单商品备注
            dmpOrderItemEntity.setItemRemark(orderItemBean.getItemRemark());

            //商品多属性
            dmpOrderItemEntity.setSpecifics(orderItemBean.getSpecifics());

            //商品状态 1：未付款 2：未发货 3：已发货 4：已作废
            dmpOrderItemEntity.setStatus(orderItemBean.getStatus());

            //商品仓位
            dmpOrderItemEntity.setStockGrid(orderItemBean.getStockGrid());

            //sku
            dmpOrderItemEntity.setSkuNo(orderItemBean.getStockSku());

            //库存状态：1.自动创建 2.待开发 3.正常 4.清仓 5.停止销售
            dmpOrderItemEntity.setStockStatus(orderItemBean.getStockStatus());

            //商品仓库编号
            dmpOrderItemEntity.setStockWarehouseId(orderItemBean.getStockWarehouseId());

            //erp平台商品id
            dmpOrderItemEntity.setErpOrderItemId(platformOrderId + "_" + orderItemBean.getStockSku());

            //汇率
            if (orderEntity.getCurrencyRate() != null
                    && orderEntity.getCurrencyRate().compareTo(BigDecimal.ZERO) <= 0
                    && orderEntity.getCurrencyId().equalsIgnoreCase("CNY")) {
                dmpOrderItemEntity.setCurrencyRate(BigDecimal.ONE);
            } else {
                dmpOrderItemEntity.setCurrencyRate(orderEntity.getCurrencyRate());
            }

            orderItemList.add(dmpOrderItemEntity);
        }
        dmpOrderItemService.checkOrderItem(orderItemList);
    }
}
