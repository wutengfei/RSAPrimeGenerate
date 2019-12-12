package cn.org.bjca.rsaprimegenerate;

import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;


public class RSAKeyPairGenerator implements AsymmetricCipherKeyPairGenerator {
    private static final BigInteger ONE = BigInteger.valueOf(1L);
    private RSAKeyGenerationParameters param;

    public RSAKeyPairGenerator() {
    }

    public void init(KeyGenerationParameters param) {
        this.param = (RSAKeyGenerationParameters) param;
    }

    public AsymmetricCipherKeyPair generateKeyPair(int i) {
        int strength = this.param.getStrength();
        int pbitlength = (strength + 1) / 2;
        int qbitlength = strength - pbitlength;
        int mindiffbits = strength / 3;
        BigInteger e = this.param.getPublicExponent();

        BigInteger p;
        do {
            while (true) {
                p = new BigInteger(pbitlength, 1, this.param.getRandom());
                if (p.mod(e).equals(ONE)) {
                    continue;
                }
                break;
            }
        } while (!p.isProbablePrime(this.param.getCertainty()) || !e.gcd(p.subtract(ONE)).equals(ONE));


        while (true) {
            BigInteger q = new BigInteger(qbitlength, 1, this.param.getRandom());
            if (q.subtract(p).abs().bitLength() >= mindiffbits && !q.mod(e).equals(ONE) && q.isProbablePrime(this.param.getCertainty()) && e.gcd(q.subtract(ONE)).equals(ONE)) {
                BigInteger n = p.multiply(q);
                if (n.bitLength() == this.param.getStrength()) {
                    BigInteger phi;
                    if (p.compareTo(q) < 0) {
                        phi = p;
                        p = q;
                        q = phi;
                    }

                    BigInteger pSub1 = p.subtract(ONE);//p-1
                    BigInteger qSub1 = q.subtract(ONE);//q-1
                    phi = pSub1.multiply(qSub1);//(p-1)*(q-1)
                    BigInteger d = e.modInverse(phi);
                    BigInteger dP = d.remainder(pSub1);
                    BigInteger dQ = d.remainder(qSub1);
                    BigInteger qInv = q.modInverse(p);

                    Log.e("generateKeyPair", "p--" + p + "\nq--" + q);


                    createFile("p", p, i);
                    createFile("q", q, i);

                    return new AsymmetricCipherKeyPair(new RSAKeyParameters(false, n, e), new RSAPrivateCrtKeyParameters(n, e, d, p, q, dP, dQ, qInv));
                }

                p = p.max(q);
            }
        }
    }

    private void createFile(String pOrq, BigInteger bigInteger, int i) {
        try {
            // 解码，然后将字节转换为文件
            File file = new File(Environment.getExternalStorageDirectory() + "/CipherTest/prime", pOrq + i + ".bin");
            if (!file.exists())
                file.getParentFile().mkdirs();
            file.createNewFile();

            FileOutputStream fisp = new FileOutputStream(file);
            byte[] bytes = bigInteger.toByteArray();
            fisp.write(bytes, 0, bytes.length);
            fisp.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
