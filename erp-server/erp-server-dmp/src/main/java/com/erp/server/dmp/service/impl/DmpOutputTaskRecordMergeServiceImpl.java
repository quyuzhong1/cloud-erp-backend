package com.erp.server.dmp.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.model.dmp.entity.DmpOutputTaskRecordMergeEntity;
import com.erp.model.dmp.entity.DmpPushMsgEntity;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.model.dmp.enums.OutputTaskRecordMergeStatusEnum;
import com.erp.server.dmp.inout.handler.output.task.api.DmpOutputErpPushTaskHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;
import com.erp.server.dmp.inout.utils.DmpOutputUtils;
import com.erp.server.dmp.mapper.DmpOutputTaskRecordMergeMapper;
import com.erp.server.dmp.service.CfgSettingService;
import com.erp.server.dmp.service.DmpOutputTaskRecordMergeService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.wrapper.FeignQuery;
import com.erp.server.dmp.service.DmpOutputTaskRecordService;
import com.erp.server.dmp.service.DmpPushMsgService;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpOutputTaskRecordMergeDTO;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
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
    
    @Resource
    private CfgSettingService cfgSettingService;
    
    @Resource
    private DmpPushMsgService dmpPushMsgService;
    
    @Autowired
	@Qualifier("dmpSdyOutputExecutorPool")
	private ExecutorService dmpSdyOutputExecutorPool;
    
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
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



    @Transactional(rollbackFor = Exception.class)
    @Override
    public void sdyMergePush(List<DmpOutputTaskRecordMergeEntity> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }

        List<String> ids = list.stream().map(DmpOutputTaskRecordMergeEntity::getMainId).collect(Collectors.toList());
        List<DmpOutputTaskRecordEntity> recordEntityList = dmpOutputTaskRecordService.listByIds(ids);
        if (CollUtil.isEmpty(recordEntityList)) {
            return;
        }
        List<String> dataList = new ArrayList<>();
        int i = 1;
        for(DmpOutputTaskRecordEntity recordEntity : recordEntityList) {
        	String requestData = recordEntity.getRequestData();
        	JSONObject parseObject = JSON.parseObject(requestData);
        	parseObject.put("dmpOutputTaskRecordId", recordEntity.getId());
			parseObject.put("dmpOutputTaskRecordDataId", recordEntity.getDataId());
			parseObject.put("dmpOutputTaskRecordIndex", "i" + i);
			dataList.add(parseObject.toJSONString());
			i = i + 1;
        }
        DmpOutputTaskRecordEntity entity = new DmpOutputTaskRecordEntity();
        entity.setMainId(recordEntityList.get(0).getMainId());
        entity.setDataId(recordEntityList.get(0).getDataId());
        entity.setStatus(DmpOutputTaskRecordStatusEnum.INIT.getCode());
        entity.setRequestData(dataList.toString());
        dmpOutputTaskRecordService.save(entity);

        String id = entity.getId();
        this.lambdaUpdate()
                .set(DmpOutputTaskRecordMergeEntity::getMergeId, id)
                .set(DmpOutputTaskRecordMergeEntity::getMergeStatus, OutputTaskRecordMergeStatusEnum.MERGE.getCode())
                .in(DmpOutputTaskRecordMergeEntity::getId, list.stream().map(DmpOutputTaskRecordMergeEntity::getId).collect(Collectors.toList()))
                .update();
        dmpOutputTaskRecordService.lambdaUpdate().in(DmpOutputTaskRecordEntity::getId, ids).setSql(" response_data = concat('合并记录id="+ id +";;' , response_data) ").update();
        
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
		    @Override
		    public void afterCommit() {
		    	List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList = new ArrayList<>();
		    	dmpOutputTaskRecordEntityList.add(entity);
		    	dmpSdyOutputExecutorPool.execute(() -> dmpOutputTaskRecordService.batchSync(dmpOutputTaskRecordEntityList));
		    }
		});
    }

    @Transactional(rollbackFor = Exception.class)
	@Override
	public boolean mergeDeal(DmpCfgOutputEntity dmpCfgOutputEntity,
			DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity) {
		if ("1801574477567165866".equals(dmpCfgOutputEntity.getSystemId())) {
			List<CfgSettingEntity> cfgList = cfgSettingService.lambdaQuery()
					.eq(CfgSettingEntity::getType, "sdy_batch_cfg")
					.eq(CfgSettingEntity::getKey, dmpCfgOutputEntity.getId())
					.eq(CfgSettingEntity::getValue, "1")
					.list();
			if(CollUtil.isEmpty(cfgList)) {
				return true;
			}
			String id = dmpOutputTaskRecordEntity.getId();
			List<DmpOutputTaskRecordMergeEntity> list = lambdaQuery()
					.eq(DmpOutputTaskRecordMergeEntity::getMergeId, id)
					.list();
			if (CollUtil.isEmpty(list)) {
				List<DmpOutputTaskRecordEntity> leDataIdList = dmpOutputTaskRecordService.lambdaQuery()
						.eq(DmpOutputTaskRecordEntity::getDataId, dmpOutputTaskRecordEntity.getDataId())
						.ne(DmpOutputTaskRecordEntity::getId, dmpOutputTaskRecordEntity.getId())
						.le(DmpOutputTaskRecordEntity::getCreateTime, dmpOutputTaskRecordEntity.getCreateTime())
						.list();
				if(CollUtil.isNotEmpty(leDataIdList)) {
					if(leDataIdList.stream().anyMatch(l -> !l.getStatus().equals(DmpOutputTaskRecordStatusEnum.FINISH.getCode()))) {
						dmpOutputTaskRecordService.lambdaUpdate()
							.set(DmpOutputTaskRecordEntity::getResponseData, "单据上一步操作未推送成功，同一dataId")
							.set(DmpOutputTaskRecordEntity::getUpdateTime, LocalDateTime.now())
							.eq(DmpOutputTaskRecordEntity::getId, dmpOutputTaskRecordEntity.getId())
							.ne(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.FINISH.getCode())
							.update();
//						return false;
					}else {
						if(validateMerge(leDataIdList.stream().map(DmpOutputTaskRecordEntity::getId).collect(Collectors.toList()), dmpOutputTaskRecordEntity)) {
//							return false;
						}
					}
				}
				
				String outputClass = dmpCfgOutputEntity.getOutputClass();
				if("DmpOutputErpPushTaskHandler".equals(outputClass)) {
					DmpOutputErpPushTaskHandler dmpOutputErpPushTaskHandler = ApplicationContextUtils.getBean(DmpHandlerUtils.dealBeanClass(outputClass) , DmpOutputErpPushTaskHandler.class);
					if(dmpOutputErpPushTaskHandler.validateSourceId(dmpCfgOutputEntity, dmpOutputTaskRecordEntity , true)) {
//						return false;
					}
				}
				
				String requestData = dmpOutputTaskRecordEntity.getRequestData();
				Boolean isQuerySync = JSON.parseObject(requestData).getBoolean("isQuerySync");
				String newRequestData = "";
				if (isQuerySync != null && isQuerySync) {
					List<DmpOutputTaskRecordEntity> erpQuerySync = dmpOutputTaskRecordService.erpQuerySync(dmpCfgOutputEntity, Arrays.asList(dmpOutputTaskRecordEntity));
					if(CollUtil.isNotEmpty(erpQuerySync)) {
						requestData = erpQuerySync.get(0).getRequestData();
						isQuerySync = JSON.parseObject(requestData).getBoolean("isQuerySync");
						if (isQuerySync != null && isQuerySync) {
							DmpPushMsgEntity dmpPushMsgEntity = dmpPushMsgService.getById(dmpOutputTaskRecordEntity.getDataId());
							if(dmpPushMsgEntity == null) {
								return true;
							}
							List<DmpPushMsgEntity> dmpPushMsgList = dmpPushMsgService.lambdaQuery()
								.eq(DmpPushMsgEntity::getSourceId, dmpPushMsgEntity.getSourceId())
								.ne(DmpPushMsgEntity::getId, dmpPushMsgEntity.getId())
								.eq(DmpPushMsgEntity::getSourcePlatform, dmpPushMsgEntity.getSourcePlatform())
								.eq(DmpPushMsgEntity::getTargetPlatform, dmpPushMsgEntity.getTargetPlatform())
								.le(DmpPushMsgEntity::getMessageUpdateTime, dmpPushMsgEntity.getMessageUpdateTime())
								.list();
							if(CollUtil.isEmpty(dmpPushMsgList)) {
								return true;
							}
							List<DmpOutputTaskRecordEntity> sourceIdList = dmpOutputTaskRecordService.lambdaQuery()
								.in(DmpOutputTaskRecordEntity::getDataId, dmpPushMsgList.stream().map(DmpPushMsgEntity::getId).collect(Collectors.toList()))
								.eq(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.FINISH.getCode())
								.likeRight(DmpOutputTaskRecordEntity::getRequestData, "{")
								.notLike(DmpOutputTaskRecordEntity::getRequestData, "isQuerySync")
								.orderByDesc(DmpOutputTaskRecordEntity::getCreateTime)
								.list();
							if(CollUtil.isEmpty(sourceIdList)) {
								return true;
							}
							JSONObject parseObject = JSON.parseObject(sourceIdList.get(0).getRequestData());
							parseObject.put("status", "已删除");
							newRequestData = parseObject.toJSONString();
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
					.set(StringUtils.isNotBlank(newRequestData) ,  DmpOutputTaskRecordEntity::getRequestData, newRequestData)
					.update();
				return false;
			}
		}
		return true;
	}

    @Override
	public boolean validateMerge(List<String> leMergeList , DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity) {
    	if(CollUtil.isEmpty(leMergeList)) {
    		return false;
    	}
    	List<DmpOutputTaskRecordMergeEntity> leMergeEntityList = this.lambdaQuery()
    			.in(DmpOutputTaskRecordMergeEntity::getMainId, leMergeList)
    			.list();
    	if(CollUtil.isNotEmpty(leMergeEntityList)) {
			if(leMergeEntityList.stream().anyMatch(l -> l.getMergeStatus().equals(OutputTaskRecordMergeStatusEnum.WAIT_MERGE.getCode()))) {
				dmpOutputTaskRecordService.lambdaUpdate()
					.set(DmpOutputTaskRecordEntity::getResponseData, "单据上一步操作未推送成功，同一dataId是待合并")
					.set(DmpOutputTaskRecordEntity::getUpdateTime, LocalDateTime.now())
					.eq(DmpOutputTaskRecordEntity::getId, dmpOutputTaskRecordEntity.getId())
					.ne(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.FINISH.getCode())
					.update();
				return true;
			}else {
				List<String> mergeIds = leMergeEntityList.stream().filter(l -> l.getMergeStatus().equals(OutputTaskRecordMergeStatusEnum.MERGE.getCode()))
					.map(DmpOutputTaskRecordMergeEntity::getMergeId).collect(Collectors.toList());
				if(CollUtil.isNotEmpty(mergeIds)) {
					Integer mergeCount = dmpOutputTaskRecordService.lambdaQuery()
						.in(DmpOutputTaskRecordEntity::getId, mergeIds)
						.ne(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.FINISH.getCode())
						.count();
					if(mergeCount != null && mergeCount > 0) {
						dmpOutputTaskRecordService.lambdaUpdate()
							.set(DmpOutputTaskRecordEntity::getResponseData, "单据上一步操作未推送成功，同一dataId是已合并")
							.set(DmpOutputTaskRecordEntity::getUpdateTime, LocalDateTime.now())
							.eq(DmpOutputTaskRecordEntity::getId, dmpOutputTaskRecordEntity.getId())
							.ne(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.FINISH.getCode())
							.update();
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void querySyncMergeDeal(DmpCfgOutputEntity dmpCfgOutputEntity,
			List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList) {
    	if ("1801574477567165866".equals(dmpCfgOutputEntity.getSystemId())) {
    		List<DmpOutputTaskRecordMergeEntity> mergeList = lambdaQuery()
    	        	.in(DmpOutputTaskRecordMergeEntity::getMainId, dmpOutputTaskRecordEntityList.stream().map(DmpOutputTaskRecordEntity::getId).collect(Collectors.toList()))
    	        	.list();
    		if(CollUtil.isNotEmpty(mergeList)) {
    			Map<String, DmpOutputTaskRecordEntity> mergeRecordIdMaps = dmpOutputTaskRecordService.lambdaQuery()
	    			.in(DmpOutputTaskRecordEntity::getId, mergeList.stream().map(DmpOutputTaskRecordMergeEntity::getMergeId).collect(Collectors.toList()))
	    			.in(DmpOutputTaskRecordEntity::getStatus, Arrays.asList(DmpOutputTaskRecordStatusEnum.INIT.getCode() , 
	    					DmpOutputTaskRecordStatusEnum.COSUMERERROR.getCode() , DmpOutputTaskRecordStatusEnum.ERROR.getCode()))
	    			.list().stream().collect(Collectors.toMap(DmpOutputTaskRecordEntity::getId, d -> d));
    			
    			Map<String, DmpOutputTaskRecordEntity> mainIdEntityMaps = dmpOutputTaskRecordEntityList.stream().collect(Collectors.toMap(DmpOutputTaskRecordEntity::getId, d -> d));
    			
    			Set<DmpOutputTaskRecordEntity> needAddEntity = new HashSet<>();
    			Set<String> needDeleteId = new HashSet<>();
    			for(DmpOutputTaskRecordMergeEntity merge : mergeList) {
    				String mainId = merge.getMainId();
    				needDeleteId.add(mainId);
    				log.warn("被合并数据查询同步无需推送" + mainId);
    				DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity = mergeRecordIdMaps.get(merge.getMergeId());
    				if(dmpOutputTaskRecordEntity == null) {
    					continue;
    				}
    				DmpOutputTaskRecordEntity mainIdEntity = mainIdEntityMaps.get(mainId);
    				String requestData = dmpOutputTaskRecordEntity.getRequestData();
    				List<JSONObject> parseArray = JSON.parseArray(requestData , JSONObject.class);
    				parseArray = parseArray.stream().map(p -> {
    					String dmpOutputTaskRecordDataId = p.getString("dmpOutputTaskRecordId");
    					if(StringUtils.isNotBlank(dmpOutputTaskRecordDataId) && mainId.equals(dmpOutputTaskRecordDataId)) {
    						return JSON.parseObject(mainIdEntity.getRequestData());
    					}else {
    						return p;
    					}
    				}).collect(Collectors.toList());
    				dmpOutputTaskRecordEntity.setRequestData(JSON.toJSONString(parseArray));
    				needAddEntity.add(dmpOutputTaskRecordEntity);
    			}
    			
    			if(CollUtil.isNotEmpty(needAddEntity)) {
    				dmpOutputTaskRecordEntityList.addAll(needAddEntity);
    				dmpOutputTaskRecordService.saveOrUpdateBatch(needAddEntity);
    			}
    			if(CollUtil.isNotEmpty(needDeleteId)) {
    				dmpOutputTaskRecordEntityList.removeIf(d -> needDeleteId.contains(d.getId()));
    				dmpOutputTaskRecordService.lambdaUpdate().in(DmpOutputTaskRecordEntity::getId, needDeleteId)
	    				.set(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.FINISH.getCode())
	    				.set(DmpOutputTaskRecordEntity::getUpdateTime, LocalDateTime.now())
	    				.update();
    			}
    		}
    	}
    	dmpOutputTaskRecordService.batchSync(dmpOutputTaskRecordEntityList);
	}
}
