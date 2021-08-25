package com.ashburndev;

// import com.google.*;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Arrays;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class GpgUtil {

    /* In production, these string values will not be "hard-coded" into this file,
     * but will instead be obtained from some external source of configuration
     * information (a properties file, a database, system properties, etc.)
     */
    public static final String GPG_ROOT_DIR = "C:\\gpg";
    public static final String GPG_KEY_DIR = "C:\\gpg\\keys-dh";
    // public static final String GPG_KEY_DIR = "C:\\gpg\\keys-jb";
    // public static final String GPG_BIN_PATH = "C:\\gpg\\GnuPG\\bin\\gpg.exe";    // works fine
    public static final String GPG_BIN_PATH = "C:\\Program Files (x86)\\GnuPG\\bin\\gpg.exe";    // also works fine
    public static final String GPG_SENDER_USERNAME = "David Holberton <dph@gmail.com";
    public static final String GPG_SENDER_PASSPHRASE = "WriteOnceRunAnywhere";
    // public static final String GPG_RECIPIENT_USERNAME = "James Bond <agent007@mi6.gov";    // works fine
    // public static final String GPG_RECIPIENT_USERNAME = "James Bond";    // also works fine
    public static final String GPG_RECIPIENT_USERNAME = "Bond";    // also works fine
    // in production, I will never have the value below, and I will never need to use it
    public static final String GPG_RECIPIENT_PASSPHRASE = "ShakenNotStirred";

    public static final String DICKENS_QUOTE =
            "It was the best of times, it was the worst of times,\n" +
                    "it was the age of wisdom, it was the age of foolishness,\n" +
                    "it was the epoch of belief, it was the epoch of incredulity,\n" +
                    "it was the season of light, it was the season of darkness,\n" +
                    "it was the spring of hope, it was the winter of despair.";

    public static void main(String[] args) {
        System.out.println("Hello GPG");
        GpgUtil gpgUtil = new GpgUtil();
        gpgUtil.testVersion();
        gpgUtil.testListKeys();
        gpgUtil.testEncrypt(GPG_RECIPIENT_USERNAME, DICKENS_QUOTE);
    }

    public void testVersion() {
        byte [] versionBytes = version();
        System.out.println(" versionBytes.length = " + versionBytes.length);
        String versionString = new String(versionBytes, StandardCharsets.UTF_8);
        System.out.println(versionString);
    }

    public void testListKeys() {
        byte [] listKeysBytes = listKeys();
        System.out.println(" listKeysBytes.length = " + listKeysBytes.length);
        String listKeysString = new String(listKeysBytes, StandardCharsets.UTF_8);
        System.out.println(listKeysString);
    }

    void testEncrypt(String recipient, String plainText) {
        System.out.println(plainText);
        byte [] plainBytes = plainText.getBytes();
        System.out.println(" plainBytes.length = " + plainBytes.length);
        byte [] encryptedBytes = encrypt(recipient, plainBytes);
        System.out.println(" encryptedBytes.length = " + encryptedBytes.length);
    }

    // https://stackoverflow.com/questions/318239/how-do-i-set-environment-variables-from-java
    void setUpEnvironment(ProcessBuilder builder) {
        Map<String, String> env = builder.environment();
        // env["GNUPGHOME"] = GPG_KEY_DIR;
        env.put("GNUPGHOME",GPG_KEY_DIR);
    }

    byte[] version() {
        String [] commandArray = {
                // "gpg",
                GPG_BIN_PATH,
                "--no-tty",
                "--batch",
                "--yes",
                "--version"
        };
        System.out.println(commandArray);
        List <String> commandList = Arrays.asList(commandArray);
        System.out.println(commandList);
        System.out.println(String.join(" ",commandList));
        ProcessBuilder pb = new ProcessBuilder(commandList);
        setUpEnvironment(pb);
        return startProcessBuilder(pb,null);
    }

    byte[] listKeys() {
        String [] commandArray = {
                // "gpg",
                GPG_BIN_PATH,
                "--no-tty",
                "--batch",
                "--yes",
                "--list-keys"
        };
        System.out.println(commandArray);
        List <String> commandList = Arrays.asList(commandArray);
        System.out.println(commandList);
        System.out.println(String.join(" ",commandList));
        ProcessBuilder pb = new ProcessBuilder(commandList);
        setUpEnvironment(pb);
        return startProcessBuilder(pb,null);
    }

    byte[] encrypt(String recipient, String inputFilename, String outputFilename) {
        // still need to implement this method
        return null;
    }

    byte[] encrypt(String recipient, byte[] plain) {
        //    String [] commandArray = {
        //            "gpg",
        //            "--no-tty",
        //            "--batch",
        //            "--yes",
        //            "--always-trust",
        //            "--recipient", recipient,
        //            "--encrypt"};
        //    System.out.println(commandArray);
        //    List <String> commandList = Arrays.asList(commandArray);
        //    System.out.println(commandList);
        String [] commandArray = {
                // "gpg",
                GPG_BIN_PATH,
                "--no-tty",
                "--batch",
                "--yes",
                "--always-trust",
                "--recipient", recipient,
                "--encrypt"
        };
        System.out.println(commandArray);
        List <String> commandList = Arrays.asList(commandArray);
        System.out.println(commandList);
        System.out.println(String.join(" ",commandList));
        try {
            //    ProcessBuilder pb = new ProcessBuilder(
            //            "gpg",
            //            "--no-tty",
            //            "--batch",
            //            "--yes",
            //            "--always-trust",
            //            "--recipient", recipient,
            //            "--encrypt");
            ProcessBuilder pb = new ProcessBuilder(commandList);
            setUpEnvironment(pb);
            System.out.println(pb.toString());
            Process p = pb.start();
            p.getOutputStream().write(plain);
            p.getOutputStream().flush();
            p.getOutputStream().close();
            int code = p.waitFor();
            if (code != 0) {
                String exceptionText = String.format("gpg --encrypt  failed with code %s: %s", code,
                        CharStreams.toString(new InputStreamReader(p.getErrorStream())));
                System.out.println (exceptionText);
                throw new RuntimeException(exceptionText);
            }
            return ByteStreams.toByteArray(p.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    byte[] startProcessBuilder(ProcessBuilder pb, byte[] stdinBytes) {
        try {
            Process p = pb.start();
            if (stdinBytes != null && stdinBytes.length > 0) {
                p.getOutputStream().write(stdinBytes);
                p.getOutputStream().flush();
                p.getOutputStream().close();
            }
            int code = p.waitFor();
            if (code != 0) {
                String exceptionText = String.format("process failed with code %s: %s", code,
                        CharStreams.toString(new InputStreamReader(p.getErrorStream())));
                System.out.println (exceptionText);
                throw new RuntimeException(exceptionText);
            }
            return ByteStreams.toByteArray(p.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}

/*
6:02:53 AM: Executing task 'GpgUtil.main()'...

> Task :compileJava
> Task :processResources NO-SOURCE
> Task :classes

> Task :GpgUtil.main()
Hello GPG
It was the best of times, it was the worst of times,
it was the age of wisdom, it was the age of foolishness,
it was the epoch of belief, it was the epoch of incredulity,
it was the season of light, it was the season of darkness,
it was the spring of hope, it was the winter of despair.
 plainBytes.length = 286
gpg --no-tty --batch --yes --always-trust --recipient James Bond --encrypt
java.lang.ProcessBuilder@5674cd4d
 encryptedBytes.length = 718

BUILD SUCCESSFUL in 688ms
2 actionable tasks: 2 executed
6:02:54 AM: Task execution finished 'GpgUtil.main()'.
*/

/*
7:19:50 AM: Executing task 'GpgUtil.main()'...

> Task :compileJava
> Task :processResources NO-SOURCE
> Task :classes

> Task :GpgUtil.main()
Hello GPG
C:\Program Files (x86)\GnuPG\bin\gpg.exe --no-tty --batch --yes --version
 versionBytes.length = 565
gpg (GnuPG) 2.2.28
libgcrypt 1.8.8
Copyright (C) 2021 g10 Code GmbH
License GNU GPL-3.0-or-later <https://gnu.org/licenses/gpl.html>
This is free software: you are free to change and redistribute it.
There is NO WARRANTY, to the extent permitted by law.

Home: C:/gpg/keys-dh
Supported algorithms:
Pubkey: RSA, ELG, DSA, ECDH, ECDSA, EDDSA
Cipher: IDEA, 3DES, CAST5, BLOWFISH, AES, AES192, AES256, TWOFISH,
        CAMELLIA128, CAMELLIA192, CAMELLIA256
Hash: SHA1, RIPEMD160, SHA256, SHA384, SHA512, SHA224
Compression: Uncompressed, ZIP, ZLIB, BZIP2

C:\Program Files (x86)\GnuPG\bin\gpg.exe --no-tty --batch --yes --list-keys
 listKeysBytes.length = 392
C:/gpg/keys-dh/pubring.kbx
--------------------------
pub   rsa4096 2021-07-05 [SC]
      14912E2F9B4D33703F5241ED90A2D107CF1C45C4
uid           [ultimate] David Holberton <dph@gmail.com>
sub   rsa4096 2021-07-05 [E]

pub   rsa4096 2021-07-05 [SC]
      E3D6F0B4BFE739B74B822001378549D87A82554F
uid           [ unknown] James Bond <agent007@mi6.gov>
sub   rsa4096 2021-07-05 [E]


It was the best of times, it was the worst of times,
it was the age of wisdom, it was the age of foolishness,
it was the epoch of belief, it was the epoch of incredulity,
it was the season of light, it was the season of darkness,
it was the spring of hope, it was the winter of despair.
 plainBytes.length = 286
C:\Program Files (x86)\GnuPG\bin\gpg.exe --no-tty --batch --yes --always-trust --recipient Bond --encrypt
java.lang.ProcessBuilder@5b6f7412
 encryptedBytes.length = 718

BUILD SUCCESSFUL in 665ms
2 actionable tasks: 2 executed
7:19:51 AM: Task execution finished 'GpgUtil.main()'.
 */
