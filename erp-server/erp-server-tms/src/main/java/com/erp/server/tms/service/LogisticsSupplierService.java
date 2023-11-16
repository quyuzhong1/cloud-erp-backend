package com.erp.server.tms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsSupplierDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 物理商表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
public interface LogisticsSupplierService extends SuperService<LogisticsSupplierEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-11-02
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(LogisticsSupplierDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-11-02
    * @param dto
    * @return
    */
    Boolean update(LogisticsSupplierDTO.UpdateDTO dto);


    /**
     * 获取tab页数量统计
     * @author yl
     * @date 2023-11-09 11:02
     * @param dto
     */
    List<LogisticsSupplierDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 物流商分页列表
     * @author yl
     * @date 2023-11-09 14:07
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.tms.dto.LogisticsSupplierDTO.PagingViewDTO>
     */
    PagingVO<LogisticsSupplierDTO.PagingViewDTO> paging(PagingDTO<LogisticsSupplierDTO.PagingParamDTO> dto);

    /**
     * 分页获取渠道相关信息
     *@parms id
     *@return 
     *@author yl
     *@date 2023-11-14
     */
    List<LogisticsSupplierDTO.ChannelViewDTO> listChannelView(String id);

    /**
     * 删除物流商
     *@parms id
     *@return 
     *@author yl
     *@date 2023-11-15
     */
    BatchResultDTO delete(String id);

    /**
     * 物流商物流渠道同步
     *@parms id
     *@return 
     *@author yl
     *@date 2023-11-15
     */
    Boolean sync(String id);

    
    /**
     * 导出
     *@parms
     *@return 
     *@author yl
     *@date 2023-11-15
     */
    Boolean export(LogisticsSupplierDTO.ExportDTO dto, HttpServletResponse httpServletResponse);
}
