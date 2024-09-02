package com.erp.server.tms.service;

import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.entity.LogisticsSupplierEntity;

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
    BatchResultDTO sync(String id);

    
    /**
     * 导出
     *@parms
     *@return 
     *@author yl
     *@date 2023-11-15
     */
    Boolean export(LogisticsSupplierDTO.ExportDTO dto);
    /**
     * @description: 物流商下拉
     * @author Will
     * @date: 2023/11/17 16:35
     * @return List<DisabledDTO>
     */
    List<BaseDropDownDTO.DisabledDTO> listAll(Boolean filterDisabled);

    /**
     * 更改启用禁用状态
     *
     *@parms dto
     *@return 
     *@author yl
     *@date 2023-11-20
     */
    Boolean updateDisabledBySupplierId(LogisticsSupplierDTO.UpdateDisabledDTO dto);

    /**
     * 获取物流商树形结构
     *@parms
     *@return 
     *@author yl
     *@date 2023-12-05
     */
    List<LogisticsSupplierDTO.ListChildTreeDTO> tree();
    /**
     * @description: 查询渠道
     * @author Will
     * @date: 2024/4/1 11:17
     * @param logisticsSupplierIdList
     * @return List<LogisticsSupplierListDTO>
     */
    List<LogisticsSupplierDTO.LogisticsSupplierListDTO> listLogisticsChannel(List<String> logisticsSupplierIdList);

    /**
     *
     * @param id 主键id
     * @return {@link LogisticsSupplierDTO.ViewDTO}
     */
    LogisticsSupplierDTO.ViewDTO detail(String id);

    List<LogisticsSupplierEntity> listByName(List<String> supplierNameList);

    PagingVO<LogisticsSupplierDTO.PagingViewDTO> exportLogisticsSupplier(PagingDTO<LogisticsSupplierDTO.ExportDTO> dto);

}
