package com.erp.server.sys.service;

import java.util.List;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.DictBasicAllDTO;
import com.erp.model.sys.dto.DictBasicAllDTO.PagingParamDTO;
import com.erp.model.sys.dto.DictBasicAllDTO.ViewDTO;

/**
 * <p>
 * 字典表 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-04-26
 */
public interface DictBasicAllService{
	
	List<DictBasicAllDTO.TabListDTO> tabList(PagingDTO<DictBasicAllDTO.PagingParamDTO> dto);

	BaseResultDTO.AddDTO add(DictBasicAllDTO.AddDTO dto);
	
	void update(DictBasicAllDTO.UpdateDTO dto);
	
	void batchOp(DictBasicAllDTO.BatchOpDTO dto);
	
	PagingVO<ViewDTO> paging(PagingDTO<PagingParamDTO> dto);
	
	Boolean exportExcel(PagingDTO<DictBasicAllDTO.ExpotParamDTO> dto);
}
