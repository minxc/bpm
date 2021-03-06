package com.dstz.bpm.engine.parser.flow;

import com.dstz.bpm.api.engine.plugin.def.BpmDef;
import com.dstz.bpm.api.model.def.BpmVariableDef;
import com.dstz.bpm.engine.model.DefaultBpmProcessDef;
import com.dstz.bpm.engine.model.DefaultBpmVariableDef;
import com.dstz.bpm.engine.parser.flow.AbsFlowParse;
import java.util.HashSet;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FlowVariablesParse extends AbsFlowParse<DefaultBpmVariableDef> {
	public String getKey() {
		return "variableList";
	}

	public String validate(Object object) {
		List<BpmVariableDef> varList = (List) object;
		HashSet<String> keys = new HashSet<String>();
		for (BpmVariableDef def : varList) {
			String key = def.getKey();
			if (keys.contains(key)) {
				throw new RuntimeException("解析流程变量出错：" + key + "在流程中重复！");
			}
			keys.add(def.getKey());
		}
		return "";
	}
	@Override
	public void setDefParam(DefaultBpmProcessDef bpmProcessDef, Object object) {
		List varList = (List) object;
		bpmProcessDef.setVarList(varList);
	}

	public boolean isArray() {
		return true;
	}

}