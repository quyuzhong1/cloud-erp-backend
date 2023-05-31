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
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-15 16:28
     */
    String add(SoInfoDTO.AddDTO dto);


    /**
     * 提交
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-16 14:41
     */
    Boolean submit(List<String> ids);


    /**
     * 新增并提交
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-16 14:49
     */
    Boolean addAndSubmit(SoInfoDTO.AddDTO dto);


    /**
     * 销售订单详情
     *
     * @param id
     * @return com.erp.model.oms.dto.SoInfoDTO.ViewDTO
     * @author yl
     * @date 2023-05-16 15:01
     */
    SoInfoDTO.ViewDTO view(String id);

    PagingVO<SoInfoDTO.PagingViewDTO> paging(PagingDTO<SoInfoDTO.PagingParamDTO> dto);


    /**
     * 暂存数据
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-17 15:00
     */
    String draft(SoInfoDTO.AddDTO dto);


    /**
     * 修改 销售订单
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-17 15:42
     */
    String updateSo(SoInfoDTO.UpdateDTO dto);


    /**
     * 修改并提交
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-17 16:43
     */
    Boolean updateAndSubmit(SoInfoDTO.UpdateDTO dto);

    /**
     * 审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-17 16:46
     */
    Boolean approve(BaseApproveParamDTO dto);

    /**
     * 反审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-17 16:48
     */
    Boolean disApprove(BaseIdsDTO.IdsDTO dto);

    /**
     * 撤销流程
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-17 16:51
     */
    Boolean cancelProcess(List<String> ids);

    /**
     * 批量删除
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-17 16:53
     */
    Boolean deleteByIds(List<String> ids);

    /**
     * 作废
     *
     * @param ids
     * @param remark
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-17 17:13
     */
    Boolean invalid(List<String> ids, String remark);

    /**
     * 导出数据
     *
     * @param dto
     * @param response
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-17 18:02
     */
    Boolean exportExcel(SoInfoDTO.ExportDTO dto, HttpServletResponse response);

    /**
     * 获取到已审核的销售订单列表
     *
     * @param
     * @return java.util.List<com.common.business.dto.base.BaseIdDTO.CodeDTO>
     * @author yl
     * @date 2023-05-17 18:59
     */
    List<BaseIdDTO.CodeDTO> listSo();

    /**
     * 根据销售单id
     * 获取到销售订单客户信息
     *
     * @param id
     * @return com.erp.model.oms.dto.SoInfoDTO.CustomerDTO
     * @author yl
     * @date 2023-05-17 19:10
     */
    SoInfoDTO.CustomerDTO getSoCustomer(String id);


    /**
     * 获取到合同信息
     *
     * @param id
     * @return com.erp.model.oms.dto.SoInfoDTO.ExportPdfDTO
     * @author yl
     * @date 2023-05-18 14:12
     */
    SoInfoDTO.ExportPdfDTO exportSoContractPdf(String id);

    /**
     * @param ids
     * @return List<ViewGenerateSalesDemandDTO>
     * @description: 下推备货申请单数据显示
     * @author Will
     * @date: 2023/5/18 19:58
     */
    List<SoInfoDTO.ViewGenerateSalesDemandDTO> viewGenerateSalesDemand(List<String> ids);

    /**
     * 根据销售订单id 集合获取到
     *
     * @param soIdList
     * @return com.erp.model.oms.dto.SoInfoDTO.CustomerDTO
     * @author yl
     * @date 2023-05-22 10:43
     */
    List<SoInfoDTO.CustomerDTO> listSoCustomerByIds(List<String> soIdList);

    /**
     * 下推发货通知单\销售出库单-列表查询
     *
     * @param ids ids
     * @return java.util.List<com.erp.model.oms.dto.SoInfoDTO.GenerateDeliveryView>
     * @Author Luo_WG
     * @Date 2023/5/25 12:02
     **/
    List<SoInfoDTO.GenerateDeliveryView> generateDeliveryView(List<String> ids);

    /**
     * 下推销售退货订单-列表查询
     *
     * @param ids ids
     * @return java.util.List<com.erp.model.oms.dto.SoInfoDTO.GenerateSoReturnView>
     * @Author Luo_WG
     * @Date 2023/5/25 15:17
     **/
    List<SoInfoDTO.GenerateSoReturnView> generateSoReturnView(List<String> ids);

    /**
     * 更改销售订单金蝶推送的状态
     *
     * @param id
     * @param syncKingdeeStatus
     * @param syncKingdeeId
     * @param syncOperate
     * @return
     * @author yl
     * @date 2023-05-31 14:20
     */
    Boolean updateSyncKingdeeStatus(String id, String syncKingdeeStatus, String syncKingdeeId, String syncOperate);

    /**
     * 方法说明
     *
     * @param customerIds
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-31 18:17
     */
    Boolean getIsUseCustomer(List<String> customerIds);
}
