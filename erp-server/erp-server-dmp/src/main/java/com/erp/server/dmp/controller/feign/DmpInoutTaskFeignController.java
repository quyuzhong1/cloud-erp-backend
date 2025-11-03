package com.erp.server.dmp.controller.feign;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncTaskDTO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.DmpInoutDTO;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.model.dmp.enums.DmpCfgInputExecSystemEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputFinishRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputHotfixCreateRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputCreateResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputFinishResponse;
import com.erp.server.dmp.inout.handler.factory.DmpInputCreateFactory;
import com.erp.server.dmp.inout.handler.factory.DmpInputTaskFactory;
import com.erp.server.dmp.inout.utils.DmpOutputUtils;
import com.erp.server.dmp.service.CfgSettingService;
import com.erp.server.dmp.service.DmpCfgInputDetailService;
import com.erp.server.dmp.service.DmpInputTaskService;
import com.erp.server.dmp.service.DmpOutputTaskRecordService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/feign/inout")
public class DmpInoutTaskFeignController{
	
	@Resource
    private DmpOutputUtils dmpOutputUtils;
	@Resource
	private DmpOutputTaskRecordService dmpOutputTaskRecordService;
	@Resource
	private DmpInputCreateFactory dmpInputCreateFactory;
	@Resource
	private DmpCfgInputDetailService dmpCfgInputDetailService;
	@Resource
	private DmpInputTaskService dmpInputTaskService;
	@Autowired
	@Qualifier("dmpInputExecutorPool")
	private ExecutorService dmpInputExecutorPool;
	@Resource
	private DmpInputTaskFactory dmpInputTaskFactory;
	@Resource
	private CfgSettingService cfgSettingService;

	/**
	 * @param updateDTO
	 */
	@PostMapping("/updateOutputTaskRecord")
    public ApiResult<Boolean> updateOutputTaskRecord(@RequestBody DmpOutputTaskRecordDTO.UpdateDTO updateDTO) {
		return ApiResult.success(dmpOutputUtils.updateStatus(updateDTO.getId(), updateDTO.getStatus(), updateDTO.getResponseData() , updateDTO.getMessage()));
	}

	/**
	 * 查询同步数据
	 * @author will
	 * @date 2024/10/21 18:44
	 * @param oneDTO
	 * @return SyncInfoDTO
	 */
	@PostMapping("/getErrorData")
	public DmpPushTaskDTO.SyncInfoDTO getErrorData(@RequestBody DmpSyncTaskDTO.OneDTO oneDTO) {
		return dmpOutputTaskRecordService.getErrorData(oneDTO);
	}

	/**
	 * 获取最后一条拉取记录
	 * @author will
	 * @date 2025/8/27 18:24
	 * @param paramDTO
	 * @return LastPullDTO
	 */
	@PostMapping("/getLastPullRecord")
	public DmpPushTaskDTO.LastPullDTO getLastPullRecord(@RequestBody DmpPushTaskDTO.LastPullParamDTO paramDTO) {
		return dmpOutputTaskRecordService.getLastPullRecord(paramDTO);
	}


	/**
	 * 公共-创建快速输入任务
	 */
	@PostMapping("/doHotfixInputTask")
	public Boolean doInputTask(@RequestBody List<DmpInoutDTO.CreateInputDTO> createDTOList) {
		List<String> systemCodeList = createDTOList.stream().map(e -> e.getSystemCode().toLowerCase()).distinct().collect(Collectors.toList());
		List<String> billTypeList = createDTOList.stream().map(DmpInoutDTO.CommonDTO::getBillType).distinct().collect(Collectors.toList());
		List<String> nextLevelIdList = createDTOList.stream().map(DmpInoutDTO.CommonDTO::getNextLevelId).distinct().collect(Collectors.toList());
		//查询任务是否存在
		List<DmpInoutDTO.ListDTO> list =  dmpCfgInputDetailService.listBySystemCodeAndBillType(
				systemCodeList,
                billTypeList,
				nextLevelIdList);
		if (CollectionUtils.isEmpty(list)){
			ServiceException.runError("任务不存在");
		}
		for (DmpInoutDTO.CreateInputDTO createDTO : createDTOList) {
			DmpInoutDTO.ListDTO listDTO = list.stream().filter(e -> e.getSystemCode().equalsIgnoreCase(createDTO.getSystemCode())
					&& e.getBillType().equalsIgnoreCase(createDTO.getBillType())
					&& e.getNextLevelId().equalsIgnoreCase(createDTO.getNextLevelId())
			).findFirst().orElse(null);
			if (null == listDTO){
				ServiceException.runError("任务不存在:{}", JSONUtil.toJsonStr(createDTO));
			}
			// 创建新中台
			DmpInputHotfixCreateRequest dmpInputCreateRequest = new DmpInputHotfixCreateRequest();
			dmpInputCreateRequest.setCfgInputDetailIdList(Collections.singletonList(listDTO.getDetailId()));
			dmpInputCreateRequest.setCfgInputId(listDTO.getCfgInputId());
			dmpInputCreateRequest.setDetailExtendJson(createDTO.getDetailExtendJson());
			// 拉取时间
			dmpInputCreateRequest.setStartTime(createDTO.checkAndGetStartTime());
			dmpInputCreateRequest.setEndTime(createDTO.checkAndGetEndTime());
			dmpInputCreateRequest.setTaskType(createDTO.getTaskType());
			// 创建任务
			DmpInputCreateResponse response = dmpInputCreateFactory.createHotfixInputTask(dmpInputCreateRequest);
			// 执行任务
			if(CollectionUtils.isNotEmpty(response.getAfterDmpInputTaskEntityList())) {
				for (DmpInputTaskEntity dmpInputTaskEntity : response.getAfterDmpInputTaskEntityList()) {
					//系统非dmp不立即执行，存在restcloud
					if (!CharSequenceUtil.equals(dmpInputTaskEntity.getExecSystem(), DmpCfgInputExecSystemEnum.DMP.getCode())) {
						continue;
					}
					dmpInputExecutorPool.execute(() -> {
						DmpInputFinishRequest dmpInputFinishRequest = new DmpInputFinishRequest();
						dmpInputFinishRequest.setInputTaskId(dmpInputTaskEntity.getId());
						dmpInputFinishRequest.setExecTimeout(dmpInputTaskEntity.getExecTimeout());
						dmpInputTaskFactory.dealInputTask(dmpInputFinishRequest);
					});
				}
			}

		}
		return true;
	}

	/**
	 * 公共-创建快速输入任务(存在返回值)
	 */
	@PostMapping("/doHotfixReturnInputTask")
	public List<String> doHotfixReturnInputTask(@RequestBody List<DmpInoutDTO.CreateInputDTO> createDTOList) {
		List<String> systemCodeList = createDTOList.stream().map(e -> e.getSystemCode().toLowerCase()).distinct().collect(Collectors.toList());
		List<String> billTypeList = createDTOList.stream().map(DmpInoutDTO.CommonDTO::getBillType).distinct().collect(Collectors.toList());
		List<String> nextLevelIdList = createDTOList.stream().map(DmpInoutDTO.CommonDTO::getNextLevelId).distinct().collect(Collectors.toList());
		//查询任务是否存在
		List<DmpInoutDTO.ListDTO> list =  dmpCfgInputDetailService.listBySystemCodeAndBillType(
				systemCodeList,
				billTypeList,
				nextLevelIdList);
		if (CollectionUtils.isEmpty(list)){
			ServiceException.runError("任务不存在");
		}
		// 准备结果列表
		List<String> responseList = new ArrayList<>();
		for (DmpInoutDTO.CreateInputDTO createDTO : createDTOList) {
			DmpInoutDTO.ListDTO listDTO = list.stream().filter(e -> e.getSystemCode().equalsIgnoreCase(createDTO.getSystemCode())
					&& e.getBillType().equalsIgnoreCase(createDTO.getBillType())
					&& e.getNextLevelId().equalsIgnoreCase(createDTO.getNextLevelId())
			).findFirst().orElse(null);
			if (null == listDTO){
				ServiceException.runError("任务不存在:{}", JSONUtil.toJsonStr(createDTO));
			}
			// 创建新中台
			DmpInputHotfixCreateRequest dmpInputCreateRequest = new DmpInputHotfixCreateRequest();
			dmpInputCreateRequest.setCfgInputDetailIdList(Collections.singletonList(listDTO.getDetailId()));
			dmpInputCreateRequest.setCfgInputId(listDTO.getCfgInputId());
			dmpInputCreateRequest.setDetailExtendJson(createDTO.getDetailExtendJson());
			// 拉取时间
			dmpInputCreateRequest.setStartTime(createDTO.checkAndGetStartTime());
			dmpInputCreateRequest.setEndTime(createDTO.checkAndGetEndTime());
			dmpInputCreateRequest.setTaskType(createDTO.getTaskType());
			// 创建任务
			DmpInputCreateResponse response = dmpInputCreateFactory.createHotfixInputTask(dmpInputCreateRequest);
			// 执行任务
			if(CollectionUtils.isNotEmpty(response.getAfterDmpInputTaskEntityList())) {
				for (DmpInputTaskEntity dmpInputTaskEntity : response.getAfterDmpInputTaskEntityList()) {
					DmpInputFinishRequest dmpInputFinishRequest = new DmpInputFinishRequest();
					dmpInputFinishRequest.setInputTaskId(dmpInputTaskEntity.getId());
					dmpInputFinishRequest.setExecTimeout(dmpInputTaskEntity.getExecTimeout());
					DmpInputFinishResponse dmpInputFinishResponse = dmpInputTaskFactory.dealInputTask(dmpInputFinishRequest);
					Map<DmpCfgInputConvertEntity, List<DmpInputTaskInitDTO>> convertMap = dmpInputFinishResponse.getConvertInputTaskInitDTOListMaps();
					if (ObjUtil.isEmpty(convertMap)) {
						break;
					}
					for (Map.Entry<DmpCfgInputConvertEntity, List<DmpInputTaskInitDTO>> entry : convertMap.entrySet()) {
						entry.getValue().stream().filter(obj -> obj.getCode().equals(200)).map(DmpInputTaskInitDTO::getMsg).forEach(responseList::add);
					}
				}
			}
		}
		return  responseList;
	}

	/**
	 * 公共-查询输入任务最新状态
	 */
	@PostMapping("/newInputTaskList")
	public List<DmpInoutDTO.LastOneDTO> newInputTaskList(@RequestBody List<DmpInoutDTO.CommonDTO> commonDTOList) {
		List<String> systemCodeList = commonDTOList.stream().map(e -> e.getSystemCode().toLowerCase()).distinct().collect(Collectors.toList());
		List<String> billTypeList = commonDTOList.stream().map(DmpInoutDTO.CommonDTO::getBillType).distinct().collect(Collectors.toList());
		List<String> nextLevelIdList = commonDTOList.stream().map(DmpInoutDTO.CommonDTO::getNextLevelId).distinct().collect(Collectors.toList());

		//查询任务是否存在
		List<DmpInoutDTO.LastOneDTO> list =  dmpInputTaskService.lastBySystemCodeAndBillType(
				systemCodeList,
				billTypeList,
				nextLevelIdList);
		// 转换对应信息
		return commonDTOList.stream()
				.map(e -> DmpInoutDTO.LastOneDTO.init(list, e.getSystemCode(), e.getBillType(), e.getNextLevelId()))
				.collect(Collectors.toList());
	}


	/**
	 * 公共-查询输入任务明细记录
	 */
	@PostMapping("/inputDetailList")
	public List<DmpInoutDTO.ListDTO> inputDetailList(@RequestBody List<DmpInoutDTO.CommonDTO> commonDTOList) {
		List<String> systemCodeList = commonDTOList.stream().map(e -> e.getSystemCode().toLowerCase()).distinct().collect(Collectors.toList());
		List<String> billTypeList = commonDTOList.stream().map(DmpInoutDTO.CommonDTO::getBillType).distinct().collect(Collectors.toList());
		List<String> nextLevelIdList = commonDTOList.stream().map(DmpInoutDTO.CommonDTO::getNextLevelId).distinct().collect(Collectors.toList());
		//查询任务是否存在
		return dmpCfgInputDetailService.listBySystemCodeAndBillType(
				systemCodeList,
				billTypeList,
				nextLevelIdList);
	}

	/**
	 * 更新任务
	 */
	@PostMapping("/updateDmpOutputTaskRecordEntity")
	public Boolean updateDmpOutputTaskRecordEntity(@RequestBody List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList){
		if(CollectionUtils.isEmpty(dmpOutputTaskRecordEntityList)){
			return null;
		}
		return dmpOutputTaskRecordService.updateBatchById(dmpOutputTaskRecordEntityList);
	}

}
