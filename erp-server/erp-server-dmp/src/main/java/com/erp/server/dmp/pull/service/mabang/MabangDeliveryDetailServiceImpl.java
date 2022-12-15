package com.erp.server.dmp.pull.service.mabang;

import com.alibaba.fastjson.JSONObject;
import com.common.core.security.HmacSHA256Utils;
import com.common.core.utils.HttpCommonUtil;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.constant.UrlContant;
import com.erp.model.dmp.entity.DmpDeliveryDetailInfoEntity;
import com.erp.model.dmp.entity.DmpDeliveryDetailItemEntity;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.kingdee.KingdeeDeliveryDetailEntity;
import com.erp.model.dmp.kingdee.KingdeeDeliveryDetailItemEntity;
import com.erp.model.dmp.mabang.OrderEntity;
import com.erp.model.dmp.mabang.OrderItemEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpDeliveryDetailInfoService;
import com.erp.server.dmp.pull.service.dmp.DmpDeliveryDetailItemService;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMethod;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import com.erp.model.dmp.entity.MabangAppEntity;

/**
 * 马帮出库详情
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.ORDER_GET_DELIVERY_LIST)
public class MabangDeliveryDetailServiceImpl implements IReportSaveService {

    @Resource
    private MongoService mongoService;

    @Resource
    private DmpErrorLogService dmpErrorLogService;

    @Resource
    private DmpDeliveryDetailInfoService dmpDeliveryDetailInfoService;

    @Resource
    private DmpDeliveryDetailItemService dmpDeliveryDetailItemService;

    @Override
    public void pullDataSave(RequestDTO dto) throws Exception {
        List<OrderEntity> orderEntities = pullDate(dto);
        if (orderEntities != null && orderEntities.size() > 0) {
            for (OrderEntity orderEntity : orderEntities) {
                OrderMongoDTO orderMongoDTO = new OrderMongoDTO();
                orderMongoDTO.setPlatformOrderId(orderEntity.getPlatformOrderId());
                List<OrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_MABANG_DELIVERY_DETAIL, OrderEntity.class);
                if (mongoData != null && mongoData.size() > 0) {
                    for (OrderEntity mongoDatum : mongoData) {
                        // 比较数据是否相同
                        if (!mongoDatum.toString().equals(orderEntity.toString())) {
                            // 修改数据
                            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(orderEntity), MapUtil.class);
                            try {
                                mongoService.updateMongoData(orderMongoDTO, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_DELIVERY_DETAIL, OrderEntity.class);
                            } catch (Exception e) {
                                DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity();
                                dmpErrorLogEntity.setTaskId(dto.getJobTaskDTO().getId());
                                dmpErrorLogEntity.setParams("");
                                dmpErrorLogEntity.setErrorMsg("==== 马帮修改mongodb出库数据失败，[ 单号 = " + orderEntity.getPlatformOrderId() + "], 错误信息 = " + e.getMessage());
                                dmpErrorLogEntity.setReturnMsg("");
                                dmpErrorLogEntity.setCreateTime(new Date());
                                dmpErrorLogService.add(dmpErrorLogEntity);
                                throw new RuntimeException("==== 马帮修改mongodb出库数据失败，[ 单号 = " + orderEntity.getPlatformOrderId() + "], 错误信息 = " + e.getMessage());
                            }
                        }
                    }
                } else {
                    mongoService.saveMongoData(orderEntity, MongoTableNameContant.ORIGINAL_MABANG_DELIVERY_DETAIL);
                }
                //存储数据到中台
                analysisDeliveryDetail(orderEntity);
            }
        }
    }

    /**
     * 请求马帮出库接口
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
                Date date = new Date(Long.valueOf(lastTime - (3L*60L)) * 1000L);
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
            String method = "order-get-order-list";
            String appKey = mabangAppEntity.getAppKey();
            String appSecret = mabangAppEntity.getSecretKey();

            //每次最多获取100条
            Integer pageSize = 1000;
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
                        log.info(" ===== 马帮拉取出库失败，错误信息：+" + stringObjectMap + " ====");
                        throw new RuntimeException(" ===== 马帮拉取出库失败，错误信息：+" + stringObjectMap + " ====");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.info("请求接口地址异常 错误信息：" + e.getMessage());
                    DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity();
                    dmpErrorLogEntity.setTaskId(jobTask.getId());
                    dmpErrorLogEntity.setParams(jsonData);
                    dmpErrorLogEntity.setErrorMsg(e.getMessage());
                    dmpErrorLogEntity.setReturnMsg(JSONObject.toJSONString(stringObjectMap));
                    dmpErrorLogEntity.setCreateTime(new Date());
                    dmpErrorLogService.add(dmpErrorLogEntity);
                }
                pageIndex++;
            }
        } catch (Exception e) {
            log.info(" ===== 获取马帮出库列表数据失败， 错误信息 = { " + e.getMessage() + " }");
            throw new RuntimeException(" ===== 获取马帮出库列表数据失败， 错误信息 = { " + e.getMessage() + " }");
        }
        return infoArrayList;
    }

    /**
     * 解析出库数据
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     * @return void
     **/
    @Transactional
    public void analysisDeliveryDetail(OrderEntity orderEntity) throws Exception {
        DmpDeliveryDetailInfoEntity deliveryDetailInfoEntity = new DmpDeliveryDetailInfoEntity();
        SimpleDateFormat sdf = new SimpleDateFormat(EnumTimePattern.y_m_dhms.toTimePattern());

        //单据编号
        deliveryDetailInfoEntity.setBillNo(orderEntity.getPlatformOrderId());

        //出库编号
        deliveryDetailInfoEntity.setOrderNo(orderEntity.getPlatformOrderId());

        //物流单号
        deliveryDetailInfoEntity.setLogisticsNo(orderEntity.getTrackNumber());

        //客户名称
        deliveryDetailInfoEntity.setCustomerName(orderEntity.getBuyerName());

        //平台名称
        deliveryDetailInfoEntity.setPlatformName(orderEntity.getPlatformId());

        //店铺编号
        deliveryDetailInfoEntity.setShopNo(orderEntity.getShopId());

        //店铺名称
        deliveryDetailInfoEntity.setShopName(orderEntity.getShopName());

        //出库成本价
        deliveryDetailInfoEntity.setItemTotalCost(orderEntity.getItemTotalCost());

        //出库总价
        deliveryDetailInfoEntity.setOrderTotalCost(orderEntity.getOrderFee());

        //国家英文名称
        deliveryDetailInfoEntity.setCountryNameEn(orderEntity.getCountryNameEN());

        //国家中文名称
        deliveryDetailInfoEntity.setCountryNameCn(orderEntity.getCountryNameCN());

        //买家城市
        deliveryDetailInfoEntity.setCity(orderEntity.getCity());

        //买家省份
        deliveryDetailInfoEntity.setProvince(orderEntity.getProvince());

        //买家地址1
        deliveryDetailInfoEntity.setManStreet(orderEntity.getStreet1());

        //买家地址2
        deliveryDetailInfoEntity.setSecondStreet(orderEntity.getStreet2());

        //所属区域
        deliveryDetailInfoEntity.setDistrict(orderEntity.getDistrict());

        //币种
        deliveryDetailInfoEntity.setCurrencyCode(orderEntity.getCarrierCode());

        //汇率
        deliveryDetailInfoEntity.setCurrencyRate(orderEntity.getCurrencyRate());

        //运费
        deliveryDetailInfoEntity.setShippingFee(orderEntity.getShippingFee());

        //补贴金额
        deliveryDetailInfoEntity.setSubsidyAmount(orderEntity.getSubsidyAmount());

        //销售部门
        deliveryDetailInfoEntity.setSaleDeptName("");

        //销售员编号
        deliveryDetailInfoEntity.setSalesManId("");

        //销售员名称
        deliveryDetailInfoEntity.setSalesManName("");
        //"orderStatus":"出库状态 2.配货中 3.已发货 4.已完成 5.已作废",
        Integer status = 1;
        switch (orderEntity.getOrderStatus()) {
            case 3:
            case 4:
            default:
                status = 1;
                break;
            case 5:
                status = 2;
                break;
        }

        //状态 1.已发货 2.已作废
        deliveryDetailInfoEntity.setStatus(status);

        //平台单据审核时间
        deliveryDetailInfoEntity.setPlatformApproveTime(null);

        //平台单据创建时间
        if (StringUtils.isNotBlank(orderEntity.getCreateDate()) && !orderEntity.getCreateDate().equals("null")) {
            deliveryDetailInfoEntity.setPlatformCreateTime(sdf.parse(orderEntity.getCreateDate()));
        }

        //平台单据修改时间
        if (StringUtils.isNotBlank(orderEntity.getOperTime()) && !orderEntity.getOperTime().equals("null")) {
            deliveryDetailInfoEntity.setPlatformUpdateTime(sdf.parse(orderEntity.getOperTime()));
        }
        //发货时间
        if (StringUtils.isNotBlank(orderEntity.getTransportTime()) && !orderEntity.getTransportTime().equals("null")) {
            deliveryDetailInfoEntity.setDeliveryDate(sdf.parse(orderEntity.getTransportTime()));
        }

        //备注
        deliveryDetailInfoEntity.setRemark(orderEntity.getRemark());

        //平台标识
        deliveryDetailInfoEntity.setPlatformSign("马帮");

        //企业Id
        deliveryDetailInfoEntity.setCompanyId("");

        //企业名称
        deliveryDetailInfoEntity.setCompanyName("");

        //创建时间
        deliveryDetailInfoEntity.setCreateTime(new Date());

        //新增出库信息
        String deliveryDetailId = dmpDeliveryDetailInfoService.checkOrder(deliveryDetailInfoEntity);
        if (StringUtils.isNotBlank(deliveryDetailId)) {
            //新增出库商品信息
            analysisReturnOrderItem(orderEntity.getOrderItem(), deliveryDetailId);
        }
    }

    /**
     * 解析出库详情商品数据
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     * @return void
     **/
    @Transactional
    public void analysisReturnOrderItem(List<OrderItemEntity> orderItem, String deliveryDetailId) {
        List<DmpDeliveryDetailItemEntity> orderItemList = new ArrayList<>();
        for (OrderItemEntity itemEntity : orderItem) {
            DmpDeliveryDetailItemEntity dmpReturnOrderItemEntity = new DmpDeliveryDetailItemEntity();

            //发货详情表id
            dmpReturnOrderItemEntity.setDeliveryDetailId(deliveryDetailId);

            //商品id
            dmpReturnOrderItemEntity.setItemId(itemEntity.getItemId());

            //平台sku
            dmpReturnOrderItemEntity.setPlatformSku(itemEntity.getPlatformSku());

            //商品sku编号
            dmpReturnOrderItemEntity.setSkuNo(itemEntity.getStockSku());

            //商品名称
            dmpReturnOrderItemEntity.setItemName(itemEntity.getTitle());

            //商品成本价
            dmpReturnOrderItemEntity.setCostPrice(itemEntity.getCostPrice());

            //商品售价
            dmpReturnOrderItemEntity.setSellPrice(itemEntity.getSellPrice());

            //商品数量
            dmpReturnOrderItemEntity.setQuantity(itemEntity.getQuantity());

            //总售价
            dmpReturnOrderItemEntity.setAmount(itemEntity.getSellPrice());

            //商品单位
            dmpReturnOrderItemEntity.setProductUnit(itemEntity.getProductUnit());

            //是否是赠品 1. 是 2. 否
            if (itemEntity.getIsGift() == 1) {
                dmpReturnOrderItemEntity.setIsGift(1);
            } else {
                dmpReturnOrderItemEntity.setIsGift(2);
            }

            //属性
            dmpReturnOrderItemEntity.setSpecifics(itemEntity.getSpecifics());

            //出库商品备注
            dmpReturnOrderItemEntity.setItemRemark(itemEntity.getItemRemark());

            //仓库
            dmpReturnOrderItemEntity.setStockName(itemEntity.getStockWarehouseName());

            //库位
            dmpReturnOrderItemEntity.setWarehouseLocation(itemEntity.getStockGrid());

            orderItemList.add(dmpReturnOrderItemEntity);
        }
        checkOrderItem(orderItemList, deliveryDetailId);
    }

    /**
     * 校验出库详情商品信息在中台是否存在，存在就修改不存在则新增
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    public void checkOrderItem(List<DmpDeliveryDetailItemEntity> orderItem, String deliveryDetailId) {
        dmpDeliveryDetailItemService.deleteDeliveryDetailItemByDetailId(deliveryDetailId);
        dmpDeliveryDetailItemService.batchAdd(orderItem);
    }
}
