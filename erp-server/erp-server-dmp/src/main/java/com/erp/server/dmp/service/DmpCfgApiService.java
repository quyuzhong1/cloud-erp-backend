package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpCfgApiDTO;
import com.common.business.vo.PagingVO;
import com.common.business.dto.ApproveDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 输入输出api信息 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
public interface DmpCfgApiService extends SuperService<DmpCfgApiEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpCfgApiDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    Boolean update(DmpCfgApiDTO.UpdateDTO dto);

    /**
    * 详情
    * @author shukai
    * @date: 2026-03-17
    * @param id
    * @return
    */
    DmpCfgApiDTO.ViewDTO view(String id);

}
