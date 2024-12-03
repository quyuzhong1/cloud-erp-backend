package com.erp.server.tms.service;

import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.TransferDeclareDTO;
import com.erp.model.tms.dto.TransferDeclareDeadlineSettingDTO;
import com.erp.model.tms.dto.TransferDeclareDetailDTO;
import com.erp.model.tms.dto.TransferDeclareGenerationSettingDTO;
import com.erp.model.tms.entity.TransferDeclareEntity;

import java.util.List;

/**
 * <p>
 * 中转报关表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
 */
public interface TransferDeclareService extends SuperService<TransferDeclareEntity> {

    /**
     * 分页列表查询
     * @Author Luo_WG
     * @Date 2024/1/20 14:50
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.tms.dto.TransferDeclareDTO.ListDTO>
     **/
    PagingVO<TransferDeclareDTO.ListDTO> paging(PagingDTO<TransferDeclareDTO.PagingParamDTO> dto);

    /**
     * 分页列表tab页
     * @Author Luo_WG
     * @Date 2024/1/20 16:54
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.tms.dto.TransferDeclareDTO.TabListDTO>>
     **/
    List<TransferDeclareDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 新增
    * @author Luo_WG
    * @date: 2024-01-19
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(TransferDeclareDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2024-01-19
    * @param dto
    * @return
    */
    Boolean update(TransferDeclareDTO.UpdateDTO dto);

    /**
     * 根据渠道id查询报关信息
     * @Author Luo_WG
     * @Date 2024/1/19 14:41
     * @param ids
     * @return com.erp.model.tms.entity.TransferDeclareEntity
     **/
    TransferDeclareEntity checkExistByChannelIds(List<String> ids);

    /**
     * 详情
     * @Author Luo_WG
     * @Date 2024/1/23 18:10
     * @param id
     * @return com.erp.model.tms.dto.TransferDeclareDTO.ViewDTO
     **/
    TransferDeclareDTO.ViewDTO view(String id);

    /**
     * 详情明细高级查询
     * @Author Luo_WG
     * @Date 2024/1/24 10:15
     * @param dto
     * @return java.util.List<com.erp.model.tms.dto.TransferDeclareDetailDTO.ViewDTO>
     **/
    List<TransferDeclareDetailDTO.ViewDTO> viewDetailList(TransferDeclareDTO.ViewDetailParamDTO dto);

    /**
     * 报关设置
     * @Author Luo_WG
     * @Date 2024/1/24 15:40
     * @param dtoList
     * @return java.lang.Boolean
     **/
    Boolean forcastSetting(List<TransferDeclareGenerationSettingDTO.AddDTO> dtoList);

    /**
     * 报告设置详情
     * @Author Luo_WG
     * @Date 2024/1/24 17:31
     * @return java.util.List<com.erp.model.tms.dto.TransferDeclareGenerationSettingDTO.ViewDTO>
     **/
    List<TransferDeclareGenerationSettingDTO.ViewDTO> forcastSettingView();

    /**
     * 截单设置
     * @Author Luo_WG
     * @Date 2024/1/24 18:00
     * @param dto
     * @return java.lang.Boolean
     **/
    Boolean deadlineSetting(List<TransferDeclareDeadlineSettingDTO.AddDTO> dto);

    /**
     * 截单设置-详情
     * @Author Luo_WG
     * @Date 2024/1/24 18:16
     * @return java.util.List<com.erp.model.tms.dto.TransferDeclareDeadlineSettingDTO.ViewDTO>
     **/
    List<TransferDeclareDeadlineSettingDTO.ViewDTO> deadlineSettingView();

    /**
     * 删除
     * @Author Luo_WG
     * @Date 2024/1/24 18:20
     * @param ids
     * @return java.lang.Boolean
     **/
    Boolean delete(List<String> ids);

    /**
     * 导出中转报关单
     *
     * @param dto
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2024/1/24 18:45
     **/
    Boolean exportExcel(TransferDeclareDTO.PagingParamDTO dto);

    /**
     * 入库预报
     * @param qtyDTO
     * @return
     */
    List<BatchResultDTO> instockForecast(BaseDTO.QtyDTO qtyDTO);

    /**
     * 报关设置自动生成-定时器调用
     * @Author Luo_WG
     * @Date 2024/1/25 14:52
     * @return void
     **/
    void declareAutoGenerationJob();

    /**
     * @description 根据销售订单id 获取中转报关信息
     * @param soId 销售订单id
     * @author Lambda
     * @return
     * @create 2024-01-26 9:25
     */
    TransferDeclareEntity getBySoId(String soId);

    /**
     * 校验中转服务商是否被使用
     * @Author Luo_WG
     * @Date 2024/1/27 14:02
     * @param supplierId
     * @return java.lang.Boolean
     **/
    Boolean checkExistTransferLogisticsSupplier(String supplierId);

    /**
     * 修改上传状态
     * @Author Luo_WG
     * @Date 2024/1/27 18:45
     * @param id
     * @param status
     * @return java.lang.Boolean
     **/
    Boolean updateUploadStatus(String id, String status);

    /**
     * 查询平台报关订单信息
     * @Author Luo_WG
     * @Date 2024/1/29 9:14
     * @return void
     **/
    void getOrderByCodeJob();

    TransferDeclareDTO.ShippingOrderDTO b2cOrderForecast(TransferDeclareDTO.B2cOrderForecastDTO b2cOrderForecastDTO);

    ApiResult<String> cancelOrderForecast(TransferDeclareDTO.CancelOrderForecastDTO cancelOrderForecastDTO);

    PagingVO<TransferDeclareDTO.ExportListDTO> exportTransferDeclare(PagingDTO<TransferDeclareDTO.PagingParamDTO> dto);
    
    BatchResultDTO pushAllocation(String id , String reportDate);
}
