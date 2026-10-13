package com.teamtea.craton.common.worldgen.deposit;

import com.teamtea.craton.Craton;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

/**
 * Independent deterministic random-sequence namespaces for deposit families.
 * A family sequence is never reused by another family. Individual deposit ids are then
 * salted below the family sequence so algorithms can be shared without spatial correlation.
 */
public enum DepositRandomSequences {
    STRATIFORM("stratiform"),
    INTRUSIVE("intrusive"),
    EPITHERMAL("epithermal"),
    VMS("vms"),
    SKARN("skarn"),
    VEIN("vein"),
    WEATHERING("weathering"),
    SPECIAL("special"),
    PLACER("placer"),
    PEAT("peat");

    private final Identifier id;
    private final long salt;

    DepositRandomSequences(String path) {
        this.id = Craton.rl("deposit_sequence/" + path);
        this.salt = DepositCandidateSampler.salt(id.toString());
    }

    public Identifier id() {
        return id;
    }

    public long salt() {
        return salt;
    }

    public long instanceSalt(String depositId) {
        return DepositCandidateSampler.mix(salt ^ DepositCandidateSampler.salt(depositId));
    }

    /** Independent keyed positional stream for this deposit family. */
    public PositionalRandomFactory factory(PositionalRandomFactory root) {
        return root.fromHashOf(id).forkPositional();
    }
}
