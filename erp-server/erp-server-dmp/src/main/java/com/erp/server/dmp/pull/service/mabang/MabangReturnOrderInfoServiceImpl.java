package com.erp.server.dmp.pull.service.mabang;

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
import com.erp.model.dmp.entity.DmpReturnOrderInfoEntity;
import com.erp.model.dmp.entity.DmpReturnOrderItemEntity;
import com.erp.model.dmp.entity.MabangAppEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.mabang.OrderEntity;
import com.erp.model.dmp.mabang.ReturnOrderEntity;
import com.erp.model.dmp.mabang.ReturnOrderItemEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import com.erp.server.dmp.pull.service.dmp.DmpReturnOrderInfoService;
import com.erp.server.dmp.pull.service.dmp.DmpReturnOrderItemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 马帮退货订单
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.ORDER_GET_RETURN_ORDER_LIST)
public class MabangReturnOrderInfoServiceImpl implements IReportSaveService {
    @Resource
    private MongoService mongoService;

    @Resource
    private DmpErrorLogService dmpErrorLogService;

    @Resource
    private DmpReturnOrderInfoService dmpReturnOrderInfoService;

    @Resource
    private DmpReturnOrderItemService dmpReturnOrderItemService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public static void main(String[] args) {
        MabangOrderInfoServiceImpl getOrderInfoService = new MabangOrderInfoServiceImpl();
        PlatformApiEnum platformApiEnum = PlatformApiEnum.getEnumByType("MABANG_GET_ORDER_LIST_TASK");
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode("order-get-order-list");
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
        System.out.println(orderEntities);
    }
    /**
     * 拉取退货订单数据
     * @param dto 任务信息
     * @return
     */
    @Override
    public void pullDataSave(RequestDTO dto) throws Exception {
        List<ReturnOrderEntity> returnOrderEntities = pullDate(dto);
        if (returnOrderEntities != null && returnOrderEntities.size() > 0) {
            for (ReturnOrderEntity returnOrderEntity : returnOrderEntities) {
                OrderMongoDTO orderMongoDTO = new OrderMongoDTO();
                orderMongoDTO.setPlatformOrderId(returnOrderEntity.getPlatformOrderId());
                List<ReturnOrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_MABANG_RETURN_ORDER, ReturnOrderEntity.class);
                if (mongoData != null && mongoData.size() > 0) {
                    for (ReturnOrderEntity mongoDatum : mongoData) {
                        // 比较数据是否相同
                        if (!mongoDatum.toString().equals(returnOrderEntity.toString())) {
                            // 修改数据
                            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(returnOrderEntity), MapUtil.class);
                            try {
                                mongoService.updateMongoData(orderMongoDTO, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_RETURN_ORDER, ReturnOrderEntity.class);
                            } catch (Exception e) {
                                DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity();
                                dmpErrorLogEntity.setTaskId(dto.getJobTaskDTO().getId());
                                dmpErrorLogEntity.setParams("");
                                dmpErrorLogEntity.setErrorMsg("==== 马帮修改mongodb退货数据失败，[ 订单号 = " + returnOrderEntity.getPlatformOrderId() + "], 错误信息 = " + e.getMessage());
                                dmpErrorLogEntity.setReturnMsg("");
                                dmpErrorLogEntity.setCreateTime(new Date());
                                dmpErrorLogService.add(dmpErrorLogEntity);
                                throw new RuntimeException("==== 马帮修改mongodb退货数据失败，[ 订单号 = " + returnOrderEntity.getPlatformOrderId() + "], 错误信息 = " + e.getMessage());
                            }
                        }
                    }
                } else {
                    mongoService.saveMongoData(returnOrderEntity, MongoTableNameContant.ORIGINAL_MABANG_RETURN_ORDER);
                }
                //存储数据到中台
                analysisReturnOrder(returnOrderEntity);
            }
        }
    }

    /**
     * 请求马帮退货订单接口
     * @param dto
     * @return
     */
    public List<ReturnOrderEntity> pullDate(RequestDTO dto) {
        List<ReturnOrderEntity> infoArrayList = new ArrayList<>();
        try {
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
                st = null;
                sd = null;
                dto.getJobTaskDTO().setLastTime(date);
            }
            MabangAppEntity mabangAppEntity = new MabangAppEntity();

            //每次最多获取100条
            Integer pageSize = 100;
            //当前页数
            Integer pageIndex = 1;
            //总页数
            Integer pageCount = 1;
            HttpCommonUtil httpCommonUtil = new HttpCommonUtil();
            while (pageIndex <= pageCount) {
                Map<String, Object> paramsMap = new HashMap();
                paramsMap.put("updateDateStart", st);
                paramsMap.put("updateDateEnd", sd);
                paramsMap.put("page", pageIndex);
                paramsMap.put("rowsPerPage", pageSize);

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
                Map<String,String> headerMap = new HashMap<>();
                headerMap.put("Content-Type", "application/json");
                headerMap.put("Authorization", authorization);

                Map<String, Object> stringObjectMap = null;
                try {
                    stringObjectMap = httpCommonUtil.sendOkhttp(UrlContant.MABANG_HOST, jsonData, null, headerMap, RequestMethod.POST);
                    if (stringObjectMap.get("code").equals(200)) {
                        JSONObject jsonObject = JSONObject.parseObject(String.valueOf(stringObjectMap.get("data")));
                        List<ReturnOrderEntity> dataList = JSONObject.parseArray(jsonObject.get("data").toString(), ReturnOrderEntity.class);
                        pageCount = Integer.valueOf(jsonObject.get("pageCount").toString());
                        infoArrayList.addAll(dataList);
                    } else {
                        log.info(" ===== 马帮拉取退货订单失败，错误信息：+" + stringObjectMap + " ==== 时间戳：" + new Date().getTime() + "");
                        throw new RuntimeException(" ===== 马帮拉取退货订单失败，错误信息：+" + stringObjectMap + " ====");
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
                }
                pageIndex++;
            }
        } catch (Exception e) {
            log.info(" ===== 获取马帮退货订单列表数据失败， 错误信息 = { " + e.getMessage() + " }");
            throw new RuntimeException(" ===== 获取马帮退货订单列表数据失败， 错误信息 = { " + e.getMessage() + " }");
        }
        return infoArrayList;
    }

    /**
     * 解析订单数据
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     * @return void
     **/
    @Transactional
    public void analysisReturnOrder(ReturnOrderEntity returnOrderEntity) throws Exception {
        DmpReturnOrderInfoEntity dmpReturnOrderInfoEntity = new DmpReturnOrderInfoEntity();
        SimpleDateFormat sdf = new SimpleDateFormat(EnumTimePattern.y_m_dhms.toTimePattern());

        //平台订单编号
        dmpReturnOrderInfoEntity.setPlatformOrderId(returnOrderEntity.getPlatformOrderId());

        //退货单号
        dmpReturnOrderInfoEntity.setReturnOrderId(returnOrderEntity.getPlatformOrderId());

        //店铺编号
        dmpReturnOrderInfoEntity.setShopNo(returnOrderEntity.getShopId());

        //店铺名称
        dmpReturnOrderInfoEntity.setShopNo(returnOrderEntity.getShopName());

        //付款时间
        if (StringUtils.isNotBlank(returnOrderEntity.getPaidTime())) {
            dmpReturnOrderInfoEntity.setPaidTime(sdf.parse(returnOrderEntity.getPaidTime()));
        }

        //发货时间
        if (StringUtils.isNotBlank(returnOrderEntity.getExpressTime())) {
            dmpReturnOrderInfoEntity.setExpressTime(sdf.parse(returnOrderEntity.getExpressTime()));
        }

        //状态：1待处理 2已退款 3已重发 4已完成 5已作废
        dmpReturnOrderInfoEntity.setStatus(returnOrderEntity.getStatus());

        //平台交易号
        dmpReturnOrderInfoEntity.setSalesRecordNumber(returnOrderEntity.getSalesRecordNumber());

        //订单金额
        dmpReturnOrderInfoEntity.setOrderFee(returnOrderEntity.getOrderFee());

        //订单重量
        dmpReturnOrderInfoEntity.setOrderWeight(returnOrderEntity.getOrderWeight());

        /**
         * 平台名称
         */
        dmpReturnOrderInfoEntity.setPlatformName(returnOrderEntity.getPlatformId());

        //国家英文名称
        dmpReturnOrderInfoEntity.setCountryNameEn(returnOrderEntity.getCountryNameEN());

        //国家中文名称
        dmpReturnOrderInfoEntity.setCountryNameCn(returnOrderEntity.getCountryNameCN());

        //买家账号
        dmpReturnOrderInfoEntity.setBuyerUserId(returnOrderEntity.getBuyerUserId());

        //买家姓名
        dmpReturnOrderInfoEntity.setBuyerName(returnOrderEntity.getBuyerName());

        //登记人编号
        dmpReturnOrderInfoEntity.setEmployeeId(returnOrderEntity.getEmployeeId());

        //登记人名称
        dmpReturnOrderInfoEntity.setEmployeeName(returnOrderEntity.getEmployeeName());

        //备注
        dmpReturnOrderInfoEntity.setRemark(returnOrderEntity.getRemark());

        //退货信息创建时间
        if (StringUtils.isNotBlank(returnOrderEntity.getCreateDate())) {
            dmpReturnOrderInfoEntity.setReturnCreateTime(sdf.parse(returnOrderEntity.getCreateDate()));
        }

        //退款时间
        if (StringUtils.isNotBlank(returnOrderEntity.getRefundTime())) {
            dmpReturnOrderInfoEntity.setRefundTime(sdf.parse(returnOrderEntity.getRefundTime()));
        }

        //币种
        dmpReturnOrderInfoEntity.setCurrencyCode(returnOrderEntity.getCountryCode());

        //汇率
        dmpReturnOrderInfoEntity.setCurrencyRate(returnOrderEntity.getCurrencyRate());

        //平台标识
        dmpReturnOrderInfoEntity.setPlatformSign("马帮");

        dmpReturnOrderInfoEntity.setCreateTime(LocalDateTime.now());

        //新增订单信息
        String orderInfoId = dmpReturnOrderInfoService.checkOrder(dmpReturnOrderInfoEntity);
        if (StringUtils.isNotBlank(orderInfoId)) {
            //新增订单商品信息
            analysisReturnOrderItem(returnOrderEntity.getItem(), orderInfoId);
        }
    }

    /**
     * 解析退货订单商品数据
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     * @return void
     **/
    public void analysisReturnOrderItem(List<ReturnOrderItemEntity> orderItem, String orderId) {
        List<DmpReturnOrderItemEntity> orderItemList = new ArrayList<>();
        for (ReturnOrderItemEntity orderItemBean : orderItem) {
            DmpReturnOrderItemEntity dmpReturnOrderItemEntity = new DmpReturnOrderItemEntity();
            //退货订单表id
            dmpReturnOrderItemEntity.setReturnOrderId(orderId);

            //sku编号
            dmpReturnOrderItemEntity.setSkuNo(orderItemBean.getStockSku());

            //商品名称
            dmpReturnOrderItemEntity.setItemName(orderItemBean.getTitle());

            //买家购买数量
            dmpReturnOrderItemEntity.setQuantity(orderItemBean.getQuantity());

            //商品单位
            dmpReturnOrderItemEntity.setProductUnit(orderItemBean.getProductUnit());

            //商品图片地址
            dmpReturnOrderItemEntity.setPictureUrl(orderItemBean.getPictureUrl());

            //售价
            dmpReturnOrderItemEntity.setSellPrice(orderItemBean.getSellPrice());

            //物品属性
            dmpReturnOrderItemEntity.setSpecifics(orderItemBean.getSpecifics());

            //状态 1待处理 2验货入库 3自然耗损
            dmpReturnOrderItemEntity.setStatus(orderItemBean.getStatus());

            orderItemList.add(dmpReturnOrderItemEntity);
        }
        checkOrderItem(orderItemList, orderId);
//        dmpOrderItemService.batchAdd(orderItemList);
//        return orderItemList;
    }

    /**
     * 校验退货订单商品信息在中台是否存在，存在就修改不存在则新增
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    public void checkOrderItem(List<DmpReturnOrderItemEntity> orderItem, String returnOrderId) {
        dmpReturnOrderItemService.deleteOrderByReturnOrderId(returnOrderId);
        dmpReturnOrderItemService.batchAdd(orderItem);
    }
}
