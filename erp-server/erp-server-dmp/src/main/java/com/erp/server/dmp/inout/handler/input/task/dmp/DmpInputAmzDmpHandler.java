//package com.erp.server.dmp.inout.handler.input.task.dmp;
//
//import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
//import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputThirdCodeDbDmpHandler;
//import org.springframework.context.annotation.Scope;
//import org.springframework.stereotype.Service;
//
///**
// * dmp处理下一个扩展handler，如何订收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
// * @author Administrator
// *
// */
//@Service
//@Scope("prototype")
//public class DmpInputAmzDmpHandler extends DmpInputThirdCodeDbDmpHandler {
//
//	@Override
//	protected DmpBasicSystemCodeEnum getDmpBasicSystemCodeEnum() {
//		return DmpBasicSystemCodeEnum.AMAZON;
//	}
//
//}
