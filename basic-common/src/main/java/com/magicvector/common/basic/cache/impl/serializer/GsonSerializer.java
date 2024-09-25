package com.magicvector.common.basic.cache.impl.serializer;

import com.github.tbwork.anole.loader.util.JSON;
import com.magicvector.common.basic.cache.Serializer;
import com.magicvector.common.basic.errors.Errors;
import com.magicvector.common.basic.exceptions.MagicException;

public class GsonSerializer implements Serializer {
    @Override
    public String serialize(Object obj) {
        String className =  obj.getClass().getName();
        return String.format("%s;%s", className, JSON.toJSONString(obj));
    }

    @Override
    public Object deserialize(String text) {
        int index = text.indexOf(";");
        String className = text.substring(0, index);
        String value = text.substring(index+1);
        if(String.class.getName().equals(className)){
            return value;
        }
        try {
            return JSON.parseObject(value, Class.forName(className));
        } catch (ClassNotFoundException e) {
            throw new MagicException(Errors.LOGIC_ERROR, "Can not found the class named {}", className);
        }
    }
}
