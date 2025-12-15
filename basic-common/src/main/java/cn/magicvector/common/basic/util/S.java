package cn.magicvector.common.basic.util;


public class S extends com.github.tbwork.anole.loader.util.S {

     
	 public static boolean isEmpty(String what){
		 return what == null || what.isEmpty();
	 }
	
	 public static String getRepeatCharString(char a, int count){
    	 int i = 0;
    	 StringBuilder sb = new StringBuilder();
    	 while( i ++ < count){
    	 	 sb.append(a);
    	 }
    	 return sb.toString();
     }
	 
}
