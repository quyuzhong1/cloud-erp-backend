package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.modules.workflow.dto.ProcessPassDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.AddBomDTO;
import com.erp.model.plm.dto.BomDTO;
import com.erp.model.plm.dto.SearchPagingDTO;
import com.erp.model.plm.dto.UpdateBomDTO;
import com.erp.model.plm.vo.BomPagingVO;
import com.erp.model.plm.vo.BomVersionVO;
import com.erp.model.workflow.dto.AuditorHandleDTO;
import com.erp.server.plm.service.BomInfoService;
import com.erp.server.plm.service.ProductBomHistoryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * BOM 管理
 *
 * @author yl
 * @since 2023-01-09 11:45:28
 */
@RestController
@RequestMapping("plm/bom")
public class BomInfoController extends BaseController {
    /**
     * 服务对象
     */
    @Resource
    private BomInfoService bomInfoService;


    @Resource
    private ProductBomHistoryService productBomHistoryService;

    /**
     * 分页查询
     *
     * @param
     * @return 查询结果
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<List<BomPagingVO>>> queryByPage(@RequestBody @Validated PagingDTO<SearchPagingDTO> dto) {
        PagingVO<List<BomPagingVO>> pagingVO = bomInfoService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 新增BOM
     *
     * @param
     * @return 新增结果
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated AddBomDTO dto) {
        Boolean flag = this.bomInfoService.insert(dto);
        return flag == true ? success() : failure();
    }

    /**
     * bom 详情
     *
     * @param
     * @return 新增结果
     */
    @PostMapping("/view")
    public ApiResult<BomDTO> details(@RequestBody @Validated BaseIdDTO dto) {
        BomDTO bom = bomInfoService.getBomDetails(dto.getId());
        return success(bom);
    }

    /**
     * bom  审核 通过
     *
     * @param
     * @return 新增结果
     */
    @PostMapping("/approvalPass")
    public ApiResult approvalPass(@RequestBody @Validated AuditParamDTO dto) {
        bomInfoService.approvalPass(dto);
        return success();
    }


    /**
     * bom  审核 不通过
     *
     * @param
     * @return 新增结果
     */
    @PostMapping("/approvalNoPass")
    public ApiResult approvalNoPass(@RequestBody @Validated AuditParamDTO dto) {
        bomInfoService.approvalNoPass(dto);
        return success();
    }


    /**
     * 提交审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/submitAudit")
    public ApiResult submitAudit(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = bomInfoService.submitAudit(dto.getId());
        return result == true ? success() : failure();
    }


    /**
     * 重启流程
     *
     * @param dto
     * @return
     */
    @PostMapping("/restartAudit")
    public ApiResult restartAudit(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = bomInfoService.restartAudit(dto.getId());
        return result == true ? success() : failure();
    }

    /**
     * 冻结bom
     *
     * @param dto
     * @return
     */
    @PostMapping("/freeze")
    public ApiResult freeze(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = bomInfoService.freeze(dto.getId());
        return result == true ? success() : failure();
    }

    /**
     * 解冻bom
     *
     * @param dto
     * @return
     */
    @PostMapping("/defrost")
    public ApiResult defrost(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = bomInfoService.defrost(dto.getId());
        return result == true ? success() : failure();
    }

    /**
     * 报废bom
     *
     * @param dto
     * @return
     */
    @PostMapping("/scrap")
    public ApiResult scrap(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = bomInfoService.scrap(dto.getId());
        return result == true ? success() : failure();
    }

    /**
     * 恢复bom
     *
     * @param dto
     * @return
     */
    @PostMapping("/recover")
    public ApiResult recover(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = bomInfoService.recover(dto.getId());
        return result == true ? success() : failure();
    }

    /**
     * 解除归档
     *
     * @param dto
     * @return
     */
    @PostMapping("/removeArchive")
    public ApiResult removeArchive(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = bomInfoService.removeArchive(dto.getId());
        return result == true ? success() : failure();
    }


    /**
     * 编辑bom
     *
     * @param
     * @return 编辑结果
     */
    @PostMapping("/update")
    public ApiResult edit(@RequestBody @Validated UpdateBomDTO dto) {
        Boolean flag = this.bomInfoService.edit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 删除bom
     *
     * @param dto 主键
     * @return 删除是否成功
     */
    @PostMapping("/delete")
    public ApiResult deleteById(@RequestBody @Validated BaseIdDTO dto) {
        Boolean flag = bomInfoService.deleteById(dto.getId());
        return flag == true ? success() : failure();
    }

    /**
     * 历史版本信息
     */
    @PostMapping("/version/list")
    public ApiResult<List<BomVersionVO>> versionList(@RequestBody @Validated BaseIdDTO dto) {
        List<BomVersionVO> list = productBomHistoryService.getVersionList(dto.getId());
        return success(list);
    }


    /**
     * 发起变更
     */
    @PostMapping("/startChange")
    public ApiResult startChange(@RequestBody @Validated UpdateBomDTO dto) {
        Boolean result = bomInfoService.startChange(dto);
        return result == true ? success() : failure();
    }


    /**
     * 导出bom 数据
     */
    @PostMapping("/exportExcel")
    public ApiResult exportExcel(@RequestBody @Validated SearchPagingDTO dto, HttpServletResponse response) {
        bomInfoService.exportExcel(dto, response);
        return success();
    }

    /**
     * bom审核通过后改变 bom 状态
     */
    @PostMapping("/workflow/pass")
    public ApiResult processPass(@RequestBody ProcessPassDTO dto) {
        bomInfoService.bomProcessPass(dto);
        return success();
    }


    /**
     * bom 审核情况
     *
     * @return
     */
    @PostMapping("/auditInfo")
    public ApiResult<List<AuditorHandleDTO>> auditInfo(@RequestBody @Validated BaseIdDTO dto) {
        List<AuditorHandleDTO> list=bomInfoService.auditInfo(dto.getId());
        return success(list);
    }


}

