package com.erp.server.plm.controller;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.workflow.dto.ProcessPassDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProductChangeEntity;
import com.erp.model.plm.vo.ProductChangePagingVO;
import com.erp.model.workflow.vo.ApproveNodeRecordVO;
import com.erp.server.plm.constant.BomConstant;
import com.erp.server.plm.service.ProductChangeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 变更管理
 *
 * @author yl
 * @since 2023-01-11 14:05:03
 */
@RestController
@RequestMapping("plm/change")
@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
@Slf4j
public class ProductChangeController extends BaseController {


    @Resource
    private ProductChangeService productChangeService;


    /**
     * 添加变更
     *
     * @param dto
     * @return
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated AddChangeDTO dto) {
        Boolean result = productChangeService.add(dto);
        return result == true ? success() : failure();
    }


    /**
     * 编辑变更
     *
     * @param dto
     * @return
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated UpdateChangeDTO dto) {
        Boolean result = productChangeService.edit(dto);
        return result == true ? success() : failure();
    }


    /**
     * 变更分页展示
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<List<ProductChangePagingVO>>> queryByPage(@RequestBody @Validated PagingDTO<SearchPagingDTO> dto) {
        PagingVO<List<ProductChangePagingVO>> pagingVO = productChangeService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 变更详情
     *
     * @param dto
     * @return
     */
    @PostMapping("/view")
    public ApiResult details(@RequestBody @Validated BaseIdDTO dto) {

        //获取到变更信息
        ProductChangeEntity changeEntity = productChangeService.getById(dto.getId());
        if (Objects.isNull(changeEntity)) {
            throw new ServiceException(ApiError.ERROR_95105);
        }
        Object object = null;
        String type = changeEntity.getType();
        //对应就是bom
        if (BomConstant.CHANGE_BOM.equals(type)) {
            ProductBomChangeDTO bomChange = productChangeService.getBomDetails(changeEntity);
            object = bomChange;
        }
        //对应sku
        if (BomConstant.CHANGE_SKU.equals(type)) {
            ProductChangeDTO skuChange = productChangeService.skuDetails(changeEntity);
            object = skuChange;
        }
        if (object != null) {
            return success(object);
        }
        return failure();
    }

    /**
     * 作废
     *
     * @param dto
     * @return
     */
    @PostMapping("/cancellation")
    public ApiResult cancellation(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = productChangeService.cancellation(dto.getId());
        return result == true ? success() : failure();
    }

    /**
     * 获取变更的信息
     *
     * @param
     * @return
     */
    @PostMapping("/list")
    public ApiResult<List<ChangeInfoDTO>> list(@RequestBody @Validated ProductChangeListSearchDTO dto) {
        List<ChangeInfoDTO> list = productChangeService.getChangeByType(dto.getType(), dto.getSearchKeyword());
        return success(list);
    }


    /**
     * change 审核 通过
     *
     * @param
     * @return 新增结果
     */
    @PostMapping("/approvalPass")
    public ApiResult approvalPass(@RequestBody @Validated AuditParamDTO dto) {
        productChangeService.approvalPass(dto);
        return success();
    }

    /**
     * change  审核 不通过
     *
     * @param
     * @return 新增结果
     */
    @PostMapping("/approvalNoPass")
    public ApiResult approvalNoPass(@RequestBody @Validated AuditParamDTO dto) {
        productChangeService.approvalNoPass(dto);
        return success();
    }

    /**
     * 重启流程
     *
     * @param dto
     * @return
     */
    @PostMapping("/restartAudit")
    public ApiResult restartAudit(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = productChangeService.restartAudit(dto.getId());
        return result == true ? success() : failure();
    }


    /**
     * 变更流程
     * 监听后 最后通过
     *
     * @return
     */
    @PostMapping("/workflow/pass")
    public ApiResult processPass(@RequestBody ProcessPassDTO dto) {
        productChangeService.processPass(dto);
        return success();
    }

    /**
     * 查询变更字段
     *
     * @param dto
     * @return
     */
    @PostMapping("/listChangeField")
    public ApiResult<List<String>> listChangeField(@RequestBody @Validated BaseIdDTO dto) {
        List<String> list = productChangeService.listChangeField(dto.getId());
        return success(list);
    }


    /**
     * bom 审核情况
     *
     * @return
     */
    @PostMapping("/auditInfo")
    public ApiResult<List<ApproveNodeRecordVO>> auditInfo(@RequestBody @Validated BaseIdDTO dto) {
        List<ApproveNodeRecordVO> list=productChangeService.auditInfo(dto.getId());
        return success(list);
    }

}

