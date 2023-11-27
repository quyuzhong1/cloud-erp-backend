package com.erp.server.dmp.pull.schedule;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
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
        String flag = "now";
        List<LocalDate> localDateList = new ArrayList<>();
        if(StrUtil.isNotBlank(jobParam)){
            XxlJobHelper.log("DmpPushTaskJob jobParam:{}", jobParam);
            JSONObject jsonParam = JSONUtil.parseObj(jobParam);
            flag = jsonParam.getStr("flag", "now");
            JSONArray listDate = jsonParam.getJSONArray("listDate");
            for (Object o : listDate) {
                LocalDate ldt = LocalDate.parse(o.toString());
                localDateList.add(ldt);
            }
        }

        dmpSkuCostService.syncPurchaseOrderSkuCost(flag, localDateList);
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
        List<String> skuNoList = Arrays.stream(jobParam.split(",")).collect(Collectors.toList());
        dmpSkuCostService.cleanSkuCostBySKuNos(skuNoList);
        return ReturnT.SUCCESS;
    }

}
