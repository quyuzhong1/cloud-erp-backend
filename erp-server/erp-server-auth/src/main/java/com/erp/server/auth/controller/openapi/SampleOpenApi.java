package com.erp.server.auth.controller.openapi;

import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.ExhibitionOrderDTO;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.plm.dto.ProductDetailShowDTO;
import com.erp.model.plm.dto.ProductSkuDTO;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.vo.SysDeptDropDownVO;
import com.erp.model.sys.vo.SysLoginUserVO;
import com.erp.model.wms.dto.*;
import com.erp.rpc.oms.feign.ExhibitionOrderFeign;
import com.erp.rpc.oms.feign.OmsDropDownFeign;
import com.erp.rpc.oms.feign.OmsFeign;
import com.erp.rpc.plm.feign.PlmFeign;
import com.erp.rpc.scm.feign.ScmFeign;
import com.erp.rpc.sys.feign.CfgQueryConditionFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysFeign;
import com.erp.rpc.wms.feign.SampleFeign;
import com.erp.rpc.wms.feign.WmsFeign;
import com.erp.server.auth.config.OpenApi;
import com.erp.server.auth.server.LoginAuthService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 样品管理
 * @author jack
 * @since 2025-09-10
 */
@OpenApi
@Component
public class SampleOpenApi {

    @Resource
    private SampleFeign sampleFeign;

    @Resource
    private ExhibitionOrderFeign exhibitionOrderFeign;

    @Resource
    private OmsDropDownFeign omsDropDownFeign;

    @Resource
    private CfgQueryConditionFeign cfgQueryConditionFeign;

    @Resource
    private SysFeign sysFeign;

    @Resource
    private WmsFeign wmsFeign;

    @Resource
    private PlmFeign plmFeign;

    @Resource
    private OmsFeign omsFeign;

    @Resource
    private ScmFeign scmFeign;

    @Resource
    private LoginAuthService loginAuthService;

    @Resource
    private SysDictFeign sysDictFeign;

    // ==================== 样品借用单相关接口 ====================

    @OpenApi("sampleBorrowInfoAdd")
    public ApiResult<BaseResultDTO.AddDTO> sampleBorrowInfoAdd(@Valid SampleBorrowInfoDTO.AddDTO dto) {
        return sampleFeign.sampleBorrowInfoAdd(dto);
    }

    @OpenApi("sampleBorrowInfoUpdate")
    public ApiResult<?> sampleBorrowInfoUpdate(@Valid SampleBorrowInfoDTO.UpdateDTO dto) {
        return sampleFeign.sampleBorrowInfoUpdate(dto);
    }

    @OpenApi("sampleBorrowInfoTabList")
    public ApiResult<List<SampleBorrowInfoDTO.TabListDTO>> sampleBorrowInfoTabList(PermissionsDTO param) {
        return sampleFeign.sampleBorrowInfoTabList(param);
    }

    @OpenApi("sampleBorrowInfoPaging")
    public ApiResult<PagingVO<SampleBorrowInfoDTO.ListDTO>> sampleBorrowInfoPaging(@Valid PagingDTO<SampleBorrowInfoDTO.PagingParamDTO> dto) {
        return sampleFeign.sampleBorrowInfoPaging(dto);
    }

    @OpenApi("sampleBorrowInfoAddAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> sampleBorrowInfoAddAndSubmit(@Valid SampleBorrowInfoDTO.AddDTO dto) {
        return sampleFeign.sampleBorrowInfoAddAndSubmit(dto);
    }

    @OpenApi("sampleBorrowInfoUpdateAndSubmit")
    public ApiResult<Void> sampleBorrowInfoUpdateAndSubmit(@Valid SampleBorrowInfoDTO.UpdateDTO dto) {
        return sampleFeign.sampleBorrowInfoUpdateAndSubmit(dto);
    }

    @OpenApi("sampleBorrowInfoSubmit")
    public ApiResult<List<BatchResultDTO>> sampleBorrowInfoSubmit(@Valid BaseIdsDTO.IdsDTO dto) {
        return sampleFeign.sampleBorrowInfoSubmit(dto);
    }

    @OpenApi("sampleBorrowInfoApprove")
    public ApiResult<List<BatchResultDTO>> sampleBorrowInfoApprove(@Valid BaseApproveParamDTO dto) {
        return sampleFeign.sampleBorrowInfoApprove(dto);
    }

    @OpenApi("sampleBorrowInfoDisApprove")
    public ApiResult<List<BatchResultDTO>> sampleBorrowInfoDisApprove(@Valid BaseIdsDTO.IdsDTO dto) {
        return sampleFeign.sampleBorrowInfoDisApprove(dto);
    }

    @OpenApi("sampleBorrowInfoDelete")
    public ApiResult<List<BatchResultDTO>> sampleBorrowInfoDelete(@Valid BaseIdsDTO.IdsDTO dto) {
        return sampleFeign.sampleBorrowInfoDelete(dto);
    }

    @OpenApi("sampleBorrowInfoInvalid")
    public ApiResult<List<BatchResultDTO>> sampleBorrowInfoInvalid(@Valid BaseIdsDTO.RemarkDTO dto) {
        return sampleFeign.sampleBorrowInfoInvalid(dto);
    }

    @OpenApi("sampleBorrowInfoCancelProcess")
    public ApiResult<List<BatchResultDTO>> sampleBorrowInfoCancelProcess(@Valid BaseIdsDTO.IdsDTO dto) {
        return sampleFeign.sampleBorrowInfoCancelProcess(dto);
    }

    @OpenApi("sampleBorrowInfoFinishBorrow")
    public ApiResult<List<BatchResultDTO>> sampleBorrowInfoFinishBorrow(@Valid BaseIdsDTO.IdsDTO dto) {
        return sampleFeign.sampleBorrowInfoFinishBorrow(dto);
    }

    @OpenApi("sampleBorrowInfoView")
    public ApiResult<SampleBorrowInfoDTO.ViewDTO> sampleBorrowInfoView(String id) {
        return sampleFeign.sampleBorrowInfoView(id);
    }

    @OpenApi("sampleBorrowInfoDropDown")
    public ApiResult<List<SampleBorrowInfoDTO.DropDownDTO>> sampleBorrowInfoDropDown(SampleBorrowInfoDTO.SelectDTO dto) {
        return sampleFeign.sampleBorrowInfoDropDown(dto);
    }

    @OpenApi("sampleBorrowInfoListSku")
    public ApiResult<PagingVO<SampleBorrowInfoDTO.SkuAvailableQtyDTO>> sampleBorrowInfoListSku(@Valid PagingDTO<SampleBorrowInfoDTO.SearchDTO> dto) {
        return sampleFeign.sampleBorrowInfoListSku(dto);
    }

    // ==================== 样品领用单相关接口 ====================

    @OpenApi("sampleRecipientAdd")
    public ApiResult<BaseResultDTO.AddDTO> sampleRecipientAdd(@Valid SampleRecipientDTO.AddDTO dto) {
        return sampleFeign.sampleRecipientAdd(dto);
    }

    @OpenApi("sampleRecipientUpdate")
    public ApiResult<?> sampleRecipientUpdate(@Valid SampleRecipientDTO.UpdateDTO dto) {
        return sampleFeign.sampleRecipientUpdate(dto);
    }

    @OpenApi("sampleRecipientTabList")
    public ApiResult<List<SampleRecipientDTO.TabListDTO>> sampleRecipientTabList(PermissionsDTO param) {
        return sampleFeign.sampleRecipientTabList(param);
    }

    @OpenApi("sampleRecipientPaging")
    public ApiResult<PagingVO<SampleRecipientDTO.ListDTO>> sampleRecipientPaging(@Valid PagingDTO<SampleRecipientDTO.PagingParamDTO> dto) {
        return sampleFeign.sampleRecipientPaging(dto);
    }

    @OpenApi("sampleRecipientAddAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> sampleRecipientAddAndSubmit(@Valid SampleRecipientDTO.AddDTO dto) {
        return sampleFeign.sampleRecipientAddAndSubmit(dto);
    }

    @OpenApi("sampleRecipientUpdateAndSubmit")
    public ApiResult<Void> sampleRecipientUpdateAndSubmit(@Valid SampleRecipientDTO.UpdateDTO dto) {
        return sampleFeign.sampleRecipientUpdateAndSubmit(dto);
    }

    @OpenApi("sampleRecipientSubmit")
    public ApiResult<List<BatchResultDTO>> sampleRecipientSubmit(@Valid BaseIdsDTO.IdsDTO dto) {
        return sampleFeign.sampleRecipientSubmit(dto);
    }

    @OpenApi("sampleRecipientApprove")
    public ApiResult<List<BatchResultDTO>> sampleRecipientApprove(@Valid BaseApproveParamDTO dto) {
        return sampleFeign.sampleRecipientApprove(dto);
    }

    @OpenApi("sampleRecipientDisApprove")
    public ApiResult<List<BatchResultDTO>> sampleRecipientDisApprove(@Valid BaseIdsDTO.IdsDTO dto) {
        return sampleFeign.sampleRecipientDisApprove(dto);
    }

    @OpenApi("sampleRecipientDelete")
    public ApiResult<List<BatchResultDTO>> sampleRecipientDelete(@Valid BaseIdsDTO.IdsDTO dto) {
        return sampleFeign.sampleRecipientDelete(dto);
    }

    @OpenApi("sampleRecipientInvalid")
    public ApiResult<List<BatchResultDTO>> sampleRecipientInvalid(@Valid BaseIdsDTO.RemarkDTO dto) {
        return sampleFeign.sampleRecipientInvalid(dto);
    }

    @OpenApi("sampleRecipientCancelProcess")
    public ApiResult<List<BatchResultDTO>> sampleRecipientCancelProcess(@Valid BaseIdsDTO.IdsDTO dto) {
        return sampleFeign.sampleRecipientCancelProcess(dto);
    }

    @OpenApi("sampleRecipientFinishRecipient")
    public ApiResult<List<BatchResultDTO>> sampleRecipientFinishRecipient(@Valid BaseIdsDTO.IdsDTO dto) {
        return sampleFeign.sampleRecipientFinishRecipient(dto);
    }

    @OpenApi("sampleRecipientView")
    public ApiResult<SampleRecipientDTO.ViewDTO> sampleRecipientView(String id) {
        return sampleFeign.sampleRecipientView(id);
    }

    @OpenApi("sampleRecipientListSku")
    public ApiResult<PagingVO<SampleRecipientDTO.SkuListResponseDTO>> sampleRecipientListSku(@Valid SampleRecipientDTO.SkuListQueryDTO dto) {
        return sampleFeign.sampleRecipientListSku(dto);
    }

    @OpenApi("sampleRecipientSkuAvailableStock")
    public ApiResult<List<SampleRecipientDTO.SkuAvailableStockDTO>> sampleRecipientSkuAvailableStock(@Valid SampleRecipientDTO.SkuAvailableStockQueryDTO dto) {
        return sampleFeign.sampleRecipientSkuAvailableStock(dto);
    }

    @OpenApi("sampleRecipientSkuCost")
    public ApiResult<List<SampleRecipientDTO.SkuDTO>> sampleRecipientSkuCost(@Valid SampleRecipientDTO.SkuCostQueryDTO dto) {
        return sampleFeign.sampleRecipientSkuCost(dto);
    }

    // ==================== 样品退回单相关接口 ====================

    @OpenApi("sampleBackInfoAdd")
    public ApiResult<BaseResultDTO.AddDTO> sampleBackInfoAdd(@Valid SampleBackInfoDTO.AddDTO dto) {
        return sampleFeign.sampleBackInfoAdd(dto);
    }

    @OpenApi("sampleBackInfoUpdate")
    public ApiResult<?> sampleBackInfoUpdate(@Valid SampleBackInfoDTO.UpdateDTO dto) {
        return sampleFeign.sampleBackInfoUpdate(dto);
    }

    @OpenApi("sampleBackInfoTabList")
    public ApiResult<List<SampleBackInfoDTO.TabListDTO>> sampleBackInfoTabList(PermissionsDTO param) {
        return sampleFeign.sampleBackInfoTabList(param);
    }

    @OpenApi("sampleBackInfoPaging")
    public ApiResult<PagingVO<SampleBackInfoDTO.ListDTO>> sampleBackInfoPaging(@Valid PagingDTO<SampleBackInfoDTO.PagingParamDTO> dto) {
        return sampleFeign.sampleBackInfoPaging(dto);
    }

    @OpenApi("sampleBackInfoAddAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> sampleBackInfoAddAndSubmit(@Valid SampleBackInfoDTO.AddDTO dto) {
        return sampleFeign.sampleBackInfoAddAndSubmit(dto);
    }

    @OpenApi("sampleBackInfoUpdateAndSubmit")
    public ApiResult<Void> sampleBackInfoUpdateAndSubmit(@Valid SampleBackInfoDTO.UpdateDTO dto) {
        return sampleFeign.sampleBackInfoUpdateAndSubmit(dto);
    }

    @OpenApi("sampleBackInfoSubmit")
    public ApiResult<List<BatchResultDTO>> sampleBackInfoSubmit(@Valid BaseIdsDTO.IdsDTO dto) {
        return sampleFeign.sampleBackInfoSubmit(dto);
    }

    @OpenApi("sampleBackInfoApprove")
    public ApiResult<List<BatchResultDTO>> sampleBackInfoApprove(@Valid BaseApproveParamDTO dto) {
        return sampleFeign.sampleBackInfoApprove(dto);
    }

    @OpenApi("sampleBackInfoDisApprove")
    public ApiResult<List<BatchResultDTO>> sampleBackInfoDisApprove(@Valid BaseIdsDTO.IdsDTO dto) {
        return sampleFeign.sampleBackInfoDisApprove(dto);
    }

    @OpenApi("sampleBackInfoDelete")
    public ApiResult<List<BatchResultDTO>> sampleBackInfoDelete(@Valid BaseIdsDTO.IdsDTO dto) {
        return sampleFeign.sampleBackInfoDelete(dto);
    }

    @OpenApi("sampleBackInfoInvalid")
    public ApiResult<List<BatchResultDTO>> sampleBackInfoInvalid(@Valid BaseIdsDTO.RemarkDTO dto) {
        return sampleFeign.sampleBackInfoInvalid(dto);
    }

    @OpenApi("sampleBackInfoCancelProcess")
    public ApiResult<List<BatchResultDTO>> sampleBackInfoCancelProcess(@Valid BaseIdsDTO.IdsDTO dto) {
        return sampleFeign.sampleBackInfoCancelProcess(dto);
    }

    @OpenApi("sampleBackInfoView")
    public ApiResult<SampleBackInfoDTO.ViewDTO> sampleBackInfoView(String id) {
        return sampleFeign.sampleBackInfoView(id);
    }

    // ==================== 样品归还单相关接口 ====================

    @OpenApi("sampleReturnInfoAdd")
    public ApiResult<BaseResultDTO.AddDTO> sampleReturnInfoAdd(@Valid SampleReturnInfoDTO.AddDTO dto) {
        return sampleFeign.sampleReturnInfoAdd(dto);
    }

    @OpenApi("sampleReturnInfoUpdate")
    public ApiResult<?> sampleReturnInfoUpdate(@Valid SampleReturnInfoDTO.UpdateDTO dto) {
        return sampleFeign.sampleReturnInfoUpdate(dto);
    }

    @OpenApi("sampleReturnInfoTabList")
    public ApiResult<List<SampleReturnInfoDTO.TabListDTO>> sampleReturnInfoTabList(PermissionsDTO param) {
        return sampleFeign.sampleReturnInfoTabList(param);
    }

    @OpenApi("sampleReturnInfoPaging")
    public ApiResult<PagingVO<SampleReturnInfoDTO.ListDTO>> sampleReturnInfoPaging(@Valid PagingDTO<SampleReturnInfoDTO.PagingParamDTO> dto) {
        return sampleFeign.sampleReturnInfoPaging(dto);
    }

    @OpenApi("sampleReturnInfoAddAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> sampleReturnInfoAddAndSubmit(@Valid SampleReturnInfoDTO.AddDTO dto) {
        return sampleFeign.sampleReturnInfoAddAndSubmit(dto);
    }

    @OpenApi("sampleReturnInfoUpdateAndSubmit")
    public ApiResult<Void> sampleReturnInfoUpdateAndSubmit(@Valid SampleReturnInfoDTO.UpdateDTO dto) {
        return sampleFeign.sampleReturnInfoUpdateAndSubmit(dto);
    }

    @OpenApi("sampleReturnInfoSubmit")
    public ApiResult<List<BatchResultDTO>> sampleReturnInfoSubmit(@Valid BaseIdsDTO.IdsDTO dto) {
        return sampleFeign.sampleReturnInfoSubmit(dto);
    }

    @OpenApi("sampleReturnInfoApprove")
    public ApiResult<List<BatchResultDTO>> sampleReturnInfoApprove(@Valid BaseApproveParamDTO dto) {
        return sampleFeign.sampleReturnInfoApprove(dto);
    }

    @OpenApi("sampleReturnInfoDisApprove")
    public ApiResult<List<BatchResultDTO>> sampleReturnInfoDisApprove(@Valid BaseIdsDTO.IdsDTO dto) {
        return sampleFeign.sampleReturnInfoDisApprove(dto);
    }

    @OpenApi("sampleReturnInfoDelete")
    public ApiResult<List<BatchResultDTO>> sampleReturnInfoDelete(@Valid BaseIdsDTO.IdsDTO dto) {
        return sampleFeign.sampleReturnInfoDelete(dto);
    }

    @OpenApi("sampleReturnInfoInvalid")
    public ApiResult<List<BatchResultDTO>> sampleReturnInfoInvalid(@Valid BaseIdsDTO.RemarkDTO dto) {
        return sampleFeign.sampleReturnInfoInvalid(dto);
    }

    @OpenApi("sampleReturnInfoCancelProcess")
    public ApiResult<List<BatchResultDTO>> sampleReturnInfoCancelProcess(@Valid BaseIdsDTO.IdsDTO dto) {
        return sampleFeign.sampleReturnInfoCancelProcess(dto);
    }

    @OpenApi("sampleReturnInfoView")
    public ApiResult<SampleReturnInfoDTO.ViewDTO> sampleReturnInfoView(String id) {
        return sampleFeign.sampleReturnInfoView(id);
    }

    // ==================== 样品报废单相关接口 ====================

    @OpenApi("sampleScrapInfoAdd")
    public ApiResult<BaseResultDTO.AddDTO> sampleScrapInfoAdd(@Valid SampleScrapInfoDTO.AddDTO dto) {
        return sampleFeign.sampleScrapInfoAdd(dto);
    }

    @OpenApi("sampleScrapInfoUpdate")
    public ApiResult<?> sampleScrapInfoUpdate(@Valid SampleScrapInfoDTO.UpdateDTO dto) {
        return sampleFeign.sampleScrapInfoUpdate(dto);
    }

    @OpenApi("sampleScrapInfoTabList")
    public ApiResult<List<SampleScrapInfoDTO.TabListDTO>> sampleScrapInfoTabList(PermissionsDTO param) {
        return sampleFeign.sampleScrapInfoTabList(param);
    }

    @OpenApi("sampleScrapInfoPaging")
    public ApiResult<PagingVO<SampleScrapInfoDTO.ListDTO>> sampleScrapInfoPaging(@Valid PagingDTO<SampleScrapInfoDTO.PagingParamDTO> dto) {
        return sampleFeign.sampleScrapInfoPaging(dto);
    }

    @OpenApi("sampleScrapInfoAddAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> sampleScrapInfoAddAndSubmit(@Valid SampleScrapInfoDTO.AddDTO dto) {
        return sampleFeign.sampleScrapInfoAddAndSubmit(dto);
    }

    @OpenApi("sampleScrapInfoUpdateAndSubmit")
    public ApiResult<Void> sampleScrapInfoUpdateAndSubmit(@Valid SampleScrapInfoDTO.UpdateDTO dto) {
        return sampleFeign.sampleScrapInfoUpdateAndSubmit(dto);
    }

    @OpenApi("sampleScrapInfoSubmit")
    public ApiResult<List<BatchResultDTO>> sampleScrapInfoSubmit(@Valid BaseIdsDTO.IdsDTO dto) {
        return sampleFeign.sampleScrapInfoSubmit(dto);
    }

    @OpenApi("sampleScrapInfoApprove")
    public ApiResult<List<BatchResultDTO>> sampleScrapInfoApprove(@Valid BaseApproveParamDTO dto) {
        return sampleFeign.sampleScrapInfoApprove(dto);
    }

    @OpenApi("sampleScrapInfoDisApprove")
    public ApiResult<List<BatchResultDTO>> sampleScrapInfoDisApprove(@Valid BaseIdsDTO.IdsDTO dto) {
        return sampleFeign.sampleScrapInfoDisApprove(dto);
    }

    @OpenApi("sampleScrapInfoDelete")
    public ApiResult<List<BatchResultDTO>> sampleScrapInfoDelete(@Valid BaseIdsDTO.IdsDTO dto) {
        return sampleFeign.sampleScrapInfoDelete(dto);
    }

    @OpenApi("sampleScrapInfoInvalid")
    public ApiResult<List<BatchResultDTO>> sampleScrapInfoInvalid(@Valid BaseIdsDTO.RemarkDTO dto) {
        return sampleFeign.sampleScrapInfoInvalid(dto);
    }

    @OpenApi("sampleScrapInfoCancelProcess")
    public ApiResult<List<BatchResultDTO>> sampleScrapInfoCancelProcess(@Valid BaseIdsDTO.IdsDTO dto) {
        return sampleFeign.sampleScrapInfoCancelProcess(dto);
    }

    @OpenApi("sampleScrapInfoView")
    public ApiResult<SampleScrapInfoDTO.ViewDTO> sampleScrapInfoView(String id) {
        return sampleFeign.sampleScrapInfoView(id);
    }

    // ==================== 样品台账相关接口 ====================

    @OpenApi("sampleLedgerTabList")
    public ApiResult<List<SampleLedgerDTO.TabListDTO>> sampleLedgerTabList(PermissionsDTO param) {
        return sampleFeign.sampleLedgerTabList(param);
    }

    @OpenApi("sampleLedgerPaging")
    public ApiResult<PagingVO<SampleLedgerDTO.ListDTO>> sampleLedgerPaging(@Valid PagingDTO<SampleLedgerDTO.PagingParamDTO> dto) {
        return sampleFeign.sampleLedgerPaging(dto);
    }

    @OpenApi("sampleLedgerView")
    public ApiResult<SampleLedgerDTO.ViewDTO> sampleLedgerView(String id) {
        return sampleFeign.sampleLedgerView(id);
    }

    @OpenApi("sampleLedgerFlowList")
    public ApiResult<PagingVO<SampleLedgerFlowDTO.ListDTO>> sampleLedgerFlowList(PagingDTO<SampleLedgerFlowDTO.PagingParamDTO> param) {
        return sampleFeign.sampleLedgerFlowList(param);
    }

    @OpenApi("sampleLedgerFlowDetail")
    public ApiResult<SampleLedgerFlowDTO.ViewDTO> sampleLedgerFlowDetail(String id) {
        return sampleFeign.sampleLedgerFlowDetail(id);
    }

    @OpenApi("sampleLedgerListSku")
    public ApiResult<PagingVO<SampleLedgerDTO.SkuAvailableQtyDTO>> sampleLedgerListSku(@Valid PagingDTO<SampleLedgerDTO.SearchDTO> pagingDTO) {
        return sampleFeign.sampleLedgerListSku(pagingDTO);
    }

    // ==================== 展会订单相关接口 ====================

    @OpenApi("exhibitionOrderListFreezeQtyBySku")
    public List<ExhibitionOrderDTO.FreezeQtyBySku> exhibitionOrderListFreezeQtyBySku(@Valid ExhibitionOrderDTO.SearchDTO dto) {
        return exhibitionOrderFeign.listFreezeQtyBySku(dto);
    }

    @OpenApi("exhibitionOrderGenerateDownstreamByExhibitionOrder")
    public ExhibitionOrderDTO.DownstreamDTO exhibitionOrderGenerateDownstreamByExhibitionOrder(String exhibitionOrderId) {
        return exhibitionOrderFeign.generateDownstreamByExhibitionOrder(exhibitionOrderId);
    }

    // ==================== OMS下拉列表相关接口 ====================

    @OpenApi("omsDropDownTree")
    public List<BaseDropDownDTO.Tree> omsDropDownTree(String key) {
        return omsDropDownFeign.tree(key);
    }

    @OpenApi("omsDropDownGetByTypeAndValue")
    public DictBasicEntity omsDropDownGetByTypeAndValue(@Valid TypeAndValueDTO dto) {
        return omsDropDownFeign.getByTypeAndValue(dto.getType(), dto.getValue());
    }

    @OpenApi("omsDropDownList")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> omsDropDownList(String key) {
        return omsDropDownFeign.list(key);
    }

    @OpenApi("omsDropDownListInternalSalesPlatform")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> omsDropDownListInternalSalesPlatform(String key) {
        return omsDropDownFeign.listInternalSalesPlatform(key);
    }

    // ==================== 查询条件配置相关接口 ====================

    @OpenApi("cfgQueryConditionGetQueryCondition")
    public ApiResult<List<CfgQueryConditionDTO.ViewDTO>> cfgQueryConditionGetQueryCondition(String code) {
        return cfgQueryConditionFeign.getQueryCondition(code);
    }

    // ==================== Sys 服务相关接口 ====================

    @OpenApi("sysCompanyList")
    public ApiResult<List<SysAccountingCompanyDTO.ListDTO>> sysCompanyList() {
        return sysFeign.companyList();
    }

    @OpenApi("sysDictCountryList")
    public ApiResult<List<DictCountryDTO.ListDTO>> sysDictCountryList() {
        return sysFeign.countryList();
    }

    @OpenApi("sysDictPartitionDropDown")
    public ApiResult<List<DictPartitionDTO.DictDTO>> sysDictPartitionDropDown(DictPartitionDTO.SelectDTO dto) {
        return sysFeign.dictPartitionDropDown(dto);
    }

    @OpenApi("sysDepartmentDropDown")
    public ApiResult<List<SysDeptDropDownVO>> sysDepartmentDropDown() {
        return sysFeign.departmentDropDown();
    }

    // ==================== WMS 服务相关接口 ====================

    @OpenApi("wmsWarehouseList")
    public ApiResult<List<WarehouseDTO.ListDTO>> wmsWarehouseList() {
        return wmsFeign.warehouseList(null);
    }

    @OpenApi("wmsDictList")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> wmsDictList(String key) {
        return wmsFeign.dictList(key);
    }

    @OpenApi("wmsDropDownApproveStatusList")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> wmsDropDownApproveStatusList() {
        return wmsFeign.approveStatusList();
    }

    // ==================== PLM 服务相关接口 ====================

    @OpenApi("plmCommonFindUserList")
    public ApiResult<List<FindUserDTO>> plmCommonFindUserList(BaseSearchDTO dto) {
        return plmFeign.findUserList(dto);
    }

    @OpenApi("plmProductDetailList")
    public ApiResult<PagingVO<ProductDetailShowDTO>> plmProductDetailList(@Valid PagingDTO<ProductSkuDTO> pagingDTO) {
        return plmFeign.productDetailList(pagingDTO);
    }

    // ==================== OMS 服务相关接口 ====================

    @OpenApi("omsCustomerListEnable")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> omsCustomerListEnable(PermissionsDTO dto) {
        return omsFeign.customerListEnable(dto);
    }

    // ==================== SCM 服务相关接口 ====================

    @OpenApi("scmDropDownApproveStatusList")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> scmDropDownApproveStatusList() {
        return scmFeign.approveStatusList();
    }

    @OpenApi("authUserGetUserByToken")
    public ApiResult<SysLoginUserVO> authUserGetUserByToken(String token){
        return ApiResult.success(loginAuthService.getByToken(token));
    }

    // ==================== 示例用户字典相关接口 ====================

    /**
     * 获取示例用户列表
     */
    @OpenApi("sampleUseUserList")
    public List<SampleUseUserDTO.ViewDTO> getSampleUseUserList() {
        return sysDictFeign.getSampleUseUserList();
    }

    /**
     * 根据条件模糊查询示例用户列表
     */
    @OpenApi("sampleUseUserListByCondition")
    public List<SampleUseUserDTO.ViewDTO> getSampleUseUserListByCondition(@Valid SampleUseUserDTO.QueryDTO queryDTO) {
        return sysDictFeign.getSampleUseUserListByCondition(queryDTO);
    }

    /**
     * 新增或更新示例用户
     */
    @OpenApi("sampleUseUserSaveOrUpdate")
    public Boolean sampleUseUserSaveOrUpdate(@Valid ValidList<SampleUseUserDTO.AddOrUpdateDTO> userList) {
        return sysDictFeign.saveOrUpdateSampleUseUser(userList);
    }

    // ==================== 样品调整单相关接口 ====================

    @OpenApi("sampleAdjustmentInfoAdd")
    public ApiResult<BaseResultDTO.AddDTO> sampleAdjustmentInfoAdd(@Valid SampleAdjustmentInfoDTO.AddDTO dto) {
        return sampleFeign.sampleAdjustmentInfoAdd(dto);
    }

    @OpenApi("sampleAdjustmentInfoUpdate")
    public ApiResult<?> sampleAdjustmentInfoUpdate(@Valid SampleAdjustmentInfoDTO.UpdateDTO dto) {
        return sampleFeign.sampleAdjustmentInfoUpdate(dto);
    }

    @OpenApi("sampleAdjustmentInfoTabList")
    public ApiResult<List<SampleAdjustmentInfoDTO.TabListDTO>> sampleAdjustmentInfoTabList(PermissionsDTO param) {
        return sampleFeign.sampleAdjustmentInfoTabList(param);
    }

    @OpenApi("sampleAdjustmentInfoPaging")
    public ApiResult<PagingVO<SampleAdjustmentInfoDTO.ListDTO>> sampleAdjustmentInfoPaging(@Valid PagingDTO<SampleAdjustmentInfoDTO.PagingParamDTO> dto) {
        return sampleFeign.sampleAdjustmentInfoPaging(dto);
    }

    @OpenApi("sampleAdjustmentInfoAddAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> sampleAdjustmentInfoAddAndSubmit(@Valid SampleAdjustmentInfoDTO.AddDTO dto) {
        return sampleFeign.sampleAdjustmentInfoAddAndSubmit(dto);
    }

    @OpenApi("sampleAdjustmentInfoUpdateAndSubmit")
    public ApiResult<Void> sampleAdjustmentInfoUpdateAndSubmit(@Valid SampleAdjustmentInfoDTO.UpdateDTO dto) {
        return sampleFeign.sampleAdjustmentInfoUpdateAndSubmit(dto);
    }

    @OpenApi("sampleAdjustmentInfoSubmit")
    public ApiResult<List<BatchResultDTO>> sampleAdjustmentInfoSubmit(@Valid BaseIdsDTO.IdsDTO dto) {
        return sampleFeign.sampleAdjustmentInfoSubmit(dto);
    }

    @OpenApi("sampleAdjustmentInfoApprove")
    public ApiResult<List<BatchResultDTO>> sampleAdjustmentInfoApprove(@Valid BaseApproveParamDTO dto) {
        return sampleFeign.sampleAdjustmentInfoApprove(dto);
    }

    @OpenApi("sampleAdjustmentInfoDisApprove")
    public ApiResult<List<BatchResultDTO>> sampleAdjustmentInfoDisApprove(@Valid BaseIdsDTO.IdsDTO dto) {
        return sampleFeign.sampleAdjustmentInfoDisApprove(dto);
    }

    @OpenApi("sampleAdjustmentInfoDelete")
    public ApiResult<List<BatchResultDTO>> sampleAdjustmentInfoDelete(@Valid BaseIdsDTO.IdsDTO dto) {
        return sampleFeign.sampleAdjustmentInfoDelete(dto);
    }

    @OpenApi("sampleAdjustmentInfoInvalid")
    public ApiResult<List<BatchResultDTO>> sampleAdjustmentInfoInvalid(@Valid BaseIdsDTO.RemarkDTO dto) {
        return sampleFeign.sampleAdjustmentInfoInvalid(dto);
    }

    @OpenApi("sampleAdjustmentInfoCancelProcess")
    public ApiResult<List<BatchResultDTO>> sampleAdjustmentInfoCancelProcess(@Valid BaseIdsDTO.IdsDTO dto) {
        return sampleFeign.sampleAdjustmentInfoCancelProcess(dto);
    }

    @OpenApi("sampleAdjustmentInfoView")
    public ApiResult<SampleAdjustmentInfoDTO.ViewDTO> sampleAdjustmentInfoView(String id) {
        return sampleFeign.sampleAdjustmentInfoView(id);
    }


    // ==================== 样品转移单相关接口 ====================

    @OpenApi("sampleTransferInfoAdd")
    public ApiResult<BaseResultDTO.AddDTO> sampleTransferInfoAdd(@Valid SampleTransferInfoDTO.AddDTO dto) {
        return sampleFeign.sampleTransferInfoAdd(dto);
    }

    @OpenApi("sampleTransferInfoUpdate")
    public ApiResult<?> sampleTransferInfoUpdate(@Valid SampleTransferInfoDTO.UpdateDTO dto) {
        return sampleFeign.sampleTransferInfoUpdate(dto);
    }

    @OpenApi("sampleTransferInfoTabList")
    public ApiResult<List<SampleTransferInfoDTO.TabListDTO>> sampleTransferInfoTabList(PermissionsDTO param) {
        return sampleFeign.sampleTransferInfoTabList(param);
    }

    @OpenApi("sampleTransferInfoPaging")
    public ApiResult<PagingVO<SampleTransferInfoDTO.ListDTO>> sampleTransferInfoPaging(@Valid PagingDTO<SampleTransferInfoDTO.PagingParamDTO> dto) {
        return sampleFeign.sampleTransferInfoPaging(dto);
    }

    @OpenApi("sampleTransferInfoAddAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> sampleTransferInfoAddAndSubmit(@Valid SampleTransferInfoDTO.AddDTO dto) {
        return sampleFeign.sampleTransferInfoAddAndSubmit(dto);
    }

    @OpenApi("sampleTransferInfoUpdateAndSubmit")
    public ApiResult<Void> sampleTransferInfoUpdateAndSubmit(@Valid SampleTransferInfoDTO.UpdateDTO dto) {
        return sampleFeign.sampleTransferInfoUpdateAndSubmit(dto);
    }

    @OpenApi("sampleTransferInfoSubmit")
    public ApiResult<List<BatchResultDTO>> sampleTransferInfoSubmit(@Valid BaseIdsDTO.IdsDTO dto) {
        return sampleFeign.sampleTransferInfoSubmit(dto);
    }

    @OpenApi("sampleTransferInfoApprove")
    public ApiResult<List<BatchResultDTO>> sampleTransferInfoApprove(@Valid BaseApproveParamDTO dto) {
        return sampleFeign.sampleTransferInfoApprove(dto);
    }

    @OpenApi("sampleTransferInfoDisApprove")
    public ApiResult<List<BatchResultDTO>> sampleTransferInfoDisApprove(@Valid BaseIdsDTO.IdsDTO dto) {
        return sampleFeign.sampleTransferInfoDisApprove(dto);
    }

    @OpenApi("sampleTransferInfoDelete")
    public ApiResult<List<BatchResultDTO>> sampleTransferInfoDelete(@Valid BaseIdsDTO.IdsDTO dto) {
        return sampleFeign.sampleTransferInfoDelete(dto);
    }

    @OpenApi("sampleTransferInfoInvalid")
    public ApiResult<List<BatchResultDTO>> sampleTransferInfoInvalid(@Valid BaseIdsDTO.RemarkDTO dto) {
        return sampleFeign.sampleTransferInfoInvalid(dto);
    }

    @OpenApi("sampleTransferInfoCancelProcess")
    public ApiResult<List<BatchResultDTO>> sampleTransferInfoCancelProcess(@Valid BaseIdsDTO.IdsDTO dto) {
        return sampleFeign.sampleTransferInfoCancelProcess(dto);
    }

    @OpenApi("sampleTransferInfoView")
    public ApiResult<SampleTransferInfoDTO.ViewDTO> sampleTransferInfoView(String id) {
        return sampleFeign.sampleTransferInfoView(id);
    }


}
