package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.AddBomDTO;
import com.erp.model.plm.dto.BomDTO;
import com.erp.model.plm.dto.BomSearchPagingDTO;
import com.erp.model.plm.dto.UpdateBomDTO;
import com.erp.model.plm.vo.BomPagingVO;
import com.erp.server.plm.service.BomInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * bom 信息表(BomInfo)表控制层
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

    /**
     * 分页查询
     *
     * @param
     * @return 查询结果
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<List<BomPagingVO>>> queryByPage(@RequestBody @Validated PagingDTO<BomSearchPagingDTO> dto) {
        PagingVO<List<BomPagingVO>> pagingVO = bomInfoService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 新增数据
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
     * bom 信息
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
     * 提交审核
     * @param dto
     * @return
     */
    @PostMapping("/submitAudit")
    public ApiResult<BomDTO> submitAudit(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = bomInfoService.submitAudit(dto.getId());
        return result==true?success():failure();
    }


    /**
     * 重启流程
     * @param dto
     * @return
     */
    @PostMapping("/restartAudit")
    public ApiResult<BomDTO> restartAudit(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = bomInfoService.restartAudit(dto.getId());
        return result==true?success():failure();
    }

    /**
     * 冻结bom
     * @param dto
     * @return
     */
    @PostMapping("/freeze")
    public ApiResult<BomDTO> freeze(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = bomInfoService.freeze(dto.getId());
        return result==true?success():failure();
    }

    /**
     * 解冻bom
     * @param dto
     * @return
     */
    @PostMapping("/defrost")
    public ApiResult<BomDTO> defrost(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = bomInfoService.defrost(dto.getId());
        return result==true?success():failure();
    }

    /**
     * 报废bom
     * @param dto
     * @return
     */
    @PostMapping("/scrap")
    public ApiResult<BomDTO> scrap(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = bomInfoService.scrap(dto.getId());
        return result==true?success():failure();
    }



    /**
     * 编辑数据
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
     * 删除数据
     *
     * @param dto 主键
     * @return 删除是否成功
     */
    @PostMapping("/delete")
    public ApiResult deleteById(@RequestBody @Validated BaseIdDTO dto) {
        Boolean flag = bomInfoService.deleteById(dto.getId());
        return flag == true ? success() : failure();
    }

}

