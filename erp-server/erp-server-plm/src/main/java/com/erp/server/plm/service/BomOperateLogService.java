package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.entity.BomOperateLogEntity;
import com.erp.model.plm.vo.BomOperateVO;

import java.util.List;

/**
 * bom 操作记录日志表(BomOperateLog)表服务接口
 *
 * @author yl
 * @since 2023-01-09 11:45:25
 */
public interface BomOperateLogService  extends IService<BomOperateLogEntity> {


    void saveOperate(String bomId, String operateType, String content);

    List<BomOperateVO> getOperateLog(String id);

    PagingVO<List<BomOperateVO>> paging(PagingDTO<BaseIdDTO> dto);
}
