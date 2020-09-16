package com.wataru.blockchain.core.primitive.crypto;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Secp256k1 {
    public static String data="e10adc3949ba59abbe56e057f20f883e";

    //生成密钥对
    public static KeyPair genKeyPair(byte[] seed) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
            ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec("secp256k1");
            keyPairGenerator.initialize(ecGenParameterSpec, new SecureRandom(seed));
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }

    public static PrivateKey fromPrivateByte(byte[] data) {
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(data);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public static PublicKey fromPublicByte(byte[] data) {
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(data);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    //公钥验签
    public static boolean verifySign(byte[] content, java.security.PublicKey publicKey, byte[] sig) {
        try {
            Signature signer = Signature.getInstance("SHA256withECDSA");
            signer.initVerify(publicKey);
            signer.update(content);
            return signer.verify(sig);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    //私钥签名
    public static byte[] sign(byte[] content, PrivateKey privateKey) {
        try {
            Signature signer = Signature.getInstance("SHA256withECDSA");
            signer.initSign(privateKey);
            signer.update(content);
            return signer.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        KeyPair keyPair= genKeyPair(new byte[] {});

        //获取公钥，并以base64格式打印出来
        PublicKey publicKey=keyPair.getPublic();
        System.out.println("公钥："+new String(Base64.getEncoder().encode(publicKey.getEncoded())));

        //获取私钥，并以base64格式打印出来
        PrivateKey privateKey=keyPair.getPrivate();
        System.out.println("私钥："+new String(Base64.getEncoder().encode(privateKey.getEncoded())));

        byte[] encryptedBytes=sign(data.getBytes(), privateKey);
        System.out.println("加密后："+new String(Base64.getEncoder().encode(encryptedBytes)));

        boolean decryptedBytes=verifySign(data.getBytes(), publicKey, encryptedBytes);
        System.out.println("解密后："+decryptedBytes);
    }
}
