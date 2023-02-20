package com.cloud.erp.chrome.controller;


import com.cloud.erp.chrome.dto.MabangOrderDTO;
import com.cloud.erp.chrome.service.MabanService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Classname MaBanController
 * @Description TODO
 * @Date 2022-08-23 16:49
 * @Created by yl
 */
@RestController
@RequestMapping("mabang/api")
@CrossOrigin(origins = "*")
public class MaBangController extends BaseController {

    @Autowired
    private MabanService mabanService;

    @PostMapping("/importFile")
    //此接口较为特殊，参数都通过表单方式提交，而非JSON
    public ApiResult importCsv(@ModelAttribute MabangOrderDTO dto) {
        mabanService.importIncomeExpensesCsv(dto);
        return success();
    }


}
