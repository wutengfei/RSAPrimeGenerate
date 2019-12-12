package cn.org.bjca.rsaprimegenerate;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;

import static cn.org.bjca.rsaprimegenerate.ZipUtils.toZip;

public class MainActivity extends AppCompatActivity {
    static final BigInteger defaultPublicExponent = BigInteger.valueOf(65537L);
    private RSAKeyPairGenerator rsaKeyPairGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        primeGenerate();
    }

    public void primeGenerate() {
        final String folderPath = Environment.getExternalStorageDirectory() + "/CipherTest/prime";
        deleteDir(folderPath);//生成前先清空目录

        new Thread(new Runnable() {
            @Override
            public void run() {
                rsaKeyPairGenerator = new RSAKeyPairGenerator();

                double startTime = System.currentTimeMillis() / 1000d;
                //rsa素数生成
                for (int i = 1; i <= 12; i++) {
                    KeyGenerationParameters param = new RSAKeyGenerationParameters(defaultPublicExponent, new SecureRandom(), 2048, 12);
                    rsaKeyPairGenerator.init(param);
                    rsaKeyPairGenerator.generateKeyPair(i);
                }
                double endTime = System.currentTimeMillis() / 1000d;

                Log.e("time:s", String.valueOf((endTime - startTime)));
                folder2ZIP(folderPath);
            }
        }).start();


    }

    public void folder2ZIP(String folderPath) {
        File file = new File(folderPath);
        File zipFile = null;
        FileOutputStream fisp = null;
        try {
             zipFile = new File(file.getParent() + "/" + file.getName() + ".zip");
            fisp = new FileOutputStream(zipFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        toZip(folderPath, fisp, true);
        zipFile.getPath();
     //   uploadZip(zipFile.getPath());
    }

    public static boolean deleteDir(String path) {
        File file = new File(path);
        if (!file.exists()) {//判断是否待删除目录是否存在
            System.err.println("The dir are not exists!");
            return false;
        }

        String[] content = file.list();//取得当前目录下所有文件和文件夹
        for (String name : content) {
            File temp = new File(path, name);
            if (temp.isDirectory()) {//判断是否是目录
                deleteDir(temp.getAbsolutePath());//递归调用，删除目录里的内容
                temp.delete();//删除空目录
            } else {
                if (!temp.delete()) {//直接删除文件
                    System.err.println("Failed to delete " + name);
                }
            }
        }
        return true;
    }
}
