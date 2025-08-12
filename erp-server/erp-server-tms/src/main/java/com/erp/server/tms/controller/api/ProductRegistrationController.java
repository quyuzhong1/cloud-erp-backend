package com.erp.server.tms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.ProductRegistrationDTO;
import com.erp.server.tms.query.ProductRegistrationQueryHandler;
import com.erp.server.tms.service.ProductRegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 产品备案表
 *
 * @author lrp
 * @since 2024-03-14
 */
@Slf4j
@RestController
@LogSystemModule("产品备案表")
@RequestMapping("/productRegistration")
public class ProductRegistrationController extends BaseController {

    @Resource
    private ProductRegistrationService productRegistrationService;

    /**
     * tabList
     * @author lrp
     * @date:  2024-03-14
     * @return ApiResult<String>
     */
    @GetMapping("/tabList")
    public ApiResult<List<ProductRegistrationDTO.TabListDTO>> tabList() {
        return success(productRegistrationService.tabList());
    }

    /**
     * 分页
     * @author lrp
     * @date:  2024-03-14
     * @return ApiResult<String>
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = ProductRegistrationQueryHandler.class)
    public ApiResult<PagingVO<ProductRegistrationDTO.PagingVO>> paging(@RequestBody @Valid PagingDTO<ProductRegistrationDTO.PagingParamDTO> dto) {
        PagingVO<ProductRegistrationDTO.PagingVO> pagingVO = productRegistrationService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 详情
     * @author lrp
     * @date:  2024-03-14
     * @return ApiResult<String>
     */
    @GetMapping("/view")
    public ApiResult<ProductRegistrationDTO.ViewVO> view(@Param("id") String id) {
        return success(productRegistrationService.view(id));
    }


    /**
    * 新增备案
    * @author lrp
    * @date:  2024-03-14
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "产品备案表新增")
    public ApiResult<List<BatchResultDTO>> add(@RequestBody @Validated ProductRegistrationDTO.AddDTO dto) {
        List<BatchResultDTO> resultDTOS = productRegistrationService.add(dto);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 取消备案
     * @author lrp
     * @date:  2024-03-14
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/cancel")
    @LogAction(value = LogActionEnum.INSERT, desc = "产品备案表取消备案")
    public ApiResult<List<BatchResultDTO>> cancel(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = productRegistrationService.cancel(dto.getIds());
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 拉取备案
     * @author lrp
     * @date:  2024-03-14
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/pull")
    @LogAction(value = LogActionEnum.INSERT, desc = "产品备案表拉取备案")
    public ApiResult<List<BatchResultDTO>> pull(@RequestBody @Validated ProductRegistrationDTO.AddDTO dto) {
        List<BatchResultDTO> resultDTOS = productRegistrationService.pull(dto);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }



    /**
     * 批量删除
     * @author lrp
     * @date:  2024-03-14
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.DELETE, desc = "产品备案表删除备案")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = productRegistrationService.delete(dto.getIds());
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 导出
     * @author lrp
     * @date:  2024-03-14
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/export")
    @LogAction(value = LogActionEnum.EXPORT, desc = "备案列表导出")
    public ApiResult<Boolean> export(@RequestBody @Validated ProductRegistrationDTO.PagingParamDTO dto) {
        productRegistrationService.export(dto);
        return success(true);
    }
}
