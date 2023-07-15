package org.example;

import org.antlr.runtime.RecognitionException;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, RecognitionException {
        String dexPath = args[0];

        boolean fix = false;
        if(args.length >= 2){
            if(args[1].equals("fix")){
                fix = true;
            }
        }
        DexPatch dexPatch = new DexPatch();
        dexPatch.parseDex(dexPath,fix);
    }
}