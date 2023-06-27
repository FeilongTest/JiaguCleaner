package org.example;

import org.antlr.runtime.RecognitionException;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, RecognitionException {
//        System.out.println("Hello world!");
        String dexPath = args[0];
        String outPath = args[1];
        DexPatch dexPatch = new DexPatch();
        dexPatch.parseDex(dexPath,outPath);
    }
}