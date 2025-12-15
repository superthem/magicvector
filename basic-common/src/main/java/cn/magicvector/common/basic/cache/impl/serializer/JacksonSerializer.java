package cn.magicvector.common.basic.cache.impl.serializer;

import cn.magicvector.common.basic.cache.Serializer;
import cn.magicvector.common.basic.errors.Errors;
import cn.magicvector.common.basic.exceptions.MagicException;

public class JacksonSerializer implements Serializer {
    @Override
    public String serialize(Object obj) {
        throw new MagicException(Errors.NOT_SUPPORTED);
    }

    @Override
    public Object deserialize(String text) {
        throw new MagicException(Errors.NOT_SUPPORTED);
    }
}
