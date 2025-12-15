package cn.magicvector.common.basic.util;

import cn.magicvector.common.basic.errors.Errors;
import cn.magicvector.common.basic.exceptions.MagicException;

import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Manifest;

/**
 * @author Tommy.Tang
 */
public class ProjectUtil {
	 
	public static String getProjectName(){ 
		//in case of run locally 
		Class<?> bootClass = getBootClass(); 
		String launchClassName = getBootClassLoaderNameByClass(bootClass); 
		if(launchClassName.startsWith("sun.misc.Launcher")){
			//locally
			return getProjectNameFromPackageName(bootClass.getPackage().getName());
		}
		else if(launchClassName.startsWith("org.springframework.boot.loader.JarLauncher")){
			//lunch from other frameworks like spring etc.
			return getProjectNameByManifest();
		}
		else{
			throw new MagicException(Errors.UNKNOWN_BOOT_LAUNCHER);
		} 
	}

	private static Class<?> getBootClass(){
		StackTraceElement []  stackTraces = (new RuntimeException()).getStackTrace(); 
		try {
			return Class.forName(stackTraces[stackTraces.length-1].getClassName());
		} catch (ClassNotFoundException e) { 
			e.printStackTrace();
			throw new MagicException(Errors.CLASS_NOT_FOUND);
		} 
	}
	
	/**
	 * For example:
	 * <pre>
	 *  class A{
	 *     void func1(){
	 *         func2();	
	 *     }
	 *  }
	 *  class B{
	 *     void func2(){
	 *         func3();
	 *     }
	 *  }
	 *  class C{
	 *     void fun3(){
	 *         Class targetClass = getUpperClassByOffset(i);
	 *     }
	 *  } 
	 *  // getUpperClassByOffset(0) refers  to  ProjectUtil.class
	 *  // getUpperClassByOffset(1) refers  to  C.class
	 *  // getUpperClassByOffset(2) refers  to  B.class
	 * </pre>
	 * @param offset the offset of target class that the called method belongs to. 
	 * @return the target class of the called method.
	 */
	public static Class<?> getUpperClassByOffset(int offset){
		StackTraceElement []  stackTraces = (new RuntimeException()).getStackTrace(); 
		try {
			if(offset >= stackTraces.length)
				throw new MagicException(Errors.MAIN_CLASS_NOT_FOUND);
			return Class.forName(stackTraces[offset].getClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new MagicException(Errors.CLASS_NOT_FOUND);
		} 
	}
	
	private static String getBootClassLoaderNameByClass(Class<?> bootClass){ 
		ClassLoader classLoader = bootClass.getClassLoader();
		if(classLoader == null)
			 return "";
		return classLoader.toString();
	}
	
	public static final String getProjectNameFromPackageName(String packageName){
		String [] subNames = packageName.split("\\.");
		List<String> subNameList = Arrays.asList(subNames);
		int index = subNameList.indexOf("service");
		if(index < 0)
			index = subNameList.indexOf("api");
		if(index < 0)
			index = subNameList.indexOf("provider");
		if(index < 0)
			index = subNameList.size();
		
		if(!subNames[0].equals("com") || !subNames[1].equals("lanehub")){
			// use the fore three fields in case of unrecognized format.
			return String.join("-", subNames[0], subNames[1], subNames[2]);
		}
		else{
			return String.join("-", subNameList.subList(2, index));
		} 
	}
	
	public static final String  getClassName(String fullClassPath){
		String [] subNames = fullClassPath.split("\\.");
		return subNames[subNames.length-1].replace("Stub", "").replace("stub", "");
	} 
	
	
	private static String getProjectNameByManifest(){
		try{ 
			Enumeration<URL> resources =  getBootClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
			if (resources.hasMoreElements()) { 
			       Manifest manifest = new Manifest(resources.nextElement().openStream());
			       return manifest.getAttributes("Implementation-Title").toString();  
			}
			throw new MagicException(Errors.MANIFEST_NOT_FOUND);
			
		}
		catch(Exception e){
			e.printStackTrace();
			throw new MagicException(Errors.UNKNOWN_BOOT_LAUNCHER);
		}
		
	}
	 
}
