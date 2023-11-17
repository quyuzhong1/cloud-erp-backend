package com.erp.server.tms.service;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsBillDetailDTO;

import java.util.List;

/**
 * <p>
 * 物流单明细表 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
 */
public interface LogisticsBillDetailService extends SuperService<LogisticsBillDetailEntity> {

    /**
    * 新增
    * @author lambda
    * @date: 2023-11-09
    * @param dto
    * @return
    */
    Boolean add(LogisticsBillDTO.AddDTO dto, String mainId);

    /**
    * 修改
    * @author lambda
    * @date: 2023-11-09
    * @param dto
    * @return
    */
    Boolean update(LogisticsBillDTO.UpdateDTO dto, String mainId);

    /**
     * 根据主表id查询详情
     * @Author Luo_WG
     * @Date 2023/11/9 19:52
     * @param mainIds
     * @return java.util.List<com.erp.model.tms.entity.LogisticsBillDetailEntity>
     **/
    List<LogisticsBillDetailEntity> listByMainIds(List<String> mainIds);

    /**
     * 根据主表id删除详情
     * @Author Luo_WG
     * @Date 2023/11/9 19:52
     * @param mainIds
     * @return java.lang.Boolean
     **/
    Boolean removeByMainIds(List<String> mainIds);

    /**
     * 更改状态
     *@parms
     *@return 
     *@author yl
     *@date 2023-11-17
     */
    BatchResultDTO updateStatus(String id, String trackStatus);
}
