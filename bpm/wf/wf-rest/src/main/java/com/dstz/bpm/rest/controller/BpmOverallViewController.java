package com.dstz.bpm.rest.controller;


import java.io.File;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.dstz.base.api.aop.annotion.CatchErr;
import com.dstz.base.api.response.impl.ResultMsg;
import com.dstz.base.core.util.BeanUtils;
import com.dstz.base.core.util.Dom4jUtil;
import com.dstz.base.core.util.FileUtil;
import com.dstz.base.core.util.StringUtil;
import com.dstz.base.core.util.ThreadMsgUtil;
import com.dstz.base.core.util.ZipUtil;
import com.dstz.base.rest.GenericController;
import com.dstz.bpm.core.manager.BpmDefOverallManager;
import com.dstz.bpm.core.model.BpmOverallView;


/**
 * 流程配置全局预览 导入
 * @author jeff
 */
@RestController
@RequestMapping("/bpm/overallView")
public class BpmOverallViewController extends GenericController {
    @Resource
    BpmDefOverallManager bpmDefOverallManager;
	private final static String ROOT_PATH = "attachFiles" + File.separator + "tempZip"; // 导入和导出的文件操作根目录

    
	@RequestMapping("getOverallView")
	public ResultMsg<BpmOverallView> getOverallView(@RequestParam String defId)throws Exception {
		BpmOverallView ovrallView = bpmDefOverallManager.getBpmOverallView(defId);
		
		return getSuccessResult(ovrallView);
	}
    
	
	@RequestMapping("overallViewSave")
	@CatchErr
	public void overallViewSave(@RequestBody BpmOverallView overAllView)throws Exception {
		
	//	bpmDefOverallParser.saveBpmOverallView(overAllView);
		
	}
    
    
    
    

	/**
	 * 导入预览
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("importPreview")
	@CatchErr
	public ResultMsg<Map<String, List<BpmOverallView>>> importPreview(MultipartHttpServletRequest request, HttpServletResponse response) throws Exception {
		MultipartFile fileLoad = request.getFile("xmlFile");
		String unZipFilePath = "";
		
		try {
			String rootRealPath = request.getSession().getServletContext().getRealPath(ROOT_PATH); // 操作的根目录
			String name = fileLoad.getOriginalFilename();
			String fileDir = StringUtils.substringBeforeLast(name, ".");

			ZipUtil.unZipFile(fileLoad, rootRealPath); // 解压文件
			unZipFilePath = rootRealPath + File.separator + fileDir; // 解压后文件的真正路径

			String flowXml = FileUtil.readFile( unZipFilePath + "/bpmdefs.flow.xml");
			if (StringUtil.isEmpty(flowXml)) {
				throw new RuntimeException("导入的未按指定的格式");
			}

			checkXmlFormat(flowXml);
			Map<String,List<BpmOverallView>> perviewMaps = bpmDefOverallManager.importPreview(flowXml);
			
			return getSuccessResult(perviewMaps);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			File boDir = new File(unZipFilePath);
			if (boDir.exists()) {
				FileUtil.deleteDir(boDir); // 删除解压后的目录
			}
		}
	}
	
	@RequestMapping("importSave")
	@CatchErr
	public ResultMsg<String> importSave(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String str = FileUtil.inputStream2String(request.getInputStream());
		List<BpmOverallView> overAllView = JSON.parseArray(str, BpmOverallView.class);
		
		if(BeanUtils.isEmpty(overAllView)){
			throw new RuntimeException("导入的数据不能为空！");
		}
		
		bpmDefOverallManager.importSave(overAllView);
		
		return getSuccessResult( "导入成功!<br/><br/>"+ThreadMsgUtil.getMessage(true));
	}
	
	
	private void checkXmlFormat(String xml) throws Exception {
		String firstName = "bpmlist";
		String nextName = "bpmDef";
		Document doc = Dom4jUtil.loadXml(xml);
		Element root = doc.getRootElement();
		String msg = "导入文件格式不对";
		if (!root.getName().equals(firstName))
			throw new Exception(msg);
		List<Element> itemLists = root.elements();
		for (Element elm : itemLists) {
			if (!elm.getName().equals(nextName))
				throw new Exception(msg);
		}

	}
}
