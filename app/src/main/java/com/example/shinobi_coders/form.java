package com.example.shinobi_coders;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class form extends AppCompatActivity {


    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    String userID;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        Button btn = (Button) findViewById(R.id.btn);
        final EditText et = (EditText) findViewById(R.id.information);


        btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                String plainText = et.getText().toString();

                Map<String, Object> keys = null;
                try {
                    keys = getRSAKeys();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                PrivateKey privateKey = (PrivateKey) keys.get("private");
                PublicKey publicKey = (PublicKey) keys.get("public");

                // First create an AES Key
                String secretAESKeyString = null;
                try {
                    secretAESKeyString = getSecretAESKeyAsString();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Encrypt our data with AES key
                String encryptedText = null;

                try {
                    encryptedText = encryptTextUsingAES(plainText, secretAESKeyString);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Encrypt AES Key with RSA Private Key
                String encryptedAESKeyString = null;
                try {
                    encryptedAESKeyString = encryptAESKey(secretAESKeyString, privateKey);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // The following logic is on the other side.

                // First decrypt the AES Key with RSA Public key
//                String decryptedAESKeyString = null;
//                try {
//                    decryptedAESKeyString = decryptAESKey(encryptedAESKeyString, publicKey);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                // Now decrypt data using the decrypted AES key!
//                String decryptedText = null;
//                try {
//                    decryptedText = decryptTextUsingAES(encryptedText, decryptedAESKeyString);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

            }
        });
    }


    // Create a new AES key. Uses 128 bit (weak)
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String getSecretAESKeyAsString() throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(128); // The AES key size in number of bits
        SecretKey secKey = generator.generateKey();
        String encodedKey = Base64.getEncoder().encodeToString(secKey.getEncoded());
        return encodedKey;
    }

    // Encrypt text using AES key
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String encryptTextUsingAES(String plainText, String aesKeyString) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(aesKeyString);
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

        // AES defaults to AES/ECB/PKCS5Padding in Java 7
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.ENCRYPT_MODE, originalKey);
        byte[] byteCipherText = aesCipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(byteCipherText);
    }

//    // Decrypt text using AES key
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    public static String decryptTextUsingAES(String encryptedText, String aesKeyString) throws Exception {
//
//        byte[] decodedKey = Base64.getDecoder().decode(aesKeyString);
//        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
//
//        // AES defaults to AES/ECB/PKCS5Padding in Java 7
//        Cipher aesCipher = Cipher.getInstance("AES");
//        aesCipher.init(Cipher.DECRYPT_MODE, originalKey);
//        byte[] bytePlainText = aesCipher.doFinal(Base64.getDecoder().decode(encryptedText));
//        return new String(bytePlainText);
//    }

    // Get RSA keys. Uses key size of 2048.
    private static Map<String, Object> getRSAKeys() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put("private", privateKey);
        keys.put("public", publicKey);
        return keys;
    }

//    // Decrypt AES Key using RSA public key
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    private static String decryptAESKey(String encryptedAESKey, PublicKey publicKey) throws Exception {
//        Cipher cipher = Cipher.getInstance("RSA");
//        cipher.init(Cipher.DECRYPT_MODE, publicKey);
//        return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedAESKey)));
//    }

    // Encrypt AES Key using RSA private key
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static String encryptAESKey(String plainAESKey, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(plainAESKey.getBytes()));
    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
    }
}
