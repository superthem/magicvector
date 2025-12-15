package cn.magicvector.common.basic.cache;

/**
 * A serializer is used to serialize the java object to
 * string object.
 */
public interface Serializer {

    /**
     * Serialize the java object to string.
     * @param obj the candidate object.
     * @return the string value of the object.
     */
    String serialize(Object obj);


    /**
     * Deserialize the string to get the real java object.
     * @param stringValue the string value of the object.
     * @return the java object.
     */
    Object deserialize(String stringValue);

}
