package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpCfgMqEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpCfgMqDTO;
import com.common.business.vo.PagingVO;
import com.common.business.dto.ApproveDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 输入输出mq信息 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
public interface DmpCfgMqService extends SuperService<DmpCfgMqEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpCfgMqDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    Boolean update(DmpCfgMqDTO.UpdateDTO dto);


    /**
    * 详情
    * @author shukai
    * @date: 2026-03-17
    * @param id
    * @return
    */
    DmpCfgMqDTO.ViewDTO view(String id);
}
