package com.erp.server.oms.service;

import cn.hutool.json.JSONObject;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.ReportDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.SoB2cCategoryTypeEnum;
import com.erp.model.oms.enums.SoB2cInvalidTypeEnum;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * B2C销售订单表 服务类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
public interface SoB2cService extends SuperService<SoB2cEntity> {

      /**
      * 分页列表查询
      * @author Will
      * @date: 2023-08-18
      * @param pagingParamDTO
      * @return PagingVO<SoB2cDTO.ListDTO>>
      */
      PagingVO<SoB2cDTO.ListDTO> paging(PagingDTO<SoB2cDTO.PagingParamDTO> pagingParamDTO);

     /**
     * 状态统计
     * @author Will
     * @date: 2023-08-18
     * @param dto
     * @return List<SoB2cDTO.TabListDTO>>
     */
     List<SoB2cDTO.TabListDTO> tabList(PermissionsDTO dto);

     /**
     * 详情
     * @author Will
     * @date: 2023-08-18
     * @param id
     * @return
     */
     SoB2cDTO.ViewDTO view(String id);

     /**
     * 新增
     * @author Will
     * @date: 2023-08-18
     * @param dto
     * @return
     */
     SoB2cEntity add(SoB2cDTO.AddDTO dto,String code);

     /**
     * 修改
     * @author Will
     * @date: 2023-08-18
     * @param dto
     * @return
     */
    BatchResultDTO update(SoB2cDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author Will
     * @date: 2023-08-18
     * @param id
     * @return
     */
    BatchResultDTO submit(String id,Boolean isProcess);

    /**
    * 审核
    * @author Will
    * @date: 2023-08-18
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 作废
    * @author Will
    * @date: 2023-08-18
    * @param id
    * @param remark
    * @return
    */
    BatchResultDTO invalid(String id, String remark, SoB2cInvalidTypeEnum soB2cInvalidTypeEnum);

    /**
     * @description: 反作废
     * @author Will
     * @date: 2023/8/18 15:33
     * @param id
     * @param soB2cInvalidTypeEnum
     * @return BatchResultDTO
     */
    BatchResultDTO unInvalid(String id, SoB2cInvalidTypeEnum soB2cInvalidTypeEnum);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, SoB2cEntity entity);
    /**
     * 修改订单备注
     * @author Will
     * @date: 2023/8/18 15:38
     * @param id
     * @param remark
     * @return BatchResultDTO
     */
    BatchResultDTO updateRemark(String id, String remark);
    /**
     * 编辑分类
     * @author Will
     * @date: 2023/8/18 15:47
     * @param id
     * @param typeEnum
     * @param categoryIdList
     * @return BatchResultDTO
     */
    BatchResultDTO updateCategory(String id, SoB2cCategoryTypeEnum typeEnum, List<String> categoryIdList);
    /**
     * @description: 订单配货数据显示
     * @author Will
     * @date: 2023/8/18 16:35
     * @param dto 
     * @return List<ViewSoB2cDistributionDTO> 
     */
    List<SoB2cDTO.ViewSoB2cDistributionDTO> viewSoB2cDistribution(BaseIdsDTO.IdsDTO dto);
    /**
     * @description: 订单配货保存
     * @author Will
     * @date: 2023/8/18 16:42
     * @param id
     * @param dto
     * @return BatchResultDTO
     */
    BatchResultDTO saveSoB2cDistribution(String id, SoB2cDTO.SaveSoB2cDistributionDTO dto);
    /**
     * @description: 获取物流单号
     * @author Will
     * @date: 2023/8/18 16:47
     * @param id
     * @param isDelivery
     * @return BatchResultDTO
     */
    BatchResultDTO getLogisticsCode(String id, Boolean isDelivery);
    /**
     * @description: 提交发货
     * @author Will
     * @date: 2023/8/18 16:49
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO submitDelivery(String id);
    /**
     * @description: 发货拦截
     * @author Will
     * @date: 2023/8/18 16:52
     * @param id
     * @param remark
     * @return BatchResultDTO
     */
    BatchResultDTO deliveryIntercept(String id, String remark);
    /**
     * @description: 取消发货拦截
     * @author Will
     * @date: 2023/8/18 16:53
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO cancelDeliveryIntercept(String id);
    /**
     * @description: 合并列表
     * @author Will
     * @date: 2023/8/18 18:32
     * @param dto
     * @return PagingVO<MergeListDTO>
     */
    PagingVO<SoB2cDTO.MergeListDTO> mergePaging(PagingDTO<SoB2cDTO.MergePagingParamDTO> dto);
    /**
     * @description: 合并列表数量
     * @author Will
     * @date: 2023/8/24 16:17
     * @param pagingParamDTO
     * @return Integer
     */
    Integer mergePagingCount(SoB2cDTO.MergePagingParamDTO pagingParamDTO);
    /**
     * @description:合并保存
     * @author Will
     * @date: 2023/8/21 9:02
     * @param ids
     */
    Boolean mergeSave(List<String> ids);
    /**
     * @description: 取消合并
     * @author Will
     * @date: 2023/8/21 9:09
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO cancelMerge(String id);
    /**
     * @description: 拆分显示
     * @author Will
     * @date: 2023/8/21 9:18
     * @param id
     * @return ViewSplitDTO
     */
    SoB2cDTO.ViewSplitDTO viewSplit(String id);
    /**
     * @description: 拆分保存
     * @author Will
     * @date: 2023/8/21 9:23
     * @param dto
     * @return Boolean
     */
    Boolean splitSave(SoB2cDTO.SplitSaveDTO dto);
    /**
     * @description: 取消合并前数据展示
     * @author Will
     * @date: 2023/8/24 11:48
     * @param ids
     * @return List<CheckCancelSplitDTO>
     */
    List<SoB2cDTO.CheckCancelSplitDTO> checkCancelSplit(List<String> ids);
    /**
     * @description: 取消合并
     * @author Will
     * @date: 2023/8/21 9:24
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO cancelSplit(String id);

    /**
     * 匹配审核规则
     */
    Boolean approveRule(String id, List<SoB2cDetailEntity> detailList, JSONObject json);

    /**
     * 匹配配货规则
     */
    Boolean distributionRule(String id, List<SoB2cDetailEntity> detailList, JSONObject json);

    /**
     * 报表管理 销售统计
     * @author yl
     * @date 2023-09-01 11:19
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.ReportDTO.ProductSalesPagingViewDTO>
     */
    PagingVO<ReportDTO.ProductSalesPagingViewDTO> productSalesPaging(PagingDTO<ReportDTO.ProductSalesPagingParamDTO> dto);

    /**
     * 导出 销售统计
     * @author yl
     * @date 2023-09-04 16:39
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean productSalesExport(ReportDTO.ProductSalesPagingParamDTO dto, HttpServletResponse response);
    /**
     * @description: 查看财务信息
     * @author Will
     * @date: 2023/9/6 15:44
     * @param dto
     * @return FinancialInfoDTO
     */
    SoB2cDTO.FinancialInfoDTO getFinancialInfoById(SoB2cDTO.FinancialParamDTO dto);

    /**
     * @description: 查看财务信息
     * @author Will
     * @date: 2023/9/6 15:44
     * @param dto
     * @return FinancialInfoDTO
     */
    SoB2cDTO.FinancialInfoDTO getFinancialInfo(SoB2cDTO.FinancialParamDTO dto,Boolean isAdd);
    /**
     * @description: 标记不合并
     * @author Will
     * @date: 2023/9/11 9:26
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO isNotNeedMerge(String id);

    /**
     * 平台订单更新或保存
     *
     * @Author Jim
     * @since 2023-11-10
     **/
    SoB2cEntity saveOrUpdateEntity(PlatformOrderDTO dto);

    /**
     * 通过哟平台订单ID和类型查询
     *
     * @Author Jim
     * @since 2023-11-10
     **/
    SoB2cEntity getByPlatformInfo(String platformCode, String dictPlatform);

    JSONObject getJson(String id);
}
