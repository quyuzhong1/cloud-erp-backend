package com.common.core.anno;

import com.common.core.enums.PannoEnum;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class ParamData implements Serializable {
	private static final long serialVersionUID = 883860826239644039L;
	
	private String colum_name;
	private String param_name;
	private PannoEnum pe;
	private Object val;
	
	
	public ParamData() {
		super();
	}
	
	public ParamData(String colum_name, String param_name, PannoEnum pe, Object val) {
		super();
		this.colum_name = colum_name;
		this.param_name = param_name;
		this.pe = pe;
		this.val = val;
	}
	
}