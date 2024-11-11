package com.erp.server.dmp.service.impl;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.constant.DmpConstant;
import com.erp.model.dmp.dto.*;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.DmpInputTaskDTO;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;
import com.erp.server.dmp.mapper.DmpInputTaskMapper;
import com.erp.server.dmp.service.DmpInputTaskService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 拉取任务 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@Service
public class DmpInputTaskServiceImpl extends SuperServiceImpl<DmpInputTaskMapper, DmpInputTaskEntity> implements DmpInputTaskService {
	@Resource
    private MQProducerService mqProducerService;
	
	@Resource
	private DmpHandlerCache dmpHandlerCache;
	
	@GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpInputTaskDTO.AddDTO addDTO) {
        DmpInputTaskEntity dmpInputTaskEntity = new DmpInputTaskEntity();
        BeanMapperUtils.copy(addDTO, dmpInputTaskEntity);

        // 数据处理
        handleData(dmpInputTaskEntity);

        log.info("开始新增拉取任务");
        boolean save = super.save(dmpInputTaskEntity);
        if(!save) {
            throw new ServiceException("拉取任务保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "拉取任务" , dmpInputTaskEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpInputTaskEntity.getId(), dmpInputTaskEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpInputTaskDTO.UpdateDTO updateDTO) {
        DmpInputTaskEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "拉取任务"));
        DmpInputTaskEntity dmpInputTaskEntity =  BeanMapperUtils.map(DmpInputTaskEntity.class, updateDTO);

        // 数据处理
        handleData(dmpInputTaskEntity);
        log.info("编辑 开始修改拉取任务数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpInputTaskEntity);
        if(!save) {
            throw new ServiceException("拉取任务保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录拉取任务日志数据，id：【{}】", dmpInputTaskEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpInputTaskEntity.getId(), "拉取任务");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpInputTaskEntity dmpInputTaskEntity) {
    // TODO 验证数据 & 数据赋值
    }
    
    @Transactional(rollbackFor = Exception.class , propagation = Propagation.REQUIRES_NEW)
    public boolean updateErrorStatus(String id , boolean errorFlag , Integer errorCount , Exception e) {
    	String errorBeforeStatus = "";
    	DmpInputTaskEntity dmpInputTaskEntity = null;
    	if(errorFlag) {
    		dmpInputTaskEntity = getById(id);
			errorBeforeStatus = dmpInputTaskEntity.getStatus() + "@@";
    	}
    	String errorMessage = errorBeforeStatus + "traceId=【" + MDC.get("traceId") + "】" + ExceptionUtil.stacktraceToString(e);
		boolean update = lambdaUpdate().eq(DmpInputTaskEntity::getId, id)
				.set(DmpInputTaskEntity::getErrorCount, errorCount)
				.set(errorFlag , DmpInputTaskEntity::getStatus, DmpInputTaskStatusEnum.ERROR.getCode())
				.set(DmpInputTaskEntity::getUpdateTime, LocalDateTime.now())
				.set(DmpInputTaskEntity::getErrorMessage,  errorMessage)
				.update();
    	
		if(errorFlag) {
			WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
	        warnMsgInfo.setBizName("新中台拉取");
	        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_DMP);
	        warnMsgInfo.setTitle("新中台拉取失败，id=" + id);
	        warnMsgInfo.setTableName("dmp_input_task");
	        warnMsgInfo.setTableId(id);
	        warnMsgInfo.setKeyInfo(errorMessage);
	        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
	        mqProducerService.sendWarnMsg(warnMsgInfo);
	        
	        String name = "";
	        String cfgInputId = dmpInputTaskEntity.getCfgInputId();
	        List<DmpCfgInputEntity> dmpCfgInputEntityList = dmpHandlerCache.getDmpCfgInputEntityList(d -> d.getId().equals(cfgInputId));
	        if(CollUtil.isNotEmpty(dmpCfgInputEntityList)) {
	        	DmpCfgInputEntity dmpCfgInputEntity = dmpCfgInputEntityList.get(0);
	        	name = dmpCfgInputEntity.getName();
	        }
	        DmpHandlerUtils.sendFeiShuMsg("输入任务记录id=【" + id + "】处理失败：" + name + "【" + e.getMessage() + "】");
		}
    	
		return update;
	}

	@Override
	public List<DmpInoutDTO.LastOneDTO> lastBySystemCodeAndBillType(List<String> strings, List<String> strings1, List<String> nextLevelIdList) {
		return baseMapper.lastBySystemCodeAndBillType(strings, strings1, nextLevelIdList);
	}

}
