package org.example;

import org.antlr.runtime.RecognitionException;
import org.jf.baksmali.Adaptors.ClassDefinition;
import org.jf.baksmali.BaksmaliOptions;
import org.jf.baksmali.formatter.BaksmaliWriter;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.writer.io.MemoryDataStore;
import org.jf.dexlib2.writer.pool.DexPool;

import java.io.*;
import java.util.Arrays;

public class DexPatch {

    public void parseDex(String dexPath,String outPath) throws IOException, RecognitionException {
        if (dexPath.equals("")){
            System.out.println("please input dexPath!");
            return;
        }
        if (outPath.equals("")){
            System.out.println("please input outPath!");
            return;
        }
        FileInputStream dex = new FileInputStream(dexPath);
        DexBackedDexFile dexBackedDexFile = DexBackedDexFile.fromInputStream(
                Opcodes.getDefault(),
                new BufferedInputStream(dex)
        );
        boolean isJiagu = false;
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
                smali = smali.replaceAll("invoke-static \\{.*}, Lcom/stub/StubApp;->interface.*\\(I\\)V",
                        "");
                smali = smali.replaceAll("\r\n\r\n    invoke-static \\{.*}, Lcom/stub/StubApp;->interface.*\\(I\\[Ljava/lang/String;\\[I\\)V",
                        "");
                smali = smali.replaceAll("\r\n\r\n    invoke-static/range \\{(.*)}, Lcom/stub/StubApp;->getOrigApplicationContext\\(Landroid/content/Context;\\)Landroid/content/Context;\r\n\r\n    move-result-object (.*)" ,"");
                if (smali.contains("stub/Stub")){
                    //todo add more
                    System.out.println(smali);
                    break;
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
        }
    }
}

