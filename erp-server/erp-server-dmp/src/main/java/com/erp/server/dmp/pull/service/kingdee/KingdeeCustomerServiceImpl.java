package com.erp.server.dmp.pull.service.kingdee;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.KingdeeSkuMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.model.dmp.entity.DmpSkuInfoEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.kingdee.KingdeeOrderEntity;
import com.erp.model.dmp.kingdee.KingdeeSkuEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import com.erp.server.dmp.pull.service.dmp.DmpSkuInfoService;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import com.erp.server.dmp.utils.KingdeeUtils;
import com.kingdee.bos.webapi.entity.QueryParam;
import com.kingdee.bos.webapi.sdk.K3CloudApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 金蝶商品
 */
@Slf4j
@Component
public class KingdeeCustomerServiceImpl implements IReportSaveService {

    @Resource
    private MongoService mongoService;

    @Resource
    private DmpErrorLogService dmpErrorLogService;

    @Resource
    private DmpSkuInfoService dmpSkuInfoService;

    public static void main(String[] args) {
        KingdeeCustomerServiceImpl kingdeeCustomerServiceImpl = new KingdeeCustomerServiceImpl();
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(PlatformApiEnum.BD_CUSTOMER.getTaskName());
        jobTaskDTO.setApiId(5);
        jobTaskDTO.setApiName("获取订单列表");
        jobTaskDTO.setId(30L);
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(null);
        jobTaskDTO.setNextTime(null);
        jobTaskDTO.setPlatformId(1);
        jobTaskDTO.setState(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(PlatformApiEnum.BD_CUSTOMER);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        List<KingdeeSkuEntity> kingdeeSkuEntities = kingdeeCustomerServiceImpl.pullDate(requestDTO);
        System.out.println(kingdeeSkuEntities);
    }

    @Override
    public void pullDataSave(RequestDTO dto) throws Exception {
        List<KingdeeSkuEntity> skuEntityList = pullDate(dto);
        if (skuEntityList != null && skuEntityList.size() > 0) {
            for (KingdeeSkuEntity skuEntity : skuEntityList) {
                KingdeeSkuMongoDTO kingdeeSkuMongoDTO = new KingdeeSkuMongoDTO();
                kingdeeSkuMongoDTO.setMaterialId(skuEntity.getFMaterialId());
                List<KingdeeSkuEntity> mongoData = mongoService.findMongoData(kingdeeSkuMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_KINGDEE_SKU, KingdeeSkuEntity.class);
                if (CollectionUtil.isNotEmpty(mongoData)) {
                    for (KingdeeSkuEntity mongoDatum : mongoData) {
                        // 比较数据是否相同
                        if (!mongoDatum.toString().equals(skuEntity.toString())) {
                            // 修改数据
                            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(skuEntity), MapUtil.class);
                            try {
                                mongoService.updateMongoData(kingdeeSkuMongoDTO, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_SKU, KingdeeSkuEntity.class);
                            } catch (Exception e) {
                                DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity();
                                dmpErrorLogEntity.setTaskId(dto.getJobTaskDTO().getId());
                                dmpErrorLogEntity.setParams("");
                                dmpErrorLogEntity.setErrorMsg("==== 金蝶云星空修改mongodb商品数据失败，[ 商品编号 = " + skuEntity.getFMaterialId() + "], 错误信息 = " + e.getMessage());
                                dmpErrorLogEntity.setReturnMsg("");
                                dmpErrorLogEntity.setCreateTime(new Date());
                                dmpErrorLogService.add(dmpErrorLogEntity);
                                throw new RuntimeException("==== 金蝶云星空修改mongodb商品数据失败，[ 商品编号 = " + skuEntity.getFMaterialId() + "], 错误信息 = " + e.getMessage());
                            }
                        }
                    }
                } else {
                    mongoService.saveMongoData(skuEntity, MongoTableNameContant.ORIGINAL_KINGDEE_SKU);
                }
                //存储数据到中台
//                analysisSku(skuEntity);
            }
        }
    }

    /**
     * 请求金蝶云星空订单接口
     *
     * @param dto
     * @return
     */
    public List<KingdeeSkuEntity> pullDate(RequestDTO dto) {
        List<KingdeeSkuEntity> infoArrayList = new ArrayList<>();
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
        //读取配置，初始化SDK
        K3CloudApi client = new K3CloudApi();

        String formId = dto.getJobTaskDTO().getApiCode();
        LinkedList<String> queryfilters = new LinkedList<>();
        queryfilters.add(String.format("FModifyDate >= '%s'", st));
        queryfilters.add(String.format("FModifyDate <= '%s'", sd));
        String filterStr = String.join(" and ", queryfilters);
        String fieldKeys = "FCUSTID,FCreateOrgId,FCreateOrgId.FNumber,FUseOrgId,FUseOrgId.FNumber,FName,FShortName,FCOUNTRY,FCOUNTRY.FNumber,FPROVINCIAL.FNumber";

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
//            if (StringUtils.isNotBlank(st)) {
//                param.setFilterString(filterStr);
//            }
            param.setLimit(pageSize);
            //"StartRow\":0,"+// 分页取数开始行索引，从0开始，例如每页10行数据，第2页开始是10，第3页开始是20

            param.setStartRow(pageIndex * pageSize);
            String s = JSONObject.toJSONString(param);

            Map<String, Object> stringObjectMap = null;
            try {
                KingdeeApiUtils kingdeeApiUtils = new KingdeeApiUtils(PlatformApiEnum.BD_CUSTOMER.taskName);
                List<Map<String, Object>> result = kingdeeApiUtils.queryList(filterStr, fieldKeys, 0, 10000);
//                List<List<Object>> result = KingdeeApiUtils.executeBillQuery(s);
                String jsonData = "{\"CreateOrgId\":1,\"Number\":\"\",\"Id\":\"\",\"IsSortBySeq\":\"false\"}";
                String view = client.view(formId, jsonData);
                if (!result.isEmpty()) {
                    if (result.size() == 1 && result.get(0).get(0).toString().contains("IsSuccess=false")) {
                        dataSign = false;
                        throw new RuntimeException(" ===== 金蝶云星空解析商品信息数据失败 ===== " + result);
                    }

//                    for (List<Object> objects : result) {
//                        Map<String, String> valMap = KingdeeUtils.keySetValByLinked(fieldKeys, objects);
//                        KingdeeSkuEntity kingdeeSkuEntity = new KingdeeSkuEntity();
//                        kingdeeSkuEntity.setFUseOrgId(valMap.get("FUseOrgId"));
//                        kingdeeSkuEntity.setFUseOrgName(valMap.get("FUseOrgId.FName"));
//                        kingdeeSkuEntity.setFNumber(valMap.get("FNumber"));
//                        kingdeeSkuEntity.setFMaterialId(valMap.get("FMaterialId"));
//                        kingdeeSkuEntity.setFName(valMap.get("FName"));
//                        kingdeeSkuEntity.setFSpecification(valMap.get("FSpecification"));
//                        kingdeeSkuEntity.setFCreateDate(valMap.get("FCreateDate"));
//                        kingdeeSkuEntity.setFModifyDate(valMap.get("FModifyDate"));
//                        kingdeeSkuEntity.setFDocumentStatus(valMap.get("FDocumentStatus"));
//                        kingdeeSkuEntity.setFForbidStatus(valMap.get("FForbidStatus"));
//                        kingdeeSkuEntity.setFRefStatus(valMap.get("FRefStatus"));
//                        kingdeeSkuEntity.setFPurPrice_CMK(valMap.get("FPurPrice_CMK"));
//                        kingdeeSkuEntity.setF_PRVD_Assistant(valMap.get("F_PRVD_Assistant.FDataValue"));
//                        kingdeeSkuEntity.setF_PRVD_Assistant1(valMap.get("F_PRVD_Assistant1.FDataValue"));
//                        kingdeeSkuEntity.setFSalePrice_CMK(valMap.get("FSalePrice_CMK"));
//                        kingdeeSkuEntity.setFSSRQ(valMap.get("F_SSRQ"));
//                        kingdeeSkuEntity.setFErpClsID(valMap.get("FErpClsID"));
//                        infoArrayList.add(kingdeeSkuEntity);
//                    }
                } else {
                    dataSign = false;
                }

            } catch (Exception e) {
                e.printStackTrace();
                log.info("请求接口地址异常 错误信息：" + e.getMessage());
                DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity();
                dmpErrorLogEntity.setTaskId(dto.getJobTaskDTO().getId());
                dmpErrorLogEntity.setParams("");
                dmpErrorLogEntity.setErrorMsg(e.getMessage());
                dmpErrorLogEntity.setReturnMsg(JSONObject.toJSONString(stringObjectMap));
                dmpErrorLogEntity.setCreateTime(new Date());
                dmpErrorLogService.add(dmpErrorLogEntity);
                dataSign = false;
            }
            pageIndex++;
        }
        return infoArrayList;
    }
}
