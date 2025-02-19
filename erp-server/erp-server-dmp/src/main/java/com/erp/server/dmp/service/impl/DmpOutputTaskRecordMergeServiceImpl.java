package com.erp.server.dmp.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSON;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.model.dmp.entity.DmpOutputTaskRecordMergeEntity;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.model.dmp.enums.OutputTaskRecordMergeStatusEnum;
import com.erp.server.dmp.inout.utils.DmpOutputUtils;
import com.erp.server.dmp.mapper.DmpOutputTaskRecordMergeMapper;
import com.erp.server.dmp.service.DmpOutputTaskRecordMergeService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.DmpOutputTaskRecordService;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpOutputTaskRecordMergeDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 推送任务记录合并表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2025-02-18
 */
@Slf4j
@Service
public class DmpOutputTaskRecordMergeServiceImpl extends SuperServiceImpl<DmpOutputTaskRecordMergeMapper, DmpOutputTaskRecordMergeEntity> implements DmpOutputTaskRecordMergeService {
    @Autowired
    private OperateLogService operateLogService;

    @Resource
    private DmpOutputTaskRecordService dmpOutputTaskRecordService;
    
    @Autowired
	protected DmpOutputUtils dmpOutputUtils;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpOutputTaskRecordMergeDTO.AddDTO addDTO) {
        DmpOutputTaskRecordMergeEntity dmpOutputTaskRecordMergeEntity = new DmpOutputTaskRecordMergeEntity();
        BeanMapperUtils.copy(addDTO, dmpOutputTaskRecordMergeEntity);

        // 数据处理
        handleData(dmpOutputTaskRecordMergeEntity);

        log.info("开始新增推送任务记录合并单");
        boolean save = super.save(dmpOutputTaskRecordMergeEntity);
        if(!save) {
            throw new ServiceException("推送任务记录合并单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "推送任务记录合并单" , dmpOutputTaskRecordMergeEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpOutputTaskRecordMergeEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpOutputTaskRecordMergeEntity.getId(), dmpOutputTaskRecordMergeEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpOutputTaskRecordMergeDTO.UpdateDTO addOrUpdateDTO) {
        DmpOutputTaskRecordMergeEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "推送任务记录合并单"));
        DmpOutputTaskRecordMergeEntity dmpOutputTaskRecordMergeEntity =  BeanMapperUtils.map(DmpOutputTaskRecordMergeEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(dmpOutputTaskRecordMergeEntity);
        log.info("编辑 开始修改推送任务记录合并单数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpOutputTaskRecordMergeEntity);
        if(!save) {
            throw new ServiceException("推送任务记录合并单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录推送任务记录合并单日志数据，id：【{}】", dmpOutputTaskRecordMergeEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpOutputTaskRecordMergeEntity.getId(), "推送任务记录合并单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpOutputTaskRecordMergeEntity, null, dmpOutputTaskRecordMergeEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpOutputTaskRecordMergeEntity dmpOutputTaskRecordMergeEntity) {
    // TODO 验证数据 & 数据赋值
    }



    @Override
    public void sdyMergePush() {
        List<DmpOutputTaskRecordMergeEntity> list = this.lambdaQuery()
                .eq(DmpOutputTaskRecordMergeEntity::getMergeStatus, OutputTaskRecordMergeStatusEnum.WAIT_MERGE.getCode())
                .last("LIMIT 1000")
                .list();
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<String> ids = list.stream().map(DmpOutputTaskRecordMergeEntity::getMainId).collect(Collectors.toList());
        List<DmpOutputTaskRecordEntity> recordEntityList = dmpOutputTaskRecordService.listByIds(ids);
        if (CollUtil.isEmpty(recordEntityList)) {
            return;
        }
        List<String> dataList = recordEntityList.stream().map(DmpOutputTaskRecordEntity::getRequestData).collect(Collectors.toList());
        DmpOutputTaskRecordEntity entity = new DmpOutputTaskRecordEntity();
        entity.setMainId(recordEntityList.get(0).getMainId());
        entity.setStatus(DmpOutputTaskRecordStatusEnum.INIT.getCode());
        entity.setRequestData(dataList.toString());
        dmpOutputTaskRecordService.save(entity);

        String id = entity.getId();
        this.lambdaUpdate()
                .set(DmpOutputTaskRecordMergeEntity::getMergeId, id)
                .set(DmpOutputTaskRecordMergeEntity::getMergeStatus, OutputTaskRecordMergeStatusEnum.MERGE.getCode())
                .in(DmpOutputTaskRecordMergeEntity::getMainId, ids)
                .update();
    }

    @Transactional(rollbackFor = Exception.class)
	@Override
	public boolean mergeDeal(DmpCfgOutputEntity dmpCfgOutputEntity,
			DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity) {
		if ("1801574477567165866".equals(dmpCfgOutputEntity.getSystemId()) ) {
			String id = dmpOutputTaskRecordEntity.getId();
			List<DmpOutputTaskRecordMergeEntity> list = lambdaQuery()
					.eq(DmpOutputTaskRecordMergeEntity::getMergeId, id)
					.list();
			if (CollUtil.isEmpty(list)) {
				String requestData = dmpOutputTaskRecordEntity.getRequestData();
				Boolean isQuerySync = JSON.parseObject(requestData).getBoolean("isQuerySync");
				if (isQuerySync != null && isQuerySync) {
					List<DmpOutputTaskRecordEntity> erpQuerySync = dmpOutputTaskRecordService.erpQuerySync(dmpCfgOutputEntity, Arrays.asList(dmpOutputTaskRecordEntity));
					if(CollUtil.isNotEmpty(erpQuerySync)) {
						requestData = erpQuerySync.get(0).getRequestData();
						isQuerySync = JSON.parseObject(requestData).getBoolean("isQuerySync");
						if (isQuerySync != null && isQuerySync) {
							return true;
						}
					}
				}
				DmpOutputTaskRecordMergeEntity entity = new DmpOutputTaskRecordMergeEntity();
				entity.setMainId(id);
				entity.setMergeStatus(OutputTaskRecordMergeStatusEnum.WAIT_MERGE.getCode());
				save(entity);
				dmpOutputTaskRecordService.lambdaUpdate()
					.eq(DmpOutputTaskRecordEntity::getId, id)
					.set(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.FINISH.getCode())
					.set(DmpOutputTaskRecordEntity::getResponseData, "待合并id=" + entity.getId())
					.update();
				return false;
			}
		}
		return true;
	}
}
