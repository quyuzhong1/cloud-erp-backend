package com.erp.server.dmp.pull.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.erp.model.dmp.dto.DictBasicDTO;
import com.erp.model.dmp.dto.DmpSkuCostDTO;
import com.erp.model.dmp.entity.DictBasicEntity;
import com.erp.model.scm.dto.SkuCostDTO;
import com.erp.server.dmp.service.DictBasicService;
import com.erp.server.dmp.service.DmpSkuCostService;
import com.google.gson.Gson;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 清洗数据job
 * @Author Luo_WG
 * @Date 2023/9/13 18:17
 **/
@Component
@Slf4j
public class CleanJob {
    @Resource
    private DmpSkuCostService dmpSkuCostService;
    @Resource
    private DictBasicService dictBasicService;
    /**
     * 成本数据清洗
     * @Author Luo_WG
     * @Date 2023/9/13 18:18
     * @return com.xxl.job.core.biz.model.ReturnT
     **/
    @XxlJob("skuCostClean")
    public ReturnT skuCostClean(){
        String jobParam = XxlJobHelper.getJobParam();
        //默认now表示用当前时间，不是now就用第二个参数的日期
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now();
        List<String> purchaseOrderIds = new ArrayList<>();
        List<String> supplierIds = new ArrayList<>();
        if(StrUtil.isNotBlank(jobParam)){
            XxlJobHelper.log("DmpPushTaskJob jobParam:{}", jobParam);
            JSONObject jsonObject = new JSONObject(jobParam);
            String startDate1 = jsonObject.getStr("startDate");
            if (CharSequenceUtil.isNotBlank(startDate1)){
                startDate = LocalDate.parse(startDate1);
            }
            String endDate1 = jsonObject.getStr("endDate");
            if (CharSequenceUtil.isNotBlank(endDate1)){
                endDate = LocalDate.parse(endDate1);
            }
            String purchaseOrderIds1 = jsonObject.getStr("purchaseOrderIds");
            if (CharSequenceUtil.isNotBlank(purchaseOrderIds1)){
                purchaseOrderIds = Arrays.asList(purchaseOrderIds1.split(","));
            }
            String supplierIds1 = jsonObject.getStr("supplierIds");
            if (CharSequenceUtil.isNotBlank(supplierIds1)){
                supplierIds = Arrays.asList(supplierIds1.split(","));
            }
        }else {
            List<DictBasicEntity> skuCostPurchaseOrderIds = dictBasicService.getByKey("skuCostPurchaseOrderIds");
            if (CollUtil.isNotEmpty(skuCostPurchaseOrderIds)){
                purchaseOrderIds = skuCostPurchaseOrderIds.stream().map(DictBasicEntity::getValue).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            }
            List<DictBasicEntity> skuCostSupplierIds = dictBasicService.getByKey("skuCostSupplierIds");
            if (CollUtil.isNotEmpty(skuCostSupplierIds)){
                supplierIds = skuCostSupplierIds.stream().map(DictBasicEntity::getValue).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            }
        }
        List<LocalDate> localDateList = Arrays.asList(startDate, endDate);
        SkuCostDTO.QueryPurchaseDTO queryPurchaseDTO = SkuCostDTO.QueryPurchaseDTO.builder()
                .localDateList(localDateList).purchaseOrderIds(purchaseOrderIds).supplierIds(supplierIds).build();
        dmpSkuCostService.syncPurchaseOrderSkuCost(queryPurchaseDTO);
        return ReturnT.SUCCESS;
    }

    /**
     * 根据sku编码清洗成本
     * @author Will
     * @date: 2023/11/23 14:48
     * @return ReturnT
     */
    @XxlJob("cleanSkuCostBySKuNos")
    public ReturnT cleanSkuCostBySKuNos(){
        String jobParam = XxlJobHelper.getJobParam();

        if(StrUtil.isBlank(jobParam)){
            XxlJobHelper.log("未找到录入参数，cleanSkuCostBySKuNos jobParam:{}",jobParam);
            return ReturnT.FAIL;
        }
        XxlJobHelper.log("cleanSkuCostBySKuNos jobParam:{}", jobParam);
        List<String> skuNoList = new ArrayList<>();
        List<String> purchaseOrderIds = new ArrayList<>();
        List<String> supplierIds = new ArrayList<>();
        if(StrUtil.isNotBlank(jobParam)){
            XxlJobHelper.log("DmpPushTaskJob jobParam:{}", jobParam);
            JSONObject jsonObject = new JSONObject(jobParam);
            String skuNoList1 = jsonObject.getStr("skuNoList");
            if (CharSequenceUtil.isNotBlank(skuNoList1)){
                skuNoList = Arrays.stream(skuNoList1.split(",")).collect(Collectors.toList());
            }

            String purchaseOrderIds1 = jsonObject.getStr("purchaseOrderIds");
            if (CharSequenceUtil.isNotBlank(purchaseOrderIds1)){
                purchaseOrderIds = Arrays.asList(purchaseOrderIds1.split(","));
            }
            String supplierIds1 = jsonObject.getStr("supplierIds");
            if (CharSequenceUtil.isNotBlank(supplierIds1)){
                supplierIds = Arrays.asList(supplierIds1.split(","));
            }
        }else {
            List<DictBasicEntity> skuCostPurchaseOrderIds = dictBasicService.getByKey("skuCostPurchaseOrderIds");
            if (CollUtil.isNotEmpty(skuCostPurchaseOrderIds)){
                purchaseOrderIds = skuCostPurchaseOrderIds.stream().map(DictBasicEntity::getValue).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            }
            List<DictBasicEntity> skuCostSupplierIds = dictBasicService.getByKey("skuCostSupplierIds");
            if (CollUtil.isNotEmpty(skuCostSupplierIds)){
                supplierIds = skuCostSupplierIds.stream().map(DictBasicEntity::getValue).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            }
        }
        dmpSkuCostService.cleanSkuCostBySKuNos(skuNoList,purchaseOrderIds,supplierIds);
        return ReturnT.SUCCESS;
    }

}
