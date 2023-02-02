package com.erp.server.dmp.pull.service.mabang;

import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.model.dmp.entity.DmpReturnOrderInfoEntity;
import com.erp.model.dmp.entity.DmpReturnOrderItemEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.mabang.ReturnOrderEntity;
import com.erp.model.dmp.mabang.ReturnOrderItemEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import com.erp.server.dmp.pull.service.dmp.DmpReturnOrderInfoService;
import com.erp.server.dmp.pull.service.dmp.DmpReturnOrderItemService;
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
 * 马帮退货订单
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.ORDER_GET_RETURN_ORDER_LIST)
public class MabangReturnOrderInfoServiceImpl implements IReportSaveService<ReturnOrderEntity> {
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

    @Resource
    @Qualifier("mabangReturnOrderInfoServiceImpl")
    private IReportSaveService reportSaveService;

    public static void main(String[] args) {
        MabangReturnOrderInfoServiceImpl getOrderInfoService = new MabangReturnOrderInfoServiceImpl();
        PlatformApiEnum platformApiEnum = PlatformApiEnum.ORDER_GET_RETURN_ORDER_LIST;
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(platformApiEnum.getTaskName());
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
        List<ReturnOrderEntity> orderEntities = null;
        try {
            orderEntities = getOrderInfoService.pullDate(requestDTO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
                                dmpErrorLogEntity.setCreateTime(LocalDateTime.now());
                                dmpErrorLogService.add(dmpErrorLogEntity);
                                throw new RuntimeException("==== 马帮修改mongodb退货数据失败，[ 订单号 = " + returnOrderEntity.getPlatformOrderId() + "], 错误信息 = " + e.getMessage());
                            }
                        }
                    }
                } else {
                    mongoService.saveMongoData(returnOrderEntity, MongoTableNameContant.ORIGINAL_MABANG_RETURN_ORDER);
                }
                //存储数据到中台
                reportSaveService.analysisOrder(returnOrderEntity);
            }
        }
    }

    /**
     * 请求马帮退货订单接口
     * @param dto
     * @return
     */
    private List<ReturnOrderEntity> pullDate(RequestDTO dto) throws Exception {
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        dto.getJobTaskDTO().setLastTime(nextTime);
        return MabangApiUtils.queryReturnOrderList(dto.getPlatformApiEnum().getTaskName(), lastTime, nextTime);
    }

    /**
     * 解析订单数据
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     * @return void
     **/
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void analysisOrder(ReturnOrderEntity returnOrderEntity) throws Exception {
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
        if(5 == returnOrderEntity.getStatus()){
            dmpReturnOrderInfoEntity.setIsDeleted(Boolean.TRUE);
        }

        //新增订单信息
        String orderInfoId = dmpReturnOrderInfoService.checkOrder(dmpReturnOrderInfoEntity);
        if (StringUtils.isNotBlank(orderInfoId)) {
            //新增订单商品信息
            analysisReturnOrderItem(returnOrderEntity.getItem(), orderInfoId, 5 == returnOrderEntity.getStatus());
        }
    }

    /**
     * 解析退货订单商品数据
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     * @return void
     **/
    public void analysisReturnOrderItem(List<ReturnOrderItemEntity> orderItem, String orderId, boolean isDeleted) {
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

            dmpReturnOrderItemEntity.setAmountAfter(orderItemBean.getSellPrice().multiply(new BigDecimal(orderItemBean.getQuantity())));

            orderItemList.add(dmpReturnOrderItemEntity);
        }
        checkOrderItem(orderItemList, orderId, isDeleted);
    }

    /**
     * 校验退货订单商品信息在中台是否存在，存在就修改不存在则新增
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    public void checkOrderItem(List<DmpReturnOrderItemEntity> orderItem, String returnOrderId, boolean isDeleted) {
        dmpReturnOrderItemService.deleteOrderByReturnOrderId(returnOrderId);
        if (!isDeleted){
            dmpReturnOrderItemService.batchAdd(orderItem);
        }
    }
}
