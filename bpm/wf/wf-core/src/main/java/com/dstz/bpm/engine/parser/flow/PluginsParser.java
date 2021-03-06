package com.dstz.bpm.engine.parser.flow;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dstz.base.core.util.AppUtil;
import com.dstz.base.core.validate.ValidateUtil;
import com.dstz.bpm.api.engine.plugin.context.BpmPluginContext;
import com.dstz.bpm.api.engine.plugin.def.BpmDef;
import com.dstz.bpm.api.engine.plugin.def.BpmPluginDef;
import com.dstz.bpm.engine.model.DefaultBpmProcessDef;
import com.dstz.bpm.engine.parser.flow.AbsFlowParse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class PluginsParser extends AbsFlowParse<BpmPluginContext> {
	
	
	public Object parseDef(DefaultBpmProcessDef bpmProcessDef, String json) {
		JSONObject plugins = JSON.parseObject((String) json);
		ArrayList<BpmPluginContext> pluginContextList = new ArrayList<BpmPluginContext>();
		for (String pluginName : plugins.keySet()) {
			BpmPluginContext pluginContext = (BpmPluginContext) AppUtil
					.getBean((String) (pluginName + "PluginContext"));
			if (pluginContext instanceof BpmPluginContext) {
				try {
					pluginContext.parse((JSON) plugins.get((Object) pluginName));
					BpmPluginDef def = pluginContext.getBpmPluginDef();
					ValidateUtil.validate((Object) def);
				} catch (Exception e) {
					this.LOG.error("插件{}解析失败:{}！", new Object[]{pluginContext.getTitle(), e.getMessage(), e});
				}
			}
			pluginContextList.add(pluginContext);
		}
		return pluginContextList;
	}

	public String getKey() {
		return "plugins";
	}

	public String validate(Object o) {
		return null;
	}

	public void setDefParam(DefaultBpmProcessDef bpmProcessDef, Object object) {
		ArrayList<BpmPluginContext> pluginContextList = (ArrayList) object;
		bpmProcessDef.setPluginContextList(pluginContextList);
	}

}