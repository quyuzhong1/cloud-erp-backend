package com.erp.server.dmp.service.impl;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.constant.DmpConstant;
import com.erp.model.dmp.dto.*;
import com.erp.model.dmp.entity.DmpCfgInputDetailEntity;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.model.dmp.enums.DmpTaskStatuEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.dmp.service.OperateLogService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
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
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
	
	@GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
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
    		if(dmpInputTaskEntity == null) {
    			return true;
    		}
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
//	        mqProducerService.sendWarnMsg(warnMsgInfo);
	        
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
    
    @Transactional(rollbackFor = Exception.class , propagation = Propagation.REQUIRES_NEW)
    public boolean updateNextExecTime(String id , LocalDateTime nextExecTime) {
    	return lambdaUpdate().eq(DmpInputTaskEntity::getId, id)
    			.set(DmpInputTaskEntity::getNextExecTime, nextExecTime)
    			.update();
    }

	@Override
	public List<DmpInoutDTO.LastOneDTO> lastBySystemCodeAndBillType(List<String> systemCodeList, List<String> billTypeList, List<String> nextLevelIdList) {
		return baseMapper.lastBySystemCodeAndBillType(systemCodeList, billTypeList, nextLevelIdList);
	}

	@Override
	public DmpInputTaskEntity getByInputIdAndExtendJson(String inputId, String key, String value) {
		if( StrUtil.isBlank(inputId) || StrUtil.isBlank(key) || StrUtil.isBlank(value)) {
			return null;
		}
		return baseMapper.getByInputIdAndExtendJson(inputId,key,value);
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void createNewTask(DmpInputTaskEntity dmpInputTaskEntity) {
		String errorMessage = dmpInputTaskEntity.getErrorMessage();
		String id = dmpInputTaskEntity.getId();
		String init = DmpInputTaskStatusEnum.INIT.getCode();
		if(StringUtils.isNotBlank(errorMessage) && errorMessage.startsWith(init)) {
			lambdaUpdate().eq(DmpInputTaskEntity::getId, id).set(DmpInputTaskEntity::getStatus, init).set(DmpInputTaskEntity::getErrorCount, 0).update();
		}else {
			dmpInputTaskEntity.setId(null);
			dmpInputTaskEntity.setStatus(init);
			dmpInputTaskEntity.setErrorCount(0);
			save(dmpInputTaskEntity);
			removeById(id);
		}
	}

    @Override
    public PagingVO<DmpInputTaskDTO.ListDTO> paging(PagingDTO<DmpInputTaskDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<DmpInputTaskDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<DmpInputTaskDTO.TabListDTO> tabList(PermissionsDTO param) {
        DmpInputTaskDTO.PagingParamDTO searchParam = new DmpInputTaskDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<DmpInputTaskDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        int total = list.stream().mapToInt(DmpInputTaskDTO.TabListDTO::getCount).sum();
        List<DmpInputTaskDTO.TabListDTO> resultList = new ArrayList<>();
        // 计算合计数量
        resultList.add(new DmpInputTaskDTO.TabListDTO("all", "全部", total));

        List<DmpTaskStatuEnum> statusList = Arrays.stream(DmpTaskStatuEnum.values()).collect(Collectors.toList());
        // 不存在的状态赋值为0
        Map<String, DmpInputTaskDTO.TabListDTO> listMap = list.stream().collect(Collectors.toMap(DmpInputTaskDTO.TabListDTO::getTabFlag, e -> e));
        statusList.forEach(status -> {
            DmpInputTaskDTO.TabListDTO tabListDTO = listMap.get(status.getCode());
            if (tabListDTO != null) {
                resultList.add(tabListDTO);
            } else {
                resultList.add(new DmpInputTaskDTO.TabListDTO(status.getCode(), status.getName(), 0));
            }
        });
        return resultList;
    }

    @Override
    public void exportList(DmpInputTaskDTO.ExportDTO dto, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("拉取任务Excel导出", FileTaskEventEnum.EXPORT_DMP_INPUT_TASK.getCode(), dto);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        DmpInputTaskEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到拉取任务数据"));
        // TODO 删除明细数据（如果有明细数据的话）

        // 删除主单数据
        log.info("删除 开始删除拉取任务主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除拉取任务日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getId(), "拉取任务");
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "删除拉取任务数据");
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.DELETE);
    }



    @Override
    public DmpInputTaskDTO.ViewDTO view(String id) {
        DmpInputTaskEntity dmpInputTaskEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到拉取任务数据"));
        DmpInputTaskDTO.ViewDTO data = BeanMapperUtils.map(DmpInputTaskDTO.ViewDTO.class, dmpInputTaskEntity);
        // 数据填充处理
        fillOne(data);
        // TODO 查询明细数据（如果有的话）
        return data;
    }

    private void fillOne(DmpInputTaskDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<DmpInputTaskDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }

        // 属性赋值
        for(DmpInputTaskDTO.ListDTO data : list) {
            // TODO 其他如需要显示名称的字段赋值
        }
    }

}
