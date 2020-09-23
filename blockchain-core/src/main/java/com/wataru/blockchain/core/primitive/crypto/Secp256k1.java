package com.wataru.blockchain.core.primitive.crypto;

import com.wataru.blockchain.core.primitive.ByteBlob;
import sun.security.ec.ECPublicKeyImpl;

import java.security.*;
import java.security.spec.*;
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
    public static boolean verifySign(ByteBlob.Byte256 content, java.security.PublicKey publicKey, ByteBlob.ByteVar sig) {
        try {
            Signature signer = Signature.getInstance("SHA256withECDSA");
            signer.initVerify(publicKey);
            signer.update(content.getData());
            return signer.verify(sig.getData());
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    //私钥签名
    public static ByteBlob.ByteVar sign(ByteBlob.Byte256 content, PrivateKey privateKey) {
        try {
            Signature signer = Signature.getInstance("SHA256withECDSA");
            signer.initSign(privateKey);
            signer.update(content.getData());
            return new ByteBlob.ByteVar(signer.sign());
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        KeyPair keyPair = genKeyPair(new byte[] {1,2,3});

        //获取公钥，并以base64格式打印出来
        PublicKey publicKey = keyPair.getPublic();
        ByteBlob.ByteVar p1 = new ByteBlob.ByteVar(publicKey.getEncoded());
        System.out.println("公钥：" + p1.getData().length + " \t" + p1.toHex());

        //获取私钥，并以base64格式打印出来
        PrivateKey privateKey = keyPair.getPrivate();
        ByteBlob.ByteVar p2 = new ByteBlob.ByteVar(privateKey.getEncoded());
        System.out.println("私钥：" + p2.getData().length + " \t" + p2.toHex());

        ByteBlob.ByteVar encryptedBytes = sign(new ByteBlob.Byte256(data.getBytes()), privateKey);
        System.out.println("签名：" + encryptedBytes.getData().length + " \t" + encryptedBytes.toHex());

        boolean decryptedBytes = verifySign(
                new ByteBlob.Byte256(data.getBytes()), publicKey,
                new ByteBlob.ByteVar(encryptedBytes.getData())
        );
        System.out.println("验签：" + decryptedBytes);
    }
}
