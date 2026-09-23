package com.teamtea.craton.api.geology.deposit;


import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.teamtea.craton.Craton;
import net.minecraft.resources.Identifier;

public interface Deposit {
    Codec<Deposit> CODEC = Codec.lazyInitialized(() ->
            Codec.STRING
                    .xmap(
                            s -> s.contains(":")
                                    ? Identifier.parse(s)
                                    : Craton.rl(s),
                            id -> id.getNamespace().equals(Craton.MODID)
                                    ? id.getPath()
                                    : id.toString()
                    )
                    .dispatch(
                            "type",
                            Deposit::getType,
                            DepositTypes.DEPOSITS::get
                    )
    );

    Identifier getType();

    MapCodec<? extends Deposit> codec();
}