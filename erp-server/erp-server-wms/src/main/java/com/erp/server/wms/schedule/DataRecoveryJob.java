package com.erp.server.wms.schedule;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SyncKingdeeOperateEnum;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.business.interceptor.CommonInterceptor;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.inventory.InventoryBatchUnApproveDTO;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.service.SoOutstockService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        if (ObjectUtils.isEmpty(ids)) {
            XxlJobHelper.log("参数错误ids={}", ids);
            return;
        }
        ids.parallelStream().forEach(item -> {
            BaseIdsDTO.IdsDTO idsDTO = new BaseIdsDTO.IdsDTO();
            idsDTO.setIds(Arrays.asList(item));
            soOutstockService.disApprove(idsDTO, Boolean.FALSE);
        });

    }
}
