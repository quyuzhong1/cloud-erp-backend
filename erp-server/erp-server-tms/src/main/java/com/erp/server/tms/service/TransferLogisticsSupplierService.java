package com.erp.server.tms.service;
import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.dto.TransferLogisticsSupplierDTO;
import com.erp.model.tms.entity.TransferLogisticsSupplierEntity;

import java.util.List;

/**
 * <p>
 * 物理商表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
 */
public interface TransferLogisticsSupplierService extends SuperService<TransferLogisticsSupplierEntity> {

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2024/1/19 15:05
     * @param dto
     * @return com.common.business.dto.base.BaseResultDTO.AddDTO
     **/
    BaseResultDTO.AddDTO add(TransferLogisticsSupplierDTO.AddDTO dto);

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2024/1/19 15:05
     * @param dto
     * @return java.lang.Boolean
     **/
    Boolean update(TransferLogisticsSupplierDTO.UpdateDTO dto);

    /**
     * 获取tab页数量统计
     * @Author Luo_WG
     * @Date 2024/1/19 15:06
     * @param dto
     * @return java.util.List<com.erp.model.tms.dto.TransferLogisticsSupplierDTO.TabListDTO>
     **/
    List<TransferLogisticsSupplierDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 物流商分页列表
     * @Author Luo_WG
     * @Date 2024/1/19 15:06
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.tms.dto.LogisticsSupplierDTO.PagingViewDTO>
     **/
    PagingVO<TransferLogisticsSupplierDTO.PagingViewDTO> paging(PagingDTO<TransferLogisticsSupplierDTO.PagingParamDTO> dto);

    /**
     * 分页获取渠道相关信息
     * @Author Luo_WG
     * @Date 2024/1/19 15:06
     * @param id
     * @param name
     * @return java.util.List<com.erp.model.tms.dto.TransferLogisticsSupplierDTO.ChannelViewDTO>
     **/
    List<TransferLogisticsSupplierDTO.ChannelViewDTO> listChannelView(String id,String name);

    /**
     * 删除物流商
     * @Author Luo_WG
     * @Date 2024/1/19 15:07
     * @param id
     * @return com.common.business.dto.base.BatchResultDTO
     **/
    BatchResultDTO delete(String id);

    /**
     * 物流商物流渠道同步
     * @Author Luo_WG
     * @Date 2024/1/19 15:07
     * @param id
     * @return com.common.business.dto.base.BatchResultDTO
     **/
    BatchResultDTO sync(String id);

    /**
     * 导出
     *
     * @param dto
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2024/1/19 15:07
     **/
    Boolean export(TransferLogisticsSupplierDTO.ExportDTO dto);

    /**
     * 物流商下拉
     * @Author Luo_WG
     * @Date 2024/1/19 15:07
     * @return java.util.List<com.common.business.dto.base.BaseDropDownDTO.DisabledDTO>
     **/
    List<BaseDropDownDTO.DisabledDTO> listAll();

    /**
     * 所有已授权的中转报关服务商下拉
     * @Author Luo_WG
     * @Date 2024/1/29 15:34
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BaseDropDownDTO.DisabledDTO>>
     **/
    List<BaseDropDownDTO.DisabledDTO> listAlreadyAll();

    /**
     * 更改启用禁用状态
     *
     *@parms dto
     *@return
     *@author yl
     *@date 2023-11-20
     */
    Boolean updateDisabledBySupplierId(TransferLogisticsSupplierDTO.UpdateDisabledDTO dto);

    /**
     * 获取物流商树形结构
     * @Author Luo_WG
     * @Date 2024/1/19 15:08
     * @return java.util.List<com.common.business.dto.base.BaseChildDTO.ListChildTreeDTO>
     **/
    List<BaseChildDTO.ListChildTreeDTO> tree();

    /**
     * 获取到所有的授权信息
     * @return
     */
    List<TransferLogisticsSupplierDTO.AuthDTO> listAllAuth();

    /**
     * 获取授权信息列表
     * @description
     * @param transferSupplierIdList
     * @return
     * @date 2024-01-29 9:25
     * @author Lambda
     */
    List<TransferLogisticsSupplierDTO.AuthDTO> listAuthByMainIds(List<String> transferSupplierIdList);

    /**
     * 根据物流商id 获取到对应的渠道的启用禁用列表
     * @param supplierId
     * @return
     */
    List<BaseIdDTO.CodeDTO> listBySupplierId(String supplierId);

    PagingVO<TransferLogisticsSupplierDTO.PagingViewDTO> exportTransferLogisticsSupplier(PagingDTO<TransferLogisticsSupplierDTO.ExportDTO> dto);
}
