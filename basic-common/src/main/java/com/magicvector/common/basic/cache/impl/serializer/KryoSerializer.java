package com.magicvector.common.basic.cache.impl.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.io.BaseEncoding;
import com.magicvector.common.basic.cache.Serializer;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class KryoSerializer implements Serializer {

    private static ThreadLocal<Kryo> threadLocal = new ThreadLocal<Kryo>();

    @Override
    public String serialize(Object obj) {
        Kryo kryo = getInstance();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output(baos);
        kryo.writeClassAndObject(output, obj);
        output.flush();
        String result =  BaseEncoding.base64().encode(baos.toByteArray());
        output.close();
        return result;
    }

    @Override
    public Object deserialize(String text) {
        byte [] orgBytes = BaseEncoding.base64().decode(text);
        InputStream inputStream = new ByteArrayInputStream(orgBytes);
        Input input = new Input(inputStream);
        Object result = getInstance().readClassAndObject(input);
        input.close();
        return result;
    }


    private Kryo getInstance(){

        if(threadLocal.get() == null){
            Kryo kryo = new Kryo();
            kryo.setReferences(false);
            kryo.setRegistrationRequired(false);
            kryo.register(ArrayList.class);
            kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
            threadLocal.set(kryo);
        }

        return threadLocal.get();
    }

}
