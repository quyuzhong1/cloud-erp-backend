package com.erp.server.mrp.controller.api;


import java.time.LocalDate;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.server.mrp.es.entity.TestEsEntity;
import com.erp.server.mrp.es.repository.TestEsRepository;

@RestController
@RequestMapping("/testEs")
public class TestEsController extends BaseController {
	@Autowired
	private TestEsRepository testEsRepository;
	
	@PostMapping("/save")
    public ApiResult<?> save(@RequestBody TestEsEntity testEsEntity){
        return success(testEsRepository.save(testEsEntity));
    }
	
	@PostMapping("/findAll")
	public ApiResult<?> findAll(){
		return success(testEsRepository.findAll(PageRequest.of(0, 3)).getContent());
	}
	
	@PostMapping("/findByCode")
	public ApiResult<?> findByCode(@RequestBody TestEsEntity testEsEntity){
		PageRequest pageable = PageRequest.of(0, 3);
		return success(testEsRepository.findByCode(testEsEntity.getCode() , pageable).getContent());
	}
	
	@PostMapping("/findByCodeInAndDateBetween")
	public ApiResult<?> findByCodeInAndDateBetween(@RequestBody TestEsEntity testEsEntity){
		PageRequest pageable = PageRequest.of(0, 3);
		String code = testEsEntity.getCode();
		LocalDate date = testEsEntity.getDate();
		return success(testEsRepository.findByCodeInAndDateBetween(Arrays.asList(code),date, date, pageable));
	}
}
