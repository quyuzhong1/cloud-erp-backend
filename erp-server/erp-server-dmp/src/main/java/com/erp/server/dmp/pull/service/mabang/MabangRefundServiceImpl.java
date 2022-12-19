package com.erp.server.dmp.pull.service.mabang;

import com.alibaba.fastjson.JSONObject;
import com.common.core.security.HmacSHA256Utils;
import com.common.core.utils.HttpCommonUtil;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.constant.UrlContant;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.model.dmp.entity.DmpRefundInfoEntity;
import com.erp.model.dmp.entity.DmpRefundItemEntity;
import com.erp.model.dmp.entity.MabangAppEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.mabang.RefundOrderEntity;
import com.erp.model.dmp.mabang.RefundOrderItemEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import com.erp.server.dmp.pull.service.dmp.DmpRefundInfoService;
import com.erp.server.dmp.pull.service.dmp.DmpRefundItemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 马帮退款列表
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.ORDER_GET_REFUND_LIST)
public class MabangRefundServiceImpl implements IReportSaveService {
    @Resource
    private MongoService mongoService;

    @Resource
    private DmpErrorLogService dmpErrorLogService;

    @Resource
    private DmpRefundInfoService dmpRefundInfoService;

    @Resource
    private DmpRefundItemService dmpRefundItemService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 拉取退款数据
     *
     * @param dto 任务信息
     * @return
     */
    @Override
    public void pullDataSave(RequestDTO dto) throws Exception {
        List<RefundOrderEntity> refundOrderEntities = pullDate(dto);
        if (refundOrderEntities != null && refundOrderEntities.size() > 0) {
            for (RefundOrderEntity refundOrderEntity : refundOrderEntities) {
                OrderMongoDTO orderMongoDTO = new OrderMongoDTO();
                orderMongoDTO.setId(refundOrderEntity.getId());
                List<RefundOrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_MABANG_REFUND, RefundOrderEntity.class);
                if (mongoData != null && mongoData.size() > 0) {
                    for (RefundOrderEntity mongoDatum : mongoData) {
                        // 比较数据是否相同
                        if (!mongoDatum.toString().equals(refundOrderEntity.toString())) {
                            // 修改数据
                            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(refundOrderEntity), MapUtil.class);
                            try {
                                mongoService.updateMongoData(orderMongoDTO, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_REFUND, RefundOrderEntity.class);
                            } catch (Exception e) {
                                DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity();
                                dmpErrorLogEntity.setTaskId(dto.getJobTaskDTO().getId());
                                dmpErrorLogEntity.setParams("");
                                dmpErrorLogEntity.setErrorMsg("==== 马帮修改mongodb退款数据失败，[ 订单号 = " + refundOrderEntity.getPlatformOrderId() + "], 错误信息 = " + e.getMessage());
                                dmpErrorLogEntity.setReturnMsg("");
                                dmpErrorLogEntity.setCreateTime(new Date());
                                dmpErrorLogService.add(dmpErrorLogEntity);
                                throw new RuntimeException("==== 马帮修改mongodb退款数据失败，[ 订单号 = " + refundOrderEntity.getPlatformOrderId() + "], 错误信息 = " + e.getMessage());
                            }
                        }
                    }
                } else {
                    mongoService.saveMongoData(refundOrderEntity, MongoTableNameContant.ORIGINAL_MABANG_REFUND);
                }
                //存储数据到中台
                analysisRefundOrder(refundOrderEntity);
            }
        }
    }

    /**
     * 请求马帮退款信息接口
     *
     * @param dto
     * @return
     */
    public List<RefundOrderEntity> pullDate(RequestDTO dto) {
        List<RefundOrderEntity> infoArrayList = new ArrayList<>();
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
        MabangAppEntity mabangAppEntity = new MabangAppEntity();

        //每次最多获取1000条
        Integer pageSize = 1000;
        //当前页数
        Integer pageIndex = 1;
        //总页数
        Integer pageCount = 1;
        //总条数
        Integer totalCount = 0;
        HttpCommonUtil httpCommonUtil = new HttpCommonUtil();
        while (pageIndex <= pageCount) {
            Map<String, Object> paramsMap = new HashMap();
            paramsMap.put("timeStart", st);
            paramsMap.put("timeEnd", sd);
            paramsMap.put("page", pageIndex);
            paramsMap.put("pageSize", pageSize);

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
                    List<RefundOrderEntity> dataList = JSONObject.parseArray(jsonObject.get("data").toString(), RefundOrderEntity.class);
                    totalCount = Integer.valueOf(jsonObject.getString("total"));
                    pageCount = (totalCount + pageSize - 1) / pageSize;
                    infoArrayList.addAll(dataList);
                } else {
                    log.info(" ===== 马帮拉取退款信息失败，错误信息：+" + stringObjectMap + " ====");
                    throw new RuntimeException(" ===== 马帮拉取退款信息失败，错误信息：+" + stringObjectMap + " ====");
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
        return infoArrayList;
    }

    /**
     * 解析退款订单数据
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     **/
    public void analysisRefundOrder(RefundOrderEntity refundOrderEntity) throws Exception {
        DmpRefundInfoEntity dmpRefundInfoEntity = new DmpRefundInfoEntity();
        SimpleDateFormat sdf = new SimpleDateFormat(EnumTimePattern.y_m_dhms.toTimePattern());

        //平台订单编号
        dmpRefundInfoEntity.setPlatformOrderId(refundOrderEntity.getPlatformOrderId());

        //退款单号
        dmpRefundInfoEntity.setRefundId(refundOrderEntity.getRefundplatformOrderId());

        //币别编号
        dmpRefundInfoEntity.setCurrencyCode(refundOrderEntity.getCountryCode());

        //退货金额
        dmpRefundInfoEntity.setRefundAmount(refundOrderEntity.getApplyRefundMoney());

        //退款类型：1、未收到货部分退款 2、未收到货全额退款 3、已收到货部分退款 4、已收到货全额退款
        dmpRefundInfoEntity.setRefundType(refundOrderEntity.getRefundType());

        //退款原因
        dmpRefundInfoEntity.setRefundReasonDesc(refundOrderEntity.getRefundReasonDesc());

        //退款备注
        dmpRefundInfoEntity.setRefundRemark(refundOrderEntity.getNote());

        //退款状态：1、新建退款 2、审核中 3、财务审核 4、成功 5、失败 6、作废
        dmpRefundInfoEntity.setRefundStatus(refundOrderEntity.getFlag());

        //申请时间
        if (StringUtils.isNotBlank(refundOrderEntity.getCreateTime())) {
            dmpRefundInfoEntity.setRefundCreateTime(sdf.parse(refundOrderEntity.getCreateTime()));
        }

        //店铺编号
        dmpRefundInfoEntity.setShopNo(refundOrderEntity.getShopId());

        //店铺名称
        dmpRefundInfoEntity.setShopName(refundOrderEntity.getShopName());

        //平台名称
        dmpRefundInfoEntity.setPlatformName(refundOrderEntity.getPlatformName());

        //退款时间
        if (StringUtils.isNotBlank(refundOrderEntity.getRefundTime())) {
            dmpRefundInfoEntity.setRefundTime(sdf.parse(refundOrderEntity.getRefundTime()));
        }

        //汇率
        dmpRefundInfoEntity.setCurrencyRate(refundOrderEntity.getCurrencyRate());

        //国家二字码 例如：US
        dmpRefundInfoEntity.setCountryCode(refundOrderEntity.getCountryCode());

        //国家中文名
        dmpRefundInfoEntity.setCountryCn(refundOrderEntity.getCountryCn());

        //国家英文名
        dmpRefundInfoEntity.setCountryEn(refundOrderEntity.getCountryEn());

        //平台交易号
        dmpRefundInfoEntity.setSalesRecordNumber(refundOrderEntity.getSalesRecordNumber());

        //买家用户Id
        dmpRefundInfoEntity.setBuyerUserId(refundOrderEntity.getBuyerUserId());

        //买家用户名
        dmpRefundInfoEntity.setBuyerName(refundOrderEntity.getBuyerName());

        //原始订单金额
        dmpRefundInfoEntity.setItemTotalOrigin(refundOrderEntity.getItemTotalOrigin());

        //原始订单运费金额
        dmpRefundInfoEntity.setShippingTotalOrigin(refundOrderEntity.getShippingTotalOrigin());

        //订单时间
        if (StringUtils.isNotBlank(refundOrderEntity.getOrderTime())) {
            dmpRefundInfoEntity.setOrderTime(sdf.parse(refundOrderEntity.getOrderTime()));
        }

        //发货时间
        if (StringUtils.isNotBlank(refundOrderEntity.getExpressTime())) {
            dmpRefundInfoEntity.setExpressTime(sdf.parse(refundOrderEntity.getExpressTime()));
        }

        //退货图片多个用英文 , 隔开
        dmpRefundInfoEntity.setPictureUrl(refundOrderEntity.getPictureUrl());

        //平台最后修改时间
        if (StringUtils.isNotBlank(refundOrderEntity.getUpdateTime())) {
            dmpRefundInfoEntity.setPlatformUpdateTime(sdf.parse(refundOrderEntity.getUpdateTime()));
        }

        //包裹单号
        dmpRefundInfoEntity.setTrackNumber(refundOrderEntity.getTrackNumber());

        //平台标识
        dmpRefundInfoEntity.setPlatformSign("马帮");

        dmpRefundInfoEntity.setCreateTime(LocalDateTime.now());

        //新增订单信息
        String refundInfoId = dmpRefundInfoService.checkOrder(dmpRefundInfoEntity);
        if (StringUtils.isNotBlank(refundInfoId)) {
            //新增订单商品信息
            analysisRefundOrderItem(refundOrderEntity.getProductList(), refundInfoId);
        }
    }

    /**
     * 解析退款订单商品数据
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     **/
    public void analysisRefundOrderItem(List<RefundOrderItemEntity> refundOrderItemEntityList, String refundInfoId) {
        List<DmpRefundItemEntity> orderItemList = new ArrayList<>();
        for (RefundOrderItemEntity refundOrderItemEntity : refundOrderItemEntityList) {
            DmpRefundItemEntity dmpRefundItemEntity = new DmpRefundItemEntity();

            //退款表id
            dmpRefundItemEntity.setRefundId(refundInfoId);

            //sku编号
            dmpRefundItemEntity.setSkuNo(refundOrderItemEntity.getRefundStock());

            //订单原始商品数量
            dmpRefundItemEntity.setQuantity(refundOrderItemEntity.getStock_quantity());

            //退款商品数量
            dmpRefundItemEntity.setRefundNum(refundOrderItemEntity.getRefund_num());

            //是否属于组合sku：0. 否 1. 是
            dmpRefundItemEntity.setIsCombo(refundOrderItemEntity.getIsCombo());

            orderItemList.add(dmpRefundItemEntity);
        }
        checkOrderItem(orderItemList, refundInfoId);
    }

    /**
     * 校验退款商品数据在中台是否存在，存在就修改不存在则新增
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     **/
    public void checkOrderItem(List<DmpRefundItemEntity> orderItem, String returnOrderId) {
        dmpRefundItemService.deleteRefundItemByRefundId(returnOrderId);
        dmpRefundItemService.batchAdd(orderItem);
    }
}
