package com.erp.server.dmp.pull.service.gyy;

import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.HttpCommonUtil;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.constant.UrlContant;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.dto.ShopDTO;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.gyy.GyyOrderEntity;
import com.erp.model.dmp.gyy.bean.DetailsBean;
import com.erp.model.sys.dto.SysUserDeptDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import com.erp.server.dmp.pull.service.dmp.DmpOrderInfoService;
import com.erp.server.dmp.pull.service.dmp.DmpOrderItemService;
import com.erp.server.dmp.pull.service.dmp.DmpShopInfoService;
import com.erp.server.dmp.utils.GyyUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管易云订单
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.GY_ERP_TRADE_GET)
public class GyyOrderInfoServiceImpl implements IReportSaveService {

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
        GyyOrderInfoServiceImpl gyyOrderInfoService = new GyyOrderInfoServiceImpl();
        PlatformApiEnum platformApiEnum = PlatformApiEnum.getEnumByType("gy.erp.trade.get");
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode("gy.erp.trade.get");
        jobTaskDTO.setApiId(7);
        jobTaskDTO.setApiName("管易云查询订单列表");
        jobTaskDTO.setId(32L);
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(0);
        jobTaskDTO.setNextTime(0);
        jobTaskDTO.setPlatformId(1);
        jobTaskDTO.setState(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(platformApiEnum);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        List<GyyOrderEntity> orderEntities = gyyOrderInfoService.pullDate(requestDTO);
        System.out.println(orderEntities);
    }

    /**
     * 拉取订单数据
     *
     * @param dto 任务信息
     * @return
     */
    @Override
    public void pullDataSave(RequestDTO dto) throws Exception {
        //请求api
        List<GyyOrderEntity> gyyOrderEntityList = pullDate(dto);

        //过滤数据
        if (gyyOrderEntityList != null && gyyOrderEntityList.size() > 0) {
            for (GyyOrderEntity gyyOrderEntity : gyyOrderEntityList) {
                if (!gyyOrderEntity.getOrderTypeName().equals("销售订单")) {
                    continue;
                }
                OrderMongoDTO orderMongoDTO = new OrderMongoDTO();
                orderMongoDTO.setPlatformCode(gyyOrderEntity.getPlatformCode());
                orderMongoDTO.setCode(gyyOrderEntity.getCode());
                List<GyyOrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_GYY_ORDER, GyyOrderEntity.class);
                if (mongoData != null && mongoData.size() > 0) {
                    for (GyyOrderEntity mongoDatum : mongoData) {
                        // 比较数据是否相同
                        if (!mongoDatum.toString().equals(gyyOrderEntity.toString())) {
                            // 修改数据
                            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(gyyOrderEntity), MapUtil.class);
                            try {
                                mongoService.updateMongoData(orderMongoDTO, mapUtil, MongoTableNameContant.ORIGINAL_GYY_ORDER, GyyOrderEntity.class);
                            } catch (Exception e) {
                                DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity();
                                dmpErrorLogEntity.setTaskId(dto.getJobTaskDTO().getId());
                                dmpErrorLogEntity.setParams("");
                                dmpErrorLogEntity.setErrorMsg("==== 管易云修改mongodb订单数据失败，[ 订单号 = " + gyyOrderEntity.getPlatformCode() + "], 错误信息 = " + e.getMessage());
                                dmpErrorLogEntity.setReturnMsg("");
                                dmpErrorLogEntity.setCreateTime(new Date());
                                dmpErrorLogService.add(dmpErrorLogEntity);
                                throw new RuntimeException("==== 管易云修改mongodb订单数据失败，[ 订单号 = " + gyyOrderEntity.getPlatformCode() + "], 错误信息 = " + e.getMessage());
                            }
                        }
                    }
                } else {
                    mongoService.saveMongoData(gyyOrderEntity, MongoTableNameContant.ORIGINAL_GYY_ORDER);
                }
                //存储数据到中台
                analysisOrder(gyyOrderEntity);
            }
        }
    }

    /**
     * 请求管易云订单接口
     *
     * @param dto
     * @return
     */
    public List<GyyOrderEntity> pullDate(RequestDTO dto) {
        List<GyyOrderEntity> infoArrayList = new ArrayList<>();
        Integer lastTime = dto.getJobTaskDTO().getLastTime();
        Integer nextTime = dto.getJobTaskDTO().getNextTime();
        String st = "";
        String sd = "";
        if (lastTime != 0 && nextTime != 0) {
            Date date = new Date(Long.valueOf(lastTime - (3L * 60L)) * 1000L);
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
        GyyAppEntity gyyAppEntity = new GyyAppEntity();

        //每次最多获取100条
        Integer pageSize = 100;
        //当前页数
        Integer pageIndex = 1;
        //总页数
        Integer pageCount = 1;
        //总条数
        Integer totalCount = 0;
        HttpCommonUtil httpCommonUtil = new HttpCommonUtil();
        while (pageIndex <= pageCount) {
            // 封装传参数据
            Map<String, Object> datas = new HashMap();
            datas.put("method", dto.getJobTaskDTO().getApiCode());
            datas.put("appkey", gyyAppEntity.getAppKey());
            datas.put("sessionkey", gyyAppEntity.getSessionKey());
            datas.put("date_type", 3);
            datas.put("start_date", st);
            datas.put("end_date", sd);
            datas.put("page_no", pageIndex);
            datas.put("page_size", pageSize);

            String str = JSONObject.toJSONString(datas);
            String sign = GyyUtils.sign(str, gyyAppEntity.getSecretKey());
            datas.put("sign", sign);
            // 将传参转为Json格式
            String jsonData = JSONObject.toJSONString(datas);

            //设置请求头
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("Content-Type", "text/json");

            Map<String, Object> stringObjectMap = null;
            try {
                stringObjectMap = httpCommonUtil.sendOkhttp(UrlContant.GYY_HOST, jsonData, null, headerMap, RequestMethod.POST);
                if (Boolean.valueOf(stringObjectMap.get("success").toString())) {
                    List<GyyOrderEntity> dataList = JSONObject.parseArray(String.valueOf(stringObjectMap.get("orders")), GyyOrderEntity.class);
                    totalCount = Integer.valueOf(stringObjectMap.get("total").toString());
                    pageCount = (totalCount + pageSize - 1) / pageSize;
                    infoArrayList.addAll(dataList);
                } else {

                    log.info(" ===== 拉取订单失败，错误信息：+" + stringObjectMap + " ====");
                    throw new RuntimeException(" ===== 拉取订单失败，错误信息：+" + stringObjectMap + " ====");
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
                break;
            }
            pageIndex++;
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
    public void analysisOrder(GyyOrderEntity gyyOrderEntity) throws Exception {
        DmpOrderInfoEntity dmpOrderInfoEntity = new DmpOrderInfoEntity();
        SimpleDateFormat sdf = new SimpleDateFormat(EnumTimePattern.y_m_dhms.toTimePattern());

        //平台订单id
        dmpOrderInfoEntity.setPlatformOrderId(gyyOrderEntity.getPlatformCode());

        //订单状态 2.配货中 3.已发货 4.已完成 5.已作废 6.退货 7.退款
        Integer orderState = 0;
        //0:未配货 1:部分配货 2:全部配货
        Integer assignState = gyyOrderEntity.getAssignState();
        if (assignState.equals(1) || assignState.equals(2)) {
            orderState = 2;
        }

        //0:未发货 1:部分发货 2:全部发货
        Integer deliveryState = gyyOrderEntity.getDeliveryState();
        if (deliveryState.equals(1) || deliveryState.equals(2)) {
            orderState = 3;
        }

        //0:未退款 1:部分退款 2:全部退款
        Integer refundState = gyyOrderEntity.getRefundState();
        if (refundState.equals(1) || refundState.equals(2)) {
            orderState = 7;
        }
        dmpOrderInfoEntity.setOrderState(orderState);

        //买家账号
        dmpOrderInfoEntity.setBuyerUserId(gyyOrderEntity.getVipCode());

        //买家姓名
        dmpOrderInfoEntity.setBuyerName(gyyOrderEntity.getVipName());

        //店铺编号
        dmpOrderInfoEntity.setShopNo(gyyOrderEntity.getShopCode());

        //店铺名称
        dmpOrderInfoEntity.setShopName(gyyOrderEntity.getShopName());

        //待审核订单 1.否 2.是
        if (gyyOrderEntity.getApprove()) {
            dmpOrderInfoEntity.setCanSend(2);
        } else {
            dmpOrderInfoEntity.setCanSend(1);
        }

        //是否退款 1.退款 2.非退款
        if (refundState.equals(1) || refundState.equals(2)) {
            dmpOrderInfoEntity.setIsRefund(1);
        } else {
            dmpOrderInfoEntity.setIsRefund(2);
        }

        //是否退货 1.退货 2.非退货
        dmpOrderInfoEntity.setIsReturned(2);

        //订单付款时间
        if (StringUtils.isNotBlank(gyyOrderEntity.getPaytime())) {
            dmpOrderInfoEntity.setPaidTime(sdf.parse(gyyOrderEntity.getPaytime()));
        }

        //平台订单时间
        if (StringUtils.isNotBlank(gyyOrderEntity.getCreatetime())) {
            dmpOrderInfoEntity.setPlatformCreateTime(sdf.parse(gyyOrderEntity.getCreatetime()));
        }

        //平台交易号
        dmpOrderInfoEntity.setSalesRecordNumber(gyyOrderEntity.getCode());

        //平台的订单状态
        dmpOrderInfoEntity.setPlatformOrderStatus(gyyOrderEntity.getPlatformTradingState());

        //订单来源平台
        dmpOrderInfoEntity.setSourcePlatform(gyyOrderEntity.getFromTypeName());
//
//        //是否合并订单 1.合并订单 2.非合并订单
//        dmpOrderInfoEntity.setIsUnion(Integer.valueOf(orderEntity.getIsUnion()));
//
//        //是否拆分订单 1.拆分订单 2.非拆分订单
//        dmpOrderInfoEntity.setIsSplit(Integer.valueOf(orderEntity.getIsSplit()));
//
//        //是否重发订单 1.重发订单 2.非重发订单
//        dmpOrderInfoEntity.setIsResend(Integer.valueOf(orderEntity.getIsRefund()));
//
//        //缺货订单 0 正在计算是否缺货 1有货 2缺货 3 已补货
//        dmpOrderInfoEntity.setHasGoods(Integer.valueOf(orderEntity.getHasGoods()));

        if (StringUtils.isNotBlank(gyyOrderEntity.getReceiverArea())) {
            String[] split = gyyOrderEntity.getReceiverArea().split("-");
            if (split.length >= 1) {
                //买家省份
                dmpOrderInfoEntity.setProvince(split[0]);
            }
            if (split.length >= 2) {
                //买家城市
                dmpOrderInfoEntity.setCity(split[1]);
            }
            if (split.length >= 3) {
                //所属区域
                dmpOrderInfoEntity.setDistrict(split[2]);
            }
        }

        //买家地址1
        dmpOrderInfoEntity.setManStreet(gyyOrderEntity.getReceiverAddress());

        //买家地址2
        dmpOrderInfoEntity.setSecondStreet("");

        //交易关闭时间
        dmpOrderInfoEntity.setCloseDate(null);

        //买家电话1
        dmpOrderInfoEntity.setManPhone(gyyOrderEntity.getReceiverMobile());

        //买家电话2
        dmpOrderInfoEntity.setSecondPhone(gyyOrderEntity.getReceiverPhone());

        //是否平台发货订单 1.否 2.是
        dmpOrderInfoEntity.setFbaFlag(null);

        //平台备注
        dmpOrderInfoEntity.setSellerMessage(gyyOrderEntity.getBuyerMemo());

        //币种
        //dmpOrderInfoEntity.setCurrencyCode(gyyOrderEntity.getCurrencyCode());
        dmpOrderInfoEntity.setCurrencyCode("CNY");

        //汇率
        dmpOrderInfoEntity.setCurrencyRate(BigDecimal.ONE);

        //商品总售价
        dmpOrderInfoEntity.setItemTotal(gyyOrderEntity.getPaymentAmount());

        //订单金额
        dmpOrderInfoEntity.setOrderFee(gyyOrderEntity.getPayment());

        //运费收入
        dmpOrderInfoEntity.setShippingFee(gyyOrderEntity.getPostFee());

        //平台费
        dmpOrderInfoEntity.setPlatformFee(BigDecimal.ZERO);

        //原始运费收入
        dmpOrderInfoEntity.setShippingTotalOrigin(BigDecimal.ZERO);

        List<DetailsBean> details = gyyOrderEntity.getDetails();
        BigDecimal costPrice = new BigDecimal(BigInteger.ZERO);
        for (DetailsBean detail : details) {
            costPrice = costPrice.add(detail.getCostPrice());

        }
        //订单成本价
        dmpOrderInfoEntity.setOrderCost(costPrice);

        //商品总成本
        dmpOrderInfoEntity.setItemTotalCost(costPrice);

        //商品原始总售价
        dmpOrderInfoEntity.setItemTotalOrigin(gyyOrderEntity.getAmount());

        //补贴金额
        dmpOrderInfoEntity.setSubsidyAmount(gyyOrderEntity.getDiscountFee());

        //国家英文名称
        dmpOrderInfoEntity.setCountryNameEn("China");

        //国家中文名称
        dmpOrderInfoEntity.setCountryNameCn("中国");

        //平台标识
        dmpOrderInfoEntity.setPlatformSign("管易云");

        //创建时间
        dmpOrderInfoEntity.setCreateTime(new Date());

        //新增订单信息
        String orderInfoId = dmpOrderInfoService.checkOrder(dmpOrderInfoEntity);
        if (StringUtils.isNotBlank(orderInfoId)) {
            //新增订单商品信息
            analysisOrderItem(gyyOrderEntity, orderInfoId);
        }
    }

    /**
     * 解析订单商品数据
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     **/
    public void analysisOrderItem(GyyOrderEntity gyyOrderEntity, String orderId) {
        List<DetailsBean> orderItem = gyyOrderEntity.getDetails();
        String warehouseCode = gyyOrderEntity.getWarehouseCode();
        List<DmpOrderItemEntity> orderItemList = new ArrayList<>();
        for (DetailsBean detailsBean : orderItem) {
            DmpOrderItemEntity dmpOrderItemEntity = new DmpOrderItemEntity();
            //订单表id
            dmpOrderItemEntity.setOrderId(orderId);

            //商品id
            dmpOrderItemEntity.setItemId(detailsBean.getItemCode());

            //平台sku
            dmpOrderItemEntity.setPlatformSku(detailsBean.getSkuCode());

            //平台原始sku数量
            dmpOrderItemEntity.setPlatformQuantity(detailsBean.getDeliveringQty());

            //商品名称
            dmpOrderItemEntity.setItemName(detailsBean.getItemName());

            //商品图片
            dmpOrderItemEntity.setPictureUrl("");

            //商品成本价
            dmpOrderItemEntity.setCostPrice(detailsBean.getCostPrice());

            //商品原始售价
            dmpOrderItemEntity.setSellPriceOrigin(detailsBean.getPrice());

            //商品售价
            dmpOrderItemEntity.setSellPrice(detailsBean.getAmount());

            //商品数量
            dmpOrderItemEntity.setQuantity(detailsBean.getQty());

            //商品单位
            dmpOrderItemEntity.setProductUnit(detailsBean.getItemUnitName());

            //是否是赠品 1. 是 2. 否
            if (detailsBean.getIsGift()) {
                dmpOrderItemEntity.setIsGift(1);
            } else {
                dmpOrderItemEntity.setIsGift(2);
            }

            //缺货订单 0.正在计算是否缺货 1.有货 2.缺货 3.已补货
            dmpOrderItemEntity.setHasGoods(0);

            //是否是组合商品 1.组合 2非组合
            dmpOrderItemEntity.setIsCombo(null);

            //订单商品备注
            dmpOrderItemEntity.setItemRemark(detailsBean.getSkuNote());

            //商品多属性
            if (StringUtils.isNotBlank(detailsBean.getPlatformSkuName())) {
                dmpOrderItemEntity.setSpecifics(detailsBean.getPlatformSkuName());
            }

            //商品状态 1：未付款 2：未发货 3：已发货 4：已作废
            Integer orderState = null;
            dmpOrderItemEntity.setStatus(orderState);

            //商品仓位
            dmpOrderItemEntity.setStockGrid(null);

            //sku
            dmpOrderItemEntity.setSkuNo(detailsBean.getItemCode());
//
            //库存状态：1.自动创建 2.待开发 3.正常 4.清仓 5.停止销售
            dmpOrderItemEntity.setStockStatus(null);
//
            //商品仓库编号
            dmpOrderItemEntity.setStockWarehouseId(warehouseCode);

            //erp平台商品id
            dmpOrderItemEntity.setErpOrderItemId(gyyOrderEntity.getPlatformCode() + "-" + detailsBean.getItemCode());

            //汇率
            dmpOrderItemEntity.setCurrencyRate(BigDecimal.ONE);

            orderItemList.add(dmpOrderItemEntity);
        }
        dmpOrderItemService.checkOrderItem(orderItemList);
    }
}
