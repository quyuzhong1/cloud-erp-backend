package com.erp.server.wms.service.impl;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.WmsDataComparePlanDTO;
import com.erp.model.wms.dto.WmsDataComparePlanDTO.ImportDataMappingDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO.SetNextDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO.SetNextViewDTO;
import com.erp.model.wms.entity.DictBasicEntity;
import com.erp.model.wms.entity.WmsDataCompareImportEntity;
import com.erp.model.wms.entity.WmsDataCompareTaskEntity;
import com.erp.model.wms.enums.WmsDataCompareImportParseStatusEnum;
import com.erp.server.wms.config.WmsDataCompareHandlerFactory;
import com.erp.server.wms.listener.WmsDataCompareExcelListener;
import com.erp.server.wms.mapper.WmsDataCompareTaskMapper;
import com.erp.server.wms.service.CommonService;
import com.erp.server.wms.service.DictBasicService;
import com.erp.server.wms.service.FbaShipmentService;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.OverseasWarehouseInboundService;
import com.erp.server.wms.service.SoOutstockService;
import com.erp.server.wms.service.WmsDataCompareImportService;
import com.erp.server.wms.service.WmsDataCompareTaskService;
import com.erp.server.wms.utils.WmsDataCompareUtils;
import com.erp.server.wms.utils.WmsDataCompareUtils.WmsDataCompareExcelDto;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
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
    private SoOutstockService soOutstockService;
    @Autowired
    private FbaShipmentService fbaShipmentService;
    @Autowired
    private OverseasWarehouseInboundService overseasWarehouseInboundService;
    @Autowired
    private WmsDataCompareHandlerFactory wmsDataCompareHandlerFactory;
    @Autowired
    private DictBasicService dictBasicService;

    @GlobalTransactional(rollbackFor = Exception.class)
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
        }
        WmsDataCompareExcelDto wmsDataCompareExcelDto = WmsDataCompareUtils.getWmsDataCompareExcelDto(excelFiles);
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
    }

	@Override
	public String downloadSystemData(BaseIdDTO dto) {
		WmsDataCompareTaskEntity wmsDataCompareTaskEntity = this.getById(dto.getId());
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
		
		importDataMappingDTOList = importDataMappingDTOList.stream().filter(i -> i.getPkFlag() != null && i.getPkFlag()).collect(Collectors.toList());
		if(CollUtil.isNotEmpty(importDataMappingDTOList)) {
			if(importDataMappingDTOList.stream().anyMatch(i -> StringUtils.isBlank(i.getImportField()) || StringUtils.isBlank(i.getImportField()))) {
				setNextViewDTO.setErrMessageList(Collections.singletonList("唯一键已设置，但系统数据字段或导入数据字段属性未设置映射"));
				return setNextViewDTO;
			}
			String id = dto.getId();
			List<WmsDataCompareImportEntity> wmsDataCompareImportEntityList = wmsDataCompareImportService.list(Wrappers.<WmsDataCompareImportEntity>lambdaQuery()
					.eq(WmsDataCompareImportEntity::getTaskId, id));
			if(CollUtil.isNotEmpty(wmsDataCompareImportEntityList)) {
				WmsDataCompareTaskEntity wmsDataCompareTaskEntity = getById(id);
				
				String billType = wmsDataCompareTaskEntity.getBillType();

				List<String> headFieldList = null;
				List<Map<Integer, String>> allDatas = new ArrayList<>();
				
				for(WmsDataCompareImportEntity wmsDataCompareImportEntity : wmsDataCompareImportEntityList) {
					WmsDataCompareExcelListener wmsDataCompareExcelListener = new WmsDataCompareExcelListener();
					EasyExcel.read(FastDFSClientUtil.getInputStream(wmsDataCompareImportEntity.getFileUrl()), wmsDataCompareExcelListener).sheet().doRead();
					List<String> currHeadFieldList = wmsDataCompareExcelListener.getHeadFieldList();
					if(CollUtil.isEmpty(currHeadFieldList)) {
						continue;
					}
					if(CollUtil.isNotEmpty(wmsDataCompareExcelListener.getDatas())) {
						allDatas.addAll(wmsDataCompareExcelListener.getDatas());
					}
					headFieldList = currHeadFieldList;
				}
				
				List<Integer> excelPkIndexList = new ArrayList<>();
				for(int i = 0; i < headFieldList.size() ; i++) {
					String headField = headFieldList.get(i);
					if(importDataMappingDTOList.stream().anyMatch(im -> im.getImportField().equals(headField))) {
						excelPkIndexList.add(i);
					}
				}
				excelPkIndexList.sort((e1 , e2) -> e1.compareTo(e2));
				
				Set<String> pkValueSet = new HashSet<>(); 
				Map<String, Integer> pkValueSameCountMaps = new HashMap<>();
				for(Map<Integer, String> data : allDatas) {
					String pkValue = WmsDataCompareUtils.getPkValue(data, excelPkIndexList);
					if(pkValueSet.contains(pkValue)) {
						Integer sameCount = pkValueSameCountMaps.get(pkValue);
						if(sameCount == null) {
							pkValueSameCountMaps.put(pkValue, 1);
						}else {
							pkValueSameCountMaps.put(pkValue, sameCount + 1);
						}
					}else {
						pkValueSet.add(pkValue);
					}
				}
				
				if(pkValueSameCountMaps.size() > 0) {
					List<String> errMessageList = new ArrayList<>();
					for(Map.Entry<String, Integer> pkValueSameCountMap : pkValueSameCountMaps.entrySet()) {
						StringBuffer sb = new StringBuffer();
						sb.append("唯一键值[");
						sb.append(pkValueSameCountMap.getKey());
						sb.append("]，重复[");
						sb.append(pkValueSameCountMap.getValue());
						sb.append("]行");
						errMessageList.add(sb.toString());
					}
					setNextViewDTO.setErrMessageList(errMessageList);
					return setNextViewDTO;
				}
				
				setNextViewDTO.setFlag(true);
				setNextViewDTO.setCode(wmsDataCompareTaskEntity.getCode());
				setNextViewDTO.setName(wmsDataCompareTaskEntity.getName());
				
				List<DictBasicEntity> dictBasicEntityList = dictBasicService.list(Wrappers.<DictBasicEntity>lambdaQuery().eq(DictBasicEntity::getType, "dataCompare" + billType));
				if(CollUtil.isNotEmpty(dictBasicEntityList)) {
					
					this.update(Wrappers.<WmsDataCompareTaskEntity>lambdaUpdate().eq(WmsDataCompareTaskEntity::getId, id)
							.set(WmsDataCompareTaskEntity::getImportDataMapping, importDataMapping));
				}else {
					setNextViewDTO.setErrMessageList(Collections.singletonList("对比单据对比属性未配置，请联系实施人员"));
				}
			}else {
				setNextViewDTO.setErrMessageList(Collections.singletonList("未查询到上传文件，请重新导入数据"));
			}
		}else {
			setNextViewDTO.setErrMessageList(Collections.singletonList("唯一键设置至少开启一个才能创建任务"));
		}
		
		return setNextViewDTO;
	}
}
