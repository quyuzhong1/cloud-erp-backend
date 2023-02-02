package com.erp.server.dmp.pull.service.kingdee;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.KingdeeSkuMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.model.dmp.entity.DmpSkuInfoEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.kingdee.KingdeeSkuEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import com.erp.server.dmp.pull.service.dmp.DmpSkuInfoService;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.erp.server.dmp.pull.service.kingdee.KingdeeOrderInfoServiceImpl.ORG_CODE;

/**
 * 金蝶商品
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.BD_MATERIAL)
public class KingdeeSkuInfoServiceImpl implements IReportSaveService<KingdeeSkuEntity> {

    @Resource
    private MongoService mongoService;

    @Resource
    private DmpErrorLogService dmpErrorLogService;

    @Resource
    private DmpSkuInfoService dmpSkuInfoService;
    @Resource
    @Qualifier("kingdeeSkuInfoServiceImpl")
    private IReportSaveService reportSaveService;

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
                                dmpErrorLogEntity.setCreateTime(LocalDateTime.now());
                                dmpErrorLogService.add(dmpErrorLogEntity);
                                throw new RuntimeException("==== 金蝶云星空修改mongodb商品数据失败，[ 商品编号 = " + skuEntity.getFMaterialId() + "], 错误信息 = " + e.getMessage());
                            }
                        }
                    }
                } else {
                    mongoService.saveMongoData(skuEntity, MongoTableNameContant.ORIGINAL_KINGDEE_SKU);
                }
                //存储数据到中台
                reportSaveService.analysisOrder(skuEntity);
            }
        }
    }

    /**
     * 请求金蝶云星空订单接口
     *
     * @param dto
     * @return
     */
    public List<KingdeeSkuEntity> pullDate(RequestDTO dto) throws Exception{
        List<KingdeeSkuEntity> infoArrayList = new ArrayList<>();
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        dto.getJobTaskDTO().setLastTime(nextTime);
        LinkedList<String> queryFilters = new LinkedList<>();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        queryFilters.add(String.format("FModifyDate >= '%s'", sdf.format(lastTime.minusMinutes(2))));
        queryFilters.add(String.format("FModifyDate <= '%s'", sdf.format(nextTime)));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FUseOrgId,FUseOrgId.FName,FNumber,FMaterialId,FName,FSpecification,FCreateDate,FModifyDate," +
                "FDocumentStatus,FForbidStatus,FRefStatus,FPurPrice_CMK,F_PRVD_Assistant.FDataValue," +
                "F_PRVD_Assistant1.FDataValue,FSalePrice_CMK,F_SSRQ,FErpClsID";

        Boolean dataSign = true;
        //当前页数
        Integer pageIndex = 0;

        //每次最多获取100条
        Integer pageSize = 10000;
        while (dataSign) {
            KingdeeApiUtils kingdeeApiUtils = new KingdeeApiUtils(dto.getPlatformApiEnum().getTaskName());
            List<Map<String, Object>> result = kingdeeApiUtils.queryList(filterStr, fieldKeys, pageSize, pageIndex, 0);
            XxlJobHelper.log("获取金蝶SKU数据第[{}]页 有{}条记录", pageIndex, pageSize);
            if (result.size() < pageSize){
                dataSign = false;
            }
            if (CollectionUtil.isEmpty(result)) {
                return Collections.emptyList();
            }
            List<KingdeeSkuEntity> entityList = result.stream().map(shopEntity ->
                    BeanUtil.toBean(shopEntity, KingdeeSkuEntity.class)).collect(Collectors.toList());
            infoArrayList.addAll(entityList);
            pageIndex ++;
        }
        return infoArrayList;
    }

    /**
     * 解析商品数据
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     **/
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void analysisOrder(KingdeeSkuEntity skuInfoEntity) throws Exception {
        if (StrUtil.isEmpty(skuInfoEntity.getFUseOrgId()) || !ORG_CODE.equals(skuInfoEntity.getFUseOrgId())){
            return;
        }
        DmpSkuInfoEntity dmpSkuInfoEntity = new DmpSkuInfoEntity();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());


        dmpSkuInfoEntity.setItemCode(skuInfoEntity.getFMaterialId());

        //sku编号
        dmpSkuInfoEntity.setSkuNo(skuInfoEntity.getFNumber());

        //中文名
        dmpSkuInfoEntity.setNameCn(skuInfoEntity.getFName());

        //英文名
        dmpSkuInfoEntity.setNameEn("");

        //统一成本价
        dmpSkuInfoEntity.setDefaultCost(new BigDecimal(skuInfoEntity.getFPurPrice_CMK()));

        Integer status = 3;
        if (skuInfoEntity.getFForbidStatus().equals("C")) {
            status = 5;
        }
        //商品状态:1.自动创建;2.待开发;3.正常;4.清仓;5.停止销售
        dmpSkuInfoEntity.setStatus(status);

        //商品创建时间
        if (StringUtils.isNotBlank(skuInfoEntity.getFCreateDate()) && !"null".equals(skuInfoEntity.getFCreateDate())) {
            dmpSkuInfoEntity.setSkuCreateTime(LocalDateTime.parse(skuInfoEntity.getFCreateDate(), sdf));
        }

        //商品修改时间
        if (StringUtils.isNotBlank(skuInfoEntity.getFModifyDate()) && !"null".equals(skuInfoEntity.getFModifyDate())) {
            dmpSkuInfoEntity.setSkuUpdateTime(LocalDateTime.parse(skuInfoEntity.getFModifyDate(), sdf));
        }

        //品牌
        dmpSkuInfoEntity.setBrandName("");

        //商品目录(一级)
        if (StringUtils.isNotBlank(skuInfoEntity.getF_PRVD_Assistant()) && !"null".equals(skuInfoEntity.getF_PRVD_Assistant())) {
            dmpSkuInfoEntity.setParentCategoryName(skuInfoEntity.getF_PRVD_Assistant());
        } else {
            dmpSkuInfoEntity.setParentCategoryName("");
        }

        //商品目录(二级)
        if (StringUtils.isNotBlank(skuInfoEntity.getF_PRVD_Assistant1()) && !"null".equals(skuInfoEntity.getF_PRVD_Assistant1())) {
            dmpSkuInfoEntity.setCategoryName(skuInfoEntity.getF_PRVD_Assistant1());
        } else {
            dmpSkuInfoEntity.setCategoryName("");
        }

        //售价
        dmpSkuInfoEntity.setSalePrice(new BigDecimal(skuInfoEntity.getFSalePrice_CMK()));

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

        //上市时间
        if (StringUtils.isNotBlank(skuInfoEntity.getFSSRQ()) && !skuInfoEntity.getFSSRQ().equals("null")) {
            dmpSkuInfoEntity.setListingTime(LocalDateTime.parse(skuInfoEntity.getFSSRQ(),sdf));
        }
        String itemProperty = "";
        switch (skuInfoEntity.getFErpClsID()) {
            case "1" :
                itemProperty = "外购";
                break;
            case "2" :
                itemProperty = "自制";
                break;
            case "3" :
                itemProperty = "委外";
                break;
            case "6" :
                itemProperty = "服务";
                break;
            default:
                itemProperty = "";
                break;
        }
        //物料属性
        dmpSkuInfoEntity.setItemProperty(itemProperty);

        dmpSkuInfoEntity.setCreateTime(LocalDateTime.now());

        dmpSkuInfoService.checkOrder(dmpSkuInfoEntity);
    }
}
