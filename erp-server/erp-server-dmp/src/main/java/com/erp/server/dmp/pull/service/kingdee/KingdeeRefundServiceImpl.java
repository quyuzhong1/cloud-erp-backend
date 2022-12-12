package com.erp.server.dmp.pull.service.kingdee;

import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.server.dmp.constant.MongoTableNameContant;
import com.erp.server.dmp.entity.dmp.DmpErrorLogEntity;
import com.erp.server.dmp.entity.dmp.DmpRefundInfoEntity;
import com.erp.server.dmp.entity.dto.JobTaskDTO;
import com.erp.server.dmp.entity.dto.OrderMongoDTO;
import com.erp.server.dmp.entity.dto.RequestDTO;
import com.erp.server.dmp.entity.kingdee.KingdeeRefundOrderEntity;
import com.erp.server.dmp.entity.kingdee.KingdeeReturnOrderEntity;
import com.erp.server.dmp.entity.kingdee.KingdeeReturnOrderItemEntity;
import com.erp.server.dmp.entity.mabang.RefundOrderEntity;
import com.erp.server.dmp.enums.PlatformApiEnum;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import com.erp.server.dmp.pull.service.dmp.DmpRefundInfoService;
import com.erp.server.dmp.pull.service.dmp.DmpRefundItemService;
import com.erp.server.dmp.utils.KingdeeUtils;
import com.kingdee.bos.webapi.entity.QueryParam;
import com.kingdee.bos.webapi.sdk.K3CloudApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 金蝶退货退款列表
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.AR_REFUNDBILL)
public class KingdeeRefundServiceImpl implements IReportSaveService {

    @Resource
    private MongoService mongoService;

    @Resource
    private DmpErrorLogService dmpErrorLogService;

    @Resource
    private DmpRefundInfoService dmpRefundInfoService;

    @Resource
    private DmpRefundItemService dmpRefundItemService;

    @Override
    public void pullDataSave(RequestDTO dto) throws Exception {
        List<KingdeeRefundOrderEntity> refundOrderEntityList = pullDate(dto);
        if (refundOrderEntityList != null && refundOrderEntityList.size() > 0) {
            for (KingdeeRefundOrderEntity refundOrderEntity : refundOrderEntityList) {
                OrderMongoDTO orderMongoDTO = new OrderMongoDTO();
                orderMongoDTO.setBillNo(refundOrderEntity.getFBillNo());
                List<KingdeeRefundOrderEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_KINGDEE_REFUND, KingdeeRefundOrderEntity.class);
                if (mongoData != null && mongoData.size() > 0) {
                    for (KingdeeRefundOrderEntity mongoDatum : mongoData) {
                        // 比较数据是否相同
                        if (!mongoDatum.toString().equals(refundOrderEntity.toString())) {
                            // 修改数据
                            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(refundOrderEntity), MapUtil.class);
                            try {
                                mongoService.updateMongoData(orderMongoDTO, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_REFUND, KingdeeRefundOrderEntity.class);
                            } catch (Exception e) {
                                DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity();
                                dmpErrorLogEntity.setTaskId(dto.getJobTaskDTO().getId());
                                dmpErrorLogEntity.setParams("");
                                dmpErrorLogEntity.setErrorMsg("==== 金蝶云星空修改mongodb退款数据失败，[ 单号 = " + refundOrderEntity.getFBillNo() + "], 错误信息 = " + e.getMessage());
                                dmpErrorLogEntity.setReturnMsg("");
                                dmpErrorLogService.add(dmpErrorLogEntity);
                                throw new RuntimeException("==== 金蝶云星空修改mongodb退款数据失败，[ 单号 = " + refundOrderEntity.getFBillNo() + "], 错误信息 = " + e.getMessage());
                            }
                        }
                    }
                } else {
                    mongoService.saveMongoData(refundOrderEntity, MongoTableNameContant.ORIGINAL_KINGDEE_REFUND);
                }
                //存储数据到中台
                analysisRefundOrder(refundOrderEntity);
            }
        }
    }

    /**
     * 请求金蝶云星空退款接口
     * @param dto
     * @return
     */
    public List<KingdeeRefundOrderEntity> pullDate(RequestDTO dto) {
        List<KingdeeRefundOrderEntity> infoArrayList = new ArrayList<>();
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
                cl.add(Calendar.DAY_OF_MONTH, -30);
                st = sdf.format(cl.getTime());
                sd = sdf.format(date);
                dto.getJobTaskDTO().setLastTime(Integer.parseInt(String.valueOf(System.currentTimeMillis() / 1000L)));
            }

            //读取配置，初始化SDK
            K3CloudApi client = new K3CloudApi();

            String formId = jobTask.getApiCode();
            LinkedList<String> queryfilters = new LinkedList<>();
            queryfilters.add(String.format("FModifyDate >= '%s'", st));
            queryfilters.add(String.format("FModifyDate <= '%s'", sd));
            queryfilters.add(String.format("FBillTypeID = '%s'", "ef06f87d394a462d9f96cb2397803372"));
            queryfilters.add(String.format("FDOCUMENTSTATUS = '%s'", "C"));
            String filterStr = String.join(" and ", queryfilters);
            String fieldKeys = "FID,FBillTypeID,FBillTypeID.FName,FBillNo,FDATE,FSETTLERATE,FREFUNDAMOUNTFOR_H,FDOCUMENTSTATUS,FRECTUNIT,FRECTUNIT.FName,FSETTLECUR.FCode,FREALREFUNDAMOUNTFOR,FEXCHANGERATE,FWRITTENOFFSTATUS,FCancelStatus,FREMARK,FCreateDate,FModifyDate,FApproveDate,FWBSETTLENO,FCountry,FSALEORGID.FName,FSALEORGID,FSALEERID,FSALEERID.FName";

            Boolean dataSign = true;
            //当前页数
            Integer pageIndex = 0;

            //每次最多获取100条
            Integer pageSize = 10000;
            while (dataSign) {
                //请求参数，示例使用的是SDK提供的模板类，还可以使用字符串拼接等方式
                QueryParam param = new QueryParam();
                param.setFormId(formId);
                param.setFieldKeys(fieldKeys);
                if (StringUtils.isNotBlank(st)) {
                    param.setFilterString(filterStr);
                }
                param.setLimit(pageSize);
                //"StartRow\":0,"+// 分页取数开始行索引，从0开始，例如每页10行数据，第2页开始是10，第3页开始是20

                param.setStartRow(pageIndex * pageSize);
                String s = JSONObject.toJSONString(param);

                Map<String, Object> stringObjectMap = null;
                try {
                    List<List<Object>> result = client.executeBillQuery(s);
                    if (!result.isEmpty()) {
                        if (result.size() == 1 && result.get(0).get(0).toString().contains("IsSuccess=false")) {
                            dataSign = false;
                            throw new RuntimeException(" ===== 金蝶云星空解析退款信息数据失败 ===== " + result);
                        }

                        for (List<Object> objects : result) {
                            Map<String, String> valMap = KingdeeUtils.keySetValByLinked(fieldKeys, objects);
                            KingdeeRefundOrderEntity kingdeeRefundOrderEntity = new KingdeeRefundOrderEntity();
                            kingdeeRefundOrderEntity.setFID(valMap.get("FID"));
                            kingdeeRefundOrderEntity.setFBillTypeID(valMap.get("FBillTypeID"));
                            kingdeeRefundOrderEntity.setFBillTypeName(valMap.get("FBillTypeName"));
                            kingdeeRefundOrderEntity.setFBillNo(valMap.get("FBillNo"));
                            kingdeeRefundOrderEntity.setFDATE(valMap.get("FDATE"));
                            kingdeeRefundOrderEntity.setFSETTLERATE(valMap.get("FSETTLERATE"));
                            kingdeeRefundOrderEntity.setFREFUNDAMOUNTFOR_H(valMap.get("FREFUNDAMOUNTFOR_H"));
                            kingdeeRefundOrderEntity.setFDOCUMENTSTATUS(valMap.get("FDOCUMENTSTATUS"));
                            kingdeeRefundOrderEntity.setFRECTUNIT(valMap.get("FRECTUNIT"));
                            kingdeeRefundOrderEntity.setFRECTUNITName(valMap.get("FRECTUNITName"));
                            kingdeeRefundOrderEntity.setFSETTLECURCode(valMap.get("FSETTLECURCode"));
                            kingdeeRefundOrderEntity.setFREALREFUNDAMOUNTFOR(valMap.get("FREALREFUNDAMOUNTFOR"));
                            kingdeeRefundOrderEntity.setFEXCHANGERATE(valMap.get("FEXCHANGERATE"));
                            kingdeeRefundOrderEntity.setFWRITTENOFFSTATUS(valMap.get("FWRITTENOFFSTATUS"));
                            kingdeeRefundOrderEntity.setFCancelStatus(valMap.get("FCancelStatus"));
                            kingdeeRefundOrderEntity.setFREMARK(valMap.get("FREMARK"));
                            kingdeeRefundOrderEntity.setFCreateDate(valMap.get("FCreateDate"));
                            kingdeeRefundOrderEntity.setFModifyDate(valMap.get("FModifyDate"));
                            kingdeeRefundOrderEntity.setFApproveDate(valMap.get("FApproveDate"));
                            kingdeeRefundOrderEntity.setFWBSETTLENO(valMap.get("FWBSETTLENO"));
                            kingdeeRefundOrderEntity.setFCountry(valMap.get("FCountry"));
                            kingdeeRefundOrderEntity.setFSALEORGName(valMap.get("FSALEORGName"));
                            kingdeeRefundOrderEntity.setFSALEORGID(valMap.get("FSALEORGID"));
                            kingdeeRefundOrderEntity.setFSALEERID(valMap.get("FSALEERID"));
                            kingdeeRefundOrderEntity.setFSALEERName(valMap.get("FSALEERName"));
                            infoArrayList.add(kingdeeRefundOrderEntity);
                        }
                    } else {
                        dataSign = false;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    log.info("请求接口地址异常 错误信息：" + e.getMessage());
                    DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity();
                    dmpErrorLogEntity.setTaskId(jobTask.getId());
                    dmpErrorLogEntity.setParams("");
                    dmpErrorLogEntity.setErrorMsg(e.getMessage());
                    dmpErrorLogEntity.setReturnMsg(JSONObject.toJSONString(stringObjectMap));
                    dmpErrorLogEntity.setCreateTime(new Date());
                    dmpErrorLogService.add(dmpErrorLogEntity);
                    dataSign = false;
                }
                pageIndex++;
            }
        } catch (Exception e) {
            log.info(" ===== 获取金蝶云星空退款数据失败， 错误信息 = { " + e.getMessage() + " }");
            throw new RuntimeException(" ===== 获取金蝶云星空退款数据失败， 错误信息 = { " + e.getMessage() + " }");
        }
        return infoArrayList;
    }

    /**
     * 解析退款数据
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     * @return void
     **/
    public void analysisRefundOrder(KingdeeRefundOrderEntity refundOrderEntity) throws Exception {
        DmpRefundInfoEntity dmpRefundInfoEntity = new DmpRefundInfoEntity();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        //平台订单编号
        dmpRefundInfoEntity.setPlatformOrderId("");

        //退款单号
        dmpRefundInfoEntity.setRefundId(refundOrderEntity.getFBillNo());

        //币别编号
        dmpRefundInfoEntity.setCurrencyCode(refundOrderEntity.getFSETTLECURCode());

        //退货金额
        dmpRefundInfoEntity.setRefundAmount(BigDecimal.valueOf(Double.valueOf(refundOrderEntity.getFREFUNDAMOUNTFOR_H())));

        //退款类型：1、未收到货部分退款 2、未收到货全额退款 3、已收到货部分退款 4、已收到货全额退款
        dmpRefundInfoEntity.setRefundType(0);

        //退款原因
        dmpRefundInfoEntity.setRefundReasonDesc(refundOrderEntity.getFREMARK());

        //退款备注
        dmpRefundInfoEntity.setRefundRemark(refundOrderEntity.getFREMARK());

        //退款状态：1、新建退款 2、审核中 3、财务审核 4、成功 5、失败 6、作废
        dmpRefundInfoEntity.setRefundStatus(4);

        //申请时间
        if (StringUtils.isNotBlank(refundOrderEntity.getFDATE()) && !refundOrderEntity.getFDATE().equals("null")) {
            dmpRefundInfoEntity.setRefundCreateTime(sdf.parse(refundOrderEntity.getFDATE()));
        }

        //店铺编号
        dmpRefundInfoEntity.setShopNo("B2B");

        //店铺名称
        dmpRefundInfoEntity.setShopName("B2B");

        //平台名称
        dmpRefundInfoEntity.setPlatformName("B2B");

        //退款时间
        dmpRefundInfoEntity.setRefundTime(null);

        //汇率
        dmpRefundInfoEntity.setCurrencyRate(BigDecimal.valueOf(Double.valueOf(refundOrderEntity.getFSETTLERATE())));

        //国家二字码 例如：US
        dmpRefundInfoEntity.setCountryCode("");

        //国家中文名
        dmpRefundInfoEntity.setCountryCn("");

        //国家英文名
        dmpRefundInfoEntity.setCountryEn("");

        //平台交易号
        dmpRefundInfoEntity.setSalesRecordNumber("");

        //买家用户Id
        dmpRefundInfoEntity.setBuyerUserId(refundOrderEntity.getFRECTUNIT());

        //买家用户名
        dmpRefundInfoEntity.setBuyerName(refundOrderEntity.getFRECTUNITName());

        //原始订单金额
        dmpRefundInfoEntity.setItemTotalOrigin(BigDecimal.valueOf(Double.valueOf(refundOrderEntity.getFREFUNDAMOUNTFOR_H())));

        //原始订单运费金额
        dmpRefundInfoEntity.setShippingTotalOrigin(BigDecimal.ZERO);

        //订单时间
        dmpRefundInfoEntity.setOrderTime(null);

        //发货时间
        dmpRefundInfoEntity.setExpressTime(null);

        //退货图片多个用英文 , 隔开
        dmpRefundInfoEntity.setPictureUrl("");

        //平台最后修改时间
        if (StringUtils.isNotBlank(refundOrderEntity.getFModifyDate()) && !refundOrderEntity.getFModifyDate().equals("null")) {
            dmpRefundInfoEntity.setPlatformUpdateTime(sdf.parse(refundOrderEntity.getFModifyDate()));
        }

        //包裹单号
        dmpRefundInfoEntity.setTrackNumber("");

        //平台标识
        dmpRefundInfoEntity.setPlatformSign("金蝶云星空");

        dmpRefundInfoEntity.setCreateTime(new Date());

        //新增订单信息
        String refundInfoId = dmpRefundInfoService.checkOrder(dmpRefundInfoEntity);
//        if (StringUtils.isNotBlank(refundInfoId)) {
//            //新增订单商品信息
//            analysisRefundOrderItem(refundOrderEntity.getProductList(), refundInfoId);
//        }
    }
}
