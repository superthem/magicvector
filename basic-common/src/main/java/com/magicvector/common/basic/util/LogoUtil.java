package com.magicvector.common.basic.util;

import com.github.tbwork.anole.loader.Anole;
import com.github.tbwork.anole.loader.AnoleApp;
import com.github.tbwork.anole.loader.core.loader.impl.AnoleFileLoader;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/**
 * Paint logo and success information.
 * @author Tommy.Tang
 */
@Slf4j
public class LogoUtil {



	  public static void printLogo(long costTime){
		  StringBuilder lb = new StringBuilder();
		  try (BufferedReader reader = new BufferedReader(
				  new InputStreamReader(
						  LogoUtil.class.getResourceAsStream("/magicvector.logo"),
						  StandardCharsets.UTF_8))) {
			  int logoWidth = 0;
			  String line;
			  while ((line = reader.readLine()) != null) {
				  logoWidth = logoWidth == 0 ? line.length() : logoWidth;
				  lb.append(line).append("\n");
			  }
			  lb.append("*").append(fillInfo(getProjectInfo(), logoWidth-2, "-")).append("*").append("\n");
			  lb.append("*").append(fillInfo(getHelloMessage(costTime), logoWidth-2, "-")).append("*").append("\n");
			  lb.append("*").append(fillInfo("superthem.com", logoWidth-2, "-")).append("*").append("\n");
			  lb.append(fillInfo("", logoWidth, "*")).append("\n");
			  System.out.println(lb.toString());
		  } catch (IOException e) {
			 log.error(e.getMessage(), e);
		  }
	  }


	  static String fillInfo(String info, int lineWidth, String blankChar){
		  // 计算总的填充字符数
		  int totalPadding = lineWidth - info.length();

		  // 如果总填充字符数小于或等于0，直接返回原字符串
		  if (totalPadding <= 0) {
			  return info;
		  }

		  // 计算左右两边的填充数量
		  int leftPadding = totalPadding / 2;
		  int rightPadding = totalPadding - leftPadding;

		  // 构建填充部分
		  StringBuilder sb = new StringBuilder();
		  for (int i = 0; i < leftPadding; i++) {
			  sb.append(blankChar);
		  }
		  sb.append(info);
		  for (int i = 0; i < rightPadding; i++) {
			  sb.append(blankChar);
		  }

		  return sb.toString();
	  }

	  private static String getProjectInfo(){
		  String projectInfo = Anole.getProperty("spring.application.name");
		  projectInfo = S.isEmpty(projectInfo) ? Anole.getProperty("artifactId"): projectInfo;
		  projectInfo = S.isEmpty(projectInfo) ? "Magic Vector": projectInfo;
		  String versionInfo = Anole.getProperty("runtime.server.version");
		  versionInfo = S.isEmpty(versionInfo) ? "Tech Support." : versionInfo;
		  projectInfo = projectInfo +"-"+ versionInfo;
		  return projectInfo;
	  }

	  private static String getHelloMessage(long costTime){
		  Integer port = Anole.getIntProperty("server.port");// for Spring
		  if(port == 0){
			  port = 8080;
		  }
		  return String.format("Started at %s:%s. Cost %d ms.", "127.0.0.1", port == null? "unkown": port+"", costTime);
	  }




	public static void main(String[] args) {
		AnoleApp.start();
		LogoUtil.printLogo(200);
	}
	
}
