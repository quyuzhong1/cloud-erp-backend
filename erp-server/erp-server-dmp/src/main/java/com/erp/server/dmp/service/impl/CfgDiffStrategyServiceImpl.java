package com.erp.server.dmp.service.impl;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.CfgDiffStrategyDTO;
import com.erp.model.dmp.dto.CfgDiffStrategyDTO.AddConditionDTO;
import com.erp.model.dmp.dto.CfgDiffStrategyDTO.AddDetailDTO;
import com.erp.model.dmp.dto.CfgDiffStrategyDTO.BatchOpDTO;
import com.erp.model.dmp.dto.CfgDiffStrategyDTO.ExpotParamDTO;
import com.erp.model.dmp.dto.CfgDiffStrategyDTO.PagingParamDTO;
import com.erp.model.dmp.dto.CfgDiffStrategyDTO.TabListDTO;
import com.erp.model.dmp.dto.CfgDiffStrategyDTO.UpdateDTO;
import com.erp.model.dmp.dto.CfgDiffStrategyDTO.ViewDTO;
import com.erp.model.dmp.entity.CfgDiffStrategyConditionEntity;
import com.erp.model.dmp.entity.CfgDiffStrategyDetailEntity;
import com.erp.model.dmp.entity.CfgDiffStrategyEntity;
import com.erp.model.dmp.entity.DictBasicEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.dmp.mapper.CfgDiffStrategyMapper;
import com.erp.server.dmp.service.CfgDiffStrategyConditionService;
import com.erp.server.dmp.service.CfgDiffStrategyDetailService;
import com.erp.server.dmp.service.CfgDiffStrategyService;
import com.erp.server.dmp.service.DictBasicService;
import com.erp.server.dmp.service.OperateLogService;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 差异策略配置基础信息 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2025-11-11
 */
@Slf4j
@Service
public class CfgDiffStrategyServiceImpl extends SuperServiceImpl<CfgDiffStrategyMapper, CfgDiffStrategyEntity> implements CfgDiffStrategyService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private DictBasicService dictBasicService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private CfgDiffStrategyDetailService cfgDiffStrategyDetailService;
    @Resource
    private CfgDiffStrategyConditionService cfgDiffStrategyConditionService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgDiffStrategyDTO.AddDTO addDTO) {
        CfgDiffStrategyEntity cfgDiffStrategyEntity = new CfgDiffStrategyEntity();
        BeanMapperUtils.copy(addDTO, cfgDiffStrategyEntity);

        // 数据处理
        handleData(cfgDiffStrategyEntity);

        log.info("开始新增差异策略配置基础信息");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_HD);
        cfgDiffStrategyEntity.setCode(code);
        boolean save = super.save(cfgDiffStrategyEntity);
        if(!save) {
            throw new ServiceException("差异策略配置基础信息保存失败");
        }
        this.dealDetail(cfgDiffStrategyEntity.getId(), addDTO);
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "差异策略配置基础信息" , cfgDiffStrategyEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_DIFF_STRATEGY.getName(), cfgDiffStrategyEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgDiffStrategyEntity.getId(), code);
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgDiffStrategyDTO.UpdateDTO addOrUpdateDTO) {
        CfgDiffStrategyEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "差异策略配置基础信息"));
        CfgDiffStrategyEntity cfgDiffStrategyEntity =  BeanMapperUtils.map(CfgDiffStrategyEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgDiffStrategyEntity);
        log.info("编辑 开始修改差异策略配置基础信息数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(cfgDiffStrategyEntity);
        if(!save) {
            throw new ServiceException("差异策略配置基础信息保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）
        this.dealDetail(addOrUpdateDTO.getId(), addOrUpdateDTO);
        // 记录主单操作日志
            log.info("编辑 开始记录差异策略配置基础信息日志数据，单号：【{}】", cfgDiffStrategyEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), old.getCode(), "差异策略配置基础信息");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgDiffStrategyEntity, ModuleTypeEnum.CFG_DIFF_STRATEGY.getName(), cfgDiffStrategyEntity.getId(), msg);
        return Boolean.TRUE;
    }

    private void dealDetail(String id , CfgDiffStrategyDTO.AddDTO addDTO) {
    	List<CfgDiffStrategyDetailEntity> dbDetailList = cfgDiffStrategyDetailService.lambdaQuery().eq(CfgDiffStrategyDetailEntity::getMainId, id).list();
    	if(CollUtil.isNotEmpty(dbDetailList)) {
    		List<String> detailIds = dbDetailList.stream().map(CfgDiffStrategyDetailEntity::getId).collect(Collectors.toList());
    		cfgDiffStrategyDetailService.removeByIds(detailIds);
    		cfgDiffStrategyConditionService.lambdaUpdate().in(CfgDiffStrategyConditionEntity::getDetailId, detailIds).remove();
    	}
    	List<AddDetailDTO> detailList = addDTO.getDetailList();
    	if(CollUtil.isNotEmpty(detailList)) {
    		Set<String> suggestTypeSet = new HashSet<>();
    		for(AddDetailDTO addDetailDTO : detailList) {
    			String suggestType = addDetailDTO.getSuggestType();
				if(!suggestTypeSet.add(suggestType)) {
    				throw new ServiceException(suggestType + "建议处理方式重复");
    			}
    			List<AddConditionDTO> conditionList = addDetailDTO.getConditionList();
    			StringBuilder conditionSql = new StringBuilder();
    			StringBuilder conditionDesc = new StringBuilder();
    			List<CfgDiffStrategyConditionEntity> cfgDiffStrategyConditionEntityList = new ArrayList<>();
    			if(CollUtil.isNotEmpty(conditionList)) {
    				conditionList.sort((c1 , c2) -> c1.getIndex().compareTo(c2.getIndex()));
    				cfgDiffStrategyConditionEntityList = BeanUtil.copyToList(conditionList, CfgDiffStrategyConditionEntity.class);
    				int size = conditionList.size();
    				int i = 1;
    				for(AddConditionDTO addConditionDTO : conditionList) {
    					conditionSql.append(addConditionDTO.getLeftBracket());
    					conditionSql.append(" ");
    					conditionSql.append(addConditionDTO.getField());
    					conditionSql.append(" ");
    					String compare = addConditionDTO.getCompare();
    					QueryConditionEnum queryConditionEnum = QueryConditionEnum.CODE_MAPS.get(compare);
    					String value = addConditionDTO.getValue();
    					if(QueryConditionEnum.STARTS_WITH == queryConditionEnum) {
    						conditionSql.append("like");
    						conditionSql.append(" '");
    						conditionSql.append("%" + value);
    						conditionSql.append("'");
    					}else if(QueryConditionEnum.ENDS_WITH == queryConditionEnum) {
    						conditionSql.append("like");
    						conditionSql.append(" '");
    						conditionSql.append(value + "%");
    						conditionSql.append("'");
    					}else if(QueryConditionEnum.CONTAINS == queryConditionEnum) {
    						conditionSql.append("like");
    						conditionSql.append(" '");
    						conditionSql.append("%" + value + "%");
    						conditionSql.append("'");
    					}else if(QueryConditionEnum.NOT_CONTAINS == queryConditionEnum) {
    						conditionSql.append("not like");
    						conditionSql.append(" '");
    						conditionSql.append("%" + value + "%");
    						conditionSql.append("'");
    					}else if(QueryConditionEnum.IN_LIST == queryConditionEnum || QueryConditionEnum.NOT_IN_LIST == queryConditionEnum) {
    						conditionSql.append(queryConditionEnum.getCode());
    						conditionSql.append(" ");
    						conditionSql.append(Arrays.asList(value.split(",")).stream().collect(Collectors.joining("','", "('", "')")));
    					}else if(QueryConditionEnum.BETWEEN == queryConditionEnum) {
    						conditionSql.append(queryConditionEnum.getCode());
    						String[] split = value.split(",");
    						conditionSql.append(" '");
    						conditionSql.append(split[0]);
    						conditionSql.append("' and '");
    						conditionSql.append(split[1]);
    						conditionSql.append("'");
    					}else {
    						conditionSql.append(queryConditionEnum.getCode());
    						conditionSql.append(" '");
    						conditionSql.append(value);
    						conditionSql.append("'");
    					}
    					conditionSql.append(" ");
    					conditionSql.append(addConditionDTO.getRightBracket());
						if(i != size) {
							conditionSql.append(" ");
	    					conditionSql.append(addConditionDTO.getLogic());
	    					conditionSql.append(" ");
    					}
						
						conditionDesc.append(addConditionDTO.getLeftBracket());
						conditionDesc.append(" ");
						conditionDesc.append(addConditionDTO.getFieldName());
						conditionDesc.append(" ");
						conditionDesc.append(addConditionDTO.getCompareName());
						conditionDesc.append(" ");
						String valueName = addConditionDTO.getValueName();
						if(StringUtils.isBlank(valueName)) {
							valueName = addConditionDTO.getValue();
						}
						conditionDesc.append(valueName);
						conditionDesc.append(" ");
						conditionDesc.append(addConditionDTO.getRightBracket());
						if(i != size) {
							conditionDesc.append(" ");
							conditionDesc.append(addConditionDTO.getLogic());
							conditionDesc.append(" ");
    					}
						
    					i = i + 1;
    				}
    			}
    			CfgDiffStrategyDetailEntity cfgDiffStrategyDetailEntity = BeanUtil.copyProperties(addDetailDTO, CfgDiffStrategyDetailEntity.class);
    			cfgDiffStrategyDetailEntity.setMainId(id);
    			cfgDiffStrategyDetailEntity.setConditionSql(conditionSql.toString());
    			cfgDiffStrategyDetailEntity.setConditionDesc(conditionDesc.toString());
    			
    			cfgDiffStrategyDetailService.save(cfgDiffStrategyDetailEntity);
    			
    			String detailId = cfgDiffStrategyDetailEntity.getId();
    			if(CollUtil.isNotEmpty(cfgDiffStrategyConditionEntityList)) {
    				cfgDiffStrategyConditionEntityList.forEach(c -> c.setDetailId(detailId));
    				cfgDiffStrategyConditionService.saveBatch(cfgDiffStrategyConditionEntityList);
    			}
    		}
    	}
    }
    
    /**
    * 新增修改处理数据
    */
    private void handleData(CfgDiffStrategyEntity cfgDiffStrategyEntity) {
    // TODO 验证数据 & 数据赋值
    }

	@Override
	public List<TabListDTO> tabList(PermissionsDTO dto) {
		List<CfgDiffStrategyEntity> list = lambdaQuery().list();
		TabListDTO ableDto = new TabListDTO();
		ableDto.setTabFlag("able");
		ableDto.setTabFlagName("启用");
		int ableCount = list.stream().filter(CfgDiffStrategyEntity::getStatus).collect(Collectors.toList()).size();
		ableDto.setCount(ableCount);
		TabListDTO disableDto = new TabListDTO();
		disableDto.setTabFlag("disable");
		disableDto.setTabFlagName("停用");
		disableDto.setCount(list.size() - ableCount);
		return Arrays.asList(ableDto , disableDto);
	}

	@Override
	public PagingVO<ViewDTO> paging(PagingDTO<PagingParamDTO> dto) {
		PagingParamDTO params = dto.getParams();
		Page query = new Page(dto.getCurrPage(), dto.getPageSize());
		IPage<CfgDiffStrategyDTO.ViewDTO> pageData = baseMapper.paging(query, params);
		List<ViewDTO> records = pageData.getRecords();
		if(CollUtil.isNotEmpty(records)) {
			List<DictBasicEntity> list = dictBasicService.lambdaQuery().in(DictBasicEntity::getType, Arrays.asList("dictDiffTag" , "dictBillType")).list();
			Map<String, String> diffMap = list.stream().filter(l -> l.getType().equals("dictDiffTag")).collect(Collectors.toMap(DictBasicEntity::getValue, DictBasicEntity::getName , (d1 , d2) -> d1));
			Map<String, String> billTypeMap = list.stream().filter(l -> l.getType().equals("dictBillType")).collect(Collectors.toMap(DictBasicEntity::getValue, DictBasicEntity::getName , (d1 , d2) -> d1));
			records.forEach(r -> {
				r.setDiffTagName(diffMap.get(r.getDiffTag()));
				r.setBillTypeName(billTypeMap.get(r.getBillType()));
				r.setStatusName(r.getStatus() ? "启用" : "停用");
			});
		}
		return new PagingVO<>(pageData);
	}

	@Override
	public Boolean exportExcel(ExpotParamDTO dto) {
		downloadTaskFeign.saveDownloadTask("差异策略配置表", FileTaskEventEnum.EXPORT_CFG_DIFF_STRATEGY.getCode(), dto);
        return Boolean.TRUE;
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void batchOp(BatchOpDTO dto) {
		String opType = dto.getOpType();
		String opTypeName = "";
		List<String> ids = dto.getIds();
        if("delete".equals(opType)) {
        	opTypeName = "批量删除";
        	removeByIds(ids);
        }else if("able".equals(opType)) {
			opTypeName = "批量启用，启用状态由{停用}改为{启用}";
			lambdaUpdate().set(CfgDiffStrategyEntity::getStatus, true).in(CfgDiffStrategyEntity::getId , ids).update();
		}else if("disable".equals(opType)) {
			opTypeName = "批量停用，启用状态由{启用}改为{停用}";
			lambdaUpdate().set(CfgDiffStrategyEntity::getStatus, false).in(CfgDiffStrategyEntity::getId , ids).update();
		}
		
		for(String id : ids) {
			operateLogService.addModuleOperateLog(opTypeName, ModuleTypeEnum.CFG_DIFF_STRATEGY.getCode(), id, "状态变更");
		}

	}

	@Override
	public UpdateDTO view(String id) {
		CfgDiffStrategyEntity cfgDiffStrategyEntity = getById(id);
		UpdateDTO result = BeanUtil.copyProperties(cfgDiffStrategyEntity, UpdateDTO.class);
		List<CfgDiffStrategyDetailEntity> cfgDiffStrategyDetailEntityList = cfgDiffStrategyDetailService.lambdaQuery().eq(CfgDiffStrategyDetailEntity::getMainId, id).list();
		if(CollUtil.isNotEmpty(cfgDiffStrategyDetailEntityList)) {
			List<AddDetailDTO> detailList = BeanUtil.copyToList(cfgDiffStrategyDetailEntityList, AddDetailDTO.class);
			List<CfgDiffStrategyConditionEntity> cfgDiffStrategyConditionEntityList = cfgDiffStrategyConditionService.lambdaQuery().in(CfgDiffStrategyConditionEntity::getDetailId, cfgDiffStrategyDetailEntityList.stream().map(CfgDiffStrategyDetailEntity::getId).collect(Collectors.toList())).list();
			if(CollUtil.isNotEmpty(cfgDiffStrategyConditionEntityList)) {
				Map<String, List<CfgDiffStrategyConditionEntity>> detailMap = cfgDiffStrategyConditionEntityList.stream().collect(Collectors.groupingBy(CfgDiffStrategyConditionEntity::getDetailId));
				for(AddDetailDTO addDetailDTO : detailList) {
					List<CfgDiffStrategyConditionEntity> list = detailMap.get(addDetailDTO.getId());
					if(CollUtil.isNotEmpty(list)) {
						addDetailDTO.setConditionList(BeanUtil.copyToList(list, AddConditionDTO.class));
					}
				}
			}
			result.setDetailList(detailList);
		}
		return result;
	}
}
