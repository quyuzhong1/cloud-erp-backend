package com.erp.server.oms.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.SoInfoEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 销售订单信息 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface SoInfoService extends SuperService<SoInfoEntity> {

    /**
     * 添加销售订单
     * @author yl
     * @date 2023-05-15 16:28
     * @param dto
     * @return java.lang.String
     */
    String add(SoInfoDTO.AddDTO dto);

    
    /**
     * 提交
     * @author yl
     * @date 2023-05-16 14:41
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean submit(List<String> ids);

    
    /**
     * 新增并提交
     * @author yl
     * @date 2023-05-16 14:49
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean addAndSubmit(SoInfoDTO.AddDTO dto);

    
    /**
     * 销售订单详情
     * @author yl
     * @date 2023-05-16 15:01
     * @param id
     * @return com.erp.model.oms.dto.SoInfoDTO.ViewDTO
     */
    SoInfoDTO.ViewDTO view(String id);

    PagingVO<SoInfoDTO.PagingViewDTO> paging(PagingDTO<SoInfoDTO.PagingParamDTO> dto);


    /**
     * 暂存数据
     * @author yl
     * @date 2023-05-17 15:00
     * @param dto
     * @return java.lang.String
     */
    String draft(SoInfoDTO.AddDTO dto);

    
    /**
     * 修改 销售订单
     * @author yl
     * @date 2023-05-17 15:42
     * @param dto
     * @return java.lang.String
     */
    String updateSo(SoInfoDTO.UpdateDTO dto);

    
    /**
     * 修改并提交
     * @author yl
     * @date 2023-05-17 16:43
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateAndSubmit(SoInfoDTO.UpdateDTO dto);

    /**
     * 审核
     * @author yl
     * @date 2023-05-17 16:46
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean approve(BaseApproveParamDTO dto);

    /**
     * 反审核
     * @author yl
     * @date 2023-05-17 16:48
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean disApprove(BaseIdsDTO.IdsDTO dto);

    /**
     * 撤销流程
     * @author yl
     * @date 2023-05-17 16:51
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean cancelProcess(List<String> ids);

    /**
     * 批量删除
     * @author yl
     * @date 2023-05-17 16:53
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean deleteByIds(List<String> ids);

    /**
     * 作废
     * @author yl
     * @date 2023-05-17 17:13
     * @param ids
     * @param remark
     * @return java.lang.Boolean
     */
    Boolean invalid(List<String> ids, String remark);

    /**
     * 导出数据
     * @author yl
     * @date 2023-05-17 18:02
     * @param dto
     * @param response
     * @return java.lang.Boolean
     */
    Boolean exportExcel(SoInfoDTO.ExportDTO dto, HttpServletResponse response);

    /**
     * 获取到已审核的销售订单列表
     * @author yl
     * @date 2023-05-17 18:59
     * @param
     * @return java.util.List<com.common.business.dto.base.BaseIdDTO.CodeDTO>
     */
    List<BaseIdDTO.CodeDTO> listSo();

    /**
     * 根据销售单id
     * 获取到销售订单客户信息
     * @author yl
     * @date 2023-05-17 19:10
     * @param id
     * @return com.erp.model.oms.dto.SoInfoDTO.CustomerDTO
     */
    SoInfoDTO.CustomerDTO getSoCustomer(String id);

    
    /**
     * 获取到合同信息
     * @author yl
     * @date 2023-05-18 14:12
     * @param id
     * @return com.erp.model.oms.dto.SoInfoDTO.ExportPdfDTO
     */
    SoInfoDTO.ExportPdfDTO exportSoContractPdf(String id);

    /**
     * @description: 下推备货申请单数据显示
     * @author Will
     * @date: 2023/5/18 19:58
     * @param ids
     * @return List<ViewGenerateSalesDemandDTO>
     */
    List<SoInfoDTO.ViewGenerateSalesDemandDTO> viewGenerateSalesDemand(List<String> ids);

    /**
     * 根据销售订单id 集合获取到
     * @author yl
     * @date 2023-05-22 10:43
     * @param soIdList
     * @return com.erp.model.oms.dto.SoInfoDTO.CustomerDTO
     */
    List<SoInfoDTO.CustomerDTO> listSoCustomerByIds(List<String> soIdList);

    /**
     * 下推发货通知单\销售出库单-列表查询
     * @Author Luo_WG
     * @Date 2023/5/25 12:02
     * @param ids ids
     * @return java.util.List<com.erp.model.oms.dto.SoInfoDTO.GenerateDeliveryView>
     **/
    List<SoInfoDTO.GenerateDeliveryView> generateDeliveryView(List<String> ids);

    /**
     * 下推销售退货订单-列表查询
     * @Author Luo_WG
     * @Date 2023/5/25 15:17
     * @param ids ids
     * @return java.util.List<com.erp.model.oms.dto.SoInfoDTO.GenerateSoReturnView>
     **/
    List<SoInfoDTO.GenerateSoReturnView> generateSoReturnView(List<String> ids);
  
    /**
     * 更改销售订单金蝶推送的状态
     * @author yl
     * @date 2023-05-31 14:20
     * @param id
     * @param syncKingdeeStatus
     * @param syncKingdeeId
     * @param syncOperate
     * @return 
     */
    Boolean updateSyncKingdeeStatus(String id, String syncKingdeeStatus, String syncKingdeeId,String syncOperate);

}
