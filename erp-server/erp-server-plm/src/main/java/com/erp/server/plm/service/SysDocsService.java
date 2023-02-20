package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.business.dto.base.BaseSearchDTO;
import com.erp.common.business.dto.base.PagingDTO;
import com.erp.common.business.vo.PagingVO;
import com.erp.model.plm.dto.DocsDTO;
import com.erp.model.plm.dto.DocsShowDTO;
import com.erp.model.plm.dto.StateDTO;
import com.erp.model.plm.entity.SysDocsEntity;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 系统产品文档 服务类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
public interface SysDocsService extends IService<SysDocsEntity> {

    void saveOrUpdateDocs(DocsDTO docsDTO);

    Boolean updateState(StateDTO dto);

    PagingVO<DocsShowDTO> paging(PagingDTO<BaseSearchDTO> dto);

    List<DocsDTO> getDocsNames(Integer yes);

    List<Map<String, Object>> sysDocsNames();

    List<String> getSysDocsName();

}
