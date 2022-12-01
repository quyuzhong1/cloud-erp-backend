package com.erp.server.dmp.pull.service.mabang;

import com.alibaba.fastjson.JSONObject;
import com.common.core.security.HmacSHA256Utils;
import com.common.core.utils.HttpCommonUtil;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.server.dmp.constant.UrlContant;
import com.erp.server.dmp.constant.MongoTableNameContant;
import com.erp.server.dmp.entity.dmp.DmpErrorLogEntity;
import com.erp.server.dmp.entity.dmp.DmpOrderInfoEntity;
import com.erp.server.dmp.entity.dmp.DmpOrderItemEntity;
import com.erp.server.dmp.entity.dmp.MabangAppEntity;
import com.erp.server.dmp.entity.dto.JobTaskDTO;
import com.erp.server.dmp.entity.dto.OrderMongoDTO;
import com.erp.server.dmp.entity.dto.RequestDTO;
import com.erp.server.dmp.entity.mabang.OrderEntity;
import com.erp.server.dmp.entity.mabang.OrderItemEntity;
import com.erp.server.dmp.enums.PlatformApiEnum;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.*;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import com.erp.server.dmp.pull.service.dmp.DmpOrderInfoService;
import com.erp.server.dmp.pull.service.dmp.DmpOrderItemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 马帮订单
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.ORDER_GET_ORDER_LIST)
public class MabangOrderInfoServiceImpl implements IReportSaveService {

    @Resource
    private MongoService mongoService;

    @Resource
    private DmpErrorLogService dmpErrorLogService;

    @Resource
    private DmpOrderItemService dmpOrderItemService;

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;

    public static void main(String[] args) {
        MabangOrderInfoServiceImpl getOrderInfoService = new MabangOrderInfoServiceImpl();
        PlatformApiEnum platformApiEnum = PlatformApiEnum.getEnumByType("MABANG_GET_ORDER_LIST_TASK");
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode("order-get-order-list");
        jobTaskDTO.setApiId(5);
        jobTaskDTO.setApiName("获取订单列表");
        jobTaskDTO.setId(30L);
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(0);
        jobTaskDTO.setNextTime(0);
        jobTaskDTO.setPlatformId(1);
        jobTaskDTO.setState(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(platformApiEnum);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        List<OrderEntity> orderEntities = getOrderInfoService.pullDate(requestDTO);
        System.out.println(orderEntities);
    }

    /**
     * 拉取订单数据
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
     * @param dto
     * @return
     */
    public List<OrderEntity> pullDate(RequestDTO dto) {
        List<OrderEntity> infoArrayList = new ArrayList<>();
        try {
            JobTaskDTO jobTask = dto.getJobTaskDTO();
            Integer lastTime = jobTask.getLastTime();
            Integer nextTime = jobTask.getNextTime();
            String st = "";
            String sd = "";
            if (lastTime != 0 && nextTime != 0) {
                Date date = new Date(Long.valueOf(lastTime - (10L*60L)) * 1000L);
                SimpleDateFormat sdf = new SimpleDateFormat(EnumTimePattern.y_m_dhms.toTimePattern());
                st = sdf.format(date);
                sd = sdf.format(new Date(nextTime * 1000L));
                dto.getJobTaskDTO().setLastTime(nextTime);
            } else {
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat(EnumTimePattern.y_m_dhms.toTimePattern());
                Calendar cl = Calendar.getInstance();
                cl.setTime(date);
                cl.add(Calendar.DAY_OF_MONTH, -1);
                st = sdf.format(cl.getTime());
                sd = sdf.format(date);
                dto.getJobTaskDTO().setLastTime(Integer.parseInt(String.valueOf(System.currentTimeMillis() / 1000L)));
            }
            MabangAppEntity mabangAppEntity = new MabangAppEntity();
            String url = UrlContant.MABANG_HOST;
            String method = jobTask.getApiCode();
            String appKey = mabangAppEntity.getAppKey();
            String appSecret = mabangAppEntity.getSecretKey();

            //每次最多获取100条
            Integer pageSize = 100;
            //当前页数
            Integer pageIndex = 1;
            //总页数
            Integer pageCount = 1;
            HttpCommonUtil httpCommonUtil = new HttpCommonUtil();
            while (pageIndex <= pageCount) {
                Map<String, Object> paramsMap = new HashMap();
                paramsMap.put("updateTimeStart", st);
                paramsMap.put("updateTimeEnd", sd);
                paramsMap.put("page", pageIndex);
                paramsMap.put("pageSize", pageSize);

                // 封装传参数据
                Map<String, Object> datas = new HashMap();
                datas.put("api", method);
                datas.put("appkey", appKey);
                datas.put("version", 1);
                datas.put("timestamp", new Long(System.currentTimeMillis() / 1000).toString());
                datas.put("data", paramsMap);

                // 将传参转为Json格式
                String jsonData = JSONObject.toJSONString(datas);
                String authorization = HmacSHA256Utils.hmacSHA256(jsonData, appSecret);

                //设置请求头
                Map<String,String> headerMap = new HashMap<>();
                headerMap.put("Content-Type", "application/json");
                headerMap.put("Authorization", authorization);

                Map<String, Object> stringObjectMap = null;
                try {
                    stringObjectMap = httpCommonUtil.sendOkhttp(url, jsonData, null, headerMap, RequestMethod.POST);
                    if (stringObjectMap.get("code").equals(200)) {
                        JSONObject jsonObject = JSONObject.parseObject(String.valueOf(stringObjectMap.get("data")));
                        List<OrderEntity> dataList = JSONObject.parseArray(jsonObject.get("data").toString(), OrderEntity.class);
                        pageCount = Integer.valueOf(jsonObject.get("pageCount").toString());
                        infoArrayList.addAll(dataList);
                    } else {
                        log.info(" ===== 马帮拉取订单失败，错误信息：+" + stringObjectMap + " ====");
                        throw new RuntimeException(" ===== 马帮拉取订单失败，错误信息：+" + stringObjectMap + " ====");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.info("请求接口地址异常 错误信息：" + e.getMessage());
                    DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity();
                    dmpErrorLogEntity.setTaskId(jobTask.getId());
                    dmpErrorLogEntity.setParams(jsonData);
                    dmpErrorLogEntity.setErrorMsg(e.getMessage());
                    dmpErrorLogEntity.setReturnMsg(JSONObject.toJSONString(stringObjectMap));
                    dmpErrorLogService.add(dmpErrorLogEntity);
                }
                pageIndex++;
            }
        } catch (Exception e) {
            log.info(" ===== 获取马帮订单列表数据失败， 错误信息 = { " + e.getMessage() + " }");
            throw new RuntimeException(" ===== 获取马帮订单列表数据失败， 错误信息 = { " + e.getMessage() + " }");
        }
        return infoArrayList;
    }

    /**
     * 解析订单数据
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     * @return void
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
            Date parse = sdf.parse(orderEntity.getPaidTime());
            dmpOrderInfoEntity.setPaidTime(parse);
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
            Date closeDate = sdf.parse(orderEntity.getCloseDate());
            dmpOrderInfoEntity.setCloseDate(closeDate);
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
        dmpOrderInfoEntity.setCurrencyRate(orderEntity.getCurrencyRate());

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

        //国家英文名称
        dmpOrderInfoEntity.setCountryNameCn(orderEntity.getCountryNameCN());

        //平台标识
        dmpOrderInfoEntity.setPlatformSign("马帮");

        dmpOrderInfoEntity.setCreateTime(new Date());

        //新增订单信息
        String orderInfoId = dmpOrderInfoService.checkOrder(dmpOrderInfoEntity);
        if (StringUtils.isNotBlank(orderInfoId)) {
            //新增订单商品信息
            analysisOrderItem(orderEntity.getOrderItem(), orderInfoId);
        }
    }

    /**
     * 解析订单商品数据
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     * @return void
     **/
    public void analysisOrderItem(List<OrderItemEntity> orderItem, String orderId) {
        List<DmpOrderItemEntity> orderItemList = new ArrayList<>();
        for (OrderItemEntity orderItemBean : orderItem) {
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
            dmpOrderItemEntity.setErpOrderItemId(orderItemBean.getErpOrderItemId());

            orderItemList.add(dmpOrderItemEntity);
        }
        dmpOrderItemService.checkOrderItem(orderItemList);
//        dmpOrderItemService.batchAdd(orderItemList);
//        return orderItemList;
    }
}
