package com.kakarote.crm9.utils;

import com.jfinal.log.Log;

import java.io.*;

public class DeepClone {

    private Log logger = Log.getLog(getClass());

    /***
     * 实现深度克隆
     * @author yue.li
     */
    public Object deepCopy(Object source){
//        ObjectOutputStream os = null;
//        ObjectInputStream ois = null;
//        try{
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            os = new ObjectOutputStream(bos);
//            os.writeObject(source);
//            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
//            ois = new ObjectInputStream(bis);
//            Object target = ois.readObject();
//            return target;
//        } catch (Exception e) {
//            logger.error(String.format("deepCopy方法异常 %s",e.getMessage()));
//        }finally {
//            try {
//                if(ois != null){
//                    ois.close();
//                }
//                if(os != null){
//                    os.close();
//                }
//            } catch (IOException e) {
//                os = null;
//                ois=null;
//            }
//        }
        
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream();
        		ObjectOutputStream os = new ObjectOutputStream(bos)) {
        	os.writeObject(source);
            try(ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            		ObjectInputStream ois = new ObjectInputStream(bis)) {
            	return ois.readObject();
            }  catch (Exception e) {
                logger.error(String.format("deepCopy方法异常 %s",e.getMessage()));
            }
        }  catch (Exception e) {
            logger.error(String.format("deepCopy方法异常 %s",e.getMessage()));
        }
        return null;
    }
}
