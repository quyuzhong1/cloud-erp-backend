package com.erp.server.wms.schedule;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.base.BaseIdsDTO;
import com.erp.server.wms.service.SoOutstockService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * 数据修复任务临时使用
 *
 * @Author Cloud
 * @Date 2023/7/13 15:06
 **/
@Component
@Slf4j
public class DataRecoveryJob {

    @Resource
    private SoOutstockService soOutstockService;

    @XxlJob("soOutStockDataRecovery")
    public void SoOutStockDataRecovery() {
        String jobParam = XxlJobHelper.getJobParam();
        if (StrUtil.isBlank(jobParam)) {
            XxlJobHelper.log("参数错误= {}",jobParam);
            return;
        }
        JSONObject param = JSONUtil.parseObj(jobParam);
        List<String> ids = param.getBeanList("ids", String.class);
        if(CollectionUtil.isEmpty(ids)){
            //ids = soOutstockService.getIdsByTemp();
        }
        if (ObjectUtils.isEmpty(ids)) {
            XxlJobHelper.log("参数错误ids={}", ids);
            return;
        }
        ids.parallelStream().forEach(item -> {
            BaseIdsDTO.IdsDTO idsDTO = new BaseIdsDTO.IdsDTO();
            idsDTO.setIds(Arrays.asList(item));
            try {
                soOutstockService.disApprove(idsDTO, Boolean.FALSE);
            } catch (Exception e) {
                XxlJobHelper.log("数据修复失败，id={} e ={}", item, e);
                log.error("数据修复失败，id={} e ={}", item, e);
            }
        });


    }
}
