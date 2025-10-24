package com.erp.server.sys.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.enums.SystemCodeEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignBuilder;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.DictBasicAllDTO;
import com.erp.model.sys.dto.DictBasicAllDTO.AddDTO;
import com.erp.model.sys.dto.DictBasicAllDTO.BatchOpDTO;
import com.erp.model.sys.dto.DictBasicAllDTO.CommonDTO;
import com.erp.model.sys.dto.DictBasicAllDTO.PagingParamDTO;
import com.erp.model.sys.dto.DictBasicAllDTO.UpdateDTO;
import com.erp.model.sys.dto.DictBasicAllDTO.ViewDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.sys.service.DictBasicAllService;
import com.erp.server.sys.service.OperateLogService;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import io.seata.spring.annotation.GlobalTransactional;

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
	
	@GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
	@Override
	public BaseResultDTO.AddDTO add(AddDTO dto) {
		String systemCode = dto.getSystemCode();
		String type = dto.getType();
		String value = dto.getValue();
		FeignBuilder feignBuilder = FeignBuilder.create(this.getEntityClass(systemCode)).eq("type", type).eq("value", value);
		List list = FeignQuery.list(feignBuilder);
		if(CollUtil.isNotEmpty(list)) {
			throw new ServiceException(type + "类型下的"+ value +"值在" + systemCode + "系统已存在");
		}
		FeignQuery.invoke(this.getServiceClass(systemCode), "save", Arrays.asList(this.getEntityMap(dto)));
		Map<String, Object> beanToMap = BeanUtil.beanToMap(FeignQuery.list(feignBuilder).get(0));
		String id = (String)beanToMap.get("id");
		String msg = CharSequenceUtil.format("用户【{}】新增【{}】系统为【{}】，类型为【{}】，类型名称为【{}】，值为【{}】", 
				UserContext.getDefaultLoginUser().getUserName(), "字典数据" , systemCode , type , dto.getTypeName() , value);
		operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DICT_BASIC.getCode(), id, "新增");
		return new BaseResultDTO.AddDTO(id , id);
	}
	
	private Map<String, Object> getEntityMap(CommonDTO dto){
		return BeanUtil.beanToMap(dto);
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
			throw new ServiceException("未查到到枚举类" + entityClass);
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
	
	@GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
	@Override
	public void update(UpdateDTO dto) {
		String systemCode = dto.getSystemCode();
		String type = dto.getType();
		String value = dto.getValue();
		String id = dto.getId();
		FeignBuilder feignBuilder = FeignBuilder.create(this.getEntityClass(systemCode)).eq("type", type).eq("value", value).ne("id", id);
		List list = FeignQuery.list(feignBuilder);
		if(CollUtil.isNotEmpty(list)) {
			throw new ServiceException(type + "类型下的"+ value +"值在" + systemCode + "系统已存在");
		}
		feignBuilder = FeignBuilder.create(this.getEntityClass(systemCode)).eq("id", id);
		Map<String, Object> old = BeanUtil.beanToMap(FeignQuery.list(feignBuilder).get(0));
		FeignQuery.invoke(this.getServiceClass(systemCode), "updateById", Arrays.asList(this.getEntityMap(dto)));
		String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), id, "字典数据");
		Map<String, Object> newEntity = BeanUtil.beanToMap(FeignQuery.list(feignBuilder).get(0));
        operateLogService.addModuleOperateLogByObj(old, newEntity, ModuleTypeEnum.DICT_BASIC.getCode(), id, msg);
	}

	@GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
	@Override
	public void batchOp(BatchOpDTO dto) {
		String systemCode = dto.getSystemCode();
		String opType = dto.getOpType();
		String opTypeName = "";
		List<String> ids = dto.getIds();
		List<Map<String, Object>> param = new ArrayList<>(ids.size());
		for(String id : ids) {
			Map<String, Object> p = new HashMap<>();
			p.put("id", id);
			if("delete".equals(opType)) {
				p.put("", true);
				opTypeName = "批量删除";
			}else if("able".equals(opType)) {
				p.put("status", true);
				opTypeName = "批量启用，启用状态由{停用}改为{启用}";
			}else if("disable".equals(opType)) {
				p.put("status", false);
				opTypeName = "批量停用，启用状态由{启用}改为{停用}";
			}
		}
		FeignQuery.invoke(this.getServiceClass(systemCode), "updateBatchById", Arrays.asList(param));
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
			if(advanceQueryDTO != null) {
				systemCode = advanceQueryDTO.getValue().toString();
			}
		}
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = FeignQuery.invoke(IPage.class , this.getMapperClass(systemCode), "paging", Arrays.asList(query , params));
        List<ViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        return new PagingVO<>(pageData);
	}

	@Override
	public Boolean exportExcel(PagingDTO<DictBasicAllDTO.ExpotParamDTO> dto) {
		downloadTaskFeign.saveDownloadTask("字典数据表", FileTaskEventEnum.EXPORT_DICT_BASIC_ALL.getCode(), dto);
		return Boolean.TRUE;
	}
}
