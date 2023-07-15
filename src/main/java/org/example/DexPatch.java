package org.example;

import javafx.scene.SubScene;
import jdk.nashorn.internal.runtime.regexp.joni.Regex;
import org.antlr.runtime.RecognitionException;
import org.jf.baksmali.Adaptors.ClassDefinition;
import org.jf.baksmali.BaksmaliOptions;
import org.jf.baksmali.formatter.BaksmaliWriter;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.writer.io.MemoryDataStore;
import org.jf.dexlib2.writer.pool.DexPool;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DexPatch {

    public void parseDex(String dexPath,boolean fix) throws IOException, RecognitionException {
        if (dexPath.equals("")){
            System.out.println("please input dexPath!");
            return;
        }
        String outPath = dexPath.replace(".dex","_out.dex");
        FileInputStream dex = new FileInputStream(dexPath);
        DexBackedDexFile dexBackedDexFile = DexBackedDexFile.fromInputStream(
                Opcodes.getDefault(),
                new BufferedInputStream(dex)
        );
        boolean isJiagu = false;
        int nativeActivityCount = 0;
        List<String> activities = new ArrayList<>();
        DexPool dexPool = new DexPool(Opcodes.getDefault());
        System.out.println("patching...");
        for(DexBackedClassDef classDef: dexBackedDexFile.getClasses()){
            StringWriter stringWriter = new StringWriter();
            BaksmaliWriter writer = new BaksmaliWriter(stringWriter);
            ClassDefinition classDefinition = new ClassDefinition(new BaksmaliOptions(), classDef);
            classDefinition.writeTo(writer);
            writer.close();
            String smali = stringWriter.toString();
            if(smali.contains("stub/Stub")){
                isJiagu = true;
                smali = smali.replaceAll("\r\n\r\n    invoke-static \\{}, Lcom/stub/StubApp;->mark\\(\\)V","");
                smali = smali.replaceAll("\r\n    invoke-static \\{.*}, Lcom/stub/StubApp;->interface.*\\(I\\[Ljava/lang/String;\\[I\\)V","");
                smali = smali.replaceAll("invoke-static \\{.*}, Lcom/stub/StubApp;->interface.*\\(I\\)V",
                        "");
                smali = smali.replaceAll("\r\n\r\n    invoke-static \\{.*}, Lcom/stub/StubApp;->interface.*\\(I\\[Ljava/lang/String;\\[I\\)V",
                        "");
                smali = smali.replaceAll("\r\n\r\n    invoke-static/range \\{(.*)}, Lcom/stub/StubApp;->getOrigApplicationContext\\(Landroid/content/Context;\\)Landroid/content/Context;\r\n\r\n    move-result-object (.*)" ,"");
                if (smali.contains("stub/Stub")){
                    System.out.println("This class has unfix 'stub/Stub' Code:\n" + classDef.toString().substring(1,classDef.toString().length()-1).replaceAll("/","."));
                    break;
                }
                if(smali.contains("native onCreate") || smali.contains("native onRequestPermissionsResult")){
                    nativeActivityCount++;
                    activities.add(classDef.toString());
                    if(fix){
                        String fixCode = ".method protected onCreate(Landroid/os/Bundle;)V\r\n" +
                                "    .registers 5\n\n" +
                                "    invoke-super {p0, p1}, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V\n\n" +
                                "    return-void\n" +
                                ".end method";
                        String matcher1 = "\\.method (.*) native onCreate\\(Landroid/os/Bundle;\\)V\r\n\\.end method";
                        String matcher2 = "\\.method (.*) native onCreate\\(Landroid/os/Bundle;\\)V\r\n    \\.param .*  # Landroid/os/Bundle;\r\n        \\.annotation build Landroidx/annotation/Nullable;\r\n        \\.end annotation\r\n    \\.end param\r\n\\.end method";



                        smali = smali.replaceAll(matcher1,fixCode);
                        smali = smali.replaceAll(matcher2,fixCode);

                        //修复onRequestPermissionsResult
                        smali = smali.replaceAll("\\.method public native onRequestPermissionsResult([\\s\\S]*)param([\\s\\S]*)    \\.end param\r\n\\.end method","");


                        if (smali.contains("native onCreate") || smali.contains("native onRequestPermissionsResult")){
                            System.out.println("This class has unfix native onCreate or onRequestPermissionsResult:\n" + classDef.toString().substring(1,classDef.toString().length()-1).replaceAll("/","."));
                            System.out.println(smali);
                            break;
                        }
                    }
                }

                //修改的Class写入新Dex
                dexPool.internClass(SmaliUtils.compileSmali(smali));
            }else{
                dexPool.internClass(classDef);
            }


        }
        if(isJiagu){
            MemoryDataStore dataStore = new MemoryDataStore();
            dexPool.writeTo(dataStore);
            byte[] newDexByte = Arrays.copyOf(dataStore.getBuffer(), dataStore.getSize());
            File file = new File(outPath);
            FileOutputStream outputStream = null;
            outputStream = new FileOutputStream(file);
            outputStream.write(newDexByte);
            outputStream.close();
            dataStore.close();
            if(!fix){
                System.out.printf("This dex has %d protected Activity! Activities List:\n",nativeActivityCount);
                for (String activity : activities) {
                    System.out.println(activity.substring(1,activity.length()-1).replaceAll("/","."));
                }
            }
            if(fix){
                System.out.println("This dex is fixed!");
            }
        }
    }
}

