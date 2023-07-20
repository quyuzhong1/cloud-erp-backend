package com.erp.server.wms.schedule;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.service.SoOutstockService;
import com.erp.server.wms.service.SoReturnInstockService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Resource
    private SoReturnInstockService soReturnInstockService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @XxlJob("soOutStockDataRecovery")
    public void SoOutStockDataRecovery() {
        String jobParam = XxlJobHelper.getJobParam();
        if (StrUtil.isBlank(jobParam)) {
            XxlJobHelper.log("参数错误= {}",jobParam);
            return;
        }
        JSONObject param = JSONUtil.parseObj(jobParam);
        List<String> ids = param.getBeanList("ids", String.class);
        String type = param.get("type", String.class);
        if(CollectionUtil.isEmpty(ids)){
            ids = soOutstockService.getIdsByTemp();
        }
        if (ObjectUtils.isEmpty(ids)) {
            XxlJobHelper.log("参数错误ids={}", ids);
            return;
        }

        ids.parallelStream().forEach(item -> {
            BaseIdsDTO.IdsDTO idsDTO = new BaseIdsDTO.IdsDTO();
            idsDTO.setIds(Arrays.asList(item));
            try {
                if(StrUtil.isNotBlank(type) && "soReturnInstockService".equals(type)){
                    soReturnInstockService.disApprove(idsDTO.getIds(), Boolean.FALSE);
                }else {
                    soOutstockService.disApprove(idsDTO, Boolean.FALSE);
                }
            } catch (Exception e) {
                XxlJobHelper.log("数据修复失败，id={} e ={}", item, e);
                log.error("数据修复失败，id={} e ={}", item, e);
            }
        });


    }

    private List<String> getInnerSoOutStockIds() {
        List<String> result= new ArrayList<>();

        Map<String,Object> condition=new HashMap<>();
        condition.put("return_msg","同步成功");
        condition.put("lastSql","cast(mq_data AS json)->>'fIsGenForIos'='true'");
        List<String> kingdeeCodeList=new ArrayList<>();
        kingdeeCodeList = dmpTaskFeign.getKingdeeSourceCode(condition);

        if(CollectionUtil.isNotEmpty(kingdeeCodeList)){
            kingdeeCodeList.stream().forEach(kingdeeCode->{
                LambdaQueryWrapper<SoOutstockEntity> queryWrapper=new LambdaQueryWrapper<>();
                queryWrapper.select(SoOutstockEntity::getId);
                queryWrapper.eq(SoOutstockEntity::getApproveStatus,"approve");
//                queryWrapper.eq(SoOutstockEntity::getIsDeleted,"f");
                queryWrapper.eq(SoOutstockEntity::getCode,kingdeeCode);

                // 按条件查询
                List<SoOutstockEntity> queryResult=soOutstockService.list(queryWrapper);
                if(CollectionUtil.isNotEmpty(queryResult)){
                    // 添加 id
                    queryResult.stream().forEach(item->result.add(item.getId()));
                }
            });
        }

        return result;
    }
}
