package com.erp.server.dmp.service.impl;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.AdsPushTaskDTO;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO.AddOutputBlackDTO;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO.ExpotParamDTO;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO.PagingParamDTO;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO.TabListDTO;
import com.erp.model.dmp.entity.doris.AdsPushTaskEntity;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.model.dmp.enums.DmpPushMonitorTabEnum;
import com.erp.model.sys.entity.DictBasicEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.dmp.mapper.doris.AdsPushTaskMapper;
import com.erp.server.dmp.service.AdsPushTaskService;
import com.erp.server.dmp.service.OperateLogService;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
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
    
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    
    @Value("${restcloud.url:172.16.100.96}")
    private String restcloudUrl;
	
	@Value("${restcloud.port:8080}")
	private String restcloudPort;

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
		queryWrapper.eq("push_status", "push");
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

	@Override
	public PagingVO<DmpOutputTaskRecordDTO.PagingDTO> paging(PagingDTO<PagingParamDTO> dto) {
		DmpOutputTaskRecordDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<DmpOutputTaskRecordDTO.PagingDTO> records = pageData.getRecords();
        //数据处理
        doOpHandleDmpPushTask(records);
        return new PagingVO<>(pageData);
	}
	
	private void doOpHandleDmpPushTask(List<DmpOutputTaskRecordDTO.PagingDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<DictBasicEntity> dictBasicEntityList = FeignQuery.create(DictBasicEntity.class).eq(DictBasicEntity::getType, "sourceType").list();
        Map<String, String> valueRemarkMap = dictBasicEntityList.stream().collect(Collectors.toMap(DictBasicEntity::getValue, DictBasicEntity::getRemark , (d1 , d2) -> d1));
        for (DmpOutputTaskRecordDTO.PagingDTO listDTO : list) {
            listDTO.setSyncTypeName("推送");

            //如果是推送成功，可能是无需推送状态
            if (DmpOutputTaskRecordStatusEnum.FINISH.getCode().equals(listDTO.getStatus()) && !listDTO.getIsNeedSync()) {
                listDTO.setStatusName(SyncStatusEnum.NO_NEED_SYNC.getName());
            } else {
                listDTO.setStatusName(DmpOutputTaskRecordStatusEnum.getName(listDTO.getStatus()));
            }
            String sourceTypeName = valueRemarkMap.get(listDTO.getSourceTypeName());
            if(StringUtils.isNotBlank(sourceTypeName)) {
            	listDTO.setSourceTypeName(sourceTypeName);
            }
        }
    }

	@Override
	public Boolean exportExcel(ExpotParamDTO dto) {
		downloadTaskFeign.saveDownloadTask("谷云推送任务表", FileTaskEventEnum.EXPORT_RESTCLOUD_PUSH_TASK.getCode(), dto);
        return Boolean.TRUE;
	}

	@Override
	public Boolean batchNoNeedSync(List<String> ids, String remark) {
		return lambdaUpdate().in(AdsPushTaskEntity::getId, ids)
				.set(AdsPushTaskEntity::getPushStatus, "self")
				.set(AdsPushTaskEntity::getStatus, DmpOutputTaskRecordStatusEnum.FINISH.getCode())
				.set(AdsPushTaskEntity::getResponseData, remark)
				.update();
	}

	@Override
	public Boolean addOutputBlack(AddOutputBlackDTO dto) {
		return lambdaUpdate().in(AdsPushTaskEntity::getId, dto.getIds())
				.set(AdsPushTaskEntity::getPushStatus, "black")
				.set(AdsPushTaskEntity::getStatus, DmpOutputTaskRecordStatusEnum.FINISH.getCode())
				.set(AdsPushTaskEntity::getResponseData, dto.getRemark())
				.update();
	}

	@Override
	public BatchResultDTO cancelOutputBlack(String id) {
		lambdaUpdate().eq(AdsPushTaskEntity::getId, id)
				.set(AdsPushTaskEntity::getPushStatus, "push")
				.update();
		return BatchResultDTO.success(id, id);
	}

	@Override
	public Boolean batchSync(List<String> ids) {
		lambdaUpdate().set(AdsPushTaskEntity::getPushStatus, "push")
        	.set(AdsPushTaskEntity::getStatus, DmpOutputTaskRecordStatusEnum.INIT.getCode())
        	.in(AdsPushTaskEntity::getId, ids)
        	.update();
		
		Map<String, Object> map = new HashMap<>();
		Map<String, String> idSqlMap = new HashMap<>();
		idSqlMap.put("idSql", " and id in (" + ids.stream().collect(Collectors.joining("','", "'", "'")) + " )");
		map.put("data", Arrays.asList(idSqlMap));
		String url = "http://"+ restcloudUrl + ":" + restcloudPort + "/restcloud/push/push_data";
		HttpResponse response = HttpRequest.post(url)
                .header("Content-Type", "application/json")
                .body(JSON.toJSONString(map))
                .timeout(60000)
                .execute();
		if (200 != response.getStatus()) {
			throw new ServiceException("调用谷云" + url + "返回状态码为：" + response.getStatus());
		}else {
			String body = response.body();
			JSONObject responseJson = JSON.parseObject(body);
			Integer resultCode = responseJson.getInteger("resultCode");
            // 判断结果异常:ETLProcessRunResultCode
            if (null == resultCode || 1 != resultCode) {
            	throw new ServiceException("调用谷云" + url + "返回报文为：" + body);
            }
		}
		return true;
	}
}
