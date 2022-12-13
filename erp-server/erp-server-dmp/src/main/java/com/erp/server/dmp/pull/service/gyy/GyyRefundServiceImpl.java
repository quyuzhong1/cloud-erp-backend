package com.erp.server.dmp.pull.service.gyy;


import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.HttpCommonUtil;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.EnumTimePattern;

import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.constant.UrlContant;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.model.dmp.entity.DmpRefundInfoEntity;
import com.erp.model.dmp.entity.DmpRefundItemEntity;
import com.erp.model.dmp.entity.GyyAppEntity;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.gyy.GyyRefundEntity;
import com.erp.model.dmp.gyy.bean.RefundDetailsBean;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.dto.GyyRefundDTO;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import com.erp.server.dmp.pull.service.dmp.DmpRefundInfoService;
import com.erp.server.dmp.pull.service.dmp.DmpRefundItemService;
import com.erp.server.dmp.utils.GyyUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 管易云退款列表
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.GY_ERP_TRADE_REFUND_GET)
public class GyyRefundServiceImpl implements IReportSaveService {
    @Resource
    private MongoService mongoService;

    @Resource
    private DmpErrorLogService dmpErrorLogService;

    @Resource
    private DmpRefundInfoService dmpRefundInfoService;

    @Resource
    private DmpRefundItemService dmpRefundItemService;

    public static void main(String[] args) {
        GyyRefundServiceImpl gyyRefundService = new GyyRefundServiceImpl();
        PlatformApiEnum platformApiEnum = PlatformApiEnum.getEnumByType("gy.erp.trade.refund.get");
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode("gy.erp.trade.refund.get");
        jobTaskDTO.setApiId(11);
        jobTaskDTO.setApiName("管易云退款列表");
        jobTaskDTO.setId(35L);
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(0);
        jobTaskDTO.setNextTime(0);
        jobTaskDTO.setPlatformId(1);
        jobTaskDTO.setState(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(platformApiEnum);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        List<GyyRefundEntity> orderEntities = gyyRefundService.pullDate(requestDTO);
        System.out.println(orderEntities);
    }


    @Override
    public void pullDataSave(RequestDTO dto) throws Exception {
        List<GyyRefundEntity> gyyRefundEntityList = pullDate(dto);
        if (gyyRefundEntityList != null && gyyRefundEntityList.size() > 0) {
            for (GyyRefundEntity gyyRefundEntity : gyyRefundEntityList) {
                GyyRefundDTO gyyRefundDTO = new GyyRefundDTO();
                gyyRefundDTO.setPlatfromCode(gyyRefundEntity.getPlatfromCode());
                gyyRefundDTO.setRefundCode(gyyRefundEntity.getRefundCode());
                List<GyyRefundEntity> mongoData = mongoService.findMongoData(gyyRefundDTO, 0, 0, MongoTableNameContant.ORIGINAL_GYY_REFUND, GyyRefundEntity.class);
                if (mongoData != null && mongoData.size() > 0) {
                    for (GyyRefundEntity mongoDatum : mongoData) {
                        // 比较数据是否相同
                        if (!mongoDatum.toString().equals(gyyRefundEntity.toString())) {
                            // 修改数据
                            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(gyyRefundEntity), MapUtil.class);
                            try {
                                mongoService.updateMongoData(gyyRefundDTO, mapUtil, MongoTableNameContant.ORIGINAL_GYY_REFUND, GyyRefundEntity.class);
                            } catch (Exception e) {
                                DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity();
                                dmpErrorLogEntity.setTaskId(dto.getJobTaskDTO().getId());
                                dmpErrorLogEntity.setParams("");
                                dmpErrorLogEntity.setErrorMsg("==== 管易云修改mongodb退款数据失败，[ 订单号 = " + gyyRefundEntity.getPlatfromCode() + "], 错误信息 = " + e.getMessage());
                                dmpErrorLogEntity.setReturnMsg("");
                                dmpErrorLogService.add(dmpErrorLogEntity);
                                throw new RuntimeException("==== 管易云修改mongodb退款数据失败，[ 订单号 = " + gyyRefundEntity.getPlatfromCode() + "], 错误信息 = " + e.getMessage());
                            }
                        }
                    }
                } else {
                    mongoService.saveMongoData(gyyRefundEntity, MongoTableNameContant.ORIGINAL_GYY_REFUND);
                }
                //存储数据到中台
                analysisRefundOrder(gyyRefundEntity);
            }
        }
    }

    /**
     * 请求管易云退款信息接口
     * @param dto
     * @return
     */
    public List<GyyRefundEntity> pullDate(RequestDTO dto) {
        List<GyyRefundEntity> infoArrayList = new ArrayList<>();
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

                String str = JSONObject.toJSONString(datas);
                String sign = GyyUtils.sign(str, secretKey);
                datas.put("sign", sign);
                // 将传参转为Json格式
                String jsonData = JSONObject.toJSONString(datas);

                //设置请求头
                Map<String,String> headerMap = new HashMap<>();
                headerMap.put("Content-Type", "text/json");

                Map<String, Object> stringObjectMap = null;
                try {
                    stringObjectMap = httpCommonUtil.sendOkhttp(url, jsonData, null, headerMap, RequestMethod.POST);
                    if (Boolean.valueOf(stringObjectMap.get("success").toString())) {
                        List<GyyRefundEntity> dataList = JSONObject.parseArray(String.valueOf(stringObjectMap.get("tradeRefunds")), GyyRefundEntity.class);
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
            log.info(" ===== 获取管易云退货订单列表数据失败， 错误信息 = { " + e.getMessage() + " }");
        }
        return infoArrayList;
    }

    /**
     * 解析退款订单数据
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     * @return void
     **/
    public void analysisRefundOrder(GyyRefundEntity gyyRefundEntity) throws Exception {
        DmpRefundInfoEntity dmpRefundInfoEntity = new DmpRefundInfoEntity();
        SimpleDateFormat sdf = new SimpleDateFormat(EnumTimePattern.y_m_dhms.toTimePattern());

        //平台订单编号
        dmpRefundInfoEntity.setPlatformOrderId(gyyRefundEntity.getPlatfromCode());

        //退货单号
        dmpRefundInfoEntity.setRefundId(gyyRefundEntity.getRefundCode());

        //币别编号
        dmpRefundInfoEntity.setCurrencyCode("CNY");

        //退货金额
        dmpRefundInfoEntity.setRefundAmount(gyyRefundEntity.getAmount());

        //退款类型：1、未收到货部分退款 2、未收到货全额退款 3、已收到货部分退款 4、已收到货全额退款
        dmpRefundInfoEntity.setRefundType(null);

        //退款原因
        dmpRefundInfoEntity.setRefundReasonDesc(gyyRefundEntity.getReason());

        //退款备注
        dmpRefundInfoEntity.setRefundRemark(gyyRefundEntity.getNote());

        Integer refundStatus = 4;
        if (gyyRefundEntity.getApprove()) {
            refundStatus = 3;
        } else {
            refundStatus = 2;
        }

        if (gyyRefundEntity.getCancel()) {
            refundStatus = 6;
        }

        if (gyyRefundEntity.getCancel()) {
            refundStatus = 6;
        }

        if (gyyRefundEntity.getAgreeRefuse() != null) {
            Integer agreeRefuse = gyyRefundEntity.getAgreeRefuse();
            if (agreeRefuse == 1) {
                refundStatus = 4;
            } else if (agreeRefuse == 1) {
                refundStatus = 5;
            }
        }

        //退款状态：1、新建退款 2、审核中 3、财务审核 4、成功 5、失败 6、作废
        dmpRefundInfoEntity.setRefundStatus(refundStatus);

        //申请时间
        if (StringUtils.isNotBlank(gyyRefundEntity.getCreateDate())) {
            dmpRefundInfoEntity.setRefundCreateTime(sdf.parse(gyyRefundEntity.getCreateDate()));
        }

        //店铺编号
        dmpRefundInfoEntity.setShopNo(gyyRefundEntity.getShopCode());

        //店铺名称
        dmpRefundInfoEntity.setShopName(gyyRefundEntity.getShopCode());

        //平台名称
        dmpRefundInfoEntity.setPlatformName("");

        //退款时间
        if (StringUtils.isNotBlank(gyyRefundEntity.getAgreeDate())) {
            dmpRefundInfoEntity.setRefundTime(sdf.parse(gyyRefundEntity.getAgreeDate()));
        }

        //汇率
        dmpRefundInfoEntity.setCurrencyRate(BigDecimal.ZERO);

        //国家二字码 例如：US
        dmpRefundInfoEntity.setCountryCode("CN");

        //国家中文名
        dmpRefundInfoEntity.setCountryCn("中国");

        //国家英文名
        dmpRefundInfoEntity.setCountryEn("China");

        //平台交易号
        dmpRefundInfoEntity.setSalesRecordNumber(gyyRefundEntity.getRefundCode());

        //买家用户Id
        dmpRefundInfoEntity.setBuyerUserId("");

        //买家用户名
        dmpRefundInfoEntity.setBuyerName("");

        //原始订单金额
        dmpRefundInfoEntity.setItemTotalOrigin(gyyRefundEntity.getAmount());

        //原始订单运费金额
        dmpRefundInfoEntity.setShippingTotalOrigin(BigDecimal.ZERO);

        //订单时间
        if (StringUtils.isNotBlank(gyyRefundEntity.getCreateDate())) {
            dmpRefundInfoEntity.setOrderTime(sdf.parse(gyyRefundEntity.getCreateDate()));
        }

        //发货时间
        dmpRefundInfoEntity.setExpressTime(null);

        //退货图片多个用英文 , 隔开
        dmpRefundInfoEntity.setPictureUrl("");

        //平台最后修改时间
        if (StringUtils.isNotBlank(gyyRefundEntity.getModifyDate())) {
            dmpRefundInfoEntity.setPlatformUpdateTime(sdf.parse(gyyRefundEntity.getModifyDate()));
        }

        //包裹单号
        dmpRefundInfoEntity.setTrackNumber(null);

        //平台标识
        dmpRefundInfoEntity.setPlatformSign("管易云");

        dmpRefundInfoEntity.setCreateTime(new Date());

        //新增订单信息
        String refundInfoId = dmpRefundInfoService.checkOrder(dmpRefundInfoEntity);
        if (StringUtils.isNotBlank(refundInfoId)) {
            //新增订单商品信息
            analysisRefundOrderItem(gyyRefundEntity.getDetails(), refundInfoId);
        }
    }

    /**
     * 解析退款订单商品数据
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     * @return void
     **/
    public void analysisRefundOrderItem(List<RefundDetailsBean> refundOrderItemEntityList, String refundInfoId) {
        List<DmpRefundItemEntity> orderItemList = new ArrayList<>();
        for (RefundDetailsBean refundDetailsBean : refundOrderItemEntityList) {
            DmpRefundItemEntity dmpRefundItemEntity = new DmpRefundItemEntity();

            //退款表id
            dmpRefundItemEntity.setRefundId(refundInfoId);

            //sku编号
            dmpRefundItemEntity.setSkuNo(refundDetailsBean.getItemCode());

            //订单原始商品数量
            dmpRefundItemEntity.setQuantity(refundDetailsBean.getQty());

            //退款商品数量
            dmpRefundItemEntity.setRefundNum(refundDetailsBean.getQty());

            //是否属于组合sku：0. 否 1. 是
            dmpRefundItemEntity.setIsCombo(0);

            orderItemList.add(dmpRefundItemEntity);
        }
        checkOrderItem(orderItemList, refundInfoId);
    }


    /**
     * 校验退款商品数据在中台是否存在，存在就修改不存在则新增
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    public void checkOrderItem(List<DmpRefundItemEntity> orderItem, String returnOrderId) {
        dmpRefundItemService.deleteRefundItemByRefundId(returnOrderId);
        dmpRefundItemService.batchAdd(orderItem);
    }
}
