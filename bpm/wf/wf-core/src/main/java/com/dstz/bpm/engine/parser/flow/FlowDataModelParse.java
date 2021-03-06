package com.dstz.bpm.engine.parser.flow;

import com.dstz.bpm.api.engine.plugin.def.BpmDef;
import com.dstz.bpm.api.model.def.BpmDataModel;
import com.dstz.bpm.engine.model.DefaultBpmProcessDef;
import com.dstz.bpm.engine.parser.flow.AbsFlowParse;
import java.util.HashSet;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FlowDataModelParse extends AbsFlowParse<BpmDataModel> {
	public String getKey() {
		return "dataModelList";
	}

	public String validate(Object object) {
		List<BpmDataModel> list = (List) object;
		HashSet<String> keys = new HashSet<String>();
		for (BpmDataModel def : list) {
			String key = def.getCode();
			if (keys.contains(key)) {
				throw new RuntimeException("解析流程数据模型出错code：" + key + "在流程中重复配置！");
			}
			keys.add(def.getCode());
		}
		return "";
	}
	@Override
	public void setDefParam(DefaultBpmProcessDef bpmProcessDef, Object object) {
		List list = (List) object;
		bpmProcessDef.setDataModelList(list);
	}

	public boolean isArray() {
		return true;
	}


	
}