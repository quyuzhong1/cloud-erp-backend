package com.erp.server.plm.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.AddBomDTO;
import com.erp.model.plm.dto.BomDTO;
import com.erp.model.plm.dto.SearchPagingDTO;
import com.erp.model.plm.dto.UpdateBomDTO;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.vo.BomPagingVO;
import com.erp.model.plm.vo.BomVO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * bom 信息表(BomInfo)表服务接口
 *
 * @author yl
 * @since 2023-01-09 11:45:28
 */
public interface BomInfoService  extends IService<BomInfoEntity> {


    Boolean insert(AddBomDTO dto);

    PagingVO<List<BomPagingVO>> paging(PagingDTO<SearchPagingDTO> dto);

    BomDTO getBomDetails(String bomId);

    Boolean edit(UpdateBomDTO dto);

    Boolean deleteById(String bomId);

    Boolean submitAudit(String bomId);

    Boolean restartAudit(String bomId);

    Boolean freeze(String bomId);

    Boolean defrost(String bomId);

    Boolean scrap(String bomId);

    Boolean recover(String id);

    Boolean startChange(UpdateBomDTO dto);

    List<BomVO> getByIds(List<String> bomIdList);

    List<BaseIdDTO> getBomInfo(String  searchKeyword);

    Boolean removeArchive(String id);

    void checkIfChange(String sourceId);

    void updateState(String sourceId, Integer state);

    void exportExcel(SearchPagingDTO dto, HttpServletResponse response);
}
