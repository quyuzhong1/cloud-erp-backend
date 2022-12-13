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
import com.erp.model.dmp.entity.DmpDeliveryDetailInfoEntity;
import com.erp.model.dmp.entity.DmpDeliveryDetailItemEntity;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.model.dmp.entity.GyyAppEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.gyy.GyyDeliveryDetailEntity;
import com.erp.model.dmp.gyy.bean.DeliveryDetailsBean;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpDeliveryDetailInfoService;
import com.erp.server.dmp.pull.service.dmp.DmpDeliveryDetailItemService;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import com.erp.server.dmp.utils.GyyUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 管易云出库详情
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.GY_ERP_TRADE_DELIVERY_GET)
public class GyyDeliveryDetailServiceImpl implements IReportSaveService {

    @Resource
    private MongoService mongoService;

    @Resource
    private DmpErrorLogService dmpErrorLogService;

    @Resource
    private DmpDeliveryDetailInfoService dmpDeliveryDetailInfoService;

    @Resource
    private DmpDeliveryDetailItemService dmpDeliveryDetailItemService;

    public static void main(String[] args) {
        GyyDeliveryDetailServiceImpl gyyOrderInfoService = new GyyDeliveryDetailServiceImpl();
        PlatformApiEnum platformApiEnum = PlatformApiEnum.getEnumByType("gy.erp.trade.deliverys.get");
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode("gy.erp.trade.deliverys.get");
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
        List<GyyDeliveryDetailEntity> orderEntities = gyyOrderInfoService.pullDate(requestDTO);
        System.out.println(orderEntities);
    }

    @Override
    public void pullDataSave(RequestDTO dto) throws Exception {
        List<GyyDeliveryDetailEntity> gyyDeliveryDetailEntityList = pullDate(dto);
        if (gyyDeliveryDetailEntityList != null && gyyDeliveryDetailEntityList.size() > 0) {
            for (GyyDeliveryDetailEntity gyyDeliveryDetailEntity : gyyDeliveryDetailEntityList) {
                OrderMongoDTO orderMongoDTO = new OrderMongoDTO();
                orderMongoDTO.setPlatformCode(gyyDeliveryDetailEntity.getPlatformCode());
                List<GyyDeliveryDetailEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_GYY_DELIVERY_DETAIL, GyyDeliveryDetailEntity.class);
                if (mongoData != null && mongoData.size() > 0) {
                    for (GyyDeliveryDetailEntity mongoDatum : mongoData) {
                        // 比较数据是否相同
                        if (!mongoDatum.toString().equals(gyyDeliveryDetailEntity.toString())) {
                            // 修改数据
                            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(gyyDeliveryDetailEntity), MapUtil.class);
                            try {
                                mongoService.updateMongoData(orderMongoDTO, mapUtil, MongoTableNameContant.ORIGINAL_GYY_DELIVERY_DETAIL, GyyDeliveryDetailEntity.class);
                            } catch (Exception e) {
                                DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity();
                                dmpErrorLogEntity.setTaskId(dto.getJobTaskDTO().getId());
                                dmpErrorLogEntity.setParams("");
                                dmpErrorLogEntity.setErrorMsg("==== 管易云修改mongodb出库详情失败，[ 订单号 = " + gyyDeliveryDetailEntity.getCode() + "], 错误信息 = " + e.getMessage());
                                dmpErrorLogEntity.setReturnMsg("");
                                dmpErrorLogService.add(dmpErrorLogEntity);
                                throw new RuntimeException("==== 管易云修改mongodb出库详情失败，[ 订单号 = " + gyyDeliveryDetailEntity.getCode() + "], 错误信息 = " + e.getMessage());
                            }
                        }
                    }
                } else {
                    mongoService.saveMongoData(gyyDeliveryDetailEntity, MongoTableNameContant.ORIGINAL_GYY_DELIVERY_DETAIL);
                }
                //存储数据到中台
                analysisDeliveryDetail(gyyDeliveryDetailEntity);
            }
        }
    }

    /**
     * 请求管易云退款信息接口
     *
     * @param dto
     * @return
     */
    public List<GyyDeliveryDetailEntity> pullDate(RequestDTO dto) {
        List<GyyDeliveryDetailEntity> infoArrayList = new ArrayList<>();
        try {
            JobTaskDTO jobTask = dto.getJobTaskDTO();
            Integer lastTime = jobTask.getLastTime();
            Integer nextTime = jobTask.getNextTime();
            String st = "";
            String sd = "";
            if (lastTime != 0 && nextTime != 0) {
                Date date = new Date(Long.valueOf(lastTime - (10L * 60L)) * 1000L);
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
            String url = UrlContant.GYY_HOST;
            String method = jobTask.getApiCode();
            String appKey = gyyAppEntity.getAppKey();
            String sessionKey = gyyAppEntity.getSessionKey();
            String secretKey = gyyAppEntity.getSecretKey();

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
                datas.put("method", method);
                datas.put("appkey", appKey);
                datas.put("sessionkey", sessionKey);
                datas.put("start_modify_date", st);
                datas.put("end_modify_date", sd);
                datas.put("page_no", pageIndex);
                datas.put("page_size", pageSize);
                datas.put("delivery", 1);

                String str = JSONObject.toJSONString(datas);
                String sign = GyyUtils.sign(str, secretKey);
                datas.put("sign", sign);
                // 将传参转为Json格式
                String jsonData = JSONObject.toJSONString(datas);

                //设置请求头
                Map<String, String> headerMap = new HashMap<>();
                headerMap.put("Content-Type", "text/json");

                Map<String, Object> stringObjectMap = null;
                try {
                    stringObjectMap = httpCommonUtil.sendOkhttp(url, jsonData, null, headerMap, RequestMethod.POST);
                    if (Boolean.valueOf(stringObjectMap.get("success").toString())) {
                        List<GyyDeliveryDetailEntity> dataList = JSONObject.parseArray(String.valueOf(stringObjectMap.get("deliverys")), GyyDeliveryDetailEntity.class);
                        totalCount = Integer.valueOf(stringObjectMap.get("total").toString());
                        pageCount = (totalCount + pageSize - 1) / pageSize;
                        infoArrayList.addAll(dataList);
                    } else {
                        log.info(" ===== 管易云拉取出库详情失败，错误信息：+" + stringObjectMap + " ====");
                        throw new RuntimeException(" ===== 管易云拉取出库详情失败，错误信息：+" + stringObjectMap + " ====");
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
                    break;
                }
                pageIndex++;
            }
        } catch (Exception e) {
            log.info(" ===== 获取管易云出库详情列表数据失败， 错误信息 = { " + e.getMessage() + " }");
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
    @Transactional
    public void analysisDeliveryDetail(GyyDeliveryDetailEntity gyyDeliveryDetailEntity) throws Exception {
        DmpDeliveryDetailInfoEntity deliveryDetailInfoEntity = new DmpDeliveryDetailInfoEntity();
        SimpleDateFormat sdf = new SimpleDateFormat(EnumTimePattern.y_m_dhms.toTimePattern());

        //单据编号
        deliveryDetailInfoEntity.setBillNo(gyyDeliveryDetailEntity.getCode());

        //订单编号
        deliveryDetailInfoEntity.setOrderNo(gyyDeliveryDetailEntity.getPlatformCode());

        //物流单号
        deliveryDetailInfoEntity.setLogisticsNo(gyyDeliveryDetailEntity.getExpressNo());

        //客户名称
        deliveryDetailInfoEntity.setCustomerName(gyyDeliveryDetailEntity.getVipName());

        //平台名称
        deliveryDetailInfoEntity.setPlatformName("");

        //店铺编号
        deliveryDetailInfoEntity.setShopNo(gyyDeliveryDetailEntity.getShopCode());

        //店铺名称
        deliveryDetailInfoEntity.setShopName(gyyDeliveryDetailEntity.getShopName());

        String currencyCode = "";

        BigDecimal totalCostPrice = BigDecimal.ZERO;
        String BusinessmanName = "";
        for (DeliveryDetailsBean detail : gyyDeliveryDetailEntity.getDetails()) {
            totalCostPrice = totalCostPrice.add(detail.getTotalCostPrice());
            currencyCode = detail.getCurrencyCode();
            BusinessmanName = detail.getBusinessmanName();
        }

        //订单成本价
        deliveryDetailInfoEntity.setItemTotalCost(totalCostPrice);

        //订单总价
        deliveryDetailInfoEntity.setOrderTotalCost(gyyDeliveryDetailEntity.getAmount());

        //国家英文名称
        deliveryDetailInfoEntity.setCountryNameEn("China");

        //国家中文名称
        deliveryDetailInfoEntity.setCountryNameCn("中国");

        if (StringUtils.isNotBlank(gyyDeliveryDetailEntity.getAreaName())) {
            String[] split = gyyDeliveryDetailEntity.getAreaName().split("-");
            if (split.length >= 1) {
                //买家省份
                deliveryDetailInfoEntity.setProvince(split[0]);
            }
            if (split.length >= 2) {
                //买家城市
                deliveryDetailInfoEntity.setCity(split[1]);
            }
            if (split.length >= 3) {
                //所属区域
                deliveryDetailInfoEntity.setDistrict(split[2]);
            }
        }

        //买家地址1
        deliveryDetailInfoEntity.setManStreet(gyyDeliveryDetailEntity.getReceiverAddress());

        //买家地址2
        deliveryDetailInfoEntity.setSecondStreet("");


        //币种
        deliveryDetailInfoEntity.setCurrencyCode(currencyCode);

        //汇率
        deliveryDetailInfoEntity.setCurrencyRate(BigDecimal.ZERO);

        //运费
        deliveryDetailInfoEntity.setShippingFee(gyyDeliveryDetailEntity.getPostFee());

        //补贴金额
        deliveryDetailInfoEntity.setSubsidyAmount(BigDecimal.ZERO);

        //销售部门
        deliveryDetailInfoEntity.setSaleDeptName("");

        //销售员编号
        deliveryDetailInfoEntity.setSalesManId("");

        //销售员名称
        deliveryDetailInfoEntity.setSalesManName(BusinessmanName);

        Integer status = 1;
        if (gyyDeliveryDetailEntity.getCancel()) {
            status = 2;
        }

        //状态 1.已发货 2..已作废
        deliveryDetailInfoEntity.setStatus(status);


        //平台单据审核时间
        deliveryDetailInfoEntity.setPlatformApproveTime(null);

        //平台单据创建时间
        if (StringUtils.isNotBlank(gyyDeliveryDetailEntity.getCreateDate()) && !gyyDeliveryDetailEntity.getCreateDate().equals("null")) {
            deliveryDetailInfoEntity.setPlatformCreateTime(sdf.parse(gyyDeliveryDetailEntity.getCreateDate()));
        }

        //平台单据修改时间
        if (StringUtils.isNotBlank(gyyDeliveryDetailEntity.getModifyDate()) && !gyyDeliveryDetailEntity.getModifyDate().equals("null")) {
            deliveryDetailInfoEntity.setPlatformUpdateTime(sdf.parse(gyyDeliveryDetailEntity.getModifyDate()));
        }

        //发货时间  //TODO 暂无
        if (StringUtils.isNotBlank(gyyDeliveryDetailEntity.getDeliveryStatusInfo().getDeliveryDate()) && !gyyDeliveryDetailEntity.getDeliveryStatusInfo().getDeliveryDate().equals("null")) {
            deliveryDetailInfoEntity.setDeliveryDate(sdf.parse(gyyDeliveryDetailEntity.getDeliveryStatusInfo().getDeliveryDate()));
        }

        //备注
        deliveryDetailInfoEntity.setRemark(gyyDeliveryDetailEntity.getSellerMemo());

        //平台标识
        deliveryDetailInfoEntity.setPlatformSign("管易云");

        //企业Id
        deliveryDetailInfoEntity.setCompanyId("");

        //企业名称
        deliveryDetailInfoEntity.setCompanyName("");

        //创建时间
        deliveryDetailInfoEntity.setCreateTime(new Date());

        //新增订单信息
        String deliveryDetailId = dmpDeliveryDetailInfoService.checkOrder(deliveryDetailInfoEntity);
        if (StringUtils.isNotBlank(deliveryDetailId)) {
            //新增订单商品信息
            analysisReturnOrderItem(gyyDeliveryDetailEntity, gyyDeliveryDetailEntity.getDetails(), deliveryDetailId);
        }
    }

    /**
     * 解析出库详情商品数据
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     **/
    @Transactional
    public void analysisReturnOrderItem(GyyDeliveryDetailEntity gyyDeliveryDetailEntity, List<DeliveryDetailsBean> orderItem, String deliveryDetailId) {
        List<DmpDeliveryDetailItemEntity> orderItemList = new ArrayList<>();
        for (DeliveryDetailsBean itemEntity : orderItem) {
            DmpDeliveryDetailItemEntity dmpReturnOrderItemEntity = new DmpDeliveryDetailItemEntity();

            //发货详情表id
            dmpReturnOrderItemEntity.setDeliveryDetailId(deliveryDetailId);

            //商品id
            dmpReturnOrderItemEntity.setItemId(itemEntity.getItemId());

            //平台sku
            dmpReturnOrderItemEntity.setPlatformSku(itemEntity.getSkuCode());

            //商品sku编号
            dmpReturnOrderItemEntity.setSkuNo(itemEntity.getSkuCode());

            //商品名称
            dmpReturnOrderItemEntity.setItemName(itemEntity.getSkuName());

            //商品成本价
            dmpReturnOrderItemEntity.setCostPrice(itemEntity.getTotalCostPrice());

            //商品单价
            dmpReturnOrderItemEntity.setSellPrice(itemEntity.getPrice());

            //商品数量
            dmpReturnOrderItemEntity.setQuantity(Double.valueOf(itemEntity.getQty()).intValue());

            //商品总价
            dmpReturnOrderItemEntity.setAmount(itemEntity.getAmount());

            //商品单位
            dmpReturnOrderItemEntity.setProductUnit(itemEntity.getItemUnitName());

            //是否是赠品 1. 是 2. 否
            if (itemEntity.getIsGift() == 0) {
                dmpReturnOrderItemEntity.setIsGift(1);
            } else {
                dmpReturnOrderItemEntity.setIsGift(2);
            }

            //属性
            dmpReturnOrderItemEntity.setSpecifics(itemEntity.getPlatformSkuName());

            //订单商品备注
            dmpReturnOrderItemEntity.setItemRemark(itemEntity.getMemo());

            //仓库
            dmpReturnOrderItemEntity.setStockName(gyyDeliveryDetailEntity.getWarehouseName());

            //库位
            dmpReturnOrderItemEntity.setWarehouseLocation(itemEntity.getLocationCode());

            orderItemList.add(dmpReturnOrderItemEntity);
        }
        checkOrderItem(orderItemList, deliveryDetailId);
    }

    /**
     * 校验出库详情商品信息在中台是否存在，存在就修改不存在则新增
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     **/
    public void checkOrderItem(List<DmpDeliveryDetailItemEntity> orderItem, String deliveryDetailId) {
        dmpDeliveryDetailItemService.deleteDeliveryDetailItemByDetailId(deliveryDetailId);
        dmpDeliveryDetailItemService.batchAdd(orderItem);
    }
}
