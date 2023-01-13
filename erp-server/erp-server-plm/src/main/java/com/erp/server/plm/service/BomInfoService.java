package com.erp.server.plm.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.AddBomDTO;
import com.erp.model.plm.dto.BomDTO;
import com.erp.model.plm.dto.BomSearchPagingDTO;
import com.erp.model.plm.dto.UpdateBomDTO;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.vo.BomPagingVO;

import java.util.List;

/**
 * bom 信息表(BomInfo)表服务接口
 *
 * @author yl
 * @since 2023-01-09 11:45:28
 */
public interface BomInfoService  extends IService<BomInfoEntity> {


    Boolean insert(AddBomDTO dto);

    PagingVO<List<BomPagingVO>> paging(PagingDTO<BomSearchPagingDTO> dto);

    BomDTO getBomDetails(String id);

    Boolean edit(UpdateBomDTO dto);

    Boolean deleteById(String id);
}
