package net.ltxprogrammer.changed.process;

import net.ltxprogrammer.changed.entity.PlayerDataExtension;
import net.ltxprogrammer.changed.entity.PlayerMover;
import net.ltxprogrammer.changed.entity.PlayerMoverInstance;
import net.ltxprogrammer.changed.entity.variant.LatexVariantInstance;
import net.ltxprogrammer.changed.util.InputWrapper;
import net.ltxprogrammer.changed.util.UniversalDist;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;

public class LockToPlayerMover extends PlayerMover<LockToPlayerMover.Instance> {
    @Override
    public Instance createInstance() {
        return new Instance(this);
    }

    public static class Instance extends PlayerMoverInstance<LockToPlayerMover> {
        public Player host = null;
        public boolean voluntarilyLeave = false;
        public boolean renderHost = true;

        public Instance(LockToPlayerMover parent) {
            super(parent);
        }

        @Override
        public void saveTo(CompoundTag tag) {
            super.saveTo(tag);
            tag.putUUID("player", host.getUUID());
            tag.putBoolean("voluntarilyLeave", voluntarilyLeave);
            tag.putBoolean("renderHost", renderHost);
        }

        @Override
        public void readFrom(CompoundTag tag) {
            super.readFrom(tag);
            this.host = UniversalDist.getLevel().getPlayerByUUID(tag.getUUID("player"));
            this.voluntarilyLeave = tag.getBoolean("voluntarilyLeave");
            this.renderHost = tag.getBoolean("renderHost");
        }

        @Override
        public void aiStep(Player player, InputWrapper input, LogicalSide side) {
            if (host == null)
                return;

            LatexVariantInstance.syncEntityPosRotWithEntity(player, host);
        }

        @Override
        public void serverAiStep(Player player, InputWrapper input, LogicalSide side) {

        }

        @Override
        public boolean shouldRemoveMover(Player player, InputWrapper input, LogicalSide side) {
            return host == null || host.isDeadOrDying() || (voluntarilyLeave && player.isCrouching());
        }

        @Override
        public EntityDimensions getDimensions(Pose pose, EntityDimensions currentDimensions) {
            return renderHost ? host.getDimensions(pose) : super.getDimensions(pose, currentDimensions);
        }

        @Override
        public float getEyeHeight(Pose pose, float eyeHeight) {
            return renderHost ? host.getEyeHeight(pose) : super.getEyeHeight(pose, eyeHeight);
        }
    }

    protected Instance latexPlayerHoldHuman(Player latexPlayer) {
        var instance = createInstance();
        instance.host = latexPlayer;
        instance.renderHost = true;
        return instance;
    }

    protected Instance humanControlLatexPlayer(Player human) {
        var instance = createInstance();
        instance.host = human;
        instance.renderHost = false;
        instance.voluntarilyLeave = true;
        return instance;
    }

    public static void setupLatexHoldHuman(Player latexPlayer, Player human, boolean voluntarilyLeave) {
        var instance = PlayerMover.LOCK_TO_PLAYER.get().latexPlayerHoldHuman(latexPlayer);
        instance.voluntarilyLeave = voluntarilyLeave;

        if (human instanceof PlayerDataExtension ext)
            ext.setPlayerMover(instance);
    }

    public static void setupHumanControlLatex(Player latexPlayer, Player human) {
        var instance = PlayerMover.LOCK_TO_PLAYER.get().humanControlLatexPlayer(human);

        if (latexPlayer instanceof PlayerDataExtension ext)
            ext.setPlayerMover(instance);
    }
}
