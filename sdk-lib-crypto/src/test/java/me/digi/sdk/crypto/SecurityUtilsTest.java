/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.crypto;

import org.junit.Before;
import org.junit.Test;

import java.security.Security;
import java.util.LinkedHashMap;

import static me.digi.sdk.crypto.ByteUtils.bytesToString;
import static org.junit.Assert.assertEquals;

public class SecurityUtilsTest {
    @SuppressWarnings("FieldCanBeLocal")
    private static final String benchmarkData = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed eu ex lobortis turpis cursus aliquam. Donec facilisis lorem vitae luctus scelerisque. Nam non laoreet ex, sed aliquet arcu. Mauris eu tristique erat, id ullamcorper purus. Nullam condimentum tortor augue, quis suscipit magna aliquam a. Nulla facilisi. In placerat, odio id interdum semper, lacus metus ultrices erat, nec pulvinar nisl massa ut erat.";

    @Before
    public void setUp() throws Exception {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    @Test
    public void aesDecryptExternalTest() throws Exception {
        LinkedHashMap<String, String> map = externalAesData();

        byte[] iv = new byte[16];
        for (int i = 0; i < 16; i++) {
            iv[i] = 0;
        }

        byte[] key = new byte[32];
        for (int i = 0; i < 32; i++) {
            key[i] = 0;
        }

        for (int i = 0; i < map.size(); i++) {
            String testData = "";
            for (int j = 0; j < i; j++) {
                testData += "A";
            }

            byte[] decryptedData = CryptoUtils.decryptAES(ByteUtils.hexToBytes(map.get(testData)), key, iv);
            assertEquals(testData, bytesToString(decryptedData));
        }
    }

    private LinkedHashMap<String, String> externalAesData() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("", "1f788fe6d86c317549697fbf0c07fa43");
        map.put("A", "040aac1a93a85e9ed24a7ca4586dd3e3");
        map.put("AA", "2505ebdbc07568d5df02caaeab3b6239");
        map.put("AAA", "466ead6eb8a1862a94e1148837dee087");
        map.put("AAAA", "0bcbc1d9509ff724dae752d417dcd9e0");
        map.put("AAAAA", "96ac978fbbd188ee0e9fb25d78d7f607");
        map.put("AAAAAA", "6cf14818b4c1960bda36c8a837ed824f");
        map.put("AAAAAAA", "72e68e1eee21a969769b2c0633bf1015");
        map.put("AAAAAAAA", "9cb8bf72126f176edeb37a667d82eb3b");
        map.put("AAAAAAAAA", "ace1cfac24a33dc5833e26f07bc4ded7");
        map.put("AAAAAAAAAA", "f2cdc805d3ee4dba3c611787cb3376e1");
        map.put("AAAAAAAAAAA", "463ca8753562429820de7b2ed824faa1");
        map.put("AAAAAAAAAAAA", "f95b9d73cb11abb166a8760e805f52bc");
        map.put("AAAAAAAAAAAAA", "d158bcdf37e5c6f65b9384514f362538");
        map.put("AAAAAAAAAAAAAA", "0ce82d33c85c3904a20247e5c4dfa745");
        map.put("AAAAAAAAAAAAAAA", "9825223a7eccaac93642a4e2d34f1d36");
        map.put("AAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827effa8088f11990f7c70321ad10d9a70af");
        map.put("AAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e3dacff8f75ae452185b2da4fae80189f");
        map.put("AAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e672491e26a3d6d26486d926f11f652f4");
        map.put("AAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827ee1339edde86def10f1d69175a1281222");
        map.put("AAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e4942a1b79024fd9bcc440f025b7884b9");
        map.put("AAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e27b05da5ef2213c1d55bf8a3084b1267");
        map.put("AAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827ec8b34032019550ee59dc7da36553b8c2");
        map.put("AAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e08e81474fe612ab590f468e747f5cc87");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827ea7454dcac6f39560fd913b45592ca21e");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e653907e14f9d0f962653a2c681a01afe");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827ec4dc968dcb459664e26de26c73a67601");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e4ceb452312d11e183a10146d98406a48");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827ea26ce00b58b12a0e28f480f8cb17652a");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e3c5605a18b2e3a7257699886eaeda2f3");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e65f122473101d6dd78b7fb0d58876b99");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e748381c768240e5a886ba5bf20bc0120");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce703ad1e038bfac469a26d800f3756e64bc");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70603c0bae55d3c44c867468991a079755");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70d80d373de646938eeb33e61bf0995b95");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce7074de7c4ec1cf94d11121ab84287b344a");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce704462780a822469518d470517d787e8b8");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70fcb2219f5579bad271383d050a096630");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce7031dd523da0f4aed35c0806a4e76c5660");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70e2e83258b244995f176abeccac345842");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70da91e88f36acb1649053baf5211440b8");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce7098898612f7c53a066d36de7b8298fd7d");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70a01e4040101b0bec76dcb68c477846a2");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce702d62ffb5a0511fd3bbe85b22081da335");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70b8b500161fb6467b5604f892e0de2415");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70fbcda8e0f54b8b90e7296a3fa370bcbd");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70f9dfe58755fecc165d90212bebab0f51");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70c1efa7f67933f96412569d1c07a078c1");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa43463760b82530f377cd39192e921e662c22");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa4346c5f42f5e4690692f0c4f6bff1216d029");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa4346f724f844fb8d7d1825c75e2d933d9456");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa4346cb7378b0d1351111134b5340da43c4ad");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa43466b8d9a2c3df378862b2711fad466cd7e");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa4346a89347fddf4aee4ab6a54e28e6fa9024");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa4346570fe57c721935e2c8cfd0e4d13ce9fa");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa4346c4cad92dbe5a9336bc22e2d50de3b9c4");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa4346a34e0de1e12532a24ee925d54aae9560");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa4346933ea9fe6102baef01242cd8146496ac");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa4346c47258b528221838995b355e6c6f3760");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa4346d52d5fdd97398c5139222a9cf4abe011");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa4346838e71c085cbebbe849dc4fe788806ac");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa434633b2e3b4c758ca0b016c48980abfff83");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa4346572849215a79ab98d0b694ccc5261749");
        map.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "7e0e7577ef9c30a6bf0b25e0621e827e76f757ef1271c5740e60c33c8ec3ce70bd3cb46076b1eab54bc1fe5b88fa43461454f8c1ab829f1cf8496ff9db8b4911");
        return map;
    }

    @Test
    public void sha512Test() {
        String expectedResult = "353c86a44ea160300e79a77504da8b46b01b10af5795318f22cb93b847fe7567889e56fcf0815dcc67fc1747a800b3a55778eeea99285ee61a2cefd155991e20";
        String actualResult = CryptoUtils.hashSha512(benchmarkData);

        assertEquals(expectedResult, actualResult);
    }
}
