/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.crypto;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.RequiresDevice;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spongycastle.jcajce.provider.asymmetric.rsa.BCRSAPrivateKey;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.util.List;

import me.digi.sdk.crypto.testmodels.CATestContent;
import me.digi.sdk.crypto.testmodels.TestJFSFile;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class CAFileCryptoTest {
    private static final String TEST_PRIVATE_KEY = "308204a50201000282010100c947db8ccf38c1f40a7cd9c4333e0368023f84d8be46d37ad5954e497000ae2d580a62e08de009313a829e82b24bba0caf0475dfaec0b36f4f84b62684f39aab69b31a4e027409d887ba88e34c8139539b2c6d2ee3fd9d23cac34330901c7ae98e4f2fad9edc0f1679571cc7259bd1a95ad6f308bf7d3cf33351324933f122bdce4006d5a0643223e759bf5fad8af59d626e8d7abc7198810e94d66c788c10329264acd6aa66153280d9c881cfe816a6fdefe85a2e2504328e76d8d642fcfcbab4b0b2ea184feb006eefe24db889b696382f122c44cece717fda7735aa12ce4e7bba914c8d45cbef9c3ab47cb8e3c40af32bf7eafd6b8aaee2f70c839362adeb02030100010282010100b133bafd377e2f7acb34e97f0ae1e09bd3c6da0cfb4f5d65b9dd6d83c7c0419797f7e4deeee8bb0f0504f3c9fa7022c681daba6f87e90ccfc541001fdf529beba6edd00db7a932f5d760889d1bc07498bf7718547cd1cd6332623fa7e467be6a1a286ac03ea85bfc1c2d6e1f8163b1ec9815bef707a6995f3ee19014d44ec996a29559ae00abc0b0b61f1a147e1aac5f792b5469aa3c7ed8358b3e186af1d9aac25677b2c05f3c664e8885d558208a46f3e3843f405399c2b3bab30c65a725d7a198bd49ca0ad7e7752da99bd00290f70fc77a0f7973120ce140e57df5799d7115bca6c4d70da91fae4f3be72d53e5bfdc1fe15dd37f046d6589e3726ad8f48902818100ec6a39092fbfce6d051bb9b671362e26a285ae5aadc92214bd4673d9690e45b5ca19b11294b5908573de7ca2e7c1d83e212dfb70831d3a50199d1946a9e49e2bdd5ecf3c918d60db7aa2526f7ebd0c84e9408993e6a41341410c2a55771f9a61a40a8af7e4798eee1e98ccd7d1dd3f8eb86ce76518372fde301f103a5cb133cd02818100d9f4863a8998c59c14b2278fb45adf99b03af2d47082a55ab2585fd0f037e90e3320150bfe92ad1b92515e1930ea8f6e55172f8a1470b34ff7b67e3b034e15e993426f974a90ad5ad6273919d8d995bc0fd9547a8db60a8228ccaa07d9139b3dcc79fe5422f7b6026966751589d4b9b89ce0261c363179b9ccaef06a6d71a0970281802348932c98d0c28928cb0383840ff701531e2a7064217191b0d1f3f64da490a8d9f9cda09d4b1fbf9b14687b93a52d95d033e1a3e01d9b975acb447b745da7719a7f4ce4984086651b3f60983d4d0fb242719c56d384474f64dae0f2926dc807ac88da46b6f5a16c4e6ab59fbc358e07c9e48f005a85da020a2288b47d23013d02818100d4cf72b8795d57a55c77cf34fb4eb780a2980c3ded55430ad9947c89cfe367855bd9f972eab060a1c92df588f7402fa7f5215c63a02da287744115e39d088350bb5e6502fde561be8dd76263a05e635b6ac6333c2e5e0ec8a3f9a213639b473b020a2390174c72c4cc112445517d0991fe6ac60b49c6e929c777107b7a3d362502818100df9d7183ca83afd0c9f22ad3f5e6fd08fa5f82092c62a0d804af11ae734519633a8ade6ff18efe86d588d8cfc396b40b2591f85a99153d84ef4f980b3d471290717a1d6329a369d16d180f8fdd1fca23e3140f5899ef49cd5ce747491aa63d0aab67922367d778cf84ac46b03232beacc4c0e20718fdbb88d367d48c975ca3f4";
    private static final String PEM_TEST_PRIVATE_EXPONENT = "42044758860479646038965328863162225695495133720257561160862160654182392562582738774896273643659210702582676339034000652828662308625238459960076257156381403266384563301297930253916846288153374766388892634109661693510964129708676686056203393092579075655435485619143510512897386780232632223924485016599868212369";
    private static final String DEC_KEY = "308204a30201000282010100a9aedea6779e528992a039f40c19a39062d33b0fa6de4f2af4b74805655cd0119069dbeb0bab90b481cdeceb2dea9f014f5ddeb0a93316e7146946e9fa8b897fb480989037587c73811231e4d22cf28b6d2ce811d7e6f1275f0783fd345cd03be945e026326188a1aa12d9174f6bd0c6d90304339eb721942fcafeee13256135359c98442b72dd10471e6dfaffdf0d916599cadfadaae025b726faf88ec44bb0945e9e8fab6e7a98152de7171e6503930fffe2f32dd3a4447a6327ce8601f795d6d57ad9640fcd8fa1b55dc248ebb7269f2da430e85de688eff1321d00097158b8f9c1f7816ab95c519375b256b0c6040bd7b9858c2d27fed2370567ba8427990203010001028201010093f3de3fd85d3c2aa8a6fce1470bb40ad9a0c506c8c15ed65dbad219a260632c6d7760427a5286425e4c682048512383c8e8589c416c42b40aa0212d3341280b2a2056e6a8db86e84fcac5a6777ca99fd8fa270027f93e9ccdc787d6e829658857c68dc3c07a3ae07ba32397a7b0a2c23fc6d98b0901354e38be0fbb1706a8d287834ec8c411f80fa44ecabd714675b0d59633acc6995512c2dc16875c586a1847688c186fa2f69c4eb9eae4331d464cd8509bf99d441eae98f7e9537ef31d6da58850f08d4b2aab82d6273f2000447bc02d5b458712fa77e356f739c32290348836db924c8eec90afb87b7dd8c20a806e0f97c98d35f94c3120bad1936241e102818100e4865f90b71a4f5b1a12fa92ea0444684ac9b3e0f1ddc1e6cb6a489537410c91b36037dec12171ccc33563cdab2a15e1cfa722a94c6407bd2be6e5dba881d8838854199f746b312d9e00bb4c171585bc5051e6ba03a152487f3718eb0967d2a8025f0f01993689eca47d605bd23ee128e3bf3c2977530f20b89c374e50af404302818100be1570ce9b9d0f4b9c14f3d5ea2f55d539e3cbb81eb9e44fa878cf9e11ce689564852d4ed79d09158e2721eb8637e961d673f84274d1384f4a95e883b0c77e5d606f24fa01b9a610bdfda1415ef608b83cd38477905e62f5b9aa3ef76d6222304c9cc2c2b12f60b78ceee5892cd17fa2872d5b980dd45e99e3123431ef7eb8f3028180521f13b28e8a2ee03f2378d658b045e0f0974143e1c6de0a5129158241c3e77f6865784e5d3ae6893dd12ed756de1dd4f2e94de466e63f7db48c1a27f08b10c25bb85528df0e32330167a3e6f918abe17b3fa3594f3aa6b614b93904257220da6d57b9adca6035fa4b361eed804546668a494b965f2202fab03cbb0732a977bf0281803860c79aa0110f6e4f96ef536d2828ff1b327343e2e923cc749d9086c3a542e3bc72bba37cd3f8d3c9dbd575b3d375872d422c4a19b7cc49c8477a35450386794f96e792b75c46e30456ebb325e53764ddb5a6be87b55708a6ced5ea31294016af427789a35ff801b8ed4a6b4b3dbfeb86c86f384431cef539a23694f101d6fd0281807ed1368c8b524e2af0bdf6d7b24f028b1f587583e617f5e599abdedb049ae9c8ae66a15c1c27a12e422521dba6316d830fdd2d3707a861793c3b93039d988ba5923ee483db546d8dfca792c7e9aa86b36796732ea6815e9e2283e21d77664b7089574e3cdd9a899b39344327fc7a057a007850fcf9252bf98c5c2c96b9a38368";

    private Context testContext;
    private CACryptoProvider provider;

    @Before
    public void setUp() {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
        testContext = InstrumentationRegistry.getInstrumentation().getContext();
    }

    @Test
    public void pemPrivateKeyParsingTest() throws Exception{
        InputStream testInput = testContext.getAssets().open("test_pem_private.pem");

        PrivateKey privKey = CryptoUtils.getPrivateKey(ByteUtils.readBytesFromStream(testInput));
        assertNotNull(privKey);
        assertThat(privKey, instanceOf(RSAPrivateKey.class));
        assertThat(((RSAPrivateKey) privKey).getPrivateExponent(), is(new BigInteger(PEM_TEST_PRIVATE_EXPONENT)));

        //Validate that spongy castle provider is loaded correctly
        assertThat(privKey, instanceOf(BCRSAPrivateKey.class));
    }

    @Test
    public void cryptoProviderInitWithBadKeyTest() throws Exception{
        String badKey = "1" + TEST_PRIVATE_KEY;
        CACryptoProvider provider = new CACryptoProvider(new CAKeyStore(badKey));
        assertFalse(provider.hasValidKeys());
    }

    @Test
    public void encryptedCAFileTest() throws Exception{
        InputStream testInput = testContext.getAssets().open("ca_file_encryption_v2.valid");
        CACryptoProvider provider = new CACryptoProvider(new CAKeyStore(TEST_PRIVATE_KEY));
        String decrypted = ByteUtils.bytesToString(provider.decryptStream(testInput, false));
        assertNotNull(decrypted);
        Gson gson = new Gson();
        TestJFSFile content = gson.fromJson(decrypted, TestJFSFile.class);
        assertNotNull(content);
        assertThat(content.version, is("18.0.0"));
        assertThat(content.createdDate, is(1489402053837L));
        assertThat(content.uuid, is(12345));
        assertThat(content.msks, hasItem("daeedf4d6acdd2ac5280c85657d00605666a3fab516ff813d58c5c0e1820dcb8"));
    }

    @Test(expected = DGMCryptoFailureException.class)
    public void encryptedCAFileInvalidHashTest() throws Exception{
        InputStream testInput = testContext.getAssets().open("ca_file_encryption_v2.invalid_hash");
        CACryptoProvider provider = new CACryptoProvider(new CAKeyStore(TEST_PRIVATE_KEY));
        String decrypted = ByteUtils.bytesToString(provider.decryptStream(testInput, false));
        assertNull(decrypted);
    }

    @Test(expected = DGMCryptoFailureException.class)
    public void encryptedCAFileInvalidDSKTest() throws Exception{
        InputStream testInput = testContext.getAssets().open("ca_file_encryption_v2.invalid_dsk");
        CACryptoProvider provider = new CACryptoProvider(new CAKeyStore(TEST_PRIVATE_KEY));
        String decrypted = ByteUtils.bytesToString(provider.decryptStream(testInput, false));
        assertNotNull(decrypted);
    }

    @Test
    public void encryptedCAFileNullContentTest() throws Exception{
        InputStream testInput = testContext.getAssets().open("ca_file_encryption_v2.valid_but_null");
        CACryptoProvider provider = new CACryptoProvider(new CAKeyStore(TEST_PRIVATE_KEY));
        String decrypted = ByteUtils.bytesToString(provider.decryptStream(testInput, false));
        assertThat(decrypted, isEmptyString());
    }

    @Test
    public void encryptedBase64StreamTest() throws Exception{
        InputStream testInput = testContext.getAssets().open("base64_encoded.valid");
        CACryptoProvider provider = new CACryptoProvider(new CAKeyStore(DEC_KEY));
        String decrypted = ByteUtils.bytesToString(provider.decryptStream(testInput));
        assertNotNull(decrypted);
        Gson gson = new Gson();
        Type type = new TypeToken<List<CATestContent>>(){}.getType();
        List<CATestContent> content = gson.fromJson(decrypted, type);
        assertNotNull(content);
        assertThat(content, hasSize(2));
        assertThat(content.get(0).entityId, is("1_10153965102085754_10153965102085754_10154435684185754"));
        assertThat(content.get(1).createdDate, is(1488370100000L));
    }

    @Test
    public void encryptedJsonStreamTest() throws Exception{
        InputStream testInput = testContext.getAssets().open("sample_response.json");
        CACryptoProvider provider = new CACryptoProvider(new CAKeyStore(DEC_KEY));

        Gson gson = new Gson();
        LinkedTreeMap<String, Object> ret = gson.fromJson(new JsonReader(new InputStreamReader(testInput)), Object.class);
        assertThat(ret.containsKey("fileContent"), is(true));
        assertNotNull(ret);

        String fileContent = (String) ret.get("fileContent");
        String decrypted = ByteUtils.bytesToString(provider.decryptStream(new ByteArrayInputStream(fileContent.getBytes("UTF-8"))));
        assertNotNull(decrypted);
        Type type = new TypeToken<List<CATestContent>>(){}.getType();
        List<CATestContent> content = gson.fromJson(decrypted, type);
        assertNotNull(content);
        assertThat(content, hasSize(2));
        assertThat(content.get(0).entityId, is("1_10153965102085754_10153965102085754_10154435684185754"));
        assertThat(content.get(1).createdDate, is(1488370100000L));
        ret.put("fileContent", content);
        String json = gson.toJson(ret);
        assertNotNull(json);
    }
}
