package com.erp.server.plm.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.plm.dto.ProductMemberDTO;
import com.erp.server.plm.service.TaskCommentRefService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 *  任务列表-任务评论
 *
 * @author Lambda
 * @since 2023-06-09
 */
@RestController
@RequestMapping("/taskCommentRef")
public class TaskCommentRefController extends BaseController {

    @Autowired
    private TaskCommentRefService taskCommentRefService;

    /**
     * @后获取对应成员信息
     * @param productId
     * @return
     */
    @GetMapping("listMemberByProductId")
    public ApiResult<List<ProductMemberDTO.TaskRefDTO>> listMemberByProductId(@RequestParam("productId") String productId) {
        List<ProductMemberDTO.TaskRefDTO> resultList = taskCommentRefService.listMemberByProductId(productId);
        return success(resultList);
    }


}
