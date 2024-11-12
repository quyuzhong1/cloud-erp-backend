package com.erp.server.dmp.controller.feign;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.DmpSyncTaskDTO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.DmpInoutDTO;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputHotfixCreateRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputFinishResponse;
import com.erp.server.dmp.inout.handler.factory.DmpInputCreateFactory;
import com.erp.server.dmp.inout.utils.DmpOutputUtils;
import com.erp.server.dmp.service.DmpCfgInputDetailService;
import com.erp.server.dmp.service.DmpCfgInputService;
import com.erp.server.dmp.service.DmpInputTaskService;
import com.erp.server.dmp.service.DmpOutputTaskRecordService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
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
	 * 公共-创建快速输入任务
	 */
	@PostMapping("/doHotfixInputTask")
	public Boolean doInputTask(@RequestBody DmpInoutDTO.CreateInputDTO createDTO) {
		//查询任务是否存在
		List<DmpInoutDTO.ListDTO> list =  dmpCfgInputDetailService.listBySystemCodeAndBillType(
                Collections.singletonList(createDTO.getSystemCode()),
                Collections.singletonList(createDTO.getBillType()),
				createDTO.getNextLevelIdList());
		if (CollectionUtils.isEmpty(list)){
			ServiceException.runError("任务不存在:系统={},业务={},nextLevelId={}",
					createDTO.getSystemCode(),
					createDTO.getBillType(),
					createDTO.getNextLevelIdList());
		}
		Map<String, List<DmpInoutDTO.ListDTO>> groupTaskList = list.stream()
				.collect(Collectors.groupingBy(item -> StrUtil.format("{}_{}", item.getSystemCode(), item.getCfgInputId())));

		List<DmpInputFinishResponse> resultList = new LinkedList<>();
		for (Map.Entry<String, List<DmpInoutDTO.ListDTO>> entry : groupTaskList.entrySet()) {
			DmpInoutDTO.ListDTO listDTO = entry.getValue().stream().findFirst().orElse(new DmpInoutDTO.ListDTO());

			List<String> inputDetailIds = entry.getValue().stream().map(DmpInoutDTO.ListDTO::getDetailId).distinct().collect(Collectors.toList());
			// 创建新中台hotfix任务
			DmpInputHotfixCreateRequest dmpInputHotfixCreateRequest = new DmpInputHotfixCreateRequest();
			dmpInputHotfixCreateRequest.setCfgInputDetailIdList(inputDetailIds);
			dmpInputHotfixCreateRequest.setCfgInputId(listDTO.getCfgInputId());
			dmpInputHotfixCreateRequest.setDetailExtendJson(createDTO.getDetailExtendJson());
			// 拉取当天
			LocalDate today = LocalDate.now();
			dmpInputHotfixCreateRequest.setStartTime(LocalDateTime.of(LocalDate.now(), LocalTime.MIN));
			dmpInputHotfixCreateRequest.setEndTime(today.atTime(LocalTime.now()));
			List<DmpInputFinishResponse> dmpInputResponses = dmpInputCreateFactory.doHotfixInputTask(dmpInputHotfixCreateRequest);
			resultList.addAll(dmpInputResponses);
		}
		return true;
	}


	/**
	 * 公共- 查询输入任务最新状态
	 */
	@PostMapping("/newInputTaskList")
	public List<DmpInoutDTO.LastOneDTO> doInputTask(@RequestBody DmpInoutDTO.CommonDTO createDTO) {
		//查询任务是否存在
		List<DmpInoutDTO.LastOneDTO> list =  dmpInputTaskService.lastBySystemCodeAndBillType(
				Collections.singletonList(createDTO.getSystemCode()),
				Collections.singletonList(createDTO.getBillType()),
				createDTO.getNextLevelIdList());
		if (CollectionUtils.isEmpty(list)){
			return Collections.emptyList();
		}
		// 转换对应信息
		return list;
	}
}
