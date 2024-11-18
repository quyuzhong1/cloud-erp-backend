package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
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

    PagingVO<BomOperateVO> paging(PagingDTO<BaseIdDTO> dto);
}
