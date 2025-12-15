package cn.magicvector.common.basic.util;

import javax.validation.*;
import java.util.*;

public class ValidateUtil { 
	
	private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private static final Validator validator = factory.getValidator();
	
	public static <T> Map<String, String> validate(T target){
		 Set<ConstraintViolation<T>> violations = validator.validate(target); 
		 Map<String, String> result = new HashMap<String, String>();
		 for (ConstraintViolation<T> violation : violations) {  
			    String path = "";
		    	for(Path.Node node : violation.getPropertyPath()){
		    		String nodeName = node.getName(); 
		    		path += (nodeName+"."); 
		    	}
		    	if(path.endsWith(".")) path = path.substring(0, path.length()-1);
		    	result.put(path, violation.getMessage());
		 }
		 return result;
	}
	
	
	public static <T> Map<String, ArrayList<String>> validate(T t, HashSet<String> skipFields){
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();		
		Set<ConstraintViolation<T>> constraintViolations = validator.validate(t);
		if(constraintViolations != null && constraintViolations.size() > 0){
			Map<String, ArrayList<String>> mapErr = new HashMap<String, ArrayList<String>>();
		    for (ConstraintViolation<T> constraintViolation : constraintViolations) {  
		    	for(Path.Node node : constraintViolation.getPropertyPath()){
		    		String fieldName = node.getName();
		    		if(skipFields == null || !skipFields.contains(fieldName)){
			    		ArrayList<String> lst = mapErr.get(fieldName);
			    		if(lst == null){
			    			lst = new ArrayList<String>();
			    		}
			    		lst.add(constraintViolation.getMessage());
			    		mapErr.put(node.getName(), lst);
		    		}
		    	}
		    }
		    return mapErr;
		}
		return null;
	}
	
	

}
