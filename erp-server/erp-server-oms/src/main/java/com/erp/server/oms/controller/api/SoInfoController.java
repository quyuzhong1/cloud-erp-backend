package com.erp.server.oms.controller.api;


import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.CustomerDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 销售订单信息 前端控制器
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@RestController
@RequestMapping("/so")
public class SoInfoController extends BaseController {




    /**
     * 获取 tab列表
     *
     * @return
     */
    @GetMapping("/tabList")
    public ApiResult<List<CustomerDTO.TabListDTO>> tabList() {
        return success(null);
    }

    /**
     * 分页列表
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<CustomerDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<CustomerDTO.PagingParamDTO> dto) {
        return success(null);
    }


    /**
     * 新增
     * @param dto
     * @return
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated CustomerDTO.AddDTO dto) {
        return  success();
    }

    /**
     * 新增并提交
     * @param dto
     * @return
     */
    @PostMapping("/addAndSubmit")
    public ApiResult<Void> addAndSubmit(@RequestBody @Validated CustomerDTO.AddDTO dto) {
        return  success();
    }


    /**
     * 详情
     * @param dto
     * @return
     */
    @PostMapping("/view")
    public ApiResult<CustomerDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        return success(null);
    }

}
