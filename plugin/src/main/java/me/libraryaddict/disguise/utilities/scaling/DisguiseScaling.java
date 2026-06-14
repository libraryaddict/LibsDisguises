package me.libraryaddict.disguise.utilities.scaling;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.libraryaddict.disguise.DisguiseConfig;

@Getter
@RequiredArgsConstructor
public class DisguiseScaling {
    public interface DisguiseScalingInternals {
        void sendTinyFigureScale(double scale);

        /**
         * Set the scale of the player to match the height of the disguise, should be ignored if 1
         *
         * @param scale
         */
        void setPlayerScale(double scale);

        /**
         * If scaling should be checked at all
         *
         * @return
         */
        boolean isScalingRelevant();

        /**
         * Used to determine if processing time should be used
         */
        boolean isTinyFigureScaleable();

        /**
         * Exposed because it has an effect on what scale the mini figure at their feet will use (It'd be scaled down further)
         */
        boolean isScalePlayerToDisguise();

        double getUnscaledHeight();

        /**
         * Returns the entity scale that's been set on the disguise watcher if scaling is relevant, or returns
         * {@link #getPlayerScaleWithoutLibsDisguises()}
         */
        double getDisguiseScale();

        /**
         * Returns the scale of the disguised entity as if LD hasn't applied any scaling attributes
         */
        double getPlayerScaleWithoutLibsDisguises();

        void setSelfDisguiseTallScaleMax(double newMax);

        double getSelfDisguiseTallScaleMax();
    }

    @Getter
    private final static double playerHeight = 1.8;
    @Getter
    private final static double maxTinyFigureHeight = 1.15;
    @Getter
    private final static double tallDisguiseAtHeight = 1.6;
    private final DisguiseScalingInternals scalingInternals;

    public double getHeightOfDisguise() {
        return scalingInternals.getUnscaledHeight() * scalingInternals.getDisguiseScale();
    }

    public void adjustScaling() {
        double playerScaleWithoutLD = scalingInternals.getPlayerScaleWithoutLibsDisguises();
        double unscaledHeight = scalingInternals.getUnscaledHeight();
        double disguiseScale = scalingInternals.getDisguiseScale();

        double scalerToMakePlayerSeePerspective = 1.0;

        if (scalingInternals.isScalePlayerToDisguise()) {
            scalerToMakePlayerSeePerspective = getHeightOfDisguise() / (playerHeight * playerScaleWithoutLD);
            scalerToMakePlayerSeePerspective = Math.min(scalerToMakePlayerSeePerspective, DisguiseConfig.getScaleSelfDisguisesMax());
            scalerToMakePlayerSeePerspective = Math.max(scalerToMakePlayerSeePerspective, DisguiseConfig.getScaleSelfDisguisesMin());
        }

        double finalPlayerHeight = playerHeight * playerScaleWithoutLD * scalerToMakePlayerSeePerspective;

        double newTinyFigureScaleMax = ((maxTinyFigureHeight / playerHeight) * finalPlayerHeight) / unscaledHeight;
        newTinyFigureScaleMax = Math.min(newTinyFigureScaleMax, Math.max(disguiseScale, playerScaleWithoutLD));

        double prevTinyFigureScaleMax = scalingInternals.getSelfDisguiseTallScaleMax();
        scalingInternals.setSelfDisguiseTallScaleMax(newTinyFigureScaleMax);

        if (!scalingInternals.isScalingRelevant()) {
            return;
        }

        if (prevTinyFigureScaleMax != newTinyFigureScaleMax) {
            double scaleToSend = scalingInternals.isTinyFigureScaleable() ? newTinyFigureScaleMax : playerScaleWithoutLD;
            scalingInternals.sendTinyFigureScale(scaleToSend);
        }

        scalingInternals.setPlayerScale(scalerToMakePlayerSeePerspective);
    }
}