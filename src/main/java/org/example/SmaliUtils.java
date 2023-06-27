package org.example;

import com.google.common.collect.Iterables;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenSource;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.writer.builder.DexBuilder;
import org.jf.dexlib2.writer.io.MemoryDataStore;
import org.jf.smali.LexerErrorInterface;
import org.jf.smali.smaliFlexLexer;
import org.jf.smali.smaliParser;
import org.jf.smali.smaliTreeWalker;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class SmaliUtils {

    public static DexBackedClassDef compileSmali(String smaliText) throws RecognitionException, IOException {
        return compileSmali(smaliText, 15);
    }

    public static DexBackedClassDef compileSmali(String smaliText, int apiLevel) throws RecognitionException, IOException {
        DexBuilder dexBuilder = new DexBuilder(Opcodes.forApi(apiLevel));
        Reader reader = new StringReader(smaliText);
        LexerErrorInterface lexer = new smaliFlexLexer(reader, apiLevel);
        //LexerErrorInterface lexer = new smaliFlexLexer(reader);
        CommonTokenStream tokens = new CommonTokenStream((TokenSource)lexer);
        smaliParser parser = new smaliParser(tokens);
        parser.setVerboseErrors(true);
        parser.setAllowOdex(false);
        parser.setApiLevel(apiLevel);
        smaliParser.smali_file_return result = parser.smali_file();
        if (parser.getNumberOfSyntaxErrors() <= 0 && lexer.getNumberOfSyntaxErrors() <= 0) {
            CommonTree t = result.getTree();
            CommonTreeNodeStream treeStream = new CommonTreeNodeStream(t);
            treeStream.setTokenStream(tokens);
            smaliTreeWalker dexGen = new smaliTreeWalker(treeStream);
            dexGen.setApiLevel(apiLevel);
            dexGen.setVerboseErrors(true);
            dexGen.setDexBuilder(dexBuilder);
            dexGen.smali_file();
            if (dexGen.getNumberOfSyntaxErrors() > 0) {
                throw new RuntimeException("Error occurred while compiling text");
            } else {
                MemoryDataStore dataStore = new MemoryDataStore();
                dexBuilder.writeTo(dataStore);
                DexBackedDexFile dexFile = new DexBackedDexFile(Opcodes.forApi(apiLevel), dataStore.getBuffer());
                return (DexBackedClassDef) Iterables.getFirst(dexFile.getClasses(), (Object)null);
            }
        } else {
            throw new RuntimeException("Error occurred while compiling text");
        }
    }

}
