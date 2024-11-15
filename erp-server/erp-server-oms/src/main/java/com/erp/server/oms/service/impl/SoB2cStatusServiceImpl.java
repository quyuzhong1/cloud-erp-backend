package com.erp.server.oms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.PlatformDeliveryInterceptDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.threadlocal.UserContext;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cFrozenTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoB2cService;
import com.erp.server.oms.service.SoB2cStatusService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * B2C销售订单表 状态服务类
 * </p>
 *
 * @author Jim
 * @date 2024/4/26 11:32
 */
@Service
public class SoB2cStatusServiceImpl implements SoB2cStatusService {

    @Resource
    private SoB2cService soB2cService;
    @Resource
    private OperateLogService operateLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateCancelAndLog(PlatformDeliveryInterceptDTO dto) {
        boolean update = soB2cService.lambdaUpdate()
                .set(SoB2cEntity::getIsCancel, dto.getOldIsCancel())
                .eq(SoB2cEntity::getId, dto.getSoB2cId())
                .update();
        if (update){
            // 添加日志
            operateLogService.addModuleOperateLog("系统更新平台订单为取消状态", ModuleTypeEnum.SO_B2C.getCode(), dto.getSoB2cId(), "系统更新");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchUpdateCancelAndLog(List<String> soB2cIdList) {
        boolean update = soB2cService.lambdaUpdate()
                .set(SoB2cEntity::getIsCancel, true)
                .in(SoB2cEntity::getId, soB2cIdList)
                .update();
        if (update){
            // 添加日志
            List<Pair<String, String>> pairList = soB2cIdList.stream()
                    .map(obj -> new Pair<>(obj, obj))
            .collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("系统更新平台订单为取消状态", ModuleTypeEnum.SO_B2C.getCode(), pairList, "系统更新");
        }
        return null;
    }

    @Override
    public BatchResultDTO freeze(String id, List<SoB2cEntity> soB2cEntityList) {
        SoB2cEntity soB2cEntity = soB2cEntityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
        if(Objects.isNull(soB2cEntity)){
            return BatchResultDTO.fail(id,id,"找不到销售订单");
        }

        if(!SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(soB2cEntity.getBillStatus()) && !SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode().equals(soB2cEntity.getBillStatus())){
            return BatchResultDTO.fail(id,soB2cEntity.getCode(),"只有待配货、配货中的订单允许操作冻结");
        }

        if(soB2cEntity.getIsFrozen()){
            return BatchResultDTO.fail(id,soB2cEntity.getCode(),"订单已冻结");
        }
        soB2cEntity.setIsFrozen(true);
        soB2cEntity.setFrozenType(SoB2cFrozenTypeEnum.ENUM_MANUAL.getCode());
        soB2cEntity.setBillStatus(SoB2cBillStatusEnum.ENUM_FROZEN.getCode());
        soB2cService.updateById(soB2cEntity);

        String msg =  CharSequenceUtil.format("用户【{}】冻结订单 ", UserContext.getDefaultLoginUser().getUserName());
        operateLogService.addModuleOperateLog(msg ,ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), "冻结订单");
        return BatchResultDTO.success(id,soB2cEntity.getCode(),"操作成功");
    }

    @Override
    public BatchResultDTO unfreeze(String id, List<SoB2cEntity> soB2cEntityList, List<SoB2cDetailEntity> allSoB2cDetailEntityList, List<SoB2cLogisticsEntity> soB2cLogisticsEntityList) {

        SoB2cEntity soB2cEntity = soB2cEntityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
        if(Objects.isNull(soB2cEntity)){
            return BatchResultDTO.fail(id,id,"找不到销售订单");
        }

        if(!SoB2cBillStatusEnum.ENUM_FROZEN.getCode().equals(soB2cEntity.getBillStatus()) && !soB2cEntity.getIsFrozen()){
            return BatchResultDTO.fail(id,soB2cEntity.getCode(),"订单未冻结");
        }

        if(!SoB2cFrozenTypeEnum.ENUM_MANUAL.getCode().equals(soB2cEntity.getFrozenType())){
            return BatchResultDTO.fail(id,soB2cEntity.getCode(),"只有手动冻结的订单支持取消冻结");
        }
        List<SoB2cDetailEntity> soB2cDetailEntityList = allSoB2cDetailEntityList.stream().filter(v->v.getMainId().equals(id)).collect(Collectors.toList());
        SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsEntityList.stream().filter(v->v.getMainId().equals(id)).findFirst().orElse(new SoB2cLogisticsEntity());
        soB2cEntity.setIsFrozen(false);
        soB2cEntity.setFrozenType("");
        if(StringUtils.isNotBlank(soB2cLogisticsEntity.getLogisticsChannelId()) && soB2cDetailEntityList.stream().allMatch(v->StringUtils.isNotBlank(v.getWarehouseId()))){
            soB2cEntity.setBillStatus(SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode());
        }else{
            soB2cEntity.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
        }
        soB2cService.updateById(soB2cEntity);

        String msg =  CharSequenceUtil.format("用户【{}】取消冻结订单 ", UserContext.getDefaultLoginUser().getUserName());
        operateLogService.addModuleOperateLog(msg ,ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), "取消冻结");
        return BatchResultDTO.success(id,soB2cEntity.getCode(),"操作成功");
    }
}
