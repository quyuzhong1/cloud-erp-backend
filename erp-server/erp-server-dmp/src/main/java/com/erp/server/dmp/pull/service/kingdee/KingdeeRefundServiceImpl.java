//package com.erp.server.dmp.pull.service.kingdee;
//
//import cn.hutool.core.bean.BeanUtil;
//import cn.hutool.core.collection.CollectionUtil;
//import cn.hutool.core.util.ObjectUtil;
//import cn.hutool.core.util.StrUtil;
//import com.alibaba.fastjson.JSONObject;
//import com.erp.common.business.constant.RocketMqTopic;
//import com.common.core.utils.MapUtil;
//import com.common.core.utils.date.EnumTimePattern;
//import com.erp.model.dmp.constant.MongoTableNameContant;
//import com.erp.model.dmp.enums.RocketMqTagEnum;
//import com.erp.model.dmp.dto.OrderMongoDTO;
//import com.erp.model.dmp.dto.RequestDTO;
//import com.erp.model.dmp.entity.DmpErrorLogEntity;
//import com.erp.model.dmp.entity.DmpRefundInfoEntity;
//import com.erp.model.dmp.enums.ApiKingdeeOrganizationEnum;
//import com.erp.model.dmp.enums.PlatformApiEnum;
//import com.erp.model.dmp.enums.PlatformEnum;
//import com.erp.model.dmp.kingdee.KingdeeOrderEntity;
//import com.erp.model.dmp.kingdee.KingdeeRefundOrderEntity;
//import com.erp.model.dmp.kingdee.KingdeeShopEntity;
//import com.erp.server.dmp.pull.mongo.MongoService;
//import com.erp.server.dmp.pull.service.IReportSaveService;
//import com.erp.server.dmp.pull.service.SaveData;
//import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
//import com.erp.server.dmp.pull.service.dmp.DmpRefundInfoService;
//import com.erp.server.dmp.service.mq.MQProducerService;
//import com.erp.server.dmp.utils.KingdeeApiUtils;
//import com.erp.server.dmp.utils.KingdeeUtils;
//import com.kingdee.bos.webapi.entity.QueryParam;
//import com.kingdee.bos.webapi.sdk.K3CloudApi;
//import com.xxl.job.core.context.XxlJobHelper;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import javax.annotation.Resource;
//import java.math.BigDecimal;
//import java.text.SimpleDateFormat;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.stream.Collectors;
//
///**
// * 金蝶退款列表
// */
//@Slf4j
//@Component
//@SaveData(method = PlatformApiEnum.AR_REFUNDBILL)
//public class KingdeeRefundServiceImpl implements IReportSaveService<KingdeeRefundOrderEntity> {
//
//    @Resource
//    private MongoService mongoService;
//
//    @Resource
//    private DmpRefundInfoService dmpRefundInfoService;
//
//    @Autowired
//    private MQProducerService<DmpRefundInfoEntity> mqProducerService;
//
//    @Resource
//    @Qualifier("kingdeeRefundServiceImpl")
//    private IReportSaveService reportSaveService;
//
//    @Override
//    public void pullDataSave(RequestDTO dto) throws Exception {
//        List<KingdeeRefundOrderEntity> entityList = pullDate(dto);
//        if (CollectionUtil.isEmpty(entityList)) {
//            log.info("拉取金蝶退款订单列表数据为空 entityList.size = 0 ");
//            return;
//        }
//        log.info("拉取金蝶退款订单列表数据 entityList.size = {} ", entityList.size());
//        List<KingdeeRefundOrderEntity> insertList = new ArrayList<>();
//        List<KingdeeRefundOrderEntity> pushToMqList = new ArrayList<>();
//        for (KingdeeRefundOrderEntity entity : entityList) {
//            OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByFIdAndBillNo(entity.getFBillNo(), entity.getFId());
//            List<KingdeeRefundOrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_KINGDEE_REFUND, KingdeeRefundOrderEntity.class);
//            if(CollectionUtil.isEmpty(mongoData)){
//                insertList.add(entity);
//                pushToMqList.add(entity);
//                continue;
//            }
//            KingdeeRefundOrderEntity mongoDatum = mongoData.get(0);
//            // 比较数据是否相同
//            if (mongoDatum.toString().equals(entity.toString())) {
//                continue;
//            }
//            pushToMqList.add(entity);
//            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
//            OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.get_id());
//            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_REFUND, KingdeeRefundOrderEntity.class);
//        }
//        if(CollectionUtil.isNotEmpty(insertList)){
//            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_KINGDEE_REFUND);
//        }
//        // 构造订单结构
//        List<DmpRefundInfoEntity> mabangToMqlist = pushToMqList.parallelStream()
//                .map(this::initOrderInfoEntity)
//                .filter(ObjectUtil::isNotEmpty)
//                .collect(Collectors.toList());
//
//        // 异步推送到MQ
//        mabangToMqlist.stream().peek(msg ->
//                        mqProducerService.asyncClassMsg(RocketMqTopic.DMP_TOPIC, RocketMqTagEnum.KINGDEE_REFUND_ORDER_TAG.getName(),
//                        msg, StrUtil.format("{}_{}",msg.getPlatformOrderId(), msg.getSalesRecordNumber())))
//                .collect(Collectors.toList());
//
//    }
//
//    /**
//     * 请求金蝶云星空退款接口
//     *
//     * @param dto
//     * @return
//     */
//    public List<KingdeeRefundOrderEntity> pullDate(RequestDTO dto) {
//        List<KingdeeRefundOrderEntity> infoArrayList = new ArrayList<>();
//        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
//        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
//        dto.getJobTaskDTO().setLastTime(nextTime);
//        LinkedList<String> queryFilters = new LinkedList<>();
//        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
//        queryFilters.add(String.format("FModifyDate >= '%s'", sdf.format(lastTime.minusMinutes(2))));
//        queryFilters.add(String.format("FModifyDate <= '%s'", sdf.format(nextTime)));
//        queryFilters.add(String.format("FBillTypeID = '%s'", "ef06f87d394a462d9f96cb2397803372"));
//        queryFilters.add(String.format("FDOCUMENTSTATUS = '%s'", "C"));
//        String filterStr = String.join(" and ", queryFilters);
//        String fieldKeys = "FID,FBillTypeID,FBillTypeID.FName,FBillTypeID.FNumber,FBillNo,FDATE,FSETTLERATE,FREFUNDAMOUNTFOR_H," +
//                "FDOCUMENTSTATUS,FRECTUNIT,FRECTUNIT.FName,FSETTLECUR.FCode,FREALREFUNDAMOUNTFOR,FEXCHANGERATE,FWRITTENOFFSTATUS," +
//                "FCancelStatus,FREMARK,FCreateDate,FModifyDate,FApproveDate,FWBSETTLENO,FCountry,FSALEORGID.FName,FSALEORGID,FSALEERID,FSALEERID.FName";
//
//        Boolean dataSign = true;
//        //当前页数
//        Integer pageIndex = 0;
//
//        //每次最多获取100条
//        Integer pageSize = 10000;
//        while (dataSign) {
//            //"StartRow\":0,"+// 分页取数开始行索引，从0开始，例如每页10行数据，第2页开始是10，第3页开始是20
//            KingdeeApiUtils kingdeeApiUtils = new KingdeeApiUtils(dto.getPlatformApiEnum().getTaskName());
//            List<Map<String, Object>> result = kingdeeApiUtils.queryList(filterStr, fieldKeys, pageSize, pageIndex, 0);
//            XxlJobHelper.log("获取金蝶店铺数据第[{}]页 有{}条记录", pageIndex, pageSize);
//            if (result.size() < pageSize){
//                dataSign = false;
//            }
//            if (CollectionUtil.isEmpty(result)) {
//                return Collections.emptyList();
//            }
//            List<KingdeeRefundOrderEntity> entityList = result.stream().map(entity ->
//                    BeanUtil.toBean(entity, KingdeeRefundOrderEntity.class)).collect(Collectors.toList());
//            pageIndex ++;
//            infoArrayList.addAll(entityList);
//        }
//        return infoArrayList;
//    }
//    /**
//     * 可用销售订单CODE
//     */
//    private static final List<String> ORDER_TYPES = new ArrayList<>(Arrays.asList("B2BXSDD","XSDD01_SYS"));
//    /**
//     * 解析退款数据
//     **/
//    private DmpRefundInfoEntity initOrderInfoEntity(KingdeeRefundOrderEntity refundOrderEntity) {
//        // 跳过非唯迹订单
//        if (StrUtil.isEmpty(refundOrderEntity.getFSaleOrgId()) || !ApiKingdeeOrganizationEnum.ORGANIZATION_WEIJI.getCode().equals(refundOrderEntity.getFSaleOrgId())){
//            return null;
//        }
//        DmpRefundInfoEntity dmpRefundInfoEntity = new DmpRefundInfoEntity();
//        //平台订单编号
//        dmpRefundInfoEntity.setPlatformOrderId(refundOrderEntity.getFBillNo());
//        //退款单号
//        dmpRefundInfoEntity.setRefundId(refundOrderEntity.getFBillNo());
//        //币别编号
//        dmpRefundInfoEntity.setCurrencyCode(refundOrderEntity.getFSettleCurCode());
//        //退货金额
//        dmpRefundInfoEntity.setRefundAmount(new BigDecimal(refundOrderEntity.getFRefundAmountForH()));
//        //退款类型：1、未收到货部分退款 2、未收到货全额退款 3、已收到货部分退款 4、已收到货全额退款
//        dmpRefundInfoEntity.setRefundType(0);
//        //退款原因
//        dmpRefundInfoEntity.setRefundReasonDesc(refundOrderEntity.getFRemark());
//        //退款备注
//        dmpRefundInfoEntity.setRefundRemark(refundOrderEntity.getFRemark());
//        //退款状态：1、新建退款 2、审核中 3、财务审核 4、成功 5、失败 6、作废
//        dmpRefundInfoEntity.setRefundStatus(4);
//        //申请时间
//        dmpRefundInfoEntity.setRefundCreateTime(refundOrderEntity.getFDate());
//        //店铺编号
//        dmpRefundInfoEntity.setShopNo("B2B");
//        //店铺名称
//        dmpRefundInfoEntity.setShopName("B2B");
//        //平台名称
//        dmpRefundInfoEntity.setPlatformName("B2B");
//        //退款时间
//        dmpRefundInfoEntity.setRefundTime(null);
//        //汇率
//        dmpRefundInfoEntity.setCurrencyRate(new BigDecimal(refundOrderEntity.getFSettleRate()));
//        //国家二字码 例如：US
//        dmpRefundInfoEntity.setCountryCode("");
//        //国家中文名
//        dmpRefundInfoEntity.setCountryCn("");
//        //国家英文名
//        dmpRefundInfoEntity.setCountryEn("");
//        //平台交易号
//        dmpRefundInfoEntity.setSalesRecordNumber(refundOrderEntity.getFBillNo());
//        //买家用户Id
//        dmpRefundInfoEntity.setBuyerUserId(refundOrderEntity.getFRectUnit());
//        //买家用户名
//        dmpRefundInfoEntity.setBuyerName(refundOrderEntity.getFRectUnitName());
//        //原始订单金额
//        dmpRefundInfoEntity.setItemTotalOrigin(new BigDecimal(refundOrderEntity.getFRefundAmountForH()));
//        //原始订单运费金额
//        dmpRefundInfoEntity.setShippingTotalOrigin(BigDecimal.ZERO);
//        //订单时间
//        dmpRefundInfoEntity.setOrderTime(null);
//        //发货时间
//        dmpRefundInfoEntity.setExpressTime(null);
//        //退货图片多个用英文 , 隔开
//        dmpRefundInfoEntity.setPictureUrl("");
//        //平台最后修改时间
//        dmpRefundInfoEntity.setPlatformUpdateTime(refundOrderEntity.getFModifyDate());
//        dmpRefundInfoEntity.setRefundCreateTime(refundOrderEntity.getFApproveDate());
//        //包裹单号
//        dmpRefundInfoEntity.setTrackNumber("");
//        //平台标识
//        dmpRefundInfoEntity.setPlatformSign(PlatformEnum.KINGDEE.getDesc());
//        dmpRefundInfoEntity.setCreateTime(LocalDateTime.now());
//        return dmpRefundInfoEntity;
//    }
//    /**
//     *         //新增订单信息
//     *         String refundInfoId = dmpRefundInfoService.checkOrder(dmpRefundInfoEntity);
//     */
//}
