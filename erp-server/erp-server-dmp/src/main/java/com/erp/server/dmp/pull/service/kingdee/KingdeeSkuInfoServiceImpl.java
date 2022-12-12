package com.erp.server.dmp.pull.service.kingdee;

import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.server.dmp.constant.MongoTableNameContant;
import com.erp.server.dmp.entity.dmp.DmpErrorLogEntity;
import com.erp.server.dmp.entity.dmp.DmpSkuInfoEntity;
import com.erp.server.dmp.entity.dto.JobTaskDTO;
import com.erp.server.dmp.entity.dto.KingdeeSkuMongoDTO;
import com.erp.server.dmp.entity.dto.OrderMongoDTO;
import com.erp.server.dmp.entity.dto.RequestDTO;
import com.erp.server.dmp.entity.kingdee.KingdeeOrderEntity;
import com.erp.server.dmp.entity.kingdee.KingdeeOrderItemEntity;
import com.erp.server.dmp.entity.kingdee.KingdeeSkuEntity;
import com.erp.server.dmp.entity.mabang.SkuInfoEntity;
import com.erp.server.dmp.enums.PlatformApiEnum;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import com.erp.server.dmp.pull.service.dmp.DmpOrderInfoService;
import com.erp.server.dmp.pull.service.dmp.DmpOrderItemService;
import com.erp.server.dmp.pull.service.dmp.DmpSkuInfoService;
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
 * 金蝶商品
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.BD_MATERIAL)
public class KingdeeSkuInfoServiceImpl implements IReportSaveService {

    @Resource
    private MongoService mongoService;

    @Resource
    private DmpErrorLogService dmpErrorLogService;

    @Resource
    private DmpSkuInfoService dmpSkuInfoService;

    @Override
    public void pullDataSave(RequestDTO dto) throws Exception {
        List<KingdeeSkuEntity> skuEntityList = pullDate(dto);
        if (skuEntityList != null && skuEntityList.size() > 0) {
            for (KingdeeSkuEntity skuEntity : skuEntityList) {
                KingdeeSkuMongoDTO kingdeeSkuMongoDTO = new KingdeeSkuMongoDTO();
                kingdeeSkuMongoDTO.setMaterialId(skuEntity.getFMaterialId());
                List<KingdeeOrderEntity> mongoData = mongoService.findMongoData(kingdeeSkuMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_KINGDEE_SKU, KingdeeOrderEntity.class);
                if (mongoData != null && mongoData.size() > 0) {
                    for (KingdeeOrderEntity mongoDatum : mongoData) {
                        // 比较数据是否相同
                        if (!mongoDatum.toString().equals(skuEntity.toString())) {
                            // 修改数据
                            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(skuEntity), MapUtil.class);
                            try {
                                mongoService.updateMongoData(kingdeeSkuMongoDTO, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_SKU, KingdeeOrderEntity.class);
                            } catch (Exception e) {
                                DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity();
                                dmpErrorLogEntity.setTaskId(dto.getJobTaskDTO().getId());
                                dmpErrorLogEntity.setParams("");
                                dmpErrorLogEntity.setErrorMsg("==== 金蝶云星空修改mongodb商品数据失败，[ 商品编号 = " + skuEntity.getFMaterialId() + "], 错误信息 = " + e.getMessage());
                                dmpErrorLogEntity.setReturnMsg("");
                                dmpErrorLogService.add(dmpErrorLogEntity);
                                throw new RuntimeException("==== 金蝶云星空修改mongodb商品数据失败，[ 商品编号 = " + skuEntity.getFMaterialId() + "], 错误信息 = " + e.getMessage());
                            }
                        }
                    }
                } else {
                    mongoService.saveMongoData(skuEntity, MongoTableNameContant.ORIGINAL_KINGDEE_SKU);
                }
                //存储数据到中台
                analysisSku(skuEntity);
            }
        }
    }

    /**
     * 请求金蝶云星空订单接口
     * @param dto
     * @return
     */
    public List<KingdeeSkuEntity> pullDate(RequestDTO dto) {
        List<KingdeeSkuEntity> infoArrayList = new ArrayList<>();
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

                st = "";
                sd = "";
                dto.getJobTaskDTO().setLastTime(Integer.parseInt(String.valueOf(System.currentTimeMillis() / 1000L)));
            }

            //读取配置，初始化SDK
            K3CloudApi client = new K3CloudApi();

            String formId = jobTask.getApiCode();
            LinkedList<String> queryfilters = new LinkedList<>();
            queryfilters.add(String.format("FModifyDate >= '%s'", st));
            queryfilters.add(String.format("FModifyDate <= '%s'", sd));
            String filterStr = String.join(" and ", queryfilters);
            String fieldKeys = "FUseOrgId,FUseOrgId.FName,FNumber,FMaterialId,FName,FSpecification,FCreateDate,FModifyDate,FDocumentStatus,FForbidStatus,FRefStatus,FPurPrice_CMK,F_PRVD_Assistant.FDataValue,F_PRVD_Assistant1.FDataValue,FSalePrice_CMK";

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
                            throw new RuntimeException(" ===== 金蝶云星空解析商品信息数据失败 ===== " + result);
                        }

                        for (List<Object> objects : result) {
                            Map<String, String> valMap = KingdeeUtils.keySetValByLinked(fieldKeys, objects);
                            KingdeeSkuEntity kingdeeSkuEntity = new KingdeeSkuEntity();
                            kingdeeSkuEntity.setFUseOrgId(valMap.get("FUseOrgId"));
                            kingdeeSkuEntity.setFUseOrgName(valMap.get("FUseOrgId.FName"));
                            kingdeeSkuEntity.setFNumber(valMap.get("FNumber"));
                            kingdeeSkuEntity.setFMaterialId(valMap.get("FMaterialId"));
                            kingdeeSkuEntity.setFName(valMap.get("FName"));
                            kingdeeSkuEntity.setFSpecification(valMap.get("FSpecification"));
                            kingdeeSkuEntity.setFCreateDate(valMap.get("FCreateDate"));
                            kingdeeSkuEntity.setFModifyDate(valMap.get("FModifyDate"));
                            kingdeeSkuEntity.setFDocumentStatus(valMap.get("FDocumentStatus"));
                            kingdeeSkuEntity.setFForbidStatus(valMap.get("FForbidStatus"));
                            kingdeeSkuEntity.setFRefStatus(valMap.get("FRefStatus"));
                            kingdeeSkuEntity.setFPurPrice_CMK(valMap.get("FPurPrice_CMK"));
                            kingdeeSkuEntity.setF_PRVD_Assistant(valMap.get("F_PRVD_Assistant.FDataValue"));
                            kingdeeSkuEntity.setF_PRVD_Assistant1(valMap.get("F_PRVD_Assistant1.FDataValue"));
                            kingdeeSkuEntity.setFSalePrice_CMK(valMap.get("FSalePrice_CMK"));
                            infoArrayList.add(kingdeeSkuEntity);
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
            log.info(" ===== 获取金蝶云星空商品信息数据失败， 错误信息 = { " + e.getMessage() + " }");
            throw new RuntimeException(" ===== 获取金蝶云星空商品信息数据失败， 错误信息 = { " + e.getMessage() + " }");
        }
        return infoArrayList;
    }

    /**
     * 解析商品数据
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     * @return void
     **/
    public void analysisSku(KingdeeSkuEntity skuInfoEntity) throws Exception {
        DmpSkuInfoEntity dmpSkuInfoEntity = new DmpSkuInfoEntity();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        dmpSkuInfoEntity.setItemCode(skuInfoEntity.getFMaterialId());

        //sku编号
        dmpSkuInfoEntity.setSkuNo(skuInfoEntity.getFNumber());

        //中文名
        dmpSkuInfoEntity.setNameCn(skuInfoEntity.getFName());

        //英文名
        dmpSkuInfoEntity.setNameEn("");

        //统一成本价
        dmpSkuInfoEntity.setDefaultCost(BigDecimal.valueOf(Double.valueOf(skuInfoEntity.getFPurPrice_CMK())));

        Integer status = 3;
        if (skuInfoEntity.getFForbidStatus().equals("C")) {
            status = 5;
        }
        //商品状态:1.自动创建;2.待开发;3.正常;4.清仓;5.停止销售
        dmpSkuInfoEntity.setStatus(status);

        //商品创建时间
        if (StringUtils.isNotBlank(skuInfoEntity.getFCreateDate()) && !skuInfoEntity.getFCreateDate().equals("null")) {
            dmpSkuInfoEntity.setSkuCreateTime(sdf.parse(skuInfoEntity.getFCreateDate()));
        }

        //商品修改时间
        if (StringUtils.isNotBlank(skuInfoEntity.getFModifyDate()) && !skuInfoEntity.getFModifyDate().equals("null")) {
            dmpSkuInfoEntity.setSkuUpdateTime(sdf.parse(skuInfoEntity.getFModifyDate()));
        }

        //品牌
        dmpSkuInfoEntity.setBrandName("");

        //商品目录(一级)
        if (StringUtils.isNotBlank(skuInfoEntity.getF_PRVD_Assistant()) && !skuInfoEntity.getF_PRVD_Assistant().equals("null")) {
            dmpSkuInfoEntity.setParentCategoryName(skuInfoEntity.getF_PRVD_Assistant());
        } else {
            dmpSkuInfoEntity.setParentCategoryName("");
        }

        //商品目录(二级)
        if (StringUtils.isNotBlank(skuInfoEntity.getF_PRVD_Assistant1()) && !skuInfoEntity.getF_PRVD_Assistant1().equals("null")) {
            dmpSkuInfoEntity.setCategoryName(skuInfoEntity.getF_PRVD_Assistant1());
        } else {
            dmpSkuInfoEntity.setCategoryName("");
        }

        //售价
        dmpSkuInfoEntity.setSalePrice(BigDecimal.valueOf(Double.valueOf(skuInfoEntity.getFSalePrice_CMK())));

        //申报价格
        dmpSkuInfoEntity.setDeclarePrice(BigDecimal.ZERO);

        //开发员id
        dmpSkuInfoEntity.setDeveloperId("");

        //开发员名称
        dmpSkuInfoEntity.setDeveloperName("");

        //平台标识
        dmpSkuInfoEntity.setPlatformSign("金蝶云星空");

        //企业id
        dmpSkuInfoEntity.setCompanyId(skuInfoEntity.getFUseOrgId());

        //企业名称
        dmpSkuInfoEntity.setCompanyName(skuInfoEntity.getFUseOrgName());

        dmpSkuInfoEntity.setCreateTime(new Date());

        dmpSkuInfoService.checkOrder(dmpSkuInfoEntity);
    }
}
