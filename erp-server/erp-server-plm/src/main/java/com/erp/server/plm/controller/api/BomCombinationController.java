package com.erp.server.plm.controller.api;

import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.plm.dto.BomCombinationDTO;
import com.erp.server.plm.service.BomCombinationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * 组合产品
 * @author Will
 * @version 1.0
 * @date 2023/8/16 9:39
 */
@RestController
@RequestMapping("bomCombination")
public class BomCombinationController extends BaseController {
    @Resource
    private BomCombinationService bomCombinationService;

    /**
     * 列表查询
     * @author Will
     * @date: 2023/8/16 10:02
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<BomCombinationDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<BomCombinationDTO.SearchParamDTO> dto) {
        PagingVO<BomCombinationDTO.ListDTO> pagingVO = bomCombinationService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 新增
     * @author Will
     * @date: 2023/8/16 10:08
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/add")
    public ApiResult<Object> add(@RequestBody @Validated BomCombinationDTO.AddDTO dto) {
        Boolean flag = this.bomCombinationService.add(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改
     * @author Will
     * @date: 2023/8/16 10:16
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/update")
    public ApiResult<Object> update(@RequestBody @Validated BomCombinationDTO.UpdateDTO dto) {
        Boolean flag = this.bomCombinationService.update(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 验证BOM是否重复
     * @author Will
     * @date: 2024/4/12 16:14
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/checkBomChildSku")
    public ApiResult<String> checkBomChildSku(@RequestBody @Validated BomCombinationDTO.CheckBomParentSkuDTO dto) {
        String msg = this.bomCombinationService.checkBomChildSku(dto);
        return success(msg);
    }

    /**
     * 查看详情
     * @author Will
     * @date: 2023/8/16 10:26
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/view")
    public ApiResult<BomCombinationDTO.ViewDTO> update(@RequestBody @Validated BaseIdDTO dto) {
        BomCombinationDTO.ViewDTO viewDTO = this.bomCombinationService.view(dto);
        return success(viewDTO);
    }


    /**
     * 导入
     * @author Will
     * @date: 2023/8/17 14:06
     * @param excelFile
     * @param response
     * @return ApiResult
     */
    @PostMapping("/importFile")
    public ApiResult<Object> importFile(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean flag = bomCombinationService.importFile(excelFile,response);
        return flag == true ? success() : failure();
    }

    /**
     * 下载导入模板
     * @author Will
     * @date: 2023/8/17 14:07
     * @param response
     * @return ApiResult
     */
    @GetMapping("/downloadTemplate")
    public ApiResult<Object> downloadTemplate(HttpServletResponse response) {
        bomCombinationService.downloadTemplate(response);
        return success();
    }

}
