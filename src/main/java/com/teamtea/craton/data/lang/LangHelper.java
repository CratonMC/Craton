package com.teamtea.craton.data.lang;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public abstract class LangHelper extends LanguageProvider {
    private final PackOutput output;


    public LangHelper(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
        this.output = output;
        this.modid = modid;
        this.locale = locale;
    }

    @Override
    public String getName() {
        return super.getName();
    }

    // public <T> void add(ResourceKey<T> key, String name) {
    //     add(ESRegistries.createLangKey(key), name);
    // }


    // There is a lot of code here that is redundant, but indispensable. In order to make corrections
    protected abstract void addTranslations();

    private final String locale;
    public final String modid;

}
