package com.erp.server.wms.schedule;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.base.BaseIdsDTO;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import com.erp.model.wms.entity.TransferInfoEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.wms.service.SoOutstockService;
import com.erp.server.wms.service.SoReturnInstockService;
import com.erp.server.wms.service.TransferInfoService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

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
    private TransferInfoService transferInfoService;

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
        Boolean isManual = param.get("isManual", Boolean.class);
        Boolean isPushKingdee = param.getBool("isPushKingdee", Boolean.FALSE);
        if(CollectionUtil.isEmpty(ids)){
            if("soReturnInstockService".equals(type)){
                ids = soOutstockService.getIdsByTemp("so_return_instock");
            }else if("transferInfoService".equals(type)){
                ids = soOutstockService.getIdsByTemp("transfer_info");
            }else if("soOutstockService".equals(type)){
                ids = soOutstockService.getIdsByTemp("so_outstock");
            }

        }
        if (ObjectUtils.isEmpty(ids)) {
            XxlJobHelper.log("参数错误ids={}", ids);
            return;
        }

        ids.parallelStream().forEach(item -> {
            BaseIdsDTO.IdsDTO idsDTO = new BaseIdsDTO.IdsDTO();
//            idsDTO.setIds(Collections.singletonList(item));
            SoOutstockEntity soOutstock = soOutstockService.getById(item);
            try {
                if(StrUtil.isNotBlank(type) && "soReturnInstockService".equals(type)){
                    SoReturnInstockEntity entity = soReturnInstockService.getById(item);
                    if (Objects.nonNull(entity)){
                        soReturnInstockService.disApprove(entity, isPushKingdee);
                    }
                }else if(StrUtil.isNotBlank(type) && "transferInfoService".equals(type)){
                    TransferInfoEntity entity = transferInfoService.getById(item);
                    if (Objects.nonNull(entity)){
                        transferInfoService.disApprove(entity, isPushKingdee, isManual);
                    }
                }else if(StrUtil.isNotBlank(type) && "soOutstockService".equals(type)){
                    soOutstockService.disApprove(soOutstock, isPushKingdee);
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
