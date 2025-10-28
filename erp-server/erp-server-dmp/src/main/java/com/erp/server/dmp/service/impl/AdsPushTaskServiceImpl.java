package com.erp.server.dmp.service.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.AdsPushTaskDTO;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO.TabListDTO;
import com.erp.model.dmp.entity.doris.AdsPushTaskEntity;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.model.dmp.enums.DmpPushMonitorTabEnum;
import com.erp.server.dmp.mapper.doris.AdsPushTaskMapper;
import com.erp.server.dmp.service.AdsPushTaskService;
import com.erp.server.dmp.service.OperateLogService;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * ads推送任务 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2025-10-28
 */
@DS("adsDoris")
@Slf4j
@Service
public class AdsPushTaskServiceImpl extends SuperServiceImpl<AdsPushTaskMapper, AdsPushTaskEntity> implements AdsPushTaskService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AdsPushTaskDTO.AddDTO addDTO) {
        AdsPushTaskEntity adsPushTaskEntity = new AdsPushTaskEntity();
        BeanMapperUtils.copy(addDTO, adsPushTaskEntity);

        // 数据处理
        handleData(adsPushTaskEntity);

        log.info("开始新增ads推送任务");
        boolean save = super.save(adsPushTaskEntity);
        if(!save) {
            throw new ServiceException("ads推送任务保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "ads推送任务" , adsPushTaskEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, adsPushTaskEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(adsPushTaskEntity.getId(), adsPushTaskEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AdsPushTaskDTO.UpdateDTO addOrUpdateDTO) {
        AdsPushTaskEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "ads推送任务"));
        AdsPushTaskEntity adsPushTaskEntity =  BeanMapperUtils.map(AdsPushTaskEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(adsPushTaskEntity);
        log.info("编辑 开始修改ads推送任务数据，id：【{}】", old.getId());
        boolean save = super.updateById(adsPushTaskEntity);
        if(!save) {
            throw new ServiceException("ads推送任务保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录ads推送任务日志数据，id：【{}】", adsPushTaskEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), adsPushTaskEntity.getId(), "ads推送任务");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, adsPushTaskEntity, null, adsPushTaskEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(AdsPushTaskEntity adsPushTaskEntity) {
    // TODO 验证数据 & 数据赋值
    }

	@Override
	public List<TabListDTO> tabList(PermissionsDTO dto) {
		DmpPushMonitorTabEnum[] values = DmpPushMonitorTabEnum.values();
		List<DmpOutputTaskRecordDTO.TabListDTO> result = new ArrayList<>(values.length);
		QueryWrapper<AdsPushTaskEntity> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("is_deleted", false);
		queryWrapper.groupBy("status");
		queryWrapper.select(" status , count(*) errorCount ");
		List<AdsPushTaskEntity> adsPushTaskEntityList = list(queryWrapper);
		Map<String, Integer> statusCountMap = adsPushTaskEntityList.stream().collect(Collectors.toMap(AdsPushTaskEntity::getStatus, AdsPushTaskEntity::getErrorCount));
		for(DmpPushMonitorTabEnum v : values) {
			DmpOutputTaskRecordDTO.TabListDTO tabDto = new TabListDTO();
			tabDto.setTabFlag(v.getCode());
			int count = 0;
			if(DmpPushMonitorTabEnum.INIT == v) {
				count = statusCountMap.getOrDefault(DmpOutputTaskRecordStatusEnum.INIT.getCode() , 0);
			}else if(DmpPushMonitorTabEnum.PUSH_ING == v) {
				count = statusCountMap.getOrDefault(DmpOutputTaskRecordStatusEnum.MQSUCCESS.getCode() , 0);
				count = count + statusCountMap.getOrDefault(DmpOutputTaskRecordStatusEnum.MQERROR.getCode() , 0);
				count = count + statusCountMap.getOrDefault(DmpOutputTaskRecordStatusEnum.COSUMERERROR.getCode() , 0);
			}else if(DmpPushMonitorTabEnum.FINISH == v) {
				count = statusCountMap.getOrDefault(DmpOutputTaskRecordStatusEnum.FINISH.getCode() , 0);
			}else if(DmpPushMonitorTabEnum.ERROR == v) {
				count = statusCountMap.getOrDefault(DmpOutputTaskRecordStatusEnum.ERROR.getCode() , 0);
			}else if(DmpPushMonitorTabEnum.NO_NEED_SYNC == v) {
				count = lambdaQuery().eq(AdsPushTaskEntity::getPushStatus, "self").count();
			}else if(DmpPushMonitorTabEnum.BLACK == v) {
				count = lambdaQuery().in(AdsPushTaskEntity::getPushStatus, "black").count();
			}
			tabDto.setCount(count);
			result.add(tabDto);
		}
		return result;
	}
}
