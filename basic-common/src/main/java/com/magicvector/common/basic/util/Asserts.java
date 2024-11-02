package com.magicvector.common.basic.util;

import com.magicvector.common.basic.errors.Errors;
import com.magicvector.common.basic.errors.Error;
import com.magicvector.common.basic.exceptions.MagicException;

/**
 * Assert tools used in Lanehub.
 */
public class Asserts {


    public static void assertTrue(boolean expression, Error error, String notes){
        if(!expression){
            throw new MagicException( error, notes);
        }
    }

    /**
     * Assert the expression is true, otherwise interrupt the request and
     * give out the notes.
     * @param expression the candidaate expression
     * @param notes notes for users
     */
    public static void assertTrue(boolean expression, String notes){
        if(!expression){
            throw new MagicException(Errors.LOGIC_ERROR, notes);
        }
    }

    public static void assertTrue(boolean expression, String notesPattern, String ... parameters){
        if(!expression){
            throw new MagicException(Errors.LOGIC_ERROR, notesPattern, parameters);
        }
    }

    public static void assertTrue(boolean expression, Error error, String notesPattern, String ... parameters){
        if(!expression){
            if(error == null){
                error = Errors.LOGIC_ERROR;
            }
            throw new MagicException(error, notesPattern, parameters);
        }
    }
}
