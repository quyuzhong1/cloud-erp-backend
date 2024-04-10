package com.erp.server.wms.service.impl;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.WmsDataComparePlanDTO;
import com.erp.model.wms.dto.WmsDataComparePlanDTO.ImportDataMappingDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO.CreateViewDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO.DataCompareDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO.PagingParamDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO.SetNextDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO.SetNextViewDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO.ViewDTO;
import com.erp.model.wms.entity.DictBasicEntity;
import com.erp.model.wms.entity.WmsDataCompareImportEntity;
import com.erp.model.wms.entity.WmsDataCompareTaskEntity;
import com.erp.model.wms.entity.WmsDataCompareTempEntity;
import com.erp.model.wms.enums.WmsDataCompareImportParseStatusEnum;
import com.erp.model.wms.enums.WmsDataCompareTaskBillTypeEnum;
import com.erp.model.wms.enums.WmsDataCompareTaskStatusEnum;
import com.erp.model.wms.enums.WmsDataCompareTaskSubStatusEnum;
import com.erp.model.wms.enums.WmsDataCompareTempCompareResultEnum;
import com.erp.model.wms.enums.WmsDataCompareTempCompareStatusEnum;
import com.erp.model.wms.enums.WmsDataCompareTempMainDataTypeEnum;
import com.erp.server.wms.config.WmsDataCompareHandlerFactory;
import com.erp.server.wms.mapper.WmsDataCompareTaskMapper;
import com.erp.server.wms.service.CommonService;
import com.erp.server.wms.service.DictBasicService;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.WmsDataCompareBillService;
import com.erp.server.wms.service.WmsDataCompareImportService;
import com.erp.server.wms.service.WmsDataCompareTaskService;
import com.erp.server.wms.service.WmsDataCompareTempService;
import com.erp.server.wms.utils.WmsDataCompareUtils;
import com.erp.server.wms.utils.WmsDataCompareUtils.MergeStrategy;
import com.erp.server.wms.utils.WmsDataCompareUtils.WmsDataCompareExcelDto;
import com.google.common.collect.Lists;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 数据对比任务 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-03-20
 */
@Slf4j
@Service
public class WmsDataCompareTaskServiceImpl extends SuperServiceImpl<WmsDataCompareTaskMapper, WmsDataCompareTaskEntity> implements WmsDataCompareTaskService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WmsDataCompareImportService wmsDataCompareImportService;
    @Autowired
    private WmsDataCompareHandlerFactory wmsDataCompareHandlerFactory;
    @Autowired
    private DictBasicService dictBasicService;
    @Autowired
    @Qualifier("wmsDataCompareExecutorPool")
    private ExecutorService wmsDataCompareExecutorPool;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Autowired
    private WmsDataCompareTempService wmsDataCompareTempService;
    @Autowired
    private WmsDataCompareTaskService wmsDataCompareTaskService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public WmsDataCompareTaskDTO.AddViewDTO add(WmsDataCompareTaskDTO.AddDTO addDTO) {
        WmsDataCompareTaskEntity wmsDataCompareTaskEntity = new WmsDataCompareTaskEntity();
        BeanMapperUtils.copy(addDTO, wmsDataCompareTaskEntity);
        
        List<String> excelFiles = addDTO.getExcelFiles();
		if(CollUtil.isEmpty(excelFiles)) {
			throw new ServiceException("对比数据导入文件为空");
        }
        // 数据处理
        handleData(wmsDataCompareTaskEntity);

        List<WmsDataCompareImportEntity> wmsDataCompareImportEntityList = new ArrayList<>(excelFiles.size());
        WmsDataCompareImportEntity wmsDataCompareImportEntity = null;
        
        for(String excelFile : excelFiles) {
        	wmsDataCompareImportEntity = new WmsDataCompareImportEntity();
        	wmsDataCompareImportEntity.setFileUrl(excelFile);
        	wmsDataCompareImportEntity.setParseStatus(WmsDataCompareImportParseStatusEnum.WAIT.getCode());
        	wmsDataCompareImportEntity.setCurrParseOffset(0);
        	wmsDataCompareImportEntityList.add(wmsDataCompareImportEntity);
        }
        WmsDataCompareExcelDto wmsDataCompareExcelDto = WmsDataCompareUtils.getWmsDataCompareExcelDto(excelFiles , false);
        List<List<String>> headFieldLists = wmsDataCompareExcelDto.getHeadFieldLists();
        if(CollUtil.isEmpty(headFieldLists) || wmsDataCompareExcelDto.getImportDataCount() == 0) {
        	throw new ServiceException("所有导入文件行数都为空，请检查");
        }
        
        if(!WmsDataCompareUtils.areAllListsEqual(headFieldLists)) {
        	throw new ServiceException("导入的多个文件表头不一致，请检查");
        }
        
		wmsDataCompareTaskEntity.setImportDataCount(wmsDataCompareExcelDto.getImportDataCount());
        
        String billType = wmsDataCompareTaskEntity.getBillType();
        String systemDataCondition = wmsDataCompareTaskEntity.getSystemDataCondition();
        
        wmsDataCompareTaskEntity.setSystemDataCount(wmsDataCompareHandlerFactory.get(billType).getSystemDataCount(systemDataCondition));
        
        log.info("开始新增数据对比任务");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_SJDB);
        wmsDataCompareTaskEntity.setCode(code);
        boolean save = super.save(wmsDataCompareTaskEntity);
        
        if(!save) {
            throw new ServiceException("数据对比任务保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "数据对比任务" , wmsDataCompareTaskEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DATA_COMPARE.getCode(), wmsDataCompareTaskEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        wmsDataCompareImportEntityList.forEach(w -> {
        	w.setTaskId(wmsDataCompareTaskEntity.getId());
        });
        wmsDataCompareImportService.saveBatch(wmsDataCompareImportEntityList);
        
        WmsDataCompareTaskDTO.AddViewDTO viewDTO = new WmsDataCompareTaskDTO.AddViewDTO();
        BeanMapperUtils.copy(wmsDataCompareTaskEntity, viewDTO);
        viewDTO.setImportDataFields(headFieldLists.get(0));
        int size = excelFiles.size();
        if(size > 1) {
        	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        	ZipOutputStream zipOutputStream = new ZipOutputStream(baos);
        	InputStream[] ins = new InputStream[size];
        	for(int i = 0; i < size ; i++) {
        		InputStream inputStream = null;
    			try {
    				inputStream = FastDFSClientUtil.getInputStream(excelFiles.get(i));
    			} catch (Exception e) {
    				log.error("获取文件失败" , e);
    				throw new ServiceException("获取文件失败");
    			}
    			ins[i] = inputStream;
        	}
        	ZipUtil.zip(zipOutputStream, excelFiles.toArray(new String[] {}), ins);
        	excelFiles = new ArrayList<>();
        	excelFiles.add(FastDFSClientUtil.uploadFile(baos.toByteArray(), UUID.randomUUID().toString() + ".zip", null));
        }
        viewDTO.setImportFileUrls(excelFiles);
        return viewDTO;
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(WmsDataCompareTaskDTO.UpdateDTO updateDTO) {
        WmsDataCompareTaskEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "数据对比任务"));
        WmsDataCompareTaskEntity wmsDataCompareTaskEntity =  BeanMapperUtils.map(WmsDataCompareTaskEntity.class, updateDTO);

        // 数据处理
        handleData(wmsDataCompareTaskEntity);
        log.info("编辑 开始修改数据对比任务数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(wmsDataCompareTaskEntity);
        if(!save) {
            throw new ServiceException("数据对比任务保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录数据对比任务日志数据，单号：【{}】", wmsDataCompareTaskEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), wmsDataCompareTaskEntity.getCode(), "数据对比任务");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, wmsDataCompareTaskEntity, null, wmsDataCompareTaskEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(WmsDataCompareTaskEntity wmsDataCompareTaskEntity) {
    // TODO 验证数据 & 数据赋值
    	if(StringUtils.isBlank(wmsDataCompareTaskEntity.getName())) {
    		throw new ServiceException("对比任务名称不能为空");
    	}
    	if(StringUtils.isBlank(wmsDataCompareTaskEntity.getBillType())) {
    		throw new ServiceException("系统单据不能为空");
    	}
    	if(StringUtils.isBlank(wmsDataCompareTaskEntity.getSystemDataCondition())) {
    		throw new ServiceException("系统数据范围不能为空");
    	}
    	
    	wmsDataCompareTaskEntity.setStatus("init");
    }

	@Override
	public String downloadSystemData(BaseIdDTO dto) {
		WmsDataCompareTaskEntity wmsDataCompareTaskEntity = this.getById(dto.getId());
		if(wmsDataCompareTaskEntity == null) {
			throw new ServiceException("任务id不存在");
        }
		String billType = wmsDataCompareTaskEntity.getBillType();
		String systemDataCondition = wmsDataCompareTaskEntity.getSystemDataCondition();
		
		return wmsDataCompareHandlerFactory.get(billType).uploadSystemDataByCondition(systemDataCondition);
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public SetNextViewDTO setNext(SetNextDTO dto) {
		SetNextViewDTO setNextViewDTO = new SetNextViewDTO();
		String importDataMapping = dto.getImportDataMapping();
		List<ImportDataMappingDTO> importDataMappingDTOList = null;
		try {
			importDataMappingDTOList = JSON.parseArray(importDataMapping , WmsDataComparePlanDTO.ImportDataMappingDTO.class);
		} catch (Exception e) {
			setNextViewDTO.setErrMessageList(Collections.singletonList("导入数据字段映射json串格式错误"));
			return setNextViewDTO;
		}
		
		if(CollUtil.isNotEmpty(importDataMappingDTOList)) {
			if(importDataMappingDTOList.stream().anyMatch(i -> i.getPkFlag() != null && i.getPkFlag() && (StringUtils.isBlank(i.getImportField()) || StringUtils.isBlank(i.getImportField())))) {
				setNextViewDTO.setErrMessageList(Collections.singletonList("唯一键已设置，但系统数据字段或导入数据字段属性未设置映射"));
				return setNextViewDTO;
			}
			String id = dto.getId();
			List<WmsDataCompareImportEntity> wmsDataCompareImportEntityList = wmsDataCompareImportService.list(Wrappers.<WmsDataCompareImportEntity>lambdaQuery()
					.eq(WmsDataCompareImportEntity::getTaskId, id));
			if(CollUtil.isNotEmpty(wmsDataCompareImportEntityList)) {
				WmsDataCompareTaskEntity wmsDataCompareTaskEntity = getById(id);
				
				List<String> headFieldList = null;
				
				Map<String, List<List<String>>> allDatasMap = new HashMap<>();
				
				for(WmsDataCompareImportEntity wmsDataCompareImportEntity : wmsDataCompareImportEntityList) {
					WmsDataCompareExcelDto wmsDataCompareExcelDto = WmsDataCompareUtils.getWmsDataCompareExcelDto(Arrays.asList(wmsDataCompareImportEntity.getFileUrl()) , true);
					List<List<String>> headFieldLists = wmsDataCompareExcelDto.getHeadFieldLists();
					if(CollUtil.isEmpty(headFieldLists)) {
						wmsDataCompareImportEntity.setParseStatus(WmsDataCompareImportParseStatusEnum.FINISH.getCode());
						continue;
					}
					List<List<String>> datas = wmsDataCompareExcelDto.getDatas();
					if(CollUtil.isNotEmpty(datas)) {
						allDatasMap.put(wmsDataCompareImportEntity.getId(), datas);
					}else {
						wmsDataCompareImportEntity.setParseStatus(WmsDataCompareImportParseStatusEnum.FINISH.getCode());
					}
					headFieldList = headFieldLists.get(0);
				}
				
				WmsDataCompareUtils.compareExcelIndexList(importDataMappingDTOList, headFieldList);
				importDataMapping = JSON.toJSONString(importDataMappingDTOList);
				
				Map<String, Integer> pkValueSameCountMaps = new HashMap<>();
				List<Integer> excelPkIndexList = importDataMappingDTOList.stream().filter(i -> i.getPkFlag() != null && i.getPkFlag()).map(ImportDataMappingDTO::getHeadIndex).collect(Collectors.toList());
				for(Map.Entry<String, List<List<String>>> allDatas : allDatasMap.entrySet()) {
					for(List<String> data : allDatas.getValue()) {
						String pkValue = excelPkIndexList.stream().map(index -> {
							String d = data.get(index);
							if(d == null) {
								d = "";
							}
							return d;
						}).collect(Collectors.joining("-"));
						Integer sameCount = 1;
						if(pkValueSameCountMaps.containsKey(pkValue)) {
							sameCount = pkValueSameCountMaps.get(pkValue) + 1;
						}
						pkValueSameCountMaps.put(pkValue, sameCount);
					}
				}
				
				List<String> errMessageList = new ArrayList<>();
				if(pkValueSameCountMaps.size() > 0) {
					for(Map.Entry<String, Integer> pkValueSameCountMap : pkValueSameCountMaps.entrySet()) {
						Integer value = pkValueSameCountMap.getValue();
						if(value > 1) {
							StringBuffer sb = new StringBuffer();
							sb.append("唯一键值[");
							sb.append(pkValueSameCountMap.getKey());
							sb.append("]，重复[");
							sb.append(value);
							sb.append("]行");
							errMessageList.add(sb.toString());
						}
					}
				}
				if(CollUtil.isNotEmpty(errMessageList)) {
					setNextViewDTO.setErrMessageList(errMessageList);
					return setNextViewDTO;
				}
				
				setNextViewDTO.setFlag(true);
				setNextViewDTO.setCode(wmsDataCompareTaskEntity.getCode());
				setNextViewDTO.setName(wmsDataCompareTaskEntity.getName());
				
				this.update(Wrappers.<WmsDataCompareTaskEntity>lambdaUpdate().eq(WmsDataCompareTaskEntity::getId, id)
						.eq(WmsDataCompareTaskEntity::getStatus, "init")
						.set(WmsDataCompareTaskEntity::getImportDataMapping, importDataMapping));
				wmsDataCompareImportService.updateBatchById(wmsDataCompareImportEntityList);
				
			}else {
				setNextViewDTO.setErrMessageList(Collections.singletonList("未查询到上传文件，请重新导入数据"));
			}
		}else {
			setNextViewDTO.setErrMessageList(Collections.singletonList("唯一键设置至少开启一个才能创建任务"));
		}
		
		return setNextViewDTO;
	}

	@Override
	public CreateViewDTO create(BaseIdDTO dto) {
		String id = dto.getId();
		WmsDataCompareTaskEntity wmsDataCompareTaskEntity = this.getById(id);
		if(wmsDataCompareTaskEntity == null) {
			throw new ServiceException("任务id不存在");
        }
		this.update(Wrappers.<WmsDataCompareTaskEntity>lambdaUpdate().eq(WmsDataCompareTaskEntity::getId, id)
				.eq(WmsDataCompareTaskEntity::getStatus, "init")
				.set(WmsDataCompareTaskEntity::getStatus, WmsDataCompareTaskStatusEnum.DOING.getCode())
				.set(WmsDataCompareTaskEntity::getSubStatus, WmsDataCompareTaskSubStatusEnum.WAIT_PARSE.getCode()));
		List<WmsDataCompareTaskEntity> list = wmsDataCompareTaskService.lambdaQuery()
				.eq(WmsDataCompareTaskEntity::getId, id)
        		.eq(WmsDataCompareTaskEntity::getStatus, WmsDataCompareTaskStatusEnum.DOING.getCode()).select(WmsDataCompareTaskEntity::getId).list();
		if(CollUtil.isNotEmpty(list)) {
			wmsDataCompareExecutorPool.submit(() -> dealParseTask(id));
		}
		CreateViewDTO createViewDTO = new CreateViewDTO();
		createViewDTO.setCode(wmsDataCompareTaskEntity.getCode());
		createViewDTO.setName(wmsDataCompareTaskEntity.getName());
		return createViewDTO;
	}
	
	@Override
	public void dealParseTask(String id) {
		String redisKey = "datacompare:task:" + id;
		try {
			if(redisTemplate.opsForValue().setIfAbsent(redisKey, DateUtil.now(), 30, TimeUnit.MINUTES)) {
				this.parseExcelData(id);
				this.compareSystemImportData(id);
				this.dealUploadResultExcel(id);
			}else {
				log.info("数据对比任务id={}正在处理中" , id);
			}
		} catch (Exception e) {
			log.error("数据对比任务id={}处理失败" , id , e);
			WmsDataCompareTaskEntity wmsDataCompareTaskEntity = getById(id);
			Integer errorCount = wmsDataCompareTaskEntity.getErrorCount();
			if(errorCount == null) {
				errorCount = 0;
			}
			errorCount = errorCount + 1;
			update(Wrappers.<WmsDataCompareTaskEntity>lambdaUpdate().set(WmsDataCompareTaskEntity::getErrorCount, errorCount)
					.set(WmsDataCompareTaskEntity::getErrorMessage, ExceptionUtil.stacktraceToString(e, 2000))
					.set(errorCount == 3 , WmsDataCompareTaskEntity::getStatus, WmsDataCompareTaskStatusEnum.ERROR.getCode())
					.eq(WmsDataCompareTaskEntity::getId, id));
			if(errorCount >= 3) {
				String body = "{\r\n" + 
						"    \"msg_type\": \"text\",\r\n" + 
						"    \"content\": {\r\n" + 
						"        \"text\": \"%s\"\r\n" + 
						"    }\r\n" + 
						"}";
				if(BusinessCommonConstants.hasProfile("prod")) {
					body = String.format(body, "数据对比生产环境告警：" + "任务id=" + id + "处理失败");
				}else {
					body = String.format(body, "数据对比测试环境告警：" + "任务id=" + id + "处理失败");
				}
				HttpUtil.post("https://open.feishu.cn/open-apis/bot/v2/hook/c76b72f8-0bf9-4967-a9ce-0728767c1ccc", body);
			}
		}finally {
			redisTemplate.delete(redisKey);
		}
	}
	
	private void parseExcelData(String id) throws Exception {
		WmsDataCompareTaskEntity wmsDataCompareTaskEntity = getById(id);
		if(WmsDataCompareTaskSubStatusEnum.WAIT_PARSE.getCode().equals(wmsDataCompareTaskEntity.getSubStatus())) {
			Map<String, List<List<String>>> allDatasMap = new HashMap<>();
			List<WmsDataCompareImportEntity> wmsDataCompareImportEntityList = wmsDataCompareImportService
					.lambdaQuery().eq(WmsDataCompareImportEntity::getTaskId, id)
					.eq(WmsDataCompareImportEntity::getParseStatus, WmsDataCompareImportParseStatusEnum.WAIT.getCode()).list();
			for(WmsDataCompareImportEntity wmsDataCompareImportEntity : wmsDataCompareImportEntityList) {
				WmsDataCompareExcelDto wmsDataCompareExcelDto = WmsDataCompareUtils.getWmsDataCompareExcelDto(Arrays.asList(wmsDataCompareImportEntity.getFileUrl()) , true);
				List<List<String>> headFieldLists = wmsDataCompareExcelDto.getHeadFieldLists();
				if(CollUtil.isEmpty(headFieldLists)) {
					continue;
				}
				List<List<String>> datas = wmsDataCompareExcelDto.getDatas();
				if(CollUtil.isNotEmpty(datas)) {
					allDatasMap.put(wmsDataCompareImportEntity.getId(), datas);
				}
			}
			List<ImportDataMappingDTO> importDataMappingDTOList = JSON.parseArray(wmsDataCompareTaskEntity.getImportDataMapping() , WmsDataComparePlanDTO.ImportDataMappingDTO.class);
			
			WmsDataCompareBillService<?> wmsDataCompareBillService = wmsDataCompareHandlerFactory.get(wmsDataCompareTaskEntity.getBillType());
			for(Map.Entry<String, List<List<String>>> allDatas : allDatasMap.entrySet()) {
				String importId = allDatas.getKey();
				List<List<String>> value = allDatas.getValue();
				WmsDataCompareImportEntity wmsDataCompareImportEntity = wmsDataCompareImportService.getById(importId);
				Integer currParseOffset = wmsDataCompareImportEntity.getCurrParseOffset();
				if(currParseOffset == value.size()) {
					wmsDataCompareImportService.update(Wrappers.<WmsDataCompareImportEntity>lambdaUpdate().eq(WmsDataCompareImportEntity::getId, importId)
							.eq(WmsDataCompareImportEntity::getParseStatus, WmsDataCompareImportParseStatusEnum.WAIT.getCode())
							.set(WmsDataCompareImportEntity::getParseStatus, WmsDataCompareImportParseStatusEnum.FINISH.getCode()));
				}else {
					value = value.subList(currParseOffset, value.size());
					List<List<List<String>>> partitionList = Lists.partition(value, 1000);
					int i = 1;
					for(List<List<String>> partition : partitionList) {
						List<WmsDataCompareTempEntity> wmsDataCompareTempEntityList = new ArrayList<>(partition.size());
						WmsDataCompareTempEntity wmsDataCompareTempEntity = null;
						for(List<String> p : partition) {
							wmsDataCompareTempEntity = new WmsDataCompareTempEntity();
							wmsDataCompareTempEntity.setTaskId(id);
							wmsDataCompareTempEntity.setMainDataType(WmsDataCompareTempMainDataTypeEnum.IMPORT.getCode());
							wmsDataCompareTempEntity.setCompareStatus(WmsDataCompareTempCompareStatusEnum.WAIT.getCode());
							DataCompareDTO compareDTO = wmsDataCompareBillService.getCompareDTO(null);
							StringBuffer sb = new StringBuffer();
							for(ImportDataMappingDTO importDataMappingDTO : importDataMappingDTOList) {
								Integer headIndex = importDataMappingDTO.getHeadIndex();
								if(headIndex != null) {
									String systemField = importDataMappingDTO.getSystemField();
									Method method = compareDTO.getClass().getMethod("set" + StringUtils.capitalize(systemField) , String.class);
									String excelValue = p.get(headIndex);
									if(excelValue == null) {
										excelValue = "";
									}
									if(StringUtils.isNotBlank(excelValue)) {
										if(systemField.endsWith("Date") || systemField.endsWith("Time")) {
											DateTime parse = null;
											try {
												parse = DateUtil.parse(excelValue);
											} catch (Exception e) {
												try {
													parse = DateUtil.parse(excelValue, "MM/dd/yyyy");
												} catch (Exception e1) {
													throw new ServiceException("日期格式解析错误" + ExceptionUtil.stacktraceToString(e1, 1500));
												}
											}
											excelValue = DateUtil.format(parse, "yyyy-MM-dd");
										}
									}
									method.invoke(compareDTO, excelValue);
									if(importDataMappingDTO.getPkFlag() != null && importDataMappingDTO.getPkFlag()) {
										sb.append("-");
										sb.append(excelValue);
									}
								}
							}
							wmsDataCompareTempEntity.setImportDataJson(JSON.toJSONString(compareDTO));
							wmsDataCompareTempEntity.setPkFieldValue(sb.length() > 0 ? sb.toString().substring(1) : "");
							wmsDataCompareTempEntityList.add(wmsDataCompareTempEntity);
						}
						wmsDataCompareTaskService.saveTempTable(importId , wmsDataCompareTempEntityList , currParseOffset , i == partitionList.size());
						i = i + 1;
					}
				}
			}
			if(wmsDataCompareImportService.count(Wrappers.<WmsDataCompareImportEntity>lambdaQuery()
					.eq(WmsDataCompareImportEntity::getTaskId, id)
					.eq(WmsDataCompareImportEntity::getParseStatus, WmsDataCompareImportParseStatusEnum.WAIT.getCode())) == 0) {
				update(Wrappers.<WmsDataCompareTaskEntity>lambdaUpdate().set(WmsDataCompareTaskEntity::getSubStatus, WmsDataCompareTaskSubStatusEnum.WAIT_COMPARE)
						.eq(WmsDataCompareTaskEntity::getId, id).eq(WmsDataCompareTaskEntity::getSubStatus, WmsDataCompareTaskSubStatusEnum.WAIT_PARSE));
			}
		}
	}
	
	private void compareSystemImportData(String id) {
		WmsDataCompareTaskEntity wmsDataCompareTaskEntity = getById(id);
		if(WmsDataCompareTaskSubStatusEnum.WAIT_COMPARE.getCode().equals(wmsDataCompareTaskEntity.getSubStatus())) {
			WmsDataCompareBillService<?> wmsDataCompareBillService = wmsDataCompareHandlerFactory.get(wmsDataCompareTaskEntity.getBillType());
			List<?> dataCompareByConditionList = wmsDataCompareBillService.getDataCompareByCondition(wmsDataCompareTaskEntity.getSystemDataCondition() , id);
			Integer resultSameCount = wmsDataCompareTaskEntity.getResultSameCount();
			Integer resultExceedCount = wmsDataCompareTaskEntity.getResultExceedCount();
			Integer resultMissCount = wmsDataCompareTaskEntity.getResultMissCount();
			Integer resultDiffCount = wmsDataCompareTaskEntity.getResultDiffCount();
			if(CollUtil.isNotEmpty(dataCompareByConditionList)) {
				List<ImportDataMappingDTO> importDataMappingDTOList = JSON.parseArray(wmsDataCompareTaskEntity.getImportDataMapping() , WmsDataComparePlanDTO.ImportDataMappingDTO.class);
				List<ImportDataMappingDTO> pkImportDataMappingDTOList = importDataMappingDTOList.stream().filter(i -> i.getPkFlag() != null && i.getPkFlag()).collect(Collectors.toList());
				importDataMappingDTOList = importDataMappingDTOList.stream().filter(i -> i.getPkFlag() == null || !i.getPkFlag()).collect(Collectors.toList());
				Map<String, List<Object>> pkFieldValueSystemDataMaps = new HashMap<>();
				dataCompareByConditionList.forEach(d -> {
					StringBuffer sb = new StringBuffer();
					for(ImportDataMappingDTO importDataMappingDTO : pkImportDataMappingDTOList) {
						String value = "";
						try {
							Method method = d.getClass().getMethod("get" + StringUtils.capitalize(importDataMappingDTO.getSystemField()));
							Object invoke = method.invoke(d);
							if(invoke != null) {
								value = invoke.toString();
							}
							sb.append("-");
							sb.append(value);
						} catch (Exception e) {
						}
					}
					String pkFieldValue = sb.length() > 0 ? sb.toString().substring(1) : "";
					List<Object> datas = pkFieldValueSystemDataMaps.get(pkFieldValue);
					if(CollUtil.isEmpty(datas)) {
						datas = new ArrayList<>();
					}
					datas.add(d);
					pkFieldValueSystemDataMaps.put(pkFieldValue, datas);
				});
				
				//数据已放入pkFieldValueSystemDataMaps，置空释放内存
				dataCompareByConditionList = new ArrayList<>();
				
				Map<String, WmsDataCompareTempEntity> pkFieldValueImportDataMaps = wmsDataCompareTempService.list(Wrappers.<WmsDataCompareTempEntity>lambdaQuery()
						.eq(WmsDataCompareTempEntity::getTaskId, id)
						.eq(WmsDataCompareTempEntity::getMainDataType, WmsDataCompareTempMainDataTypeEnum.IMPORT.getCode())
						.in(WmsDataCompareTempEntity::getPkFieldValue, pkFieldValueSystemDataMaps.keySet()))
						.stream().collect(Collectors.toMap(WmsDataCompareTempEntity::getPkFieldValue, w -> w));
				List<WmsDataCompareTempEntity> insertOrUpdateTempEntity = new ArrayList<>();
				for(Map.Entry<String, List<Object>> pkFieldValueSystemDataMap : pkFieldValueSystemDataMaps.entrySet()) {
					String pkFieldValue = pkFieldValueSystemDataMap.getKey();
					List<Object> systemDataList = pkFieldValueSystemDataMap.getValue();
					WmsDataCompareTempEntity wmsDataCompareTempEntity = pkFieldValueImportDataMaps.get(pkFieldValue);
					if(wmsDataCompareTempEntity != null) {
						Object systemDataDto = systemDataList.get(0);
						if(systemDataList.size() == 1) {
							systemDataList = new ArrayList<>();
						}else {
							systemDataList = systemDataList.subList(1, systemDataList.size());
						}
						DataCompareDTO importDataDto = wmsDataCompareBillService.getCompareDTO(wmsDataCompareTempEntity.getImportDataJson());
						List<ImportDataMappingDTO> diffImportDataMappingDTO = new ArrayList<>();
						for(ImportDataMappingDTO importDataMappingDTO : importDataMappingDTOList) {
							String systemField = importDataMappingDTO.getSystemField();
							String systemData = "";
							String importData = "";
							try {
								Method method = systemDataDto.getClass().getMethod("get" + StringUtils.capitalize(systemField));
								Object invokeObject = method.invoke(systemDataDto);
								if(invokeObject != null) {
									systemData = invokeObject.toString();
								}
								method = importDataDto.getClass().getMethod("get" + StringUtils.capitalize(systemField));
								invokeObject = method.invoke(importDataDto);
								if(invokeObject != null) {
									importData = invokeObject.toString();
									if(systemField.endsWith("Date") || systemField.endsWith("Time")) {
										importData = DateUtil.format(DateUtil.parse(importData), "yyyy-MM-dd");
									}
								}
							} catch (Exception e) {
								throw new ServiceException(wmsDataCompareTaskEntity.getBillType() + "调用get反射方法失败");
							}
							if(!systemData.equals(importData)) {
								diffImportDataMappingDTO.add(importDataMappingDTO);
							}
						}
						if(CollUtil.isNotEmpty(diffImportDataMappingDTO)) {
							wmsDataCompareTempEntity.setCompareResult(WmsDataCompareTempCompareResultEnum.DIFF.getCode());
							resultDiffCount = resultDiffCount + 1;
							wmsDataCompareTempEntity.setDiffFields(JSON.toJSONString(diffImportDataMappingDTO));
						}else {
							wmsDataCompareTempEntity.setCompareResult(WmsDataCompareTempCompareResultEnum.SAME.getCode());
							resultSameCount = resultSameCount + 1;
						}
						wmsDataCompareTempEntity.setSystemDataJson(JSON.toJSONString(systemDataDto));
						wmsDataCompareTempEntity.setSystemDataId(((DataCompareDTO)systemDataDto).getId());
						wmsDataCompareTempEntity.setCompareStatus(WmsDataCompareTempCompareStatusEnum.FINISH.getCode());
						insertOrUpdateTempEntity.add(wmsDataCompareTempEntity);
					}
					
					for(Object systemData : systemDataList) {
						wmsDataCompareTempEntity = new WmsDataCompareTempEntity();
						wmsDataCompareTempEntity.setTaskId(id);
						wmsDataCompareTempEntity.setMainDataType(WmsDataCompareTempMainDataTypeEnum.SYSTEM.getCode());
						wmsDataCompareTempEntity.setCompareStatus(WmsDataCompareTempCompareStatusEnum.FINISH.getCode());
						wmsDataCompareTempEntity.setPkFieldValue(pkFieldValue);
						wmsDataCompareTempEntity.setCompareResult(WmsDataCompareTempCompareResultEnum.EXCEED.getCode());
						resultExceedCount = resultExceedCount + 1;
						wmsDataCompareTempEntity.setSystemDataJson(JSON.toJSONString(systemData));
						wmsDataCompareTempEntity.setSystemDataId(((DataCompareDTO)systemData).getId());
						insertOrUpdateTempEntity.add(wmsDataCompareTempEntity);
					}
				}
				wmsDataCompareTempService.saveOrUpdateBatch(insertOrUpdateTempEntity);
			}
			
			resultMissCount = resultMissCount + wmsDataCompareTempService.lambdaQuery().eq(WmsDataCompareTempEntity::getTaskId, id)
					.eq(WmsDataCompareTempEntity::getCompareStatus, WmsDataCompareTempCompareStatusEnum.WAIT.getCode()).count();
			wmsDataCompareTempService.lambdaUpdate().eq(WmsDataCompareTempEntity::getTaskId, id)
													.eq(WmsDataCompareTempEntity::getCompareStatus, WmsDataCompareTempCompareStatusEnum.WAIT.getCode())
													.set(WmsDataCompareTempEntity::getCompareResult, WmsDataCompareTempCompareResultEnum.MISS.getCode())
													.set(WmsDataCompareTempEntity::getCompareStatus, WmsDataCompareTempCompareStatusEnum.FINISH.getCode())
													.update();
			lambdaUpdate().eq(WmsDataCompareTaskEntity::getId, id)
													.eq(WmsDataCompareTaskEntity::getSubStatus, WmsDataCompareTaskSubStatusEnum.WAIT_COMPARE.getCode())
													.set(WmsDataCompareTaskEntity::getSubStatus, WmsDataCompareTaskSubStatusEnum.WAIT_UPLOAD.getCode())
													.set(WmsDataCompareTaskEntity::getResultSameCount , resultSameCount)
													.set(WmsDataCompareTaskEntity::getResultExceedCount, resultExceedCount)
													.set(WmsDataCompareTaskEntity::getResultMissCount, resultMissCount)
													.set(WmsDataCompareTaskEntity::getResultDiffCount, resultDiffCount)
													.update();
		}
	}
	
	public void dealUploadResultExcel(String id) {
		WmsDataCompareTaskEntity wmsDataCompareTaskEntity = getById(id);
		if(WmsDataCompareTaskSubStatusEnum.WAIT_UPLOAD.getCode().equals(wmsDataCompareTaskEntity.getSubStatus())) {
			String resultReportUrl = wmsDataCompareTaskEntity.getResultReportUrl();
			if(StringUtils.isBlank(resultReportUrl)) {
				List<WmsDataCompareTempEntity> wmsDataCompareTempEntityList = wmsDataCompareTempService.lambdaQuery().eq(WmsDataCompareTempEntity::getTaskId, id)
						   .in(WmsDataCompareTempEntity::getCompareResult, Arrays.asList(WmsDataCompareTempCompareResultEnum.EXCEED.getCode() 
								   , WmsDataCompareTempCompareResultEnum.MISS.getCode() , WmsDataCompareTempCompareResultEnum.DIFF.getCode()))
						   .list();
				if(CollUtil.isNotEmpty(wmsDataCompareTempEntityList)) {
					List<ImportDataMappingDTO> importDataMappingDTOList = JSON.parseArray(wmsDataCompareTaskEntity.getImportDataMapping() , WmsDataComparePlanDTO.ImportDataMappingDTO.class);
					String billType = wmsDataCompareTaskEntity.getBillType();
					Map<String, String> valueNameMaps = dictBasicService.list(Wrappers.<DictBasicEntity>lambdaQuery()
							.eq(DictBasicEntity::getType, "datacompare_" + billType))
							.stream().collect(Collectors.toMap(DictBasicEntity::getValue, DictBasicEntity::getName));
					if(valueNameMaps.isEmpty()) {
						throw new ServiceException("对比单据对比属性未配置，请联系实施人员");
					}
					
					String fileName = System.getProperty("java.io.tmpdir") + "对比结果"+ UUID.fastUUID().toString() +".xlsx";
					ExcelWriter excelWriter = EasyExcel.write(fileName).build();
				    List<List<String>> headList = new ArrayList<>();
				    List<String> firstHead = new ArrayList<>();
				    firstHead.add("对比结果");
				    List<String> secordHead = new ArrayList<>();
				    secordHead.add("");
				    boolean firstFlag = true;
				    for(ImportDataMappingDTO importDataMappingDTO : importDataMappingDTOList) {
				    	if(firstFlag) {
				    		firstHead.add("系统数据");
				    		firstFlag = false;
				    	}else {
				    		firstHead.add("");
				    	}
				    	secordHead.add(valueNameMaps.get(importDataMappingDTO.getSystemField()));
				    }
				    firstFlag = true;
				    for(ImportDataMappingDTO importDataMappingDTO : importDataMappingDTOList) {
				    	if(firstFlag) {
				    		firstHead.add("导入数据");
				    		firstFlag = false;
				    	}else {
				    		firstHead.add("");
				    	}
				    	secordHead.add(importDataMappingDTO.getImportField());
				    }
				    
				    headList.add(firstHead);
					headList.add(secordHead);
					
				    WmsDataCompareBillService<?> wmsDataCompareBillService = wmsDataCompareHandlerFactory.get(billType);
				    List<String> diffIndexs = new ArrayList<>();
				    int i = headList.size();
				    for(WmsDataCompareTempEntity wmsDataCompareTempEntity : wmsDataCompareTempEntityList) {
				    	String compareResult = wmsDataCompareTempEntity.getCompareResult();
				    	String systemDataJson = "";
				    	if(WmsDataCompareTempCompareResultEnum.EXCEED.getCode().equals(compareResult) || WmsDataCompareTempCompareResultEnum.DIFF.getCode().equals(compareResult) ) {
				    		systemDataJson = wmsDataCompareTempEntity.getSystemDataJson();
				    	}
				    	String importDataJson = "";
				    	if(WmsDataCompareTempCompareResultEnum.MISS.getCode().equals(compareResult) || WmsDataCompareTempCompareResultEnum.DIFF.getCode().equals(compareResult) ) {
				    		importDataJson = wmsDataCompareTempEntity.getImportDataJson();
				    	}
				    	if(WmsDataCompareTempCompareResultEnum.DIFF.getCode().equals(compareResult)) {
				    		String diffFields = wmsDataCompareTempEntity.getDiffFields();
				    		if(StringUtils.isNotBlank(diffFields)) {
				    			List<ImportDataMappingDTO> diffDtoList = JSON.parseArray(diffFields , WmsDataComparePlanDTO.ImportDataMappingDTO.class);
				    			for(ImportDataMappingDTO diffDto : diffDtoList) {
				    				int j = 1;
				    				for(ImportDataMappingDTO importDataMappingDTO : importDataMappingDTOList) {
				    					if(importDataMappingDTO.getSystemField().equals(diffDto.getSystemField())) {
				    						diffIndexs.add(i + "-" + j);
				    						diffIndexs.add(i + "-" + (j + importDataMappingDTOList.size()));
				    						break;
				    					}
				    					j = j + 1;
				    				}
				    			}
				    		}
				    	}
				    	List<String> writeDatas = new ArrayList<>();
				    	writeDatas.add(WmsDataCompareTempCompareResultEnum.getName(compareResult));
				    	writeDatas.addAll(getWriteDatas(wmsDataCompareBillService, importDataMappingDTOList, systemDataJson));
				    	writeDatas.addAll(getWriteDatas(wmsDataCompareBillService, importDataMappingDTOList, importDataJson));
				    	headList.add(writeDatas);
				    	i = i + 1;
			    	}
				    
				    int size = importDataMappingDTOList.size();
					WriteSheet writeSheet = EasyExcel.writerSheet().registerWriteHandler(new MergeStrategy(size , diffIndexs)).build();
					excelWriter.write(headList, writeSheet);
				    excelWriter.finish();
				    
					resultReportUrl = FastDFSClientUtil.uploadFile(new File(fileName), fileName);
				}
			}
				wmsDataCompareTaskService.dealFinishData(id , resultReportUrl);
		}
	}
	
	
	@Transactional(rollbackFor = Exception.class , propagation = Propagation.REQUIRES_NEW)
	@Override
	public void dealFinishData(String id , String resultReportUrl) {
		lambdaUpdate().eq(WmsDataCompareTaskEntity::getId, id)
		.eq(WmsDataCompareTaskEntity::getSubStatus, WmsDataCompareTaskSubStatusEnum.WAIT_UPLOAD.getCode())
		.set(WmsDataCompareTaskEntity::getSubStatus, WmsDataCompareTaskSubStatusEnum.FINISH.getCode())
		.set(WmsDataCompareTaskEntity::getStatus, WmsDataCompareTaskStatusEnum.FINISH.getCode())
		.set(WmsDataCompareTaskEntity::getResultReportUrl, resultReportUrl)
		.update();
		wmsDataCompareTempService.deleteData(id);
	}
	
	private List<String> getWriteDatas(WmsDataCompareBillService<?> wmsDataCompareBillService , List<ImportDataMappingDTO> importDataMappingDTOList , String dtoJson){
		List<String> writeDatas = new ArrayList<>();
    	for(ImportDataMappingDTO importDataMappingDTO : importDataMappingDTOList) {
    		if(StringUtils.isNotBlank(dtoJson)) {
    			DataCompareDTO compareDTO = wmsDataCompareBillService.getCompareDTO(dtoJson);
    			try {
    				Method method = compareDTO.getClass().getMethod("get" + StringUtils.capitalize(importDataMappingDTO.getSystemField()));
    				Object invoke = method.invoke(compareDTO);
    				if(invoke != null) {
    					writeDatas.add(invoke.toString());
    				}else {
    					writeDatas.add("");
    				}
    			}catch (Exception e) {
    				throw new ServiceException(compareDTO.getClass().getName() + "调用get反射方法失败");
				}
    		}else {
    			writeDatas.add("");
    		}
	    }
    	return writeDatas;
	}
	
	@Transactional(rollbackFor = Exception.class , propagation = Propagation.REQUIRES_NEW)
	@Override
	public void saveTempTable(String importId , List<WmsDataCompareTempEntity> wmsDataCompareTempEntityList , Integer currParseOffset , boolean isLast) {
		wmsDataCompareTempService.saveBatch(wmsDataCompareTempEntityList);
		wmsDataCompareImportService.update(Wrappers.<WmsDataCompareImportEntity>lambdaUpdate().eq(WmsDataCompareImportEntity::getId, importId)
				.set(isLast , WmsDataCompareImportEntity::getParseStatus, WmsDataCompareImportParseStatusEnum.FINISH.getCode())
				.set(WmsDataCompareImportEntity::getCurrParseOffset, currParseOffset + wmsDataCompareTempEntityList.size()));
	}
	
	@Override
	public PagingVO<ViewDTO> paging(PagingDTO<PagingParamDTO> dto) {
		PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<ViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        //处理分页数据
        fillPaging(list,false);
        return new PagingVO<>(pageData);
	}
	
	private void fillPaging(List<ViewDTO> list,Boolean isExport) {
		list.forEach(l -> {
			String billType = l.getBillType();
			if(StringUtils.isNotBlank(billType)) {
				l.setBillTypeName(WmsDataCompareTaskBillTypeEnum.getName(billType));
			}
			String status = l.getStatus();
			if(StringUtils.isNotBlank(status)) {
				l.setStatusName(WmsDataCompareTaskStatusEnum.getName(status));
			}
		});
	}

}
