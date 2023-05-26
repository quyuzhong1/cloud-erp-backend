package com.erp.server.wms.controller.api;


import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.TransferInDTO;
import com.erp.server.wms.service.TransferInService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * 调拨管理-分布式调入
 *
 * @author lambda
 * @since 2023-05-10
 */
@RestController
@RequestMapping("/transfer/in")
public class TransferInController extends BaseController {

    @Resource
    private TransferInService transferInService;


    /**
     * 获取 tab列表
     *
     * @return
     */
    @GetMapping("/tabList")
    public ApiResult<List<TransferInDTO.TabListDTO>> tabList() {
        List<TransferInDTO.TabListDTO>  tabList= transferInService.tabList();
        return success(tabList);
    }

    /**
     * 分页列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<TransferInDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<TransferInDTO.PagingParamDTO> dto) {
        return success(null);
    }


    /**
     * 分布式调出单下推 分布式调入
     *
     * @param list
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-05-23 15:15
     */
    @PostMapping("/generateTransferIn")
    public ApiResult generateTransferIn(@RequestBody @Valid ValidList<TransferInDTO.ViewGenerateTransferInDTO> list) {
        Boolean result = transferInService.generateTransferIn(list);
        return result ? success() : failure();

    }



    /**
     * 提交
     *
     * @param dto
     * @return
     */
    @PostMapping("/submit")
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success();
    }

    /**
     * 新增并提交
     *
     * @param dto
     * @return
     */
    @PostMapping("/addAndSubmit")
    public ApiResult<Void> addAndSubmit(@RequestBody @Validated TransferInDTO.AddDTO dto) {
        return success();
    }

    /**
     * 详情
     *
     * @param dto
     * @return
     */
    @PostMapping("/view")
    public ApiResult<TransferInDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        return success(null);
    }

    /**
     * 修改
     *
     * @param dto
     * @return
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated TransferInDTO.UpdateDTO dto) {
        return success(null);
    }

    /**
     * 修改并提交
     *
     * @param dto
     * @return
     */
    @PostMapping("/updateAndSubmit")
    public ApiResult updateAndSubmit(@RequestBody @Validated TransferInDTO.UpdateDTO dto) {
        return success(null);
    }

    /**
     * 审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/approve")
    public ApiResult audit(@RequestBody @Validated BaseApproveParamDTO dto) {
        return success();
    }

    /**
     * 反审核
     */
    @PostMapping("/disApprove")
    public ApiResult disApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        return success();
    }

    /**
     * 删除仓库
     *
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    public ApiResult delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        return success();
    }

    /**
     * 作废
     *
     * @param dto
     * @return ApiResult
     * @author Will
     * @date: 2023/5/10 20:11
     */
    @PostMapping("/invalid")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        return success();
    }

    /**
     * 导出
     * 数据
     */
    @PostMapping("/export")
    public ApiResult exportWarehouse(@RequestBody @Valid TransferInDTO.ExportDTO dto, HttpServletResponse response) {

        return success();
    }


}
