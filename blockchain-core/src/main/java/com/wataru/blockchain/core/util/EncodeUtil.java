package com.wataru.blockchain.core.util;

import com.wataru.blockchain.core.primitive.ByteBlob;
import com.wataru.blockchain.core.primitive.crypto.Ripemd160;
import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.Cipher;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

/**
 * Created by zzq on 2017-08-27.
 */
public class EncodeUtil {
    
    public static String data="e10adc3949ba59abbe56e057f20f883e";

    //生成密钥对
    public static KeyPair rsaGenKeyPair() throws Exception{
        KeyPairGenerator keyPairGenerator=KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        return keyPairGenerator.generateKeyPair();
    }

    //公钥加密  
    public static byte[] rsaEncrypt(byte[] content, PublicKey publicKey) throws Exception{
        Cipher cipher= Cipher.getInstance("RSA");//java默认"RSA"="RSA/ECB/PKCS1Padding"  
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(content);
    }

    //私钥解密  
    public static byte[] rsaDecrypt(byte[] content, PrivateKey privateKey) throws Exception{
        Cipher cipher=Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(content);
    }

    //md5
    public static String md5(String str) {
        return DigestUtils.md5Hex(str);
    }

    public static String sha512(String str) {
        return DigestUtils.sha512Hex(str);
    }

    public static String sha256(String str) {
        return DigestUtils.sha256Hex(str);
    }

    public static ByteBlob.Byte256 sha256(byte[] bytes) {
        return new ByteBlob.Byte256(DigestUtils.sha256(bytes));
    }

    public static String sha1(String str) {
        return DigestUtils.sha1Hex(str);
    }

    private static byte[] hash160(byte[] data) {
        return Ripemd160.getHash(DigestUtils.sha256(data));
    }

    public static ByteBlob.Byte160 hash160(ByteBlob.Byte256 data) {
        return new ByteBlob.Byte160(EncodeUtil.hash160(data.getData()));
    }

    //hash
    public static String hash(String str, String encode) {
        if ("md5".equalsIgnoreCase(encode)) {
            return md5(str);
        } else if ("sha512".equalsIgnoreCase(encode)) {
            return sha512(str);
        } else if ("sha1".equalsIgnoreCase(encode)) {
            return sha1(str);
        }
        return null;
    }

    /**
     * convert byte[] to HexString
     */
    public static String bytesToHexString(byte[] bArray) {
        if (bArray == null || bArray.length <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        String sTemp;
        for (byte b : bArray) {
            sTemp = Integer.toHexString(0xFF & b);
            if (sTemp.length() < 2) {
                sb.append(0);
            }
            sb.append(sTemp);
        }
        return sb.toString();
    }

    /**
     * convert HexString to byte[]
     */
    public static byte[] hexStringToByte(String hex) {
        if (hex == null || "".equals(hex.trim())) {
            return new byte[0];
        }
        int len = hex.length() / 2;
        byte[] bytes = new byte[len];
        char[] achar = hex.toCharArray();

        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            bytes[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return bytes;

    }

    public static byte[] reverseByteArray(byte[] data) {
        for (int i = 0; i < data.length / 2; i++) {
            byte tmp = data[i];
            data[i] = data[data.length - i - 1];
            data[data.length - i - 1] = tmp;
        }
        return data;
    }

    private static final String HEX_STRING = "0123456789abcdef";
    private static byte toByte(char c){
        return (byte) HEX_STRING.indexOf(c);
    }

    public static void main(String[] args) throws Exception {
        KeyPair keyPair=rsaGenKeyPair();

        //获取公钥，并以base64格式打印出来  
        PublicKey publicKey=keyPair.getPublic();
        System.out.println("公钥："+new String(Base64.getEncoder().encode(publicKey.getEncoded())));

        //获取私钥，并以base64格式打印出来  
        PrivateKey privateKey=keyPair.getPrivate();
        System.out.println("私钥："+new String(Base64.getEncoder().encode(privateKey.getEncoded())));

        //公钥加密  
        byte[] encryptedBytes=rsaEncrypt(data.getBytes(), publicKey);
        System.out.println("加密后："+new String(Base64.getEncoder().encode(encryptedBytes)));

        //私钥解密  
        byte[] decryptedBytes=rsaDecrypt(encryptedBytes, privateKey);
        System.out.println("解密后："+new String(decryptedBytes));

        String result3 = DigestUtils.sha512Hex("123456");
        System.out.println(result3);

        System.out.println("---------------- 计算merkle root --------------");
        ByteBlob.Byte256 s1 = new ByteBlob.Byte256("8347cee4a1cb5ad1bb0d92e86e6612dbf6cfc7649c9964f210d4069b426e720a");
        ByteBlob.Byte256 s2 = new ByteBlob.Byte256("a16f3ce4dd5deb92d98ef5cf8afeaf0775ebca408f708b2146c4fb42b41e14be");
        System.out.println(s1.toLittleEndianNumberHex());
        System.out.println(s2.toLittleEndianNumberHex());
        ByteBlob.ByteVar s3 = new ByteBlob.ByteVar(s1.toLittleEndianNumberHex() + s2.toLittleEndianNumberHex());
        System.out.println(s3.toHex());
        ByteBlob.Byte256 h1 = sha256(sha256(s3.getData()).getData());
        System.out.println(h1.toHex());
        System.out.println(h1.toLittleEndianNumberHex());
    }
}
