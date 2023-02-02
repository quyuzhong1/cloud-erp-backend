package com.erp.server.dmp.pull.service.mabang;

import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.model.dmp.entity.DmpRefundInfoEntity;
import com.erp.model.dmp.entity.DmpRefundItemEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.mabang.RefundOrderEntity;
import com.erp.model.dmp.mabang.RefundOrderItemEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import com.erp.server.dmp.pull.service.dmp.DmpRefundInfoService;
import com.erp.server.dmp.pull.service.dmp.DmpRefundItemService;
import com.erp.server.dmp.utils.MabangApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 马帮退款列表
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.ORDER_GET_REFUND_LIST)
public class MabangRefundServiceImpl implements IReportSaveService<RefundOrderEntity> {
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

    @Resource
    @Qualifier("mabangRefundServiceImpl")
    private IReportSaveService reportSaveService;


    public static void main(String[] args) {
        MabangRefundServiceImpl gyyOrderInfoService = new MabangRefundServiceImpl();
        PlatformApiEnum platformApiEnum = PlatformApiEnum.ORDER_GET_REFUND_LIST;
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(platformApiEnum.getTaskName());
        jobTaskDTO.setApiId(7);
        jobTaskDTO.setApiName("管易云查询订单列表");
        jobTaskDTO.setId(32L);
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(null);
        jobTaskDTO.setNextTime(null);
        jobTaskDTO.setPlatformId(1);
        jobTaskDTO.setState(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(platformApiEnum);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        List<RefundOrderEntity> refundOrderEntities = null;
        try {
            refundOrderEntities = gyyOrderInfoService.pullDate(requestDTO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println(refundOrderEntities);
    }

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
                                log.error("==== 马帮修改mongodb退款数据失败，[ 订单号 = " + refundOrderEntity.getPlatformOrderId() + "], 错误信息 = ", e);
                                dmpErrorLogEntity.setErrorMsg("==== 马帮修改mongodb退款数据失败，[ 订单号 = " + refundOrderEntity.getPlatformOrderId() + "], 错误信息 = " + e.getMessage());
                                dmpErrorLogEntity.setReturnMsg("");
                                dmpErrorLogEntity.setCreateTime(LocalDateTime.now());
                                dmpErrorLogService.add(dmpErrorLogEntity);
                                throw new RuntimeException("==== 马帮修改mongodb退款数据失败，[ 订单号 = " + refundOrderEntity.getPlatformOrderId() + "], 错误信息 = " + e.getMessage());
                            }
                        }
                    }
                } else {
                    mongoService.saveMongoData(refundOrderEntity, MongoTableNameContant.ORIGINAL_MABANG_REFUND);
                }
                //存储数据到中台
                reportSaveService.analysisOrder(refundOrderEntity);
            }
        }
    }

    /**
     * 请求马帮退款信息接口
     *
     * @param dto
     * @return
     */
    private List<RefundOrderEntity> pullDate(RequestDTO dto) throws Exception {
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        dto.getJobTaskDTO().setLastTime(nextTime);
        return MabangApiUtils.queryRefundList(dto.getPlatformApiEnum().getTaskName(), lastTime, nextTime);
    }

    /**
     * 解析退款订单数据
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     **/
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void analysisOrder(RefundOrderEntity refundOrderEntity) throws Exception {
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
            dmpRefundItemEntity.setAmountAfter(BigDecimal.ZERO);

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
