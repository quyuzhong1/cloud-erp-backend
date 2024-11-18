package com.erp.server.wms.service.impl;


import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Resource;

import cn.hutool.core.text.CharSequenceUtil;
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
import com.baomidou.mybatisplus.core.toolkit.Sequence;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.WmsDataComparePlanDTO;
import com.erp.model.wms.dto.WmsDataComparePlanDTO.ImportDataMappingDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO.CreateViewDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO.DataCompareSettingDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO.DataCompareSettingImprotDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO.DataCompareSettingMapDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO.DataCompareSettingTypeDTO;
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
import com.erp.model.wms.enums.WmsDataCompareTaskClassTypeEnum;
import com.erp.model.wms.enums.WmsDataCompareTaskStatusEnum;
import com.erp.model.wms.enums.WmsDataCompareTaskSubStatusEnum;
import com.erp.model.wms.enums.WmsDataCompareTempCompareResultEnum;
import com.erp.model.wms.enums.WmsDataCompareTempCompareStatusEnum;
import com.erp.model.wms.enums.WmsDataCompareTempMainDataTypeEnum;
import com.erp.model.wms.enums.WmsDataCompareTypeEnum;
import com.erp.server.wms.config.WmsDataCompareHandlerFactory;
import com.erp.server.wms.mapper.WmsDataCompareTaskMapper;
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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
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
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private WmsDataCompareImportService wmsDataCompareImportService;
    @Resource
    private WmsDataCompareHandlerFactory wmsDataCompareHandlerFactory;
    @Resource
    private DictBasicService dictBasicService;
    @Resource
    @Qualifier("wmsDataCompareExecutorPool")
    private ExecutorService wmsDataCompareExecutorPool;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private WmsDataCompareTempService wmsDataCompareTempService;
    @Resource
    private WmsDataCompareTaskService wmsDataCompareTaskService;

    private static final Integer INSERT_SIZE = 5000;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public WmsDataCompareTaskDTO.AddViewDTO add(WmsDataCompareTaskDTO.AddDTO addDTO) {
        WmsDataCompareTaskEntity wmsDataCompareTaskEntity = new WmsDataCompareTaskEntity();
        BeanMapperUtils.copy(addDTO, wmsDataCompareTaskEntity);
        // 数据处理
        handleData(wmsDataCompareTaskEntity);
        
        String billType = wmsDataCompareTaskEntity.getBillType();
        String systemDataCondition = wmsDataCompareTaskEntity.getSystemDataCondition();
        
        List<String> excelFiles = addDTO.getExcelFiles();
		if(CollUtil.isEmpty(excelFiles)) {
			throw new ServiceException("对比数据导入文件为空");
        }
        
        WmsDataCompareBillService wmsDataCompareBillService = wmsDataCompareHandlerFactory.get(billType);
        WmsDataCompareExcelDto wmsDataCompareExcelDto = validataImportFile(excelFiles);
        
        List<WmsDataCompareImportEntity> wmsDataCompareImportEntityList = new ArrayList<>(excelFiles.size());
        WmsDataCompareImportEntity wmsDataCompareImportEntity = null;
        for(String excelFile : excelFiles) {
        	wmsDataCompareImportEntity = new WmsDataCompareImportEntity();
        	wmsDataCompareImportEntity.setFileUrl(excelFile);
        	wmsDataCompareImportEntity.setParseStatus(WmsDataCompareImportParseStatusEnum.WAIT.getCode());
        	wmsDataCompareImportEntity.setCurrParseOffset(0);
        	wmsDataCompareImportEntity.setMainFlag(Boolean.FALSE);
        	wmsDataCompareImportEntityList.add(wmsDataCompareImportEntity);
        }
        
		wmsDataCompareTaskEntity.setImportDataCount(wmsDataCompareExcelDto.getImportDataCount());
		
		List<String> mainExcelFiles = addDTO.getMainExcelFiles();
		List<String> sysHeadFields = null;
		if(CollUtil.isNotEmpty(mainExcelFiles)) {
			for(String excelFile : mainExcelFiles) {
	        	wmsDataCompareImportEntity = new WmsDataCompareImportEntity();
	        	wmsDataCompareImportEntity.setFileUrl(excelFile);
	        	wmsDataCompareImportEntity.setParseStatus(WmsDataCompareImportParseStatusEnum.FINISH.getCode());
	        	wmsDataCompareImportEntity.setMainFlag(Boolean.TRUE);
	        	wmsDataCompareImportEntityList.add(wmsDataCompareImportEntity);
	        }
			WmsDataCompareExcelDto sysWmsDataCompareExcelDto = validataImportFile(mainExcelFiles);
			sysHeadFields = sysWmsDataCompareExcelDto.getHeadFieldLists().get(0);
			wmsDataCompareTaskEntity.setSystemDataCount(sysWmsDataCompareExcelDto.getImportDataCount());
		}else {
			wmsDataCompareTaskEntity.setSystemDataCount(wmsDataCompareBillService.getDbSystemDataCount(systemDataCondition));
		}
        
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
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "数据对比任务" , wmsDataCompareTaskEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DATA_COMPARE.getCode(), wmsDataCompareTaskEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        wmsDataCompareImportEntityList.forEach(w -> {
        	w.setTaskId(wmsDataCompareTaskEntity.getId());
        });
        wmsDataCompareImportService.saveBatch(wmsDataCompareImportEntityList);
        
        WmsDataCompareTaskDTO.AddViewDTO viewDTO = new WmsDataCompareTaskDTO.AddViewDTO();
        BeanMapperUtils.copy(wmsDataCompareTaskEntity, viewDTO);
        
        List<DataCompareSettingDTO> setting = getSetting(billType , wmsDataCompareExcelDto.getHeadFieldLists().get(0) , sysHeadFields);
        if(CollUtil.isEmpty(sysHeadFields) && wmsDataCompareTaskEntity.getCompareType().equals(WmsDataCompareTypeEnum.PK.getCode())) {
        	setting.forEach(s -> s.setStatus(!s.getStatus()));
        }
		viewDTO.setSettingList(setting);
        
        viewDTO.setImportFileUrls(Collections.singletonList(WmsDataCompareUtils.mergeExcel(excelFiles)));
        return viewDTO;
    }

    private WmsDataCompareExcelDto validataImportFile(List<String> excelFiles) {
		WmsDataCompareExcelDto wmsDataCompareExcelDto = WmsDataCompareUtils.getWmsDataCompareExcelDto(excelFiles);
		List<List<String>> headFieldLists = wmsDataCompareExcelDto.getHeadFieldLists();
        if(CollUtil.isEmpty(headFieldLists)) {
        	throw new ServiceException("所有导入文件行数都为空，请检查");
        }
        
        if(!WmsDataCompareUtils.areAllListsEqual(headFieldLists)) {
        	throw new ServiceException("导入的多个文件表头不一致，请检查");
        }
		
		Set<String> fieldSet = new HashSet<>();
        for(String field : headFieldLists.get(0)) {
        	if(CharSequenceUtil.isBlank(field)) {
        		throw new ServiceException("导入文件表头含有空值，请检查");
        	}
        	if(fieldSet.contains(field.trim())) {
        		throw new ServiceException("导入文件表头含有重复值，请检查");
        	}
        	fieldSet.add(field);
        }
		return wmsDataCompareExcelDto;
	}
    
    private List<DataCompareSettingDTO> getSetting(String billType , List<String> importHeadFields , List<String> sysHeadFields) {
    	List<DataCompareSettingDTO> settingList = new ArrayList<>();
        List<DataCompareSettingImprotDTO> importFileds = new ArrayList<>();
        for(int i = 0;i < importHeadFields.size(); i++) {
        	DataCompareSettingImprotDTO dataCompareSettingImprotDTO = new DataCompareSettingImprotDTO();
        	dataCompareSettingImprotDTO.setImportField(importHeadFields.get(i));
        	dataCompareSettingImprotDTO.setImportFieldIndex(i + 1);
        	importFileds.add(dataCompareSettingImprotDTO);
        }
        
        List<DataCompareSettingTypeDTO> dataTypes = new ArrayList<>();
        WmsDataCompareTaskClassTypeEnum[] values = WmsDataCompareTaskClassTypeEnum.values();
        for(int i = 0;i < values.length; i++) {
        	DataCompareSettingTypeDTO dataCompareSettingTypeDTO = new DataCompareSettingTypeDTO();
        	dataCompareSettingTypeDTO.setTypeCode(values[i].getCode());
        	dataCompareSettingTypeDTO.setTypeName(values[i].getName());
        	dataTypes.add(dataCompareSettingTypeDTO);
        }
        
        if(CollUtil.isNotEmpty(sysHeadFields)) {
        	int index = 0;
        	for(String sysHeadField : sysHeadFields) {
        		DataCompareSettingDTO dataCompareSettingDTO = new DataCompareSettingDTO();
        		dataCompareSettingDTO.setSysFieldName(sysHeadField);
        		dataCompareSettingDTO.setSysField(sysHeadField);
        		dataCompareSettingDTO.setSysFieldIndex(index + 1);
        		dataCompareSettingDTO.setImportField(importFileds.stream().filter(i -> i.getImportField().equals(dataCompareSettingDTO.getSysFieldName()))
        				.findAny().orElse(new DataCompareSettingImprotDTO()).getImportField());
        		dataCompareSettingDTO.setImportFields(importFileds);
        		dataCompareSettingDTO.setClassType(WmsDataCompareTaskClassTypeEnum.STRING.getCode());
            	dataCompareSettingDTO.setDataTypes(dataTypes);
        		dataCompareSettingDTO.setStatus(Boolean.FALSE);
        		index = index + 1;
        		settingList.add(dataCompareSettingDTO);
        	}
        }else {
        	List<DictBasicEntity> dictBasicEntityList = dictBasicService.list(Wrappers.<DictBasicEntity>lambdaQuery()
					.eq(DictBasicEntity::getType, "datacompare_" + billType));
        	for(DictBasicEntity dictBasicEntity : dictBasicEntityList) {
        		DataCompareSettingDTO dataCompareSettingDTO = new DataCompareSettingDTO();
        		dataCompareSettingDTO.setSysFieldName(dictBasicEntity.getName());
        		dataCompareSettingDTO.setSysField(dictBasicEntity.getValue());
        		dataCompareSettingDTO.setSysFieldIndex(dictBasicEntity.getSort());
        		dataCompareSettingDTO.setImportField(importFileds.stream().filter(i -> i.getImportField().equals(dataCompareSettingDTO.getSysFieldName()))
        				.findAny().orElse(new DataCompareSettingImprotDTO()).getImportField());
        		dataCompareSettingDTO.setImportFields(importFileds);
        		String remark = dictBasicEntity.getRemark();
        		if(CharSequenceUtil.isBlank(remark)) {
        			remark = WmsDataCompareTaskClassTypeEnum.STRING.getCode();
        		}
        		dataCompareSettingDTO.setClassType(remark);
        		dataCompareSettingDTO.setDataTypes(dataTypes);
        		dataCompareSettingDTO.setStatus(dictBasicEntity.getStatus());
        		settingList.add(dataCompareSettingDTO);
        	}
        }
        
        return settingList;
    }
    
    private Map<String, DataCompareSettingMapDTO> getSettingMap(List<DataCompareSettingDTO> settingList){
    	Map<String, DataCompareSettingMapDTO> map = new HashMap<>();
    	for(DataCompareSettingDTO setting : settingList) {
    		DataCompareSettingMapDTO dataCompareSettingMapDTO = new DataCompareSettingMapDTO();
    		BeanUtil.copyProperties(setting, dataCompareSettingMapDTO);
    		dataCompareSettingMapDTO.setImportFieldMap(setting.getImportFields().stream().collect(Collectors.toMap(DataCompareSettingImprotDTO::getImportField, d -> d)));
    		map.put(setting.getSysField(), dataCompareSettingMapDTO);
    	}
    	return map;
    }
    
    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(WmsDataCompareTaskDTO.UpdateDTO updateDTO) {
        WmsDataCompareTaskEntity old = super.getById(updateDTO.getId());
		if (Objects.isNull(old)){
			throw new ServiceException(ApiError.NOT_EXIST_BILL, "数据对比任务");
		}
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
            String msg = CharSequenceUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), wmsDataCompareTaskEntity.getCode(), "数据对比任务");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, wmsDataCompareTaskEntity, null, wmsDataCompareTaskEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(WmsDataCompareTaskEntity wmsDataCompareTaskEntity) {
    // TODO 验证数据 & 数据赋值
    	if(CharSequenceUtil.isBlank(wmsDataCompareTaskEntity.getName())) {
    		throw new ServiceException("对比任务名称不能为空");
    	}
    	if(CharSequenceUtil.isBlank(wmsDataCompareTaskEntity.getBillType())) {
//    		throw new ServiceException("系统单据不能为空");
    	}
    	if(CharSequenceUtil.isBlank(wmsDataCompareTaskEntity.getSystemDataCondition())) {
    		throw new ServiceException("系统数据范围不能为空");
    	}
    	if(CharSequenceUtil.isBlank(wmsDataCompareTaskEntity.getCompareType())) {
    		wmsDataCompareTaskEntity.setCompareType(WmsDataCompareTypeEnum.GROUP.getCode());
    	}
    	
    	wmsDataCompareTaskEntity.setStatus("init");
    }

	@Override
	public String downloadSystemData(BaseIdDTO dto) {
		String id = dto.getId();
		WmsDataCompareTaskEntity wmsDataCompareTaskEntity = this.getById(id);
		if(wmsDataCompareTaskEntity == null) {
			throw new ServiceException("任务id不存在");
        }
		return wmsDataCompareHandlerFactory.get(wmsDataCompareTaskEntity.getBillType()).uploadSystemDataByCondition(wmsDataCompareTaskEntity);
	}
	
	@Transactional(rollbackFor = Exception.class)
	@Override
	public SetNextViewDTO setNext(SetNextDTO dto) {
		String id = dto.getId();
		WmsDataCompareTaskEntity wmsDataCompareTaskEntity = this.getById(id);
		if(wmsDataCompareTaskEntity == null) {
			throw new ServiceException("任务id不存在");
        }
		WmsDataCompareTypeEnum compareType = EnumMessage.getByCode(WmsDataCompareTypeEnum.class, wmsDataCompareTaskEntity.getCompareType());

		SetNextViewDTO setNextViewDTO = new SetNextViewDTO();
		String importDataMapping = dto.getImportDataMapping();
		List<ImportDataMappingDTO> importDataMappingDTOList = null;
		try {
			importDataMappingDTOList = JSON.parseArray(importDataMapping , WmsDataComparePlanDTO.ImportDataMappingDTO.class);
		} catch (Exception e) {
			setNextViewDTO.setErrMessageList(Collections.singletonList("导入数据字段映射json串格式错误"));
			return setNextViewDTO;
		}
		
		importDataMappingDTOList.removeIf(i -> CharSequenceUtil.isBlank(i.getSystemField()) || CharSequenceUtil.isBlank(i.getImportField()));
		if(CollUtil.isNotEmpty(importDataMappingDTOList)) {
			long count = importDataMappingDTOList.stream().filter(i -> i.getStatus() != null && i.getStatus()).count();
			if(WmsDataCompareTypeEnum.PK == compareType) {
				if(count == 0) {
					setNextViewDTO.setErrMessageList(Collections.singletonList("主键字段至少需要设置一个"));
					return setNextViewDTO;
				}
			}else {
				if(count == 0) {
					setNextViewDTO.setErrMessageList(Collections.singletonList("汇总字段需要设置一个"));
					return setNextViewDTO;
				}
				if(count > 1) {
					setNextViewDTO.setErrMessageList(Collections.singletonList("汇总字段只能设置一个"));
					return setNextViewDTO;
				}
				if(importDataMappingDTOList.size() < 2) {
					setNextViewDTO.setErrMessageList(Collections.singletonList("除汇总字段设置映射外，非汇总字段至少设置一个映射"));
					return setNextViewDTO;
				}
				ImportDataMappingDTO pkImportDataMappingDTO = importDataMappingDTOList.stream().filter(i -> i.getStatus() != null && i.getStatus()).findAny().orElse(null);
				if(!WmsDataCompareTaskClassTypeEnum.INT.getCode().equals(pkImportDataMappingDTO.getClassType())) {
					setNextViewDTO.setErrMessageList(Collections.singletonList("汇总字段必须为数字类型"));
					return setNextViewDTO;
				}
			}
			
			List<WmsDataCompareImportEntity> allWmsDataCompareImportEntityList = wmsDataCompareImportService.list(Wrappers.<WmsDataCompareImportEntity>lambdaQuery()
					.eq(WmsDataCompareImportEntity::getTaskId, id));
			List<WmsDataCompareImportEntity> wmsDataCompareImportEntityList = allWmsDataCompareImportEntityList.stream().filter(w -> w.getMainFlag() == null || !w.getMainFlag()).collect(Collectors.toList());
			if(CollUtil.isNotEmpty(wmsDataCompareImportEntityList)) {
				Map<String, List<Map<String, String>>> importAllDatasMap = new HashMap<>();
				List<String> importHeadFields = null;
				for(WmsDataCompareImportEntity wmsDataCompareImportEntity : wmsDataCompareImportEntityList) {
					WmsDataCompareExcelDto wmsDataCompareExcelDto = WmsDataCompareUtils.getWmsDataCompareExcelDto(Collections.singletonList(wmsDataCompareImportEntity.getFileUrl()));
					List<List<String>> headFieldLists = wmsDataCompareExcelDto.getHeadFieldLists();
					if(CollUtil.isEmpty(headFieldLists)) {
						wmsDataCompareImportEntity.setParseStatus(WmsDataCompareImportParseStatusEnum.FINISH.getCode());
						continue;
					}
					List<Map<String, String>> datas = wmsDataCompareExcelDto.getDatas();
					if(CollUtil.isNotEmpty(datas)) {
						importAllDatasMap.put(wmsDataCompareImportEntity.getId(), datas);
					}else {
						wmsDataCompareImportEntity.setParseStatus(WmsDataCompareImportParseStatusEnum.FINISH.getCode());
					}
					importHeadFields = headFieldLists.get(0);
				}
				
				List<WmsDataCompareImportEntity> sysWmsDataCompareImportEntityList = allWmsDataCompareImportEntityList.stream().filter(w -> w.getMainFlag() != null && w.getMainFlag()).collect(Collectors.toList());
				Map<String, List<Map<String, String>>> systemAllDatasMap = new HashMap<>();
				List<String> systemHeadFields = null;
				for(WmsDataCompareImportEntity wmsDataCompareImportEntity : sysWmsDataCompareImportEntityList) {
					WmsDataCompareExcelDto wmsDataCompareExcelDto = WmsDataCompareUtils.getWmsDataCompareExcelDto(Collections.singletonList(wmsDataCompareImportEntity.getFileUrl()));
					List<List<String>> headFieldLists = wmsDataCompareExcelDto.getHeadFieldLists();
					if(CollUtil.isEmpty(headFieldLists)) {
						wmsDataCompareImportEntity.setParseStatus(WmsDataCompareImportParseStatusEnum.FINISH.getCode());
						continue;
					}
					List<Map<String, String>> datas = wmsDataCompareExcelDto.getDatas();
					if(CollUtil.isNotEmpty(datas)) {
						systemAllDatasMap.put(wmsDataCompareImportEntity.getId(), datas);
					}else {
						wmsDataCompareImportEntity.setParseStatus(WmsDataCompareImportParseStatusEnum.FINISH.getCode());
					}
					systemHeadFields = headFieldLists.get(0);
				}
				
				Map<String, DataCompareSettingMapDTO> settingMap = getSettingMap(getSetting(wmsDataCompareTaskEntity.getBillType(), importHeadFields, systemHeadFields));
				
				String duplicates = importDataMappingDTOList.stream().collect(Collectors.groupingBy(ImportDataMappingDTO::getSystemField)).entrySet()
		                .stream().filter(e -> e.getValue().size() > 1).map(e -> e.getKey()).collect(Collectors.joining("、"));
					if(CharSequenceUtil.isNotBlank(duplicates)) {
						setNextViewDTO.setErrMessageList(Collections.singletonList("系统数据字段中【"+ duplicates +"】重复"));
						return setNextViewDTO;
					}
				
				String emptyHeadIndex = importDataMappingDTOList.stream()
						.filter(i -> settingMap.get(i.getSystemField()) == null)
						.map(ImportDataMappingDTO::getSystemField).collect(Collectors.joining("、"));
				if(CharSequenceUtil.isNotBlank(emptyHeadIndex)) {
					setNextViewDTO.setErrMessageList(Collections.singletonList("系统数据字段中第【"+ emptyHeadIndex +"】行在系统表头不存在"));
					return setNextViewDTO;
				}
				importDataMappingDTOList.forEach(i -> i.setSystemFieldName(settingMap.get(i.getSystemField()).getSysFieldName()));
				
				duplicates = importDataMappingDTOList.stream().collect(Collectors.groupingBy(ImportDataMappingDTO::getImportField)).entrySet()
		                .stream().filter(e -> e.getValue().size() > 1).map(e -> e.getKey()).collect(Collectors.joining("、"));;
				if(CharSequenceUtil.isNotBlank(duplicates)) {
					setNextViewDTO.setErrMessageList(Collections.singletonList("导入数据字段中【"+ duplicates +"】重复"));
					return setNextViewDTO;
				}
				
				emptyHeadIndex = importDataMappingDTOList.stream()
						.filter(i -> settingMap.get(i.getSystemField()).getImportFieldMap().get(i.getImportField()) == null)
						.map(ImportDataMappingDTO::getImportField).collect(Collectors.joining("、"));
				if(CharSequenceUtil.isNotBlank(emptyHeadIndex)) {
					setNextViewDTO.setErrMessageList(Collections.singletonList("导入数据字段中第【"+ emptyHeadIndex +"】行在导入文件表头不存在"));
					return setNextViewDTO;
				}
				
				List<String> errMessageList = new ArrayList<>();
				validateExcelData(errMessageList, importAllDatasMap, importDataMappingDTOList, Boolean.FALSE , compareType);
				validateExcelData(errMessageList, systemAllDatasMap, importDataMappingDTOList, Boolean.TRUE , compareType);
				
				if(CollUtil.isNotEmpty(errMessageList)) {
					setNextViewDTO.setErrMessageList(errMessageList);
					return setNextViewDTO;
				}
				
				setNextViewDTO.setFlag(true);
				setNextViewDTO.setCode(wmsDataCompareTaskEntity.getCode());
				setNextViewDTO.setName(wmsDataCompareTaskEntity.getName());
				
				this.update(Wrappers.<WmsDataCompareTaskEntity>lambdaUpdate().eq(WmsDataCompareTaskEntity::getId, id)
						.eq(WmsDataCompareTaskEntity::getStatus, "init")
						.set(WmsDataCompareTaskEntity::getImportDataMapping, JSON.toJSONString(importDataMappingDTOList)));
				wmsDataCompareImportService.updateBatchById(allWmsDataCompareImportEntityList);
				
			}else {
				setNextViewDTO.setErrMessageList(Collections.singletonList("未查询到上传文件，请重新导入数据"));
			}
		}else {
			setNextViewDTO.setErrMessageList(Collections.singletonList("字段映射至少有一个才能创建任务"));
		}
		
		return setNextViewDTO;
	}

	private void validateExcelData(List<String> errMessageList , Map<String, List<Map<String, String>>> allDatasMap 
			, List<ImportDataMappingDTO> importDataMappingDTOList, Boolean sysFlag , WmsDataCompareTypeEnum compareType) {
		String fileType = "导入的对比数据文件";
		if(sysFlag != null && sysFlag) {
			fileType = "导入的系统数据文件";
		}
		Map<String, Integer> pkValueSameCountMaps = new HashMap<>();
		for(Map.Entry<String, List<Map<String, String>>> allDatas : allDatasMap.entrySet()) {
			int row = 1;
			for(Map<String, String> data : allDatas.getValue()) {
				row = row + 1;
				for(ImportDataMappingDTO importDataMappingDTO : importDataMappingDTOList) {
					String type = importDataMappingDTO.getClassType();
					WmsDataCompareTaskClassTypeEnum classType = EnumMessage.getByCode(WmsDataCompareTaskClassTypeEnum.class, type);
					String field = importDataMappingDTO.getImportField();
					if(sysFlag != null && sysFlag) {
						field = importDataMappingDTO.getSystemField();
					}
					String value = data.get(field);
					if(CharSequenceUtil.isNotBlank(value)) {
						try {
							if(classType == WmsDataCompareTaskClassTypeEnum.INT) {
								getBigDecimalValue(value);
							}else if(classType == WmsDataCompareTaskClassTypeEnum.DATE) {
								getDateValue(value);
							}
						} catch (Exception e) {
							errMessageList.add("字段类型校验失败："+ fileType + "第" + row + "行【" + field + "】的值是【" + value + "】，不为" + classType.getName() + "类型");
						}
					}
				}
				
				if(WmsDataCompareTypeEnum.PK == compareType && (sysFlag == null || !sysFlag)) {
					StringBuffer sb = new StringBuffer();
					for(ImportDataMappingDTO importDataMappingDTO : importDataMappingDTOList) {
						if(importDataMappingDTO.getStatus() != null && importDataMappingDTO.getStatus()) {
							String field = importDataMappingDTO.getImportField();
							if(sysFlag != null && sysFlag) {
								field = importDataMappingDTO.getSystemField();
							}
							String excelValue = data.get(field);
							if(excelValue == null) {
								excelValue = "";
							}
							sb.append("-");
							sb.append(excelValue);
						}
					}

					if(Stream.of(sb.toString().split("-")).allMatch(StringUtils::isBlank)) {
						continue;
					}
					String pkValue = sb.length() > 0 ? sb.toString().substring(1) : "";
					Integer sameCount = pkValueSameCountMaps.get(pkValue);
					if(sameCount == null) {
						sameCount = 0;
					}
					pkValueSameCountMaps.put(pkValue, sameCount + 1);
				}
			}
		}
		
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
	}
	
	private String getDateValue(String value) {
		try {
			return DateUtil.format(DateUtil.parse(value), "yyyy-MM-dd");
		} catch (Exception e) {
			return DateUtil.format(DateUtil.parse(value, "MM/dd/yyyy"), "yyyy-MM-dd");
		}
	}
	
	private BigDecimal getBigDecimalValue(Object value) {
		if(value == null) {
			return BigDecimal.ZERO;
		}
		if(CharSequenceUtil.isBlank(value.toString())) {
			return BigDecimal.ZERO;
		}
		return new BigDecimal(value.toString().replace(",", "").replace("，", ""));
	}

	@Override
	public CreateViewDTO create(BaseIdDTO dto) {
		String id = dto.getId();
		WmsDataCompareTaskEntity wmsDataCompareTaskEntity = this.getById(id);
		if(wmsDataCompareTaskEntity == null) {
			throw new ServiceException("任务id不存在");
        }else if(CharSequenceUtil.isBlank(wmsDataCompareTaskEntity.getImportDataMapping())) {
        	throw new ServiceException("对比映射未配置");
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
					.set(WmsDataCompareTaskEntity::getUpdateTime, LocalDateTime.now())
					.set(errorCount == 3 , WmsDataCompareTaskEntity::getStatus, WmsDataCompareTaskStatusEnum.ERROR.getCode())
					.eq(WmsDataCompareTaskEntity::getId, id));
			if(errorCount >= 3) {
				Map<String, Object> bodyMap = new HashMap<String, Object>();
				bodyMap.put("msg_type", "text");
				Map<String, String> contentMap = new HashMap<String, String>();
				
				if(BusinessCommonConstants.hasProfile("prod")) {
					contentMap.put("text", "数据对比生产环境告警：" + "任务id=" + id + "处理失败");
				}else {
					contentMap.put("text", "数据对比测试环境告警：" + "任务id=" + id + "处理失败");
				}
				bodyMap.put("content", contentMap);
				HttpUtil.post("https://open.feishu.cn/open-apis/bot/v2/hook/c76b72f8-0bf9-4967-a9ce-0728767c1ccc", JSON.toJSONString(bodyMap));
			}
		}finally {
			redisTemplate.delete(redisKey);
		}
	}
	
	private void parseExcelData(String id) throws Exception {
		log.warn("数据对比开始解析：{}" , id);
		WmsDataCompareTaskEntity wmsDataCompareTaskEntity = getById(id);
		if(WmsDataCompareTaskSubStatusEnum.WAIT_PARSE.getCode().equals(wmsDataCompareTaskEntity.getSubStatus())) {
			Map<String, List<Map<String, String>>> allDatasMap = new HashMap<>();
			List<WmsDataCompareImportEntity> wmsDataCompareImportEntityList = wmsDataCompareImportService
					.lambdaQuery().eq(WmsDataCompareImportEntity::getTaskId, id)
					.eq(WmsDataCompareImportEntity::getMainFlag, Boolean.FALSE)
					.eq(WmsDataCompareImportEntity::getParseStatus, WmsDataCompareImportParseStatusEnum.WAIT.getCode()).list();
			for(WmsDataCompareImportEntity wmsDataCompareImportEntity : wmsDataCompareImportEntityList) {
				WmsDataCompareExcelDto wmsDataCompareExcelDto = WmsDataCompareUtils.getWmsDataCompareExcelDto(Collections.singletonList(wmsDataCompareImportEntity.getFileUrl()));
				List<List<String>> headFieldLists = wmsDataCompareExcelDto.getHeadFieldLists();
				if(CollUtil.isEmpty(headFieldLists)) {
					continue;
				}
				List<Map<String, String>> datas = wmsDataCompareExcelDto.getDatas();
				if(CollUtil.isNotEmpty(datas)) {
					allDatasMap.put(wmsDataCompareImportEntity.getId(), datas);
				}
			}
			List<ImportDataMappingDTO> importDataMappingDTOList = JSON.parseArray(wmsDataCompareTaskEntity.getImportDataMapping() , WmsDataComparePlanDTO.ImportDataMappingDTO.class);
			
			Sequence sequence = new Sequence();
			for(Map.Entry<String, List<Map<String, String>>> allDatas : allDatasMap.entrySet()) {
				String importId = allDatas.getKey();
				List<Map<String, String>> value = allDatas.getValue();
				WmsDataCompareImportEntity wmsDataCompareImportEntity = wmsDataCompareImportService.getById(importId);
				Integer currParseOffset = wmsDataCompareImportEntity.getCurrParseOffset();
				if(currParseOffset >= value.size()) {
					wmsDataCompareImportService.update(Wrappers.<WmsDataCompareImportEntity>lambdaUpdate().eq(WmsDataCompareImportEntity::getId, importId)
							.eq(WmsDataCompareImportEntity::getParseStatus, WmsDataCompareImportParseStatusEnum.WAIT.getCode())
							.set(WmsDataCompareImportEntity::getParseStatus, WmsDataCompareImportParseStatusEnum.FINISH.getCode()));
				}else {
					if(currParseOffset >= 1) {
						value = value.subList(currParseOffset, value.size());
					}
					List<List<Map<String, String>>> partitionList = Lists.partition(value, INSERT_SIZE);
					int i = 1;
					for(List<Map<String, String>> partition : partitionList) {
						List<WmsDataCompareTempEntity> wmsDataCompareTempEntityList = new ArrayList<>(partition.size());
						WmsDataCompareTempEntity wmsDataCompareTempEntity = null;
						for(Map<String, String> p : partition) {
							wmsDataCompareTempEntity = new WmsDataCompareTempEntity();
							wmsDataCompareTempEntity.setId(Long.valueOf(sequence.nextId()).toString());
							wmsDataCompareTempEntity.setTaskId(id);
							wmsDataCompareTempEntity.setMainDataType(WmsDataCompareTempMainDataTypeEnum.IMPORT.getCode());
							wmsDataCompareTempEntity.setCompareStatus(WmsDataCompareTempCompareStatusEnum.WAIT.getCode());
							Map<String, String> tempData = new HashMap<>();
							StringBuffer sb = new StringBuffer();
							for(ImportDataMappingDTO importDataMappingDTO : importDataMappingDTOList) {
								String excelValue = p.get(importDataMappingDTO.getImportField());
								if(excelValue == null) {
									excelValue = "";
								}
								if(CharSequenceUtil.isNotBlank(excelValue)) {
									if(WmsDataCompareTaskClassTypeEnum.DATE.getCode().equals(importDataMappingDTO.getClassType())) {
										excelValue = getDateValue(excelValue);
									}
								}
								tempData.put(importDataMappingDTO.getImportField() , excelValue);
								if(WmsDataCompareTypeEnum.PK.getCode().equals(wmsDataCompareTaskEntity.getCompareType())) {
									if(importDataMappingDTO.getStatus() != null && importDataMappingDTO.getStatus()) {
										sb.append("-");
										sb.append(excelValue);
									}
								}else {
									if(importDataMappingDTO.getStatus() == null || !importDataMappingDTO.getStatus()) {
										sb.append("-");
										sb.append(excelValue);
									}
								}
							}
							wmsDataCompareTempEntity.setImportDataJson(JSON.toJSONString(tempData));
							if(Stream.of(sb.toString().split("-")).allMatch(StringUtils::isBlank)) {
								continue;
							}
							wmsDataCompareTempEntity.setPkFieldValue(sb.length() > 0 ? sb.toString().substring(1) : "");
							wmsDataCompareTempEntity.setCompareResult("");
							wmsDataCompareTempEntityList.add(wmsDataCompareTempEntity);
						}
						currParseOffset = currParseOffset + wmsDataCompareTempEntityList.size();
						wmsDataCompareTaskService.saveTempTable(importId , wmsDataCompareTempEntityList , currParseOffset , i == partitionList.size());
						i = i + 1;
					}
				}
			}

			lambdaUpdate().eq(WmsDataCompareTaskEntity::getId, id)
						.eq(WmsDataCompareTaskEntity::getSubStatus, WmsDataCompareTaskSubStatusEnum.WAIT_PARSE.getCode())
						.set(WmsDataCompareTaskEntity::getSubStatus, WmsDataCompareTaskSubStatusEnum.WAIT_COMPARE.getCode())
						.set(WmsDataCompareTaskEntity::getUpdateTime, LocalDateTime.now())
						.update();
		}
		log.warn("数据对比解析结束：{}" , id);
	}
	
	private void compareSystemImportData(String id) {
		log.warn("数据对比开始对比：{}" , id);
		WmsDataCompareTaskEntity wmsDataCompareTaskEntity = getById(id);
		if(WmsDataCompareTaskSubStatusEnum.WAIT_COMPARE.getCode().equals(wmsDataCompareTaskEntity.getSubStatus())) {
			if(WmsDataCompareTypeEnum.GROUP.getCode().equals(wmsDataCompareTaskEntity.getCompareType())) {
				compareGroup(wmsDataCompareTaskEntity);
			}else {
				comparePk(wmsDataCompareTaskEntity);
			}
		}
		lambdaUpdate().eq(WmsDataCompareTaskEntity::getId, id)
					.eq(WmsDataCompareTaskEntity::getSubStatus, WmsDataCompareTaskSubStatusEnum.WAIT_COMPARE.getCode())
					.set(WmsDataCompareTaskEntity::getSubStatus, WmsDataCompareTaskSubStatusEnum.WAIT_UPLOAD.getCode())
					.set(WmsDataCompareTaskEntity::getUpdateTime, LocalDateTime.now())
					.update();

		log.warn("数据对比结束对比：{}" , id);
	}

	private void compareGroup(WmsDataCompareTaskEntity wmsDataCompareTaskEntity) {
		String id = wmsDataCompareTaskEntity.getId();
		WmsDataCompareBillService wmsDataCompareBillService = wmsDataCompareHandlerFactory.get(wmsDataCompareTaskEntity.getBillType());
		List<Map<String, String>> systemDataMapList = wmsDataCompareBillService.getSystemData(wmsDataCompareTaskEntity);
		Map<String , Map<String, String>> systemPkDataMaps = new HashMap<>();

		List<ImportDataMappingDTO> importDataMappingDTOList = JSON.parseArray(wmsDataCompareTaskEntity.getImportDataMapping() , WmsDataComparePlanDTO.ImportDataMappingDTO.class);
		List<ImportDataMappingDTO> pkImportDataMappingDTOList = importDataMappingDTOList.stream().filter(i -> i.getStatus() != null && i.getStatus()).collect(Collectors.toList());
		List<ImportDataMappingDTO> notPkImportDataMappingDTOList = importDataMappingDTOList.stream().filter(i -> i.getStatus() == null || !i.getStatus()).collect(Collectors.toList());

		String systemField = pkImportDataMappingDTOList.get(0).getSystemField();
		String importField = pkImportDataMappingDTOList.get(0).getImportField();

		if(CollUtil.isNotEmpty(systemDataMapList)) {
			systemDataMapList.forEach(d -> {
				Map<String, String> tempData = new HashMap<>();
				StringBuffer sb = new StringBuffer();
				for(ImportDataMappingDTO importDataMappingDTO : notPkImportDataMappingDTOList) {
					String excelValue = d.get(importDataMappingDTO.getSystemField());
					if(excelValue == null) {
						excelValue = "";
					}
					if(CharSequenceUtil.isNotBlank(excelValue)) {
						if(WmsDataCompareTaskClassTypeEnum.DATE.getCode().equals(importDataMappingDTO.getClassType())) {
							excelValue = getDateValue(excelValue);
						}
					}
					sb.append("-");
					sb.append(excelValue);
					tempData.put(importDataMappingDTO.getSystemField(), excelValue);
				}
				if(Stream.of(sb.toString().split("-")).allMatch(StringUtils::isBlank)) {
					return;
				}

				String pkFieldValue = sb.length() > 0 ? sb.toString().substring(1) : "";
				Map<String, String> data = systemPkDataMaps.get(pkFieldValue);
				BigDecimal count = BigDecimal.ZERO;
				if(data == null) {
					data = tempData;
				}else {
					count = getBigDecimalValue(data.get(systemField));
				}
				data.put(systemField, count.add(getBigDecimalValue(d.get(systemField))).toString());
				systemPkDataMaps.put(pkFieldValue, data);
			});
			
			//数据已放入systemPkDataMaps，置空释放内存
			systemDataMapList = new ArrayList<>();
		}

		Sequence sequence = new Sequence();

		Map<String, Map<String, String>> importPkDataMaps = new HashMap<>();
		Map<String, List<WmsDataCompareTempEntity>> pkFieldValueImportDataMaps = wmsDataCompareTempService.list(Wrappers.<WmsDataCompareTempEntity>lambdaQuery()
				.eq(WmsDataCompareTempEntity::getTaskId, id)
				.eq(WmsDataCompareTempEntity::getCompareStatus, WmsDataCompareTempCompareStatusEnum.WAIT.getCode())
				.eq(WmsDataCompareTempEntity::getMainDataType, WmsDataCompareTempMainDataTypeEnum.IMPORT.getCode()))
				.stream().collect(Collectors.groupingBy(WmsDataCompareTempEntity::getPkFieldValue));
		for(Map.Entry<String, List<WmsDataCompareTempEntity>> pkFieldValueImportDataMap : pkFieldValueImportDataMaps.entrySet()) {
			List<WmsDataCompareTempEntity> value = pkFieldValueImportDataMap.getValue();
			BigDecimal count = value.stream().map(p -> {
				Map<String , String> parseObject = JSON.parseObject(p.getImportDataJson() , Map.class);
				return getBigDecimalValue(parseObject.get(importField));
			}).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
			
			Map<String , String> oneMap = JSON.parseObject(value.get(0).getImportDataJson() , Map.class);
			oneMap.put(importField, count.toString());
			importPkDataMaps.put(pkFieldValueImportDataMap.getKey(), oneMap);
		}
		//数据已放入importPkDataMaps，置空释放内存
		pkFieldValueImportDataMaps = new HashMap<>();

		List<WmsDataCompareTempEntity> insertTempEntity = new ArrayList<>();
		WmsDataCompareTempEntity addWmsDataCompareTempEntity = null;
		for(Map.Entry<String, Map<String, String>> systemPkDataMap : systemPkDataMaps.entrySet()) {
			String pkFieldValue = systemPkDataMap.getKey();
			Map<String, String> systemData = systemPkDataMap.getValue();
			Map<String, String> importData = importPkDataMaps.get(pkFieldValue);

			addWmsDataCompareTempEntity = new WmsDataCompareTempEntity();
			addWmsDataCompareTempEntity.setId(Long.valueOf(sequence.nextId()).toString());
			addWmsDataCompareTempEntity.setTaskId(id);
			addWmsDataCompareTempEntity.setPkFieldValue(pkFieldValue);
			addWmsDataCompareTempEntity.setCompareStatus(WmsDataCompareTempCompareStatusEnum.FINISH.getCode());
			addWmsDataCompareTempEntity.setSystemDataJson(JSON.toJSONString(systemData));
			if(importData != null) {
				addWmsDataCompareTempEntity.setMainDataType(WmsDataCompareTempMainDataTypeEnum.IMPORT.getCode());
				if(this.groupCompare(systemData, systemField, importData, importField)) {
					addWmsDataCompareTempEntity.setCompareResult(WmsDataCompareTempCompareResultEnum.SAME.getCode());
				}else {
					addWmsDataCompareTempEntity.setCompareResult(WmsDataCompareTempCompareResultEnum.DIFF.getCode());
					addWmsDataCompareTempEntity.setDiffFields(JSON.toJSONString(pkImportDataMappingDTOList));
				}
				addWmsDataCompareTempEntity.setImportDataJson(JSON.toJSONString(importData));

				importPkDataMaps.remove(pkFieldValue);
			}else {
				addWmsDataCompareTempEntity.setMainDataType(WmsDataCompareTempMainDataTypeEnum.SYSTEM.getCode());
				addWmsDataCompareTempEntity.setCompareResult(WmsDataCompareTempCompareResultEnum.EXCEED.getCode());
			}
			insertTempEntity.add(addWmsDataCompareTempEntity);
		}

		for(Map.Entry<String, Map<String, String>> importPkDataMap : importPkDataMaps.entrySet()) {
			addWmsDataCompareTempEntity = new WmsDataCompareTempEntity();
			addWmsDataCompareTempEntity.setId(Long.valueOf(sequence.nextId()).toString());
			addWmsDataCompareTempEntity.setTaskId(id);
			addWmsDataCompareTempEntity.setPkFieldValue(importPkDataMap.getKey());
			addWmsDataCompareTempEntity.setCompareStatus(WmsDataCompareTempCompareStatusEnum.FINISH.getCode());
			addWmsDataCompareTempEntity.setMainDataType(WmsDataCompareTempMainDataTypeEnum.IMPORT.getCode());
			addWmsDataCompareTempEntity.setCompareResult(WmsDataCompareTempCompareResultEnum.MISS.getCode());
			addWmsDataCompareTempEntity.setImportDataJson(JSON.toJSONString(importPkDataMap.getValue()));

			insertTempEntity.add(addWmsDataCompareTempEntity);
		}

		wmsDataCompareTaskService.compareGroupSaveTempTable(id, insertTempEntity);
	}

	private void comparePk(WmsDataCompareTaskEntity wmsDataCompareTaskEntity) {
		String id = wmsDataCompareTaskEntity.getId();
		WmsDataCompareBillService wmsDataCompareBillService = wmsDataCompareHandlerFactory.get(wmsDataCompareTaskEntity.getBillType());
		List<Map<String, String>> systemDataMapList = wmsDataCompareBillService.getSystemData(wmsDataCompareTaskEntity);
		List<ImportDataMappingDTO> importDataMappingDTOList = JSON.parseArray(wmsDataCompareTaskEntity.getImportDataMapping() , WmsDataComparePlanDTO.ImportDataMappingDTO.class);
		List<ImportDataMappingDTO> pkImportDataMappingDTOList = importDataMappingDTOList.stream().filter(i -> i.getStatus() != null && i.getStatus()).collect(Collectors.toList());
		List<ImportDataMappingDTO> notPkImportDataMappingDTOList = importDataMappingDTOList.stream().filter(i -> i.getStatus() == null || !i.getStatus()).collect(Collectors.toList());

		Map<String, List<Map<String, String>>> pkFieldValueSystemDataMaps = new HashMap<>();
		Map<String, WmsDataCompareTempEntity> pkFieldValueImportDataMaps = wmsDataCompareTempService.list(Wrappers.<WmsDataCompareTempEntity>lambdaQuery()
				.eq(WmsDataCompareTempEntity::getTaskId, id)
				.eq(WmsDataCompareTempEntity::getCompareStatus, WmsDataCompareTempCompareStatusEnum.WAIT.getCode())
				.eq(WmsDataCompareTempEntity::getMainDataType, WmsDataCompareTempMainDataTypeEnum.IMPORT.getCode()))
				.stream().collect(Collectors.toMap(WmsDataCompareTempEntity::getPkFieldValue , w -> w));

		Sequence sequence = new Sequence();
		List<WmsDataCompareTempEntity> insertTempEntityList = new ArrayList<>();
		List<WmsDataCompareTempEntity> updateTempEntityList = new ArrayList<>();
		if(CollUtil.isNotEmpty(systemDataMapList)) {
			systemDataMapList.forEach(d -> {
				StringBuffer sb = new StringBuffer();
				for(ImportDataMappingDTO importDataMappingDTO : pkImportDataMappingDTOList) {
					String excelValue = d.get(importDataMappingDTO.getSystemField());
					if(excelValue == null) {
						excelValue = "";
					}
					if(CharSequenceUtil.isNotBlank(excelValue)) {
						if(WmsDataCompareTaskClassTypeEnum.DATE.getCode().equals(importDataMappingDTO.getClassType())) {
							excelValue = getDateValue(excelValue);
						}
					}
					sb.append("-");
					sb.append(excelValue);
				}
				if(Stream.of(sb.toString().split("-")).allMatch(StringUtils::isBlank)) {
					return;
				}
				String pkFieldValue = sb.length() > 0 ? sb.toString().substring(1) : "";
				List<Map<String, String>> datas = pkFieldValueSystemDataMaps.get(pkFieldValue);
				if(CollUtil.isEmpty(datas)) {
					datas = new ArrayList<>();
				}
				datas.add(d);
				pkFieldValueSystemDataMaps.put(pkFieldValue, datas);
			});

			//数据已放入pkFieldValueSystemDataMaps，置空释放内存
			systemDataMapList = new ArrayList<>();

			for(Map.Entry<String, List<Map<String, String>>> pkFieldValueSystemDataMap : pkFieldValueSystemDataMaps.entrySet()) {
				String pkFieldValue = pkFieldValueSystemDataMap.getKey();
				List<Map<String, String>> systemDataList = pkFieldValueSystemDataMap.getValue();
				WmsDataCompareTempEntity importDataTemp = pkFieldValueImportDataMaps.get(pkFieldValue);

				List<ImportDataMappingDTO> diff = null;
				if(importDataTemp != null) {
					diff = pkCompare(systemDataList.get(0), JSON.parseObject(importDataTemp.getImportDataJson() , Map.class), notPkImportDataMappingDTOList);
					if(CollUtil.isEmpty(diff)) {
						importDataTemp.setCompareResult(WmsDataCompareTempCompareResultEnum.SAME.getCode());
					}else {
						importDataTemp.setCompareResult(WmsDataCompareTempCompareResultEnum.DIFF.getCode());
						importDataTemp.setDiffFields(JSON.toJSONString(diff));
					}
					importDataTemp.setSystemDataJson(JSON.toJSONString(systemDataList.get(0)));
					importDataTemp.setCompareStatus(WmsDataCompareTempCompareStatusEnum.FINISH.getCode());
					updateTempEntityList.add(importDataTemp);

					systemDataList.remove(0);
				}

				if(CollUtil.isNotEmpty(systemDataList)) {
					for(Map<String, String> systemData : systemDataList) {
						importDataTemp = new WmsDataCompareTempEntity();
						importDataTemp.setId(Long.valueOf(sequence.nextId()).toString());
						importDataTemp.setTaskId(id);
						importDataTemp.setPkFieldValue(pkFieldValue);
						importDataTemp.setMainDataType(WmsDataCompareTempMainDataTypeEnum.SYSTEM.getCode());
						importDataTemp.setCompareStatus(WmsDataCompareTempCompareStatusEnum.FINISH.getCode());
						importDataTemp.setCompareResult(WmsDataCompareTempCompareResultEnum.EXCEED.getCode());
						importDataTemp.setSystemDataJson(JSON.toJSONString(systemData));
						insertTempEntityList.add(importDataTemp);
					}
				}
			}
		}

		wmsDataCompareTaskService.comparePkSaveTempTable(id, insertTempEntityList, updateTempEntityList);
	}
	
	private List<ImportDataMappingDTO> pkCompare(Map<String, String> systemData , Map<String, String> importData , List<ImportDataMappingDTO> notPkImportDataMappingDTOList) {
		List<ImportDataMappingDTO> diff = new ArrayList<>();
		for(ImportDataMappingDTO importDataMappingDTO : notPkImportDataMappingDTOList) {
			String systemValue = systemData.get(importDataMappingDTO.getSystemField());
			String importValue = importData.get(importDataMappingDTO.getImportField());
			if(systemValue == null) {
				systemValue = "";
			}
			if(importValue == null) {
				importValue = "";
			}
			if(!systemValue.equals(importValue)) {
				if(WmsDataCompareTaskClassTypeEnum.INT.getCode().equals(importDataMappingDTO.getClassType())) {
					if(CharSequenceUtil.isNotBlank(systemValue) && CharSequenceUtil.isNotBlank(importValue)) {
						if(getBigDecimalValue(systemValue).compareTo(getBigDecimalValue(importValue)) != 0) {
							diff.add(importDataMappingDTO);
						}
					}
				}else {
					diff.add(importDataMappingDTO);
				}
			}
		}
		return diff;
	}
	
	private boolean groupCompare(Map<String, String> systemData , String systemField , Map<String, String> importData , String importField) {
		String systemValue = systemData.get(systemField);
		String importValue = importData.get(importField);
		if(systemValue == null) {
			systemValue = "";
		}
		if(importValue == null) {
			importValue = "";
		}
		if(!systemValue.equals(importValue)) {
			if(CharSequenceUtil.isNotBlank(systemValue) && CharSequenceUtil.isNotBlank(importValue)) {
				return getBigDecimalValue(systemValue).compareTo(getBigDecimalValue(importValue)) == 0;
			}else {
				return false;
			}
		}else {
			return true;
		}
	}
	
	public void dealUploadResultExcel(String id) {
		log.warn("数据对比开始上传结果：{}" , id);
		WmsDataCompareTaskEntity wmsDataCompareTaskEntity = getById(id);
		if(WmsDataCompareTaskSubStatusEnum.WAIT_UPLOAD.getCode().equals(wmsDataCompareTaskEntity.getSubStatus())) {
			String resultReportUrl = wmsDataCompareTaskEntity.getResultReportUrl();
			Integer resultSameCount = wmsDataCompareTaskEntity.getResultSameCount();
			Integer resultExceedCount = wmsDataCompareTaskEntity.getResultExceedCount();
			Integer resultMissCount = wmsDataCompareTaskEntity.getResultMissCount();
			Integer resultDiffCount = wmsDataCompareTaskEntity.getResultDiffCount();
			if(CharSequenceUtil.isBlank(resultReportUrl)) {
				List<WmsDataCompareTempEntity> wmsDataCompareTempEntityList = wmsDataCompareTempService.lambdaQuery().eq(WmsDataCompareTempEntity::getTaskId, id).list();
				if(CollUtil.isNotEmpty(wmsDataCompareTempEntityList)) {
					resultSameCount = (int)wmsDataCompareTempEntityList.stream().filter(w -> w.getCompareResult().equals(WmsDataCompareTempCompareResultEnum.SAME.getCode())).count();
					resultExceedCount = (int)wmsDataCompareTempEntityList.stream().filter(w -> w.getCompareResult().equals(WmsDataCompareTempCompareResultEnum.EXCEED.getCode())).count();
					resultMissCount = (int)wmsDataCompareTempEntityList.stream().filter(w -> w.getCompareResult().equals(WmsDataCompareTempCompareResultEnum.MISS.getCode())).count();
					resultDiffCount = (int)wmsDataCompareTempEntityList.stream().filter(w -> w.getCompareResult().equals(WmsDataCompareTempCompareResultEnum.DIFF.getCode())).count();
					
					wmsDataCompareTempEntityList.sort((w1 , w2) -> w1.getCompareResult().compareTo(w2.getCompareResult()));
					
					List<ImportDataMappingDTO> importDataMappingDTOList = JSON.parseArray(wmsDataCompareTaskEntity.getImportDataMapping() , WmsDataComparePlanDTO.ImportDataMappingDTO.class);
					
					String fileName = System.getProperty("java.io.tmpdir") + File.separator + "对比结果"+ UUID.fastUUID().toString() +".xlsx";
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
				    	secordHead.add(importDataMappingDTO.getSystemFieldName());
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
					
				    List<String> diffIndexs = new ArrayList<>();
				    int i = headList.size();
				    for(WmsDataCompareTempEntity wmsDataCompareTempEntity : wmsDataCompareTempEntityList) {
				    	String compareResult = wmsDataCompareTempEntity.getCompareResult();
				    	String systemDataJson = wmsDataCompareTempEntity.getSystemDataJson();
				    	String importDataJson = wmsDataCompareTempEntity.getImportDataJson();
				    	if(WmsDataCompareTempCompareResultEnum.DIFF.getCode().equals(compareResult)) {
				    		String diffFields = wmsDataCompareTempEntity.getDiffFields();
				    		if(CharSequenceUtil.isNotBlank(diffFields)) {
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
				    	writeDatas.addAll(getWriteDatas(importDataMappingDTOList, systemDataJson , Boolean.TRUE));
				    	writeDatas.addAll(getWriteDatas(importDataMappingDTOList, importDataJson , Boolean.FALSE));
				    	headList.add(writeDatas);
				    	i = i + 1;
			    	}
				    
				    int size = importDataMappingDTOList.size();
					WriteSheet writeSheet = EasyExcel.writerSheet().registerWriteHandler(new MergeStrategy(size , diffIndexs)).build();
					excelWriter.write(headList, writeSheet);
				    excelWriter.finish();
				    
					File file = new File(fileName);
					resultReportUrl = FastDFSClientUtil.uploadFile(file, fileName);
					file.deleteOnExit();
				}
			}
				wmsDataCompareTaskService.dealFinishData(id , resultReportUrl , resultSameCount, resultExceedCount, resultMissCount, resultDiffCount);
		}
		log.warn("数据对比结束上传结果：{}" , id);
	}
	
	
	@Transactional(rollbackFor = Exception.class , propagation = Propagation.REQUIRES_NEW)
	@Override
	public void dealFinishData(String id , String resultReportUrl , Integer resultSameCount, Integer resultExceedCount, Integer resultMissCount, Integer resultDiffCount) {
		lambdaUpdate().eq(WmsDataCompareTaskEntity::getId, id)
		.eq(WmsDataCompareTaskEntity::getSubStatus, WmsDataCompareTaskSubStatusEnum.WAIT_UPLOAD.getCode())
		.set(WmsDataCompareTaskEntity::getSubStatus, WmsDataCompareTaskSubStatusEnum.FINISH.getCode())
		.set(WmsDataCompareTaskEntity::getStatus, WmsDataCompareTaskStatusEnum.FINISH.getCode())
		.set(WmsDataCompareTaskEntity::getResultReportUrl, resultReportUrl)
		.set(WmsDataCompareTaskEntity::getUpdateTime, LocalDateTime.now())
		.set(WmsDataCompareTaskEntity::getResultSameCount , resultSameCount)
		.set(WmsDataCompareTaskEntity::getResultExceedCount, resultExceedCount)
		.set(WmsDataCompareTaskEntity::getResultMissCount, resultMissCount)
		.set(WmsDataCompareTaskEntity::getResultDiffCount, resultDiffCount)
		.update();
		wmsDataCompareTempService.deleteData(id);
	}
	
	private List<String> getWriteDatas(List<ImportDataMappingDTO> importDataMappingDTOList , String dtoJson , Boolean sysFlag){
		List<String> writeDatas = new ArrayList<>();
    	for(ImportDataMappingDTO importDataMappingDTO : importDataMappingDTOList) {
    		if(CharSequenceUtil.isNotBlank(dtoJson)) {
    			Map<String , String> dtoMap = JSON.parseObject(dtoJson , Map.class);
    			String field = importDataMappingDTO.getImportField();
				if(sysFlag != null && sysFlag) {
					field = importDataMappingDTO.getSystemField();
				}
				writeDatas.add(dtoMap.get(field));
    		}else {
    			writeDatas.add("");
    		}
	    }
    	return writeDatas;
	}
	
	@Transactional(rollbackFor = Exception.class , propagation = Propagation.REQUIRES_NEW)
	@Override
	public void saveTempTable(String importId , List<WmsDataCompareTempEntity> wmsDataCompareTempEntityList , Integer currParseOffset , boolean isLast) {
		wmsDataCompareTempService.batchInsertWmsDataCompareTemp(wmsDataCompareTempEntityList);
		wmsDataCompareImportService.update(Wrappers.<WmsDataCompareImportEntity>lambdaUpdate().eq(WmsDataCompareImportEntity::getId, importId)
				.set(isLast , WmsDataCompareImportEntity::getParseStatus, WmsDataCompareImportParseStatusEnum.FINISH.getCode())
				.set(WmsDataCompareImportEntity::getCurrParseOffset, currParseOffset));
	}
	
	@Transactional(rollbackFor = Exception.class , propagation = Propagation.REQUIRES_NEW)
	@Override
	public void compareGroupSaveTempTable(String taskId , List<WmsDataCompareTempEntity> wmsDataCompareTempEntityList) {
		wmsDataCompareTempService.deleteData(taskId);
		List<List<WmsDataCompareTempEntity>> partition = Lists.partition(wmsDataCompareTempEntityList, INSERT_SIZE);
		for(List<WmsDataCompareTempEntity> p : partition) {
			wmsDataCompareTempService.batchInsertWmsDataCompareTemp(p);
		}
	}

	@Transactional(rollbackFor = Exception.class , propagation = Propagation.REQUIRES_NEW)
	@Override
	public void comparePkSaveTempTable(String taskId , List<WmsDataCompareTempEntity> insertTempEntityList , List<WmsDataCompareTempEntity> updateTempEntityList) {
		List<List<WmsDataCompareTempEntity>> partition = Lists.partition(insertTempEntityList, INSERT_SIZE);
		for(List<WmsDataCompareTempEntity> p : partition) {
			wmsDataCompareTempService.batchInsertWmsDataCompareTemp(p);
		}
		wmsDataCompareTempService.updateBatchById(updateTempEntityList);
		wmsDataCompareTempService.lambdaUpdate().eq(WmsDataCompareTempEntity::getTaskId, taskId)
				.eq(WmsDataCompareTempEntity::getCompareStatus, WmsDataCompareTempCompareStatusEnum.WAIT.getCode())
				.set(WmsDataCompareTempEntity::getCompareResult, WmsDataCompareTempCompareResultEnum.MISS.getCode())
				.set(WmsDataCompareTempEntity::getCompareStatus, WmsDataCompareTempCompareStatusEnum.FINISH.getCode()).update();
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
			if(CharSequenceUtil.isNotBlank(billType)) {
				l.setBillTypeName(WmsDataCompareTaskBillTypeEnum.getName(billType));
			}
			String status = l.getStatus();
			if(CharSequenceUtil.isNotBlank(status)) {
				l.setStatusName(WmsDataCompareTaskStatusEnum.getName(status));
			}
		});
	}

}
