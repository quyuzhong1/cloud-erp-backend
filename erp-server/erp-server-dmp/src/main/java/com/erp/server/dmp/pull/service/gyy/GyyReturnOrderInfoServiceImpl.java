package com.erp.server.dmp.pull.service.gyy;

import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.HttpCommonUtil;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.constant.UrlContant;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.model.dmp.entity.DmpReturnOrderInfoEntity;
import com.erp.model.dmp.entity.DmpReturnOrderItemEntity;
import com.erp.model.dmp.entity.GyyAppEntity;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.gyy.GyyReturnOrderEntity;
import com.erp.model.dmp.gyy.bean.ReturnOrderDetailsBean;
import com.erp.model.dmp.gyy.bean.ReturnOrderPayments;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import com.erp.server.dmp.pull.service.dmp.DmpReturnOrderInfoService;
import com.erp.server.dmp.pull.service.dmp.DmpReturnOrderItemService;
import com.erp.server.dmp.utils.GyyUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 管易云退货订单
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.GY_ERP_TRADE_RETURN_GET)
public class GyyReturnOrderInfoServiceImpl implements IReportSaveService {
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
        GyyReturnOrderInfoServiceImpl gyyReturnOrderInfoService = new GyyReturnOrderInfoServiceImpl();
        PlatformApiEnum platformApiEnum = PlatformApiEnum.getEnumByType("gy.erp.trade.return.get");
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode("gy.erp.trade.return.get");
        jobTaskDTO.setApiId(9);
        jobTaskDTO.setApiName("获取退货订单数据");
        jobTaskDTO.setId(34L);
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(null);
        jobTaskDTO.setNextTime(null);
        jobTaskDTO.setPlatformId(1);
        jobTaskDTO.setState(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(platformApiEnum);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        List<GyyReturnOrderEntity> orderEntities = gyyReturnOrderInfoService.pullDate(requestDTO);
        System.out.println(orderEntities);
    }

    /**
     * 拉取退货订单数据
     *
     * @param dto 任务信息
     */
    @Override
    public void pullDataSave(RequestDTO dto) throws Exception {
        List<GyyReturnOrderEntity> gyyReturnOrderEntityList = pullDate(dto);
        if (gyyReturnOrderEntityList != null && gyyReturnOrderEntityList.size() > 0) {
            for (GyyReturnOrderEntity gyyReturnOrderEntity : gyyReturnOrderEntityList) {
                OrderMongoDTO orderMongoDTO = new OrderMongoDTO();
                orderMongoDTO.setPlatformCode(gyyReturnOrderEntity.getPlatformCode());
                orderMongoDTO.setCode(gyyReturnOrderEntity.getCode());
                List<GyyReturnOrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_GYY_RETURN_ORDER, GyyReturnOrderEntity.class);
                if (mongoData != null && mongoData.size() > 0) {
                    for (GyyReturnOrderEntity mongoDatum : mongoData) {
                        // 比较数据是否相同
                        if (!mongoDatum.toString().equals(gyyReturnOrderEntity.toString())) {
                            // 修改数据
                            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(gyyReturnOrderEntity), MapUtil.class);
                            try {
                                mongoService.updateMongoData(orderMongoDTO, mapUtil, MongoTableNameContant.ORIGINAL_GYY_RETURN_ORDER, GyyReturnOrderEntity.class);
                            } catch (Exception e) {
                                DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity();
                                dmpErrorLogEntity.setTaskId(dto.getJobTaskDTO().getId());
                                dmpErrorLogEntity.setParams("");
                                dmpErrorLogEntity.setErrorMsg("==== 管易云修改mongodb退货数据失败，[ 订单号 = " + gyyReturnOrderEntity.getPlatformCode() + "], 错误信息 = " + e.getMessage());
                                dmpErrorLogEntity.setReturnMsg("");
                                dmpErrorLogEntity.setCreateTime(LocalDateTime.now());
                                dmpErrorLogService.add(dmpErrorLogEntity);
                                throw new RuntimeException("==== 管易云修改mongodb退货数据失败，[ 订单号 = " + gyyReturnOrderEntity.getPlatformCode() + "], 错误信息 = " + e.getMessage());
                            }
                        }
                    }
                } else {
                    mongoService.saveMongoData(gyyReturnOrderEntity, MongoTableNameContant.ORIGINAL_GYY_RETURN_ORDER);
                }
                //存储数据到中台
                analysisReturnOrder(gyyReturnOrderEntity);
            }
        }
    }

    /**
     * 请求管易云退货订单接口
     *
     * @param dto
     * @return
     */
    public List<GyyReturnOrderEntity> pullDate(RequestDTO dto) {
        List<GyyReturnOrderEntity> infoArrayList = new ArrayList<>();
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
            datas.put("modify_start_date", st);
            datas.put("modify_end_date", sd);
            datas.put("page_no", pageIndex);
            datas.put("page_size", pageSize);
            datas.put("receive", 1);

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
                    List<GyyReturnOrderEntity> dataList = JSONObject.parseArray(String.valueOf(stringObjectMap.get("tradeReturns")), GyyReturnOrderEntity.class);
                    totalCount = Integer.valueOf(stringObjectMap.get("total").toString());
                    pageCount = (totalCount + pageSize - 1) / pageSize;
                    infoArrayList.addAll(dataList);
                } else {
                    log.info(" ===== 管易云拉取退货订单失败，错误信息：+" + stringObjectMap + " ====");
                    throw new RuntimeException(" ===== 管易云拉取退货订单失败，错误信息：+" + stringObjectMap + " ====");
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
                    dmpErrorLogEntity.setCreateTime(LocalDateTime.now());
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
    @Transactional
    public void analysisReturnOrder(GyyReturnOrderEntity gyyReturnOrderEntity) throws Exception {
        DmpReturnOrderInfoEntity dmpReturnOrderInfoEntity = new DmpReturnOrderInfoEntity();
        SimpleDateFormat sdf = new SimpleDateFormat(EnumTimePattern.y_m_dhms.toTimePattern());

        //平台订单编号
        dmpReturnOrderInfoEntity.setPlatformOrderId(gyyReturnOrderEntity.getPlatformCode());

        //退货单号
        dmpReturnOrderInfoEntity.setReturnOrderId(gyyReturnOrderEntity.getCode());

        //店铺编号
        dmpReturnOrderInfoEntity.setShopNo(gyyReturnOrderEntity.getShopCode());

        //店铺名称
        dmpReturnOrderInfoEntity.setShopName(gyyReturnOrderEntity.getShopName());
        List<ReturnOrderPayments> payments = gyyReturnOrderEntity.getPayments();
        if (payments.size() > 0) {
            //付款时间
            if (StringUtils.isNotBlank(payments.get(0).getPayTime())) {
                dmpReturnOrderInfoEntity.setPaidTime(sdf.parse(payments.get(0).getPayTime()));
            }
        }

        //发货时间
        dmpReturnOrderInfoEntity.setExpressTime(null);

        //0:未处理 1:同意退货 2:拒绝退货
        Integer status = 4;
        if (gyyReturnOrderEntity.getAgreeRefuse() != null) {
            switch (gyyReturnOrderEntity.getAgreeRefuse()) {
                case 0:
                    status = 1;
                    break;
                case 1:
                    status = 2;
                    break;
                case 2:
                    status = 4;
                    break;
                default:
                    break;
            }
        }

        //状态：1待处理 2已退款 3已重发 4已完成 5已作废
        dmpReturnOrderInfoEntity.setStatus(status);

        //平台交易号
        dmpReturnOrderInfoEntity.setSalesRecordNumber(gyyReturnOrderEntity.getCode());

        List<ReturnOrderDetailsBean> details = gyyReturnOrderEntity.getDetails();

        BigDecimal amount = new BigDecimal(BigInteger.ZERO);
        for (ReturnOrderDetailsBean detail : details) {
            amount = amount.add(detail.getAmount());
        }

        //订单金额
        dmpReturnOrderInfoEntity.setOrderFee(amount);

        //订单重量
        dmpReturnOrderInfoEntity.setOrderWeight(new BigDecimal(BigInteger.ZERO));

        //平台名称
        dmpReturnOrderInfoEntity.setPlatformName("");

        //国家英文名称
        dmpReturnOrderInfoEntity.setCountryNameEn("中国");

        //国家英文名称
        dmpReturnOrderInfoEntity.setCountryNameCn("China");

        //买家账号
        dmpReturnOrderInfoEntity.setBuyerUserId(gyyReturnOrderEntity.getVipCode());

        //买家姓名
        dmpReturnOrderInfoEntity.setBuyerName(gyyReturnOrderEntity.getReceiverName());

        //登记人编号
        dmpReturnOrderInfoEntity.setEmployeeId("");

        //登记人名称
        dmpReturnOrderInfoEntity.setEmployeeName(gyyReturnOrderEntity.getBusinessMan());

        //备注
        dmpReturnOrderInfoEntity.setRemark(gyyReturnOrderEntity.getNote());

        //退货信息创建时间
        if (StringUtils.isNotBlank(gyyReturnOrderEntity.getCreateDate())) {
            dmpReturnOrderInfoEntity.setReturnCreateTime(sdf.parse(gyyReturnOrderEntity.getCreateDate()));
        }

        //退款时间
        if (StringUtils.isNotBlank(gyyReturnOrderEntity.getApproveDate())) {
            dmpReturnOrderInfoEntity.setRefundTime(sdf.parse(gyyReturnOrderEntity.getApproveDate()));
        }

        //币种
        dmpReturnOrderInfoEntity.setCurrencyCode("CNY");

        //汇率
        dmpReturnOrderInfoEntity.setCurrencyRate(BigDecimal.ZERO);

        //平台标识
        dmpReturnOrderInfoEntity.setPlatformSign("管易云");

        dmpReturnOrderInfoEntity.setCreateTime(LocalDateTime.now());

        //新增订单信息
        String orderInfoId = dmpReturnOrderInfoService.checkOrder(dmpReturnOrderInfoEntity);
        if (StringUtils.isNotBlank(orderInfoId)) {
            //新增订单商品信息
            analysisReturnOrderItem(gyyReturnOrderEntity.getDetails(), orderInfoId);
        }
    }

    /**
     * 解析退货订单商品数据
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     **/
    public void analysisReturnOrderItem(List<ReturnOrderDetailsBean> orderItem, String orderId) {
        List<DmpReturnOrderItemEntity> orderItemList = new ArrayList<>();
        for (ReturnOrderDetailsBean orderItemBean : orderItem) {
            DmpReturnOrderItemEntity dmpReturnOrderItemEntity = new DmpReturnOrderItemEntity();
            //退货订单表id
            dmpReturnOrderItemEntity.setReturnOrderId(orderId);

            //sku编号
            dmpReturnOrderItemEntity.setSkuNo(orderItemBean.getItemCode());

            //商品名称
            dmpReturnOrderItemEntity.setItemName(orderItemBean.getItemName());

            //买家购买数量
            dmpReturnOrderItemEntity.setQuantity(orderItemBean.getQty());

            //商品单位
            dmpReturnOrderItemEntity.setProductUnit("");

            //商品图片地址
            dmpReturnOrderItemEntity.setPictureUrl("");

            //售价
            dmpReturnOrderItemEntity.setSellPrice(orderItemBean.getTotalCostPrice());

            //物品属性
            dmpReturnOrderItemEntity.setSpecifics(null);

            //状态 1待处理 2验货入库 3自然耗损
            dmpReturnOrderItemEntity.setStatus(null);

            //折扣后金额
            dmpReturnOrderItemEntity.setAmountAfter(new BigDecimal(orderItemBean.getAmountAfter()));

            orderItemList.add(dmpReturnOrderItemEntity);
        }
        checkOrderItem(orderItemList, orderId);
    }

    /**
     * 校验退货订单商品信息在中台是否存在，存在就修改不存在则新增
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     **/
    public void checkOrderItem(List<DmpReturnOrderItemEntity> orderItem, String returnOrderId) {
        dmpReturnOrderItemService.deleteOrderByReturnOrderId(returnOrderId);
        dmpReturnOrderItemService.batchAdd(orderItem);
    }
}
