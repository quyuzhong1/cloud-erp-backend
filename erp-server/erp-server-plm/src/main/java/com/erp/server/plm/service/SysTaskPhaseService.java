package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.business.dto.base.BaseSearchDTO;
import com.erp.common.business.dto.base.PagingDTO;
import com.erp.common.business.vo.PagingVO;
import com.erp.model.plm.dto.BasicDTO;
import com.erp.model.plm.dto.TaskPhaseDTO;
import com.erp.model.plm.dto.UpdateBasicNameDTO;
import com.erp.model.plm.entity.SysTaskPhaseEntity;

import java.util.List;

/**
 * @Classname SysTaskPhase
 * @Description TODO
 * @Date 2022-09-13 16:31
 * @Created by yl
 */
public interface SysTaskPhaseService extends IService<SysTaskPhaseEntity> {
    void updateTaskPhase(UpdateBasicNameDTO dto);

    void batchSaveOrUpdate(List<UpdateBasicNameDTO> list);

    List<TaskPhaseDTO> getSysTaskPhase(List<String> nameList);

    List<SysTaskPhaseEntity> getSysTaskPhaseNames();

    boolean removeSysTaskPhase(String id);

    List<BasicDTO> getSysTaskPhaseList();

    PagingVO<SysTaskPhaseEntity> paging(PagingDTO<BaseSearchDTO> dto);
}
