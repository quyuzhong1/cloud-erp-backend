package com.erp.server.sys.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.enums.SystemCodeEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.StringUtil;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignBuilder;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.entity.BasicDictEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.DictBasicAllDTO;
import com.erp.model.sys.dto.DictBasicAllDTO.AddDTO;
import com.erp.model.sys.dto.DictBasicAllDTO.BatchOpDTO;
import com.erp.model.sys.dto.DictBasicAllDTO.CommonDTO;
import com.erp.model.sys.dto.DictBasicAllDTO.PagingParamDTO;
import com.erp.model.sys.dto.DictBasicAllDTO.TabListDTO;
import com.erp.model.sys.dto.DictBasicAllDTO.UpdateDTO;
import com.erp.model.sys.dto.DictBasicAllDTO.ViewDTO;
import com.erp.model.sys.entity.DictBasicEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.sys.service.DictBasicAllService;
import com.erp.server.sys.service.OperateLogService;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;

/**
 * <p>
 * 字典表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-26
 */
@Service
public class DictBasicAllServiceImpl implements DictBasicAllService {

	@Resource
    private OperateLogService operateLogService;
	
	@Resource
    private DownloadTaskFeign downloadTaskFeign;
	
	@Autowired
	protected IdentifierGenerator identifierGenerator;
	
	private static final Map<String, Map<String, String>> systemCodeDiffFieldMap = new HashMap<>();
	static {
		Map<String , String> plmMap = new HashMap<>();
		plmMap.put("sort", "order_index");
		systemCodeDiffFieldMap.put(SystemCodeEnum.PLM.getCode(), plmMap);
		
		Map<String , String> srmTmsMrpMap = new HashMap<>();
		srmTmsMrpMap.put("sort", "index");
		srmTmsMrpMap.put("value", "code");
		systemCodeDiffFieldMap.put(SystemCodeEnum.SRM.getCode(), srmTmsMrpMap);
		systemCodeDiffFieldMap.put(SystemCodeEnum.TMS.getCode(), srmTmsMrpMap);
		systemCodeDiffFieldMap.put(SystemCodeEnum.MRP.getCode(), srmTmsMrpMap);
	}
	
    @Transactional(rollbackFor = Exception.class)
	@Override
	public BaseResultDTO.AddDTO add(AddDTO dto) {
		String systemCode = dto.getSystemCode();
		String type = dto.getType();
		String value = dto.getValue();
		FeignBuilder feignBuilder = FeignBuilder.create(this.getEntityClass(systemCode)).eq("type", type).eq(this.convertField(systemCode , "value" , false), value);
		List list = FeignQuery.list(feignBuilder);
		if(CollUtil.isNotEmpty(list)) {
			throw new ServiceException(type + "类型下的"+ value +"值在" + systemCode + "系统已存在");
		}
		Map<String, Object> entityMap = this.getEntityMap(dto);
		entityMap.put("id", identifierGenerator.nextId(new BasicDictEntity()).toString());
		FeignQuery.invoke(this.getServiceClass(systemCode), "saveJsonObject", Arrays.asList(entityMap));
		Map<String, Object> beanToMap = BeanUtil.beanToMap(FeignQuery.list(feignBuilder).get(0));
		String id = (String)beanToMap.get("id");
		String msg = CharSequenceUtil.format("用户【{}】新增【{}】系统为【{}】，类型为【{}】，类型名称为【{}】，值为【{}】，值名称为【{}】，状态为【{}】，排序为【{}】，备注为【{}】", 
				UserContext.getDefaultLoginUser().getUserName(), "字典数据" , systemCode , type , dto.getTypeName() , value , dto.getName() , dto.getStatus() , dto.getSort() , dto.getRemark());
		operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DICT_BASIC.getCode(), id, "新增");
		return new BaseResultDTO.AddDTO(id , id);
	}
	
	private Map<String, Object> getEntityMap(CommonDTO dto){
		Map<String, Object> map = BeanUtil.beanToMap(dto);
		String systemCode = dto.getSystemCode();
		Map<String, Object> newMap = new HashMap<>();
		for(Map.Entry<String, Object> m : map.entrySet()) {
			newMap.put(this.convertField(systemCode, m.getKey(), true), m.getValue());
		}
		return newMap;
	}
	
	private String convertField(String systemCode , String field , boolean isCamel) {
		Map<String, String> diffFieldMap = systemCodeDiffFieldMap.get(systemCode);
		if(diffFieldMap != null) {
			String convertField = diffFieldMap.get(field);
			if(convertField != null) {
				if(isCamel) {
					return StringUtil.convertToCamel(convertField);
				}else {
					return convertField;
				}
			}
		}
		return field;
	}
	
	private String getServiceClass(String systemCode) {
		return "com.erp.server."+ systemCode +".service.impl."+ this.getClassName(systemCode) +"ServiceImpl";
	}
	
	private String getMapperClass(String systemCode) {
		return "com.erp.server."+ systemCode +".mapper."+ this.getClassName(systemCode) +"Mapper";
	}
	
	private Class<?> getEntityClass(String systemCode) {
		String entityClass = "com.erp.model."+ systemCode + ".entity."+ this.getClassName(systemCode) +"Entity";
		Class<?> clazz = null;
		try {
			clazz = Class.forName(entityClass);
		} catch (ClassNotFoundException e) {
			throw new ServiceException("未查到到字典类" + entityClass);
		}
		return clazz;
	}
	
	private String getClassName(String systemCode) {
		String className = "DictBasic";
		if(SystemCodeEnum.PLM.getCode().equals(systemCode)) {
			className = "BasicDict";
		}
		return className;
	}
	
    @Transactional(rollbackFor = Exception.class)
	@Override
	public void update(UpdateDTO dto) {
		String systemCode = dto.getSystemCode();
		String type = dto.getType();
		String value = dto.getValue();
		String id = dto.getId();
		FeignBuilder feignBuilder = FeignBuilder.create(this.getEntityClass(systemCode)).eq("type", type).eq(this.convertField(systemCode , "value" , false), value).ne("id", id);
		List list = FeignQuery.list(feignBuilder);
		if(CollUtil.isNotEmpty(list)) {
			throw new ServiceException(type + "类型下的"+ value +"值在" + systemCode + "系统已存在");
		}
		feignBuilder = FeignBuilder.create(this.getEntityClass(systemCode)).eq("id", id);
		Map<String, Object> old = BeanUtil.beanToMap(FeignQuery.list(feignBuilder).get(0));
		FeignQuery.invoke(this.getServiceClass(systemCode), "updateJsonObject", Arrays.asList(Arrays.asList(this.getEntityMap(dto))));
		String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), id, "字典数据");
		Map<String, Object> newEntity = BeanUtil.beanToMap(FeignQuery.list(feignBuilder).get(0));
        operateLogService.addModuleOperateLogByObj(JSON.parseObject(JSON.toJSONString(old) , DictBasicEntity.class), JSON.parseObject(JSON.toJSONString(newEntity) , DictBasicEntity.class), ModuleTypeEnum.DICT_BASIC.getCode(), id, msg);
	}

    @Transactional(rollbackFor = Exception.class)
	@Override
	public void batchOp(BatchOpDTO dto) {
		String systemCode = dto.getSystemCode();
		String opType = dto.getOpType();
		String opTypeName = "";
		List<String> ids = dto.getIds();
		List<Map<String, Object>> param = new ArrayList<>(ids.size());
        if("delete".equals(opType)) {
        	opTypeName = "批量删除";
        	FeignQuery.invoke(this.getServiceClass(systemCode), "removeByIds", Arrays.asList(ids));
        }else {
        	for(String id : ids) {
    			Map<String, Object> p = new HashMap<>();
    			p.put("id", id);
    			if("able".equals(opType)) {
    				p.put("status", true);
    				opTypeName = "批量启用，启用状态由{停用}改为{启用}";
    			}else if("disable".equals(opType)) {
    				p.put("status", false);
    				opTypeName = "批量停用，启用状态由{启用}改为{停用}";
    			}
    			param.add(p);
    		}
        	FeignQuery.invoke(this.getServiceClass(systemCode), "updateJsonObject", Arrays.asList(param));
        }
		
		for(String id : ids) {
			operateLogService.addModuleOperateLog(opTypeName, ModuleTypeEnum.DICT_BASIC.getCode(), id, "状态变更");
		}
	}

	@Override
	public PagingVO<ViewDTO> paging(PagingDTO<PagingParamDTO> dto) {
		String systemCode = SystemCodeEnum.SYS.getCode();
		PagingParamDTO params = dto.getParams();
		List<AdvanceQueryDTO> advanceQueryDTOList = params.getAdvanceQueryDTOList();
		if(CollUtil.isNotEmpty(advanceQueryDTOList)) {
			AdvanceQueryDTO advanceQueryDTO = advanceQueryDTOList.stream().filter(a -> a.getField().equals("systemCode") && Objects.nonNull(a.getValue())).findFirst().orElse(null);
			if(advanceQueryDTO != null && advanceQueryDTO.getValue() != null && StringUtils.isNotBlank(advanceQueryDTO.getValue().toString())) {
				systemCode = advanceQueryDTO.getValue().toString();
			}
			String finalSystemCode = systemCode;
			advanceQueryDTOList.forEach(a -> {
				String sql = params.getSqlMap().get("default");
				String field = a.getField();
				String tStart = "t.";
				if(field.startsWith(tStart)) {
					params.getSqlMap().put("default", sql.replace(field , tStart + this.convertField(finalSystemCode, field.replace(tStart, ""), false)));
				}
			});
		}
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        Page pageData = FeignQuery.invoke(Page.class , this.getMapperClass(systemCode), "paging", Arrays.asList(query , params));
        List<ViewDTO> list = JSON.parseArray(JSON.toJSONString(pageData.getRecords()), ViewDTO.class);
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        for(ViewDTO l : list) {
        	l.setSystemCode(systemCode);
        	Boolean status = l.getStatus();
        	if(status != null && status) {
        		l.setStatusName("启用");
        	}else {
        		l.setStatusName("停用");
        	}
        }
        pageData.setRecords(list);
        return new PagingVO<>(pageData);
	}

	@Override
	public Boolean exportExcel(PagingDTO<DictBasicAllDTO.ExpotParamDTO> dto) {
		downloadTaskFeign.saveDownloadTask("字典数据表", FileTaskEventEnum.EXPORT_DICT_BASIC_ALL.getCode(), dto);
		return Boolean.TRUE;
	}

	@Override
	public List<TabListDTO> tabList(PagingDTO<DictBasicAllDTO.PagingParamDTO> dto) {
		String systemCode = SystemCodeEnum.SYS.getCode();
		PagingParamDTO params = dto.getParams();
		List<AdvanceQueryDTO> advanceQueryDTOList = params.getAdvanceQueryDTOList();
		if(CollUtil.isNotEmpty(advanceQueryDTOList)) {
			AdvanceQueryDTO advanceQueryDTO = advanceQueryDTOList.stream().filter(a -> a.getField().equals("systemCode") && Objects.nonNull(a.getValue())).findFirst().orElse(null);
			if(advanceQueryDTO != null && advanceQueryDTO.getValue() != null && StringUtils.isNotBlank(advanceQueryDTO.getValue().toString())) {
				systemCode = advanceQueryDTO.getValue().toString();
			}
		}
		TabListDTO ableDto = new TabListDTO();
		ableDto.setTabFlag("able");
		ableDto.setTabFlagName("启用");
		TabListDTO disableDto = new TabListDTO();
		disableDto.setTabFlag("disable");
		disableDto.setTabFlagName("停用");
		
		List<JSONObject> invokeList = FeignQuery.invokeList(JSONObject.class , this.getServiceClass(systemCode), "list");
		if(CollUtil.isNotEmpty(invokeList)) {
			ableDto.setCount(invokeList.stream().filter(j -> j.getBoolean("status")).collect(Collectors.toList()).size());
			disableDto.setCount(invokeList.stream().filter(j -> !j.getBoolean("status")).collect(Collectors.toList()).size());
		}
		
		return Arrays.asList(ableDto , disableDto);
	}
}
